/*
 * Project:	JDSPlayer, Direct Show Media Player for Java
 * Author:	Han Sloetjes
 * Version:	1.0, Oct. 2010
 */

//#include "stdafx.h"// for now don't use precompiled headers
#include <jawt.h>
#include <jawt_md.h>
#include <dshow.h>
#include "nl_mpi_jds_JDSPlayer.h"
#include "DSPlayer.h"
#include "DSUtil.h"
#include "JNIUtil.h"
#include "PlayerMap.h"


static PlayerMap pm;

#ifdef __cplusplus
extern "C" {
#endif

/*
 * Class:     nl_mpi_jds_JDSPlayer
 * Method:    start
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_nl_mpi_jds_JDSPlayer_start
  (JNIEnv *env, jobject thisObj, jlong id) {
	//printf("Native start\n");
	if (pm.containsKey((long)id)) {
		DSPlayer* dsp = pm.get((long)id);

		HRESULT hr = dsp->start();
		if (FAILED(hr)) {
			printf("Native Start: could not start the media");
		}
	} else {
		printf("Native Start: Player not found, id %d\n", id);
	}
	fflush(stdout);
  }

/*
 * Class:     nl_mpi_jds_JDSPlayer
 * Method:    stop
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_nl_mpi_jds_JDSPlayer_stop
  (JNIEnv *env, jobject thisObj, jlong id) {
	//printf("Native stop\n");
	if (pm.containsKey((long)id)) {
		DSPlayer* dsp = pm.get((long)id);

		HRESULT hr = dsp->stop();
		if (FAILED(hr)) {
			printf("Native Stop: could not stop the media");
		}
	} else {
		printf("Native Stop: Player not found, id %d\n", id);
	}
	fflush(stdout);
  }

/*
 * Class:     nl_mpi_jds_JDSPlayer
 * Method:    pause
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_nl_mpi_jds_JDSPlayer_pause
	(JNIEnv *env, jobject thisObj, jlong id) {
	if (pm.containsKey((long)id)) {
		DSPlayer* dsp = pm.get((long)id);

		HRESULT hr = dsp->pause();
		if (FAILED(hr)) {
			printf("Native Pause: could not pause the media");
		}
	} else {
		printf("Native Pause: Player not found, id %d\n", id);
	}
	fflush(stdout);
}

/*
 * Class:     nl_mpi_jds_JDSPlayer
 * Method:    isPlaying
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_nl_mpi_jds_JDSPlayer_isPlaying
	(JNIEnv *env, jobject thisObj, jlong id) {
	if (pm.containsKey((long)id)) {
		DSPlayer* dsp = pm.get((long)id);

		if (dsp->isPlaying()) {
			fflush(stdout);
			return JNI_TRUE;
		}
	} else {
		printf("Native isPlaying: Player not found, id %d\n", id);
	}
	fflush(stdout);
	return JNI_FALSE;
}

/*
 * Class:     nl_mpi_jds_JDSPlayer
 * Method:    stopWhenReady
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_nl_mpi_jds_JDSPlayer_stopWhenReady
	(JNIEnv *env, jobject thisObj, jlong id) {
	if (pm.containsKey((long)id)) {
		DSPlayer* dsp = pm.get((long)id);

		dsp->stopWhenReady();
	}  else {
		printf("Native stopWhenReady: Player not found, id %d\n", id);
	}
	fflush(stdout);
}

/*
 * Class:     nl_mpi_jds_JDSPlayer
 * Method:    getState
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_nl_mpi_jds_JDSPlayer_getState
	(JNIEnv *env, jobject thisObj, jlong id) {
	if (pm.containsKey((long)id)) {
		DSPlayer* dsp = pm.get((long)id);

		fflush(stdout);
		return dsp->getState();
	} else {
		printf("Native getState: Player not found, id %d\n", id);
	}
	fflush(stdout);
	return 0;// stopped
}

/*
 * Class:     nl_mpi_jds_JDSPlayer
 * Method:    setRate
 * Signature: (JF)V
 */
JNIEXPORT void JNICALL Java_nl_mpi_jds_JDSPlayer_setRate
	(JNIEnv *env, jobject thisObj, jlong id, jfloat rate) {
	if (pm.containsKey((long)id)) {
		DSPlayer* dsp = pm.get((long)id);
		if (rate > 0.0) { 
			dsp->setRate((double) rate);
		} else {
			// hier throw an exception?
		}
	} else {
		printf("Native setRate: Player not found, id %d\n", id);
	}
	fflush(stdout);
}

/*
 * Class:     nl_mpi_jds_JDSPlayer
 * Method:    getRate
 * Signature: (J)F
 */
JNIEXPORT jfloat JNICALL Java_nl_mpi_jds_JDSPlayer_getRate
	(JNIEnv *env, jobject thisObj, jlong id) {
	if (pm.containsKey((long)id)) {
		DSPlayer* dsp = pm.get((long)id);
		return (jfloat) dsp->getRate();
	} else {
		printf("Native getRate: Player not found, id %d\n", id);
	}
	fflush(stdout);
	return 1.0;
}

/*
 * Class:     nl_mpi_jds_JDSPlayer
 * Method:    setVolume
 * Signature: (JF)V
 * The volume should be passed as a value between 0 and 1, inclusive.
 */
JNIEXPORT void JNICALL Java_nl_mpi_jds_JDSPlayer_setVolume
	(JNIEnv * env, jobject thisObj, jlong id, jfloat volume) {
	if (pm.containsKey((long)id)) {
		DSPlayer* dsp = pm.get((long)id);
		long vol = (long) ((volume * 10000) - 10000);
		dsp->setVolume(vol);
	} else {
		printf("Native setVolume: Player not found, id %d\n", id);
	}
	fflush(stdout);
}

/*
 * Class:     nl_mpi_jds_JDSPlayer
 * Method:    getVolume
 * Signature: (J)F
 * The volume is returned as a value between 0 and 1, inclusive.
 */
JNIEXPORT jfloat JNICALL Java_nl_mpi_jds_JDSPlayer_getVolume
	(JNIEnv *env, jobject thisObj, jlong id) {
	jfloat retVolume = (jfloat)1.0;
	if (pm.containsKey((long)id)) {
		DSPlayer* dsp = pm.get((long)id);
		long vol = dsp->getVolume();

		retVolume = (jfloat) ((vol + 10000) / (float)10000);
	} else {
		printf("Native getVolume: Player not found, id %d\n", id);
	}
	fflush(stdout);
	return retVolume;
}

/*
 * Class:     nl_mpi_jds_JDSPlayer
 * Method:    setBalance
 * Signature: (JJ)V
 */
JNIEXPORT void JNICALL Java_nl_mpi_jds_JDSPlayer_setBalance
	(JNIEnv *env, jobject thisObj, jlong id, jlong balance) {
	if (pm.containsKey((long)id)) {
		DSPlayer* dsp = pm.get((long)id);
		
		dsp->setBalance((long) balance);
	} else {
		printf("Native setBalance: Player not found, id %d\n", id);
	}
	fflush(stdout);
}

/*
 * Class:     nl_mpi_jds_JDSPlayer
 * Method:    getBalance
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_nl_mpi_jds_JDSPlayer_getBalance
	(JNIEnv *env, jobject thisObj, jlong id) {
	jlong retBalance = (jlong) 0;

	if (pm.containsKey((long)id)) {
		DSPlayer* dsp = pm.get((long)id);
		retBalance = (jlong) dsp->getBalance();
	} else {
		printf("Native getBalance: Player not found, id %d\n", id);
	}
	fflush(stdout);

	return retBalance;
}
/*
 * Class:     nl_mpi_jds_JDSPlayer
 * Method:    setMediaTime
 * Signature: (JJ)V
 * The time value is expected to be in 100 nanoseconds values.
 */
JNIEXPORT void JNICALL Java_nl_mpi_jds_JDSPlayer_setMediaTime
	(JNIEnv *env, jobject thisObj, jlong id, jlong time) {
	if (pm.containsKey((long)id)) {
		DSPlayer* dsp = pm.get((long)id);
		// printf("Native JDSPlayer setMediaTime: %d\n", time);
		// the default media time format is 100 nano seconds
		dsp->setMediaPosition(time);
	} else {
		printf("Native setMediaTime: Player not found, id %d\n", id);
	}
	fflush(stdout);
}

/*
 * Class:     nl_mpi_jds_JDSPlayer
 * Method:    getMediaTime
 * Signature: (J)J
 * The time value is returned in reference time, units of 100 nan0seconds.
 */
JNIEXPORT jlong JNICALL Java_nl_mpi_jds_JDSPlayer_getMediaTime
	(JNIEnv *env, jobject thisObj, jlong id) {
	jlong medTime = 0;
	if (pm.containsKey((long)id)) {
		DSPlayer* dsp = pm.get((long)id);
		// the default media time format is "reference time", units of 100 nano seconds
		medTime = (jlong) dsp->getMediaPosition();
	} else {
		printf("Native getMediaTime: Player not found, id %d\n", id);
	}
	fflush(stdout);
	return medTime;
}

/*
 * Class:     nl_mpi_jds_JDSPlayer
 * Method:    getDuration
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_nl_mpi_jds_JDSPlayer_getDuration
	(JNIEnv *env, jobject thisObj, jlong id) {
	jlong duration = 0;
	if (pm.containsKey((long)id)) {
		DSPlayer* dsp = pm.get((long)id);
		// the default media time format is "reference time", units of 100 nano seconds
		duration = (jlong) dsp->getDuration();
	} else {
		printf("Native getDuration: Player not found, id %d\n", id);
	}
	fflush(stdout);
	return duration;
}

/*
 * Class:     nl_mpi_jds_JDSPlayer
 * Method:    getFrameRate
 * Signature: (J)D
 */
JNIEXPORT jdouble JNICALL Java_nl_mpi_jds_JDSPlayer_getFrameRate
	(JNIEnv *env, jobject thisObj, jlong id) {
	jdouble framerate = 0.0;
	if (pm.containsKey((long)id)) {
		DSPlayer* dsp = pm.get((long)id);
		double timePerFrame = dsp->getTimePerFrame();
		//printf("Time per frame double 1: %f\n", timePerFrame);
		if (timePerFrame > 0) {
			framerate = (jdouble) (1 / timePerFrame);
		}
	} else {
		printf("Native getFrameRate: Player not found, id %d\n", id);
	}
	fflush(stdout);
	// 0 indicates an error, throw exception?
	return framerate;
}

/*
 * Class:     nl_mpi_jds_JDSPlayer
 * Method:    getTimePerFrame
 * Signature: (J)D
 * Returns time pre frame in seconds!
 */
JNIEXPORT jdouble JNICALL Java_nl_mpi_jds_JDSPlayer_getTimePerFrame
(JNIEnv *env, jobject thisObj, jlong id) {
	jdouble frametime = 0.0;
	if (pm.containsKey((long)id)) {
		DSPlayer* dsp = pm.get((long)id);
		frametime = (jdouble) dsp->getTimePerFrame();
	} else {
		printf("Native getTimePerFrame: Player not found, id %d\n", id);
	}
	fflush(stdout);
	// 0 indicates an error, throw exception?
	return frametime;
}


/*
 * Class:     nl_mpi_jds_JDSPlayer
 * Method:    getAspectRatio
 * Signature: (J)F
 */
JNIEXPORT jfloat JNICALL Java_nl_mpi_jds_JDSPlayer_getAspectRatio
	(JNIEnv *env, jobject thisObj, jlong id) {
	jfloat ratio = 1.0;
	if (pm.containsKey((long)id)) {
		DSPlayer* dsp = pm.get((long)id);		
		long h = dsp->getOrgVideoHeight();

		if (h > 0) {
			long w = dsp->getOrgVideoWidth();
			ratio = (jfloat) w / h;
		}
	} else {
		printf("Native getAspectRatio: Player not found, id %d\n", id);
	}
	fflush(stdout);
	return ratio;
}

/*
 * Class:     nl_mpi_jds_JDSPlayer
 * Method:    getOriginalSize
 * Signature: (J)Ljava/awt/Dimension;
 */
JNIEXPORT jobject JNICALL Java_nl_mpi_jds_JDSPlayer_getOriginalSize
	(JNIEnv *env, jobject thisObj, jlong id) {
	jobject size;
	if (pm.containsKey((long)id)) {
		DSPlayer* dsp = pm.get((long)id);		
		long h = dsp->getOrgVideoHeight();

		if (h > 0) {
			long w = dsp->getOrgVideoWidth();
			
			jclass clazz = env->FindClass("java/awt/Dimension");
			jmethodID mid = env->GetMethodID(clazz, "<init>", "(II)V");
			size = env->NewObject(clazz, mid, (jint) w, (jint) h);
		} else {
			size = NULL;
		}
	} else {
		printf("Native getOriginalSize: Player not found, id %d\n", id);
		size = NULL;
	}
	fflush(stdout);
	return size;
}


/*
 * Class:     nl_mpi_jds_JDSPlayer
 * Method:    setVisualComponent
 * Signature: (JLjava/awt/Component;)V
 */
JNIEXPORT void JNICALL Java_nl_mpi_jds_JDSPlayer_setVisualComponent
  (JNIEnv *env, jobject thisObj, jlong id, jobject compObj) {
	// if compObj is NULL set the video owner to the void pointer
	if (compObj == NULL) {
		if (pm.containsKey((long)id)) {
			DSPlayer* dsp = pm.get((long)id);

			HRESULT hr = dsp->setOwnerWindow(NULL);
			if (FAILED(hr)){
				printf("Native setVisualComponent: failed to set the window handle of the player\n");
			} 
			//else {
			//	printf("Native: window handle succesfully applied\n");
			//}
		} else {
			printf("Native setVisualComponent: Player not found, id %d\n", id);
		}
		fflush(stdout);
		return;
	}
	// get the right player, set the owner
	// get the window handle of compObj
	JAWT awt;
	awt.version = JAWT_VERSION_1_4;
	JAWT_DrawingSurface* ds;
	JAWT_DrawingSurfaceInfo* dsi;
	JAWT_Win32DrawingSurfaceInfo* dsi_win;
	jboolean result;
	jint lock;
	HWND hwnd;

	result = JAWT_GetAWT(env, &awt);
	if (result == JNI_FALSE) {
		printf("Native setVisualComponent: unable to get AWT\n");
		fflush(stdout);
		return;
	}
	ds = awt.GetDrawingSurface(env, compObj);
	if (ds == NULL) {
		printf("Native setVisualComponent: unable to get the Drawing Surface\n");
		fflush(stdout);
		return;
	}
	lock = ds->Lock(ds);
	if ((lock & JAWT_LOCK_ERROR) != 0) {
		printf("Native setVisualComponent: unable to get lock the Drawing Surface\n");
		fflush(stdout);
		return;
	}

	// Get the drawing surface info
	dsi = ds->GetDrawingSurfaceInfo(ds);

	// Get the platform-specific drawing info
	dsi_win = (JAWT_Win32DrawingSurfaceInfo*)dsi->platformInfo;

	// get the handle
	hwnd = (HWND) dsi_win->hwnd;
	if (DSUtil::JDS_DEBUG) {
		printf("Native setVisualComponent: Canvas handle: %p\n", hwnd);
	}
	// Free the drawing surface info
	ds->FreeDrawingSurfaceInfo(dsi);

	// Unlock the drawing surface
	ds->Unlock(ds);

	// Free the drawing surface
	awt.FreeDrawingSurface(ds);
	if (pm.containsKey((long)id)) {
		DSPlayer* dsp = pm.get((long)id);

		//env->MonitorEnter(compObj);
		HRESULT hr = dsp->setOwnerWindow(hwnd);
		if (FAILED(hr)){
			printf("Native setVisualComponent: failed to set the window handle of the player\n");
		} 
		//else {
		//	printf("Native: window handle succesfully applied\n");
		//}
		//env->MonitorExit(compObj);
	} else {
		printf("Native setVisualComponent: Player not found, id %d\n", id);
	}
	fflush(stdout);
	// test result
  }

/*
 * Class:     nl_mpi_jds_JDSPlayer
 * Method:    setVisualComponentPos
 * Signature: (JIIII)V
 */
JNIEXPORT void JNICALL Java_nl_mpi_jds_JDSPlayer_setVisualComponentPos
	(JNIEnv *env, jobject thisObj, jlong id, jint x, jint y, jint w, jint h) {

	if (pm.containsKey((long)id)) {
		DSPlayer* dsp = pm.get((long)id);

		HRESULT hr = dsp->setVideoWindowPos((long)x, (long)y, (long)w, (long)h);
		if (FAILED(hr)) {
			printf("Native setVisualComponentPos: could not set the window position\n");
		}
	} else {
		printf("Native setVisualComponentPos: Player not found, id %d\n", id);
	}
	fflush(stdout);
}

/*
 * Class:     nl_mpi_jds_JDSPlayer
 * Method:    setVisible
 * Signature: (JZ)V
 */
JNIEXPORT void JNICALL Java_nl_mpi_jds_JDSPlayer_setVisible
	(JNIEnv *env, jobject thisObj, jlong id, jboolean visible) {
	if (pm.containsKey((long)id)) {
		DSPlayer* dsp = pm.get((long)id);
		HRESULT hr;

		if (visible == JNI_TRUE) {
			hr = dsp->setVisible(-1);
		} else {
			hr = dsp->setVisible(0);
		}
		if (FAILED(hr)) {
			printf("Native setVisible: could not set the window visibility\n");
		}
	} else {
		printf("Native setVisible: Player not found, id %d\n", id);
	}
	fflush(stdout);
}

JNIEXPORT void JNICALL Java_nl_mpi_jds_JDSPlayer_setVideoSourcePos
	(JNIEnv *env, jobject thisObj, jlong id, jint x, jint y, jint w, jint h) {
	if (pm.containsKey((long)id)) {
		DSPlayer* dsp = pm.get((long)id);
		// do something with the HRESULT returned?
		dsp->setVideoSourcePos((long)x, (long)y, (long)w, (long)h);
	} else {
		printf("Native setVideoSourcePos: Player not found, id %d\n", id);
	}
	fflush(stdout);
}

JNIEXPORT void JNICALL Java_nl_mpi_jds_JDSPlayer_setVideoDestinationPos
(JNIEnv *env, jobject thisObj, jlong id, jint x, jint y, jint w, jint h) {
	if (pm.containsKey((long)id)) {
		DSPlayer* dsp = pm.get((long)id);
		dsp->setVideoDestinationPos((long)x, (long)y, (long)w, (long)h);
		// do something with the HRESULT returned?
		
	} else {
		printf("Native setVideoDestinationPos: Player not found, id %d\n", id);
	}
	fflush(stdout);
}

JNIEXPORT jintArray JNICALL Java_nl_mpi_jds_JDSPlayer_getVideoDestinationPos
	(JNIEnv *env, jobject thisObj, jlong id) {
		jintArray iArray;
	if (pm.containsKey((long)id)) {
		DSPlayer* dsp = pm.get((long)id);
		// do something with the HRESULT returned?
		const int size = 4;
		long vdPos[size];
		HRESULT hr = dsp->getVideoDestinationPos(&vdPos[0], &vdPos[1], &vdPos[2], &vdPos[3]);
		if (SUCCEEDED(hr)) {
			iArray = env->NewIntArray((jsize) size);
			jint isize = env->GetArrayLength(iArray);
			//printf("Size of new int array: %d\n", isize);
			jboolean isCopy;
			jint* pIntArray;
			pIntArray = env->GetIntArrayElements(iArray, &isCopy);
			pIntArray[0] = vdPos[0];
			pIntArray[1] = vdPos[1];
			pIntArray[2] = vdPos[2];
			pIntArray[3] = vdPos[3];

			env->ReleaseIntArrayElements(iArray, pIntArray, 0);
		} else {
			iArray = NULL;
		}
		return iArray; 
		
	} else {
		printf("Native getVideoDestinationPos: Player not found, id %d\n", id);
		iArray = NULL;
	}
	fflush(stdout);
	return iArray;
}

/*
 * Class:     nl_mpi_jds_JDSPlayer
 * Method:    isVisualMedia
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_nl_mpi_jds_JDSPlayer_isVisualMedia
	(JNIEnv *env, jobject thisObj, jlong id) {
	if (pm.containsKey((long)id)) {
		DSPlayer* dsp = pm.get((long)id);

		if (dsp->isVisualMedia()) {
			return JNI_TRUE;
		}
	} else {
		printf("Native isVisualMedia: Player not found, id %d\n", id);
	}
	fflush(stdout);
	return JNI_FALSE;
}

/*
 * Class:     nl_mpi_jds_JDSPlayer
 * Method:    initWithFile
 * Signature: (Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_nl_mpi_jds_JDSPlayer_initWithFile
  (JNIEnv *env, jobject thisObj, jstring filepath) {
	long nextId = DSUtil::createID();
	//printf("Native create id: %d\n", nextId);
	
	JNIUtil jniu;
	wchar_t *str;
	//str = jniu.jstringToCharArray(env, filepath);
	str = jniu.convert(env, filepath);
	//printf("Native path: %s\n", str);
	// try, catch exception
	DSPlayer* dsp = new DSPlayer();

	HRESULT hr;
	hr = dsp->setMediaFile(str);
	if (FAILED(hr)) {

		// throw JDSException
		//jthrowable throwObj;
		jclass exClass;
		//jmethodID mid; 

		const int ERROR_SIZE = 256;
		//WCHAR errorText[ERROR_SIZE]; // wide char variant
		//DWORD errLength = AMGetErrorTextW(hr, (LPWSTR)errorText, ERROR_SIZE);
		TCHAR cerrorText[ERROR_SIZE];
		DWORD cerrLength = AMGetErrorTextA(hr, (LPSTR)cerrorText, ERROR_SIZE);
		if (cerrLength == 0) {
			char *mes = "Unknown error occurred while creating a Direct Show player.";
			memcpy(cerrorText, mes, sizeof(char) * ERROR_SIZE);
			//printf("Error %s\n", cerrorText);
		} 
 		//if (errLength == 0) {
		//	wchar_t *mes = L"Unknown error occurred while creating a Direct Show player.";
			//wprintf(L"Error: %d %s\n", errLength, errorText);
			//copy to errorText
		//}

		exClass = env->FindClass("nl/mpi/jds/JDSException");
		//mid = env->GetMethodID(exClass, "<init>", "(Ljava/lang/String;)V");

		if (exClass != NULL) {
			env->ThrowNew(exClass, cerrorText);
		} else {
			exClass = env->FindClass("java/lang/Exception");
			env->ThrowNew(exClass, cerrorText);
		}

		delete(str);
		delete(dsp);
		fflush(stdout);// make sure messages make it to the Java world
		return (jlong)-1;
	}
	// store in map
	if (SUCCEEDED(hr)) {
		pm.put(nextId, dsp);
	}
	//parr.put(nextId, dsp);
	/*
	HRESULT hr;
	hr = dsp.initGraph();
	if (SUCCEEDED(hr)) {
		printf("Native: graph has been created\n");
	} else {
		printf("Native: could not create the graph\n");
		return -1;
	}
	*/
	// DSPlayer maintains its own copy
	delete(str);
	fflush(stdout);// make sure messages make it to the Java world
	return (jlong)nextId;
  }

