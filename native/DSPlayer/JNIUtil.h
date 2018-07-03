#include <jni.h>

#ifndef included_JNIUtil
#define included_JNIUtil

class JNIUtil{
public:
	JNIUtil();
	char * jstringToCharArray(JNIEnv *env, jstring str); 
	wchar_t * convert(JNIEnv * env, jstring str);
	jchar * convertToJchar(JNIEnv * env, wchar_t * in, size_t * length);
};

#endif