/**
 * @(#)sanji_image_feature_Gist.cpp, 2012-11-05.
 * @author leo
 *
 * Copyleft 2013 leo(leoyonn@gmail.com). All lefts released. 
 */

#include <jni.h>
#include <stdio.h>
#include "sanji_image_feature_Gist.h"
#include "../utils/math_utils.h"

extern "C" {
#include "gist.h"
}

const int DEFAILT_NBLOCKS = 4;
const int DEFAILT_NSCALES = 4;
const int DEFAILT_ORIENTATIONS_PER_SCALE[4] = {4, 4, 4,4};

/**
 * default gist function using jint
 * pixels' length should be width * height
 *
 * @param pixels
 * @param width
 * @param height
 * @param result
 * @return the result's size, i.e., feature's length
 */
int gist(jint* pixels, int width, int height, double *& result) {
	color_image_t *im = color_image_new(width, height);
	for (int i = 0; i < width * height; i++) {
		int pixel = (int)pixels[i];
		im->c1[i] = get_r(pixel);
		im->c2[i] = get_g(pixel);
		im->c3[i] = get_b(pixel);
	}

	result = color_gist_scaletab(im, DEFAILT_NBLOCKS, DEFAILT_NSCALES,
			DEFAILT_ORIENTATIONS_PER_SCALE);
    color_image_delete(im);

	int fsize = 0;
	// compute descriptor size
	for (int i = 0; i < DEFAILT_NSCALES; i++) {
		fsize += DEFAILT_NBLOCKS * DEFAILT_NBLOCKS * DEFAILT_ORIENTATIONS_PER_SCALE[i];
	}
	fsize *= 3; // r + g + b color channels

	// print descriptor
	printf("{Gist.cpp} nblock: %d, ", DEFAILT_NBLOCKS);
	for (int i = 0; i < DEFAILT_NSCALES; i++) {
		printf("orient[%d]:%d, ", i, DEFAILT_ORIENTATIONS_PER_SCALE[i]);
	}
	printf("all %d features got\n", fsize);
	return fsize;
}

/**
 * implement of javah generated jni function
 * @param env
 * @param clazz
 * @param pixels	image's pixels
 * @param size		image's size
 */
JNIEXPORT jdoubleArray JNICALL Java_sanji_image_feature_Gist_extract
  (JNIEnv *env, jclass clazz, jintArray _pixels, jint width, jint height) {
	jint* pixels = env->GetIntArrayElements(_pixels, NULL);
	jint size = env->GetArrayLength(_pixels);
	printf("{Gist.cpp} Begin.. Image size:%d(width: %d, height: %d)(width*height=%d)\n",
			size, width, height, width * height);
	double* gists = NULL;
	int fsize = gist(pixels, width, height, gists);
	printf("{Gist.cpp} Done! got %d features, return to java...\n", fsize);
	jdoubleArray jgists = env->NewDoubleArray(fsize);
	env->SetDoubleArrayRegion(jgists, 0, fsize, gists);
	delete[] gists;
	return jgists;
}
