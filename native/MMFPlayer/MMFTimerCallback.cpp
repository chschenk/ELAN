#include "MMFTimerCallback.h"
#include <comdef.h>

/*
* Constructor.
* A MMFTimerCallback is connected to and managed by a MMFPlayer.
* A note on callback to the Java world: a JNIEnv object is only valid 
* in the Thread it is attached to. IMFMediaSession->Start() is asynchronous
* and a callback via the IMFTimer is always performed on another thread then the one 
* that calls Start(). It seems therefore not possible to call a method in the Java 
* class after e.g. a stop callback of the session.
*/
MMFTimerCallback::MMFTimerCallback() {
	m_nRefCount = 1;
	m_pPlayer = NULL;
}

/*
* Destructor.
*/
MMFTimerCallback::~MMFTimerCallback() {
	m_pPlayer = NULL;
}

// IUnknown methods

HRESULT MMFTimerCallback::QueryInterface(REFIID riid, void** ppv) {
	printf("MMFTimerCallback Info: QueryInterface called.\n");
    //return S_OK;
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

ULONG MMFTimerCallback::AddRef() {
	//printf("MMFTimerCallback Info: AddRef:...\n");
    return InterlockedIncrement(&m_nRefCount);
}

ULONG MMFTimerCallback::Release() {
	//printf("MMFTimerCallback Info: Release:...\n");
    ULONG uCount = InterlockedDecrement(&m_nRefCount);
    if (uCount == 0) {
		//printf("MMFTimerCallback Info: Release: Deleting callback.\n");
		m_pPlayer = NULL;
        //delete this;// do not delete
    }
    return uCount;
}

/*
* Callback for IMFTimer events.
* Currently only used for signalling the stop time. 
* Because sometimes Invoke is called when the player is started, a check is performed
* to see if invoke is called close to the stop time.
*
*  pAsyncResult: Pointer to the result, ignored.
*/

HRESULT MMFTimerCallback::Invoke(IMFAsyncResult *pResult) {
	if (pResult == NULL) {
		return E_POINTER;//??
	}
	if (m_pPlayer == NULL) {
		return E_POINTER;
	}

	printf("MMFTimerCallback Info: Invoke: Timer callback.\n");
	HRESULT hr = S_OK;
	__int64 curTime;
	curTime = m_pPlayer->getMediaPosition();
	printf("MMFTimerCallback Info: Invoke: Timer callback called at %lld.\n", curTime);
	__int64 stopTime=m_pPlayer->getStopPosition();
	__int64 diff = stopTime-curTime;
	if (diff > -1000000 && diff < 1000000) {
		hr = m_pPlayer->pause();
		//m_pPlayer->setMediaPosition(stopTime);
	} else {
		printf("MMFTimerCallback Warning: Invoke: false callback, wrong time: %lld.\n", curTime);
	}

	return hr;
}

/*
* Sets the player for callback.
*/
HRESULT MMFTimerCallback::SetPlayer(MMFPlayer *player) {
	if (player == NULL) {
		m_pPlayer = NULL;
	} else {
		m_pPlayer = player;
	}
	return S_OK;
}