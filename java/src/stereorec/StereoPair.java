/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package stereorec;


class StereoPair {
	int imgWidth;
	int imgHeight;
	RawImage lImg;
	RawImage rImg;
		
		
	void disposeData () {
		imgWidth	= 0;
		imgHeight	= 0;
		lImg = null;
		rImg = null;
		System.gc ();		
	}
    

	public void readAnaglyph (String fileName) {
		try {
			lImg = new RawImage ();
			lImg.readFromFileChannel (fileName, "red");
			rImg = new RawImage ();
			rImg.readFromFileChannel (fileName, "green");
		} catch (Exception e) {
			e.printStackTrace ();
			disposeData ();
		}
	}

	
	public void readAnaglyph (String leftFileName, String rightFileName) {
		disposeData ();
		try {
			lImg = new RawImage ();
			lImg.readFromFile (leftFileName);
			rImg = new RawImage ();
			rImg.readFromFile (rightFileName);
		} catch (Exception e) {
			e.printStackTrace ();
			disposeData ();
			return;
		}
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
	static public double costFuncMAD (RawImage img1, RawImage img2, int x1, int y1, int x2, int y2, int n) {
		int err = 0;
		for (int i = 0; i < n; i++)
			for (int j = 0; j < n; j++)
				err += Math.abs (img1.getPixel(x1 + i, y1 + j) - img2.getPixel(x2 + i, y2 + j));
		return (double) err / (n * n);
	}

	// temporary class?
	class IntVec {
		public int x = 0, y = 0;
		public double minMAD;
	}

	/**
	 * For debugging purposes - saves image with motion vectors
	 * @param w image width
	 * @param h image heigth
	 * @param mbSize macro block size
	 * @param vectors two dimensional array of macro block motion vectors
	 */
	public void saveVectImage (int w, int h, int mbSize, IntVec[][] vectors, String fileNameSuffix) {
		// TODO: save debugging image
		RawImage testImage = new RawImage (w, h, 8);
		// draw grid (assuming mbSize is power of two)
		final int mask = mbSize - 1;
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				if ((x & mask) == 0 || (y & mask) == 0)
					testImage.setPixel (x, y, 64);
				else
					testImage.setPixel (x, y, 0);
			}
		}
		// draw vectors		
		for (int y = 0; y < h / mbSize; y++) {
			for (int x = 0; x < w / mbSize; x++) {
				IntVec v = vectors[x][y];
				IntVec pt = new IntVec ();
				pt.x = x * mbSize + mbSize / 2;
				pt.y = y * mbSize + mbSize / 2;
				if (v == null)
					continue;
				if (v.x == 0 && v.y == 0) {
					testImage.setPixel (pt.x, pt.y, 128);
					continue;				
				}
				if (Math.abs (v.x) > Math.abs (v.y)) {
					// draw mostly horizontal line
					int dx = v.x / Math.abs(v.x);
					float dy = v.y / (float) Math.abs (v.x);
					int i = 0;
					while (i <= Math.abs (v.x) && pt.x > 0 && pt.y > 0 && pt.x < w && pt.y < h) {
						testImage.setPixel (pt.x + dx * i, pt.y + Math.round (dy * i), 255);
						i++;
					}
				} else {
					// mostly vertical line
					int dy = v.y / Math.abs(v.y);
					float dx = v.x / (float) Math.abs (v.y);
					int i = 0;
					while (i <= Math.abs (v.y) && pt.x > 0 && pt.y > 0 && pt.x < w && pt.y < h) {
						testImage.setPixel (pt.x + Math.round (dx * i), pt.y + dy * i, 255);
						i++;
					}
				}
			}
		}
		try {
			testImage.saveToFile ("c:\\temp\\arps_vectors" + fileNameSuffix + ".png");
		} catch (Exception e) {
			e.printStackTrace ();
		}
	}

	/**
	 * Saves image with displacement in X-axis
	 * @param w image widht
	 * @param h image height
	 * @param mbSize macro block size
	 * @param vectors vectors two dimensional array of macro block motion vectors
	 */
	public void saveXDisImage (int w, int h, int mbSize, IntVec[][] vectors, String fileNameSuffix) {
		final int mbArrWidth	= w / mbSize;
		final int mbArrHeight	= h / mbSize;		
		RawImage magImage		= new RawImage (mbArrWidth, mbArrHeight, 8);
		for (int y = 0; y < mbArrHeight; y++) {
			for (int x = 0; x < mbArrWidth; x++) {
				if (vectors[x][y] != null)
					magImage.setPixel (x, y, 128 + vectors[x][y].x);
			}
		}		
		try {
			magImage.saveToFile ("c:\\temp\\arps_heightmap" + fileNameSuffix + ".png");
		} catch (Exception e) {
			e.printStackTrace ();
		}
	}

	/**
	 * Saves image with error costs
	 * @param w image widht
	 * @param h image height
	 * @param mbSize macro block size
	 * @param vectors vectors two dimensional array of macro block motion vectors
	 */	
	public void saveMADImage (int w, int h, int mbSize, IntVec[][] vectors, String fileNameSuffix) {
		final int mbArrWidth	= w / mbSize;
		final int mbArrHeight	= h / mbSize;		
		RawImage madImage		= new RawImage (mbArrWidth, mbArrHeight, 16);
		for (int y = 0; y < mbArrHeight; y++) {
			for (int x = 0; x < mbArrWidth; x++) {
				if (vectors[x][y] != null)
					madImage.setPixel (x, y,  Math.min ((int) vectors[x][y].minMAD * 64, 255 * 256) );
			}
		}		
		try {
			madImage.saveToFile ("c:\\temp\\arps_difference" + fileNameSuffix + ".png");
		} catch (Exception e) {
			e.printStackTrace ();
		}		
	}

	/**
	 * Pyramidal adaptive root pattern search
	 * my contribution to adaptive root pattern search :-)
	 */
	
	public IntVec[][] predictFromUpperLevel (final IntVec upper[][], final int w, final int h, final int upperMBSize) {
		final int upArrWidth = w / upperMBSize, upArrHeight = h / upperMBSize;		
		final int mbSize = upperMBSize / 2;
		final int mbArrWidth = w / mbSize, mbArrHeight = h / mbSize;
		IntVec result[][] = new IntVec[mbArrWidth][mbArrHeight];
		
		for (int mbY = 0; mbY < mbArrHeight; mbY++) {
			for (int mbX = 0; mbX < mbArrWidth;  mbX++) {
				result[mbX][mbY] = new IntVec ();
				int upX = mbX / 2;
				int upY = mbY / 2;
				// use bilinear interpolation where possible
				if ((mbX > 0) && (mbY > 0) && (mbX < upArrWidth * 2 - 1) && (mbY < upArrHeight*2-1)) {
					
					// TODO: put some code here ...					
					// this is preliminary one ...
					result[mbX][mbY].x  = upper[upX][upY].x;
					result[mbX][mbY].y  = upper[upX][upY].y;
				}
				
				if ((mbX == 0) || (mbY == 0) || (mbX == upArrWidth * 2 - 1) || (mbY == upArrHeight*2-1)) {
					// copy motion vector from upper pyramid on it's edges
					result[mbX][mbY].x  = upper[upX][upY].x;
					result[mbX][mbY].y  = upper[upX][upY].y;
				} else if ((mbX == upArrWidth * 2) && (mbY == upArrHeight * 2)) {
					// extrapolate motion vector at bottom right corner
					result[mbX][mbY].x  = upper[upX - 1][upY-1].x;
					result[mbX][mbY].y  = upper[upX - 1][upY-1].y;
				} else if (mbX == upArrWidth * 2) {
					// extrapolate motion vector on right edge
					result[mbX][mbY].x  = upper[upX - 1][upY].x;
					result[mbX][mbY].y  = upper[upX - 1][upY].y;					
				} else {
					// extrapolate motion vector on bottom edge
					result[mbX][mbY].x  = upper[upX][upY - 1].x;
					result[mbX][mbY].y  = upper[upX][upY - 1].y;					
				}
				
				
				
			}
		}
		// fill edges from upper level values
		
		
		// do bilinear interpolation from upper level of pyramid
		final int k1 = 1, k2 = 3, d = 4;
		return null;
	}
	
	
	/**
	 * 
	 * @param img1
	 * @param img2
	 * @param mbSize
	 * @param p
	 */		
	 public IntVec[][] motionEstARPS (RawImage img1, RawImage img2, int mbSize, int p) {
		final int w = img1.getWidth (), h = img1.getHeight ();		
		final int mbArrWidth = w / mbSize, mbArrHeight = h / mbSize;
		IntVec vectors[][]	= new IntVec[mbArrWidth][mbArrHeight];
		double costs[]		= new double[6];
		int checkMatrix[][] = new int[2*p+1][2*p+1];
		int stepSize;
		int maxIndex;
		IntVec ldsp[] = new IntVec[6]; // large diamond search pattern (plus predicted vector)
		IntVec sdsp[] = new IntVec[5]; // small diamond search pattern
		// initalize all vector arrays ... grrr :-(
		for (int i = 0; i < ldsp.length; i++) 
			ldsp[i] = new IntVec ();
		for (int i = 0; i < sdsp.length; i++) 
			sdsp[i] = new IntVec ();
		
		sdsp[0].x = 0;	sdsp[0].y = -1;
		sdsp[1].x = -1;	sdsp[1].y = 0;
		sdsp[2].x = 0;	sdsp[2].y = 0;
		sdsp[3].x = 1;	sdsp[3].y = 0;
		sdsp[4].x = 0;	sdsp[4].y = 1;
		
		int mbX = 0, mbY = 0;
		for (int i = 0; mbY < mbArrHeight; i += mbSize, mbY++) {
			mbX = 0;
			System.out.println ("Line: " + i);
			for (int j = 0; mbX < mbArrWidth; j += mbSize, mbX++) {
				int x = j;
				int y = i;

				// initialize costs for diamond
				for (int k = 0; k < costs.length; k++)
					costs[k] = Double.MAX_VALUE;
				costs[2] = costFuncMAD (img1, img2, j, i, j, i, mbSize);
				
				// zerofill checkmatrix
				for (int m = 0; m < 2*p+1; m++)
					for (int n = 0; n < 2*p+1; n++)
						checkMatrix[m][n] = 0;
				checkMatrix[p][p] = 1;
				
				maxIndex = 4;
				if (j == 0) {
					// no prediction
					stepSize = 2;
				} else {
					// predicted movement vector from left neighbour
					IntVec pm = vectors[mbX-1][mbY];	
					stepSize = Math.max (Math.abs (pm.x), Math.abs (pm.y));
					if ((pm.x != 0) && (pm.y != 0)) {
						maxIndex = 5;
						ldsp[5] = pm;
					}
				}				
				ldsp[0].x = 0;			ldsp[0].y = -stepSize;
				ldsp[1].x = -stepSize;	ldsp[1].y = 0;
				ldsp[2].x = 0;			ldsp[2].y = 0;
				ldsp[3].x = stepSize;	ldsp[3].y = 0;
				ldsp[4].x = 0;			ldsp[4].y = stepSize;
				if (stepSize > 0 ) {
					for (int k = 0; k <= maxIndex; k++) {
						int refBlkX = x + ldsp[k].x;
						int refBlkY = y + ldsp[k].y;
						if ( // outside of image boundary or center point already calculated
							(refBlkX < 0) || ((refBlkX + mbSize) > w) ||
							(refBlkY < 0) || ((refBlkY + mbSize) > h) ||
							(k == 2) 
						)
							continue;
						costs[k] = costFuncMAD (img1, img2, j, i, refBlkX, refBlkY, mbSize);
						checkMatrix [ldsp[k].x + p][ldsp[k].y + p] = 1;
					}
				}
				int point = 0;
				double minCost = Double.MAX_VALUE;
				for (int k = 0; k <= maxIndex; k++) {
					if (costs[k] < minCost) {
						minCost = costs[k];
						point = k;
					}
				}
				
				x += ldsp[point].x;
				y += ldsp[point].y;
				for (int k = 0; k < costs.length; k++)
					costs[k] = Double.MAX_VALUE;
				costs[2] = minCost;
				boolean done = false;
				while (!done) {
					for (int k = 0; k < 5; k++) {
						int refBlkX = x + sdsp[k].x;
						int refBlkY = y + sdsp[k].y;
						if ( // outside of image boundary or center point already calculated
							(refBlkX < 0) || ((refBlkX + mbSize) > w) ||
							(refBlkY < 0) || ((refBlkY + mbSize) > h) ||
							(k == 2) 
						)
							continue;
						// motion vector went beyond search boundary
						if (refBlkX < j-p || refBlkX > j+p || refBlkY < i-p || refBlkY > i+p)
							continue;
						// already checked
						if (checkMatrix[refBlkX-j+p][refBlkY-i+p] == 1)
							continue;
						costs[k] = costFuncMAD (img1, img2, j, i, refBlkX, refBlkY, mbSize);
						checkMatrix[refBlkX-j+p][refBlkY-i+p] = 1;						
					}
					minCost = Double.MAX_VALUE;
					for (int k = 0; k <= maxIndex; k++) {
						if (costs[k] < minCost) {
							minCost = costs[k];
							point = k;
						}
					}
					
					if (point == 2) {
						done = true;
					} else {
						x += sdsp[point].x;
						y += sdsp[point].y;
						for (int k = 0; k < costs.length; k++)
							costs[k] = Double.MAX_VALUE;
						costs[2] = minCost;
					}
					
				} // while (!done)
				vectors[mbX][mbY] = new IntVec ();
				vectors[mbX][mbY].x = x - j;
				vectors[mbX][mbY].y = y - i;
				vectors[mbX][mbY].minMAD = costs[2];
			} // for j (rows)
			mbX = mbY; // useless
		} // for i (columns)
				
		
      		
		// The rest of code is for debugging purposes only
		try {			
			saveVectImage (w, h, mbSize, vectors, "");
			saveXDisImage (w, h, mbSize, vectors, "");
			saveMADImage  (w, h, mbSize, vectors, "");	

			//img1.saveToFile ("c:\\temp\\arps_img1.png");
			//img2.saveToFile ("c:\\temp\\arps_img2.png");			 
 
		} catch (Exception e) {
			e.printStackTrace ();
		}
		return vectors;
	}
	 
	 public IntVec[][] motionEstARPSMod (RawImage img1, RawImage img2, int mbSize, int p, IntVec predVect[][], int level) {
		final int w = img1.getWidth (), h = img1.getHeight ();		
		final int mbArrWidth = w / mbSize, mbArrHeight = h / mbSize;
		IntVec vectors[][]	= new IntVec[mbArrWidth][mbArrHeight];
		double costs[]		= new double[6];
		int checkMatrix[][] = new int[2*p+1][2*p+1];
		int stepSize;
		int maxIndex;
		IntVec ldsp[] = new IntVec[6]; // large diamond search pattern (plus predicted vector)
		IntVec sdsp[] = new IntVec[5]; // small diamond search pattern
		// initalize all vector arrays ... grrr :-(
		for (int i = 0; i < ldsp.length; i++) 
			ldsp[i] = new IntVec ();
		for (int i = 0; i < sdsp.length; i++) 
			sdsp[i] = new IntVec ();
		
		sdsp[0].x = 0;	sdsp[0].y = -1;
		sdsp[1].x = -1;	sdsp[1].y = 0;
		sdsp[2].x = 0;	sdsp[2].y = 0;
		sdsp[3].x = 1;	sdsp[3].y = 0;
		sdsp[4].x = 0;	sdsp[4].y = 1;
		
		int mbX = 0, mbY = 0;
		for (int i = 0; mbY < mbArrHeight; i += mbSize, mbY++) {
			mbX = 0;
			System.out.println ("Line: " + i);
			for (int j = 0; mbX < mbArrWidth; j += mbSize, mbX++) {
				int x = j;
				int y = i;

				// initialize costs for diamond
				for (int k = 0; k < costs.length; k++)
					costs[k] = Double.MAX_VALUE;
				costs[2] = costFuncMAD (img1, img2, j, i, j, i, mbSize);
				
				// zerofill checkmatrix
				for (int m = 0; m < 2*p+1; m++)
					for (int n = 0; n < 2*p+1; n++)
						checkMatrix[m][n] = 0;
				checkMatrix[p][p] = 1;
				
				// predicted movement vector from upper level
				maxIndex = 4;
				if (predVect != null) {
					IntVec pm = predVect[mbX][mbY];	
					stepSize = Math.max (Math.abs (pm.x), Math.abs (pm.y));
					if ((pm.x != 0) && (pm.y != 0)) {
						maxIndex = 5;
						ldsp[5] = pm;
					}
				// prediction from left neighbour	
				} else {
					if (j == 0) {
						// no prediction
						stepSize = 2;
					} else {
						// predicted movement vector from left neighbour
						IntVec pm = vectors[mbX-1][mbY];	
						stepSize = Math.max (Math.abs (pm.x), Math.abs (pm.y));
						if ((pm.x != 0) && (pm.y != 0)) {
							maxIndex = 5;
							ldsp[5] = pm;
						}
					}										
				}
				
				
				ldsp[0].x = 0;			ldsp[0].y = -stepSize;
				ldsp[1].x = -stepSize;	ldsp[1].y = 0;
				ldsp[2].x = 0;			ldsp[2].y = 0;
				ldsp[3].x = stepSize;	ldsp[3].y = 0;
				ldsp[4].x = 0;			ldsp[4].y = stepSize;
				if (stepSize > 0 ) {
					for (int k = 0; k <= maxIndex; k++) {
						int refBlkX = x + ldsp[k].x;
						int refBlkY = y + ldsp[k].y;
						if ( // outside of image boundary or center point already calculated
							(refBlkX < 0) || ((refBlkX + mbSize) > w) ||
							(refBlkY < 0) || ((refBlkY + mbSize) > h) ||
							(k == 2) 
						)
							continue;
						costs[k] = costFuncMAD (img1, img2, j, i, refBlkX, refBlkY, mbSize);
						checkMatrix [ldsp[k].x + p][ldsp[k].y + p] = 1;
					}
				}
				int point = 0;
				double minCost = Double.MAX_VALUE;
				for (int k = 0; k <= maxIndex; k++) {
					if (costs[k] < minCost) {
						minCost = costs[k];
						point = k;
					}
				}
				
				x += ldsp[point].x;
				y += ldsp[point].y;
				for (int k = 0; k < costs.length; k++)
					costs[k] = Double.MAX_VALUE;
				costs[2] = minCost;
				boolean done = false;
				while (!done) {
					for (int k = 0; k < 5; k++) {
						int refBlkX = x + sdsp[k].x;
						int refBlkY = y + sdsp[k].y;
						if ( // outside of image boundary or center point already calculated
							(refBlkX < 0) || ((refBlkX + mbSize) > w) ||
							(refBlkY < 0) || ((refBlkY + mbSize) > h) ||
							(k == 2) 
						)
							continue;
						// motion vector went beyond search boundary
						if (refBlkX < j-p || refBlkX > j+p || refBlkY < i-p || refBlkY > i+p)
							continue;
						// already checked
						if (checkMatrix[refBlkX-j+p][refBlkY-i+p] == 1)
							continue;
						costs[k] = costFuncMAD (img1, img2, j, i, refBlkX, refBlkY, mbSize);
						checkMatrix[refBlkX-j+p][refBlkY-i+p] = 1;						
					}
					minCost = Double.MAX_VALUE;
					for (int k = 0; k <= maxIndex; k++) {
						if (costs[k] < minCost) {
							minCost = costs[k];
							point = k;
						}
					}
					
					if (point == 2) {
						done = true;
					} else {
						x += sdsp[point].x;
						y += sdsp[point].y;
						for (int k = 0; k < costs.length; k++)
							costs[k] = Double.MAX_VALUE;
						costs[2] = minCost;
					}
					
				} // while (!done)
				vectors[mbX][mbY] = new IntVec ();
				vectors[mbX][mbY].x = x - j;
				vectors[mbX][mbY].y = y - i;
				vectors[mbX][mbY].minMAD = costs[2];
			} // for j (rows)
			mbX = mbY; // useless
		} // for i (columns)
				
		
      		
		// The rest of code is for debugging purposes only
		try {			
			saveVectImage (w, h, mbSize, vectors, "_level" + String.valueOf (level));
			saveXDisImage (w, h, mbSize, vectors, "_level" + String.valueOf (level));
			saveMADImage  (w, h, mbSize, vectors, "_level" + String.valueOf (level));	

			//img1.saveToFile ("c:\\temp\\arps_img1_level" + String.valueOf (level) + ".png");
			//img2.saveToFile ("c:\\temp\\arps_img2_level" + String.valueOf (level) + ".png");			 
 
		} catch (Exception e) {
			e.printStackTrace ();
		}
		return vectors;
	}

	 
	void computeDisplacement () {
		// TODO: scale input images down -> can be ran in multiple threads
		// perform diference of gaussians filtering on image pyramid
		// find correlation between images (by computing or interpolation)
	}
		
		
		
		
	}
	