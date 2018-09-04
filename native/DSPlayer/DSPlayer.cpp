/*
 * Project:	JDSPlayer, Direct Show Media Player for Java
 * Author:	Han Sloetjes
 * Version:	1.0, Oct. 2010
 */
//#include "stdafx.h"// for now don't use precompiled headers
#include "DSPlayer.h"
#include "DSUtil.h"
//#include "VMRUtil.h"
#include <Objbase.h>
#include <stdio.h>
#include <iostream>
using namespace std;

#define _WIN32_DCOM

DSPlayer::DSPlayer() {
	mediaPath = NULL;
	prefSplitter = NULL;
	initFields();
}

DSPlayer::DSPlayer(const wchar_t *path) {
	mediaPath = DSUtil::copyWchar(path);
	prefSplitter = NULL;
	initFields();
}

DSPlayer::DSPlayer(const wchar_t *path, const wchar_t *splitterCodec) {
	mediaPath = DSUtil::copyWchar(path);
	prefSplitter = DSUtil::copyWchar(splitterCodec);
	initFields();
	//initGraph();
}

DSPlayer::~DSPlayer() {
	if (DSUtil::JDS_DEBUG) {
		printf("DSPlayer Destructor called.\n");
	}
	if (!cleanUpCalled) {
		cleanUpOnClose();
	}
}

void DSPlayer::initFields() {
	pGraph = NULL;
	pCapGraph = NULL;
    pControl = NULL;
	pSeeking = NULL;
	pPosition = NULL;
	pEvent = NULL;
	pVideoWindow = NULL;
	pBasicVideo = NULL;
	pBasicAudio = NULL;
	isVideo = true; //set video as the default
	cleanUpCalled = false;
	pGraphConfig = NULL;
	pPluginControl = NULL;
}

HRESULT DSPlayer::setMediaFile(const wchar_t *path) {
	if (DSUtil::JDS_DEBUG) {
		printf("Set media file.\n");
	}
	if (pGraph != NULL) {
		if (DSUtil::JDS_DEBUG) {
			printf("The graph is already connected.\n");
		}
		return VFW_E_ALREADY_CONNECTED;
	}
	
	mediaPath = DSUtil::copyWchar(path);
	prefSplitter = NULL;
	initFields();
	// render file
	// check the file type
	DSUtil dsu;
	wchar_t *type = dsu.getMediaSubType(mediaPath);
	if (type == NULL) {	
		if (DSUtil::JDS_DEBUG) {
			wprintf(L"Media sub type is NULL.\n");
		}
		return E_POINTER;
	}
	const wchar_t * unsupported = L"{00000000-0000-0000-0000-000000000000}";
	if (DSUtil::JDS_DEBUG) {
		wprintf(L"Media type: %s\n", type);
	}

	if (wcscmp(type, unsupported) == 0) {
		return VFW_E_UNSUPPORTED_STREAM;
	}

	HRESULT hr;
	if (type == NULL) {
		hr = initGraph();
	} else {
		// check the media type, string wise
		wchar_t * mpeg1system;
		wchar_t * mpeg1systemstream;
		wchar_t * mpeg1video;
		wchar_t * mpeg2video;
		wchar_t * mpeg2program;
		wchar_t * mpeg2trans;
		StringFromCLSID(MEDIASUBTYPE_MPEG1System, &mpeg1system);
		StringFromCLSID(MEDIASUBTYPE_MPEG1Video, &mpeg1video);
		StringFromCLSID(MEDIATYPE_MPEG1SystemStream, &mpeg1systemstream);
		StringFromCLSID(MEDIASUBTYPE_MPEG2_VIDEO, &mpeg2video);
		StringFromCLSID(MEDIASUBTYPE_MPEG2_PROGRAM, &mpeg2program);
		StringFromCLSID(MEDIASUBTYPE_MPEG2_TRANSPORT, &mpeg2trans);
			
		if (wcscmp(type, mpeg1system) == 0 || wcscmp(type, mpeg1video) == 0 || 
			wcscmp(type, mpeg1systemstream) == 0) {
			hr = createDefaultMPEG1Graph();
			if (FAILED(hr)) {
				if (pGraph != NULL) {
					pGraph->Release();
				}
				// try auto connect
				hr = initGraph();
			}
		} else if (wcscmp(type, mpeg2video) == 0 || wcscmp(type, mpeg2program) == 0 || 
			wcscmp(type, mpeg2trans) == 0) {
			// the Elecard plugin doesn't support mpeg2 program files
			hr = createDefaultMPEG2Graph();
				/* untested alternative ?
			if (DSUtil::isWin7OrLater()) {
				printf("Running on Windows 7 or higher.\n");
				hr = createDefaultMPEG2GraphPluginControl();
			} else {
				printf("Running on system older than Windows 7.\n");
				hr = createDefaultMPEG2Graph();
			}
			*/
			if (FAILED(hr)) {
				if (pGraph != NULL) {
					pGraph->Release();
				}
				// try auto connect
				hr = initGraph();
			}
		} else {
			hr = initGraph();
		}
		CoTaskMemFree(mpeg1system);
		CoTaskMemFree(mpeg1systemstream);
		CoTaskMemFree(mpeg1video);
		CoTaskMemFree(mpeg2video);
		CoTaskMemFree(mpeg2program);
		CoTaskMemFree(mpeg2trans);
	}
	return hr;
}

HRESULT DSPlayer::setMediaFile(const wchar_t *path, const wchar_t *splitterCodec) {
	if (pGraph != NULL) {
		return VFW_E_ALREADY_CONNECTED;
	}
	HRESULT hr;
	mediaPath = DSUtil::copyWchar(path);
	prefSplitter = DSUtil::copyWchar(splitterCodec);
	initFields();
	// render file
	hr = initGraph();
	return hr;
}

HRESULT DSPlayer::start() {
	if (pControl != NULL) {
		// check running state?
		HRESULT hr;
		//printf("Media Control in Start: %d\n", &pControl);
		hr = pControl->Run();
		if (SUCCEEDED(hr) && DSUtil::JDS_DEBUG) {
			printf("Control started\n");
		}
		//long evCode;
		//HRESULT ret = pEvent->WaitForCompletion(10000, &evCode);
		//if (SUCCEEDED(ret)) {
		//	printf("Wait completed: ");
		//	printf("%d\n", evCode);
		//} else {
		//	printf("Wait failed");
		//}
		return hr;
	} else {
		return S_FALSE;
	}
}

HRESULT DSPlayer::stop() {
	if (pControl != NULL) {
		// check running state?
		HRESULT hr;
		//printf("Media Control in Stop: %d\n", &pControl);
		hr = pControl->Stop();
		if (SUCCEEDED(hr) && DSUtil::JDS_DEBUG) {
			printf("Control stopped\n");
		}
		return hr;
	} else {
		return S_FALSE;
	}
}

HRESULT DSPlayer::pause() {
	if (pControl != NULL) {
		// check running state?
		HRESULT hr;
		//printf("Media Control in Pause: %d\n", &pControl);
		hr = pControl->Pause();
		if (SUCCEEDED(hr) && DSUtil::JDS_DEBUG) {
			printf("Control paused\n");
		}
		return hr;
	} else {
		return S_FALSE;
	}
}

void DSPlayer::stopWhenReady() {
	if (pControl != NULL) {
		HRESULT hr;
		//printf("Media Control in stopWhenReady: %d\n", &pControl);
		hr = pControl->StopWhenReady();
		if (SUCCEEDED(hr) && DSUtil::JDS_DEBUG) {
			printf("Control stopped when ready.\n");
		}
	}
}

HRESULT DSPlayer::setOwnerWindow(HWND handle) {
	//printf("Owner Window: %d\n", &pVideoWindow);

		if (pVideoWindow != NULL) {
		// check running state?
		HRESULT hr;

		OAHWND owner = (OAHWND) handle;
		//printf("Window handle: %p\n", owner);

		if (!IsWindow(handle) && DSUtil::JDS_DEBUG){
			printf("The canvas handle is NOT a window handle\n");
		}

		hr = pVideoWindow->put_Owner(owner);

		if (SUCCEEDED(hr)) {
			//printf("Window owner set successfully\n");
			hr = pVideoWindow->put_MessageDrain(owner);		 
		} else {
			printf("Setting window owner failed\n");
		}
		
		return hr;
	} else {
		printf("No video window created...\n");
		return S_FALSE;
	}
}

HRESULT DSPlayer::setVideoWindowPos(long x, long y, long w, long h) {
	if (pVideoWindow != NULL) {
		// check running state?
		HRESULT hr;
		hr = pVideoWindow->SetWindowPosition(x, y, w, h);
		
		if (SUCCEEDED(hr)) {
			if (DSUtil::JDS_DEBUG) {
				printf("Window position set successfully\n");
			}
		} else {
			printf("Setting window position failed\n");
		}
		
		return hr;
	} else {
		printf("No video window...\n");
		return S_FALSE;
	}
}

HRESULT DSPlayer::setVideoSourcePos(long x, long y, long w, long h) {
	if (pBasicVideo != NULL) {
		// check running state?
		HRESULT hr;
		hr = pBasicVideo->SetSourcePosition(x, y, w, h);
		
		if (SUCCEEDED(hr)) {
			if (DSUtil::JDS_DEBUG) {
				printf("Video source position set successfully\n");
			}
		} else {
			printf("Setting video source position failed\n");
		}
		
		return hr;
	} else {
		printf("No basic video...\n");
		return S_FALSE;
	}
}

HRESULT DSPlayer::setVideoDestinationPos(long x, long y, long w, long h) {
	if (pBasicVideo != NULL) {
		if ( w == 0 || h == 0) {
			return E_INVALIDARG;
		}
		// check running state?
		HRESULT hr;
		hr = pBasicVideo->SetDestinationPosition(x, y, w, h);
		
		if (SUCCEEDED(hr)) {
			if (DSUtil::JDS_DEBUG) {
				printf("Video destination position set successfully\n");
			}
		} else {
			printf("Setting video destination position failed %d\n", hr);
		}
		
		return hr;
	} else {
		printf("No basic video...\n");
		return S_FALSE;
	}
}

HRESULT DSPlayer::getVideoDestinationPos(long *x, long *y, long *w, long *h) {
	if (pBasicVideo != NULL) {
		HRESULT hr;
		hr = pBasicVideo->GetDestinationPosition(x, y, w, h);
		if (SUCCEEDED(hr)) {
			if (DSUtil::JDS_DEBUG) {
				printf("Video destination position retrieved successfully!\n");
			}
		} else {
			printf("Getting video destination position failed %d\n", hr);
		}
		return hr;
	}  else {
		printf("No basic video...\n");
	}
	return S_FALSE;
}

HRESULT DSPlayer::setVisible(long visible) {
	if (pVideoWindow != NULL) {
		// check running state?
		HRESULT hr;
		hr = pVideoWindow->put_Visible(visible);
		if (DSUtil::JDS_DEBUG) {
			if (SUCCEEDED(hr)) {
				if (DSUtil::JDS_DEBUG) {
					printf("Window visibility set successfully\n");
				}
			} else {
				printf("Setting window visibility failed\n");
			}
		}
		
		return hr;
	} else {
		printf("No video window...\n");
		return S_FALSE;
	}
}

