/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package stereorec;

/**
 *
 * @author Pavel
 */
public class Main {
	public Main () {
	}

	public static void main (String[] args) {
		
        try {
			StereoPair sp = new StereoPair ();
			sp.readAnaglyph ("C:\\devel-c\\stereopair\\Mira Images 2007 - Anaglyphs\\New Project Folder$fld\\New Project$prj\\EM Image 17-55-46 05-09-07$3D\\_left.png", 
                                "C:\\devel-c\\stereopair\\Mira Images 2007 - Anaglyphs\\New Project Folder$fld\\New Project$prj\\EM Image 17-55-46 05-09-07$3D\\_right.png");
			//sp.readAnaglyph("c:\\temp\\EM Image 13-16-21  04-21-05$3D\\left.png", "c:\\temp\\EM Image 13-16-21  04-21-05$3D\\right.png");
			long startTime = System.nanoTime();
			/*
			sp.motionEstARPS (sp.lImg, sp.rImg, 16, 24);
			*/
			int res = 64;
			sp.rImg = sp.rImg.computeDoG (2, 5);
			sp.lImg = sp.lImg.computeDoG (2, 5);
			StereoPair.IntVec vecArr[][] = sp.motionEstARPS (
				sp.lImg,
				sp.rImg, 
				res, 64
			);
			int lvl = 1;
			while (res > 2) {
				StereoPair.IntVec pred[][] = sp.predictFromUpperLevel (vecArr, sp.imgWidth, sp.imgHeight , res);
				res /= 2;
				lvl += 1;
				vecArr = sp.motionEstARPSMod (sp.lImg, sp.rImg, res, 64, pred, lvl);
			}
			System.out.println ("Computation took " + (System.nanoTime() - startTime)/1e9 +  " seconds");
        } catch (Exception e) {
            e.printStackTrace ();
        }
		
    }



}
