/**
 * @(#)math_utils.h, 2012-11-2.
 * @author leo
 *
 * Copyleft 2013 leo(leoyonn@gmail.com). All lefts released. 
 */

/**
 * get the r from rgb
 *
 * @param rgb
 * @return
 */
inline int get_r(int rgb) {
	return (rgb & 0xff0000) >> 16;
}

/**
 * get the g from rgb
 *
 * @param rgb
 * @return
 */
inline int get_g(int rgb) {
	return (rgb & 0xff00) >> 8;
}

/**
 * get the b from rgb
 *
 * @param rgb
 * @return
 */
inline int get_b(int rgb) {
	return (rgb & 0xff);
}

inline int make_rgb(int r, int g, int b) {
	return ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
}
