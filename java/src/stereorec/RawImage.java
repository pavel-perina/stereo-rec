/* RawImage.java */

package stereorec;

import java.awt.image.BufferedImage;
import java.io.IOException;

class RawImage {
	private int width	= 0;
	private int height	= 0;
	public int data[] = null; // int is used to perform computations in 16b unsigned
	public int depth = 8;
	// TODO: 16bit flag, readFrom (string fileName), constructor for 16b, toBufferedImage for 16b

	// constructors
	public RawImage () {		
	}
	
	public RawImage (int width, int height, int depth) {
		this.width	= width;
		this.height	= height;
		this.depth	= depth;
		data = new int[width * height];
	}

	
	public RawImage (RawImage copyFrom) {
		width	= copyFrom.getWidth ();
		height	= copyFrom.getHeight ();
		depth	= copyFrom.depth;
		data	= new int[width * height];
		System.arraycopy (copyFrom.data, 0, data, 0, data.length);
	}
	
	
	public RawImage (int width, int height, int data[], int depth) {
		this.width	= width;
		this.height = height;
		this.data   = new int[width * height];
		this.depth	= depth;
		System.arraycopy (data, 0, this.data, 0, data.length);
	}


	public int getWidth () {
		return width;
	}

	
	public int getHeight () {
		return height;
	}
	
	
	public int getDepth () {
		return depth;
	}

	
	public void setPixel (int x, int y, int value) {
		data[y * width + x] = value;
	}

	
	public int getPixel (int x, int y) {
		return data[y * width + x];
	}

	
	public void dispose () {
		width	= 0;
		height	= 0;
		data	= null;				
	}

	
	// similar to CopyRegion in VegaTC
	void copyRegionTo (int srcX, int srcY, int dstX, int dstY, int width, int height, RawImage dstImage) {
		// TODO: some overflow tests
		for (int y = 0; y < height ; y++) {
			int srcOffset = (srcY + y) * width + srcX;
			int dstOffset = (dstY + y) * dstImage.getWidth() + dstX; 						
			for (int x = 0; x < width; x++)
				dstImage.data[dstOffset++] = data[srcOffset++];
		}					
	}

	
	// almost like RevertEdges in VegaTC
	public RawImage revertEdges (int edgeSize) {
		int i, x, y;
		RawImage dstImage = new RawImage (width + edgeSize * 2, height + edgeSize * 2, depth);
		copyRegionTo (0 ,0, edgeSize, edgeSize, width, height, dstImage);

		for (i = 0; i < edgeSize; i++) {
			for (x = edgeSize; x < width + edgeSize; x++) {
				// vrsek
				dstImage.setPixel (x, i, dstImage.getPixel (x, 2 * edgeSize - i));
				// spodek
				dstImage.setPixel(x, height + 2 * edgeSize - 1 - i, dstImage.getPixel(x, height - 1 + i));
			}			
		}

		for (y = edgeSize; y < height + edgeSize; y++) {
			for (i = 0; i < edgeSize; i++) {
				// levy
				dstImage.setPixel (i, y, dstImage.getPixel (2 * edgeSize - i, y));					
				// pravy
				dstImage.setPixel (width + 2 * edgeSize - 1 - i, y, dstImage.getPixel (width - 1 + i, y));
			}
		}		

		return dstImage;			
	}


	public RawImage cropEdges (int edgeSize) {
		RawImage dstImage = new RawImage (width - edgeSize * 2, height - edgeSize * 2, depth);
		copyRegionTo (edgeSize, edgeSize, 0, 0, dstImage.getWidth (), dstImage.getHeight (), dstImage);
		return dstImage;			
	}

	
	// like VegaTC
	public RawImage gaussBlur (double radius) {
		int halfLen = (int) Math.ceil (radius * 3.0);
		int len		= halfLen * 2 + 1;
		int x, y, i;
		RawImage bigSrc = this.revertEdges (halfLen);

		// FIR impulse response
		double h[] = new double[len];
		double sum_h = 0.0;
		for (i = 0; i < len; i++) {
			h[i] = Math.exp (-0.5 * (Math.pow (((double) (i - halfLen) / radius), 2)));
			sum_h += h[i];
		}
		for (i = 0; i < len; i++)
			h[i] /= sum_h;

		// separabilni filtrace
		// omezeni filtrovane oblasti o okraje
		int x1 = halfLen;					
		int y1 = halfLen;
		int x2 = width + halfLen;
		int y2 = height + halfLen;

		// rozostreni po radcich (konvoluce) ...
		RawImage tmpImage = new RawImage (bigSrc);
		for (y = y1; (y < y2) /*&& (*Running)*/; y++) {
			for (x = x1; x < x2; x++) {
				double sum = 0.0;
					for (i = 0; i < len; i++)
						sum += h[i] * bigSrc.getPixel (x + halfLen - i, y);
				tmpImage.setPixel (x, y, (int) sum);
			}
		}

		// ...a po sloupcich s orezem do vysledneho obrazku
		RawImage dstImage = new RawImage (width, height, depth);
		for (y = y1; (y < y2) /*&& (*Running)*/; y++)  {
			for (x = x1; x < x2; x++) {
				double sum = 0.0;
				for (i = 0; i < len; i++)
					sum += h[i] * tmpImage.getPixel (x, y + halfLen - i);
				dstImage.setPixel (x - x1, y - y1, (int) sum);
			}
		}

		return dstImage;
	}
	
	
	public RawImage getHalvedNearest () {
		RawImage dstImage = new RawImage (width / 2, height / 2, depth);
		for (int ys = 0, yd = 0; ys < height; ys += 2, yd++) {
			int srcOff = ys * width;
			int dstOff = yd * width / 2;
			int maxDst = dstOff + width / 2;
			for (; dstOff < maxDst; srcOff += 2, dstOff++)
				dstImage.data[dstOff] = data[srcOff];
		}
		return dstImage;
	}
	
	
	public RawImage getHalvedInterpolated () {
		RawImage blurredImage = gaussBlur (0.5);
		return blurredImage.getHalvedNearest ();
	}
	
	
	public RawImage substractImage (RawImage imageToSub) {
		RawImage dst = new RawImage (width, height, depth);
		for (int i = 0; i < width*height; i++)
			dst.data[i] = data[i] - imageToSub.data[i];
		return dst;
	}
	
