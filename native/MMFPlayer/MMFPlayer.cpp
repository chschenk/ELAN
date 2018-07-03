/* 
 * Project:	JMMFPlayer, Microsoft Media Foundation Player for Java
 * Author:	Han Sloetjes
 * Version:	1.0, March 2012
 */
#include "MMFPlayer.h"
#include "MMFUtil.h"
#include <mferror.h>
#include <comdef.h>

MMFPlayer::MMFPlayer() {
	initFields();
}

MMFPlayer::MMFPlayer(bool synchronous) {
	if (MMFUtil::JMMF_DEBUG) {
		printf("MMFPlayer Info: Constructor: Creating a player in synchronous mode: %d.\n", synchronous);
	}
	initFields();
	synchronousMode = synchronous;
}

MMFPlayer::MMFPlayer(const wchar_t *path) {
	if (MMFUtil::JMMF_DEBUG) {
		printf("MMFPlayer Info: Constructor: Creating a player with path.\n");
	}
	initFields();
}

MMFPlayer::MMFPlayer(const wchar_t *path, bool synchronous) {
	if (MMFUtil::JMMF_DEBUG) {
		printf("MMFPlayer Info: Constructor: Creating a player with path and synchronous flag.\n");
	}	
	initFields();
	synchronousMode = synchronous;
}

/*
* It is (now) assumed the player and the session are closed before deleting 
* the player.
*/
MMFPlayer::~MMFPlayer() {
	if (MMFUtil::JMMF_DEBUG) {
		printf("MMFPlayer Info: Destructor: Cleaning up player.\n");
	}

	if (!cleanUpCalled) {
		//cleanUpOnClose();

		// test 
		cleanUpCalled = true;
		HRESULT	hr = FinalClosing();
		if (FAILED(hr)) {
			printf("MMFPlayer Error: Destructor: Failed to finalize close operation: %d (%x).\n", hr, hr);
		} else {
			if (MMFUtil::JMMF_DEBUG) {
				printf("MMFPlayer Info: Destructor: Successfully finalized the close operation.\n");
			}
		}
	}
}

void MMFPlayer::initFields() {
	InitializeCriticalSection(&m_criticalSection);
	m_hwndVideo = NULL;
	m_pSource = NULL;
	m_pVideoDisplay = NULL;
	m_pSession = NULL;
	m_pTopology = NULL;
	m_pRate = NULL;
	m_pRateSupport = NULL;
	m_pClock = NULL;
	//m_pVolume = NULL;
	m_pStreamVolume = NULL;
	m_nRefCount = 1;
	m_state = PlayerState_NoSession;
	userPlaybackRate = 1;
	thinEnabled = FALSE;
	cleanUpCalled = false;
	topoInited = false;
	duration = 0;
	stopTime = 0;
	tempW = 0;
	tempH = 0;
	initVolume = 1;
	initMediaTime = 0;
	m_pStopTimer = NULL;
	m_pTimerCancelKey = NULL;
	pStopState = new StopTimeState();// initialize before first call to Invoke
	m_pEndTimer = NULL;
	pEndState = new StopTimeState();
	pendingAction = new PendingAction();
	clearPendingAction(pendingAction);
	cachedAction = new PendingAction();
	clearPendingAction(cachedAction);
	synchronousMode = false;
}

void MMFPlayer::clearPendingAction(PendingAction *pAction) {
	if (pAction != NULL) {
		pAction->action = DoNothing;
		pAction->timeValue = -1;
		pAction->rateValue = 1.0;
	}
}

/*
* Delegates to either the synchronous or the asynchronous version of start()
*/
HRESULT MMFPlayer::start() {
	if (synchronousMode) {
		return startSynchronous();
	} else {
		return startA();
	}
}

/*
* The default, asynchronous, start method.
*/
HRESULT MMFPlayer::startA() {
	if (m_state == PlayerState_Started || pendingAction->action == SetStateStarted) {
		if (MMFUtil::JMMF_DEBUG) {
			printf("MMFPlayer Warning: start: The player is already started.\n");
		}
		return MF_E_INVALIDREQUEST;
	}
	EnterCriticalSection(&m_criticalSection);
	HRESULT hr = S_OK;
	if (m_pSession != NULL) {
		// reset the rate
		if (m_pRate != NULL) {
			float curRate;
			//BOOL thin;
			hr = m_pRate->GetRate(FALSE, &curRate);
			if (curRate != userPlaybackRate) {
				m_pSession->Pause();// pause without pending action
				hr = m_pRate->SetRate(FALSE, userPlaybackRate);// set rate without pending
				if (FAILED(hr)) {
					printf("MMFPlayer Error: start: Cannot set rate to playback rate.\n");
				} else {
					if (MMFUtil::JMMF_DEBUG) {
						printf("MMFPlayer Info: start: Successfully set the rate to playback rate %f.\n", userPlaybackRate);
					}
				}
			} else {
				if (MMFUtil::JMMF_DEBUG) {
					printf("MMFPlayer Info: start: The rate is already to user playback rate %f.\n", userPlaybackRate);
				}
			}
		}
				
		// here check state of the player
		PROPVARIANT varStart;
		PropVariantInit(&varStart);

		varStart.vt = VT_EMPTY;

		HRESULT hr = m_pSession->Start(&GUID_NULL, &varStart);// start from current position

		if (SUCCEEDED(hr)) {
			// Start is an asynchronous operation. However, we can treat our state 
			// as being already started. If Start fails later, we'll get an 
			// MESessionStarted event with an error code, and will update our state.
			//m_state = PlayerState_Started;
			pendingAction->action = SetStateStarted;

			if (stopTime > 0 && m_pStopTimer != NULL) {
				hr = m_pStopTimer->SetTimer(0, stopTime, this, pStopState, &m_pTimerCancelKey);
				if (FAILED(hr)) {
					printf("MMFPlayer Error: start: Failed to set the stop time for the stop time timer.\n");
				} else {
					if (MMFUtil::JMMF_DEBUG) {
						printf("MMFPlayer Info: start: Successfully set the stop time for the stop time timer %lld.\n", stopTime);
					}
				}
			}
			if (duration > 2000000 && m_pEndTimer != NULL) {// 200 ms
				hr = m_pEndTimer->SetTimer(0, duration - 2000000, this, pEndState, &m_pTimerCancelKey);
				if (FAILED(hr)) {
					printf("MMFPlayer Error: start: Failed to set the end of media timer.\n");
				} else {
					if (MMFUtil::JMMF_DEBUG) {
						printf("MMFPlayer Info: start: Successfully set the time for the end of media timer.\n");
					}
				}
			}
		} else {
			printf("MMFPlayer Error: start: Starting the player failed.\n");
		}

		PropVariantClear(&varStart);
		
	} else {
		printf("MMFPlayer Error: start: Cannot start player, session is null.\n");
		hr = E_UNEXPECTED;
	}

	LeaveCriticalSection(&m_criticalSection);
	return hr;
}

/*
* The start method in synchronous mode. GetEvent is called immediately after calls
* to session method that result in an event.
*/
HRESULT MMFPlayer::startSynchronous() {
	if (!synchronousMode) {
		printf("MMFPlayer Warning: startSynchronous: Function called while not in synchronous mode.\n");
	}
	//printf("MMFPlayer Info: startSynchronous: Player state is %d.\n", m_state);
	if (m_state == PlayerState_Started) {
		if (MMFUtil::JMMF_DEBUG) {
			printf("MMFPlayer Warning: startSynchronous: The player is already started.\n");
		}
		return MF_E_INVALIDREQUEST;
	}
	EnterCriticalSection(&m_criticalSection);
	HRESULT hr = S_OK;
	if (m_pSession != NULL) {
		// reset the rate
		if (m_pRate != NULL) {
			float curRate;
			//BOOL thin;
			hr = m_pRate->GetRate(FALSE, &curRate);
			if (curRate != userPlaybackRate) {
				hr = m_pSession->Pause();
				
				if (FAILED(hr)) {
					printf("MMFPlayer Error: startSynchronous: Cannot pause the player in order to change the rate.\n");
				} else {
					if (MMFUtil::JMMF_DEBUG) {
						printf("MMFPlayer Info: startSynchronous: Successfully paused the player to change the rate.\n");
					}

					HRESULT ehr = PullMediaEventsUntilEventTypeSynchronous(MESessionPaused);

					if (SUCCEEDED(ehr)) {
						// what to do if an error is returned
						if (MMFUtil::JMMF_DEBUG) {
							printf("MMFPlayer Info: startSynchronous: Player successfully paused for changing the rate.\n");
						}
					} else {
						printf("MMFPlayer Error: startSynchronous: Failed to pause the player for changing the rate.\n");
					}
				}
				
				hr = m_pRate->SetRate(FALSE, userPlaybackRate);
				if (FAILED(hr)) {
					printf("MMFPlayer Error: startSynchronous: Cannot set rate to playback rate.\n");
				} else {
					if (MMFUtil::JMMF_DEBUG) {
						printf("MMFPlayer Info: startSynchronous: Successfully set the rate to playback rate %f.\n", userPlaybackRate);
					}

					HRESULT ehr = PullMediaEventsUntilEventTypeSynchronous(MESessionRateChanged);

					if (SUCCEEDED(ehr)) {
						// what to do if an error is returned
						if (MMFUtil::JMMF_DEBUG) {
							printf("MMFPlayer Info: startSynchronous: Rate successfully changed.\n");
						}
					} else {
						printf("MMFPlayer Error: startSynchronous: Failed to change the rate %d (%x).\n", ehr, ehr);
					}

				}
			} else {
				if (MMFUtil::JMMF_DEBUG) {
					printf("MMFPlayer Info: startSynchronous: The rate is already to user playback rate %f.\n", userPlaybackRate);
				}
			}
		}
				
		// here check state of the player
		PROPVARIANT varStart;
		PropVariantInit(&varStart);

		varStart.vt = VT_EMPTY;

		HRESULT hr = m_pSession->Start(&GUID_NULL, &varStart);// start from current position
		
		if (SUCCEEDED(hr)) {
			// Start is an asynchronous operation. Get event from the session. Do it here or after setting stop times?
			HRESULT ehr = PullMediaEventsUntilEventTypeSynchronous(MESessionStarted);

			if (SUCCEEDED(ehr)) {
				// what to do if an error is returned
				if (MMFUtil::JMMF_DEBUG) {
					printf("MMFPlayer Info: startSynchronous: Started the session.\n");
				}
			} else {
				printf("MMFPlayer Error: startSynchronous: Failed to start the session.\n");
			}
			// this should now have been set in the processing of the event
			//m_state = PlayerState_Started;
			/* test comment 24-03 when timer are used an asynchronous event is generated and somehow that leads to MULTIPLE_SUBSCRIBERS error
			// when getting the event
			if (stopTime > 0 && m_pStopTimer != NULL) {
				hr = m_pStopTimer->SetTimer(0, stopTime, this, pStopState, &m_pTimerCancelKey);
				if (FAILED(hr)) {
					printf("MMFPlayer Error: startSynchronous: Failed to set the stop time for the stop time timer.\n");
				} else {
					if (MMFUtil::JMMF_DEBUG) {
						printf("MMFPlayer Info: startSynchronous: Successfully set the stop time for the stop time timer %lld.\n", stopTime);
					}
				}
			}
			if (duration > 2000000 && m_pEndTimer != NULL) {// 200 ms
				hr = m_pEndTimer->SetTimer(0, duration - 2000000, this, pEndState, &m_pTimerCancelKey);
				if (FAILED(hr)) {
					printf("MMFPlayer Error: startSynchronous: Failed to set the end of media timer.\n");
				} else {
					if (MMFUtil::JMMF_DEBUG) {
						printf("MMFPlayer Info: startSynchronous: Successfully set the time for the end of media timer.\n");
					}
				}
			}
			*/
		} else {
			printf("MMFPlayer Error: startSynchronous: Starting the player failed.\n");
		}

		PropVariantClear(&varStart);
		
	} else {
		printf("MMFPlayer Error: startSynchronous: Cannot start player, session is null.\n");
		hr = E_UNEXPECTED;
	}

	LeaveCriticalSection(&m_criticalSection);
	return hr;
}

/*
* Delegates to either the synchronous or the asynchronous version of stop().
*/
HRESULT MMFPlayer::stop() {
	if (synchronousMode) {
		return stopSynchronous();
	} else {
		return stopA();
	}
}

/*
* For temporary stopping the player, pause() needs to be called. 
* stop() resets the play back and should only be called before closing the session.
*/
HRESULT MMFPlayer::stopA() {
    // only pause if the player has started
	if (m_state != PlayerState_Started && m_state != PlayerState_Paused) {
        return MF_E_INVALIDREQUEST;
    }

	EnterCriticalSection(&m_criticalSection);
	// if session is null return error
	if (m_pSession == NULL) {
		LeaveCriticalSection(&m_criticalSection);
		return E_UNEXPECTED;
	}

	HRESULT hr = m_pSession->Stop();// pauses the session

	if (SUCCEEDED(hr)) {
		//m_state = PlayerState_Stopped;
		pendingAction->action = SetStateStopped;
	}
	// setRate(0)?
	LeaveCriticalSection(&m_criticalSection);

	return hr;
}

/*
* Stops the player before closing, synchronously.
*/
HRESULT MMFPlayer::stopSynchronous() {
    // only stop if the player has started
	if (m_state != PlayerState_Started && m_state != PlayerState_Paused) {
        return MF_E_INVALIDREQUEST;
    }

	EnterCriticalSection(&m_criticalSection);
	// if session is null return error
	if (m_pSession == NULL) {
		LeaveCriticalSection(&m_criticalSection);
		return E_UNEXPECTED;
	}

	HRESULT hr = m_pSession->Stop();// pauses the session

	if (SUCCEEDED(hr)) {
		HRESULT ehr = PullMediaEventsUntilEventTypeSynchronous(MESessionStopped);

		if (SUCCEEDED(ehr)) {
			// what to do if an error is returned
			if (MMFUtil::JMMF_DEBUG) {
				printf("MMFPlayer Info: stopSynchronous: Successfully stopped the player.\n");
			}
		} else {
			printf("MMFPlayer Info: stopSynchronous: Failed to stop the player.\n");
		}

	}
	// setRate(0)?
	LeaveCriticalSection(&m_criticalSection);

	return hr;
}

/*
* Delegates to either the synchronous or the asynchronous variant of pause().
*/
HRESULT MMFPlayer::pause() {
	if (synchronousMode) {
		return pauseSynchronous();
	} else {
		return pauseA();
	}
}

/*
* The default, asynchronous, pause method.
* Pausing by setting the rate to 0 while the player remains "started"
* doesn't seem to work; the player keeps playing. Setting the rate to 0
* means "scrubbing", one image is produced, but in combination with "started" 
* this has no effect.?
*/
HRESULT MMFPlayer::pauseA() {
	EnterCriticalSection(&m_criticalSection);
    // only pause if the player has started
	if (m_state != PlayerState_Started && pendingAction->action != SetStateStarted) {
		if (MMFUtil::JMMF_DEBUG) {
			printf("MMFPlayer Error: pause: The media player is not started.\n");
		}
		LeaveCriticalSection(&m_criticalSection);
        return MF_E_INVALIDREQUEST;
    }
	// if session is null return error
	if (m_pSession == NULL) {
		LeaveCriticalSection(&m_criticalSection);
		return E_UNEXPECTED;
	}

	HRESULT hr = m_pSession->Pause();// pauses the session

	if (SUCCEEDED(hr)) {
		//m_state = PlayerState_Paused;
		pendingAction->action = SetStatePaused;// store current time?
		pendingAction->timeValue = getMediaPosition();
		if (MMFUtil::JMMF_DEBUG) {
			__int64 curTime = getMediaPosition();
			printf("MMFPlayer Info: pause: Paused the media player at %lld.\n", curTime);
		}
	} else {
		__int64 curTime = getMediaPosition();
		printf("MMFPlayer Error: pause: Error while pausing the media player at %lld, %d (%x).\n", curTime, hr, hr);
	}

	LeaveCriticalSection(&m_criticalSection);

	return hr;
}

