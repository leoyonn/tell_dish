/**
 * @(#)MathUtils.java, 2012-10-16. 

 * Copyleft 2013 leo(leoyonn@gmail.com). All lefts released. 
 *  */

package sanji.math;

import sanji.base.SanjiException;

/**
 * math related utils.
 * 
 * @author leo
 */
public class MathUtils {
    /**
     * restrict a between min and max
     * 
     * @param <T>
     * @param a
     * @param min
     * @param max
     * @return
     */
    public static <T extends Comparable<T>> T clamp(T a, T min, T max) {
        if (a.compareTo(min) < 0) {
            return min;
        } else if (a.compareTo(max) > 0) {
            return max;
        } else {
            return a;
        }
    }

    /**
     * get sum of array, no handle of overflow
     * 
     * @param array
     * @return
     */
    public static int sum(int[] array) {
        int sum = 0;
        if (array != null) {
            for (int a: array) {
                sum += a;
            }
        }
        return sum;
    }
    
    public static double[][] meshGrid(int[]x, int []y) {
        return null;
    }
    
    
    /**
     * add padding to left & right & to & bottom to matrix (used for image expand
     * <pre>
     *  __________________
     * |                  |
     * |-p - ________     |
     * |    |        |    |
     * |    |        |    |
     * |    |        |    |
     * |    +--------+    |
     * |                  |
     * +------------------+
     * </pre>
     * 
     * @param matrix
     * @param width
     * @param height
     * @param psize
     * @return
     */
    public static int[] padding(int[] matrix, int width, int height, int psize) {
        if (matrix.length != width * height) {
            throw new SanjiException("illegal matrix size and width/height.");
        }
        int newWidth = width + 2 * psize;
        int newHeight = height + 2 * psize;
        int[] newMatrix = new int [newWidth * newHeight];
        int x, y;
        // middle
        for (y = 0; y < height; y ++) {
            for (x = 0; x < width; x ++) {
                newMatrix[(y + psize) * newWidth + x + psize] = matrix[y * width + x];
            }
        }
        // top & bottom
        for (y = 0; y < psize; y ++) {
            for (x = 0; x < width; x ++) {
                newMatrix[y * newWidth + x + psize] = matrix[(psize - y - 1) * width + x];
                newMatrix[(y + psize + height) * newWidth + x + psize] = matrix[(height - y - 1) * width + x];
            }
        }
        
        // left & right
        for (y = 0; y < newHeight; y ++) {
            for (x = 0; x < psize; x ++) {
                newMatrix[y * newWidth + x] = newMatrix[y * newWidth + 2 * psize - x - 1];
                newMatrix[y * newWidth + width + psize + x] = newMatrix[y * newWidth + width + psize - x - 1];
            }
        }

        return newMatrix;
    }

    /**
     * remove padding of pad, and set result to rem.
     * @param pad
     * @param rem
     * @param width
     * @param height
     * @param psize
     * @return
     */
    public static void removePadding(int[] pad, int[] rem, int width, int height, int psize) {
        int pwidth = width + 2 * psize;
        int pheight = height + 2 * psize;
        if (rem.length != width * height || pad.length != pwidth * pheight) {
            throw new SanjiException("illegal matrix size and width/height.");
        }
        int x, y;
        for (y = 0; y < height; y ++) {
            for (x = 0; x < width; x ++) {
                rem[y * width + x] = pad[(y + psize) * pwidth + x + psize];
            }
        }
    }
    
    public static boolean isPowerOf2(int x) {
        return (x & (x - 1)) == 0;
    }
}
