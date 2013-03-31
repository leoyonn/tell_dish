/**
 * @(#)char_utils.h, 2012-9-29.
 * @author leo
 *
 * Copyleft 2013 leo(leoyonn@gmail.com). All lefts released. 
 */
/**************************************************************************
 * usage:
 * 1. return a jstring to Java from char *a = "某些文字...":
 *  return w2j(env, u2g(a))
 * 2. print a jstring from Java:
 *  printf(env->GetStringUTFChars(msg, NULL))
 *  printf(g2u(j2w(env, jstr)))
 **************************************************************************/

#include <iostream>
#include <string>
#include <windows.h>

using namespace std;

/**
 * convert a jstring to wchar
 * @param env
 * @param jstring
 */
inline char* j2w(JNIEnv *env, jstring jstr) {
    int length = env->GetStringLength(jstr);
    const jchar* jcstr = env->GetStringChars(jstr, 0);
    char* rtn = (char*) malloc(length * 2 + 1);
    int size = WideCharToMultiByte(CP_ACP, 0, (LPCWSTR) jcstr, length, rtn, (length * 2 + 1), NULL, NULL);
    if (size <= 0) {
        return NULL;
    }
    env->ReleaseStringChars(jstr, jcstr);
    rtn[size] = 0;
    return rtn;
}

/**
 * convert a wcar array to jstring
 * @param env
 * @param str
 */
inline jstring w2j(JNIEnv *env, const char* str) {
    jstring rtn = 0;
    int slen = strlen(str);
    unsigned short* buffer = 0;
    if (slen == 0) {
        rtn = env->NewStringUTF(str);
    } else {
        int length = MultiByteToWideChar(CP_ACP, 0, (LPCSTR) str, slen, NULL, 0);
        buffer = (unsigned short*) malloc(length * 2 + 1);
        if (MultiByteToWideChar(CP_ACP, 0, (LPCSTR) str, slen, (LPWSTR) buffer, length) > 0)
            rtn = env->NewString((jchar*) buffer, length);
    }
    if (buffer) {
        free(buffer);
    }
    return rtn;
}

/**
 * convert a utf8 string to gb2312
 * @param utf8
 */
inline char* u2g(const char* utf8) {
    int len = MultiByteToWideChar(CP_UTF8, 0, utf8, -1, NULL, 0);
    wchar_t* wstr = new wchar_t[len + 1];
    memset(wstr, 0, len + 1);
    MultiByteToWideChar(CP_UTF8, 0, utf8, -1, wstr, len);
    len = WideCharToMultiByte(CP_ACP, 0, wstr, -1, NULL, 0, NULL, NULL);
    char* str = new char[len + 1];
    memset(str, 0, len + 1);
    WideCharToMultiByte(CP_ACP, 0, wstr, -1, str, len, NULL, NULL);
    if (wstr) {
        delete[] wstr;
    }
    return str;
}

/**
 * convert a gb2312 string to a utf8
 * @param gb2312
 */
inline char* g2u(const char* gb2312) {
    int len = MultiByteToWideChar(CP_ACP, 0, gb2312, -1, NULL, 0);
    wchar_t* wstr = new wchar_t[len + 1];
    memset(wstr, 0, len + 1);
    MultiByteToWideChar(CP_ACP, 0, gb2312, -1, wstr, len);
    len = WideCharToMultiByte(CP_UTF8, 0, wstr, -1, NULL, 0, NULL, NULL);
    char* str = new char[len + 1];
    memset(str, 0, len + 1);
    WideCharToMultiByte(CP_UTF8, 0, wstr, -1, str, len, NULL, NULL);
    if (wstr) {
        delete[] wstr;
    }
    return str;
}