/*
* The synchronous variant of pausing the player.
*/
HRESULT MMFPlayer::pauseSynchronous() {
	// temp
	//printf("MMFPlayer Info: pauseSynchronous: Player state is %d.\n", m_state);
	
    // only pause if the player has started
	if (m_state != PlayerState_Started) {
		if (MMFUtil::JMMF_DEBUG) {
			printf("MMFPlayer Error: pauseSynchronous: The media player is not started.\n");
		}
        return MF_E_INVALIDREQUEST;
    }
	// if session is null return error
	if (m_pSession == NULL) {
		return E_UNEXPECTED;
	}
	EnterCriticalSection(&m_criticalSection);//TODO need lock in synchronous mode?

	HRESULT hr = m_pSession->Pause();// pauses the session

	if (SUCCEEDED(hr)) {
		HRESULT ehr = PullMediaEventsUntilEventTypeSynchronous(MESessionPaused);
		if (SUCCEEDED(ehr)) {
			// ignore the return value?
			//ProcessMediaEventSynchronous(pEvent);
			if (MMFUtil::JMMF_DEBUG) {
				printf("MMFPlayer Info: pauseSynchronous:  Paused the player %d (%x).\n", ehr, ehr);
			}
		} else {
			printf("MMFPlayer Error: pauseSynchronous: Failed to pause the player %d (%x).\n", ehr, ehr);
		}

		if (MMFUtil::JMMF_DEBUG) {
			__int64 curTime = getMediaPosition();
			printf("MMFPlayer Info: pauseSynchronous: Paused the media player at %lld.\n", curTime);
		}
	} else {
		__int64 curTime = getMediaPosition();
		printf("MMFPlayer Error: pauseSynchronous: Error while pausing the media player at %lld, %d (%x).\n", curTime, hr, hr);
	}

	LeaveCriticalSection(&m_criticalSection);

	return hr;
}

/*
* Delegates to either the synchronous or asynchronous version of initSessionWithFile().
*/
HRESULT MMFPlayer::initSessionWithFile(const wchar_t *path) {
	if (synchronousMode) {
		printf("MMFPlayer Info: initSessionWithFile: init session in synchronous mode\n");
		return initSessionWithFileSynchronous(path);
	} else {
		printf("MMFPlayer Info: initSessionWithFile: init session in asynchronous mode\n");
		return initSessionWithFileA(path);
	}
}

/*
* Initialization in the default, asynchronous, mode.
* Initializes the session. In case of "audio only" it immediately creates a topology
* and sets it for the media session.
* In case of video initialization is done partially, first when the video window is available
* the topology is created and passed to the session.
*/
HRESULT MMFPlayer::initSessionWithFileA(const wchar_t *path) {
	mediaPath = MMFUtil::copyWchar(path);
	wprintf(L"MMFPlayer Info: initSessionWithFile: Media path: %s\n", mediaPath);

	// Initialize the COM library
	HRESULT hr = CoInitializeEx(NULL, COINIT_MULTITHREADED);//COINIT_MULTITHREADED  COINIT_APARTMENTTHREADED
	
	if (FAILED(hr)) {
		printf("MMFPlayer Error: initSessionWithFile: Failed to initialize COM: %d (%x).\n", hr, hr);
		return hr;
	}
	hr = MFStartup(MF_VERSION);
	if (FAILED(hr)) {
		printf("MMFPlayer Error: initSessionWithFile: Failed to startup MediaFoundation: %d (%x).\n", hr, hr);
		return hr;
	}

	// create an event that will be fired when the asynchronous IMFMediaSession::Close() 
    // operation is complete
    m_closeCompleteEvent = CreateEvent(NULL, FALSE, FALSE, NULL);

	if (m_closeCompleteEvent == NULL) {
		printf("MMFPlayer Error: initSessionWithFile: Failed to create a 'close complete' event.\n");
		//return E_UNEXPECTED; // should fail??
	}

	EnterCriticalSection(&m_criticalSection);

	hr = MFCreateMediaSession(NULL, &m_pSession);

	if (FAILED(hr)) {
		printf("MMFPlayer Error: initSessionWithFile: Failed to create a media session: %d (%x).\n", hr, hr);
		return hr;
	} else {
		printf("MMFPlayer Info: initSessionWithFile: Created a media session: %d (%x).\n", hr, hr);
	}
	
	// Start pulling events from the media session
    hr = m_pSession->BeginGetEvent((IMFAsyncCallback*)this, NULL);
	
	if (FAILED(hr)){
		printf("MMFPlayer Error: initSessionWithFile: Failed to BeginGetEvent.\n");
	} else {
		if (MMFUtil::JMMF_DEBUG) {
			printf("MMFPlayer Info: initSessionWithFile: Successfully called BeginGetEvent.\n");
		}
	}
	// create media source
	hr = CreateMediaSource(mediaPath);

	if (FAILED(hr)) {
		printf("MMFPlayer Error: initSessionWithFile: Failed to create a media source: %d (%x).\n", hr, hr);
		return hr;
	} else {
		if (MMFUtil::JMMF_DEBUG) {
			printf("MMFPlayer Info: initSessionWithFile: Created a media source: %d (%x).\n", hr, hr);
		}
	}
	// check major media type; in case of audio only create the topology and set it for the session
	// in case of video partially create a topology and wait for the window handle
	// create topology
	if (isVideo && m_hwndVideo == NULL) {
		LeaveCriticalSection(&m_criticalSection);

		return hr;
	}

	hr = CreateTopologyFromSource();
	if (FAILED(hr)) {
		printf("MMFPlayer Error: initSessionWithFile: Failed to create a topology: %d (%x).\n", hr, hr);
		return hr;
	} else {
		if (MMFUtil::JMMF_DEBUG) {
			printf("MMFPlayer Info: initSessionWithFile: Created a topology %d (%x).\n", hr, hr);
		}
	}

	//hr = m_pSession->SetTopology(MFSESSION_SETTOPOLOGY_NORESOLUTION, m_pTopology);
	hr = m_pSession->SetTopology(0, m_pTopology);

	if (FAILED(hr)) {
		printf("MMFPlayer Error: initSessionWithFile: Failed to set the topology of the session: %d (%x).\n", hr, hr);
	} else {
		printf("MMFPlayer Info: initSessionWithFile: Set the topology of the session: %d (%x).\n", hr, hr);
	}

	LeaveCriticalSection(&m_criticalSection);

	return hr;

}

/*
* Initialize a session synchronously.
*/
HRESULT MMFPlayer::initSessionWithFileSynchronous(const wchar_t *path) {
	mediaPath = MMFUtil::copyWchar(path);
	wprintf(L"MMFPlayer Info: initSessionWithFileSynchronous: Media path: %s\n", mediaPath);

	// Initialize the COM library
	HRESULT hr = CoInitializeEx(NULL, COINIT_MULTITHREADED);//COINIT_MULTITHREADED  COINIT_APARTMENTTHREADED
	
	if (FAILED(hr)) {
		printf("MMFPlayer Error: initSessionWithFileSynchronous: Failed to initialize COM: %d (%x).\n", hr, hr);
		return hr;
	}
	hr = MFStartup(MF_VERSION);
	if (FAILED(hr)) {
		printf("MMFPlayer Error: initSessionWithFileSynchronous: Failed to startup MediaFoundation: %d (%x).\n", hr, hr);
		return hr;
	}

	EnterCriticalSection(&m_criticalSection);// in synchronous mode locking is not necessary?

	hr = MFCreateMediaSession(NULL, &m_pSession);// is synchronous

	if (FAILED(hr)) {
		printf("MMFPlayer Error: initSessionWithFileSynchronous: Failed to create a media session: %d (%x).\n", hr, hr);
		return hr;
	} else {
		printf("MMFPlayer Info: initSessionWithFileSynchronous: Created a media session: %d (%x).\n", hr, hr);
	}
	
	// create media source
	hr = CreateMediaSource(mediaPath);// is currently synchronous

	if (FAILED(hr)) {
		printf("MMFPlayer Error: initSessionWithFileSynchronous: Failed to create a media source: %d (%x).\n", hr, hr);
		return hr;
	} else {
		if (MMFUtil::JMMF_DEBUG) {
			printf("MMFPlayer Info: initSessionWithFileSynchronous: Created a media source: %d (%x).\n", hr, hr);
		}
	}
	// check major media type; in case of audio only create the topology and set it for the session
	// in case of video partially create a topology and wait for the window handle
	// create topology
	if (isVideo && m_hwndVideo == NULL) {
		LeaveCriticalSection(&m_criticalSection);

		return hr;
	}

	hr = CreateAndSetTopologySynchronous();

	LeaveCriticalSection(&m_criticalSection);

	return hr;

}

/*
* Fully initializes a media session for the specified file and using the 
* specified window handle for the video.
*/
HRESULT MMFPlayer::initSession(const wchar_t *path, HWND hwnd) {// TODO remove HWND if init without handle works
	m_hwndVideo = hwnd;
	return this->initSessionWithFile(path);
}

/*
* Changing the media file is not suppported, for each file played a new MMFPlayer is created. 
*/
HRESULT MMFPlayer::setMediaFile(const wchar_t *path) {
	return E_NOTIMPL;
}

/*
* Returns whether the session contains video. Should only be called after initialization of the session. 
*/
bool MMFPlayer::isVisualMedia() {
	return isVideo;
}

/*
* Sets the new window in case of an existing VideoDisplayControl object, 
* initializes the topology in case it is the first time a window is set.
* In the latter case it is assumed the media source (file) has been set.
*/
HRESULT MMFPlayer::setOwnerWindow(HWND hwnd) {
	if (MMFUtil::JMMF_DEBUG) {
		printf("MMFPlayer Info: setOwnerWindow: Window handle: %p.\n", hwnd);
	}
	if (hwnd == NULL) {
		// don't try to set the window to NULL?
		if (MMFUtil::JMMF_DEBUG) {
			printf("MMFPlayer Warning: setOwnerWindow: Setting window handle to NULL not supported.\n");
		}
		return E_POINTER;
	}

	if (m_hwndVideo == NULL) {
		// first initialization
		if (m_pSource == NULL) {
			printf("MMFPlayer Error: setOwnerWindow: Setting the window handle while there is no source not supported.\n");
			return E_UNEXPECTED;
		}
		m_hwndVideo = hwnd;
		HRESULT hr = S_OK;

		if (synchronousMode) {
			hr = CreateAndSetTopologySynchronous();
			return hr;
		}

		EnterCriticalSection(&m_criticalSection);
		hr = CreateTopologyFromSource();
		if (FAILED(hr)) {
			printf("MMFPlayer Error: setOwnerWindow: Failed to create a topology with window handle: %d (%x).\n", hr, hr);
			return hr;
		} else {
			if (MMFUtil::JMMF_DEBUG) {
				printf("MMFPlayer Info: setOwnerWindow: Successfully created a topology.\n");
			}
		}

		//hr = m_pSession->SetTopology(MFSESSION_SETTOPOLOGY_NORESOLUTION, m_pTopology);
		hr = m_pSession->SetTopology(0, m_pTopology);

		if (FAILED(hr)) {
			printf("MMFPlayer Error: setOwnerWindow: Failed to set the topology of the session: %d (%x).\n", hr, hr);
		} else {
			if (MMFUtil::JMMF_DEBUG) {
				printf("MMFPlayer Info: setOwnerWindow: Successfully set the topology of the session.\n");
			}
		}


		LeaveCriticalSection(&m_criticalSection);

		return hr;

	} else {
		// check player state, use critical section?
		// check for NULL?
		m_hwndVideo = hwnd;
		HRESULT hr = S_OK;
		if (m_pVideoDisplay != NULL) {
			hr = m_pVideoDisplay->SetVideoWindow(hwnd);
			if (FAILED(hr)) {
				printf("MMFPlayer Error: setOwnerWindow: Failed to set window handle for the Video Display: %d (%x).\n", hr, hr);
			} else {
				if (MMFUtil::JMMF_DEBUG) {
					printf("MMFPlayer Info: setOwnerWindow: Successfully set window handle for Video Display.\n");
				}
			}
		} else {
			printf("MMFPlayer Error: setOwnerWindow: Video Display is NULL.\n");
			hr = E_POINTER;
		}
		return hr;
	}
}

// copied from MMF's MF_BasicPlayback example
//  Creates a media source from a URL.

HRESULT MMFPlayer::CreateMediaSource(PCWSTR sURL) {
    MF_OBJECT_TYPE ObjectType = MF_OBJECT_INVALID;

    IMFSourceResolver* pSourceResolver = NULL;
    IUnknown* pSource = NULL;

	if (m_pSource != NULL) {
		m_pSource->Release();
		m_pSource = NULL;
	}
    EnterCriticalSection(&m_criticalSection);

	if (MMFUtil::JMMF_DEBUG) {
		wprintf(L"MMFPlayer Info: CreateMediaSource: Creating source resolver for: %s\n", sURL);
	}
    // Create the source resolver.
    HRESULT hr = MFCreateSourceResolver(&pSourceResolver);
    if (FAILED(hr)) {
		printf("MMFPlayer Error: CreateMediaSource: Failed to create a source resolver.\n");
		if (pSourceResolver != NULL) {
			pSourceResolver->Release();
		}
        return hr;
    }
	if (MMFUtil::JMMF_DEBUG) {
		wprintf(L"MMFPlayer Info: CreateMediaSource: Creating a source object for URL: %s\n", sURL);
	}
    // Use the source resolver to create the media source.

    // Note: For simplicity this sample uses the synchronous method on
    // IMFSourceResolver to create the media source. However, creating a media 
    // source can take a noticeable amount of time, especially for a network 
    // source. For a more responsive UI, use the asynchronous 
    // BeginCreateObjectFromURL method.
	// Note: if this is changed initSessionWithFileSynchronous needs to changed not to call this method

    hr = pSourceResolver->CreateObjectFromURL(// synchronous
                sURL,                       // URL of the source.
                MF_RESOLUTION_MEDIASOURCE | 
					MF_RESOLUTION_CONTENT_DOES_NOT_HAVE_TO_MATCH_EXTENSION_OR_MIME_TYPE,// Create a source object.
                NULL,                       // Optional property store.
                &ObjectType,                // Receives the created object type.
                &pSource                    // Receives a pointer to the media source.
            );
    if (FAILED(hr)){
		printf("MMFPlayer Error: CreateMediaSource: Failed to create a source object from URL: %d (%x).\n", hr, hr);
		//_com_error e( hr );
		//wprintf(L"Error: %s.\n", e.Description());

        if (pSourceResolver != NULL) {
			pSourceResolver->Release();
			pSourceResolver = NULL;
		}
		if (pSource != NULL) {
			pSource->Release();
			pSource = NULL;
		}
		LeaveCriticalSection(&m_criticalSection);

        return hr;
    }

    // Get the IMFMediaSource interface from the media source.
    hr = pSource->QueryInterface(IID_PPV_ARGS(&m_pSource));
	if (FAILED(hr)) {
		printf("MMFPlayer Error: CreateMediaSource: Failed to get the MediaSource interface: %d (%x).\n", hr, hr);
	} else {
		if (MMFUtil::JMMF_DEBUG) {
			printf("MMFPlayer Info: CreateMediaSource: Successfully received a MediaSource interface.\n");
		}
	}

	if (SUCCEEDED(hr)) {
		BOOL hasVideo = FALSE;
		HRESULT hres = MMFUtil::HasVideoMediaType(m_pSource, &hasVideo);
		if (FAILED(hres)) {
			printf("MMFPlayer Error: CreateMediaSource: Unable to check whether the media type has video: %d (%x).\n", hres, hres);
		} else {
			printf("MMFPlayer Info: CreateMediaSource: The media type has video: %d\n", hasVideo);
			if (hasVideo) {
				isVideo = true;
			} else {
				isVideo = false;
			}
			//isVideo = (bool) hasVideo;
		}
		//CheckMajorMediaType(m_pSource);
	}

	if (pSourceResolver != NULL) {
		pSourceResolver->Release();
		pSourceResolver = NULL;
	}
	if (pSource != NULL) {
		pSource->Release();
		pSource = NULL;
	}
	
	LeaveCriticalSection(&m_criticalSection);

    return hr;
}