bool DSPlayer::isPlaying() {
	if (pControl != NULL) {
		FILTER_STATE fs;
		HRESULT hr = pControl->GetState(10, (OAFilterState*)&fs);
		if (hr == S_OK && fs == State_Running) {
			return true;
		}
	}

	return false;
}

int DSPlayer::getState() {
	if (pControl != NULL) {
		FILTER_STATE fs;
		HRESULT hr = pControl->GetState(10, (OAFilterState*)&fs);
		if (hr == S_OK) {
			return (int) fs;
		}
	}
	return 0;
}

void DSPlayer::setRate(double rate) {
	if (DSUtil::JDS_DEBUG) {
		if (rate < 0) {
			printf("Negative playback rates (reverse playback) are ignored!\n");
		}
	}
	// the graph manager takes care of stopping the graph etc.
	if (pSeeking != NULL && rate > 0.0) {// some filters support reverse playback but most of them not
		pSeeking->SetRate(rate);
	}
}

double DSPlayer::getRate() {
	double dRate = 1.0;
	if (pSeeking != NULL) {
		pSeeking->GetRate(&dRate);
	}
	return dRate;
}

void DSPlayer::setVolume(long volume) {
	if (pBasicAudio != NULL) {
		if (volume >= -10000 && volume <= 0) { 
			pBasicAudio->put_Volume(volume);
		}
	}
}

long DSPlayer::getVolume() {
	long lVolume = 0;
	if (pBasicAudio != NULL) {
		pBasicAudio->get_Volume(&lVolume);
	}
	return lVolume;
}

void DSPlayer::setBalance(long balance) {
	if (pBasicAudio != NULL) {
		HRESULT hr = pBasicAudio->put_Balance(balance);
		if (DSUtil::JDS_DEBUG) {
			if (FAILED(hr)) {
				printf("Could not set the audio Balance: %d\n", balance);
			}
		}
	} else {
		if (DSUtil::JDS_DEBUG) {
			printf("No BasicAudio available\n");
		}
	}
}

long DSPlayer::getBalance() {
	long lBalance = 0;

	if (pBasicAudio != NULL) {
		HRESULT hr = pBasicAudio->get_Balance(&lBalance);
		if (DSUtil::JDS_DEBUG) {
			if (FAILED(hr)) {
				printf("Could not get the audio Balance\n");
			}
		}
	} else {
		if (DSUtil::JDS_DEBUG) {
			printf("No BasicAudio available\n");
		}
	}

	return lBalance;
}

void DSPlayer::setMediaPosition(__int64 posInCentNanos) {
	if (pSeeking != NULL) {// should player be stopped?
		// ignore result return values??
		LONGLONG lDur;
		pSeeking->GetDuration(&lDur);

		if (posInCentNanos > (__int64) lDur) {
			posInCentNanos = (__int64) lDur;
		}
		LONGLONG lPos = posInCentNanos;
		// don't change the stop time
		HRESULT hr = pSeeking->SetPositions(&lPos, (DWORD)AM_SEEKING_AbsolutePositioning, 
			NULL, (DWORD)AM_SEEKING_NoPositioning);
		//printf("Setting position...\n");
		//HRESULT hr = pPosition->put_CurrentPosition((REFTIME) (posInCentNanos / 10000));
		if (DSUtil::JDS_DEBUG) {
			if (FAILED(hr)) {
				printf("Native DSPlayer setMediaPosition failed %d\n", lPos);
				if (hr == E_NOTIMPL) {
					printf("E_NOTIMPL\n");
				} else if (hr == E_INVALIDARG) {
					printf("E_INVALIDARG\n");
				}
			} else {
				printf("Native DSPlayer setMediaPosition %d\n", lPos);
			}
		}
	}
}

__int64 DSPlayer::getMediaPosition() {
	if (pSeeking != NULL) {
		LONGLONG lPos;
		pSeeking->GetCurrentPosition(&lPos);
		// ignore result values? S_OK, E_NOTIMPL
		return lPos;
	}
	return 0;
}

__int64 DSPlayer::getDuration() {
	if (pSeeking != NULL) {
		LONGLONG lDur;
		pSeeking->GetDuration(&lDur);
		// ignore result values? S_OK, E_NOTIMPL
		return lDur;
	}
	return 0;
}

HRESULT DSPlayer::setStopPosition(__int64 stopPos) {
	if (pSeeking != NULL) {
		//GUID tFormat;
		//pSeeking->GetTimeFormat(&tFormat);
		//if (tFormat == TIME_FORMAT_MEDIA_TIME) {
		//	wprintf(L"Time Format: Reference Time\n");
		//}
		LONGLONG lDur;
		LONGLONG lPos;
		pSeeking->GetDuration(&lDur);
		
		if (stopPos > (__int64) lDur) {
			lPos = (__int64) lDur;
		} else {
			lPos = stopPos;
		}
		LONGLONG lCur;
		pSeeking->GetCurrentPosition(&lCur);
		lCur++; //increment with one time unit
		HRESULT hr = pSeeking->SetPositions(NULL, AM_SEEKING_NoPositioning, &lPos, AM_SEEKING_AbsolutePositioning);
		// workaround for the case where an interval is played with the stoptime < media duration and stopped 
		// somewhere between start and end of the interval. If then normal playback is resumed the media 
		// playhead jumps to the end once it reaches the previuos stoptime, the end of the interval.
		if (SUCCEEDED(hr)) {
			pSeeking->SetPositions(&lCur, AM_SEEKING_AbsolutePositioning, NULL, AM_SEEKING_NoPositioning);
		}
		return hr;
	} else {
		printf("No media seeking\n");
	}
	return S_OK;
}

__int64 DSPlayer::getStopPosition(){
	if (pSeeking != NULL) {
		LONGLONG lStop;
		pSeeking->GetStopPosition(&lStop);

		return lStop;
	}
	return 0;
}

/*
 * Note: get_AvgTimePerFrame returns a value in seconds, not in reference time units!
 */
double DSPlayer::getTimePerFrame() {
	if (pBasicVideo != NULL) {
		REFTIME pRt;
		pBasicVideo->get_AvgTimePerFrame(&pRt);
		// ignore result values? S_OK, E_NOTIMPL
		return pRt;
	}
	return 0.0;
}

bool DSPlayer::isVisualMedia() {
	return isVideo;
}

long DSPlayer::getOrgVideoWidth() {
	if (pBasicVideo != NULL) {
		long width;
		pBasicVideo->get_VideoWidth(&width);
		return width;
	}
	return 0;
}

long DSPlayer::getOrgVideoHeight() {
	if (pBasicVideo != NULL) {
		long height;
		pBasicVideo->get_VideoHeight(&height);
		return height;
	}
	return 0;
}

HRESULT DSPlayer::getPreferredAspectRatio(long *pX, long *pY) {
	HRESULT hr = NULL;
	if (pGraph != NULL) {
		IBasicVideo2 *pBasicVideo2;
		HRESULT hr = pGraph->QueryInterface(IID_IBasicVideo2, (void **)&pBasicVideo2);
		if (SUCCEEDED(hr)) {
			hr = pBasicVideo2->GetPreferredAspectRatio(pX, pY);
		}
	}
	return hr;
}

/*
 * Wrapper round IBasicVideo.GetCurrentImage. Has to be called twice. 
 * If the second argument is null only the bufSize is filled and the 
 * function returns. Otherwise it is assumed that the bitmap has been
 * created and that the size is correct.
 */
HRESULT DSPlayer::getCurrentImage(long *bufSize, long *pDIBImage) {
	HRESULT hr = NULL;
	if (pBasicVideo != NULL) {
		// check if the player is stopped?
		hr = pBasicVideo->GetCurrentImage(bufSize, pDIBImage);
	} else {
		hr = E_FAIL;
	}

	return hr;
}

HRESULT DSPlayer::cleanUpOnClose() {
	if (cleanUpCalled) {
		return S_OK;
	}

	list<IBaseFilter *>::iterator iter;
	for(iter = filtList.begin(); iter != filtList.end(); iter++) {
		if (*iter != NULL) {
			(*iter)->Release();
		}
	} 
	if (pGraph != NULL) {
		pGraph->Release();
	}
	if (pControl != NULL) {
		pControl->Release();
	}
	if (pSeeking != NULL) {
		pSeeking->Release();
	}
	if (pPosition != NULL) {
		pPosition->Release();
	}
	if (pEvent != NULL) {
		pEvent->Release();
	}
	if (pVideoWindow != NULL) {
		pVideoWindow->put_Owner(NULL);
		pVideoWindow->Release();
	}
	if (pBasicVideo != NULL) {
		pBasicVideo->Release();
	}
	if (pBasicAudio != NULL) {
		pBasicAudio->Release();
	}
	if (mediaPath != NULL) {
		delete(mediaPath);
	}
	if (prefSplitter != NULL) {
		delete(prefSplitter);
	}
	if (pGraphConfig != NULL) {
		pGraphConfig->Release();
	}
	if (pPluginControl != NULL) {
		pPluginControl->Release();
	}

	CoUninitialize();//here??
	cleanUpCalled = true;
	if (DSUtil::JDS_DEBUG) {
		printf("DSPlayer: Cleaning up completed.\n");
	}
	return S_OK;
}