/*
 * Class:     nl_mpi_jds_JDSPlayer
 * Method:    initWithFileAndCodec
 * Signature: (Ljava/lang/String;Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_nl_mpi_jds_JDSPlayer_initWithFileAndCodec
  (JNIEnv *env, jobject thisObj, jstring filepath, jstring codec) {
	  long nextId = DSUtil::createID();
	//printf("Native create id: %d\n", nextId);

	JNIUtil jniu;
	wchar_t *str;
	wchar_t *cod;
	str = jniu.convert(env, filepath);
	cod = jniu.convert(env, codec);
	//printf("Native path: %s\n", str);
	//wprintf(L"Native codec: %s\n", cod);
	// try, catch exception
	//DSPlayer dsp(str, cod);
	DSPlayer* dsp = new DSPlayer();
	HRESULT hr;
	hr = dsp->setMediaFile(str, cod);
	if (FAILED(hr)) {
		// throw JDSException
		//jthrowable throwObj;
		jclass exClass;
		//jmethodID mid;
		const int ERROR_SIZE = 256;
		TCHAR cerrorText[ERROR_SIZE];
		DWORD cerrLength = AMGetErrorTextA(hr, (LPSTR)cerrorText, ERROR_SIZE);
		if (cerrLength == 0) {
			char *mes = "Unknown error occurred while creating a Direct Show player.";
			memcpy(cerrorText, mes, sizeof(char) * ERROR_SIZE);
		} 

		exClass = env->FindClass("nl/mpi/jds/JDSException");
		//mid = env->GetMethodID(exClass, "<init>", "(Ljava/lang/String;)V");

		if (exClass != NULL) {
			env->ThrowNew(exClass, cerrorText);
		} else {
			exClass = env->FindClass("java/lang/Exception");
			env->ThrowNew(exClass, cerrorText);
		}

		delete(str);
		delete(dsp);
		fflush(stdout);// make sure messages make it to the Java world
		return (jlong)-1;
	}
	// store in map
	if (SUCCEEDED(hr)) {
		pm.put(nextId, dsp);
	}
	delete(str);
	delete(cod);
	fflush(stdout);
	return (jlong) nextId;
  }

/*
 * Class:     nl_mpi_jds_JDSPlayer
 * Method:    isCodecInstalled
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_nl_mpi_jds_JDSPlayer_isCodecInstalled
  (JNIEnv *env, jobject thisObj, jstring codec) {
	DSUtil dsu;
	JNIUtil jniu;
	wchar_t *str = jniu.convert(env, codec);
	
	boolean b = dsu.isCodecInstalled(str);
	
	delete(str);
	fflush(stdout);
	return (jboolean) b;
}

/*
 * Class:     nl_mpi_jds_JDSPlayer
 * Method:    getRegisteredFilters
 * Signature: ()[Ljava/lang/String;
 */