//  copied and adapted from MMF's MF_BasicPlayback example
//  Creates a playback topology from the media source.
//
//  Pre-condition: The media source must be created already.
//                 Call CreateMediaSource() before calling this method.

HRESULT MMFPlayer::CreateTopologyFromSource(){
	if (m_pSession == NULL) {
		return E_FAIL;
	}
	
	if (m_pSource == NULL) {
		return E_FAIL;
	}

    IMFPresentationDescriptor* pSourcePD = NULL;
    DWORD cSourceStreams = 0;

	EnterCriticalSection(&m_criticalSection);

    // Create a new topology.
    HRESULT hr = MFCreateTopology(&m_pTopology);
    if (FAILED(hr)) {
		if (m_pTopology != NULL) {
			m_pTopology->Release();
		}
		printf("MMFPlayer Error: CreateTopologyFromSource: Failed to create a topology: %d (%x).\n", hr, hr);
		LeaveCriticalSection(&m_criticalSection);
		return hr;
	} else {
		if (MMFUtil::JMMF_DEBUG) {
			printf("MMFPlayer Info: CreateTopologyFromSource: Successfully created a topology.\n");
		}
	}

    // Create the presentation descriptor for the media source.
    hr = m_pSource->CreatePresentationDescriptor(&pSourcePD);// synchronous
    if (FAILED(hr)) {
		if (pSourcePD != NULL) {
			pSourcePD->Release();
			pSourcePD = NULL;
		}
		printf("MMFPlayer Error: CreateTopologyFromSource: Failed to create a presentation descriptor: %d (%x).\n", hr, hr);
		LeaveCriticalSection(&m_criticalSection);
		return hr;
	} else {
		if (MMFUtil::JMMF_DEBUG) {
			printf("MMFPlayer Info: CreateTopologyFromSource: Successfully created a presentation descriptor.\n");
		}
		// check if the duration can already been retrieved
		
		HRESULT hhrr = pSourcePD->GetUINT64(MF_PD_DURATION, (UINT64*)&duration);
		if (FAILED(hhrr)) {
			printf("MMFPlayer Error: CreateTopologyFromSource: Cannot retrieve the media duration: %d (%x).\n", hr, hr);
		} else {
			printf("MMFPlayer Info: CreateTopologyFromSource: Media duration: %lld\n", duration);
		}
	}

    // Get the number of streams in the media source.
    hr = pSourcePD->GetStreamDescriptorCount(&cSourceStreams);
    if (FAILED(hr)) {
		if (pSourcePD != NULL) {
			pSourcePD->Release();
			pSourcePD = NULL;
		}
		LeaveCriticalSection(&m_criticalSection);
		return hr;
    }

    // For each stream, create the topology nodes and add them to the topology.
    for (DWORD i = 0; i < cSourceStreams; i++) {
        hr = AddBranchToPartialTopology(pSourcePD, i);
        if (FAILED(hr)) {
			if (pSourcePD != NULL) {
				pSourcePD->Release();
				pSourcePD = NULL;
			}
			printf("MMFPlayer Error: CreateTopologyFromSource: Failed to add branch to topology: %d (%x).\n", hr, hr);
			LeaveCriticalSection(&m_criticalSection);
			return hr;
		} else {
			if (MMFUtil::JMMF_DEBUG) {
				printf("MMFPlayer Info: CreateTopologyFromSource: Successfully added branch to topology.\n");
			}
		}
    }

	if (pSourcePD != NULL) {
		pSourcePD->Release();
		pSourcePD = NULL;
	}
	LeaveCriticalSection(&m_criticalSection);
    return hr;
}

HRESULT MMFPlayer::CreateAndSetTopologySynchronous() {
	HRESULT hr;
	hr = CreateTopologyFromSource();// is synchronous
	if (FAILED(hr)) {
		printf("MMFPlayer Error: CreateAndSetTopologySynchronous: Failed to create a topology: %d (%x).\n", hr, hr);
		return hr;
	} else {
		if (MMFUtil::JMMF_DEBUG) {
			printf("MMFPlayer Info: CreateAndSetTopologySynchronous: Created a topology: %d (%x).\n", hr, hr);
		}
	}

	//hr = m_pSession->SetTopology(MFSESSION_SETTOPOLOGY_NORESOLUTION, m_pTopology);
	hr = m_pSession->SetTopology(0, m_pTopology);

	if (FAILED(hr)) {
		printf("MMFPlayer Error: CreateAndSetTopologySynchronous: Failed to set the topology of the session: %d (%x).\n", hr, hr);
	} else {
		printf("MMFPlayer Info: CreateAndSetTopologySynchronous: Set the topology of the session: %d (%x).\n", hr, hr);

		HRESULT ehr = PullMediaEventsUntilEventTypeSynchronous(MESessionTopologyStatus);

		if (FAILED(ehr)) {
			printf("MMFPlayer Error: CreateAndSetTopologySynchronous: An error occurred while setting the topology of the session: %d (%x).\n", ehr, ehr);
			hr = ehr;
		} else {
			if (MMFUtil::JMMF_DEBUG) {
				printf("MMFPlayer Info: CreateAndSetTopologySynchronous: Successfully set the topology of the session.\n");
			}
		}
	}

	return hr;
}

// copied and adapted from CPlayer
//  Adds a topology branch for one stream.
//
//  pSourcePD: The source's presentation descriptor.
//  iStream: Index of the stream to render.
//
//  Pre-conditions: The topology must be created already.
//
//  Notes: For each stream, we must do the following:
//    1. Create a source node associated with the stream.
//    2. Create an output node for the renderer.
//    3. Connect the two nodes.
//  The media session will resolve the topology, so we do not have
//  to worry about decoders or other transforms.

HRESULT MMFPlayer::AddBranchToPartialTopology(
    IMFPresentationDescriptor *pSourcePD,
    DWORD iStream) {
	if (m_pTopology == NULL) {
		return E_UNEXPECTED;
	}
	HRESULT hr = S_OK;

	IMFStreamDescriptor* pStreamDescriptor;
	IMFTopologyNode* pSourceNode;
	IMFTopologyNode* pOutputNode;
    BOOL streamSelected = FALSE;

	do {
		hr = pSourcePD->GetStreamDescriptorByIndex(iStream, &streamSelected, 
            &pStreamDescriptor);
		if (FAILED(hr)){
			printf("MMFPlayer Error: AddBranchToPartialTopology: Failed to get the stream descriptor at index %d: %d (%x).\n", iStream, hr, hr);
			break;
		}

		// Create the topology branch only if the stream is selected - IE if the user(?) wants to play it.
		if (streamSelected) {
			// Create a source node for this stream.
            hr = CreateSourceStreamNode(pSourcePD, pStreamDescriptor, &pSourceNode);
			if (FAILED(hr)){
				printf("MMFPlayer Error: AddBranchToPartialTopology: Failed to create a source node for index %d: %d (%x).\n", iStream, hr, hr);
				break;
			}

			// Create the output, sink node for the renderer.
            hr = CreateOutputNode(pStreamDescriptor, &pOutputNode);
            if (FAILED(hr)){
				printf("MMFPlayer Error: AddBranchToPartialTopology: Failed to create a sink node for index %d: %d (%x).\n", iStream, hr, hr);
				break;
			}

			// Add the source and sink nodes to the topology.
            hr = m_pTopology->AddNode(pSourceNode);
			if (FAILED(hr)){
				printf("MMFPlayer Error: AddBranchToPartialTopology: Failed to add the source node to the topology %d: %d (%x).\n", iStream, hr, hr);
				break;
			}

			hr = m_pTopology->AddNode(pOutputNode);
			if (FAILED(hr)){
				printf("MMFPlayer Error: AddBranchToPartialTopology: Failed to add the sink node to the topology %d: %d (%x).\n", iStream, hr, hr);
				break;
			}

			// Connect the source node to the sink node.  The resolver will find the
            // intermediate nodes needed to convert media types.
            hr = pSourceNode->ConnectOutput(0, pOutputNode, 0);
			// cache the source node, remove when setting the stop time is done differently
			if (SUCCEEDED(hr)) {
				IMFMediaTypeHandler *pHandler = NULL;
			    GUID guidMajorType = GUID_NULL;
				HRESULT cr;
				cr = pStreamDescriptor->GetMediaTypeHandler(&pHandler);
				if (SUCCEEDED(cr)) {
					cr = pHandler->GetMajorType(&guidMajorType);
					if (SUCCEEDED(cr)){
						if (guidMajorType == MFMediaType_Video) {
							// in order to get the frame rate (= num frames per second) get the media type from the handler, 
							// and call MFGetAttributeRatio
							IMFMediaType *pMediaType;
							cr = pHandler->GetCurrentMediaType(&pMediaType);
							if (SUCCEEDED(cr)) {
								cr = MFGetAttributeRatio(pMediaType, MF_MT_FRAME_RATE, &frameRateNumerator, &frameRateDenominator);
								if (FAILED(cr)) {
									printf("MMFPlayer Error: AddBranchToPartialTopology: Cannot retrieve the frame rate from the media type: %d (%x).\n", cr, cr);
								} else {
									printf("MMFPlayer Info: AddBranchToPartialTopology: Frame rate, numer.: %d, denom.: %d\n", frameRateNumerator, frameRateDenominator);
								}
							}
							
							if (pMediaType != NULL) {
								pMediaType->Release();
								pMediaType = NULL;
							}
						}
					}
				}
				if (pHandler != NULL) {
					pHandler->Release();
					pHandler = NULL;
				}
			}
		}
	} while (false);

	if (pStreamDescriptor != NULL) {
		pStreamDescriptor->Release();
		pStreamDescriptor = NULL;
	}
	if (pSourceNode != NULL) {
		pSourceNode->Release();
		pSourceNode = NULL;
	}
	if (pOutputNode != NULL) {
		pOutputNode->Release();
		pOutputNode = NULL;
	}

	return hr;
}

//  Creates a source-stream node for a stream.
//
//  pSourcePresD: Presentation descriptor for the media source.
//  pSourceStreamD: Stream descriptor for the stream.
//  ppNode: Receives a pointer to the new node.

HRESULT MMFPlayer::CreateSourceStreamNode(IMFPresentationDescriptor *pSourcePresD,
	IMFStreamDescriptor *pSourceStreamD, IMFTopologyNode **ppNode) {
	HRESULT hr = S_OK;
	
	if (pSourcePresD == NULL || pSourceStreamD == NULL || ppNode == NULL) {
		return E_POINTER;
	}

	IMFTopologyNode *pNode = NULL;

	do {
		// Create the source-stream node.
		HRESULT hr = MFCreateTopologyNode(MF_TOPOLOGY_SOURCESTREAM_NODE, &pNode);
		if (FAILED(hr)) {
			printf("MMFPlayer Error: CreateSourceStreamNode: Failed to create topology node: %d (%x)\n", hr, hr);
			break;
		}
		// Associate the node with the source by passing in a pointer to the media source
        // and indicating that it is the source
        hr = pNode->SetUnknown(MF_TOPONODE_SOURCE, m_pSource);
		if (FAILED(hr)) {
			printf("MMFPlayer Error: CreateSourceStreamNode: Failed to set the source for the node: %d (%x)\n", hr, hr);
			break;
		}
		// Set the node presentation descriptor attribute of the node by passing 
        // in a pointer to the presentation descriptor
        hr = pNode->SetUnknown(MF_TOPONODE_PRESENTATION_DESCRIPTOR, pSourcePresD);
		if (FAILED(hr)) {
			printf("MMFPlayer Error: CreateSourceStreamNode: Failed to set the presentation descriptor for the node: %d (%x)\n", hr, hr);
			break;
		}
		// Set the node stream descriptor attribute by passing in a pointer to the stream
        // descriptor
        hr = pNode->SetUnknown(MF_TOPONODE_STREAM_DESCRIPTOR, pSourceStreamD);
		if (FAILED(hr)) {
			printf("MMFPlayer Error: CreateSourceStreamNode: Failed to set the stream descriptor for the node: %d (%x)\n", hr, hr);
			break;
		} 
	} while (false);

	if (pNode != NULL) {
		// Return the IMFTopologyNode pointer to the caller.
		*ppNode = pNode;
		(*ppNode)->AddRef();

		pNode->Release();
	}
	return hr;
}

//  Creates an output node for a stream.
//
//  pSourceStreamD: Stream descriptor for the stream.
//  ppNode: Receives a pointer to the new node.
//
//  Notes:
//  This function does the following:
//  1. Chooses a renderer based on the media type of the stream.
//  2. Creates an IActivate object for the renderer.
//  3. Creates an output topology node.
//  4. Sets the IActivate pointer on the node.
HRESULT MMFPlayer::CreateOutputNode(IMFStreamDescriptor *pSourceStreamD,
    IMFTopologyNode **ppNode) {
	if (pSourceStreamD == NULL || ppNode == NULL) {
		return E_POINTER;
	}

	HRESULT hr = S_OK;

	IMFTopologyNode *pNode = NULL;
    IMFMediaTypeHandler *pHandler = NULL;
    IMFActivate *pRendererActivate = NULL;

    GUID guidMajorType = GUID_NULL;

	do {
		// Get the media type handler for the stream.
		hr = pSourceStreamD->GetMediaTypeHandler(&pHandler);
		if (FAILED(hr)) {
			printf("MMFPlayer Error: CreateOutputNode: Failed to get the media type handler of the source stream: %d (%x).\n", hr, hr);
			break;
		}
		
		// Get the major media type.
		hr = pHandler->GetMajorType(&guidMajorType);
		if (FAILED(hr)) {
			printf("MMFPlayer Error: CreateOutputNode: Failed to get the major type of the media handler: %d (%x).\n", hr, hr);
			break;
		}

		// Create an IMFActivate controller object for the renderer, based on the media type
        // The activation objects are used by the session in order to create the renderers 
        // only when they are needed - i.e. only right before starting playback.  The 
        // activation objects are also used to shut down the renderers.
        if (guidMajorType == MFMediaType_Audio) {
            // if the stream major type is audio, create the audio renderer.
            hr = MFCreateAudioRendererActivate(&pRendererActivate);
			if (FAILED(hr)) {
				printf("MMFPlayer Error: CreateOutputNode: Failed to create an audio renderer activation object: %d (%x).\n", hr, hr);
			}
        } else if (guidMajorType == MFMediaType_Video) {
			if (m_hwndVideo == NULL) {
				hr = E_UNEXPECTED;
				if (FAILED(hr)) {
					printf("MMFPlayer Error: CreateOutputNode: Failed to create a video renderer activation object without window handle: %d (%x).\n", hr, hr);
				}
			} else {
				// if the stream major type is video, create the video renderer, passing in the
				// video window handle - that's where the video will be playing.
				hr = MFCreateVideoRendererActivate(m_hwndVideo, &pRendererActivate);
				if (FAILED(hr)) {
					printf("MMFPlayer Error: CreateOutputNode: Failed to create a video renderer activation object: %d (%x).\n", hr, hr);
				}
			}
        } else {
            // fail if the stream type is not video or audio.  For example, fail
            // if we encounter a CC stream.
            hr = E_FAIL;
			printf("MMFPlayer Error: CreateOutputNode: Failed to create a node, the major type is audio nor video.\n");
        }
		if (FAILED(hr)) {
			break;
		}


		// Create a downstream node, the node that will represent the renderer.
		hr = MFCreateTopologyNode(MF_TOPOLOGY_OUTPUT_NODE, &pNode);

		if (FAILED(hr)) {
			printf("MMFPlayer Error: CreateOutputNode: Failed to create an output node for the renderer: %d (%x).\n", hr, hr);
			break;
		}
		// Store the IActivate object in the sink node - it will be extracted later by the
        // media session during the topology render phase.
        hr = pNode->SetObject(pRendererActivate);
		if (FAILED(hr)) {
			printf("MMFPlayer Error: CreateOutputNode: Failed to add the render activation object to the output node: %d (%x).\n", hr, hr);
		}
	} while (false);

	if (pNode != NULL) {
		// Return the IMFTopologyNode pointer to the caller.
		*ppNode = pNode;
		(*ppNode)->AddRef();

		pNode->Release();
	}
	if (pHandler != NULL) {
		pHandler->Release();
	}
	if (pRendererActivate != NULL) {
		pRendererActivate->Release();
	}
	return hr;
}

