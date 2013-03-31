/**
 * @(#)hello_HelloJni.cpp, 2012-9-29.
 * @author leo
 *
 * Copyleft 2013 leo(leoyonn@gmail.com). All lefts released. 
 */

#include <jni.h>
#include "hello_HelloJni.h"
#include <stdio.h>
#include "../utils/char_utils.h"

/**
 * echo the hello
 */
JNIEXPORT jstring JNICALL Java_hello_HelloJni_echo
  (JNIEnv * env, jclass cls, jstring msg) {
    char *w = j2w(env, msg);
    printf("1.ori : %s\n", msg);
    printf("1.j2w : %s\n", w);
    printf("1.g2u : %s\n", g2u(w));
    printf("1.u2g : %s\n", u2g(w));
    printf("1.gu  : %s\n", env->GetStringChars(msg, 0));
    printf("1.gs  : %s\n", env->GetStringUTFChars(msg, NULL));
    char *o = "hello, 鐑熼浘寮�!!!";
    printf("2.ori : %s\n", o);
    printf("2.w2j : %s\n", w2j(env, o));
    printf("2.j2w : %s\n", j2w(env, w2j(env, o)));
    printf("2.u2g : %s\n", u2g(o));
    printf("2.g2u : %s\n", w2j(env, u2g(o)));
    return w2j(env, u2g(o));
}