JNIEXPORT jobjectArray JNICALL Java_nl_mpi_jds_JDSPlayer_getRegisteredFilters
  (JNIEnv *env, jobject thisObj) {  
	HRESULT hr;
	list<wchar_t *> filtList;
	DSUtil dsu;
	hr = dsu.printAllFilters(filtList);
	if (SUCCEEDED(hr)) {
		JNIUtil jnu;
		// create a string array with size of the filter list
		size_t size = filtList.size();
		jclass clazz = env->FindClass("java/lang/String");
		jobjectArray filtArray;
		filtArray = env->NewObjectArray((jint) size, clazz, NULL);
		int i = 0;
		list<wchar_t *>::iterator iter;
		for(iter = filtList.begin(); iter != filtList.end(); iter++) {
			size_t length;
			jchar *jc = jnu.convertToJchar(env, *iter, &length); 
			// convert the wchar to a jstring
			jstring filt = env->NewString(jc, length);
			env->SetObjectArrayElement(filtArray, i, filt);
			env->DeleteLocalRef(filt);
			delete(jc);
			// can we delete the contents of the list?
			delete(*iter);
			i++;
		}
		
		return filtArray;
	}
	return NULL;
}

/*
 * Class:     nl_mpi_jds_JDSPlayer
 * Method:    getFiltersInGraph
 * Signature: (J)[Ljava/lang/String;
 */
