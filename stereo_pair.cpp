///////////////////////////////////////////////////////////////////////////////////
//! \file	stereo_pair.cpp
//! \brief	Stereo Pair Reconstruction (proof of concept)
//! \author Pavel Perina <pavel.perina@tescan.com>, TESCAN 3DIM s.r.o. 
//! \date	May 2018
///////////////////////////////////////////////////////////////////////////////////

#include "stereo_pair.h"

#include <limits>

#include <opencv2/core/core.hpp>


struct IntVec {
	int x;         //!< \brief x-coordinate  
	int y;		   //!< \brief y-coordinate 
	double minMAD; //!< \brief minimal Mean Absolute Difference

	IntVec()
		: x(0)
		, y(0)
		, minMAD(std::numeric_limits<double>::max())
	{
	}
	explicit IntVec(int _x, int _y)
		: x(_x)
		, y(_y)
		, minMAD(std::numeric_limits<double>::max())
	{
	}
};

class StereoPairPriv
{
public:
//	cv::Mat lImg;
//	cv::Mat rImg;

	void disposeData();

	//! \brief Estimate motion by Adaptive Root Pattern Search algorithm
	cv::Mat motionEstimateARPS();
};


void StereoPairPriv::disposeData()
{

}


/**
* Computes the Mean Absolute Difference (MAD) for the given two square blocks
* @param img1 current Image
* @param img2 reference Image
* @param x1 left x coordinate of img1 block
* @param y1 top y coordinate of img1 block
* @param x2 left x coordinate of img2 block
* @param y2 top y coordinate of img2 block
* @param n side of the square image block
* @return The MAD for the two blocks
*/
static double costFuncMAD(const cv::Mat &img1, const cv::Mat &img2, int x1, int y1, int x2, int y2, int n) {
	int err = 0;
	for (int i = 0; i < n; i++)
		for (int j = 0; j < n; j++)
			err += abs(img1.at<uint16_t>(y1 + j, x1 + i) - img2.at<uint16_t>(y2 + j, x2 + i));
	return (double)err / (n * n);
}

//////////////////////////////////////////////////

StereoPair::StereoPair()
	: p( std::make_unique<StereoPairPriv>() )
{
}


bool StereoPair::readAnaglyph(const std::string &fileNameUtf8)
{
	bool result = true;
	return result;
}