HRESULT DSPlayer::initGraph() {
	if (DSUtil::JDS_DEBUG) {
		printf("Initialising default filter graph\n");
	}
	HRESULT hr;
	// Initialize the COM library
	hr = CoInitializeEx(NULL, COINIT_MULTITHREADED);//APARTMENTTHREADED? COINIT_MULTITHREADED
	if (FAILED(hr)) {
		printf("DSPlayer: Failed to initialize COM: %d.\n", hr);
		return hr;
	}
	if (DSUtil::JDS_DEBUG) {
		printf("COM initialized OK\n");
	}
	// Create the filter graph manager and query for interfaces.
    hr = CoCreateInstance(CLSID_FilterGraph, NULL, CLSCTX_INPROC_SERVER,
                          IID_IGraphBuilder, (void **)&pGraph);
    if (FAILED(hr))
    {
        printf("ERROR - Could not create the Filter Graph Manager.\n");
		//CoUninitialize();  // uninitialize COM
        return hr;
    }
	//printf("Filter Graph Manager initialized OK: %d\n", &pGraph);

	// Using QueryInterface on the graph builder, 
    // Get the Media Control object.
    hr = pGraph->QueryInterface(IID_IMediaControl, (void **)&pControl);
    if (FAILED(hr))
    {
        printf("ERROR - Could not create the Media Control object.\n");
        //pGraph->Release();	// Clean up
		//pGraph = NULL;
		//CoUninitialize();  // uninitalize COM
        return hr;
    }
	if (DSUtil::JDS_DEBUG) {
		printf("Media Control initialized OK: %d\n", &pControl);
	}

	hr = pGraph->QueryInterface(IID_IMediaSeeking, (void **)&pSeeking);
	if (FAILED(hr)) {
		printf("ERROR - Could not create the Media Seeking object.\n");
	}

	hr = pGraph->QueryInterface(IID_IMediaPosition, (void **)&pPosition);
	if (FAILED(hr)) {
		printf("ERROR - Could not create the Media Position object.\n");
	}

	 // And get the Media Event object, too.
    hr = pGraph->QueryInterface(IID_IMediaEvent, (void **)&pEvent);
	if (FAILED(hr))
    {
        printf("ERROR - Could not create the Media Event object.\n");
        //pGraph->Release();	// Clean up
        //pControl->Release();
		//pGraph = NULL;
		//pControl = NULL;
	    //CoUninitialize();  // uninitalize COM
        return hr;
    }
	if (DSUtil::JDS_DEBUG){
		printf("Media Event initialized OK: %d\n", &pEvent);
	}

	hr = pGraph->QueryInterface(IID_IVideoWindow, (void **)&pVideoWindow);
	if (FAILED(hr)){// this even works with audio files, but check first
		printf("ERROR: could not get the video window\n");
		//pGraph->Release();	// Clean up
        //pControl->Release();
		//pEvent->Release();
	    //CoUninitialize();  // uninitalize COM
		return hr;
	}
	if (DSUtil::JDS_DEBUG) {
		printf("Video Window initialized OK: %d\n", &pVideoWindow);
	}

	hr = pGraph->QueryInterface(IID_IBasicVideo, (void **)&pBasicVideo);
	if (FAILED(hr)){// this even works with audio files, but check first
		printf("ERROR: could not get the basic video\n");
		//pGraph->Release();	// Clean up
        //pControl->Release();
		//pEvent->Release();
		//pVideoWindow->Release();
	    //CoUninitialize();  // uninitalize COM
		return hr;
	}

	hr = pGraph->QueryInterface(IID_IBasicAudio, (void **)&pBasicAudio);
	if (FAILED(hr)) {
		printf("ERROR: could not get the basic audio\n");
	}

	if (prefSplitter != NULL) {
		IBaseFilter	  *pSourceFileFilter = NULL;
		IBaseFilter	  *pSplitterFileFilter = NULL;
		// first add a source filter
		hr = pGraph->AddSourceFilter((LPCWSTR)mediaPath, (LPCWSTR)L"Source", &pSourceFileFilter);

		if (FAILED(hr)) {
			printf("Failed to add source filter...\n");
			/*pGraph->Release();
			pControl->Release();
			pEvent->Release();
			pVideoWindow->Release();
			if (pBasicVideo != NULL) {
				pBasicVideo->Release();
			}
			if (pBasicAudio != NULL) {
				pBasicAudio->Release();
			}
			CoUninitialize();*/
			return hr;
		}
		filtList.push_back(pSourceFileFilter);
		// then add the splitter
		DSUtil dsu;
		wchar_t *clsid;
		clsid = dsu.CLSIDFromFriendlyName(prefSplitter);
		if (DSUtil::JDS_DEBUG) {
			wprintf(L"CLSID splitter: %s\n", clsid);
		}
		if (clsid != NULL) {
			// load the filter and add to the graph
			CLSID splitterCLSID;
			HRESULT hrr = CLSIDFromString((LPOLESTR)clsid, (LPCLSID)&splitterCLSID);
			delete(clsid);
			if (SUCCEEDED(hrr)) {			
				hrr = CoCreateInstance(splitterCLSID, NULL, CLSCTX_INPROC_SERVER, IID_IBaseFilter, (void **)&pSplitterFileFilter);
				if (SUCCEEDED(hrr)) {
					filtList.push_back(pSplitterFileFilter);
					hrr = pGraph->AddFilter(pSplitterFileFilter, (LPCWSTR)prefSplitter);
					if (SUCCEEDED(hrr) && DSUtil::JDS_DEBUG){
						printf("initGraph: Added splitter filter to graph\n");
					}
				}
			}		
			// should we anticipate that CLSID is not NULL but loading and adding the filter failed??
			// then call render on the source filter output pin or splitter output pins
			IPin *pOutputPin = NULL;
			IPin *pInputPin = NULL;

			pOutputPin = GetPin(pSourceFileFilter, PINDIR_OUTPUT);
		
			if (pOutputPin != NULL) {
				pInputPin = GetPin(pSplitterFileFilter, PINDIR_INPUT);
				if (pInputPin != NULL){
					hr = pGraph->ConnectDirect(pOutputPin, pInputPin, NULL);// don't use intelligent connect
					if (FAILED(hr)) {
						printf("initGraph: Could not connect source filter and splitter, rendering source\n");
						hr = pGraph->Render(pOutputPin);// renders the source
						//return hr;
					} else {
						// render all output pins of splitter
						RenderAllOutputPins(pGraph, pSplitterFileFilter);
					}
				} else {
					printf("Could not get the splitter input pin, rendering the source output pin\n");
					// this creates a video renderer
					hr = pGraph->Render(pOutputPin);// renders the source
					if (FAILED(hr)) {
						// hier clean up?
						wprintf(L"initGraph: Could not render the file: %s\n\n", mediaPath);
						return hr;
					}
				}
			} else {
				printf("Could not get the source output pin\n");
				// clean up
				return VFW_E_NOT_FOUND;
			}
		} else { // CLSID of preferred splitter is null, codec not installed
			printf("Could not load the preferred codec, reverting to standard Intelligent Connect\n");
			hr = pGraph->RenderFile((LPCWSTR)mediaPath, NULL);
			if (FAILED(hr)) {// hier clean up?
				wprintf(L"Could not render the file: %s\n", mediaPath);
				return hr;
			}
		}
	} else {
		// no preferred splitter codec
		//wprintf(L"Native: Media path: %s\n", mediaPath);
		hr = pGraph->RenderFile((LPCWSTR)mediaPath, NULL);
		if (FAILED(hr)) { // hier clean up?
			wprintf(L"Could not render the file: %s\n", mediaPath);
			return hr;
		}
	}

	//printf("Basic Video initialized OK: %d\n", &pBasicVideo);
	// this fails in case of audio. Seems the easiest and the most reliable way(?) 
	// to check whether the media has visible media (rather than relying on media (sub)types
	hr = pVideoWindow->put_WindowStyle(WS_CHILD);
	if (FAILED(hr)) {
		printf("ERROR: could not set the video window style\n");
		isVideo = false;
	}
	//hr = pVideoWindow->put_Visible(-1);
	hr = pControl->Pause();
	//hr = pControl->Run();
	if (DSUtil::JDS_DEBUG) {
		DSUtil dsu;
		dsu.printFilters(pGraph);
	}
	//hh = dsu.printAllFilters();
	return S_OK;
}


