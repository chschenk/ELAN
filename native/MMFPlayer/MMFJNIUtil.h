/* 
 * Project:	JMMFPlayer, Microsoft Media Foundation Player for Java
 * Author:	Han Sloetjes
 * Version:	1.0, March 2012
 */
#include <jni.h>

#ifndef included_MMFJNIUtil
#define included_MMFJNIUtil

class MMFJNIUtil{
public:
	MMFJNIUtil();
	char * jstringToCharArray(JNIEnv *env, jstring str); 
	wchar_t * convert(JNIEnv * env, jstring str);
	jchar * convertToJchar(JNIEnv * env, wchar_t * in, size_t * length);
};

#endif