JNIEXPORT jobjectArray JNICALL Java_nl_mpi_jds_JDSPlayer_getFiltersInGraph
  (JNIEnv *env, jobject thisObj, jlong id) {
  	if (pm.containsKey((long)id)) {
		DSPlayer* dsp = pm.get((long)id);

		HRESULT hr;
		list<wchar_t *> filtList;
		DSUtil dsu;
		hr = dsu.printFilters(dsp->pGraph, filtList);
		if (SUCCEEDED(hr)) {
			JNIUtil jnu;
			// create a string array with size of the filter list
			size_t size = filtList.size();
			jclass clazz = env->FindClass("java/lang/String");
			jobjectArray filtArray;
			filtArray = env->NewObjectArray((jint) size, clazz, NULL);
			int i = 0;
			list<wchar_t *>::iterator iter;
			for(iter = filtList.begin(); iter != filtList.end(); iter++) {
				size_t length;
				jchar *jc = jnu.convertToJchar(env, *iter, &length); 
				// convert the wchar to a jstring
				jstring filt = env->NewString(jc, length);
				env->SetObjectArrayElement(filtArray, i, filt);
				env->DeleteLocalRef(filt);
				delete(jc);
				i++;
				// can we delete the contents of the list?
				delete(*iter);
			}
			return filtArray;
		}
		} else {
			printf("Native getFiltersInGraph: Player not found, id %d\n", id);
		}
	fflush(stdout);
	return NULL;
}