	public RawImage computeDoG (double r1, double r2) {
		RawImage blurred1 = gaussBlur (r1);
		RawImage blurred2 = gaussBlur (r2);
		// TODO: min, max crop, 16bit version
		RawImage dst = blurred2.substractImage (blurred1);
		if (depth == 8)
			return dst.addOffset(127, 0, 255);
		return dst.addOffset (32767, 0, 65535);
	}
	
	// used after substract image to remove negative numbers (all types are signed in Java)
	public RawImage addOffset (int offset, int minValue, int maxValue) {
		RawImage dst = new RawImage (width, height, depth);
		for (int i = 0; i < width*height; i++)
			dst.data[i] = Math.min (Math.max (data[i] + offset, minValue), maxValue);
		return dst;
	}

	
	public BufferedImage toBufferedImage () {
		int type;
		if (depth == 8)
			type = BufferedImage.TYPE_BYTE_GRAY;
		else
			type = BufferedImage.TYPE_USHORT_GRAY;
		
		BufferedImage img = new BufferedImage (width, height, type);
		java.awt.image.WritableRaster Raster = img.getRaster ();
		if (depth == 8) {
			byte buf[] = new byte[width * height];
			for (int i = 0; i < width * height; i++)
				buf[i] = (byte) data[i];
			Raster.setDataElements (0, 0, width, height, buf);
		} else {
			short buf[] = new short[width * height];
			for (int i = 0; i < width * height; i++)
				buf[i] = (short) data[i];
			Raster.setDataElements (0, 0, width, height, buf);
		}
		return img;
	}
	
	
	public void saveToFile (String fileName) throws IOException {
		javax.imageio.ImageIO.write (toBufferedImage (), "png", new java.io.File (fileName));			
	}
	
	
	public void readFromFile (String fileName) throws Exception {
		BufferedImage img = javax.imageio.ImageIO.read (new java.io.File (fileName));
		width	= img.getWidth ();
		height	= img.getHeight ();
		if (img.getType () == BufferedImage.TYPE_BYTE_GRAY || img.getType () == BufferedImage.TYPE_BYTE_INDEXED)
			depth = 8;
		else if (img.getType () == BufferedImage.TYPE_USHORT_GRAY)
			depth = 16;
		else
			throw new Exception ("File must be 8 or 16 bit image, image type is " + String.valueOf( img.getType ()));

		data = img.getRaster ().getPixels (0, 0, width, height, (int[]) null);
		if (depth == 8) {
			for (int i = 0; i < data.length; i++)
				data[i] <<= 8;
			depth = 16;
		}
	}

	
	public void readFromFileChannel (String fileName, String channel) throws Exception {
		BufferedImage img = javax.imageio.ImageIO.read (new java.io.File (fileName));
		width	= img.getWidth ();
		height	= img.getHeight ();

		// get band in ARGB color model
		int band;
		if (channel.equalsIgnoreCase ("red")) {
			band = 0;
		} else if (channel.equalsIgnoreCase ("green")) {
			band = 1;
		} else if (channel.equalsIgnoreCase ("blue")) {
			band = 2;
		}  else if (channel.equalsIgnoreCase ("alpha")) {
			band = 3;
		} else {
			throw new Exception ("Invalid channel specified ... must be red, green, blue or alpha");
		}
		
		// convert image from whatever into ARGB color model
		BufferedImage convImg;
		if (img.getType () == BufferedImage.TYPE_INT_ARGB) {
			convImg = img;
		} else {
			convImg = new BufferedImage (width, height, BufferedImage.TYPE_INT_ARGB);
			java.awt.Graphics2D g = convImg.createGraphics ();
			g.drawImage (img, 0, 0, null);
			g.dispose ();
		}

		// get data from desired color channel and convert them to 16bit depth
		data = convImg.getRaster ().getSamples (0, 0, width, height, band, new int[width * height]);
		

		for (int i = 0; i < data.length; i++)
			data[i] <<= 8;
		depth = 16;		
 
	}
	
	
}
