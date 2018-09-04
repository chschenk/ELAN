/*
 * Project:	JDSPlayer, Direct Show Media Player for Java
 * Author:	Han Sloetjes
 * Version:	1.0, Oct. 2010
 */
//#include "stdafx.h"// for now don't use precompiled headers
#include "DSUtil.h"
#include <stdlib.h>
#include <string.h>
#include <wchar.h>
#include <windows.h>
#include <iostream>
using namespace std;

long DSUtil::idcount = (long)1000;
bool DSUtil::JDS_DEBUG = false;

wchar_t * DSUtil::charToWchar(const char *orig){
	size_t origsize = strlen(orig) + 1;
    const size_t newsize = 1000;// should be enough?
    size_t convertedChars = 0;
    wchar_t wcstring[newsize];
    mbstowcs_s(&convertedChars, wcstring, origsize, orig, _TRUNCATE);
	//wcout << wcstring << endl;

	return wcstring;
}

wchar_t * DSUtil::copyWchar(const wchar_t *in) {
	//wcout << L"in " << in << endl;
	size_t len = wcslen(in);
	wchar_t *out = new wchar_t[len + 1];
	wmemcpy(out, in, len);
	out[len] = 0;
	//wcout << L"out " << out << endl;
	return out;
}

HRESULT DSUtil::printFilters(IFilterGraph  *pGraph) {
	IEnumFilters *pEnumFilters = NULL;
	IBaseFilter *pFilter;
	IEnumPins *pEnumPins = NULL;
	IPin *pPin;
	HRESULT hr;
	int i = 0;
	int j = 0;
	hr = pGraph->EnumFilters(&pEnumFilters);

	if (FAILED(hr)) {
		return hr;
	}

	while (pEnumFilters->Next(1, &pFilter, NULL) == S_OK) {
		FILTER_INFO fInfo;
		hr = pFilter->QueryFilterInfo(&fInfo);
		if (FAILED(hr)) {
			wprintf(L"Filter %d: %s\n", i, L"No info");
			i++;
			continue;
		}
		wprintf(L"Filter %d: %s\n", i, fInfo.achName);
		j = 0;
		hr = pFilter->EnumPins(&pEnumPins);

		if (SUCCEEDED(hr)) {
			while (pEnumPins->Next(1, &pPin, NULL) == S_OK) {
				PIN_INFO pInfo;
				hr = pPin->QueryPinInfo(&pInfo);
				if (FAILED(hr)) {
					wprintf(L"\tPin %d: %s\n", j, L"No info");
					j++;
					continue;
				}
				wprintf(L"\tPin %d: %s\n", j, pInfo.achName);
				if (pInfo.pFilter != NULL) {
					pInfo.pFilter->Release();
				}
				// test for printing the Id of a pin, which is not always the same as name
				/*
				LPWSTR id = NULL;
				HRESULT nhr = pPin->QueryId(&id);
				if (SUCCEEDED(nhr)) {
					wprintf(L"\tPin Id: %s\n", id);
				} else {
					wprintf(L"Can not get Pin Id\n");
				}
				*/
				j++;
			}
			pPin->Release();
		}
		pEnumPins->Release();

		if (fInfo.pGraph != NULL) {
			fInfo.pGraph->Release();
		}
		pFilter->Release();
		i++;
	}
	pEnumFilters->Release();

	return S_OK;
}

/*
 * Adds all filters in the graph to the specified list.
 */