HRESULT DSPlayer::createDefaultMPEG1Graph() {
		if (DSUtil::JDS_DEBUG) {
		printf("Creating default MPEG-1 graph\n");
	}
	HRESULT hr;
	// Initialize the COM library
	hr = CoInitializeEx(NULL, COINIT_MULTITHREADED);//APARTMENTTHREADED? COINIT_MULTITHREADED
	if (FAILED(hr)) {
		return hr;
	}
	if (DSUtil::JDS_DEBUG) {
		printf("COM initialized OK\n");
	}
	// Create the filter graph manager and query for interfaces.
	// using this way a message loop thread has to be created
    hr = CoCreateInstance(CLSID_FilterGraph, NULL, CLSCTX_INPROC_SERVER,
                          IID_IGraphBuilder, (void **)&pGraph);
    if (FAILED(hr))
    {
        printf("ERROR - Could not create the Filter Graph Manager.\n");
		//CoUninitialize();  // uninitialize COM
        return hr;
    }
	if (DSUtil::JDS_DEBUG) {
		printf("Filter Graph Manager initialized OK: %d\n", &pGraph);
	}

	IBaseFilter	  *pSourceFileFilter = NULL;
	IBaseFilter	  *pSplitterFileFilter = NULL;
	IBaseFilter   *pMpegAudioDecFileFilter = NULL;
	IBaseFilter   *pMpegVideoDecFileFilter = NULL;
	IBaseFilter   *pDirectSoundFileFilter = NULL;
	IBaseFilter   *pVideoRendererFileFilter = NULL;
	// more filters
	//HRESULT hr = NULL;
	if (DSUtil::JDS_DEBUG) {
		wprintf(L"Native: Media path: %s\n", mediaPath);
	}
	hr = pGraph->AddSourceFilter((LPCWSTR)mediaPath, (LPCWSTR)L"Source", &pSourceFileFilter);

	if (FAILED(hr)) {
		printf("Failed to add source filter...\n");
		//pGraph->Release();
		//pGraph = NULL;
		//CoUninitialize();
		return hr;
	}
	filtList.push_back(pSourceFileFilter);
	if (DSUtil::JDS_DEBUG) {
		printf("Added source filter...\n");
	}
	hr = CoCreateInstance(CLSID_MPEG1Splitter, NULL, CLSCTX_INPROC_SERVER, IID_IBaseFilter, (void **)&pSplitterFileFilter);
	if (SUCCEEDED(hr)) {
		filtList.push_back(pSplitterFileFilter);
		hr = pGraph->AddFilter(pSplitterFileFilter, (LPCWSTR)L"MPEG1 Splitter");		
	}
	if (FAILED(hr)) {
		printf("Failed to create and add mpeg1 spitter\n");
		//pGraph->Release();
		//pGraph = NULL;
		//CoUninitialize();
		return hr;
	}
	if (DSUtil::JDS_DEBUG) {
		printf("Added MPEG-1 Splitter\n");
	}
	// hier... only create the audio chain if there is an audio output pin?
	hr = CoCreateInstance(CLSID_CMpegAudioCodec, NULL, CLSCTX_INPROC_SERVER, IID_IBaseFilter, (void **)&pMpegAudioDecFileFilter);
	if (SUCCEEDED(hr)) {
		filtList.push_back(pMpegAudioDecFileFilter);
		hr = pGraph->AddFilter(pMpegAudioDecFileFilter, (LPCWSTR)L"MPEG1 Audio Decoder");
	}
	if (FAILED(hr)) {
		printf("Failed to create and add mpeg1 audio decoder\n");
		//return hr;// hier don't return, continue
	} else {
		if (DSUtil::JDS_DEBUG) {
			printf("Added MPEG-1 Audio Decoder\n");
		}
	}
	
	hr = CoCreateInstance(CLSID_CMpegVideoCodec, NULL, CLSCTX_INPROC_SERVER, IID_IBaseFilter, (void **)&pMpegVideoDecFileFilter);
	if (SUCCEEDED(hr)) {
		filtList.push_back(pMpegVideoDecFileFilter);
		hr = pGraph->AddFilter(pMpegVideoDecFileFilter, (LPCWSTR)L"MPEG1 Video Decoder");
	}
	if (FAILED(hr)) {
		printf("Failed to create and add mpeg1 video decoder\n");
		//pGraph->Release();
		//pGraph = NULL;
		//pSplitterFileFilter->Release();
		//pMpegVideoDecFileFilter->Release();
		//if(pMpegAudioDecFileFilter != NULL) {
		//	pMpegAudioDecFileFilter->Release();
		//}
		//CoUninitialize();
		return hr;
	}
	if (DSUtil::JDS_DEBUG) {
		printf("Added MPEG-1 Video Decoder\n");
	}

	hr = CoCreateInstance(CLSID_DSoundRender, NULL, CLSCTX_INPROC_SERVER, IID_IBaseFilter, (void **)&pDirectSoundFileFilter);
	if (SUCCEEDED(hr)) {
		filtList.push_back(pDirectSoundFileFilter);
		hr = pGraph->AddFilter(pDirectSoundFileFilter, (LPCWSTR)L"Direct Sound Renderer");
	}
	if (FAILED(hr)) {
		printf("Failed to create and add direct sound renderer\n");
		//pGraph->Release();
		//pGraph = NULL;
		//pSplitterFileFilter->Release();
		//pMpegVideoDecFileFilter->Release();
		//pMpegAudioDecFileFilter->Release();
		//if (pDirectSoundFileFilter != NULL) {
		//	pDirectSoundFileFilter->Release();
		//}
		//CoUninitialize();
		return hr;
	}
	if (DSUtil::JDS_DEBUG) {
		printf("Added direct sound renderer\n");
	}
	// if the video renderer is created with CoCreateInstance, window messages should be handled
	// on the application thread (or a worker thread should be created?)
	//vmr9 {51B4ABF3-748F-4E3B-A276-C828330E926A}  CLSID_VideoMixingRenderer9
	// CLSID_VideoRenderer
	// CLSID_VideoRendererDefault
	// CLSID_VideoMixingRenderer
	/*
	hr = CoCreateInstance(CLSID_VideoRenderer, NULL, CLSCTX_INPROC_SERVER, IID_IBaseFilter, (void **)&pVideoRendererFileFilter);
	if (SUCCEEDED(hr)) {
		hr = pGraph->AddFilter(pVideoRendererFileFilter, (LPCWSTR)L"Video Renderer");
	}
	if (FAILED(hr)) {
		printf("Failed to create and add video renderer\n");
		return hr;
	}
	printf("Added video renderer\n");
	// this is for the mixing renderer
	//IVMRFilterConfig *pConfig;
	//hr = pVideoRendererFileFilter->QueryInterface(IID_IVMRFilterConfig, (void **)&pConfig);
	//if(SUCCEEDED(hr)) {
	//	DWORD pMode;
	//	pConfig->GetRenderingMode(&pMode);
	//	printf("Rendering Mode: %d\n", pMode);
	//}
*/
	// connect pins, or don't

	IPin *pOutputPin = NULL;
	IPin *pInputPin = NULL;

	pOutputPin = GetPin(pSourceFileFilter, PINDIR_OUTPUT);
	
	if (pOutputPin != NULL) {
		pInputPin = GetPin(pSplitterFileFilter, PINDIR_INPUT);
		if (pInputPin != NULL){
			hr = pGraph->ConnectDirect(pOutputPin, pInputPin, NULL);// don't use intelligent connect
			if (FAILED(hr)) {
				printf("Could not connect source filter and splitter\n");
				pOutputPin->Release();
				return hr;
			}
		} else {
			printf("Could not get the splitter input pin\n");
			return VFW_E_NOT_FOUND;
		}
	} else {
		printf("Could not get the source output pin\n");
		return VFW_E_NOT_FOUND;
	}
	
	// can we re-use the pins pointers?
	hr = pSplitterFileFilter->FindPin((LPCWSTR)L"Audio", &pOutputPin);
	if(SUCCEEDED(hr)) {
		pInputPin = GetPin(pMpegAudioDecFileFilter, PINDIR_INPUT);
		if (pInputPin != NULL) {
			hr = pGraph->ConnectDirect(pOutputPin, pInputPin, NULL);
			if (FAILED(hr)) {
				printf("Could not connect splitter and audio decoder\n");
				pOutputPin->Release();
				return hr;
			}
		} else {
			printf("Could not get audio decoder input pin\n");
			return VFW_E_NOT_FOUND;
		}
	}
	if (DSUtil::JDS_DEBUG) {
		printf("Connected splitter and audio decoder\n");
	}
	hr = pSplitterFileFilter->FindPin((LPCWSTR)L"Video", &pOutputPin);
	if(SUCCEEDED(hr)) {
		pInputPin = GetPin(pMpegVideoDecFileFilter, PINDIR_INPUT);
		if (pInputPin != NULL) {
			hr = pGraph->ConnectDirect(pOutputPin, pInputPin, NULL);
			if (FAILED(hr)) {
				printf("Could not connect splitter and video decoder\n");
				return hr;
			}
		} else {
			printf("Could not get video decoder input pin\n");
			return VFW_E_NOT_FOUND;
		}
	}
	if (DSUtil::JDS_DEBUG) {
		printf("Connected splitter and video decoder\n");
	}

	pOutputPin = GetPin(pMpegAudioDecFileFilter, PINDIR_OUTPUT);	
	if(pOutputPin != NULL) {
		pInputPin = GetPin(pDirectSoundFileFilter, PINDIR_INPUT);
		if (pInputPin != NULL) {
			hr = pGraph->ConnectDirect(pOutputPin, pInputPin, NULL);
			if (FAILED(hr)) {
				printf("Could not connect audio decoder and audio renderer\n");
				return hr;
			}
		} else {
			printf("Could not get audio renderer input pin\n");
			return VFW_E_NOT_FOUND;
		}
	} else {
		printf("Could not get audio decoder output pin\n");
		return VFW_E_NOT_FOUND;
	}
	if (DSUtil::JDS_DEBUG) {
		printf("Connected audio decoder and audio renderer\n");
	}

	pOutputPin = GetPin(pMpegVideoDecFileFilter, PINDIR_OUTPUT);
	/* can only be used when the VideoRenderer has been created
	if(pOutputPin != NULL) {
		pInputPin = GetPin(pVideoRendererFileFilter, PINDIR_INPUT);
		if (pInputPin != NULL) {
			//pGraph->Render(pOutputPin);
			hr = pGraph->ConnectDirect(pOutputPin, pInputPin, NULL);
			//hr = pGraph->Connect(pOutputPin, pInputPin);
			if (FAILED(hr)) {
				printf("Could not connect video decoder and video renderer\n");
				return hr;
			}
		} else {
			printf("Could not get video renderer input pin\n");
			return VFW_E_NOT_FOUND;
		}
	} else {
		printf("Could not get video decoder output pin\n");
		return VFW_E_NOT_FOUND;
	}
	printf("Connected video decoder and video renderer\n");
	*/
	//hr = pGraph->Render(pOutputPin);

	//if (FAILED(hr)) {
	//	printf("Failed to render the source output pin");
	//	return false;
	//}
	// Using QueryInterface on the graph builder, 
    // Get the Media Control object.
    hr = pGraph->QueryInterface(IID_IMediaControl, (void **)&pControl);
    if (FAILED(hr))
    {
        printf("ERROR - Could not create the Media Control object.\n");
        //pGraph->Release();	// Clean up
		//pGraph = NULL;
		//CoUninitialize();  // uninitalize COM
        return hr;
    }
	if (DSUtil::JDS_DEBUG) {
		printf("Media Control initialized OK: %d\n", &pControl);
	}

	hr = pGraph->QueryInterface(IID_IMediaSeeking, (void **)&pSeeking);
	if (FAILED(hr)) {
		printf("ERROR - Could not create the Media Seeking object.\n");
	}
	hr = pGraph->QueryInterface(IID_IMediaPosition, (void **)&pPosition);
	if (FAILED(hr)) {
		printf("ERROR - Could not create the Media Position object.\n");
	}

	 // And get the Media Event object, too.
    hr = pGraph->QueryInterface(IID_IMediaEvent, (void **)&pEvent);
	if (FAILED(hr))
    {
        printf("ERROR - Could not create the Media Event object.\n");
        //pGraph->Release();	// Clean up
        //pControl->Release();
		//pGraph = NULL;
		//pControl = NULL;
	    //CoUninitialize();  // uninitalize COM
        return hr;
    }
	if (DSUtil::JDS_DEBUG) {
		printf("Media Event initialized OK: %d\n", &pEvent);
	}
	//is it possible to cocreate a videowindow as the docs suggest?
	hr = pGraph->QueryInterface(IID_IVideoWindow, (void **)&pVideoWindow);
	//hr = pVideoRendererFileFilter->QueryInterface(IID_IVideoWindow, (void **)&pVideoWindow);
	if (FAILED(hr)){// this even works with audio files, but check first
		printf("ERROR: could not get the video window\n");
		//pGraph->Release();	// Clean up
        //pControl->Release();
		//pEvent->Release();
	    //CoUninitialize();  // uninitalize COM
		return hr;
	}
	if (DSUtil::JDS_DEBUG) {
		printf("Video Window initialized OK: %d\n", &pVideoWindow);
	}
	
	hr = pGraph->QueryInterface(IID_IBasicVideo, (void **)&pBasicVideo);
	if (FAILED(hr)){// this even works with audio files, but check first
		printf("ERROR: could not get the basic video\n");
		//pGraph->Release();	// Clean up
        //pControl->Release();
		//pEvent->Release();
		//pVideoWindow->Release();
	    //CoUninitialize();  // uninitalize COM
		return hr;
	}
	if (DSUtil::JDS_DEBUG) {
		printf("Basic Video initialized OK: %d\n", &pBasicVideo);
	}
	
	hr = pGraph->QueryInterface(IID_IBasicAudio, (void **)&pBasicAudio);
	if (FAILED(hr)) {
		printf("ERROR: could not get the basic audio\n");
	}

	//hr = pGraph->RenderFile((LPCWSTR)mediaPath, NULL);
	// this creates a video renderer
	hr = pGraph->Render(pOutputPin);
	if (FAILED(hr)) {
		wprintf(L"Could not render the file: %s\n", mediaPath);
		return hr;
	}
	hr = pVideoWindow->put_WindowStyle(WS_CHILD);// this fails in case of audio
	if (FAILED(hr)) {
		printf("ERROR: could not set the video window style\n");
		isVideo = false;
	}
	hr = pVideoWindow->put_Visible(-1);
	hr = pControl->Pause();
	if (DSUtil::JDS_DEBUG) {
		DSUtil dsu;
		dsu.printFilters(pGraph);
	}
	return S_OK;
}