//  Callback for asynchronous BeginGetEvent method.
//
//  pAsyncResult: Pointer to the result.

HRESULT MMFPlayer::Invoke(IMFAsyncResult *pResult) {
	if (MMFUtil::JMMF_DEBUG) {
		printf("MMFPlayer Info: Invoke: Invoke called.\n");
	}
	if (m_pSession == NULL || cleanUpCalled) {
		if (MMFUtil::JMMF_DEBUG) {
			printf("MMFPlayer Warning: Invoke: Invoke called after the session has been closed and player is cleaning up.\n");
		}
		return E_UNEXPECTED;
	}
	if (m_state == PlayerState_Closed) {
		if (MMFUtil::JMMF_DEBUG) {
			printf("MMFPlayer Warning: Invoke: Invoke called after the session has been closed.\n");
		}
		return E_UNEXPECTED;
	}
	if (pResult == NULL) {
		if (MMFUtil::JMMF_DEBUG) {
			printf("MMFPlayer Warning: Invoke: Result is NULL.\n");
		}
		return E_UNEXPECTED;
	}
    IMFMediaEvent *pEvent = NULL;
	IUnknown *punkState = NULL;
	HRESULT hr = S_OK;

	EnterCriticalSection(&m_criticalSection);

	do {
		HRESULT hhrr = pResult->GetState(&punkState);
		if (SUCCEEDED(hhrr)) {
			// the result status is S_OK in case a timer has been called at the designated time
			// when the player is stopped by hand an event is generated but the status is an error code
			HRESULT statusHR = pResult->GetStatus();

			if (punkState == pStopState) {
				if (MMFUtil::JMMF_DEBUG) {
					printf("MMFPlayer Info: Invoke: Received stop time event, status is %d (%x).\n", statusHR, statusHR);
				}
				punkState->Release();
				if (SUCCEEDED(statusHR)) {
					if (synchronousMode) {// this should not happen anymore, no mixing of synchronous and asynchronous calls
						// pull out the end event to be able to continue in sync mode
						HRESULT eehr = m_pSession->EndGetEvent(pResult, &pEvent);
						printf("MMFPlayer Info: Invoke: Stop got EndGetEvent, %d (%x).\n", eehr, eehr);
					}
					hr = ProcessStopTimeEvent();
				}
				break;
			} 
			else if (punkState == pEndState) {
				if (MMFUtil::JMMF_DEBUG) {
					printf("MMFPlayer Info: Invoke: Received end of media event, status is %d (%x).\n", statusHR, statusHR);
				}
				punkState->Release();
				if (SUCCEEDED(statusHR)) {
					if (synchronousMode) {// this should not happen anymore, no mixing of synchronous and asynchronous calls
						// pull out the end event to be able to continue in sync mode
						HRESULT eehr = m_pSession->EndGetEvent(pResult, &pEvent);
						printf("MMFPlayer Info: Invoke: End got EndGetEvent, %d (%x).\n", eehr, eehr);
					}
					hr = ProcessEndTimeEvent();
				}
				break;
			}
			
		}
		// handle events
		hr = m_pSession->EndGetEvent(pResult, &pEvent);

		if (FAILED(hr)) {
			printf("MMFPlayer Error: Invoke: Failed to get the media end event: %d (%x).\n", hr, hr);

			break;
		} else {
			if (MMFUtil::JMMF_DEBUG) {
				printf("MMFPlayer Info: Invoke: Successfully got the media end event.\n");
			}
		}
		// handle event if the player is not closing
		
		//if (m_state != PlayerState_Closing) {
			hr = ProcessMediaEvent(pEvent);
           	if (FAILED(hr)) {
				printf("MMFPlayer Error: Invoke: Failed to process the media event: %d (%x).\n", hr, hr);
				//break; // don't break, keep on pulling events
			} else {
				if (MMFUtil::JMMF_DEBUG) {
					printf("MMFPlayer Info: Invoke: Successfully processed the media event.\n");
				}
			}
		//}
		
		if (hr != S_FALSE && m_state != PlayerState_Closed) {
			if (MMFUtil::JMMF_DEBUG) {
				printf("MMFPlayer Info: Invoke: Begin get event.\n");
			}
			hr = m_pSession->BeginGetEvent((IMFAsyncCallback*)this, NULL);
		}
		
		
	} while (false);

	if (punkState != NULL) {
		punkState->Release();
	}
	if (pEvent != NULL) {
		pEvent->Release();
	}

	LeaveCriticalSection(&m_criticalSection);

	return hr;
}

//
//  Called by Invoke() to do the actual event processing, and determine what, if anything,
//  needs to be done.  Returns S_FALSE if the media event type is MESessionClosed.
//
HRESULT MMFPlayer::ProcessMediaEvent(IMFMediaEvent *pMediaEvent) {
    HRESULT hrStatus = S_OK;            // Event status
    HRESULT hr = S_OK;
    UINT32 TopoStatus = MF_TOPOSTATUS_INVALID; 
    MediaEventType eventType;
    do {
		if( pMediaEvent == NULL) {
			hr = E_POINTER;	
			printf("MMFPlayer Error: ProcessMediaEvent: The event is NULL.\n");
			break;
		}

        // Get the event type.
        hr = pMediaEvent->GetType(&eventType);
		if(FAILED(hr)) {
			printf("MMFPlayer Error: ProcessMediaEvent: Failed to get the media event type.\n");
			break;
		} else {
			// Switch on the event type. Update the internal state of the Player as needed.
			if (MMFUtil::JMMF_DEBUG) {
				printf("MMFPlayer Info: ProcessMediaEvent: The media event type is: ");
				switch(eventType) {
					case MESessionTopologySet:
						printf("%d - MESessionTopologySet.\n", eventType);
						break;
					case MESessionStarted:
						printf("%d - MESessionStarted.\n", eventType);
						break;
					case MESessionPaused:
						printf("%d - MMESessionPaused.\n", eventType);
						break;
					case MESessionStopped:
						printf("%d - MESessionStopped.\n", eventType);
						break;
					case MESessionClosed:
						printf("%d - MESessionClosed.\n", eventType);
						break;
					case MESessionEnded:
						printf("%d - MESessionEnded.\n", eventType);
						break;
					case MESessionRateChanged:
						printf("%d - MESessionRateChanged.\n", eventType);
						break;
					case MESessionScrubSampleComplete:
						printf("%d - MESessionScrubSampleComplete.\n", eventType);
						break;
					case MESessionCapabilitiesChanged:
						printf("%d - MESessionCapabilitiesChanged.\n", eventType);
						break;
					case MESessionTopologyStatus:
						printf("%d - MESessionTopologyStatus.\n", eventType);
						break;
					case MESessionNotifyPresentationTime:
						printf("%d - MESessionNotifyPresentationTime.\n", eventType);
						break;
					case MENewPresentation:
						printf("%d - MENewPresentation.\n", eventType);
						break;
					case MESessionStreamSinkFormatChanged:
						printf("%d - MESessionStreamSinkFormatChanged.\n", eventType);
						break;
					case MEEndOfPresentation:
						printf("%d - MEEndOfPresentation.\n", eventType);
						break;
					default:
						printf("%d.\n", eventType);
				}
			}
		}

        // Get the event status. If the operation that triggered the event did
        // not succeed, the status is a failure code.
        hr = pMediaEvent->GetStatus(&hrStatus);
		if(FAILED(hr)) {
			printf("MMFPlayer Error: ProcessMediaEvent: Failed to get the media event status.\n");
			break;
		} else {
			if (MMFUtil::JMMF_DEBUG) {
				printf("MMFPlayer Info: ProcessMediaEvent: The media event status is: %d (%x)\n", hrStatus, hrStatus);
			}
		}

        // Check if the async operation succeeded.
        if (FAILED(hrStatus)) {
            hr = hrStatus;
			if (MMFUtil::JMMF_DEBUG) {
				printf("MMFPlayer Warning: ProcessMediaEvent: The media event status indicates an error: %d (%x).\n", hr, hrStatus);
			}
            //break;//?? this prevents checking the event type, maybe don't break?
		} else {
			if (MMFUtil::JMMF_DEBUG) {
				printf("MMFPlayer Info: ProcessMediaEvent: The media event status is OK.\n");
			}
		}

		// Switch on the event type. Update the internal state of the Player as needed.
		// Because of the do-while(false) pattern we don't use a switch statement here
        if(eventType == MESessionTopologyStatus) {
            // Get the status code.
            hr = pMediaEvent->GetUINT32(MF_EVENT_TOPOLOGY_STATUS, (UINT32*)&TopoStatus);
            if(FAILED(hr)) {
				printf("MMFPlayer Error: ProcessMediaEvent: Failed to get the topology status: %d (%x).\n", hr, hr);
				break;
			}

            if (TopoStatus == MF_TOPOSTATUS_READY) {
				if (MMFUtil::JMMF_DEBUG) {
					printf("MMFPlayer Warning: ProcessMediaEvent: Topology status: Ready.\n");
				}
                m_state = PlayerState_Ready;//??
			
                hr = OnTopologyReady();
            }
		} else if(eventType == MEEndOfPresentation || eventType == MESessionEnded) {
            //m_state = PlayerState_Stopped;
			m_state = PlayerState_Paused;
			// the session rewinds to begin time, try to prevent this//
			//setMediaPosition(getDuration() - 800000);
        } else if (eventType == MESessionClosed) {
			if (pendingAction->action == SetStateClosed) {
				m_state = PlayerState_Closed;
				clearPendingAction(pendingAction);
			}
			if (MMFUtil::JMMF_DEBUG) {
				printf("MMFPlayer Info: ProcessMediaEvent: Setting close event.\n");
			}
            // signal to anybody listening that the session is closed
			// maybe this is not needed since the player is managed in a JVM
            //SetEvent(m_closeCompleteEvent);
		} else if (eventType == MESessionPaused) {
			if (MMFUtil::JMMF_DEBUG) {
				printf("MMFPlayer Info: ProcessMediaEvent: Media paused at time %lld.\n", getMediaPosition());
			}
			if (pendingAction->action == SetStatePaused) {
				m_state = PlayerState_Paused;
				if (MMFUtil::JMMF_CORRECT_AT_PAUSE) { 
					// this forces the video 2 or 3 frames back to the position of the time
					if (pendingAction->timeValue > 0) {
						setMediaPosition(pendingAction->timeValue);
					} else {
						printf("MMFPlayer Error: ProcessMediaEvent: The pending media pause time was set to -1.\n");
					}
				}
				clearPendingAction(pendingAction);
			} 
			//else if (pendingAction->action == SetMediaPosition) {
			//}
			//setMediaPosition(getMediaPosition());
		} else if (eventType == MESessionStarted) {
			if (pendingAction->action == SetStateStarted) {
				clearPendingAction(pendingAction);
				clearPendingAction(cachedAction);
				if (MMFUtil::JMMF_DEBUG) {
					printf("MMFPlayer Info: ProcessMediaEvent: Set started state.\n");
				}
				m_state = PlayerState_Started;
			} else if (pendingAction->action == SetMediaPosition) {
				if (MMFUtil::JMMF_DEBUG) {
					printf("MMFPlayer Info: ProcessMediaEvent: Started media event, as part of a seek operation position: %lld.\n", pendingAction->timeValue);
				}
			} else {
				if (MMFUtil::JMMF_DEBUG) {
					printf("MMFPlayer Info: ProcessMediaEvent: Started media event without setting started state: %lld.\n", getMediaPosition());
				}
			} 
		} else if (eventType == MESessionStopped) {
			if (pendingAction->action == SetStateStopped) {
					clearPendingAction(pendingAction);
					m_state = PlayerState_Stopped;
					
				}
		} else if (eventType == MESessionRateChanged) {
			if (FAILED(hrStatus)) {
				printf("MMFPlayer Error: ProcessMediaEvent: Setting rate failed, requested rate %f, actual rate %f.\n", userPlaybackRate, getRate());
			}
			if (pendingAction->action == SetRate) {
				clearPendingAction(pendingAction);
				
				if (cachedAction->action == SetRate) {
					setRateCached((float)cachedAction->rateValue);
					clearPendingAction(cachedAction);
				}
			}
		} else if (eventType == MESessionScrubSampleComplete) {
			if (pendingAction->action == SetMediaPosition) {
				if (MMFUtil::JMMF_DEBUG) {
					printf("MMFPlayer Info: ProcessMediaEvent: Started media event, as part of a seek operation: %lld.\n", getMediaPosition());
					if (pendingAction->timeValue != getMediaPosition()) {
						printf("MMFPlayer Warning: ProcessMediaEvent: Media set to the wrong position: %lld, %lld.\n", pendingAction->timeValue, getMediaPosition());
					}
				}
				clearPendingAction(pendingAction);
				// the last event of a set position or seek action is the scrub complete
				if (cachedAction->action == SetMediaPosition) {
					setMediaPositionCached(cachedAction->timeValue);
					clearPendingAction(cachedAction);
				} else {
					m_state = PlayerState_Paused;
				}
			}

		}
    }
    while(false);
	
    return hr;
}

