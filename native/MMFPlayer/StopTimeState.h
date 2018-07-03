#include <mfapi.h>

#ifndef included_StopTimeSate
#define included_StopTimeSate

class StopTimeState : public IUnknown {
public:
	StopTimeState();
	~StopTimeState();

	// IUnknown methods
    STDMETHODIMP QueryInterface(REFIID iid, void** ppv);
    STDMETHODIMP_(ULONG) AddRef();
    STDMETHODIMP_(ULONG) Release();

};
#endif