HRESULT DSUtil::printFilters(IFilterGraph  *pGraph, list<wchar_t *> &filtList) {
	IEnumFilters *pEnumFilters = NULL;
	IBaseFilter *pFilter;
	IEnumPins *pEnumPins = NULL;
	IPin *pPin;
	HRESULT hr;
	int i = 1;
	int j = 1;
	hr = pGraph->EnumFilters(&pEnumFilters);

	if (FAILED(hr)) {
		return hr;
	}

	while (pEnumFilters->Next(1, &pFilter, NULL) == S_OK) {
		FILTER_INFO fInfo;
		hr = pFilter->QueryFilterInfo(&fInfo);
		if (FAILED(hr)) {
			wprintf(L"Filter %d: %s\n", i, L"No info");
			i++;
			continue;
		}
		//wprintf(L"Filter %d: %s\n", i, fInfo.achName);
		const size_t infLength = wcslen(fInfo.achName) + 12;
		
		//const size_t infLength = infLength2;//140
		wchar_t *next = new wchar_t[infLength];
		wcscpy_s(next, infLength, charToWchar("Filter "));
		//char *index= new char[2];
		wchar_t *index= new wchar_t[2];
		//_itoa(i, index, 10);
		_itow(i, index,  10);
		wcscat_s(next, infLength, index);
		//wcscat_s(next, infLength, reinterpret_cast<wchar_t *>(index));
		wcscat_s(next, infLength, charToWchar(": "));
		wcscat_s(next, infLength, fInfo.achName);
		filtList.push_back(next);
		delete(index);
		j = 1;
		hr = pFilter->EnumPins(&pEnumPins);

		if (SUCCEEDED(hr)) {
			while (pEnumPins->Next(1, &pPin, NULL) == S_OK) {
				PIN_INFO pInfo;
				hr = pPin->QueryPinInfo(&pInfo);
				if (FAILED(hr)) {
					wprintf(L"\tPin %d: %s\n", j, L"No info");
					j++;
					continue;
				}
				//wprintf(L"\tPin %d: %s\n", j, pInfo.achName);
				const size_t pinLength = wcslen(pInfo.achName) + 12;
				//const size_t pinLength = 138;
				wchar_t *nextpin = new wchar_t[pinLength];
				wcscpy_s(nextpin, pinLength, L"\tPin ");
				//char *jindex = new char[1];
				wchar_t *jindex = new wchar_t[1];
				//_itoa(j, jindex, 10);
				_itow(j, jindex, 10);
				//wcscat_s(nextpin, pinLength, reinterpret_cast<wchar_t *>(jindex));
				wcscat_s(nextpin, pinLength, jindex);
				wcscat_s(nextpin, pinLength, charToWchar(": "));
				wcscat_s(nextpin, pinLength, pInfo.achName);
				filtList.push_back(nextpin);
				//filtList.push_back(copyWchar(pInfo.achName));
				delete(jindex);
				if (pInfo.pFilter != NULL) {
					pInfo.pFilter->Release();
				}
				j++;
			}
			pPin->Release();
		}
		pEnumPins->Release();

		if (fInfo.pGraph != NULL) {
			fInfo.pGraph->Release();
		}
		pFilter->Release();
		i++;
	}
	pEnumFilters->Release();

	return S_OK;
}

HRESULT DSUtil::printAllFilters() {
	HRESULT hr;	
	hr = CoInitializeEx(NULL, COINIT_MULTITHREADED);//APARTMENTTHREADED? COINIT_MULTITHREADED
	if (FAILED(hr)) {
		return hr;
	}
	ICreateDevEnum *pDevEnum;
	
	hr = CoCreateInstance(CLSID_SystemDeviceEnum, NULL, CLSCTX_INPROC_SERVER, IID_ICreateDevEnum, (void **)&pDevEnum);
	
	if (FAILED(hr)) {
		CoUninitialize();
		return hr;
	}
	
	IEnumMoniker *pEnumMoniker;
	// dwFlags = 0 enumerates all native DirectShow filters and DirectX Media Objects (DMOs)
	hr = pDevEnum->CreateClassEnumerator(CLSID_LegacyAmFilterCategory, &pEnumMoniker, 0);

	if (FAILED(hr)) {
		CoUninitialize();
		pDevEnum->Release();
		return hr;
	}
	IMoniker *pMoniker = NULL; 
 
	int i = 1;
	while (pEnumMoniker->Next(1, &pMoniker, NULL) == S_OK) {
		IPropertyBag *pPropBag;
		hr = pMoniker->BindToStorage(NULL, NULL, IID_IPropertyBag, (void **)&pPropBag);

		if (SUCCEEDED(hr)) {
            VARIANT varName;
            VariantInit(&varName);
            hr = pPropBag->Read(L"FriendlyName", &varName, NULL);
            if (SUCCEEDED(hr)) {
				wprintf(L"Filter %d: %s\n", i, varName.bstrVal);
			}
			hr = pPropBag->Read(L"CLSID", &varName, NULL);
            if (SUCCEEDED(hr)) {
				wprintf(L"Filter CLSID %d: %s\n", i, varName.bstrVal);
			}
            VariantClear(&varName);
			pPropBag->Release();
		}
		i++;
		pMoniker->Release();
	}
	pEnumMoniker->Release();
	pDevEnum->Release();
	CoUninitialize();

	return S_OK;
}

