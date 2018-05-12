# Stereo Pair reconstruction

To compile with MSVS2015:

Set environment variable CVDIR e.g. to `c:\opencv\build\x64\vc14\`.
Directory structure here contains these files:

	etc/
	include/opencv
	include/opencv2
	x64/vc14/bin
	x64/vc14/lib
	LICENSE
	OpenCVConfig.cmake
	OpenCVConfig-version.cmake

To run it, set proper PATH environment
E.g. in Visual Studio:

	PATH=$(CVDIR)\x64\vc14\bin;$(PATH)

NOTE:
Standard environment settings in TESCAN Brno. 
Sorry for not using cmake.