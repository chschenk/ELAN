/*
 * Project:	JDSPlayer, Direct Show Media Player for Java
 * Author:	Han Sloetjes
 * Version:	1.0, Oct. 2010
 */
#include <dshow.h>
#include <list>
using namespace std;

//#include "stdafx.h"
#ifndef included_DSPlayer
#define included_DSPlayer

class DSPlayer {
public:
	DSPlayer();
	DSPlayer(const wchar_t *path);
	DSPlayer(const wchar_t *path, const wchar_t *splitterCodec);
	~DSPlayer();
	IGraphBuilder *pGraph;
	HRESULT setMediaFile(const wchar_t *path);
	HRESULT setMediaFile(const wchar_t *path, const wchar_t *splitterCodec);
	HRESULT start(void);
	HRESULT stop(void);
	HRESULT pause(void);
	HRESULT initGraph(void);
	HRESULT setOwnerWindow(HWND);
	HRESULT setVideoWindowPos(long, long, long, long);
	HRESULT setVideoSourcePos(long, long, long, long);
	HRESULT setVideoDestinationPos(long, long, long, long);
	HRESULT setVisible(long);
	HRESULT cleanUpOnClose(void);
	int getState(void);
	bool isPlaying(void);
	void stopWhenReady(void);
	void setRate(double);
	double getRate(void);
	void setVolume(long);
	long getVolume(void);
	void setBalance(long);
	long getBalance(void);
	void setMediaPosition(__int64);
	__int64 getMediaPosition(void);
	__int64 getDuration(void);
	HRESULT setStopPosition(__int64);
	__int64 getStopPosition(void);
	double getTimePerFrame(void);
	bool isVisualMedia(void);
	long getOrgVideoWidth(void);
	long getOrgVideoHeight(void);
	HRESULT getVideoDestinationPos(long *, long *, long *, long *);
	HRESULT getPreferredAspectRatio(long *, long *);
	HRESULT getCurrentImage(long *, long *);

private:
	void initFields(void);
	HRESULT createDefaultMPEG1Graph(void);
	HRESULT createDefaultMPEG2Graph(void);
	HRESULT createDefaultMPEG2GraphPluginControl(void);
	HRESULT createVMR9MPEG1Graph(void);
	IPin *GetPin(IBaseFilter *pFilter, PIN_DIRECTION PinDir);
	void RenderAllOutputPins(IGraphBuilder *, IBaseFilter *);
	wchar_t *mediaPath;
	wchar_t *prefSplitter;
	bool cleanUpCalled;
	bool isVideo;
	
	ICaptureGraphBuilder2 *pCapGraph;
    IMediaControl *pControl;
	IMediaSeeking *pSeeking;
	IMediaPosition *pPosition;
	IMediaEvent *pEvent;
	IVideoWindow  *pVideoWindow;
	IBasicVideo   *pBasicVideo;
	IBasicAudio   *pBasicAudio;
	list<IBaseFilter *> filtList;

	IGraphConfig *pGraphConfig;
	IAMPluginControl *pPluginControl;
};

#endif