HRESULT DSUtil::printAllFilters(list<wchar_t *> &filtList) {
	HRESULT hr;	
	hr = CoInitializeEx(NULL, COINIT_MULTITHREADED);//APARTMENTTHREADED? COINIT_MULTITHREADED
	if (FAILED(hr)) {
		return hr;
	}
	ICreateDevEnum *pDevEnum;
	
	hr = CoCreateInstance(CLSID_SystemDeviceEnum, NULL, CLSCTX_INPROC_SERVER, IID_ICreateDevEnum, (void **)&pDevEnum);
	
	if (FAILED(hr)) {
		CoUninitialize();
		return hr;
	}
	
	IEnumMoniker *pEnumMoniker;
	// dwFlags = 0 enumerates all native DirectShow filters and DirectX Media Objects (DMOs)
	hr = pDevEnum->CreateClassEnumerator(CLSID_LegacyAmFilterCategory, &pEnumMoniker, 0);

	if (FAILED(hr)) {
		CoUninitialize();
		pDevEnum->Release();
		return hr;
	}
	IMoniker *pMoniker = NULL; 
 
	int i = 1;
	while (pEnumMoniker->Next(1, &pMoniker, NULL) == S_OK) {
		IPropertyBag *pPropBag;
		hr = pMoniker->BindToStorage(NULL, NULL, IID_IPropertyBag, (void **)&pPropBag);

		if (SUCCEEDED(hr)) {
            VARIANT varName;
            VariantInit(&varName);
            hr = pPropBag->Read(L"FriendlyName", &varName, NULL);
            if (SUCCEEDED(hr)) {
				//wprintf(L"Filter %d: %s\n", i, varName.bstrVal);
				filtList.push_back(copyWchar(varName.bstrVal));
			}
			//hr = pPropBag->Read(L"CLSID", &varName, NULL);
		    // if (SUCCEEDED(hr)) {
			//	wprintf(L"Filter CLSID %d: %s\n", i, varName.bstrVal);
			//}
            VariantClear(&varName);
			pPropBag->Release();
		}
		i++;
		pMoniker->Release();
	}
	pEnumMoniker->Release();
	pDevEnum->Release();
	CoUninitialize();

	return S_OK;
}

