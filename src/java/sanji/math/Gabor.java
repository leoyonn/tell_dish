/**
 * @(#)Gabor.java, 2012-10-29. 
 * 
 * Copyleft 2013 leo(leoyonn@gmail.com). All lefts released. 
 */

package sanji.math;

import sanji.image.base.Image;

/**
 * create gabor filter
 * 
 * @author leo
 */
public class Gabor {
    /**
     * Precomputes filter transfer functions. All computations are done on the
     * Fourier domain.
     * 
     * @param orient
     *            number of orientations per scale:
     *            vector that contains the number of orientations at each scale
     *            (from HF to BF)
     * @param w
     *            width of image
     * @param h
     *            height of image
     * @return
     */
    public static double[][] createFilters(int[] orient, int w, int h) {
        // 1. prepare param
        int nscales = orient.length;
        int nfilters = MathUtils.sum(orient);
        int level = 0;
        double[][] param = new double[nfilters][4];
        final double TMP_3D = 16 / Math.pow(32, 2);
        for (int i = 0; i < nscales; i++) {
            for (int j = 0; j < orient[i]; j++) {
                param[level][0] = .35;
                param[level][1] = .3 / Math.pow(1.85, i);
                param[level][2] = Math.pow(orient[i], 2) * TMP_3D;
                param[level][3] = Math.PI / (orient[i]) * j;
                level++;
            }
        }

        // 2. prepare fr & t
        int size = w * h;
        double[] fr = new double[size], t  = new double[size];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int idx = y * w + x;
                double fx = x - w / 2.0;
                double fy = y - h / 2.0;
                fr[idx] = Math.hypot(fx, fy);
                t [idx] = Math.atan2(fy, fx);
            }
        }
        FFT.fftShift(fr, w, h);
        FFT.fftShift(t, w, h);

        // 3. compute n filters
        double[][] filters = new double[nfilters][size];
        for (int fn = 0; fn < nfilters; fn++) {
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    int idx = y * w + x;
                    double tmp = t[idx] + param[fn][3];
                    tmp = tmp < -Math.PI ? tmp + 2.0f * Math.PI : tmp > Math.PI ? tmp - 2.0f * Math.PI : tmp;
                    filters[fn][idx] = Math.exp(-10.0 * param[fn][0]
                            * (fr[idx] / h / param[fn][1] - 1) * (fr[idx] / w / param[fn][1] - 1)
                            - 2.0 * param[fn][2] * Math.PI * tmp * tmp);
                }
            }
        }
        return filters;
    }
    
    /**
     * do gabor use filters
     * @param pixels
     * @param w
     * @param h
     * @param nblock
     * @param filters
     * @return
     */
    public static double[] gabor(int[]pixels, int w, int h, int nblock, double[][]filters) {
        double[] res = new double[3 * nblock * nblock * filters.length];
        int size = w * h;
        Complex[][] in = new Complex[3][size];
        Complex[][] out = new Complex[3][size];
        Complex[][] iin = new Complex[3][size];
        Complex[][] iout = new Complex[3][size];
        // prepare input
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int idx = y * w + x;
                int pixel = pixels[idx];
                int r = Image.getR(pixel), g = Image.getG(pixel), b = Image.getB(pixel);
                in[0][idx] = new Complex(r, .0);
                in[1][idx] = new Complex(g, .0);
                in[2][idx] = new Complex(b, .0);
            }
        }

        // FFT
        out[0] = FFT.Slow.fft(in[0]);
        out[1] = FFT.Slow.fft(in[1]);
        out[2] = FFT.Slow.fft(in[2]);

        for(int k = 0; k < filters.length; k++) {
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    int idx = y * w + x;
                    iout[0][idx] = new Complex( out[0][idx].re() * filters[k][idx],
                                                out[0][idx].im() * filters[k][idx]);
                    iout[1][idx] = new Complex( out[1][idx].re() * filters[k][idx],
                                                out[1][idx].im() * filters[k][idx]);
                    iout[2][idx] = new Complex( out[2][idx].re() * filters[k][idx],
                                                out[2][idx].im() * filters[k][idx]);
                }
            }

            iin[0] = FFT.Slow.ifft(iout[0]);
            iin[1] = FFT.Slow.ifft(iout[1]);
            iin[2] = FFT.Slow.ifft(iout[2]);

            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    int idx = y * w + x;
                    int r = (int) (iin[0][idx].abs() / size);
                    int g = (int) (iin[1][idx].abs() / size);
                    int b = (int) (iin[2][idx].abs() / size);
                    pixels[idx] = Image.rgb(r, g, b);
                }
            }
            averageDownN(res, k * nblock * nblock, filters.length * nblock * nblock, pixels, w, h, nblock);
        }

        return res;
    }

    /**
     * averaging over non-overlapping spuare image blocks
     * @param res
     * @param stride
     * @param pixels
     * @param w
     * @param h
     * @param N
     */
    private static void averageDownN(double[] res, int start, int stride, int[] pixels, int w, int h, int N) {
        int[] nx = new int[N + 1];
        int[] ny = new int[N + 1];
        for (int i = 0; i < N + 1; i++) {
            nx[i] = i * w / N;
            ny[i] = i * h / N;
        }

        for (int l = 0; l < N; l++) {
            for (int k = 0; k < N; k++) {
                double rmean = .0, gmean = .0, bmean = .0;
                for (int j = ny[l]; j < ny[l + 1]; j++) {
                    for (int i = nx[k]; i < nx[k + 1]; i++) {
                        int pixel = pixels[j * w + i];
                        rmean += Image.getR(pixel);
                        gmean += Image.getG(pixel);
                        bmean += Image.getB(pixel);
                    }
                }

                double denom = (ny[l + 1] - ny[l]) * (nx[k + 1] - nx[k]);
                res[start + k * N + l + 0 * stride] = rmean / denom;
                res[start + k * N + l + 1 * stride] = gmean / denom;
                res[start + k * N + l + 2 * stride] = bmean / denom;
            }
        }
    }
}