// find a solution for releasing created filters and pointers
HRESULT DSPlayer::createDefaultMPEG2Graph() {
	if (DSUtil::JDS_DEBUG) {
		printf("Creating default MPEG-2 graph\n");
	}
	HRESULT hr;
	// Initialize the COM library
	hr = CoInitializeEx(NULL, COINIT_MULTITHREADED);//APARTMENTTHREADED? COINIT_MULTITHREADED
	if (FAILED(hr)) {
		return hr;
	}
	if (DSUtil::JDS_DEBUG) {
		printf("COM initialized OK\n");
	}
	//check if the Elecard Demultiplexer is installed
	//clsid of Elecard MPEG Demultiplexer is {136DCBF5-3874-4B70-AE3E-15997D6334F7}
	CLSID splitterCLSID;
	hr = CLSIDFromString((LPOLESTR)L"{136DCBF5-3874-4B70-AE3E-15997D6334F7}", (LPCLSID)&splitterCLSID);
	if (hr != NOERROR) {
		//CoUninitialize();  // uninitalize COM
		return hr;
	}
	// Create the filter graph manager and query for interfaces.
	// using this way a message loop thread has to be created
    hr = CoCreateInstance(CLSID_FilterGraph, NULL, CLSCTX_INPROC_SERVER,
                          IID_IGraphBuilder, (void **)&pGraph);
    if (FAILED(hr))
    {
        printf("ERROR - Could not create the Filter Graph Manager.\n");
		//CoUninitialize();  // uninitialize COM
        return hr;
    }
	if (DSUtil::JDS_DEBUG) {
		printf("Filter Graph Manager initialized OK: %d\n", &pGraph);
	}

	IBaseFilter	  *pSourceFileFilter = NULL;
	IBaseFilter	  *pSplitterFileFilter = NULL;
	IBaseFilter   *pMpegAudioDecFileFilter = NULL;
	IBaseFilter   *pMpegVideoDecFileFilter = NULL;
	IBaseFilter   *pElecardVideoDecFileFilter = NULL;
	IBaseFilter   *pDirectSoundFileFilter = NULL;
	//IBaseFilter   *pVideoRendererFileFilter = NULL;
	if (DSUtil::JDS_DEBUG) {
		wprintf(L"Native: Media path: %s\n", mediaPath);
	}
	hr = pGraph->AddSourceFilter((LPCWSTR)mediaPath, (LPCWSTR)L"Source", &pSourceFileFilter);

	if (FAILED(hr)) {
		printf("Failed to add source filter...\n");
		//pGraph->Release();
		//CoUninitialize();
		return hr;
	}
	filtList.push_back(pSourceFileFilter);
	if (DSUtil::JDS_DEBUG) {
		printf("Added source filter...\n");
	}

	hr = CoCreateInstance(splitterCLSID, NULL, CLSCTX_INPROC_SERVER, IID_IBaseFilter, (void **)&pSplitterFileFilter);
	if (SUCCEEDED(hr)) {
		filtList.push_back(pSplitterFileFilter);
		hr = pGraph->AddFilter(pSplitterFileFilter, (LPCWSTR)L"Elecard MPEG Demultiplexer");
	}
	if (FAILED(hr)) {
		printf("Failed to create and add the Elecard MPEG Demultiplexer \n");
		//pGraph->Release();
		//pSplitterFileFilter->Release();
		//CoUninitialize();
		return hr;
	}

	if (DSUtil::JDS_DEBUG) {
		printf("Added Elecard MPEG Demultiplexer\n");
	}

	// only create the audio chain if there is an audio output pin? 
	// what is the audio clsid of the Elecard audio decoder: 7EC22329-A916-4F81-9926-187C4350D317
	hr = CoCreateInstance(CLSID_CMpegAudioCodec, NULL, CLSCTX_INPROC_SERVER, IID_IBaseFilter, (void **)&pMpegAudioDecFileFilter);
	if (SUCCEEDED(hr)) {
		filtList.push_back(pMpegAudioDecFileFilter);
		hr = pGraph->AddFilter(pMpegAudioDecFileFilter, (LPCWSTR)L"MPEG Audio Decoder");
	}
	if (FAILED(hr)) {
		printf("Failed to create and add audio decoder\n");
		return hr;// hier don't return, continue
	}
	if (DSUtil::JDS_DEBUG) {
		printf("Added MPEG-2 Audio Decoder\n");
	}

	hr = CoCreateInstance(CLSID_DSoundRender, NULL, CLSCTX_INPROC_SERVER, IID_IBaseFilter, (void **)&pDirectSoundFileFilter);
	if (SUCCEEDED(hr)) {
		filtList.push_back(pDirectSoundFileFilter);
		hr = pGraph->AddFilter(pDirectSoundFileFilter, (LPCWSTR)L"Direct Sound Renderer");
	}
	if (FAILED(hr)) {
		printf("Failed to create and add direct sound renderer\n");
		return hr;
	}
	if (DSUtil::JDS_DEBUG) {
		printf("Added direct sound renderer\n");
	}
	// don't do the above if there is no audio??

	hr = CoCreateInstance(CLSID_CMpegVideoCodec, NULL, CLSCTX_INPROC_SERVER, IID_IBaseFilter, (void **)&pMpegVideoDecFileFilter);
	if (SUCCEEDED(hr)) {
		filtList.push_back(pMpegVideoDecFileFilter);
		hr = pGraph->AddFilter(pMpegVideoDecFileFilter, (LPCWSTR)L"MPEG Video Decoder");
	}
	if (FAILED(hr)) {
		printf("Failed to create and add mpeg video decoder\n");
		return hr;
	}
	if (DSUtil::JDS_DEBUG) {
		printf("Added MPEG Video Decoder\n");
	}

	// connect pins, or don't

	IPin *pOutputPin = NULL;
	IPin *pInputPin = NULL;

	pOutputPin = GetPin(pSourceFileFilter, PINDIR_OUTPUT);
	
	if (pOutputPin != NULL) {
		pInputPin = GetPin(pSplitterFileFilter, PINDIR_INPUT);
		if (pInputPin != NULL){
			hr = pGraph->ConnectDirect(pOutputPin, pInputPin, NULL);// don't use intelligent connect
			if (FAILED(hr)) {
				printf("Could not connect source filter and splitter\n");
				return hr;
			}
		} else {
			printf("Could not get the splitter input pin\n");
			return VFW_E_NOT_FOUND;
		}
	} else {
		printf("Could not get the source output pin\n");
		return VFW_E_NOT_FOUND;
	}
	
	// can we re-use the pins pointers?
	hr = pSplitterFileFilter->FindPin((LPCWSTR)L"Audio (ID 192 @ Prog# 0)", &pOutputPin);
	if(SUCCEEDED(hr)) {
		pInputPin = GetPin(pMpegAudioDecFileFilter, PINDIR_INPUT);
		if (pInputPin != NULL) {
			hr = pGraph->ConnectDirect(pOutputPin, pInputPin, NULL);
			if (FAILED(hr)) {
				printf("Could not connect splitter and audio decoder\n");
				return hr;
			}
		} else {
			printf("Could not get audio decoder input pin\n");
			return VFW_E_NOT_FOUND;
		}
	}
	printf("Connected splitter and audio decoder\n");

	hr = pSplitterFileFilter->FindPin((LPCWSTR)L"Video (ID 224 @ Prog# 0)", &pOutputPin);
	if(SUCCEEDED(hr)) {
		pInputPin = GetPin(pMpegVideoDecFileFilter, PINDIR_INPUT);
		if (pInputPin != NULL) {
			hr = pGraph->ConnectDirect(pOutputPin, pInputPin, NULL);
			if (FAILED(hr)) {
				printf("Could not connect splitter and MPEG video decoder\n");
				return hr;
				// here try the Elecard video decoder
				/* untested alternative?
				CLSID elecardVidDecoderCLSID;
				hr = CLSIDFromString((LPOLESTR)L"{BC4EB321-771F-4E9F-AF67-37C631ECA106}", (LPCLSID)&elecardVidDecoderCLSID);
				if (hr != NOERROR) {
					return hr;
				}
				hr = CoCreateInstance(elecardVidDecoderCLSID, NULL, CLSCTX_INPROC_SERVER, IID_IBaseFilter, 
					(void **)&pElecardVideoDecFileFilter);
				if (SUCCEEDED(hr)) {
					filtList.push_back(pElecardVideoDecFileFilter);
					hr = pGraph->AddFilter(pElecardVideoDecFileFilter, (LPCWSTR)L"Elecard MPEG-2 Video Decoder HD");
					pGraph->RemoveFilter(pMpegVideoDecFileFilter);
					pMpegVideoDecFileFilter->Release();
					pMpegVideoDecFileFilter = NULL;
				}
				if (FAILED(hr)) {
					printf("Could not create or add Elecard MPEG video decoder\n");
					return hr;
				}
				hr = pElecardVideoDecFileFilter->FindPin((LPCWSTR)L"In", &pInputPin);// Id is not "MPEG In" but just "In"
				if (pInputPin != NULL) {
					hr = pGraph->ConnectDirect(pOutputPin, pInputPin, NULL);

					if (FAILED(hr)) {
						printf("Could not connect splitter and Elecard video decoder\n");
						return hr;
					} else {
						if (DSUtil::JDS_DEBUG) {
							printf("Connected the splitter and the Elecard video decoder\n");
						}
					}
				} else {
					printf("Could not find the video input pin of Elecard video decoder\n");
					return VFW_E_NOT_FOUND;
				}
				*/
				// end elecard decoder
			}
		} else {
			printf("Could not get video decoder input pin\n");
			return VFW_E_NOT_FOUND;
		}
	}
	if (DSUtil::JDS_DEBUG) {
		printf("Connected splitter and video decoder\n");
	}

	pOutputPin = GetPin(pMpegAudioDecFileFilter, PINDIR_OUTPUT);	
	if(pOutputPin != NULL) {
		pInputPin = GetPin(pDirectSoundFileFilter, PINDIR_INPUT);
		if (pInputPin != NULL) {
			hr = pGraph->ConnectDirect(pOutputPin, pInputPin, NULL);
			if (FAILED(hr)) {
				printf("Could not connect audio decoder and audio renderer\n");
				return hr;
			}
		} else {
			printf("Could not get audio renderer input pin\n");
			return VFW_E_NOT_FOUND;
		}
	} else {
		printf("Could not get audio decoder output pin\n");
		return VFW_E_NOT_FOUND;
	}
	if (DSUtil::JDS_DEBUG) {
		printf("Connected audio decoder and audio renderer\n");
	}

	if (pMpegVideoDecFileFilter != NULL) {
		pOutputPin = GetPin(pMpegVideoDecFileFilter, PINDIR_OUTPUT);
	} else {
		if (pElecardVideoDecFileFilter != NULL) {
			hr = pElecardVideoDecFileFilter->FindPin((LPCWSTR)L"Out", &pOutputPin);// pin id is "Out" not "Video Out"
		}
		// return on fail or just continue and see what happens?
		//if (pOutputPin == NULL) {
		//	return hr;
		//}
	}
	
	/* can only be used when the VideoRenderer has been created
	if(pOutputPin != NULL) {
		pInputPin = GetPin(pVideoRendererFileFilter, PINDIR_INPUT);
		if (pInputPin != NULL) {
			//pGraph->Render(pOutputPin);
			hr = pGraph->ConnectDirect(pOutputPin, pInputPin, NULL);
			//hr = pGraph->Connect(pOutputPin, pInputPin);
			if (FAILED(hr)) {
				printf("Could not connect video decoder and video renderer\n");
				return hr;
			}
		} else {
			printf("Could not get video renderer input pin\n");
			return VFW_E_NOT_FOUND;
		}
	} else {
		printf("Could not get video decoder output pin\n");
		return VFW_E_NOT_FOUND;
	}
	printf("Connected video decoder and video renderer\n");
	*/
	//hr = pGraph->Render(pOutputPin);

	//if (FAILED(hr)) {
	//	printf("Failed to render the source output pin");
	//	return false;
	//}
	// Using QueryInterface on the graph builder, 
    // Get the Media Control object.
    hr = pGraph->QueryInterface(IID_IMediaControl, (void **)&pControl);
    if (FAILED(hr))
    {
        printf("ERROR - Could not create the Media Control object.\n");
        //pGraph->Release();	// Clean up
		//pGraph = NULL;
		//CoUninitialize();  // uninitalize COM
        return hr;
    }
	if (DSUtil::JDS_DEBUG) {
		printf("Media Control initialized OK: %d\n", &pControl);
	}

	hr = pGraph->QueryInterface(IID_IMediaSeeking, (void **)&pSeeking);
	if (FAILED(hr)) {
		printf("ERROR - Could not create the Media Seeking object.\n");
	}

	hr = pGraph->QueryInterface(IID_IMediaPosition, (void **)&pPosition);
	if (FAILED(hr)) {
		printf("ERROR - Could not create the Media Position object.\n");
	}

	 // And get the Media Event object, too.
    hr = pGraph->QueryInterface(IID_IMediaEvent, (void **)&pEvent);
	if (FAILED(hr))
    {
        printf("ERROR - Could not create the Media Event object.\n");
        //pGraph->Release();	// Clean up
        //pControl->Release();
		//pGraph = NULL;
		//pControl = NULL;
	    //CoUninitialize();  // uninitalize COM
        return hr;
    }
	if (DSUtil::JDS_DEBUG) {
		printf("Media Event initialized OK: %d\n", &pEvent);
	}
	//is it possible to cocreate a videowindow as the docs suggest?
	hr = pGraph->QueryInterface(IID_IVideoWindow, (void **)&pVideoWindow);
	//hr = pVideoRendererFileFilter->QueryInterface(IID_IVideoWindow, (void **)&pVideoWindow);
	if (FAILED(hr)){// this even works with audio files, but check first
		printf("ERROR: could not get the video window\n");
		//pGraph->Release();	// Clean up
        //pControl->Release();
		//pEvent->Release();
	    //CoUninitialize();  // uninitalize COM
		return hr;
	}
	if (DSUtil::JDS_DEBUG) {
		printf("Video Window initialized OK: %d\n", &pVideoWindow);
	}
	
	hr = pGraph->QueryInterface(IID_IBasicVideo, (void **)&pBasicVideo);
	if (FAILED(hr)){// this even works with audio files, but check first
		printf("ERROR: could not get the basic video\n");
		//pGraph->Release();	// Clean up
        //pControl->Release();
		//pEvent->Release();
		//pVideoWindow->Release();
	    //CoUninitialize();  // uninitalize COM
		return hr;
	}
	if (DSUtil::JDS_DEBUG) {
		printf("Basic Video initialized OK: %d\n", &pBasicVideo);
	}
	
	hr = pGraph->QueryInterface(IID_IBasicAudio, (void **)&pBasicAudio);
	if (FAILED(hr)) {
		printf("ERROR: could not get the basic audio\n");
	}

	//hr = pGraph->RenderFile((LPCWSTR)mediaPath, NULL);
	// this creates a video renderer
	hr = pGraph->Render(pOutputPin);
	if (FAILED(hr)) {
		wprintf(L"Could not render the file: %s\n", mediaPath);
		return hr;
	}
	hr = pVideoWindow->put_WindowStyle(WS_CHILD);// this fails in case of audio
	if (FAILED(hr)) {
		printf("ERROR: could not set the video window style\n");
		isVideo = false;
	}
	hr = pVideoWindow->put_Visible(-1);
	hr = pControl->Pause();

	if (DSUtil::JDS_DEBUG) {
		DSUtil dsu;
		dsu.printFilters(pGraph);
	}
	return S_OK;
}

