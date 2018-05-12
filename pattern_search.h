#pragma once

#include "pattern_search_result.h"

#include <memory>
#include <string>
#include <vector>

#include <opencv2\core\mat.hpp>
// forward declaration(s):
class PatternSearchPriv;

//////////////////////////////////////////////////////////


class PatternSearch final
{
public:
	
	//! \brief Constructor
	PatternSearch();

	//! \brief Esimate motion using ARPS algorithm.
	//! \param[in] img1 First image
	//! \param[in] img2 Second image
	//! \param[in] mbSize Macroblock size in pixels (used by similarity check)
	//! \param[in] mbStep Macroblock step in pixels (defines output resulotion)
	//! \returns 2D array of motion vectors with additional information (cost, quality)
	PsResultMatrix motionEstimateARPS(const cv::Mat &img1, const cv::Mat &img2, int mbSize = 16, int mbStep = 16, int maxDistance = 64);

private:

	std::unique_ptr<PatternSearchPriv> p;



};