/*
 * Class:     nl_mpi_jds_JDSPlayer
 * Method:    getFileType
 * Signature: (Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_nl_mpi_jds_JDSPlayer_getFileType
  (JNIEnv *env, jobject thisObj, jstring mediaPath) {
	  JNIUtil jniu;
	  wchar_t *file = jniu.convert(env, mediaPath);
	  DSUtil dsu;
	  wchar_t *type = dsu.getMediaSubType(file); 
	  if (type != NULL) {
		  size_t length;
		  jchar *medType = jniu.convertToJchar(env, type, &length);
		  jstring typeString = env->NewString(medType, (jsize) length);
		  // clean up 
		  delete(file);
		  delete(type);
		  delete(medType);
		  fflush(stdout);
		  return typeString;
	  }
	  fflush(stdout);
	  return NULL;
}

/*
 * Class:     nl_mpi_jds_JDSPlayer
 * Method:    setStopTime
 * Signature: (JJ)V
 * The stopTime parameter is expected to be in reference time (100 nano sec)
 */
JNIEXPORT void JNICALL Java_nl_mpi_jds_JDSPlayer_setStopTime
  (JNIEnv *env, jobject thisObj, jlong id, jlong stopTime) {
	if (pm.containsKey((long)id)) {
		DSPlayer* dsp = pm.get((long)id);
		HRESULT hr = dsp->setStopPosition((__int64) stopTime);
		if (FAILED(hr)) {
			// throw JDSException
			//jthrowable throwObj;
			jclass exClass;
			//jmethodID mid;

			exClass = env->FindClass("nl/mpi/jds/JDSException");
			//mid = env->GetMethodID(exClass, "<init>", "(Ljava/lang/String;)V");
			if (exClass != NULL) {
				env->ThrowNew(exClass, "SetStopPosition is not supported");
			} else {
				exClass = env->FindClass("java/lang/Exception");
				env->ThrowNew(exClass, "SetStopPosition is not supported");
			}
		}
	} else {
		printf("Native setStopTime: Player not found, id %d\n", id);
	}
	fflush(stdout);
}