// new PluginControl method. Windows 7 only. Not tested, status unknown
HRESULT DSPlayer::createDefaultMPEG2GraphPluginControl() {
	if (DSUtil::JDS_DEBUG) {
		printf("Creating default MPEG-2 graph, with plugin control\n");
	}
	HRESULT hr;
	// Initialize the COM library
	hr = CoInitializeEx(NULL, COINIT_MULTITHREADED);//APARTMENTTHREADED? COINIT_MULTITHREADED
	if (FAILED(hr)) {
		return hr;
	}
	if (DSUtil::JDS_DEBUG) {
		printf("COM initialized OK\n");
	}

	//check if the Elecard Demultiplexer is installed
	//clsid of Elecard MPEG Demultiplexer is {136DCBF5-3874-4B70-AE3E-15997D6334F7}
	CLSID splitterCLSID;
	hr = CLSIDFromString((LPOLESTR)L"{136DCBF5-3874-4B70-AE3E-15997D6334F7}", (LPCLSID)&splitterCLSID);
	if (hr != NOERROR) {
		printf("Failed to get the CLSID of the Elecard MPEG Demultiplexer\n");
		return hr;
	}
	// retrieving the CLSID works even if the filter is not installed...
	printf("The CLSID of the Elecard MPEG Demultiplexer: %d\n", splitterCLSID);
	// Create the filter graph manager and query for interfaces.
	// using this way a message loop thread has to be created
    hr = CoCreateInstance(CLSID_FilterGraph, NULL, CLSCTX_INPROC_SERVER,
                          IID_IGraphBuilder, (void **)&pGraph);
    if (FAILED(hr)) {
        printf("ERROR - Could not create the Filter Graph Manager.\n");
        return hr;
    }
	if (DSUtil::JDS_DEBUG) {
		printf("Filter Graph Manager initialized OK: %d\n", &pGraph);
	}

	IBaseFilter	  *pSplitterFileFilter = NULL;
	HRESULT splithr = CoCreateInstance(splitterCLSID, NULL, CLSCTX_INPROC_SERVER, IID_IBaseFilter, (void **)&pSplitterFileFilter);

	if (FAILED(splithr)) {
		printf("Failed to create and add the Elecard MPEG Demultiplexer to the cache\n");
		return splithr;
	}

    // Get the Graph Config object.
    hr = pGraph->QueryInterface(IID_IGraphConfig, (void **)&pGraphConfig);
    if (FAILED(hr)) {
		printf("ERROR - Could not create the Filter Graph Config.\n");
        return hr;
	}
	if (DSUtil::JDS_DEBUG) {
		printf("Filter Graph Config initialized OK: %d\n", &pGraphConfig);
	}

	if (SUCCEEDED(splithr)) {
		filtList.push_back(pSplitterFileFilter);
		//hr = pGraph->AddFilter(pSplitterFileFilter, (LPCWSTR)L"Elecard MPEG Demultiplexer");
		hr = pGraphConfig->AddFilterToCache(pSplitterFileFilter);
		if (DSUtil::JDS_DEBUG) {
			printf("Added Elecard MPEG Demultiplexer to the cache\n");
		}
	}

	IBaseFilter	  *pSourceFileFilter = NULL;
	IBaseFilter   *pMpegAudioDecFileFilter = NULL;
	IBaseFilter   *pMpegVideoDecFileFilter = NULL;
	IBaseFilter   *pDirectSoundFileFilter = NULL;
	//IBaseFilter   *pVideoRendererFileFilter = NULL;
	if (DSUtil::JDS_DEBUG) {
		wprintf(L"Native: Media path: %s\n", mediaPath);
	}

	// on win 7 disable a few Microsoft decoders that get in the graph after the Elecard splitter
	// Microsoft DTV-DVD Video Decoder = CLSID {212690FB-83E5-4526-8FD7-74478B7939CD}
	// Microsoft DTV-DVD Audio Decoder = CLSID {E1F1A0B8-BEEE-490D-BA7C-066C40B5E2B9}

	hr = CoCreateInstance(CLSID_DirectShowPluginControl, NULL, CLSCTX_INPROC_SERVER,
						IID_IAMPluginControl, (void **)&pPluginControl);
	if (FAILED(hr)) {
		printf("ERROR - Could not create the Plugin Control.\n");
		return hr;
	} else {
		// enable/disable filters could check whether there is audio or video first
		IBaseFilter	  *pAudioDecoder = NULL;
		IBaseFilter	  *pVideoDecoder = NULL;

		CLSID audioCLSID;
		HRESULT auClsid = CLSIDFromString((LPOLESTR)L"{E1F1A0B8-BEEE-490D-BA7C-066C40B5E2B9}", (LPCLSID)&audioCLSID);
		if (SUCCEEDED(auClsid)){
			printf("Retrieved CLSID for Microsoft Audio Decoder\n");
			hr = pPluginControl->SetDisabled(audioCLSID, true);
			if (DSUtil::JDS_DEBUG) {
				if (SUCCEEDED(hr)){
					printf("Disabled Microsoft Audio Decoder\n");
				} else {
					printf("Failed to disable Microsoft Audio Decoder\n");
				}
			}
		} else {
			printf("Failed to retrieve CLSID for Microsoft Audio Decoder\n");
		}
		CLSID videoCLSID;
		HRESULT vdClsid = CLSIDFromString((LPOLESTR)L"{E1F1A0B8-BEEE-490D-BA7C-066C40B5E2B9}", (LPCLSID)&videoCLSID);
		if (SUCCEEDED(vdClsid)){
			printf("Retrieved CLSID for Microsoft Video Decoder\n");
			hr = pPluginControl->SetDisabled(videoCLSID, true);
			if (DSUtil::JDS_DEBUG) {
				if (SUCCEEDED(hr)){
					printf("Disabled Microsoft Video Decoder\n");
				} else {
					printf("Failed to disable Microsoft Video Decoder\n");
				}
			}
		} else {
			printf("Failed to retrieve CLSID for Microsoft Video Decoder\n");
		}

		pPluginControl->SetPreferredClsid(MEDIASUBTYPE_MPEG2_VIDEO, &splitterCLSID);// set the preferred filter for mpeg-2?
		pPluginControl->SetPreferredClsid(MEDIASUBTYPE_MPEG2_PROGRAM, &splitterCLSID);// set the preferred filter for mpeg-2?
		pPluginControl->SetPreferredClsid(MEDIASUBTYPE_MPEG2_TRANSPORT, &splitterCLSID);// set the preferred filter for mpeg-2?
	}


	/*
	hr = pGraph->AddSourceFilter((LPCWSTR)mediaPath, (LPCWSTR)L"Source", &pSourceFileFilter);

	if (FAILED(hr)) {
		printf("Failed to add source filter...\n");
		//pGraph->Release();
		//CoUninitialize();
		return hr;
	}
	filtList.push_back(pSourceFileFilter);
	if (DSUtil::JDS_DEBUG) {
		printf("Added source filter...\n");
	}
	*/
	//hr = CoCreateInstance(splitterCLSID, NULL, CLSCTX_INPROC_SERVER, IID_IBaseFilter, (void **)&pSplitterFileFilter);
	//if (SUCCEEDED(hr)) {
	//	filtList.push_back(pSplitterFileFilter);
	//	//hr = pGraph->AddFilter(pSplitterFileFilter, (LPCWSTR)L"Elecard MPEG Demultiplexer");
	//	hr = pGraphConfig->AddFilterToCache(pSplitterFileFilter);
	//}
	//if (FAILED(hr)) {
	//	printf("Failed to create and add the Elecard MPEG Demultiplexer to the cache\n");
	//	return hr;
	//}

	if (DSUtil::JDS_DEBUG) {
		printf("Added Elecard MPEG Demultiplexer to the cache\n");
	}

	// only create the audio chain if there is an audio output pin? 
	// what is the audio clsid of the Elecard audio decoder: 7EC22329-A916-4F81-9926-187C4350D317
	hr = CoCreateInstance(CLSID_CMpegAudioCodec, NULL, CLSCTX_INPROC_SERVER, IID_IBaseFilter, (void **)&pMpegAudioDecFileFilter);
	if (SUCCEEDED(hr)) {
		filtList.push_back(pMpegAudioDecFileFilter);
		//hr = pGraph->AddFilter(pMpegAudioDecFileFilter, (LPCWSTR)L"MPEG Audio Decoder");
		hr = pGraphConfig->AddFilterToCache(pMpegAudioDecFileFilter);
	}
	if (FAILED(hr)) {
		printf("Failed to create and add audio decoder to the cache\n");
		return hr;// hier don't return, continue
	}
	if (DSUtil::JDS_DEBUG) {
		printf("Added MPEG-2 Audio Decoder to the cache\n");
	}

	hr = CoCreateInstance(CLSID_DSoundRender, NULL, CLSCTX_INPROC_SERVER, IID_IBaseFilter, (void **)&pDirectSoundFileFilter);
	if (SUCCEEDED(hr)) {
		filtList.push_back(pDirectSoundFileFilter);
		//hr = pGraph->AddFilter(pDirectSoundFileFilter, (LPCWSTR)L"Direct Sound Renderer");
		hr = pGraphConfig->AddFilterToCache(pDirectSoundFileFilter);
	}
	if (FAILED(hr)) {
		printf("Failed to create and add direct sound renderer to the cache\n");
		return hr;
	}
	if (DSUtil::JDS_DEBUG) {
		printf("Added direct sound renderer to the cache\n");
	}
	// don't do the above if there is no audio??

	hr = CoCreateInstance(CLSID_CMpegVideoCodec, NULL, CLSCTX_INPROC_SERVER, IID_IBaseFilter, (void **)&pMpegVideoDecFileFilter);
	if (SUCCEEDED(hr)) {
		filtList.push_back(pMpegVideoDecFileFilter);
		//hr = pGraph->AddFilter(pMpegVideoDecFileFilter, (LPCWSTR)L"MPEG Video Decoder");
		hr = pGraphConfig->AddFilterToCache(pMpegVideoDecFileFilter);
	}
	if (FAILED(hr)) {
		printf("Failed to create and add mpeg video decoder to the cache\n");
		return hr;
	}
	if (DSUtil::JDS_DEBUG) {
		printf("Added MPEG Video Decoder to the cache\n");
	}

	// Using QueryInterface on the graph builder, 
    // Get the Media Control object.
    hr = pGraph->QueryInterface(IID_IMediaControl, (void **)&pControl);
    if (FAILED(hr))
    {
        printf("ERROR - Could not create the Media Control object.\n");
        return hr;
    }
	if (DSUtil::JDS_DEBUG) {
		printf("Media Control initialized OK: %d\n", &pControl);
	}

	hr = pGraph->QueryInterface(IID_IMediaSeeking, (void **)&pSeeking);
	if (FAILED(hr)) {
		printf("ERROR - Could not create the Media Seeking object.\n");
	}

	hr = pGraph->QueryInterface(IID_IMediaPosition, (void **)&pPosition);
	if (FAILED(hr)) {
		printf("ERROR - Could not create the Media Position object.\n");
	}

	 // And get the Media Event object, too.
    hr = pGraph->QueryInterface(IID_IMediaEvent, (void **)&pEvent);
	if (FAILED(hr))
    {
        printf("ERROR - Could not create the Media Event object.\n");
        return hr;
    }
	if (DSUtil::JDS_DEBUG) {
		printf("Media Event initialized OK: %d\n", &pEvent);
	}
	//is it possible to cocreate a videowindow as the docs suggest?
	hr = pGraph->QueryInterface(IID_IVideoWindow, (void **)&pVideoWindow);
	//hr = pVideoRendererFileFilter->QueryInterface(IID_IVideoWindow, (void **)&pVideoWindow);
	if (FAILED(hr)){// this even works with audio files, but check first
		printf("ERROR: could not get the video window\n");
		return hr;
	}
	if (DSUtil::JDS_DEBUG) {
		printf("Video Window initialized OK: %d\n", &pVideoWindow);
	}
	
	hr = pGraph->QueryInterface(IID_IBasicVideo, (void **)&pBasicVideo);
	if (FAILED(hr)){// this even works with audio files, but check first
		printf("ERROR: could not get the basic video\n");
		return hr;
	}
	if (DSUtil::JDS_DEBUG) {
		printf("Basic Video initialized OK: %d\n", &pBasicVideo);
	}
	
	hr = pGraph->QueryInterface(IID_IBasicAudio, (void **)&pBasicAudio);
	if (FAILED(hr)) {
		printf("ERROR: could not get the basic audio\n");
	}

	hr = pGraph->RenderFile((LPCWSTR)mediaPath, NULL);
	// this creates a video renderer
	/*
	hr = pGraph->Render(pOutputPin);
	*/
	if (FAILED(hr)) {
		wprintf(L"Could not render the file: %s\n", mediaPath);
		return hr;
	}
	
	hr = pVideoWindow->put_WindowStyle(WS_CHILD);// this fails in case of audio
	if (FAILED(hr)) {
		printf("ERROR: could not set the video window style\n");
		isVideo = false;
	}
	hr = pVideoWindow->put_Visible(-1);
	hr = pControl->Pause();

	if (DSUtil::JDS_DEBUG) {
		DSUtil dsu;
		dsu.printFilters(pGraph);
	}
	return S_OK;
}