bool DSUtil::isCodecInstalled(const wchar_t *codec_name){
	bool found = false;
	HRESULT hr;	
	hr = CoInitializeEx(NULL, COINIT_MULTITHREADED);//APARTMENTTHREADED? COINIT_MULTITHREADED
	if (FAILED(hr)) {
		return found;
	}

	ICreateDevEnum *pDevEnum;
	hr = CoCreateInstance(CLSID_SystemDeviceEnum, NULL, CLSCTX_INPROC_SERVER, IID_ICreateDevEnum, (void **)&pDevEnum);
	
	if (FAILED(hr)) {
		CoUninitialize();
		return found;
	}
	
	IEnumMoniker *pEnumMoniker;
	// dwFlags = 0 enumerates all native DirectShow filters and DirectX Media Objects (DMOs)
	hr = pDevEnum->CreateClassEnumerator(CLSID_LegacyAmFilterCategory, &pEnumMoniker, 0);

	if (FAILED(hr)) {
		CoUninitialize();
		pDevEnum->Release();
		return found;
	}
	IMoniker *pMoniker = NULL; 
 
	int i = 1;
	while (pEnumMoniker->Next(1, &pMoniker, NULL) == S_OK) {
		IPropertyBag *pPropBag;
		hr = pMoniker->BindToStorage(NULL, NULL, IID_IPropertyBag, (void **)&pPropBag);

		if (SUCCEEDED(hr)) {
            VARIANT varName;
            VariantInit(&varName);
            hr = pPropBag->Read(L"FriendlyName", &varName, NULL);
            if (SUCCEEDED(hr)) {
				//wprintf(L"Filter %d: %s\n", i, varName.bstrVal);
				if (wcscmp(varName.bstrVal, codec_name) == 0) {
					found = true;
				}
			}
            VariantClear(&varName);
			pPropBag->Release();
		}
		i++;
		pMoniker->Release();
		if (found) {
			break;
		}
	}

	pEnumMoniker->Release();
	pDevEnum->Release();
	CoUninitialize();

	return found;
}

/*
 * Returns the CLSID string from the filter with the specified Friendly Name, or NULL if the filter was not found. 
 */
wchar_t *  DSUtil::CLSIDFromFriendlyName(const wchar_t *codec_name) {
	bool found = false;
	HRESULT hr;	
	hr = CoInitializeEx(NULL, COINIT_MULTITHREADED);//APARTMENTTHREADED? COINIT_MULTITHREADED
	if (FAILED(hr)) {
		return NULL;
	}

	ICreateDevEnum *pDevEnum;
	hr = CoCreateInstance(CLSID_SystemDeviceEnum, NULL, CLSCTX_INPROC_SERVER, IID_ICreateDevEnum, (void **)&pDevEnum);
	
	if (FAILED(hr)) {
		CoUninitialize();
		return NULL;
	}
	
	IEnumMoniker *pEnumMoniker;
	// dwFlags = 0 enumerates all native DirectShow filters and DirectX Media Objects (DMOs)
	hr = pDevEnum->CreateClassEnumerator(CLSID_LegacyAmFilterCategory, &pEnumMoniker, 0);

	if (FAILED(hr)) {
		CoUninitialize();
		pDevEnum->Release();
		return NULL;
	}
	wchar_t * guidString;
	IMoniker *pMoniker = NULL; 
 
	int i = 1;
	while (pEnumMoniker->Next(1, &pMoniker, NULL) == S_OK) {
		IPropertyBag *pPropBag;
		hr = pMoniker->BindToStorage(NULL, NULL, IID_IPropertyBag, (void **)&pPropBag);

		if (SUCCEEDED(hr)) {
            VARIANT varName;
            VariantInit(&varName);
            hr = pPropBag->Read(L"FriendlyName", &varName, NULL);
            if (SUCCEEDED(hr)) {
				//wprintf(L"Filter %d: %s\n", i, varName.bstrVal);
				if (wcscmp(varName.bstrVal, codec_name) == 0) {
					found = true;
					//wprintf(L"Filter found %d: %s\n", i, varName.bstrVal);
					hr = pPropBag->Read(L"CLSID", &varName, NULL);
					if (SUCCEEDED(hr)) {
						guidString = copyWchar(varName.bstrVal);
						//wprintf(L"Filter CLSID %d: %s\n", i, clsid);
					}
				}
			}
            VariantClear(&varName);
			pPropBag->Release();
		}
		i++;
		pMoniker->Release();
		if (found) {
			break;
		}
	}

	pEnumMoniker->Release();
	pDevEnum->Release();
	CoUninitialize();

	if(found) {
		return guidString;
	} else {
		return NULL;
	}
}