/*
 * Returns the current stoptime in reference time units (100 nano sec)
 * Default (if an error occurred) is 0
 */
JNIEXPORT jlong JNICALL Java_nl_mpi_jds_JDSPlayer_getStopTime
	(JNIEnv *env, jobject thisObj, jlong id){
	jlong stopTime = 0;
	if (pm.containsKey((long)id)) {
		DSPlayer* dsp = pm.get((long)id);
		stopTime = (jlong) dsp->getStopPosition();
	} else {
		printf("Native getStopTime: Player not found, id %d\n", id);
	}
	fflush(stdout);
	return stopTime;
}


/*
 * Class:     nl_mpi_jds_JDSPlayer
 * Method:    getSourceHeight
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_nl_mpi_jds_JDSPlayer_getSourceHeight
  (JNIEnv *env, jobject thisObj, jlong id) {
	jint h;
	if (pm.containsKey((long)id)) {
		DSPlayer* dsp = pm.get((long)id);		
		h = (jint) dsp->getOrgVideoHeight();

	} else {
		printf("Native getSourceHeight: Player not found, id %d\n", id);
		h = 0;
	}
	fflush(stdout);
	return h;
}

/*
 * Class:     nl_mpi_jds_JDSPlayer
 * Method:    getSourceWidth
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_nl_mpi_jds_JDSPlayer_getSourceWidth
  (JNIEnv *env, jobject thisObj, jlong id) {
	jint w;
	if (pm.containsKey((long)id)) {
		DSPlayer* dsp = pm.get((long)id);		
		w = (jint) dsp->getOrgVideoWidth();

	} else {
		printf("Native getSourceWidth: Player not found, id %d\n", id);
		w = 0;
	}
	fflush(stdout);
	return w;
}

/*
 * Class:     nl_mpi_jds_JDSPlayer
 * Method:    getPreferredAspectRatio
 * Signature: (J)[I
 */
