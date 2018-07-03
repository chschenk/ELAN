/* 
 * Project:	JMMFPlayer, Microsoft Media Foundation Player for Java
 * Author:	Han Sloetjes
 * Version:	1.0, May 2011
 */
#include <jawt.h>
#include <jawt_md.h>
#include "nl_mpi_jmmf_JMMFPlayer.h"
#include "MMFPlayerMap.h"
#include "MMFUtil.h"
#include "MMFJNIUtil.h"


static MMFPlayerMap pm;

#ifdef __cplusplus
extern "C" {
#endif

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    createPlayerFor
 * Signature: (JLjava/lang/String;Ljava/awt/Component;)Z
 */
JNIEXPORT jboolean JNICALL Java_nl_mpi_jmmf_JMMFPlayer_createPlayerFor
	(JNIEnv *env, jobject thisObj, jlong id, jstring filepath, jobject compObj) {
	MMFJNIUtil *jniu = new MMFJNIUtil();
	wchar_t *str;
	//str = jniu->jstringToCharArray(env, filepath);
	str = jniu->convert(env, filepath);
	printf("MMF JNI createPlayerFor: media path is: %ls\n", str);
	// try, catch exception
	if (pm.containsKey((long)id)) {
		MMFPlayer* dsp = pm.get((long)id);
		
		
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
			printf("MMF JNI createPlayerFor: unable to get AWT.\n");
			fflush(stdout);
			return (jlong)-1;
		}
		ds = awt.GetDrawingSurface(env, compObj);
		if (ds == NULL) {
			printf("MMF JNI createPlayerFor: unable to get the Drawing Surface.\n");
			fflush(stdout);
			return (jlong)-1;
		}
		lock = ds->Lock(ds);
		if ((lock & JAWT_LOCK_ERROR) != 0) {
			printf("MMF JNI createPlayerFor: unable to get lock of the Drawing Surface.\n");
			fflush(stdout);
			//return (jlong)-1;
		}

		// Get the drawing surface info
		dsi = ds->GetDrawingSurfaceInfo(ds);

		// Get the platform-specific drawing info
		dsi_win = (JAWT_Win32DrawingSurfaceInfo*)dsi->platformInfo;

		// get the handle
		hwnd = (HWND) dsi_win->hwnd;
		if (MMFUtil::JMMF_DEBUG) {
			printf("MMF JNI createPlayerFor: canvas handle: %p\n", hwnd);
		}
		// Free the drawing surface info
		ds->FreeDrawingSurfaceInfo(dsi);

		// Unlock the drawing surface
		ds->Unlock(ds);

		// Free the drawing surface
		awt.FreeDrawingSurface(ds);
		if (MMFUtil::JMMF_DEBUG) {
			printf("MMF JNI createPlayerFor: retrieved the window handle: %p\n", hwnd);
		}
		fflush(stdout);
		HRESULT hr;

		hr = dsp->initSession(str, hwnd);
		//printf("MMF JNI createPlayerFor: init session... %d\n", hr);
		if (FAILED(hr)) {
			// throw JMMFException
			//jthrowable throwObj;
			jclass exClass;
			//jmethodID mid; 

			char *mes = "Unknown error occurred while creating a Microsoft Media Foundation player.\n";

			exClass = env->FindClass("nl/mpi/jmmf/JMMFException");
			//mid = env->GetMethodID(exClass, "<init>", "(Ljava/lang/String;)V");

			if (exClass != NULL) {
				env->ThrowNew(exClass, mes);
			} else {
				exClass = env->FindClass("java/lang/Exception");
				env->ThrowNew(exClass, mes);
			}

			delete(str);
			delete(jniu);
			fflush(stdout);// make sure messages make it to the Java world
			return JNI_FALSE;
		}
	} else {
		printf("MMF JNI createPlayerFor: Player not found, id %lld\n", id);
	}
	// MMFPlayer maintains its own copy
	delete(str);
	delete(jniu);
	fflush(stdout);
	return JNI_TRUE;
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    initPlayer
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_nl_mpi_jmmf_JMMFPlayer_initPlayer
	(JNIEnv *env, jobject thisObj, jboolean synchronous) {
		long nextId = MMFUtil::createID();
		
		MMFPlayer* jmmf = new MMFPlayer((bool) synchronous);
			// store in map
		pm.put(nextId, jmmf);
		if (MMFUtil::JMMF_DEBUG) {
			printf("MMF JNI initPlayer: player instance created, %d\n", nextId);
		}
		return (jlong) nextId;
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    start
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_nl_mpi_jmmf_JMMFPlayer_start
	(JNIEnv *env, jobject thisObj, jlong id) {
	if (pm.containsKey((long)id)) {
		MMFPlayer* dsp = pm.get((long)id);

		HRESULT hr = dsp->start();
		if (FAILED(hr)) {
			printf("MMF JNI start: could not start the media.\n");
		}
	} else {
		printf("MMF JNI start: Player not found, id %lld\n", id);
	}
	fflush(stdout);
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    stop
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_nl_mpi_jmmf_JMMFPlayer_stop
	(JNIEnv *env, jobject thisObj, jlong id) {
	if (pm.containsKey((long)id)) {
		MMFPlayer* dsp = pm.get((long)id);

		HRESULT hr = dsp->stop();
		if (FAILED(hr)) {
			printf("MMF JNI stop: could not stop the media, maybe it was already stopped.\n");
		}
	} else {
		printf("MMF JNI stop: player not found, id %lld\n", id);
	}
	fflush(stdout);
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    pause
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_nl_mpi_jmmf_JMMFPlayer_pause
	(JNIEnv *env, jobject thisObj, jlong id) {
	if (pm.containsKey((long)id)) {
		MMFPlayer* dsp = pm.get((long)id);

		HRESULT hr = dsp->pause();
		if (FAILED(hr)) {
			printf("MMF JNI pause: could not pause the media.\n");
		}
	} else {
		printf("MMF JNI pause: Player not found, id %lld\n", id);
	}
	fflush(stdout);
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    isPlaying
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_nl_mpi_jmmf_JMMFPlayer_isPlaying
	(JNIEnv *env, jobject thisObj, jlong id) {
	jboolean playing = JNI_FALSE;
	if (pm.containsKey((long)id)) {
		MMFPlayer* dsp = pm.get((long)id);
		playing = (jboolean) dsp->isPlaying();
	}  else {
		printf("MMF JNI isPlaying: Player not found, id %lld\n", id);
	}
	fflush(stdout);
	return playing;
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    getState
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_nl_mpi_jmmf_JMMFPlayer_getState
	(JNIEnv *env, jobject thisObj, jlong id) {
	if (pm.containsKey((long)id)) {
		MMFPlayer* dsp = pm.get((long)id);

		int state = dsp->getPlayerState();
		return (jint) state;
	} else {
		printf("MMF JNI getState: Player not found, id %lld\n", id);
	}
	fflush(stdout);
	return -1;
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    setRate
 * Signature: (JF)V
 */
JNIEXPORT void JNICALL Java_nl_mpi_jmmf_JMMFPlayer_setRate
	(JNIEnv *env, jobject thisObj, jlong id, jfloat rate) {

	if (pm.containsKey((long)id)) {
		MMFPlayer* dsp = pm.get((long)id);
		// the play back rate is a double, the default is 1.0
		dsp->setRate(rate);
	} else {
		printf("MMF JNI setRate: Player not found, id %lld\n", id);
	}
	fflush(stdout);
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    getRate
 * Signature: (J)F
 */
JNIEXPORT jfloat JNICALL Java_nl_mpi_jmmf_JMMFPlayer_getRate
	(JNIEnv *env, jobject thisObj, jlong id) {
	jfloat rate = 1.0;
	if (pm.containsKey((long)id)) {
		MMFPlayer* dsp = pm.get((long)id);
		// the play back rate is returned as a double, the default is 1.0
		rate = (jfloat) dsp->getRate();
	} else {
		printf("MMF JNI getRate: Player not found, id %lld\n", id);
	}
	fflush(stdout);
	
	return rate;
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    setVolume
 * Signature: (JF)V
 */
JNIEXPORT void JNICALL Java_nl_mpi_jmmf_JMMFPlayer_setVolume
	(JNIEnv *env, jobject thisObj, jlong id, jfloat volume) {
	if (pm.containsKey((long)id)) {
		MMFPlayer* dsp = pm.get((long)id);
		// the volume is a float, between 0.0 and 1.0, the default is 1.0
		dsp->setVolume(volume);
	} else {
		printf("MMF JNI setVolume: Player not found, id %lld\n", id);
	}
	fflush(stdout);
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    getVolume
 * Signature: (J)F
 */
JNIEXPORT jfloat JNICALL Java_nl_mpi_jmmf_JMMFPlayer_getVolume
	(JNIEnv *env, jobject thisObj, jlong id) {
	float volume = 1.0;
	if (pm.containsKey((long)id)) {
		MMFPlayer* dsp = pm.get((long)id);
		// the volume is a float, between 0.0 and 1.0, the default is 1.0
		volume = (jfloat) dsp->getVolume();
	} else {
		printf("MMF JNI getVolume: Player not found, id %lld\n", id);
	}
	fflush(stdout);
	return (jfloat) 1;
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    setMediaTime
 * Signature: (JJ)V
 */
JNIEXPORT void JNICALL Java_nl_mpi_jmmf_JMMFPlayer_setMediaTime
	(JNIEnv *env, jobject thisObj, jlong id, jlong time) {
	if (pm.containsKey((long)id)) {
		MMFPlayer* dsp = pm.get((long)id);
		// the default media time format is "reference time", units of 100 nano seconds
		dsp->setMediaPosition(time);
	} else {
		printf("MMF JNI setMediaTime: Player not found, id %lld\n", id);
	}
	fflush(stdout);
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    getMediaTime
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_nl_mpi_jmmf_JMMFPlayer_getMediaTime
	(JNIEnv *env, jobject thisObj, jlong id) {
	jlong medTime = 0;
	if (pm.containsKey((long)id)) {
		MMFPlayer* dsp = pm.get((long)id);
		// the default media time format is "reference time", units of 100 nano seconds
		medTime = (jlong) dsp->getMediaPosition();
	} else {
		printf("MMF JNI getMediaTime: Player not found, id %lld\n", id);
	}
	fflush(stdout);
	return medTime;
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    getDuration
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_nl_mpi_jmmf_JMMFPlayer_getDuration
	(JNIEnv *env, jobject thisObj, jlong id) {
	if (pm.containsKey((long)id)) {
		MMFPlayer* dsp = pm.get((long)id);

		__int64 dur = dsp->getDuration();
		// the default media time format is "reference time", units of 100 nano seconds
		return (jlong) dur;
	} else {
		printf("MMF JNI getState: Player not found, id %lld\n", id);
	}
	fflush(stdout);
	return (jlong) 0;
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    getFrameRate
 * Signature: (J)D
 */
JNIEXPORT jdouble JNICALL Java_nl_mpi_jmmf_JMMFPlayer_getFrameRate
	(JNIEnv *env, jobject thisObj, jlong id) {
	jdouble frameRate = 1.0;
	if (pm.containsKey((long)id)) {
		MMFPlayer* dsp = pm.get((long)id);
		double timePerFrame = dsp->getTimePerFrame();

		if (timePerFrame > 0) {
			frameRate = (jdouble) (1 / timePerFrame);
		}
	} else {
		printf("MMF JNI getFrameRate: Player not found, id %lld\n", id);
	}
	fflush(stdout);
	return frameRate;
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    getTimePerFrame
 * Signature: (J)D
 */
JNIEXPORT jdouble JNICALL Java_nl_mpi_jmmf_JMMFPlayer_getTimePerFrame
	(JNIEnv *env, jobject thisObj, jlong id) {
	jdouble timePerFrame = 1.0;
	if (pm.containsKey((long)id)) {
		MMFPlayer* dsp = pm.get((long)id);
		timePerFrame = dsp->getTimePerFrame();
	} else {
		printf("MMF JNI getTimePerFrame: Player not found, id %lld\n", id);
	}
	fflush(stdout);
	return timePerFrame;
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    getAspectRatio
 * Signature: (J)F
 */
JNIEXPORT jfloat JNICALL Java_nl_mpi_jmmf_JMMFPlayer_getAspectRatio
	(JNIEnv *env, jobject thisObj, jlong id) {
	jfloat aspectRatio = 0;
	if (pm.containsKey((long)id)) {
		MMFPlayer* dsp = pm.get((long)id);
		long x = dsp->getOrgVideoWidth();
		long y = dsp->getOrgVideoHeight();

		if (y > 0) {
			aspectRatio = (jfloat) x / y;
		} else {
			printf("MMF JNI getAspectRatio: cannot calculate the aspect ratio, height is 0.\n");
		}

	} else {
		printf("MMF JNI getAspectRatio: Player not found, id %lld\n", id);
	}
	fflush(stdout);
	return aspectRatio;
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    getOriginalSize
 * Signature: (J)Ljava/awt/Dimension;
 */
JNIEXPORT jobject JNICALL Java_nl_mpi_jmmf_JMMFPlayer_getOriginalSize
	(JNIEnv *env, jobject thisObj, jlong id) {
	jobject size;
	if (pm.containsKey((long)id)) {
		MMFPlayer* dsp = pm.get((long)id);		
		long h = dsp->getOrgVideoHeight();

		if (h > 0) {
			long w = dsp->getOrgVideoWidth();
			
			jclass clazz = env->FindClass("java/awt/Dimension");
			jmethodID mid = env->GetMethodID(clazz, "<init>", "(II)V");
			size = env->NewObject(clazz, mid, (jint) w, (jint) h);
		} else {
			size = NULL;
			printf("MMF JNI getOriginalSize: height is 0.\n");
		}
	} else {
		printf("MMF JNI getOriginalSize: Player not found, id %lld\n", id);
		size = NULL;
	}
	fflush(stdout);
	return size;
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    setVisualComponent
 * Signature: (JLjava/awt/Component;)V
 */
JNIEXPORT void JNICALL Java_nl_mpi_jmmf_JMMFPlayer_setVisualComponent
	(JNIEnv *env, jobject thisObj, jlong id, jobject compObj) {
	// if compObj is NULL set the video owner to the void pointer
		// this is problematic?
	if (compObj == NULL) {
		if (pm.containsKey((long)id)) {
			MMFPlayer* dsp = pm.get((long)id);

			HRESULT hr = dsp->setOwnerWindow(NULL);
			if (FAILED(hr)){
				printf("MMF JNI setVisualComponent: failed to set the window handle of the player.\n");
			} 
			//else {
			//	printf("JNI: window handle succesfully applied\n");
			//}
		} else {
			printf("MMF JNI setVisualComponent: Player not found, id %lld\n", id);
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
		printf("MMF JNI setVisualComponent: unable to get AWT.\n");
		fflush(stdout);
		return;
	}
	ds = awt.GetDrawingSurface(env, compObj);
	if (ds == NULL) {
		printf("MMF JNI setVisualComponent: unable to get the Drawing Surface.\n");
		fflush(stdout);
		return;
	}
	lock = ds->Lock(ds);
	if ((lock & JAWT_LOCK_ERROR) != 0) {
		printf("MMF JNI setVisualComponent: unable to get lock the Drawing Surface.\n");
		fflush(stdout);
		return;
	}

	// Get the drawing surface info
	dsi = ds->GetDrawingSurfaceInfo(ds);

	// Get the platform-specific drawing info
	dsi_win = (JAWT_Win32DrawingSurfaceInfo*)dsi->platformInfo;

	// get the handle
	hwnd = (HWND) dsi_win->hwnd;
	if (MMFUtil::JMMF_DEBUG) {
		printf("MMF JNI setVisualComponent: Canvas handle: %p\n", hwnd);
	}
	// Free the drawing surface info
	ds->FreeDrawingSurfaceInfo(dsi);

	// Unlock the drawing surface
	ds->Unlock(ds);

	// Free the drawing surface
	awt.FreeDrawingSurface(ds);
	if (pm.containsKey((long)id)) {
		MMFPlayer* dsp = pm.get((long)id);

		HRESULT hr = dsp->setOwnerWindow(hwnd);
		if (FAILED(hr)){
			printf("MMF JNI setVisualComponent: failed to set the window handle of the player.\n");
		} 
		else {
			printf("MMF JNI setVisualComponent: window handle succesfully applied.\n");
		}

	} else {
		printf("MMF JNI setVisualComponent: Player not found, id %lld\n", id);
	}
	fflush(stdout);
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    setVisible
 * Signature: (JZ)V
 */
JNIEXPORT void JNICALL Java_nl_mpi_jmmf_JMMFPlayer_setVisible
	(JNIEnv *env, jobject thisObj, jlong id, jboolean visible) {

}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    setVideoSourcePos
 * Signature: (JIIII)V
 */
JNIEXPORT void JNICALL Java_nl_mpi_jmmf_JMMFPlayer_setVideoSourcePos
	(JNIEnv *env, jobject thisObj, jlong id, jfloat x, jfloat y, jfloat w, jfloat h) {
	if (pm.containsKey((long)id)) {
		MMFPlayer* dsp = pm.get((long)id);
		HRESULT hr = dsp->setVideoSourcePos(x, y, w, h);
	} else {
		printf("MMF JNI setVideoSourcePos: Player not found, id %lld\n", id);
	}
	fflush(stdout);
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    setVideoDestinationPos
 * Signature: (JIIII)V
 */
JNIEXPORT void JNICALL Java_nl_mpi_jmmf_JMMFPlayer_setVideoDestinationPos
	(JNIEnv *env, jobject thisObj, jlong id, jint x, jint y, jint w, jint h) {
	if (pm.containsKey((long)id)) {
		MMFPlayer* dsp = pm.get((long)id);
		HRESULT hr = dsp->setVideoDestinationPos(x, y, w, h);
	} else {
		printf("MMF JNI setVideoDestinationPos: Player not found, id %lld\n", id);
	}
	fflush(stdout);
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    setVideoSourceAndDestPos
 * Signature: (JFFFFIIII)V
 */
JNIEXPORT void JNICALL Java_nl_mpi_jmmf_JMMFPlayer_setVideoSourceAndDestPos
	(JNIEnv *env, jobject thisObj, jlong id, jfloat sx, jfloat sy, jfloat sw, jfloat sh, jint dx, jint dy, jint dw, jint dh) {
	if (pm.containsKey((long)id)) {
		MMFPlayer* dsp = pm.get((long)id);
		HRESULT hr = dsp->setVideoSourceAndDestPos(sx, sy, sw, sh, dx, dy, dw, dh);
	} else {
		printf("MMF JNI setVideoSourceAndDestPos: Player not found, id %lld\n", id);
	}
	fflush(stdout);
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    getVideoDestinationPos
 * Signature: (J)[I
 */
JNIEXPORT jintArray JNICALL Java_nl_mpi_jmmf_JMMFPlayer_getVideoDestinationPos
	(JNIEnv *env, jobject thisObj, jlong id) {
	jintArray iArray = NULL;
	if (pm.containsKey((long)id)) {
		MMFPlayer* dsp = pm.get((long)id);
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
	} else {
		printf("MMF JNI getVideoDestinationPos: Player not found, id %lld\n", id);
	}
	fflush(stdout);

	return iArray;
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    initWithFile
 * Signature: (Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_nl_mpi_jmmf_JMMFPlayer_initWithFile
	(JNIEnv *env, jobject thisObj, jstring filepath, jboolean synchronous) {
	long nextId = MMFUtil::createID();
	//printf("JNI create id: %d\n", nextId);
	
	MMFJNIUtil *jniu = new MMFJNIUtil();
	wchar_t *str;
	//str = jniu->jstringToCharArray(env, filepath);
	str = jniu->convert(env, filepath);
	//printf("MMF JNI initWithFile: media path: %s\n", str);
	// try, catch exception
	MMFPlayer* dsp = new MMFPlayer((bool) synchronous);
	// if dsp is NULL throw OouOfMemory 
	HRESULT hr;
	hr = dsp->initSessionWithFile(str);

	if (FAILED(hr)) {
		// throw JMMFException
		//jthrowable throwObj;
		jclass exClass;
		//jmethodID mid; 

		char *mes = "Unknown error occurred while creating a Microsoft Media Foundation player.\n";

		exClass = env->FindClass("nl/mpi/jmmf/JMMException");
		//mid = env->GetMethodID(exClass, "<init>", "(Ljava/lang/String;)V");

		if (exClass != NULL) {
			env->ThrowNew(exClass, mes);
		} else {
			exClass = env->FindClass("java/lang/Exception");
			env->ThrowNew(exClass, mes);
		}

		delete(str);
		delete(jniu);
		fflush(stdout);// make sure messages make it to the Java world
		return (jlong)-1;
	}
	// store in map
	pm.put(nextId, dsp);
	//dsp->setJavaPeer(env->NewGlobalRef(thisObj));//hier delete in clean up
	// MMFPlayer maintains its own copy
	delete(str);
	delete(jniu);
	fflush(stdout);// make sure messages make it to the Java world
	return (jlong)nextId;
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    initWithFileAndOwner
 * Signature: (Ljava/lang/String;Ljava/awt/Component;)J
 kan weg??
 */
JNIEXPORT jlong JNICALL Java_nl_mpi_jmmf_JMMFPlayer_initWithFileAndOwner
	(JNIEnv *env, jobject thisObj, jstring filepath, jobject compObj) {
	long nextId = MMFUtil::createID();
	//printf("JNI create id: %d\n", nextId);
	
	MMFJNIUtil *jniu = new MMFJNIUtil();
	wchar_t *str;
	//str = jniu->jstringToCharArray(env, filepath);
	str = jniu->convert(env, filepath);
	printf("MMF JNI initWithFileAndOwner: media path: %ls\n", str);
	// try, catch exception
	MMFPlayer* dsp = new MMFPlayer();
	//printf("JNI player constructed: %d\n", nextId);
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
		printf("MMF JNI initWithFileAndOwner: unable to get AWT.\n");
		fflush(stdout);
		return (jlong)-1;
	}
	ds = awt.GetDrawingSurface(env, compObj);
	if (ds == NULL) {
		printf("MMF JNI initWithFileAndOwner: unable to get the Drawing Surface.\n");
		fflush(stdout);
		return (jlong)-1;
	}
	lock = ds->Lock(ds);
	if ((lock & JAWT_LOCK_ERROR) != 0) {
		printf("MMF JNI setVisualComponent: unable to get lock the Drawing Surface.\n");
		fflush(stdout);
		return (jlong)-1;
	}

	// Get the drawing surface info
	dsi = ds->GetDrawingSurfaceInfo(ds);

	// Get the platform-specific drawing info
	dsi_win = (JAWT_Win32DrawingSurfaceInfo*)dsi->platformInfo;

	// get the handle
	hwnd = (HWND) dsi_win->hwnd;
	if (MMFUtil::JMMF_DEBUG) {
		printf("MMF JNI setVisualComponent: Canvas handle: %p\n", hwnd);
	}
	// Free the drawing surface info
	ds->FreeDrawingSurfaceInfo(dsi);

	// Unlock the drawing surface
	ds->Unlock(ds);

	// Free the drawing surface
	awt.FreeDrawingSurface(ds);
	printf("Retrieved the window handle: %p\n", hwnd);
	fflush(stdout);
	HRESULT hr;

	hr = dsp->initSession(str, hwnd);
	printf("Init session... %d\n", hr);
	if (FAILED(hr)) {
		// throw JMMFException
		//jthrowable throwObj;
		jclass exClass;
		//jmethodID mid; 

		char *mes = "Unknown error occurred while creating a Microsoft Media Foundation player.\n";

		exClass = env->FindClass("nl/mpi/jmmf/JMMFException");
		//mid = env->GetMethodID(exClass, "<init>", "(Ljava/lang/String;)V");

		if (exClass != NULL) {
			env->ThrowNew(exClass, mes);
		} else {
			exClass = env->FindClass("java/lang/Exception");
			env->ThrowNew(exClass, mes);
		}

		delete(str);
		delete(jniu);
		fflush(stdout);// make sure messages make it to the Java world
		return (jlong)-1;
	}
	// store in map
	pm.put(nextId, dsp);
	// MMFPlayer maintains its own copy
	delete(str);
	delete(jniu);
	fflush(stdout);// make sure messages make it to the Java world
	return (jlong)nextId;
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    getFileType
 * Signature: (Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_nl_mpi_jmmf_JMMFPlayer_getFileType
	(JNIEnv *env, jobject thisObj, jstring file) {
	return NULL;
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    isVisualMedia
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_nl_mpi_jmmf_JMMFPlayer_isVisualMedia
	(JNIEnv *env, jobject thisObj, jlong id) {
	if (pm.containsKey((long)id)) {
		MMFPlayer* dsp = pm.get((long)id);

		if (dsp->isVisualMedia()) {
			return JNI_TRUE;
		}
	} else {
		printf("MMF JNI isVisualMedia: Player not found, id %lld\n", id);
	}
	fflush(stdout);
	return JNI_FALSE;
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    setStopTime
 * Signature: (JJ)V
 */
JNIEXPORT void JNICALL Java_nl_mpi_jmmf_JMMFPlayer_setStopTime
	(JNIEnv *env, jobject thisObj, jlong id, jlong time) {
		if (pm.containsKey((long)id)) {
		MMFPlayer* dsp = pm.get((long)id);

		HRESULT hr = dsp->setStopPosition((__int64) time);
		if (FAILED(hr)) {
			printf("MMF JNI setStopTime: failed to set the stop time.\n");
			// throw an exception?
		}

	} else {
		printf("MMF JNI setStopTime: Player not found, id %lld\n", id);
	}
	fflush(stdout);
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    getStopTime
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_nl_mpi_jmmf_JMMFPlayer_getStopTime
	(JNIEnv *env, jobject thisObj, jlong id) {
	jlong stopTime = 1;
	if (pm.containsKey((long)id)) {
		MMFPlayer* dsp = pm.get((long)id);
		stopTime = (jlong) dsp->getStopPosition();
	} else {
		printf("MMF JNI getStopTime: Player not found, id %lld\n", id);
	}
	fflush(stdout);
	return stopTime;
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    getSourceHeight
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_nl_mpi_jmmf_JMMFPlayer_getSourceHeight
	(JNIEnv *env, jobject thisObj, jlong id) {
	jint sheight = 1;
	if (pm.containsKey((long)id)) {
		MMFPlayer* dsp = pm.get((long)id);
		sheight = (jint) dsp->getOrgVideoHeight();
	} else {
		printf("MMF JNI getSourceHeight: Player not found, id %lld\n", id);
	}
	fflush(stdout);
	return sheight;
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    getSourceWidth
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_nl_mpi_jmmf_JMMFPlayer_getSourceWidth
	(JNIEnv *env, jobject thisObj, jlong id) {
	jint swidth = 1;
	if (pm.containsKey((long)id)) {
		MMFPlayer* dsp = pm.get((long)id);
		swidth = dsp->getOrgVideoWidth();
	} else {
		printf("MMF JNI getSourceWidth: Player not found, id %lld\n", id);
	}
	fflush(stdout);
	return swidth;
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    getPreferredAspectRatio
 * Signature: (J)[I
 */
JNIEXPORT jintArray JNICALL Java_nl_mpi_jmmf_JMMFPlayer_getPreferredAspectRatio
	(JNIEnv *env, jobject thisObj, jlong id) {
	jintArray iArray;
	jfloat aspectRatio = 1;
	if (pm.containsKey((long)id)) {
		MMFPlayer* dsp = pm.get((long)id);
		long x;
		long y;
		HRESULT hr = dsp->getPreferredAspectRatio(&x, &y);
		if (SUCCEEDED(hr)) {
			jboolean isCopy;
			iArray = env->NewIntArray(2);
			jint *pIA = env->GetIntArrayElements(iArray, &isCopy);
			pIA[0] = (jint) x;
			pIA[1] = (jint) y;

			env->ReleaseIntArrayElements(iArray, pIA, 0);
		} else {
			printf("MMF JNI getPreferredAspectRatio: cannot get the preferred aspect ratio.\n");
		}
	} else {
		printf("MMF JNI getPreferredAspectRatio: Player not found, id %lld\n", id);
	}
	fflush(stdout);
	return iArray;
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    getCurrentImage
 * Signature: (J)[B
 */
JNIEXPORT jbyteArray JNICALL Java_nl_mpi_jmmf_JMMFPlayer_getCurrentImage
	(JNIEnv *env, jobject thisObj, jlong id, jobject header) {
	jbyteArray bArray;
	if (pm.containsKey((long)id)) {
		MMFPlayer* dsp = pm.get((long)id);
       
		if (!dsp->isPlaying()) {
			if (MMFUtil::JMMF_DEBUG) {
				printf("MMF JNI Info: getCurrentImage: Player is paused.\n");
			}
			fflush(stdout);
			HRESULT hr;
			BITMAPINFOHEADER biHeader;
			biHeader.biSize = sizeof(BITMAPINFOHEADER);
			BYTE *pDibData; 
			DWORD dibSize;

			hr = dsp->getCurrentImage(&biHeader, &pDibData, &dibSize);
			if (SUCCEEDED(hr)) {
				// convert to a jbyteArray
				int size = (int) dibSize;
				bArray = env->NewByteArray((jsize) size);
				jint isize = env->GetArrayLength(bArray);
				//printf("Size of new byte array: %d\n", isize);
				jboolean isCopy;
				jbyte* pByteArray;
				pByteArray = env->GetByteArrayElements(bArray, &isCopy);

				for (int i = 0; i < size; i++) {
					pByteArray[i] = (jbyte) pDibData[i];
				}
				if (MMFUtil::JMMF_DEBUG) {
					printf("MMF JNI: getCurrentImage: Image array copied to jbyte array.\n");
				}
				fflush(stdout);
				//if(isCopy == JNI_TRUE) { // always release?
					env->ReleaseByteArrayElements(bArray, pByteArray, 0);
				//}
				
				//free(pDibData);
				CoTaskMemFree(pDibData);
				if (header != NULL) {
					jclass headerClass = env->GetObjectClass(header);
					jfieldID fid = env->GetFieldID(headerClass, "size", "J");
					if (fid != NULL) {
						env->SetLongField(header, fid, (jlong) biHeader.biSize);
					}
					fid = env->GetFieldID(headerClass, "width", "I");
					if (fid != NULL) {
						env->SetIntField(header, fid, (jint) biHeader.biWidth);
					}
					fid = env->GetFieldID(headerClass, "height", "I");
					if (fid != NULL) {
						env->SetIntField(header, fid, (jint) biHeader.biHeight);
					}
					fid = env->GetFieldID(headerClass, "planes", "I");
					if (fid != NULL) {
						env->SetIntField(header, fid, (jint) biHeader.biPlanes);
					}
					fid = env->GetFieldID(headerClass, "bitCount", "I");
					if (fid != NULL) {
						env->SetIntField(header, fid, (jint) biHeader.biBitCount);
					}
					fid = env->GetFieldID(headerClass, "compression", "I");
					if (fid != NULL) {
						env->SetIntField(header, fid, (jint) biHeader.biCompression);
					}
					fid = env->GetFieldID(headerClass, "sizeImage", "J");
					if (fid != NULL) {
						env->SetLongField(header, fid, (jlong) biHeader.biSizeImage);
					}
					fid = env->GetFieldID(headerClass, "xPelsPerMeter", "J");
					if (fid != NULL) {
						env->SetLongField(header, fid, (jlong) biHeader.biXPelsPerMeter);
					}
					fid = env->GetFieldID(headerClass, "yPelsPerMeter", "J");
					if (fid != NULL) {
						env->SetLongField(header, fid, (jlong) biHeader.biYPelsPerMeter);
					}
					fid = env->GetFieldID(headerClass, "clrUsed", "Z");
					if (fid != NULL) {
						env->SetBooleanField(header, fid, (jboolean) biHeader.biClrUsed);
					}
					fid = env->GetFieldID(headerClass, "clrImportant", "Z");
					if (fid != NULL) {
						env->SetBooleanField(header, fid, (jboolean) biHeader.biClrImportant);
					}
					//printf("MMF JNI: Size of image buffer: %ld\n", dibSize);
					//printf("MMF JNI: Header, bit count %ld\n", biHeader.biBitCount);
					//printf("MMF JNI: Header, image width %ld\n", biHeader.biWidth);
					//printf("MMF JNI: Header, image height %ld\n", biHeader.biHeight);
					//printf("MMF JNI: Header, size %ld\n", biHeader.biSize);
					//printf("MMF JNI: Header, size image %ld\n", biHeader.biSizeImage);
					//printf("MMF JNI: Header, compression %ld\n", biHeader.biCompression);
					//printf("MMF JNI: Header, no. planes %ld\n", biHeader.biPlanes);
					//fflush(stdout);
				}
			} else {
				printf("MMF JNI: getCurrentImage: No buffer created, no buffer filled: %d.\n", hr);
				bArray = NULL;
			}
		} else {
			printf("MMF JNI: getCurrentImage: The player is not paused...\n");
			// stop the player or return an error? 
			bArray = NULL;
		}
	} else {
		printf("MMF JNI: getCurrentImage: Player not found, id %lld\n", id);
		bArray = NULL;
	}
	printf("MMF JNI: getCurrentImage: Returning image byte array.\n");
	fflush(stdout);
	return bArray;
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    getImageAtTime
 * Signature: (JJ)[B
 */
JNIEXPORT jbyteArray JNICALL Java_nl_mpi_jmmf_JMMFPlayer_getImageAtTime
	(JNIEnv *env, jobject thisObj, jlong id, jobject header, jlong time) {
	return NULL;// TO DO
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    repaintVideo
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_nl_mpi_jmmf_JMMFPlayer_repaintVideo
	(JNIEnv *env, jobject thisObj, jlong id) {
	if (pm.containsKey((long)id)) {
		MMFPlayer* dsp = pm.get((long)id);
		dsp->repaintVideo();
	} else {
		printf("MMF JNI repaintVideo: Player not found, id %lld\n", id);
	}
	fflush(stdout);
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    enableDebugMode
 * Signature: (Z)V
 */
JNIEXPORT void JNICALL Java_nl_mpi_jmmf_JMMFPlayer_enableDebugMode
	(JNIEnv *env, jclass clazz, jboolean debug) {
	printf("MMF JNI enableDebugMode: setting debug mode: %d\n", debug);
	MMFUtil::JMMF_DEBUG = (bool) debug;
	fflush(stdout);
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    correctAtPause
 * Signature: (Z)V
 */
JNIEXPORT void JNICALL Java_nl_mpi_jmmf_JMMFPlayer_correctAtPause
	(JNIEnv *env, jclass clazz, jboolean correct) {
	printf("MMF JNI correctAtPause: correct media position when pausing the player: %d\n", correct);
	if ((bool) correct) {
		MMFUtil::JMMF_CORRECT_AT_PAUSE = TRUE;
	} else {
		MMFUtil::JMMF_CORRECT_AT_PAUSE = FALSE;
	}
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    clean
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_nl_mpi_jmmf_JMMFPlayer_clean
	(JNIEnv *env, jobject thisObj, jlong id) {
	if (pm.containsKey((long)id)) {
		MMFPlayer* dsp = pm.remove((long)id);
		// instead of calling cleanUpOnClose rely on the destructor of MMFPlayer
		delete (dsp);
		/*
		HRESULT hr = dsp->cleanUpOnClose();
		if (SUCCEEDED(hr)) {
			printf("MMF JNI clean: Deleting Player, id %d\n", id);
			delete (dsp);
		}
		*/
	} else {
		printf("MMF JNI clean: Player not found, id %lld\n", id);
	}
	fflush(stdout);
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    closeSession
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_nl_mpi_jmmf_JMMFPlayer_closeSession
	(JNIEnv *env, jobject thisObj, jlong id) {
	if (pm.containsKey((long)id)) {
		MMFPlayer* dsp = pm.get((long)id);
		HRESULT hr = dsp->CloseSession();

		if (FAILED(hr)) {
			printf("MMF JNI closeSession: Closing Session failed, id %lld\n", id);
		}
	} else {
		printf("MMF JNI clean: Player not found, id %lld\n", id);
	}
	fflush(stdout);
}

/*
 * Class:     nl_mpi_jmmf_JMMFPlayer
 * Method:    deletePlayer
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_nl_mpi_jmmf_JMMFPlayer_deletePlayer
	(JNIEnv *env, jobject thisObj, jlong id) {
	if (pm.containsKey((long)id)) {
		MMFPlayer* dsp = pm.remove((long)id);
		// instead of calling cleanUpOnClose rely on the destructor of MMFPlayer
		delete (dsp);

	} else {
		printf("MMF JNI deletePlayer: Player not found, id %lld\n", id);
	}
	fflush(stdout);
}

#ifdef __cplusplus
}
#endif