/*
 * Retrieves and returns the media subtype of the output pin of a source filter, as a string.
 * NB version with an [out] wchar_t pointer as a parameter didn't work when calling from jds_JDSPlayer
 */
wchar_t * DSUtil::getMediaSubType(const wchar_t *file){
	HRESULT hr;
	wchar_t * guidString;
	hr = CoInitializeEx(NULL, COINIT_MULTITHREADED);//APARTMENTTHREADED? COINIT_MULTITHREADED
	if (FAILED(hr)) {
		printf("DSUtil: Failed to initialize COM.\n");
		return NULL;
	}

	IGraphBuilder *pGraph;
	hr = CoCreateInstance(CLSID_FilterGraph, NULL, CLSCTX_INPROC_SERVER,
                 IID_IGraphBuilder, (void **)&pGraph);
	if (FAILED(hr)) {
		if (DSUtil::JDS_DEBUG) {
			printf("DSUtil: Failed to create a GraphBuilder.\n");
		}
		CoUninitialize();
		return NULL;
	}

	IBaseFilter	  *pSourceFileFilter = NULL;
	hr = pGraph->AddSourceFilter((LPCWSTR)file, (LPCWSTR)L"Source", &pSourceFileFilter);

	if (FAILED(hr)) {
		if (DSUtil::JDS_DEBUG) {
			printf("DSUtil: Failed to add Source Filter.\n");
		}
		pGraph->Release();
		CoUninitialize();
		return NULL;
	}

	// enumerate the pins and find the media sub type of the output pin 
	IEnumPins *pEnumPins = NULL;
	IPin *pPin;
	GUID result = GUID_NULL;

	hr = pSourceFileFilter->EnumPins(&pEnumPins);
	if (SUCCEEDED(hr)) {
		while (pEnumPins->Next(1, &pPin, NULL) == S_OK) {
			PIN_DIRECTION PinDirThis;
			pPin->QueryDirection(&PinDirThis);

			if(PinDirThis == PINDIR_OUTPUT) {
				IEnumMediaTypes *pEnumMT = NULL;
				AM_MEDIA_TYPE *pmt = NULL;
				hr = pPin->EnumMediaTypes(&pEnumMT);

				if (SUCCEEDED(hr)) {
					while(pEnumMT->Next(1, &pmt, NULL) == S_OK) {
						// return the first subtype
						result = pmt->subtype;
						LPOLESTR lplp;
						hr = StringFromCLSID(result, &lplp);
						//hr = StringFromCLSID(result, &guidString);

						guidString = copyWchar(lplp);
						//wprintf(L"Subtype: %s\n", guidString);
						// free memory
						CoTaskMemFree(lplp);

						if (pmt->cbFormat != 0) {
							CoTaskMemFree((PVOID)pmt->pbFormat);
							pmt->cbFormat = 0;
							pmt->pbFormat = NULL;
						}
						if (pmt->pUnk != NULL) {
							// pUnk should not be used.
							pmt->pUnk->Release();
							pmt->pUnk = NULL;
						}
						
						break;
					}
				}
				pEnumMT->Release();
			}
			pPin->Release();
		}
		pEnumPins->Release();
	}
	if (DSUtil::JDS_DEBUG) {
		printf("DSUtil getMediaSubType: Cleaning up.\n");
	}
	pSourceFileFilter->Release();
	pGraph->Release();
	CoUninitialize();

	return guidString;
}

bool DSUtil::isWin7OrLater(){
	OSVERSIONINFO osvi;

    ZeroMemory(&osvi, sizeof(OSVERSIONINFO));
    osvi.dwOSVersionInfoSize = sizeof(OSVERSIONINFO);

    GetVersionEx(&osvi);
	printf("Windows version, major: %d, minor %d\n", osvi.dwMajorVersion, osvi.dwMinorVersion);
	// Windows 7 should be major 6, minor 1
	return ( (osvi.dwMajorVersion > 6) ||
       ( (osvi.dwMajorVersion == 6) && (osvi.dwMinorVersion >= 1) ));
}