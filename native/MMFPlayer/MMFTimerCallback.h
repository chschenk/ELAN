#include <mfapi.h>
#include <mfidl.h>
#include "MMFPlayer.h"

#ifndef included_MMFTimerCallback
#define included_MMFTimerCallback

class MMFTimerCallback : public IMFAsyncCallback {

public:
	MMFTimerCallback();
	~MMFTimerCallback();

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

	STDMETHODIMP SetPlayer(MMFPlayer*);

private:
	volatile long    m_nRefCount;        // Reference count.
	MMFPlayer		*m_pPlayer;

};

#endif