JNIEXPORT jintArray JNICALL Java_nl_mpi_jds_JDSPlayer_getPreferredAspectRatio
  (JNIEnv *env, jobject thisObj, jlong id) {
	jintArray iArray;
	if (pm.containsKey((long)id)) {
		DSPlayer* dsp = pm.get((long)id);
		long pX;
		long pY;

		HRESULT hr = dsp->getPreferredAspectRatio(&pX, &pY);
		if (hr == S_OK) {
			jboolean isCopy;
			iArray = env->NewIntArray(2);
			jint *pIA = env->GetIntArrayElements(iArray, &isCopy);
			pIA[0] = (jint) pX;
			pIA[1] = (jint) pY;

			//if(isCopy == JNI_TRUE) {//always release??
				env->ReleaseIntArrayElements(iArray, pIA, 0);
			//}
		}

	} else {
		printf("Native getPreferredAspectRatio: Player not found, id %d\n", id);
		iArray = NULL;
	}
	fflush(stdout);
	return iArray;
}

/*
 * Class:     nl_mpi_jds_JDSPlayer
 * Method:    getCurrentImage
 * Signature: (J)[B
 */
JNIEXPORT jbyteArray JNICALL Java_nl_mpi_jds_JDSPlayer_getCurrentImage
  (JNIEnv *env, jobject thisObj, jlong id) {
	jbyteArray bArray;
	if (pm.containsKey((long)id)) {
		DSPlayer* dsp = pm.get((long)id);
       
		if (!dsp->isPlaying()) {
			HRESULT hr;
			long size;
			//printf("Size of long %d\n", sizeof(long));
			hr = dsp->getCurrentImage(&size, NULL);
			if (SUCCEEDED(hr)) {
				// print size
				//printf("Size of image buffer: %d\n", size);
				byte* pDIBImage;
				pDIBImage = (byte *)malloc(size);
				hr = dsp->getCurrentImage(&size, (long *)pDIBImage);
				if (SUCCEEDED(hr)) {
					// convert to a jbyteArray
					bArray = env->NewByteArray((jsize) size);
					jint isize = env->GetArrayLength(bArray);
					//printf("Size of new byte array: %d\n", isize);
					jboolean isCopy;
					jbyte* pByteArray;
					pByteArray = env->GetByteArrayElements(bArray, &isCopy);

					for (int i = 0; i < size; i++) {
						pByteArray[i] = (jbyte) pDIBImage[i];
					}
					
					//if(isCopy == JNI_TRUE) { // always release?
						env->ReleaseByteArrayElements(bArray, pByteArray, 0);
					//}
				} else {
					printf("Native getCurrentImage: No buffer created, no buffer filled: %d\n", hr);
					bArray = NULL;
				}
				free(pDIBImage);
			} else {
				printf("Native getCurrentImage: No buffer size calculated: %d\n", hr);
				bArray = NULL;
			}
		} else {
			printf("Native getCurrentImage: Player is not paused...\n");
			// stop the player or return an error? 
			bArray = NULL;
		}
	} else {
		printf("Native getCurrentImage: Player not found, id %d\n", id);
		bArray = NULL;
	}
	fflush(stdout);
	return bArray;
}