/*
* Checks a media event that has been produced in synchronous event handling mode.
*/
HRESULT MMFPlayer::ProcessMediaEventSynchronous(IMFMediaEvent *pMediaEvent) {
    HRESULT hrStatus = S_OK;            // Event status
    HRESULT hr = S_OK;
    UINT32 TopoStatus = MF_TOPOSTATUS_INVALID; 
    MediaEventType eventType;
    do {
		if( pMediaEvent == NULL) {
			hr = E_POINTER;	
			printf("MMFPlayer Error: ProcessMediaEventSynchronous: The event is NULL.\n");
			break;
		}

        // Get the event type.
        hr = pMediaEvent->GetType(&eventType);
		if(FAILED(hr)) {
			printf("MMFPlayer Error: ProcessMediaEventSynchronous: Failed to get the media event type.\n");
			break;
		} else {
			// Switch on the event type. Update the internal state of the Player as needed.
			if (MMFUtil::JMMF_DEBUG) {
				printf("MMFPlayer Info: ProcessMediaEventSynchronous: The media event type is: ");
				switch(eventType) {
					case MESessionTopologySet:
						printf("%d - MESessionTopologySet.\n", eventType);
						break;
					case MESessionStarted:
						printf("%d - MESessionStarted.\n", eventType);
						break;
					case MESessionPaused:
						printf("%d - MMESessionPaused.\n", eventType);
						break;
					case MESessionStopped:
						printf("%d - MESessionStopped.\n", eventType);
						break;
					case MESessionClosed:
						printf("%d - MESessionClosed.\n", eventType);
						break;
					case MESessionEnded:
						printf("%d - MESessionEnded.\n", eventType);
						break;
					case MESessionRateChanged:
						printf("%d - MESessionRateChanged.\n", eventType);
						break;
					case MESessionScrubSampleComplete:
						printf("%d - MESessionScrubSampleComplete.\n", eventType);
						break;
					case MESessionCapabilitiesChanged:
						printf("%d - MESessionCapabilitiesChanged.\n", eventType);
						break;
					case MESessionTopologyStatus:
						printf("%d - MESessionTopologyStatus.\n", eventType);
						break;
					case MESessionNotifyPresentationTime:
						printf("%d - MESessionNotifyPresentationTime.\n", eventType);
						break;
					case MENewPresentation:
						printf("%d - MENewPresentation.\n", eventType);
						break;
					case MESessionStreamSinkFormatChanged:
						printf("%d - MESessionStreamSinkFormatChanged.\n", eventType);
						break;
					case MEEndOfPresentation:
						printf("%d - MEEndOfPresentation.\n", eventType);
						break;
					default:
						printf("%d.\n", eventType);
				}
			}
		}

        // Get the event status. If the operation that triggered the event did
        // not succeed, the status is a failure code.
        hr = pMediaEvent->GetStatus(&hrStatus);
		if(FAILED(hr)) {
			printf("MMFPlayer Error: ProcessMediaEventSynchronous: Failed to get the media event status.\n");
			break;
		} else {
			if (MMFUtil::JMMF_DEBUG) {
				printf("MMFPlayer Info: ProcessMediaEventSynchronous: The media event status is: %d (%x)\n", hrStatus, hrStatus);
			}
		}

        // Check if the media operation succeeded.
        if (FAILED(hrStatus)) {
            hr = hrStatus;
			if (MMFUtil::JMMF_DEBUG) {
				printf("MMFPlayer Warning: ProcessMediaEventSynchronous: The media event status indicates an error: %d (%x).\n", hr, hrStatus);
			}
            //break;//?? this prevents checking the event type, maybe don't break?
		} else {
			if (MMFUtil::JMMF_DEBUG) {
				printf("MMFPlayer Info: ProcessMediaEventSynchronous: The media event status is OK.\n");
			}
		}

		// Switch on the event type. Update the internal state of the Player as needed.
		// Because of the do-while(false) pattern we don't use a switch statement here
        if (eventType == MESessionClosed) {
			m_state = PlayerState_Closed;
			
			if (MMFUtil::JMMF_DEBUG) {
				printf("MMFPlayer Info: ProcessMediaEventSynchronous: Setting close event.\n");
			}
		} else if (eventType == MESessionPaused) {
			if (MMFUtil::JMMF_DEBUG) {
				printf("MMFPlayer Info: ProcessMediaEventSynchronous: Media paused at time %lld.\n", getMediaPosition());
			}

			m_state = PlayerState_Paused;
		} else if (eventType == MESessionStarted) {
			if (MMFUtil::JMMF_DEBUG) {
				printf("MMFPlayer Info: ProcessMediaEventSynchronous: Set started state.\n");
			}
			m_state = PlayerState_Started; 
		} else if (eventType == MESessionStopped) {
			if (MMFUtil::JMMF_DEBUG) {
				printf("MMFPlayer Info: ProcessMediaEventSynchronous: Set stopped state.\n");
			}
			m_state = PlayerState_Stopped;
		} else if (eventType == MESessionRateChanged) {
			if (MMFUtil::JMMF_DEBUG) {
				printf("MMFPlayer Info: ProcessMediaEventSynchronous: The rate changed.\n");
			}
			if (FAILED(hrStatus)) {
				printf("MMFPlayer Error: ProcessMediaEventSynchronous: Setting rate failed, requested rate %f, actual rate %f.\n", userPlaybackRate, getRate());
			}
		} else if (eventType == MESessionScrubSampleComplete) {
			if (MMFUtil::JMMF_DEBUG) {
				printf("MMFPlayer Info: ProcessMediaEventSynchronous: Scrub frame complete.\n");
			}
			m_state = PlayerState_Paused;
		} else if (eventType == MESessionTopologyStatus) {
				// only in this case we'll worry about the event status
			if (FAILED(hrStatus)) {
				printf("MMFPlayer Error: ProcessMediaEventSynchronous: The topology status event indicates a status error: %d (%x).\n", hrStatus, hrStatus);
				hr = hrStatus;
				break;
			}
			HRESULT subHr = pMediaEvent->GetUINT32(MF_EVENT_TOPOLOGY_STATUS, (UINT32*)&TopoStatus);
			
			if (FAILED(subHr)) {
				printf("MMFPlayer Error: ProcessMediaEventSynchronous: Failed to get the toplogy status of the event: %d (%x).\n", subHr, subHr);
				hr = subHr;
				break;
			}
			if (TopoStatus == MF_TOPOSTATUS_READY) {
				if (MMFUtil::JMMF_DEBUG) {
					printf("MMFPlayer Info: ProcessMediaEventSynchronous: Topology status: Ready.\n");
				}
				m_state = PlayerState_Ready;//??

				hr = OnTopologyReady();
				break;
			} else {
				if (MMFUtil::JMMF_DEBUG) {
					printf("MMFPlayer Info: ProcessMediaEventSynchronous: Topology status: Noy Ready.\n");
				}
			}
		}
    }
    while(false);
	
    return hr;
}

/*
* Method to pull events from the session until the expected event occurred.
* If S_OK is returned the pEvent parameter will hold the event.
* The caller is responsible for releasing the event. 
*/
HRESULT MMFPlayer::PullMediaEventsUntilEventTypeSynchronous(MediaEventType eventType) {
	if (m_pSession == NULL) {
		printf("MMFPlayer Warning: PullMediaEventsUntilEventTypeSynchronous: The media session is null.\n");

		return E_POINTER;
	}

	if (MMFUtil::JMMF_DEBUG) {
		printf("MMFPlayer Info: PullMediaEventsUntilEventTypeSynchronous: Start pulling until event type %d.\n", eventType);
	}

	IMFMediaEvent *pEvent = NULL;
	HRESULT hr = S_OK;
	HRESULT hrType = S_OK;
	MediaEventType currentType = MEUnknown;
	int counter = 0;
	int maxNumEvents = 10;
	do {
		counter++;
		hr = m_pSession->GetEvent(0, &pEvent);
		if (FAILED(hr)) {
			printf("MMFPlayer Error: PullMediaEventsUntilEventTypeSynchronous: Unable to GetEvent from Session %d (%x).\n", hr, hr);
			if (MMFUtil::JMMF_DEBUG) {
				if (hr == MF_E_MULTIPLE_SUBSCRIBERS) {
					printf("MMFPlayer Error: PullMediaEventsUntilEventTypeSynchronous: MF_E_MULTIPLE_SUBSCRIBERS.\n");
					// calling m_pSession->EndGetEvent(0,&pEvent); here doesn't help, it doesn't remove the multiple subscribers error
				} 
				else if (hr == MF_E_NO_EVENTS_AVAILABLE) {
					printf("MMFPlayer Error: PullMediaEventsUntilEventTypeSynchronous: MF_E_NO_EVENTS_AVAILABLE.\n");
				} 
				else if (hr == MF_E_SHUTDOWN) {
					printf("MMFPlayer Error: PullMediaEventsUntilEventTypeSynchronous: MF_E_SHUTDOWN.\n");
				}
				else if (hr == E_INVALIDARG) {
					printf("MMFPlayer Error: PullMediaEventsUntilEventTypeSynchronous: E_INVALIDARG.\n");
				}
				else {
					printf("MMFPlayer Error: PullMediaEventsUntilEventTypeSynchronous: unknown error.\n");
				}			
			}
			break;
		}
		// use other result 
		hr = ProcessMediaEventSynchronous(pEvent);

		if (FAILED(hr)) {
			printf("MMFPlayer Error: PullMediaEventsUntilEventTypeSynchronous: Error during processing of the event %d (%x).\n", hr, hr);
			break;
		} else {
			hrType = pEvent->GetType(&currentType);
			if (FAILED(hrType)) {//
				printf("MMFPlayer Error: PullMediaEventsUntilEventTypeSynchronous: Unable to GetType of the event %d (%x).\n", hrType, hrType);
				hr = hrType;
				break;
			} else {
				if (eventType == currentType) {
					// success
					// the caller can check status etc.
					if (MMFUtil::JMMF_DEBUG) {
						printf("MMFPlayer Info: PullMediaEventsUntilEventTypeSynchronous: Found the right event type in %d attempts.\n", counter);
					}
					break;
				} else {
					if (MMFUtil::JMMF_DEBUG) {
						printf("MMFPlayer Info: PullMediaEventsUntilEventTypeSynchronous: Not the right event type in attempt: %d.\n", counter);
					}
				}
			}
		}

		if (counter == maxNumEvents - 1) {
			hr = S_FALSE;//To Do find an appropriate return value
			printf("MMFPlayer Error: PullMediaEventsUntilEventTypeSynchronous: Did not get the requested event type within %d tries.\n", maxNumEvents);
		}

	} while (counter < 10);

	if (pEvent != NULL) {
		pEvent->Release();
	}

	return hr;
}

/*
* Stops the player after receiving a StopTimer event via Invoke.
*/
HRESULT MMFPlayer::ProcessStopTimeEvent() {
	if (cleanUpCalled) {
		printf("MMFPlayer Info: ProcessStopTimeEvent: Timer callback called after closing the session.\n");
	}
	HRESULT hr = S_OK;
	__int64 curTime = getMediaPosition();
	if (MMFUtil::JMMF_DEBUG) {
		printf("MMFPlayer Info: ProcessStopTimeEvent: Timer callback called at %lld.\n", curTime);
	}
	__int64 diff = stopTime - curTime;
	if (diff > -1000000 && diff < 1000000) {// arbitrary precision
		hr = pause();
	} else {
		if (MMFUtil::JMMF_DEBUG) {
			printf("MMFPlayer Warning: ProcessStopTimeEvent: false callback, wrong time: %lld, stop time: %lld.\n", curTime, stopTime);
		}
	}
	return hr;
}

/*
* Stops the player when a (close to) end of media timer event has been received. 
* By default the session rewinds to the begin of media once the end has been reached.
*/
HRESULT MMFPlayer::ProcessEndTimeEvent() {
	if (cleanUpCalled) {
		printf("MMFPlayer Info: ProcessEndTimeEvent: Timer callback called after closing the session.\n");
	}
	HRESULT hr = S_OK;
	__int64 curTime = getMediaPosition();
	if (MMFUtil::JMMF_DEBUG) {
		printf("MMFPlayer Info: ProcessEndTimeEvent: Timer callback called at %lld, duration is %lld.\n", curTime, duration);
	}

	hr = pause();

	return hr;
}

/*
* Returns the current player state, without taking into account pending
* player state changes.
*/
int MMFPlayer::getPlayerState() {
	return m_state;
}

/*
* Get the video display, clock, rate support, rate control etc. when the topology is resolved and ready
*/
HRESULT MMFPlayer::OnTopologyReady() {
	HRESULT hr = S_OK;

    do {
		// release any previous instance of the m_pVideoDisplay interface
        //m_pVideoDisplay->Release();

		// Ask the session for the IMFVideoDisplayControl interface. This interface is 
        // implemented by the EVR (Enhanced Video Renderer) and is exposed by the media 
        // session as a service.  The session will query the topology for the right 
        // component and return this EVR interface.  The interface will be used to tell the
        // video to repaint whenever the hosting window receives a WM_PAINT window message.
        hr = MFGetService(m_pSession, MR_VIDEO_RENDER_SERVICE,  IID_IMFVideoDisplayControl,
                (void**)&m_pVideoDisplay);
		if (FAILED(hr)) {
			printf("MMFPlayer Error: OnTopologyReady: Failed to get the video renderer service (VideoDisplayControl).\n");
		} else {
			if (MMFUtil::JMMF_DEBUG) {
				printf("MMFPlayer Info: OnTopologyReady: Successfully got the video renderer service (VideoDisplayControl).\n");
			}
			// load the first video frame? 
			// the rendering prefs used here require Win 7 or >, comment out for Vista
			m_pVideoDisplay->SetRenderingPrefs(MFVideoRenderPrefs_DoNotRepaintOnStop |
				MFVideoRenderPrefs_AllowBatching | MFVideoRenderPrefs_AllowOutputThrottling);
			//m_pVideoDisplay->SetRenderingPrefs(MFVideoRenderPrefs_DoNotRepaintOnStop);
			m_pVideoDisplay->SetAspectRatioMode(MFVideoARMode_None);
			if (tempW != 0 && tempH != 0) {
				setVideoDestinationPos(0, 0, tempW, tempH);
			}
		}
		// The audio volume interface.
		// This may fail, e.g.when there is no audio.
		/*
		HRESULT hhrr = MFGetService(m_pSession, MR_POLICY_VOLUME_SERVICE, 
			__uuidof(IMFSimpleAudioVolume), (void**)&m_pVolume);
		if (FAILED(hhrr)) {
			printf("MMFPlayer Warning: OnTopologyReady: Unable to get the volume control, maybe there is no audio: %d.\n", hhrr);
		} else {
			if (MMFUtil::JMMF_DEBUG) {
				printf("MMFPlayer Info: OnTopologyReady: Successfully got the volume control.\n");
			}
		}
		*/
		// audio stream and channel level control
		
		HRESULT hhrr = MFGetService(m_pSession, MR_STREAM_VOLUME_SERVICE,
			__uuidof(IMFAudioStreamVolume), (void**)&m_pStreamVolume);
				if (FAILED(hhrr)) {
			printf("MMFPlayer Warning: OnTopologyReady: Unable to get the audio stream volume control, maybe there is no audio: %d (%x).\n", hhrr, hhrr);
		} else {
			if (MMFUtil::JMMF_DEBUG) {
				printf("MMFPlayer Info: OnTopologyReady: Successfully got the audio stream volume control.\n");
			}
		}
		
		// get clock and rate control
		IMFClock *pClock = NULL;
		// Get the presentation clock (optional)
		hhrr = m_pSession->GetClock(&pClock);
		if (SUCCEEDED(hhrr)) {
			hr = pClock->QueryInterface(__uuidof(IMFPresentationClock), (void**)&m_pClock);
			// get a Timer objects
			if (SUCCEEDED(hr)) {
				if (MMFUtil::JMMF_DEBUG) {
					printf("MMFPlayer Info: OnTopologyReady: Successfully got the Clock interface.\n");
				}
				if (!synchronousMode) {
					hr = m_pClock->QueryInterface(__uuidof(IMFTimer) ,(void**) &m_pStopTimer);

					if (FAILED(hr)) {
						printf("MMFPlayer Error: OnTopologyReady: Failed to get a Timer instance for stop time notifications: %d (%x).\n", hr, hr);
					} else {
						if (MMFUtil::JMMF_DEBUG) {
							printf("MMFPlayer Info: OnTopologyReady: Successfully got a Timer instance for stop time notifications.\n");
						}
					}
					hr = m_pClock->QueryInterface(__uuidof(IMFTimer) ,(void**) &m_pEndTimer);

					if (FAILED(hr)) {
						printf("MMFPlayer Error: OnTopologyReady: Failed to get a Timer instance for end of media notifications: %d (%x).\n", hr, hr);
					} else {
						if (MMFUtil::JMMF_DEBUG) {
							printf("MMFPlayer Info: OnTopologyReady: Successfully got a Timer instance for end of media notifications.\n");
						}
					}
				}
			}
		} else {
			printf("MMFPlayer Error: OnTopologyReady: Failed to get a Clock interface: %d (%x).\n", hhrr, hhrr);
			hr = hhrr;
		}

		// Get the rate control interface (optional)
		hhrr = MFGetService(m_pSession, MF_RATE_CONTROL_SERVICE, __uuidof(IMFRateControl), (void**)&m_pRate);
		if (FAILED(hhrr)) {
			printf("MMFPlayer Warning: OnTopologyReady: Failed to get the Rate Control: %d (%x).\n", hhrr, hhrr);
			//hr = hhrr; //non crucial control, don't fail
		} else {
			if (MMFUtil::JMMF_DEBUG) {
				printf("MMFPlayer Info: OnTopologyReady: Successfully got the Rate Control.\n");
			}
			// try to get a rate support object as well
			hhrr = MFGetService(m_pSession, MF_RATE_CONTROL_SERVICE, __uuidof(IMFRateSupport),
				(void**)&m_pRateSupport);
			if (FAILED(hhrr)) {
				printf("MMFPlayer Warning: OnTopologyReady: Failed to get the Rate Support: %d (%x).\n", hhrr, hhrr);
			} else {
				printf("MMFPlayer Info: OnTopologyReady: Successfully got the Rate Support.\n");
			}
		}

	} while (false);
	//m_state = PlayerState_Ready;
	topoInited = true;
	setVolume(initVolume);
	setMediaPosition(initMediaTime);
	
	return hr;
}

