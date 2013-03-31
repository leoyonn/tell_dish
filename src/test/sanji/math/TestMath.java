/**
 * @(#)TestMath.java, 2012-10-29. 

 * Copyleft 2013 leo(leoyonn@gmail.com). All lefts released. 
 *  */

package sanji.math;

import junit.framework.Assert;

import org.junit.Test;

/**
 * @author leo
 *
 */
public class TestMath {
    @Test
    public void testGabor() {
        int[] orient = new int[]{2,2,2,2}; 
        double[][]filters = Gabor.createFilters(orient, 3, 3);
        for (double[]f: filters) {
            for (double v: f) {
                System.out.print(String.format("%8.4f, ", v));
            }
            System.out.println();
        }
    }
    
    private void print(double[] img, int w, int h) {
        for (int y = 0; y < h; y ++) {
            for (int x = 0; x < w; x ++) {
                System.out.print(String.format("%2.1f, ", img[y * w + x]));
            }
            System.out.println();
        }
        System.out.println("--------------------------------------------");
    }
    
    private void print(int[] img, int w, int h) {
        for (int y = 0; y < h; y ++) {
            for (int x = 0; x < w; x ++) {
                System.out.print(String.format("%2d, ", img[y * w + x]));
            }
            System.out.println();
        }
        System.out.println("--------------------------------------------");
    }
    
    private void testFFTShift1(double [] img, int w, int h) {
        print(img, w, h);
        FFT.fftShift(img, w, h);
        print(img, w, h);
    }
    
    @Test
    public void testFFTShift() {
        testFFTShift1(new double[]{1,2,3,4}, 2, 2);
        testFFTShift1(new double[]{1,2,3,4,5,6}, 3, 2);
        testFFTShift1(new double[]{1,2,3,4,5,6}, 2, 3);
        testFFTShift1(new double[]{1,2,3,4,5,6,7,8}, 4, 2);
        testFFTShift1(new double[]{1,2,3,4,5,6,7,8}, 2, 4);
    }

    private void testPadding1(int[]img, int w, int h, int psize) {
        print(img, w, h);
        int[] res = MathUtils.padding(img, w, h, psize);
        print(res, w + 2 * psize, h + 2 * psize);
        MathUtils.removePadding(res, img, w, h, psize);
        print(img, w, h);
    }
    
    @Test
    public void testPadding() {
        testPadding1(new int[]{1}, 1, 1, 1);
        testPadding1(new int[]{1,2,3,4}, 2, 2, 1);
        testPadding1(new int[]{1,2,3,4}, 2, 2, 2);
        testPadding1(new int[]{1,2,3,4,5,6}, 2, 3, 2);
        testPadding1(new int[]{1,2,3,4,5,6}, 3, 2, 2);
    }
    
    @Test
    public void testComplex() {
        Complex a = new Complex(5.0, 6.0);
        Complex b = new Complex(-3.0, 4.0);
        System.out.println("a            = " + a);
        System.out.println("b            = " + b);
        System.out.println("Re(a)        = " + a.re());
        System.out.println("Im(a)        = " + a.im());
        System.out.println("b + a        = " + b.plus(a));
        System.out.println("a - b        = " + a.minus(b));
        System.out.println("a * b        = " + a.times(b));
        System.out.println("b * a        = " + b.times(a));
        System.out.println("a / b        = " + a.divides(b));
        System.out.println("(a / b) * b  = " + a.divides(b).times(b));
        System.out.println("conj(a)      = " + a.conjugate());
        System.out.println("|a|          = " + a.abs());
        System.out.println("tan(a)       = " + a.tan());
    }
    
    /**
     * display an array of Complex numbers to standard output
     * @param x
     * @param title
     */
    public static void show(Complex[] x, String title) {
        System.out.println(title);
        System.out.println("-------------------");
        for (int i = 0; i < x.length; i++) {
            System.out.println(x[i]);
        }
        System.out.println();
    }

    @Test
    public void testFFT() {
        int N = 8;
        Complex[] x = new Complex[N];

        // original data
        for (int i = 0; i < N; i++) {
            x[i] = new Complex(i, 0);
            // x[i] = new Complex(-2*Math.random() + 1, 0);
        }
        show(x, "x");

        // FFT of original data
        Complex[] y = FFT.Slow.fft(x);
        show(y, "y = fft(x)");

        // take inverse FFT
        Complex[] z = FFT.Slow.ifft(y);
        show(z, "z = ifft(y)");

        // circular convolution of x with itself
        Complex[] c = FFT.Slow.cconvolve(x, x);
        show(c, "c = cconvolve(x, x)");

        // linear convolution of x with itself
        Complex[] d = FFT.Slow.convolve(x, x);
        show(d, "d = convolve(x, x)");
    }
    
    @Test
    public void test() {
        Assert.assertTrue(MathUtils.isPowerOf2(1));
        Assert.assertTrue(MathUtils.isPowerOf2(2));
        Assert.assertTrue(MathUtils.isPowerOf2(4));
        Assert.assertTrue(MathUtils.isPowerOf2((int)Math.pow(2, 30)));
        Assert.assertFalse(MathUtils.isPowerOf2(3));
        Assert.assertFalse(MathUtils.isPowerOf2(100));
        Assert.assertFalse(MathUtils.isPowerOf2((int)Math.pow(2, 30) - 1));
    }
}
