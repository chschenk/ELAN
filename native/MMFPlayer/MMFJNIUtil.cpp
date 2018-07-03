/*
 * Project:	MMFPlayer, Microsoft Media Foundation Player for Java
 * Author:	Han Sloetjes
 * Version:	1.0, Feb. 2012
 */
#include "MMFJNIUtil.h"
#include <stdlib.h>
#include <string.h>
#include <wchar.h>

MMFJNIUtil::MMFJNIUtil() {
}

char * MMFJNIUtil::jstringToCharArray(JNIEnv *env, jstring str){
	jbyteArray bytes = 0;
	jthrowable exc;
	char *result = 0;
	if (env->EnsureLocalCapacity(2) < 0) {
		return 0;//out of memory
	}
	jmethodID mid;
	jclass cls;
	cls = env->GetObjectClass(str);
	mid = env->GetMethodID(cls, "getBytes", "()[B");
	bytes = (jbyteArray) env->CallObjectMethod(str, mid);
	exc = env->ExceptionOccurred();

	if (!exc) {
		jint len = env->GetArrayLength(bytes);
		result = (char *)malloc(len + 1);
		if (result == 0) {
			// throw out of memory exception hier...
			env->DeleteLocalRef(bytes);
			return 0;
		}
		env->GetByteArrayRegion(bytes, 0, len, (jbyte *)result);
		result[len] = 0;//0 terminate
	} else {
		env->DeleteLocalRef(exc);
	}
	env->DeleteLocalRef(bytes);

	return result;
}

//Helper for converting jstring to wchar_t. From Eclipse, IBM
wchar_t * MMFJNIUtil::convert(JNIEnv * env, jstring s) {
	//get the string and its length into original and len
	// is this good enough for localization?
	const jchar * original = env->GetStringChars(s, 0);
	const jsize len = env->GetStringLength(s);

	//allocate extra one for the null
	wchar_t *converted = new wchar_t[len+1];

	//copy from original into converted
	memcpy(converted, original, sizeof(wchar_t)*len);
	env->ReleaseStringChars(s, original);

	//null terminate it
	converted[len] = 0;

	return converted;
}

jchar * MMFJNIUtil::convertToJchar(JNIEnv *env, wchar_t *in, size_t *length) {
	//wprintf(L"In wchar: %s\n", in);
	size_t len = wcslen(in);
	*length = (size_t)len;
	jchar *out = new jchar[len];

	//copy from original into converted
	memcpy(out, in, sizeof(jchar)*len);

	return out;
}