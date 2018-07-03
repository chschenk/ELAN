#include "StopTimeState.h"

StopTimeState::StopTimeState() {
}

StopTimeState::~StopTimeState() {
}

/*
* Minimum inplementation of an IUnknown interface,
* does not follow the standard IUnknown rules.
*/
ULONG StopTimeState::AddRef() {
	return 1;
}

/*
* Minimum inplementation of an IUnknown interface,
* does not follow the standard IUnknown rules.
*/
ULONG StopTimeState::Release() {
	return 1;
}

/*
* Minimum inplementation of an IUnknown interface,
* does not follow the standard IUnknown rules.
*/
HRESULT StopTimeState::QueryInterface(const IID &iid, void **ppv) {
	HRESULT hr = S_OK;

    if(ppv == NULL) {
        hr = E_POINTER;
    }
    else if(iid == __uuidof(IUnknown)) {
        *ppv = static_cast<IUnknown*>(this);
    }
    else {
        *ppv = NULL;
        hr = E_NOINTERFACE;
    }

    return hr;
}