/*
 * Class:     nl_mpi_jds_JDSPlayer
 * Method:    getImageAtTime
 * Signature: (JJ)[B
 */
JNIEXPORT jbyteArray JNICALL Java_nl_mpi_jds_JDSPlayer_getImageAtTime
  (JNIEnv *env, jobject thisObj, jlong id, jlong time) {
	jbyteArray bArray;
	if (pm.containsKey((long)id)) {
		DSPlayer* dsp = pm.get((long)id);
		bool isPlaying = dsp->isPlaying();

		if (isPlaying) {
			dsp->pause();
		}
		__int64 curTime = dsp->getMediaPosition();
		if (time != curTime) {
			dsp->setMediaPosition(time);
		}
		bArray = Java_nl_mpi_jds_JDSPlayer_getCurrentImage(env, thisObj, id);
		
		dsp->setMediaPosition(curTime);
		if (isPlaying) {
			dsp->start();
		}
	} else {
		printf("Native getImageAtTime: Player not found, id %d\n", id);
		bArray = NULL;
	}
	return bArray;
}

/*
 * Class:     nl_mpi_jds_JDSPlayer
 * Method:    enableDebugMode
 * Signature: (Z)V
 */
JNIEXPORT void JNICALL Java_nl_mpi_jds_JDSPlayer_enableDebugMode
	(JNIEnv *env, jclass theClass, jboolean enable) {
		printf("Setting debug mode %d\n", enable);
		DSUtil::JDS_DEBUG = (bool) enable;
}

/*
 * Class:     nl_mpi_jds_JDSPlayer
 * Method:    clean
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_nl_mpi_jds_JDSPlayer_clean
	(JNIEnv *env, jobject thisObj, jlong id) {
	if (pm.containsKey((long)id)) {
		DSPlayer* dsp = pm.remove((long)id);
		HRESULT hr;
		hr = dsp->cleanUpOnClose();
		if (FAILED(hr)) {
			printf("Native clean: Unable to clean up all resources, code: %d\n", hr);
		}
		//delete dsp;//??
		delete(dsp);
	}
	fflush(stdout);
}

#ifdef __cplusplus
}
#endif