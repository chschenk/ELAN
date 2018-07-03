/*
 * Project:	JDSPlayer, Direct Show Media Player for Java
 * Author:	Han Sloetjes
 * Version:	1.0, Oct. 2010
 */
#include <dshow.h>
#include <list>
using namespace std;

#ifndef included_DSUtil
#define included_DSUtil

class DSUtil {
public:
	static long createID() {
		return idcount++;
	}
	static bool JDS_DEBUG;

	static wchar_t *charToWchar(const char *in);
	static wchar_t *copyWchar(const wchar_t *in);
	HRESULT printFilters(IFilterGraph  *pGraph);
	HRESULT printFilters(IFilterGraph  *pGraph, list<wchar_t *>&);
	HRESULT printAllFilters(void);
	HRESULT printAllFilters(list<wchar_t *>&);
	bool isCodecInstalled(const wchar_t *codec_name);
	wchar_t * CLSIDFromFriendlyName(const wchar_t *codec_name);
	wchar_t * getMediaSubType(const wchar_t *in);
	static bool isWin7OrLater(void);
private:
	static long idcount;
	
};

#endif