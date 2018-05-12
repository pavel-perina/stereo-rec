/////////////////////////////////////////////////////////////////////////////
//! \file	stereo_pair.cpp
//! \brief	Stereo Pair Reconstruction (proof of concept)
//! \author Pavel Perina <pavel.perina@tescan.com>, TESCAN 3DIM s.r.o. 
//! \date	May 2018
//! \details
//!
//! *Papers:*
//!
//! Yao Nie and Kai-Kuang Ma 2002
//! Adaptive Rood Pattern Search for Fast Block Matching Estimation. 
//! IEEE Transactions on Image Processing, vol 11 No 12, 1442- 1447
//! http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.1016.9431&rep=rep1&type=pdf
//!
//! (xvid libbrary?)
//!
//! *Abrreviations*:
//! |      |                              |
//! |------|------------------------------|
//! | ARPS | Adaptive Root Pattern Search |
//! | DS   | Diamond Search               |
//! | MAD  | Mean Absolute Difference     |
//! | MB   | Macro block                  |
//! | MV   | Motion vector                |
//! | SAD  | Sum of Absolute Differences  |
//!
//! OpenCV Matrix Types
//! http://ninghang.blogspot.cz/2012/11/list-of-mat-type-in-opencv.html


/////////////////////////////////////////////////////////////////////////////

#include "pattern_search.h"

#include <opencv2/core/core.hpp>
#include <limits>
#include <cassert>
#include <array>
#include <iostream>

/////////////////////////////////////////////////////////////////////////////

class PatternSearchPriv
{
public:
//	cv::Mat lImg;
//	cv::Mat rImg;

	void disposeData();

	//! \brief Estimate motion by Adaptive Rood Pattern Search algorithm
	//! \param[in] img1 First image
	//! \param[in] img2 Second image
	//! \param[in] mbSize Macro block size
	//! \param[in] mbStep Macro block step (equal to mbSize in video encoding)
	//! \param[in] maxDist Maximum search distance
	PsResultMatrix motionEstimateARPS(const cv::Mat &img1, const cv::Mat &img2, int mbSize, int mbStep, int maxDist);
private:

};


void PatternSearchPriv::disposeData()
{

}

/**
* Computes the Mean Absolute Difference (MAD) for the given two square blocks (lower is better)
* @param[in] img1 current Image
* @param[in] img2 reference Image
* @param[in] x1 left x coordinate of img1 block
* @param[in] y1 top y coordinate of img1 block
* @param[in] x2 left x coordinate of img2 block
* @param[in] y2 top y coordinate of img2 block
* @param[in] n side of the square image block
* @return The MAD for the two blocks
*/
static double costFuncMAD(const cv::Mat &img1, const cv::Mat &img2, int x1, int y1, int x2, int y2, int n)
{
	int err = 0;
	for (int i = 0; i < n; i++) {
		for (int j = 0; j < n; j++) {
			err += abs(img1.at<uint16_t>(y1 + j, x1 + i) - img2.at<uint16_t>(y2 + j, x2 + i));
		}
	}
	return (double)err / (n * n);
}

//! \brief Initialize diamond search pattern in array
//! \param[in] dsp Diamond Search Pattern (size >= 5)
//! \param[in] diamondSize size of diamond
static void initDiamondSearchPattern(std::array<PsResult, 6> &dsp, int diamondSize) 
{
	const int s = diamondSize;
	dsp[0].x = 0;	dsp[0].y = -s;
	dsp[1].x = -s;	dsp[1].y = 0;
	dsp[2].x = 0;	dsp[2].y = 0;
	dsp[3].x = s;	dsp[3].y = 0;
	dsp[4].x = 0;	dsp[4].y = -s;
}