// IUnknown methods
HRESULT MMFPlayer::QueryInterface(REFIID riid, void** ppv) {
	if (MMFUtil::JMMF_DEBUG) {
		printf("MMFPlayer Info: QueryInterface: Called.\n");	
	}
	
	HRESULT hr = S_OK;

    if(ppv == NULL) {
        return E_POINTER;
    }

    if(riid == __uuidof(IMFAsyncCallback)) {
        *ppv = static_cast<IMFAsyncCallback*>(this);
    }
    else if(riid == __uuidof(IUnknown)) {
        *ppv = static_cast<IUnknown*>(this);
    }
    else {
        *ppv = NULL;
        hr = E_NOINTERFACE;
    }

	if(SUCCEEDED(hr)) {
        AddRef();
	}

    return hr;
}

// IUnknown method
ULONG MMFPlayer::AddRef() {
	if (MMFUtil::JMMF_DEBUG) {
		printf("MMFPlayer Info: AddRef: Called.\n");
	}
	
    return InterlockedIncrement(&m_nRefCount);
}

// IUnknown method
ULONG MMFPlayer::Release() {
	if (MMFUtil::JMMF_DEBUG) {
		printf("MMFPlayer Info: Release: Called.\n");
	}
	
    ULONG uCount = InterlockedDecrement(&m_nRefCount);
    if (uCount == 0) {
		if (MMFUtil::JMMF_DEBUG) {
			printf("MMFPlayer Info: Release: Count = 0, player can be deleted.\n");
		}
        //delete this;// do not delete
		// deletion is performed by the JNI code
    }
    return uCount;
}

/**
* Tries to close and release all resources, calls MFShutdown and
* CoUninitialize etc.
*/
HRESULT MMFPlayer::cleanUpOnClose() {
	if (cleanUpCalled) {
		if (MMFUtil::JMMF_DEBUG) {
			printf("MMFPlayer Info: cleanUpOnClose: Clean has already been called.\n");
		}
		return S_OK;
	}
	// stop player, end session, release all objects etc
	cleanUpCalled = true;
	if (m_state == PlayerState_Started || m_state == PlayerState_Paused) {
		stop();
		// should wait for Stopped event? Or skip this and just rely on Session->Close
	}

	if(m_pVideoDisplay != NULL) {
		//m_pVideoDisplay->SetVideoWindow(NULL);
		m_pVideoDisplay->Release();
		m_pVideoDisplay = NULL;
	}
	m_state = PlayerState_Closing;
	pendingAction->action = SetStateClosed;
	HRESULT hr = m_pSession->Close();

	if (FAILED(hr)) {
		printf("MMFPlayer Error: cleanUpOnClose: Closing the Session failed.\n");
	} else {
		if (MMFUtil::JMMF_DEBUG) {
			printf("MMFPlayer Info: cleanUpOnClose: Closing the Session succeeded.\n");
		}
	}
	// wait for close event or continue from Invoke when the closed event has been received?
	
	DWORD result = WaitForSingleObject(m_closeCompleteEvent, 5000);
	if (result == WAIT_TIMEOUT)	{
		printf("MMFPlayer Error: cleanUpOnClose: Closing Session timed out!\n");
		return E_UNEXPECTED;
	}
	hr = FinalClosing();
	if (FAILED(hr)) {
		printf("MMFPlayer Error: cleanUpOnClose: Failed to finalize close operation: %d (%x).\n", hr, hr);
	} else {
		if (MMFUtil::JMMF_DEBUG) {
			printf("MMFPlayer Info: cleanUpOnClose: Successfully finalized the close operation.\n");
		}
	}
	
	return hr;
}
/*
* Tries to close the session without waiting for the close event.
*/
HRESULT MMFPlayer::CloseSession() {
	
	if (cleanUpCalled) {
		if (MMFUtil::JMMF_DEBUG) {
			printf("MMFPlayer Info: CloseSession: Clean has already been called.\n");
		}
		return E_UNEXPECTED;
	}

	if (m_state == PlayerState_Closing || m_state == PlayerState_Closed) {
		if (MMFUtil::JMMF_DEBUG) {
			printf("MMFPlayer Info: CloseSession: Session is already closed or closing.\n");
		}
		return E_UNEXPECTED;
	}
	
	if (synchronousMode) {
		return CloseSessionSynchronous();
	} else {
		return CloseSessionA();
	}
}

/*
* The asynchronous part of closing the session. 
*/
HRESULT MMFPlayer::CloseSessionA() {
	// stop the stop timer but a callback can still be expected after this
	// not necessary? leads to crashes?
	HRESULT hr = S_OK;
	//if (m_pStopTimer != NULL) {
	//	//hr = m_pStopTimer->CancelTimer(m_pTimerCancelKey);
	//	if (FAILED(hr)) {
	//		printf("MMFPlayer Info: CloseSession: Failed to cancel the stop timer.\n");
	//	}
	//}

	// It is assumed the player is stopped or paused beforehand!
	if (m_state == PlayerState_Started || m_state == PlayerState_Paused) {
		stop();
		// should wait for Stopped event? Or skip this and just rely on Session->Close
	}

	if(m_pVideoDisplay != NULL) {
		//m_pVideoDisplay->SetVideoWindow(NULL);
		m_pVideoDisplay->Release();
		m_pVideoDisplay = NULL;
	}
	EnterCriticalSection(&m_criticalSection);
	m_state = PlayerState_Closing;
	pendingAction->action = SetStateClosed;
	hr = m_pSession->Close();

	if (MMFUtil::JMMF_DEBUG) {
		printf("MMFPlayer Info: CloseSessionA: Session closing succeeded %d (%x).\n", hr, hr);
	}
	LeaveCriticalSection(&m_criticalSection);

	return hr;
}

/*
* The synchronous part of closing the session.
*/
HRESULT MMFPlayer::CloseSessionSynchronous() {
	HRESULT hr = S_OK;
	// It is assumed the player is stopped or paused beforehand!
	if (m_state == PlayerState_Started || m_state == PlayerState_Paused) {
		stop();
	}

	if(m_pVideoDisplay != NULL) {
		//m_pVideoDisplay->SetVideoWindow(NULL);
		m_pVideoDisplay->Release();
		m_pVideoDisplay = NULL;
	}

	EnterCriticalSection(&m_criticalSection);

	m_state = PlayerState_Closing;
	pendingAction->action = SetStateClosed;
	hr = m_pSession->Close();

	if (SUCCEEDED(hr)) {
		HRESULT ehr = PullMediaEventsUntilEventTypeSynchronous(MESessionClosed);
		if (SUCCEEDED(ehr)) {
			if (MMFUtil::JMMF_DEBUG) {
				printf("MMFPlayer Info: CloseSessionSynchronous: Session closing succeeded %d (%x).\n", ehr, ehr);
			}
		} else {
			printf("MMFPlayer Info: CloseSessionSynchronous: Failed to get the Session closed event %d (%x).\n", ehr, ehr);
		}
	} else {
		if (MMFUtil::JMMF_DEBUG) {
			printf("MMFPlayer Info: CloseSessionSynchronous: Session closing failed %d (%x).\n", hr, hr);
		}
	}

	LeaveCriticalSection(&m_criticalSection);

	return hr;
}
/*
* This is called when the session has been closed (and should not otherwise be called).
* First shutdown and close the source and session and then release other objects.
* Then call MFShutdown? 
*/
HRESULT MMFPlayer::FinalClosing() {
	HRESULT hr = S_OK;
	//m_state = PlayerState_Closing;

	if (m_pSource != NULL) {
		hr = m_pSource->Shutdown();
		if (FAILED(hr) && MMFUtil::JMMF_DEBUG) {
			printf("MMFPlayer Error: FinalClosing: Failed to shutdown the Source.\n");
		}
		//printf("MMFPlayer Info: FinalClosing: Shutdown the Source.\n");
	}
	if (m_pSession != NULL) {
		hr = m_pSession->Shutdown();
		if (FAILED(hr) && MMFUtil::JMMF_DEBUG) {
			printf("MMFPlayer Error: FinalClosing: Failed to shutdown the Session.\n");
		}
		//printf("MMFPlayer Info: FinalClosing: Shutdown the Session.\n");
	}

	if (m_pSource != NULL) {
		m_pSource->Release();
		m_pSource = NULL;
		//printf("MMFPlayer Info: FinalClosing: Releasing the Source.\n");
	}

	if (m_pSession != NULL) {
		m_pSession->Release();
		m_pSession = NULL;
		//printf("MMFPlayer Info: FinalClosing: Releasing the Session.\n");
	}

	if (m_pStopTimer != NULL) {
		//m_pStopTimer->CancelTimer(m_pTimerCancelKey);

		m_pStopTimer->Release();
		m_pStopTimer = NULL;
		//printf("MMFPlayer Info: FinalClosing: Canceled the Stop Timer.\n");
	}
	if (m_pTimerCancelKey != NULL) {
		m_pTimerCancelKey->Release();
		m_pTimerCancelKey = NULL;
	}
	if (pStopState != NULL) {
		delete(pStopState);
		pStopState = NULL;
		//printf("MMFPlayer Info: FinalClosing: Deleted the Stop State.\n");
	}
	if (m_pEndTimer != NULL) {
		m_pEndTimer->Release();
		m_pEndTimer = NULL;
	}
	if (pEndState != NULL) {
		delete(pEndState);
		pEndState = NULL;
	}

	// release pointers
	if (m_pTopology != NULL) {
		m_pTopology->Release();
		m_pTopology = NULL;
		//printf("MMFPlayer Info: FinalClosing: Released the Topology.\n");
	}
	if(m_pRate != NULL) {
		m_pRate->Release();
		m_pRate = NULL;
		//printf("MMFPlayer Info: FinalClosing: Released the Rate Control.\n");
	}
	if (m_pRateSupport != NULL) {
		m_pRateSupport->Release();
		m_pRateSupport = NULL;
		//printf("MMFPlayer Info: FinalClosing: Released the Rate Support.\n");
	}
	if (m_pClock != NULL) {
		m_pClock->Release();
		m_pClock = NULL;
		//printf("MMFPlayer Info: FinalClosing: Released the Clock.\n");
	}
	//if (m_pVolume != NULL) {
	//	m_pVolume->Release();
	//	m_pVolume = NULL;
	//	//printf("MMFPlayer Info: FinalClosing: Released the Volume.\n");
	//}
	if (m_pStreamVolume != NULL) {
		m_pStreamVolume->Release();
		m_pStreamVolume = NULL;
	}
	
	m_hwndVideo = NULL;
	//printf("MMFPlayer Info: FinalClosing: Set the Video Handle NULL.\n");
	delete (pendingAction);
	delete (cachedAction);
//	printf("MMFPlayer Info: FinalClosing: Deleted the PendingAction.\n");
	//hr = MFShutdown();
	//if (FAILED(hr) && MMFUtil::JMMF_DEBUG) {
	//	printf("MMFPlayer Error: FinalClosing: Failed to shutdown MF.\n");
	//}

	// temp ??
	//CloseHandle(m_closeCompleteEvent);

//	printf("MMFPlayer Info: FinalClosing: Closed the Close Event.\n");
	DeleteCriticalSection(&m_criticalSection);
//	printf("MMFPlayer Info: FinalClosing: Deleted the Critical Section State.\n");
	//CoUninitialize();

	return hr;
}


	//HRESULT setVideoWindowPos(long, long, long, long);
HRESULT MMFPlayer::setVideoSourcePos(float x, float y, float w, float h) {
	if (m_pVideoDisplay == NULL) {
		if (MMFUtil::JMMF_DEBUG) {
			printf("MMFPlayer Warning: setVideoSourcePos: There is no video display.\n");
		}
		return E_POINTER;
	}
	MFVideoNormalizedRect normRect;
	normRect.left = x;
	normRect.top = y;
	normRect.right = w;
	normRect.bottom = h;
	HRESULT hr = S_OK; 
	hr = m_pVideoDisplay->SetVideoPosition(&normRect, NULL);
	if (FAILED(hr)) {
		printf("MMFPlayer Error: setVideoSourcePos: Failed to set the video source rectangle.\n");
	} else {
		if (MMFUtil::JMMF_DEBUG) {
			printf("MMFPlayer Info: setVideoSourcePos: Successfully set the destination rectangle to %f, %f, %f, %f.\n", x, y, w, h);
		}
	}
	return hr;
}

HRESULT MMFPlayer::setVideoDestinationPos(long x, long y, long width, long height) {
	if (m_pVideoDisplay == NULL) {
		tempW = width; // cache
		tempH = height;
		if (MMFUtil::JMMF_DEBUG) {
			printf("MMFPlayer Warning: setVideoDestinationPos: There is no video display.\n");
		}
		return E_POINTER;
	}
	
	RECT rect;
	rect.left = x;
	rect.top = y;
	rect.right = width;
	rect.bottom = height;
	//HRESULT hr = S_OK;
	HRESULT hr = m_pVideoDisplay->SetVideoPosition(NULL, (LPRECT) &rect);

	if (FAILED(hr)) {
		printf("MMFPlayer Error: setVideoDestinationPos: Failed to set the video destination rectangle.\n");
	} else {
		if (MMFUtil::JMMF_DEBUG) {
			printf("MMFPlayer Info: setVideoDestinationPos: Successfully set the destination rectangle to %d, %d, %d, %d.\n", x, y, width, height);
		}
	}
	return hr;
}

HRESULT MMFPlayer::setVideoSourceAndDestPos(float sx, float sy, float sw, float sh, long x, long y, long width, long height) {
	if (m_pVideoDisplay == NULL) {
		tempW = width; // cache
		tempH = height;
		if (MMFUtil::JMMF_DEBUG) {
			printf("MMFPlayer Warning: setVideoSourceAndDestPos: There is no video display.\n");
		}
		return E_POINTER;
	}
	HRESULT hr = S_OK;
	// source normalized rect
	MFVideoNormalizedRect normRect;
	normRect.left = sx;
	normRect.top = sy;
	normRect.right = sw;
	normRect.bottom = sh;
	// destination rect
	RECT rect;
	rect.left = x;
	rect.top = y;
	rect.right = width;
	rect.bottom = height;

	hr = m_pVideoDisplay->SetVideoPosition(&normRect, (LPRECT) &rect);

	if (FAILED(hr)) {
		printf("MMFPlayer Error: setVideoSourceAndDestPos: Failed to set the source and destination rectangle.\n");
	} else {
		if (MMFUtil::JMMF_DEBUG) {
			printf("MMFPlayer Info: setVideoSourceAndDestPos: Successfully set the source and destination rectangle to: %f, %f, %f, %f, %d, %d, %d, %d.\n", sx, sy, sw, sh, x, y, width, height);
		}
	}
	return hr;
}

	//HRESULT setVisible(long);
	//int getState(void);
