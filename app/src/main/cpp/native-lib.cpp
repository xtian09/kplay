#include <jni.h>

extern "C"
JNIEXPORT jstring

JNICALL
Java_com_test_kplay_MainActivity_stringFromJNI(JNIEnv *env, jobject thiz) {
    return env->NewStringUTF("Hello from C++");
}