// test code with VMR9
HRESULT DSPlayer::createVMR9MPEG1Graph() {
	HRESULT hr;
	// Initialize the COM library
	hr = CoInitializeEx(NULL, COINIT_MULTITHREADED);//APARTMENTTHREADED? COINIT_MULTITHREADED
	if (FAILED(hr)) {
		return hr;
	}
	printf("COM initialized OK\n");
	// Create the filter graph manager and query for interfaces.
	// using this way a message loop thread has to be created
    hr = CoCreateInstance(CLSID_CaptureGraphBuilder2, NULL, CLSCTX_INPROC_SERVER,
                          IID_ICaptureGraphBuilder2, (void **)&pCapGraph);
    if (FAILED(hr))
    {
        printf("ERROR - Could not create the Filter Graph Manager 2.\n");
		//CoUninitialize();  // uninitialize COM
        return hr;
    }
	printf("Filter Graph Manager 2 initialized OK: %d\n", &pCapGraph);
	// initialize the graph builder of capture graph
    hr = CoCreateInstance(CLSID_FilterGraph, NULL, CLSCTX_INPROC_SERVER,
                          IID_IGraphBuilder, (void **)&pGraph);
    if (FAILED(hr))
    {
        printf("ERROR - Could not create the Filter Graph Manager.\n");
		//pCapGraph->Release();
		//CoUninitialize();  // uninitialize COM
        return hr;
    }
	printf("Filter Graph Manager initialized OK: %d\n", &pGraph);
	hr = pCapGraph->SetFiltergraph(pGraph);

	IBaseFilter	  *pSourceFileFilter = NULL;
	IBaseFilter	  *pSplitterFileFilter = NULL;
	IBaseFilter   *pMpegAudioDecFileFilter = NULL;
	IBaseFilter   *pMpegVideoDecFileFilter = NULL;
	IBaseFilter   *pDirectSoundFileFilter = NULL;
	IBaseFilter   *pVideoRendererFileFilter = NULL;
	// more filters
	//HRESULT hr = NULL;
	wprintf(L"Native: Media path: %s\n", mediaPath);
	hr = pGraph->AddSourceFilter((LPCWSTR)mediaPath, (LPCWSTR)L"Source", &pSourceFileFilter);

	if (FAILED(hr)) {
		printf("Failed to add source filter...\n");
		return hr;
	}
	printf("Added source filter...\n");
	hr = CoCreateInstance(CLSID_MPEG1Splitter, NULL, CLSCTX_INPROC_SERVER, IID_IBaseFilter, (void **)&pSplitterFileFilter);
	if (SUCCEEDED(hr)) {
		hr = pGraph->AddFilter(pSplitterFileFilter, (LPCWSTR)L"MPEG1 Splitter");
	}
	if (FAILED(hr)) {
		printf("Failed to create and add mpeg1 spitter\n");
		return hr;
	}
	
	printf("Added MPEG-1 Splitter\n");
	hr = CoCreateInstance(CLSID_CMpegAudioCodec, NULL, CLSCTX_INPROC_SERVER, IID_IBaseFilter, (void **)&pMpegAudioDecFileFilter);
	if (SUCCEEDED(hr)) {
		hr = pGraph->AddFilter(pMpegAudioDecFileFilter, (LPCWSTR)L"MPEG1 Audio Decoder");
	}
	if (FAILED(hr)) {
		printf("Failed to create and add mpeg1 audio decoder\n");
		return hr;
	}
	printf("Added MPEG-1 Audio Decoder\n");
	
	hr = CoCreateInstance(CLSID_CMpegVideoCodec, NULL, CLSCTX_INPROC_SERVER, IID_IBaseFilter, (void **)&pMpegVideoDecFileFilter);
	if (SUCCEEDED(hr)) {
		hr = pGraph->AddFilter(pMpegVideoDecFileFilter, (LPCWSTR)L"MPEG1 Video Decoder");
	}
	if (FAILED(hr)) {
		printf("Failed to create and add mpeg1 video decoder\n");
		return hr;
	}
	printf("Added MPEG-1 Video Decoder\n");

	hr = CoCreateInstance(CLSID_DSoundRender, NULL, CLSCTX_INPROC_SERVER, IID_IBaseFilter, (void **)&pDirectSoundFileFilter);
	if (SUCCEEDED(hr)) {
		hr = pGraph->AddFilter(pDirectSoundFileFilter, (LPCWSTR)L"Direct Sound Renderer");
	}
	if (FAILED(hr)) {
		printf("Failed to create and add direct sound renderer\n");
		return hr;
	}
	printf("Added direct sound renderer\n");
	// if the video renderer is created with CoCreateInstance, window messages should be handled
	// on the application thread (or a worker thread should be created?)
	//vmr9 {51B4ABF3-748F-4E3B-A276-C828330E926A}  CLSID_VideoMixingRenderer9
	// CLSID_VideoRenderer
	// CLSID_VideoRendererDefault
	// CLSID_VideoMixingRenderer
	
	hr = CoCreateInstance(CLSID_VideoMixingRenderer9, NULL, CLSCTX_INPROC_SERVER, IID_IBaseFilter, (void **)&pVideoRendererFileFilter);
	if (SUCCEEDED(hr)) {
		hr = pGraph->AddFilter(pVideoRendererFileFilter, (LPCWSTR)L"Video Renderer");
	}
	if (FAILED(hr)) {
		printf("Failed to create and add video renderer\n");
		return hr;
	} else {
		printf("Added video renderer\n");
	}
	// hier create a separate source file for Vmr9 stuff!
	//IBaseFilter *pConfig;
	//hr = pVideoRendererFileFilter->QueryInterface(IID_IVMRFilterConfig9, (void **)&pConfig);
	if(SUCCEEDED(hr)) {
		//DWORD pMode;
		//pConfig->GetRenderingMode(&pMode);
		//printf("Rendering Mode: %d\n", pMode);
		//pConfig->SetRenderingMode(VMRMode_Windowless);
		//pConfig->SetNumberOfStreams(1);
		//hr = configureVMRrenderer(pConfig, NULL);
	}

	// connect pins, or don't

	IPin *pOutputPin = NULL;
	IPin *pInputPin = NULL;

	pOutputPin = GetPin(pSourceFileFilter, PINDIR_OUTPUT);
	
	if (pOutputPin != NULL) {
		pInputPin = GetPin(pSplitterFileFilter, PINDIR_INPUT);
		if (pInputPin != NULL){
			hr = pGraph->ConnectDirect(pOutputPin, pInputPin, NULL);// don't use intelligent connect
			if (FAILED(hr)) {
				printf("Could not connect source filter and splitter\n");
				return hr;
			}
		} else {
			printf("Could not get the splitter input pin\n");
			return VFW_E_NOT_FOUND;
		}
	} else {
		printf("Could not get the source output pin\n");
		return VFW_E_NOT_FOUND;
	}
	
	// can we re-use the pins pointers?
	hr = pSplitterFileFilter->FindPin((LPCWSTR)L"Audio", &pOutputPin);
	if(SUCCEEDED(hr)) {
		pInputPin = GetPin(pMpegAudioDecFileFilter, PINDIR_INPUT);
		if (pInputPin != NULL) {
			hr = pGraph->ConnectDirect(pOutputPin, pInputPin, NULL);
			if (FAILED(hr)) {
				printf("Could not connect splitter and audio decoder\n");
				return hr;
			}
		} else {
			printf("Could not get audio decoder input pin\n");
			return VFW_E_NOT_FOUND;
		}
	}
	printf("Connected splitter and audio decoder\n");

	hr = pSplitterFileFilter->FindPin((LPCWSTR)L"Video", &pOutputPin);
	if(SUCCEEDED(hr)) {
		pInputPin = GetPin(pMpegVideoDecFileFilter, PINDIR_INPUT);
		if (pInputPin != NULL) {
			hr = pGraph->ConnectDirect(pOutputPin, pInputPin, NULL);
			if (FAILED(hr)) {
				printf("Could not connect splitter and video decoder\n");
				return hr;
			}
		} else {
			printf("Could not get video decoder input pin\n");
			return VFW_E_NOT_FOUND;
		}
	}
	printf("Connected splitter and video decoder\n");

	pOutputPin = GetPin(pMpegAudioDecFileFilter, PINDIR_OUTPUT);	
	if(pOutputPin != NULL) {
		pInputPin = GetPin(pDirectSoundFileFilter, PINDIR_INPUT);
		if (pInputPin != NULL) {
			hr = pGraph->ConnectDirect(pOutputPin, pInputPin, NULL);
			if (FAILED(hr)) {
				printf("Could not connect audio decoder and audio renderer\n");
				return hr;
			}
		} else {
			printf("Could not get audio renderer input pin\n");
			return VFW_E_NOT_FOUND;
		}
	} else {
		printf("Could not get audio decoder output pin\n");
		return VFW_E_NOT_FOUND;
	}
	printf("Connected audio decoder and audio renderer\n");

	pOutputPin = GetPin(pMpegVideoDecFileFilter, PINDIR_OUTPUT);
	/* can only be used when the VideoRenderer has been created
	if(pOutputPin != NULL) {
		pInputPin = GetPin(pVideoRendererFileFilter, PINDIR_INPUT);
		if (pInputPin != NULL) {
			//pGraph->Render(pOutputPin);
			hr = pCapGraph->ConnectDirect(pOutputPin, pInputPin, NULL);
			//hr = pGraph->Connect(pOutputPin, pInputPin);
			if (FAILED(hr)) {
				printf("Could not connect video decoder and video renderer\n");
				return hr;
			}
		} else {
			printf("Could not get video renderer input pin\n");
			return VFW_E_NOT_FOUND;
		}
	} else {
		printf("Could not get video decoder output pin\n");
		return VFW_E_NOT_FOUND;
	}
	printf("Connected video decoder and video renderer\n");
	*/
	//hr = pCapGraph->Render(pOutputPin);

	//if (FAILED(hr)) {
	//	printf("Failed to render the source output pin");
	//	return false;
	//}
		// Using QueryInterface on the graph builder, 
    // Get the Media Control object.
    hr = pGraph->QueryInterface(IID_IMediaControl, (void **)&pControl);
    if (FAILED(hr))
    {
        printf("ERROR - Could not create the Media Control object.\n");
        //pCapGraph->Release();	// Clean up
		//pCapGraph = NULL;
		//pGraph->Release();
		//pGraph = NULL;
		//CoUninitialize();  // uninitalize COM
        return hr;
    }
	printf("Media Control initialized OK: %d\n", &pControl);

	hr = pGraph->QueryInterface(IID_IMediaSeeking, (void **)&pSeeking);
	if (FAILED(hr)) {
		printf("ERROR - Could not create the Media Seeking object.\n");
	}

	hr = pGraph->QueryInterface(IID_IMediaPosition, (void **)&pPosition);
	if (FAILED(hr)) {
		printf("ERROR - Could not create the Media Position object.\n");
	}

	 // And get the Media Event object, too.
    hr = pGraph->QueryInterface(IID_IMediaEvent, (void **)&pEvent);
	if (FAILED(hr))
    {
        printf("ERROR - Could not create the Media Event object.\n");
        //pCapGraph->Release();	// Clean up
		//pGraph->Release();
        //pControl->Release();
		//pCapGraph = NULL;
		//pGraph = NULL;
		//pControl = NULL;
	    //CoUninitialize();  // uninitalize COM
        return hr;
    }
	printf("Media Event initialized OK: %d\n", &pEvent);
	//is it possible to cocreate a videowindow as the docs suggest?
	hr = pGraph->QueryInterface(IID_IVideoWindow, (void **)&pVideoWindow);
	//hr = pVideoRendererFileFilter->QueryInterface(IID_IVideoWindow, (void **)&pVideoWindow);
	if (FAILED(hr)){// this even works with audio files, but check first
		printf("ERROR: could not get the video window\n");
		//pCapGraph->Release();	// Clean up
		//pGraph->Release();
        //pControl->Release();
		//pEvent->Release();
		//pCapGraph = NULL;
		//pGraph = NULL;
		//pControl = NULL;
		//pEvent = NULL;
	    //CoUninitialize();  // uninitalize COM
		return hr;
	}
	printf("Video Window initialized OK: %d\n", &pVideoWindow);
	
	hr = pGraph->QueryInterface(IID_IBasicVideo, (void **)&pBasicVideo);
	if (FAILED(hr)){// this even works with audio files, but check first
		printf("ERROR: could not get the basic video\n");
		/*pCapGraph->Release();	// Clean up
		pGraph->Release();
        pControl->Release();
		pEvent->Release();
		pVideoWindow->Release();
		pCapGraph = NULL;
		pGraph = NULL;
		pControl = NULL;
		pEvent = NULL;
		pVideoWindow = NULL;
	    CoUninitialize();*/  // uninitalize COM
		return hr;
	}
	printf("Basic Video initialized OK: %d\n", &pBasicVideo);

	hr = pGraph->QueryInterface(IID_IBasicAudio, (void **)&pBasicAudio);
	if (FAILED(hr)) {
		printf("ERROR: could not get the basic audio\n");
	}

	hr = pGraph->Render(pOutputPin);
	if (FAILED(hr)) {
		wprintf(L"Could not render the file: %s\n", mediaPath);
		return hr;
	}
	hr = pVideoWindow->put_WindowStyle(WS_CHILD);// this fails in case of audio
	if (FAILED(hr)) {
		printf("ERROR: could not set the video window style\n");
	}
	hr = pVideoWindow->put_Visible(-1);
	hr = pControl->Pause();
	//hr = pControl->Run();
	DSUtil dsu;
	HRESULT hh = dsu.printFilters(pGraph);
	return S_OK;
}

