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

**Notes:**
- Standard environment settings in TESCAN Brno. Sorry for not using cmake.
- When using opencv*.props file, go to corresponding settings in project and check `Use settings from parent ...`
- MSVC project is mostly left at defaults which is messy build environment.
- Results are saved into CSV file in `c:\temp\` directory
- Results are visualized by simple [gnuplot](http://www.gnuplot.info/) script files. 

Algorithm
-----------

Yao Nie and Kai-Kuang Ma 2002
Adaptive Rood Pattern Search for Fast Block Matching Estimation. 
IEEE Transactions on Image Processing, vol 11 No 12, 1442- 1447
[Link](http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.1016.9431&rep=rep1&type=pdf)

Part 1 is cost function that decreases with increases macroblock similarity. Typical size of macroblock is 16x16. Larger block decreases number of mismatches, but decreses ability to find small features. 
Cost function used is very simple sum of absolute differences between pixels (averaged and normalized).

Part 2 of algorithm to find shift between macroblocks in image1 and image2 for which the cost function is minimal. This can be done by exhaustive search limited by maximum range.

**Warning:** algorithm has range set e.g. to 40 pixels. Images have to be prealigned.

This algorithm uses diamond search pattern finding current cost and cost of N,S,W,E neighbors. If cost of center is the lowest, search is interrupted, otherwise it movesto the neighbor with lowest cost and process is repeated.

<pre>
+--X--+
|  |  |
X--X--X
|  |  |
+--X--+
</pre>

Adaptive Rood Search Pattern uses larger diamond (2x2 pixels or NxN pixels where N is distance from previous search - left neighbor). Prediction from left neighbor is added, e.g:

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

**Note:** *original algorithm written in Java (2008) uses difference of gaussian filter as edge detection prior to pattern search. It can make sense because matching can depend on brightness more than on features. Unlike DoG filter in GIMP it works with signed values and adds constant offset. 
It has just one macroblock size which is both step size and search rectangle size.*

Problems
----------

* Does not work well with images containing little details
* Does not work well with images containing steep slopes or overhangs
* Works better if images are prealigned (due to limited search differences)
* Sometimes hides the fact it does not work (it somewhat preserves features even when result is completely off)
* Sometimes it tends to use predicted vector even thou it's no longer valid
* Not using prediction does not solve previous problem. It makes program much slower and it can't converge to optimal solution when it's far zero movement. 

Advantages:
----------

* It's pretty fast 
* It works quite well with some images
* Can be used for fast initial search
* Simple implementation of cost function and diamond pattern search. ARPS makes first pass different.

Example
-----
![wood.tif](https://i.imgur.com/z5dICXk.png)
Note: uses prediction from left and top neighbor, 3 pixels step and 12x12 pixels for matching. 

  
  
  

  