/*
* Returns true if the player state = started.
*/
bool MMFPlayer::isPlaying() {
	return (m_state == PlayerState_Started || pendingAction->action == SetStateStarted);
}

/*
* Delegates to either the synchronous or the asynchronous version of setRate(). 
*/
void MMFPlayer::setRate(double rate) {
	if (synchronousMode) {
		setRateSynchronous(rate);
	} else {
		setRateA(rate);
	}
}

/*
* The default, asynchronous, implementation of setting the play back rate.
* Sets the rate of the player. Checks whether the rate is supported
* and tries "thinning" if that is the only way to achieve the requested rate.
* The player should be paused before changing the rate. 
*/
void MMFPlayer::setRateA(double rate) {
	if (m_pRate == NULL) {
		if (topoInited) {
			printf("MMFPlayer Error: setRate: Unable to set the playback rate; the Rate Control is null.\n");
		}
		return;
	}
	EnterCriticalSection(&m_criticalSection);

	BOOL thin = FALSE;
	float actRate;
	if (m_pRate->GetRate(&thin, &actRate) == rate) {// nothing changes. Check for pending changes?
		if (MMFUtil::JMMF_DEBUG) {
			printf("MMFPlayer Info: setRate: The playback rate is already as requested %f\n", rate);
		}
		LeaveCriticalSection(&m_criticalSection);
		return;
	}

	if (pendingAction->action != DoNothing) {
		// don't overwrite seek actions
		if (cachedAction->action == DoNothing || cachedAction->action == SetRate) {
			cachedAction->action = SetRate;
			cachedAction->rateValue = (float) rate;
		}
		LeaveCriticalSection(&m_criticalSection);
		return;
	}

	actRate = (float) rate;
	float nearRate;
	HRESULT hr;

	if (m_pRateSupport != NULL) {
		hr = m_pRateSupport->IsRateSupported(FALSE, actRate, &nearRate);
		if (FAILED(hr)) {
			if (MMFUtil::JMMF_DEBUG) {
				printf("MMFPlayer Info: setRate: Rate not supported without thinning, nearest rate %f.\n", nearRate);
			}
			hr = m_pRateSupport->IsRateSupported(TRUE, actRate, &nearRate);
			if (FAILED(hr)) {
				if (MMFUtil::JMMF_DEBUG) {
					printf("MMFPlayer Info: setRate: Rate not supported with thinning, nearest rate %f.\n", nearRate);
				}
				//rate not supported, try nearest
				actRate = nearRate;
			}
			thin = TRUE;
		}
	}
	if (MMFUtil::JMMF_DEBUG) {
		printf("MMFPlayer Info: setRate: Thinning of stream %i, setting actual rate to %f.\n", thin, actRate);
	}
	
	// before setting the rate check the player state
	if (m_state != PlayerState_Paused && m_state != PlayerState_Stopped && m_state != PlayerState_Ready) {
		m_pSession->Pause();
	}
	// test without thinning?
	//hr = m_pRate->SetRate(thin, actRate);
	hr = m_pRate->SetRate(FALSE, actRate);
	thinEnabled = thin;
	
	if (FAILED(hr)) {
		printf("MMFPlayer Error: setRate: Failed to set the playback rate %f.\n", actRate);
	} else {
		if (MMFUtil::JMMF_DEBUG) {
			printf("MMFPlayer Info: setRate: Succesfully set the playback rate to %f.\n", actRate);
		}
		userPlaybackRate = actRate;
	}

	pendingAction->action = SetRate;
	pendingAction->rateValue = actRate;
	// check in ProcessMediaEvent if the rate change succeeded
	LeaveCriticalSection(&m_criticalSection);
}

/*
* The synchronous variant of setting the play back rate.
*/
void MMFPlayer::setRateSynchronous(double rate) {
	if (m_pRate == NULL) {
		if (topoInited) {
			printf("MMFPlayer Error: setRateSynchronous: Unable to set the playback rate; the Rate Control is null.\n");
		}
		return;
	}
	//EnterCriticalSection(&m_criticalSection);// not needed in synchronous mode? Leads to a dead lock in synchronous mode in the context of ELAN.

	BOOL thin = FALSE;
	float actRate;
	if (m_pRate->GetRate(&thin, &actRate) == rate) {// nothing changes.
		if (MMFUtil::JMMF_DEBUG) {
			printf("MMFPlayer Info: setRateSynchronous: The playback rate is already as requested %f\n", rate);
		}
		//LeaveCriticalSection(&m_criticalSection);
		return;
	}

	actRate = (float) rate;
	float nearRate;
	HRESULT hr;

	if (m_pRateSupport != NULL) {
		hr = m_pRateSupport->IsRateSupported(FALSE, actRate, &nearRate);
		if (FAILED(hr)) {
			if (MMFUtil::JMMF_DEBUG) {
				printf("MMFPlayer Info: setRateSynchronous: Rate not supported without thinning, nearest rate %f.\n", nearRate);
			}
			hr = m_pRateSupport->IsRateSupported(TRUE, actRate, &nearRate);
			if (FAILED(hr)) {
				if (MMFUtil::JMMF_DEBUG) {
					printf("MMFPlayer Info: setRateSynchronous: Rate not supported with thinning, nearest rate %f.\n", nearRate);
				}
				//rate not supported, try nearest
				actRate = nearRate;
			}
			thin = TRUE;
		}
	}
	if (MMFUtil::JMMF_DEBUG) {
		printf("MMFPlayer Info: setRateSynchronous: Thinning of stream %i, setting actual rate to %f.\n", thin, actRate);
	}

	// before setting the rate check the player state
	if (m_state != PlayerState_Paused && m_state != PlayerState_Stopped && m_state != PlayerState_Ready) {
		hr = m_pSession->Pause();
		
		if (FAILED(hr)) {
			printf("MMFPlayer Warning: setRateSynchronous: Failed to pause the playback for changing the rate.\n");
		} else {
			if (MMFUtil::JMMF_DEBUG) {
				printf("MMFPlayer Info: setRateSynchronous: Paused the playback for changing the rate.\n");
			}
			
			HRESULT ehr = PullMediaEventsUntilEventTypeSynchronous(MESessionPaused);
			
			if (SUCCEEDED(ehr)) {
				printf("MMFPlayer Info: setRateSynchronous: Succesfully paused the playback for changing the rate.\n");
			} else {
				printf("MMFPlayer Warning: setRateSynchronous: Failed to pause the playback for changing the rate.\n");
			}
		}
	}
	// test without thinning?
	//hr = m_pRate->SetRate(thin, actRate);
	hr = m_pRate->SetRate(FALSE, actRate);
	thinEnabled = thin;
	
	if (FAILED(hr)) {
		printf("MMFPlayer Error: setRateSynchronous: Failed to set the playback rate %f.\n", actRate);
	} else {
		if (MMFUtil::JMMF_DEBUG) {
			printf("MMFPlayer Info: setRateSynchronous: Succesfully set the playback rate to %f.\n", actRate);
		}
		userPlaybackRate = actRate;

		HRESULT ehr = PullMediaEventsUntilEventTypeSynchronous(MESessionRateChanged);

		if (SUCCEEDED(ehr)) {
			// what to do if an error is returned
			if (MMFUtil::JMMF_DEBUG) {
				printf("MMFPlayer Info: setRateSynchronous: Setting the playback rate to %f completed succesfully.\n", actRate);
			}
		} else {
			printf("MMFPlayer Error: setRateSynchronous: Failed to complete setting the playback rate to %f.\n", actRate);
		}
	}

	//LeaveCriticalSection(&m_criticalSection);
}

void MMFPlayer::setRateCached(float rate) {
	if (m_pRate == NULL) {
		printf("MMFPlayer Error: setRateCached: Unable to set the playback rate; the Rate Control is null.\n");
		return;
	}
	if (MMFUtil::JMMF_DEBUG) {
		printf("MMFPlayer Info: setRateCached: Setting a cached rate %f.\n", rate);
	}
	HRESULT hr;
	float actRate = (float) rate;
	float nearRate;

	if (m_pRateSupport != NULL) {
		hr = m_pRateSupport->IsRateSupported(FALSE, actRate, &nearRate);
		if (FAILED(hr)) {
			if (MMFUtil::JMMF_DEBUG) {
				printf("MMFPlayer Info: setRateCached: Rate not supported without thinning, nearest rate %f.\n", nearRate);
			}
			hr = m_pRateSupport->IsRateSupported(TRUE, actRate, &nearRate);
			if (FAILED(hr)) {
				if (MMFUtil::JMMF_DEBUG) {
					printf("MMFPlayer Info: setRateCached: Rate not supported with thinning, nearest rate %f.\n", nearRate);
				}
				//rate not supported, try nearest
				actRate = nearRate;
			}
		}
	}

	hr = m_pRate->SetRate(FALSE, actRate);
	
	if (FAILED(hr)) {
		printf("MMFPlayer Error: setRateCached: Failed to set the playback rate %f.\n", rate);
	} else {
		if (MMFUtil::JMMF_DEBUG) {
			printf("MMFPlayer Info: setRateCached: Succesfully set the playback rate to %f.\n", rate);
		}
		userPlaybackRate = actRate;
	}
}

/*
* Returns the rate of the player. The "thinning" flag is ignored. 
*/
double MMFPlayer::getRate() {
	if (m_pRate == NULL) {
		printf("MMFPlayer Error: getRate: Unable to get the playback rate; the Rate Control is null.\n");
		return 1.0;
	}
	EnterCriticalSection(&m_criticalSection);//?? need to lock for getting the rate?
	HRESULT hr;
	//BOOL pfThin;// the thinning flag is ignored in this call
	float pflRate;
	hr = m_pRate->GetRate(NULL, &pflRate);
	if (FAILED(hr)) {
		printf("MMFPlayer Error: getRate: Failed to get the rate.\n");
		pflRate = 1.0;
		//return 1.0;
	}
	LeaveCriticalSection(&m_criticalSection);
	return pflRate;
}

/*
* Sets the volume, a value between 0.0 and 1.0
*/
void MMFPlayer::setVolume(float volume) {
	/*
	if (m_pVolume == NULL) {
		if (topoInited) {
			printf("MMFPlayer Error: setVolume: Unable to set the volume, no volume control.\n");
		}
		// cache the value?
		initVolume = volume;
		return;
	}
	HRESULT hr = m_pVolume->SetMasterVolume(volume);
	*/
	if (m_pStreamVolume == NULL) {
		if (topoInited) {
			printf("MMFPlayer Error: setVolume: Unable to set the audio stream volume, no audio stream volume control.\n");
		}
		// cache the value?
		initVolume = volume;
		return;
	}

	UINT32 chCount;
	HRESULT hr = m_pStreamVolume->GetChannelCount(&chCount);
	if (FAILED(hr)) {
		printf("MMFPlayer Error: setVolume: Failed to get the number of audio channels.\n");
	} else {
		float *vols = new float[chCount];
		for (UINT32 i = 0; i < chCount; i++) {
			vols[i] = volume;
		}
		hr = m_pStreamVolume->SetAllVolumes(chCount, vols);
		delete vols;
	}
	
	if (FAILED(hr)) {
		printf("MMFPlayer Error: setVolume: Failed to set the volume to: %f\n", volume);
	}
}


float MMFPlayer::getVolume() {
	/*
	if (m_pVolume == NULL) {
		if (topoInited) {
			printf("MMFPlayer Error: getVolume: Unable to get the volume, no volume control.\n");
		}
		return 1;
	}
	float volume = 1;
	HRESULT hr = m_pVolume->GetMasterVolume(&volume);
	if (FAILED(hr)) {
		printf("MMFPlayer Error: getVolume: Failed to get the volume level.\n");
	}
	return volume;
	*/
	if (m_pStreamVolume == NULL) {
		if (topoInited) {
			printf("MMFPlayer Error: setVolume: Unable to set the audio stream volume, no audio stream volume control.\n");
		}
		return 1;
	}
	
	UINT32 chCount;
	HRESULT hr = m_pStreamVolume->GetChannelCount(&chCount);

	if (FAILED(hr)) {
		printf("MMFPlayer Error: getVolume: Failed to get the number of audio channels.\n");
		return 1;
	}

	float *vols = new float[chCount];
	// initialize the floats
	for (UINT32 i = 0; i < chCount; i++) {
		vols[i] = 1;
	}
	hr = m_pStreamVolume->GetAllVolumes(chCount, vols);

	if (FAILED(hr)) {
		printf("MMFPlayer Error: getVolume: Failed to get the audio stream volume levels.\n");
		return 1;
	}
	// currently return the first channels level
	float chzero = vols[0];
	delete vols;

	return chzero;
}	
	//void setBalance(long);
	//long getBalance(void);

/*
* Delegates to either the synchronous or the asynchronous version of setMediaPosition()
*/
void MMFPlayer::setMediaPosition(__int64 medPosition) {
	if (synchronousMode) {
		setMediaPositionSynchronous(medPosition);
	} else {
		setMediaPositionA(medPosition);
	}
}

/*
* The default, asynchronous, implementation of set media position.
* Sets the position of the media playhead.
* In Media Foundation the only way to do this seems via the Start function.
* Therefore if the player is paused, the rate should be set to 0 in order
* not to start the player.
*/
void MMFPlayer::setMediaPositionA(__int64 medPosition) {
	if (m_pSession == NULL || !topoInited) {
		if (topoInited) {
			printf("MMFPlayer Warning: setMediaPosition: Unable to set media position, no media session.\n");
		}
		initMediaTime = medPosition;
		return;
	}
	if (MMFUtil::JMMF_DEBUG) {
		printf("MMFPlayer Info: setMediaPosition: Request to set the media position to %lld.\n", medPosition);
	}
	if (medPosition < 0) {
		printf("MMFPlayer Warning: setMediaPosition: Unable to set the media position, position < 0: %lld\n", medPosition);
		return;
	} else if (medPosition > duration) {
		printf("MMFPlayer Warning: setMediaPosition: Unable to set the media position, position > duration: %lld\n", medPosition);
		return;
	}

	// critical section
	EnterCriticalSection(&m_criticalSection);
	// what to do if the player is already seeking? return, ignore this request or have a queue of set position requests?
	if (pendingAction->action == SetMediaPosition) {
		cachedAction->action = SetMediaPosition;
		cachedAction->timeValue = medPosition;
		if (MMFUtil::JMMF_DEBUG) {
			printf("MMFPlayer Info: setMediaPosition: Caching the request to set the media position to %lld.\n", medPosition);
		}
		LeaveCriticalSection(&m_criticalSection);	
		return;
	}

	const MFTIME plPosTime(medPosition);
	PROPVARIANT varStart;
	PropVariantInit(&varStart);
    varStart.vt = VT_I8;
    varStart.hVal.QuadPart = plPosTime;

	HRESULT hr;
	if (m_state == PlayerState_Started) {
		m_state = PlayerState_SeekingPosition;
		pendingAction->action = SetMediaPosition;
		pendingAction->timeValue = medPosition;

		//hr = m_pSession->Start(NULL, &varStart);
		// first pause
		hr = m_pSession->Pause();
		if (MMFUtil::JMMF_DEBUG) {
			printf("MMFPlayer Info: setMediaPosition: Successfully paused a started player before setting the position.\n");
		}
		if (FAILED(hr)) {
			printf("MMFPlayer Error: setMediaPosition: Failed to pause the started player: %lld.\n", medPosition);
		}
		if (m_pRate != NULL) {
			// setRate
			hr = m_pRate->SetRate(FALSE, 0);// zero means scrubbing, single frame
			if (SUCCEEDED(hr)) {
				// scrub
				hr = m_pSession->Start(NULL, &varStart);
			}
		}
	} else {
		if (m_pRate != NULL) {
			// check rate
			float curRate;
			//BOOL thinned;
			hr = m_pRate->GetRate(FALSE, &curRate);
			// check result
			if (curRate != 0) {
				hr = m_pRate->SetRate(FALSE, 0);// zero means scrubbing, single frame
				if (FAILED(hr)) {
					printf("MMFPlayer Error: setMediaPosition: Failed to set the rate to 0 (scrubbing).\n");
				}
				m_state = PlayerState_SeekingPosition;
				pendingAction->action = SetMediaPosition;
				pendingAction->timeValue = medPosition;
				hr = m_pSession->Start(NULL, &varStart);
				if (FAILED(hr) && MMFUtil::JMMF_DEBUG) {
					printf("MMFPlayer Error: setMediaPosition: Failed to set the media position of a paused player.\n");
				}
		
			} else {// rate == 0
				m_state = PlayerState_SeekingPosition;
				pendingAction->action = SetMediaPosition;
				pendingAction->timeValue = medPosition;
				hr = m_pSession->Start(NULL, &varStart);
				if (FAILED(hr) && MMFUtil::JMMF_DEBUG) {
					printf("MMFPlayer Error: setMediaPosition: Failed to set the media position of a paused player.\n");
				}
			}
		} else {// what else? just set time?
			if (MMFUtil::JMMF_DEBUG) {
				printf("MMFPlayer Info: setMediaPosition: Setting the media position of a paused player.\n");
			}
			m_state = PlayerState_SeekingPosition;
			pendingAction->action = SetMediaPosition;
			pendingAction->timeValue = medPosition;
			hr = m_pSession->Start(NULL, &varStart);
		}
	}
	PropVariantClear(&varStart);
	// leave critical section
	LeaveCriticalSection(&m_criticalSection);
}

