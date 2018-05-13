//! \file	pattern_search.h
//! \author Pavel Perina <pavel.perina@tescan.com>, TESCAN 3DIM s.r.o. 
//! \date	May 2018

#pragma once

#include "pattern_search_result.h"

#include <opencv2\core\mat.hpp>

//! \brief Esimate motion using Adaptive Rood Pattern Search algorithm (ARPS).
//! \param[in] img1 First image
//! \param[in] img2 Second image
//! \param[in] mbSize Macroblock size in pixels (used by similarity check)
//! \param[in] mbStep Macroblock step in pixels (defines output resulotion)
//! \param[in] maxDistance Maximum search distance
//! \returns 2D array of motion vectors with additional information (cost, quality)
extern PsArray2x2<PsResult> motionEstimateARPS(const cv::Mat &img1, const cv::Mat &img2, int mbSize = 16, int mbStep = 16, int maxDistance = 64);

