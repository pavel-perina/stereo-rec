Stereo Pair reconstruction
=====================

To build and run with MSVS2015
--------------------

Set environment variable CVDIR e.g. to `c:\opencv\build\`.
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

*Notes:*
- Standard environment settings in TESCAN Brno. 
  Sorry for not using cmake.
- When using opencv*.props file, go to corresponding
  settings in project and check use settings from parent
  
Algorithm
-----------

Part 1 is cost function that decreases with increases macroblock similarity.
Typical size of macroblock is 16x16. Larger block decreases number of mismatches,
but decreses ability to find small features. 

Cost function used is very simple difference between pictures (averaged and normalized).

Part 2 of algorithm to find shift between macroblocks in image1 and image2 for
which cost function is minimal. This can be done by exhaustive search limited
by maximum range.

*Warning:* algorithm has range set e.g. to 40 pixels. Images have to be prealigned.

This algorithm uses diamond search pattern finding current cost and cost of N,S,W,E
neighbors. If cost of center is the lowest, search is interrupted, otherwise it moves
to the neighbor with lowest cost and process is repeated.

<pre>
+--X--+
|  |  |
X--X--X
|  |  |
+--X--+
</pre>

Adaptive Rood Search Pattern uses larger diamond (2x2 pixels or NxN pixels where
N is distance from previous search - left neighbor). Prediction from left neighbor
is added, e.g:

<pre>
+++++0+++++++
+++++++++++++
+++++++++++++
1++++2++++3++
+++++++++++++
++++++++++5++
+++++4+++++++
</pre>

Note: pixel 5 is from previous search

Algorithms masks already checked pixels.

Note: original algorithm written in Java (2008) uses difference of gaussian filter
as edge detection prior to pattern search. It can make sense because matching can
depend on brightness more than on features. 

  
  
  

  

