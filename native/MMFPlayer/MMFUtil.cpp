/* 
 * Project:	JMMFPlayer, Microsoft Media Foundation Player for Java
 * Author:	Han Sloetjes
 * Version:	1.0, March 2012
 */
#include "MMFUtil.h"
#include <stdio.h>

long MMFUtil::idcount = (long)100;
bool MMFUtil::JMMF_DEBUG = false;
BOOL MMFUtil::JMMF_CORRECT_AT_PAUSE = TRUE;

wchar_t * MMFUtil::charToWchar(const char *orig){
	size_t origsize = strlen(orig) + 1;
    const size_t newsize = 1000;// should be enough?
    size_t convertedChars = 0;
    wchar_t wcstring[newsize];
    mbstowcs_s(&convertedChars, wcstring, origsize, orig, _TRUNCATE);
	//wcout << wcstring << endl;

	return wcstring;
}

wchar_t * MMFUtil::copyWchar(const wchar_t *in) {
	//wcout << L"in " << in << endl;
	size_t len = wcslen(in);
	wchar_t *out = new wchar_t[len + 1];
	wmemcpy(out, in, len);
	out[len] = 0;
	//wcout << L"out " << out << endl;
	return out;
}

/*
* Checks whether a video stream is present in a media source. 
* [in]  IMFMediaSource the media source
* [out] BOOL will receive the boolean value
*/
HRESULT MMFUtil::HasVideoMediaType(IMFMediaSource *pMediaSource, BOOL *VideoFound) {
	if (pMediaSource == NULL) {
		return E_POINTER;
	}
	BOOL hasVideo = FALSE;
	DWORD numSourceStreams = 0;
	IMFPresentationDescriptor *pSourcePD = NULL;

	HRESULT hr = pMediaSource->CreatePresentationDescriptor(&pSourcePD);
	if (FAILED(hr)) {
		if (pSourcePD != NULL) {
			pSourcePD->Release();
		}
		return hr;
	}

	hr = pSourcePD->GetStreamDescriptorCount(&numSourceStreams);
	if (FAILED(hr)) {
		if (pSourcePD != NULL) {
			pSourcePD->Release();
		}
		return hr;
	}
	
	BOOL selected = FALSE;
	BOOL canBreak = FALSE;
	GUID guidMajorType = GUID_NULL;

	for (DWORD i = 0; i < numSourceStreams; i++) {
		IMFStreamDescriptor* pStreamDes = NULL;
		HRESULT hres = pSourcePD->GetStreamDescriptorByIndex(i, &selected, &pStreamDes);

		if (SUCCEEDED(hres)) {
			if (selected) {
				IMFMediaTypeHandler *pHandler = NULL;
				
				hres = pStreamDes->GetMediaTypeHandler(&pHandler);
				if (SUCCEEDED(hres)) {
					hres = pHandler->GetMajorType(&guidMajorType);

					if (SUCCEEDED(hres)) {
						if (MFMediaType_Video == guidMajorType) {
							hasVideo = TRUE;
							canBreak = TRUE;
							//printf("MMFUtil: Video found for media source\n");
						}
					}
				}
				if (pHandler != NULL) {
					pHandler->Release();
				}
			}
		}

		if (pStreamDes != NULL) {
			pStreamDes->Release();
		}

		if (canBreak) {
			break;
		}
	}

	if (pSourcePD != NULL) {
		pSourcePD->Release();
	}

	if (SUCCEEDED(hr)) {
		//printf("MMFUtil: setting return value\n");
		*VideoFound = hasVideo;
	}

	return hr;
}