PsResultMatrix PatternSearchPriv::motionEstimateARPS(const cv::Mat &img1, const cv::Mat &img2, int mbSize, int mbStep, int p)
{
	if (   img1.rows != img2.rows
		|| img1.cols != img2.cols)
	{
		assert(false);
		return PsResultMatrix();
	}

	const int rows = img1.rows;
	const int cols = img1.cols;
	// Macroblock array r
	const int mbArrRows = rows / mbStep;
	const int mbArrCols = cols / mbStep;
	PsResultMatrix result(mbArrCols, mbArrRows);
	constexpr double maxCost = std::numeric_limits<double>::max();


	// Rows
	for (int i = 0, mbY = 0;  mbY < mbArrRows;  i += mbStep, mbY++) {
		std::cout << "Line: " << i << std::endl;
		// Columns
		for (int j = 0, mbX = 0;  mbX < mbArrCols;  j += mbStep, mbX++) {
			// X and Y are current search position
			int x = j;
			int y = i;


			// Prepare LDPS
			std::array<double, 6> costs;
			costs.fill(maxCost);
			costs[2] = costFuncMAD(img1, img2, j, i, j, i, mbSize);

			// Matrix of already checked motion vectors.
			cv::Mat checkMatrix(2 * p + 1, 2 * p + 1, CV_64FC1, maxCost);
			checkMatrix.at<double>(p, p) = costs[2];

			bool done = false;
			int  pass = 0;
			do {
				// -- Initialize diamond search pattern, dsp[5] is optional prediction --
				std::array<PsResult, 6> dsp;
				// max index of search pattern
				int psMaxIndex = 4;

				if (pass == 0) {
					// Initialize large diamond pattern search
					int stepSize;
					if (j == 0) {
						// first column, no prediction
						stepSize = 2;
					}
					else {
						PsResult &predicted = result(mbX - 1, mbY);
						stepSize = std::max(abs(predicted.x), abs(predicted.y));
						if ((predicted.x != 0) && (predicted.y != 0)) {
							// we have predictiction from left block
							psMaxIndex = 5;
							dsp[5] = predicted;
						}
					}
					initDiamondSearchPattern(dsp, stepSize);
				}
				else {
					// pass != 0
					// Initialize small diamond pattern search
					initDiamondSearchPattern(dsp, 1);

				}

				// -- Perform diamond pattern search ---------------------
				for (int k = 0; k <= psMaxIndex; k++) {
					int refBlkX = x + dsp[k].x;
					int refBlkY = y + dsp[k].y;

					// outside of image boundary or center point already calculated
					if (
						(refBlkX < 0) || ((refBlkX + mbSize) > cols) ||
						(refBlkY < 0) || ((refBlkY + mbSize) > rows) ||
						(k == 2)
						)
						continue;

					if (pass != 0) {
						// motion vector went beyond search boundary
						if (refBlkX < j - p || refBlkX > j + p || refBlkY < i - p || refBlkY > i + p)
							continue;
						// already checked
						if (checkMatrix.at<double>(refBlkY - i + p, refBlkX - j + p) != maxCost)
							continue;
					}

					// Get cost
					double cost = costFuncMAD(img1, img2, j, i, refBlkX, refBlkY, mbSize);
					costs[k] = cost;
					checkMatrix.at<double>(refBlkY - i + p, refBlkX - j + p) = cost;

				}

				// -- Find minimal cost ----
				double minCost = costs[0];
				int minCostIndex = 0;
				for (int k = 1; k <= psMaxIndex; k++) {
					if (costs[k] < minCost) {
						minCost = costs[k];
						minCostIndex = k;
					}
				}
				// Move x,y to point with the best cost
				x += dsp[minCostIndex].x;
				y += dsp[minCostIndex].y;

				// never abort on first pass, because it's large search pattern
				// and we want to continue
				if (pass != 0 && minCostIndex == 2) {
					done = true;
				}
				else {
					// reset DSP costs and proceed
					costs.fill(maxCost);
					costs[2] = minCost;
					++pass;
				}
			} while (!done);

			result(mbY, mbX).x = x - j;
			result(mbY, mbX).y = y - i;
			result(mbY, mbX).cost = costs[2];

			// EXPERIMENTAL 
			// Quality measurement
			double	worst   = costs[0], 
					best2nd = costs[0];
			for (int k = 1; k < 5; k++) {
				if (k == 2)
					continue;
				if (costs[k] > worst)
					worst = costs[k];
				if (costs[k] < best2nd)
					best2nd = costs[k];
			}
			result(mbY, mbX).quality = best2nd - costs[2];
			
		} // for columns
	} // for rows

	return result;
}


/////////////////////////////////////////////////////////////////////////////

PatternSearch::PatternSearch()
	: p( std::make_unique<PatternSearchPriv>() )
{
}


PsResultMatrix PatternSearch::motionEstimateARPS(const cv::Mat & img1, const cv::Mat & img2, int mbSize, int mbStep, int maxDistance)
{
	return p->motionEstimateARPS(img1, img2, mbSize, mbStep, maxDistance);
}