/*
* The synchronous variant of set media position.
*/
void MMFPlayer::setMediaPositionSynchronous(__int64 medPosition) {
	if (m_pSession == NULL || !topoInited) {
		if (topoInited) {
			printf("MMFPlayer Warning: setMediaPositionSynchronous: Unable to set media position, no media session.\n");
		}
		initMediaTime = medPosition;
		return;
	}
	if (MMFUtil::JMMF_DEBUG) {
		printf("MMFPlayer Info: setMediaPositionSynchronous: Request to set the media position to %lld.\n", medPosition);
		// temp
		printf("MMFPlayer Info: setMediaPositionSynchronous: Player state is %d.\n", m_state);
	}
	if (medPosition < 0) {
		printf("MMFPlayer Warning: setMediaPositionSynchronous: Unable to set the media position, position < 0: %lld\n", medPosition);
		return;
	} else if (medPosition > duration) {
		printf("MMFPlayer Warning: setMediaPositionSynchronous: Unable to set the media position, position > duration: %lld\n", medPosition);
		return;
	}
	// critical section
	EnterCriticalSection(&m_criticalSection);

	const MFTIME plPosTime(medPosition);
	PROPVARIANT varStart;
	PropVariantInit(&varStart);
    varStart.vt = VT_I8;
    varStart.hVal.QuadPart = plPosTime;

	HRESULT hr;
	if (m_state == PlayerState_Started) {
		//m_state = PlayerState_SeekingPosition;
		// first pause

		hr = m_pSession->Pause();
		
		if (SUCCEEDED(hr)) {
			HRESULT ehr = PullMediaEventsUntilEventTypeSynchronous(MESessionPaused);
			// ignore the return value?
			if (SUCCEEDED(ehr)) {
				// ignore the return value?
				if (MMFUtil::JMMF_DEBUG) {
					printf("MMFPlayer Info: setMediaPositionSynchronous:  Successfully paused a started player before setting the media position.\n");
				}
			} else {
				printf("MMFPlayer Error: setMediaPositionSynchronous: Failed to pause the player before setting the media position.\n");
			}

		} else {
			printf("MMFPlayer Error: setMediaPositionSynchronous: Failed to pause the started player: %lld.\n", medPosition);
		}
	} 
	
	if (m_pRate != NULL) {
		// check rate
		float curRate;
		//BOOL thinned;
		hr = m_pRate->GetRate(FALSE, &curRate);
		// check result
		if (curRate != 0) {
			// setRate
			hr = m_pRate->SetRate(FALSE, 0);// zero means scrubbing, single frame

			if (SUCCEEDED(hr)) {
				HRESULT ehr = PullMediaEventsUntilEventTypeSynchronous(MESessionRateChanged);
				if (SUCCEEDED(ehr)) {
					// ignore the return value?
					if (MMFUtil::JMMF_DEBUG) {
						printf("MMFPlayer Info: setMediaPositionSynchronous: Successfully set the rate to 0.\n");
					}
				} else {
					printf("MMFPlayer Error: setMediaPositionSynchronous: Failed to set the rate to 0.\n");
				}
			}
		}
	}

	// scrub sample
	hr = m_pSession->Start(NULL, &varStart);

	if (SUCCEEDED(hr)) {
		HRESULT ehr = PullMediaEventsUntilEventTypeSynchronous(MESessionScrubSampleComplete);
		if (SUCCEEDED(ehr)) {
			// ignore the return value?
			if (MMFUtil::JMMF_DEBUG) {
				printf("MMFPlayer Info: setMediaPositionSynchronous: Successfully scrubbed one sample.\n");
			}
		} else {
			printf("MMFPlayer Error: setMediaPositionSynchronous: Failed to scrub a single sample.\n");
		}
	}

	PropVariantClear(&varStart);
	// leave critical section
	LeaveCriticalSection(&m_criticalSection);
}

/*
* Sets the cached media position without checking the rate and paused state etc.
*/
void MMFPlayer::setMediaPositionCached(__int64 medPosition) {
	if (m_pSession == NULL) {
		printf("MMFPlayer Warning: setMediaPositionCached: Unable to set media position, no media session.\n");
		return;
	}
	if (MMFUtil::JMMF_DEBUG) {
		printf("MMFPlayer Info: setMediaPositionCached: Setting the media position of a paused player %lld.\n", medPosition);
	}

	const MFTIME plPosTime(medPosition);
	PROPVARIANT varStart;
	PropVariantInit(&varStart);
    varStart.vt = VT_I8;
    varStart.hVal.QuadPart = plPosTime;
	m_state = PlayerState_SeekingPosition;

	HRESULT hr = m_pSession->Start(NULL, &varStart);
	if (FAILED(hr) && MMFUtil::JMMF_DEBUG) {
		printf("MMFPlayer Error: setMediaPositionCached: Failed to set the media position of a paused player.\n");
	}

	pendingAction->action = SetMediaPosition;
	pendingAction->timeValue = medPosition;
	PropVariantClear(&varStart);
}

/*
* Returns the current media position (if there is a presentation clock).
*/
__int64 MMFPlayer::getMediaPosition() {
	if (m_pClock != NULL) {
		//EnterCriticalSection(&m_criticalSection);// don't need the lock to get the time?

		MFTIME curTime;
		HRESULT hr = m_pClock->GetTime(&curTime);
		if (SUCCEEDED(hr)) {
			//LeaveCriticalSection(&m_criticalSection);
			//if (MMFUtil::JMMF_DEBUG) {
			//	printf("MMFPlayer Info: getMediaPosition: Successfully got the media time.\n");
			//}
			return curTime;
		} else {
			if (MMFUtil::JMMF_DEBUG && topoInited) {
				printf("MMFPlayer Error: getMediaPosition: Failed to get the media time.\n");
			}
		}

		//LeaveCriticalSection(&m_criticalSection);
	} else {
		if (MMFUtil::JMMF_DEBUG && topoInited) {
			printf("MMFPlayer Error: getMediaPosition: Unable to get the media time, no Clock.\n");
		}
	}

	return 0;
}

/**
* Returns the duration.
* ToDo: check if the duration is always reliable/available before the topology is fully resolved.
*/
__int64 MMFPlayer::getDuration() {
	//if (duration == 0) {
		// get the duration after initialization
	//}
	return duration;
}

/*
* Sets the current stop time. 
* Implemented by setting a timer that calls invoke on a callback object at the specified time.
* Updating the timer has to be done AFTER starting the player.
*
* Setting an attribute on each output topology node is not suited:  
* - it can be done before the media session first starts
* - it can be done after the session started, but with a limit of roughly 7 minutes.
*/
HRESULT MMFPlayer::setStopPosition(__int64 stopPosition) {
	if (stopPosition <= duration) {
		stopTime = stopPosition;
	} else {
		stopTime = duration;
	}
	// check the player state?
	HRESULT hr = S_OK;
	if (!synchronousMode) {
		if (m_pStopTimer != NULL && m_pTimerCancelKey != NULL) {
			//printf("MMFPlayer Info: setStopTime: the timer cancel key is not NULL.\n");
			hr = m_pStopTimer->CancelTimer(m_pTimerCancelKey);

			if (FAILED(hr)) {
				printf("MMFPlayer Error: setStopTime: failed to cancel the timer.\n");
			} else {
				if (MMFUtil::JMMF_DEBUG) {
					printf("MMFPlayer Info: setStopTime: successfully cancelled the timer.\n");
				}
			}
		}
	}
	//if (m_pStopTimer != NULL) {
	//	hr = m_pStopTimer->SetTimer(0, stopTime, this, NULL, &m_pTimerCancelKey);
	//	if (FAILED(hr)) {
	//		printf("MMFPlayer Error: setStopTime: failed to set the stop time for the timer.\n");
	//	} else {
	//		printf("MMFPlayer Info: setStopTime: successfully set the stop time for the timer %d.\n", stopTime);
	//	}
	//}
	return hr;
}

/*
* Returns the current stop time.
*/
__int64 MMFPlayer::getStopPosition() {
	return stopTime;
}

/*
* Returns the time per frame (in case of video) in seconds.
*/
double MMFPlayer::getTimePerFrame() {
	if (isVideo) {
		if (frameRateNumerator != 0 && frameRateDenominator != 0) {
			if (MMFUtil::JMMF_DEBUG) {
				printf("MMFPlayer Info: getTimePerFrame: Frame rate is %f\n", (frameRateDenominator / (double) frameRateNumerator));
			}
			return frameRateDenominator / (double) frameRateNumerator;
		} else {
			printf("MMFPlayer Error: getTimePerFrame: Frame rate numerator or denominator is 0.\n");
		}
	}
	return 0.0;
}

long MMFPlayer::getOrgVideoWidth() {
	if (m_pVideoDisplay != NULL) {
		SIZE origSize;
		SIZE origAR;
		HRESULT hr = m_pVideoDisplay->GetNativeVideoSize(&origSize, &origAR);
		if (SUCCEEDED(hr)) {
			if (MMFUtil::JMMF_DEBUG) {
				printf("MMFPlayer Info: getOrgVideoWidth: Original w * h and ar_x * ar_y: %d * %d, %d * %d\n", origSize.cx, origSize.cy, origAR.cx, origAR.cy);
			}
			return origSize.cx;
		} else {
			printf("MMFPlayer Error: getOrgVideoWidth: Failed to get the original video width.\n");
		}
	} else {
		if (topoInited) {
			printf("MMFPlayer Error: getOrgVideoWidth: Unable to get the original video width, no Video Display Control.\n");
		}
	}
	return 0;
}

long MMFPlayer::getOrgVideoHeight() {
	if (m_pVideoDisplay != NULL) {
		SIZE origSize;
		SIZE origAR;
		HRESULT hr = m_pVideoDisplay->GetNativeVideoSize(&origSize, &origAR);
		if (SUCCEEDED(hr)) {
			if (MMFUtil::JMMF_DEBUG) {
				printf("MMFPlayer Info: getOrgVideoHeight: Original w * h and ar_x * ar_y: %d * %d, %d * %d\n", origSize.cx, origSize.cy, origAR.cx, origAR.cy);
			}
			return origSize.cy;
		} else {
			printf("MMFPlayer Error: getOrgVideoHeight: Failed to get the original video height.\n");
		}
	} else {
		if (topoInited) {
			printf("MMFPlayer Error: getOrgVideoHeight: Unable to get the original video height, no Video Display Control.\n");
		}
	}
	return 0;
}

/*
* Returns the position of the video destination rect relative to the clipping window.
* x and y can be negative.
*/
HRESULT MMFPlayer::getVideoDestinationPos(long *x, long *y, long *w, long *h) {
	HRESULT hr = S_OK;
	if (m_pVideoDisplay != NULL) {
		MFVideoNormalizedRect normRect;
		RECT rect;
		hr = m_pVideoDisplay->GetVideoPosition(&normRect, (LPRECT) &rect);
		if (SUCCEEDED(hr)) {
			if (MMFUtil::JMMF_DEBUG) {
				printf("MMFPlayer Info: getVideoDestinationPos: Video destination position is: %d, %d, %d, %d\n", rect.left, rect.top, rect.right, rect.bottom);
			}
			*x = rect.left;
			*y = rect.top;
			*w = rect.right;
			*h = rect.bottom;
		} else {
			printf("MMFPlayer Error: getVideoDestinationPos: Failed to get the video destination position.\n");
		}
	}  else {
		printf("MMFPlayer Error: getVideoDestinationPos: Unable to get the video destination position, no Video Display Control.\n");
	}
	return hr;
}

HRESULT MMFPlayer::getPreferredAspectRatio(long *width, long *height) {
	if (m_pVideoDisplay != NULL) {
		SIZE origSize;
		SIZE origAR;
		HRESULT hr = m_pVideoDisplay->GetNativeVideoSize(&origSize, &origAR);
		if (SUCCEEDED(hr)) {
			if (MMFUtil::JMMF_DEBUG) {
				printf("MMFPlayer Info: getPreferredAspectRatio: Original w * h and ar_x * ar_y: %d * %d, %d * %d\n", origSize.cx, origSize.cy, origAR.cx, origAR.cy);
			}
			*width = origAR.cx;
			*height = origAR.cy;
		} else {
			printf("MMFPlayer Error: getPreferredAspectRatio: Failed to get the preferred aspect ratio.\n");
		}
		return hr;
	} else {
		if (topoInited) {
			printf("MMFPlayer Error: getPreferredAspectRatio: Unable to get the aspect ratio, no Video Display Control.\n");
		}
	}
	return E_POINTER;
}

/*
* Returns the current image.
*/
HRESULT MMFPlayer::getCurrentImage(BITMAPINFOHEADER *pBih, BYTE **pDib, DWORD *pcbDib) {
	if (m_pVideoDisplay == NULL) {
		printf("MMFPlayer Error: getCurrentImage: Unable to get the image, no Video Display Control.\n");
		return E_POINTER;
	}
	__int64 curPos = getMediaPosition();
	
	HRESULT hr = m_pVideoDisplay->GetCurrentImage(pBih, pDib, pcbDib, &curPos);

	if(SUCCEEDED(hr)) {
		if (MMFUtil::JMMF_DEBUG) {
			printf("MMFPlayer Info: getCurrentImage: Successfully extracted the current video image.\n");
		}
	} else {
		printf("MMFPlayer Error: getCurrentImage: Could not retrieve the current video image: %d (%x).\n", hr, hr);
	}

	return hr;
}

HRESULT MMFPlayer::getOwnerWindow(HWND *hwnd) {
	// just return m_hwndVideo ?? or get it from the video display control
	if (m_hwndVideo == NULL) {
		return E_POINTER;
	} else {
		*hwnd = m_hwndVideo;
		return S_OK;
	}
}

/*
* Tries to repaint the video if there is a video display object.
* 
*/
void MMFPlayer::repaintVideo() {
	if (m_pVideoDisplay != NULL) {
		m_pVideoDisplay->RepaintVideo();
		//if (MMFUtil::JMMF_DEBUG) {
		//	printf("MMFPlayer Info: repaintVideo: Repainted the video.\n");
		//}
	}
}

/*
* Returns true if the mode is synchronous, false otherwise 
*/
bool MMFPlayer::isSynchronousMode() {
	return synchronousMode;
}