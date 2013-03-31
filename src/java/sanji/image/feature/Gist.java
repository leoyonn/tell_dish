/**
 * @(#)Gist.java, 2012-11-5. 

 * Copyleft 2013 leo(leoyonn@gmail.com). All lefts released. 
 *  */

package sanji.image.feature;

import java.io.IOException;

import sanji.base.SanjiException;
import sanji.utils.Logger;

/**
 * gist of image, jni version
 * 
 * @see http://people.csail.mit.edu/torralba/code/spatialenvelope/
 *      (Modeling the shape of the scene: a holistic representation of the spatial envelope，
 *       International Journal of Computer Vision, Vol. 42(3): 145-175, 2001.)
 * @author leo
 */
public class Gist {
    static {
        try {
            Logger.info("{Gist} loading " + System.getProperty("user.dir") + "/libgist.so");
            System.loadLibrary("gist");
        } catch (UnsatisfiedLinkError e) {
            throw new SanjiException("Native code library failed to load.\n", e);
        }
    }

    public native static double[] extract(int[] pixels, int width, int height);

    public static void main(String[] args) throws IOException {
    }
}
