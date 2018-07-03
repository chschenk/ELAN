/* 
 * Project:	JMMFPlayer, Microsoft Media Foundation Player for Java
 * Author:	Han Sloetjes
 * Version:	1.0, March 2012
 */
#include <mfapi.h>
#include <mfidl.h>
#include <wchar.h>

#ifndef included_MMFUtil
#define included_MMFUtil

class MMFUtil {
public:
	static long createID() {
		return idcount++;
	}
	static bool JMMF_DEBUG;
	static BOOL JMMF_CORRECT_AT_PAUSE;

	static HRESULT HasVideoMediaType(IMFMediaSource *, BOOL *);

	static wchar_t *charToWchar(const char *in);
	static wchar_t *copyWchar(const wchar_t *in);

private:
	static long idcount;
};

#endif