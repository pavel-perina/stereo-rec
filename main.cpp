//! \file main.cpp  Defines the entry point for the console application.
#include "pattern_search.h"

#include <opencv2/core/core.hpp>
#include <opencv2/imgcodecs.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <string>
#include <iostream>
#include <fstream>

int main()
{
	std::string outCsvFile = "c:\\temp\\pattern-search.csv";
#if 1
	std::string inImageFile = "c:\\temp\\wood.tif";
	cv::Mat inImage = cv::imread(inImageFile, cv::IMREAD_COLOR);
	cv::Mat bgr[3];
	cv::split(inImage, bgr);
#else
	cv::Mat bgr[3];
	bgr[0] = cv::imread("c:\\temp\\stagetilt-alicona+10.5.png", cv::IMREAD_GRAYSCALE);
	bgr[2] = cv::imread("c:\\temp\\stagetilt-alicona-10.5.png", cv::IMREAD_GRAYSCALE);
#endif
	const int mbStep = 3;
	PsArray2x2<PsResult> results = motionEstimateARPS(bgr[0], bgr[2], 12, mbStep, 96);

	std::ofstream ofs;
	ofs.open(outCsvFile, std::ios_base::out | std::ios_base::trunc);
	ofs << "x\ty\tdx\tdy\tcost\tquality\tvalue\n";
	for (int r = 0; r < results.rows(); r++) {
		for (int c = 0; c < results.cols(); c++) {
			PsResult &result = results(r, c);
			int value1 = 0;
			if (bgr[0].type() == CV_8UC1)
				value1 = bgr[0].at<uint8_t>(r*mbStep, c*mbStep);
			else if (bgr[0].type() == CV_16UC1)
				value1 = bgr[0].at<uint16_t>(r*mbStep, c*mbStep);
			ofs	<< c*mbStep << "\t"
				<< r*mbStep << "\t"
				<< result.x << "\t"
				<< result.y << "\t"
				<< result.cost << "\t"
				<< result.quality << "\t"
				<< value1 << "\n";
		}
	}
	ofs.close();
	return 0;
}

