/* 
 * Project:	JMMFPlayer, Microsoft Media Foundation Player for Java
 * Author:	Han Sloetjes
 * Version:	1.0, March 2012
 */
#include "StopTimeState.h"
#include <mfapi.h>
#include <mfidl.h>
#include <evr.h>
#include <list>
using namespace std;

#ifndef included_MMFPlayer
#define included_MMFPlayer

enum PlayerState
{
    PlayerState_NoSession = 0,     // No session.
    PlayerState_Ready,          // Session was created, ready to open a file.
    PlayerState_Opening,    // Session is opening a file.
    PlayerState_Started,        // Session is playing a file.
    PlayerState_Paused,         // Session is paused.
	PlayerState_SeekingPosition,// Session is seeking.
    PlayerState_Stopped,        // Session is stopped (ready to play).
    PlayerState_Closing,         // Application has closed the session, but is waiting for MESessionClosed
    PlayerState_Closed        // MESessionClosed.
};

enum SessionActions {
	SetStateStarted = 1000,
	SetStatePaused = 1001,
	SetStateStopped = 1002,
	SetStateClosing = 1004,
	SetStateClosed = 1005,
	SetMediaPosition = 1006,
	SetRate = 1007,
	DoNothing = 2000
};

/*
* Struct for pending actions, to be performed after the async media event has been received 
*/
struct PendingAction {
	int action;// one of the SessionActions
	__int64 timeValue;
	double rateValue;
};

class MMFPlayer : public IMFAsyncCallback {

public:
	MMFPlayer();
	MMFPlayer(bool synchronous);
	MMFPlayer(const wchar_t *path);
	MMFPlayer(const wchar_t *path, bool synchronous);
	~MMFPlayer();

	// IUnknown methods
    STDMETHODIMP QueryInterface(REFIID iid, void** ppv);
    STDMETHODIMP_(ULONG) AddRef();
    STDMETHODIMP_(ULONG) Release();

	// IMFAsyncCallback methods
    STDMETHODIMP  GetParameters(DWORD*, DWORD*)
    {
        // Implementation of this method is optional.
        return E_NOTIMPL;
    }
    STDMETHODIMP  Invoke(IMFAsyncResult* pAsyncResult);

	HRESULT initSession(const wchar_t *, HWND);
	// initializes a session; in case of audio only the session/topology will be
	// resolved, in case of video the topology will partially be build and 
	// resolved once a window handle is available (for the time being, 
	// until it is clear whether it is possible to init a video display object without a HWND)
	HRESULT initSessionWithFile(const wchar_t *);
	HRESULT setOwnerWindow(HWND);
	HRESULT setMediaFile(const wchar_t *);
	HRESULT start(void);
	HRESULT stop(void);
	HRESULT pause(void);
	HRESULT cleanUpOnClose(void);
	HRESULT CloseSession(void);
	bool isVisualMedia(void);
	int getPlayerState(void);
	
	//HRESULT setVideoWindowPos(long, long, long, long);
	HRESULT setVideoSourcePos(float, float, float, float);
	HRESULT setVideoDestinationPos(long, long, long, long);
	HRESULT setVideoSourceAndDestPos(float, float, float, float, long, long, long, long);
	//HRESULT setVisible(long);
	//int getState(void);
	bool isPlaying(void);
	void setRate(double);
	double getRate(void);
	void setVolume(float);
	float getVolume(void);
	//void setBalance(long);
	//long getBalance(void);
	void setMediaPosition(__int64);
	__int64 getMediaPosition(void);
	__int64 getDuration(void);
	HRESULT setStopPosition(__int64);
	__int64 getStopPosition(void);
	double getTimePerFrame(void);
	long getOrgVideoWidth(void);
	long getOrgVideoHeight(void);
	HRESULT getVideoDestinationPos(long *, long *, long *, long *);
	HRESULT getPreferredAspectRatio(long *, long *);
	HRESULT getCurrentImage(BITMAPINFOHEADER *, BYTE **, DWORD *);
	HRESULT getOwnerWindow(HWND *);
	void repaintVideo(void);
	bool isSynchronousMode(void);

protected:
	HRESULT CreateMediaSource(PCWSTR);
	HRESULT CreateTopologyFromSource();
	HRESULT AddBranchToPartialTopology(IMFPresentationDescriptor *, DWORD);
	void CheckMajorMediaType(IMFMediaSource *);
	HRESULT CreateSourceStreamNode(IMFPresentationDescriptor *, 
		IMFStreamDescriptor *, IMFTopologyNode **);
	HRESULT CreateOutputNode(IMFStreamDescriptor *, IMFTopologyNode **);
	// MF event handling functionality
    HRESULT ProcessMediaEvent(IMFMediaEvent *);
	HRESULT ProcessMediaEventSynchronous(IMFMediaEvent *);
	HRESULT PullMediaEventsUntilEventTypeSynchronous(MediaEventType);
    // Media event handlers
    HRESULT OnTopologyReady(void);
	HRESULT FinalClosing(void);
	HRESULT ProcessStopTimeEvent(void);
	HRESULT ProcessEndTimeEvent(void);
	void clearPendingAction(PendingAction *);

private:
	void setMediaPositionCached(__int64);
	void setRateCached(float); 
	// synchronous and asynchronous variants of methods
	HRESULT initSessionWithFileSynchronous(const wchar_t *);
	HRESULT initSessionWithFileA(const wchar_t *);
	HRESULT CreateAndSetTopologySynchronous();
	HRESULT startSynchronous(void);
	HRESULT startA(void);
	HRESULT stopSynchronous(void);
	HRESULT stopA(void);
	HRESULT pauseSynchronous(void);
	HRESULT pauseA(void);
	void setRateSynchronous(double);
	void setRateA(double);
	void setMediaPositionSynchronous(__int64);
	void setMediaPositionA(__int64);
	HRESULT CloseSessionSynchronous(void);
	HRESULT CloseSessionA(void);

	volatile long            m_nRefCount;        // Reference count.
	IMFMediaSession			*m_pSession;
	IMFMediaSource			*m_pSource;
	IMFTopology             *m_pTopology;
	HWND				     m_hwndVideo;
	IMFVideoDisplayControl  *m_pVideoDisplay;
	IMFRateControl          *m_pRate;
    IMFRateSupport          *m_pRateSupport;
    IMFPresentationClock    *m_pClock;
	//IMFSimpleAudioVolume    *m_pVolume;
	IMFAudioStreamVolume    *m_pStreamVolume; // for per channel volume
	IMFTimer				*m_pStopTimer; // for stop time of play selection
	IUnknown				*m_pTimerCancelKey; // for stop event of play selection
	StopTimeState			*pStopState; // for stop event of play selection
	IMFTimer				*m_pEndTimer; // for stop at end of media
	StopTimeState			*pEndState; // for end of media event

	bool isVideo;
	void initFields(void);
	wchar_t *mediaPath;
	__int64 duration;
	float userPlaybackRate;
	BOOL thinEnabled;
	__int64 stopTime; 
	unsigned int frameRateNumerator;
	unsigned int frameRateDenominator;
	bool cleanUpCalled;
	bool topoInited;
	long tempW;//cache width
	long tempH;// cache height
	float initVolume;
	__int64 initMediaTime;
	bool synchronousMode;

	CRITICAL_SECTION m_criticalSection;
	PlayerState m_state;            // Current state of the media session.
	PendingAction *pendingAction;
	PendingAction *cachedAction;

    HANDLE m_closeCompleteEvent;   // event fired when session colse is complete
};

// test actions and pending action structure



#endif