// Code from the DX9 SDK, extend with major media type.
// This code allows us to find a pin (input or output) on a filter, and return it.
IPin *DSPlayer::GetPin(IBaseFilter *pFilter, PIN_DIRECTION PinDir)
{
    BOOL       bFound = FALSE;
    IEnumPins  *pEnum;
    IPin       *pPin;

	// Begin by enumerating all the pins on a filter
    HRESULT hr = pFilter->EnumPins(&pEnum);
    if (FAILED(hr))
    {
        return NULL;
    }

	// Now, look for a pin that matches the direction characteristic.
	// When we've found it, we'll return with it.
    while(pEnum->Next(1, &pPin, 0) == S_OK)
    {
        PIN_DIRECTION PinDirThis;
        pPin->QueryDirection(&PinDirThis);
        if (bFound = (PinDir == PinDirThis))
            break;
        pPin->Release();
    }
    pEnum->Release();
    return (bFound ? pPin : NULL);
}

void DSPlayer::RenderAllOutputPins(IGraphBuilder *pGraph, IBaseFilter *pFilter) {
	IEnumPins  *pEnum;
    IPin       *pPin;
	// Begin by enumerating all the pins on the filter
    HRESULT hr = pFilter->EnumPins(&pEnum);
    if (FAILED(hr)) {
        return;
    }
	// render all output pins, continue if anything goes wrong
    while(pEnum->Next(1, &pPin, 0) == S_OK) {
        PIN_DIRECTION PinDirThis;
        pPin->QueryDirection(&PinDirThis);
		if (PINDIR_OUTPUT == PinDirThis) {
			pGraph->Render(pPin);
		}
        pPin->Release();
    }
    pEnum->Release();
}
