/**
 * @(#)HsvHist.java, 2012-10-15. 

 * Copyleft 2013 leo(leoyonn@gmail.com). All lefts released. 
 *  */

package sanji.image.feature;

import sanji.base.SanjiException;
import sanji.utils.SysUtils;

/**
 * hsv直方图特征提取
 * @author leo
 */
public class HsvHist {
    static {
        SysUtils.addUserPath(System.getProperty("user.dir") + "/so");
        try {
            System.loadLibrary("HsvHist");
        } catch (UnsatisfiedLinkError e) {
            e.printStackTrace();
            throw new SanjiException("Native code library failed to load.\n", e);
        }
    }

    public native static double[] echo(String msg);


}
