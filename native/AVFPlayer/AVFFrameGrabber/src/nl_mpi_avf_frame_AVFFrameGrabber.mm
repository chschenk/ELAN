#import "nl_mpi_avf_frame_AVFFrameGrabber.h"
#import <jni.h>
#import <jawt_md.h>
#import <Foundation/Foundation.h>
#import <AVFoundation/AVFoundation.h>


static jlong grabberId = 10000;
static NSMutableDictionary *grabberDict;

/*
 * Creates an AVImageGenerator for the video url, generates one image and sets 
 * some values in the Java class.
 *
 * Class:     nl_mpi_avf_frame_AVFFrameGrabber
 * Method:    initNativeAsset
 * Signature: (Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_nl_mpi_avf_frame_AVFFrameGrabber_initNativeAsset
    (JNIEnv *env, jobject callerObject, jstring videoURL) {
        // a global dictionary for storing pointers to a generator per video player
        jlong nextId = grabberId++;
        if (grabberDict == NULL) {
             NSLog(@"AVFFrameGrabber: Initialize a frame image generator Dictionary");
            grabberDict = [[NSMutableDictionary alloc] init];
        }
        
        // initialize assets
        const char *videoURLChars = env->GetStringUTFChars(videoURL, NULL);
        // convert jstring to NSString
        NSString *urlString = [NSString stringWithUTF8String:videoURLChars];
        NSLog(@"AVF:initNativeAsset: Video URL: %@", urlString);
        NSURL *videoNSURL = [NSURL fileURLWithPath:urlString isDirectory:NO];
        
        AVURLAsset *urlAsset = [AVURLAsset URLAssetWithURL:videoNSURL options:nil];
        // synchronous call to get the duration
        CMTime cmDuration = [urlAsset duration];
        NSLog(@"AVF:initNativeAsset: Duration: %lld / %d", cmDuration.value, cmDuration.timescale);
        
        // set duration field in the Java object
        jclass classRef = env->GetObjectClass(callerObject);
        jfieldID durFieldId = env->GetFieldID(classRef, "videoDuration", "J");
        env->SetLongField(callerObject, durFieldId, (jlong) ((cmDuration.value / cmDuration.timescale) * 1000));

        // check for video tracks, the first one will be used by the generator
        NSArray *trackArray = [urlAsset tracks];
        NSLog(@"AVF:initNativeAsset: Track count: %ld", [trackArray count]);
        
        NSArray *videoTrackArray = [urlAsset tracksWithMediaType:AVMediaTypeVideo];
        NSLog(@"AVF:initNativeAsset: Video track count: %lu", [videoTrackArray count]);
        
        if ([videoTrackArray count] > 0) {
            for (int i = 0; i < [videoTrackArray count]; i++) {
                AVAssetTrack *videoTrack = videoTrackArray[i];
                CGSize natSize = [videoTrack naturalSize];
                NSLog(@"AVF:initNativeAsset: Video track %d: width: %f, height %f", i, natSize.width, natSize.height);
                if (i == 0) {
                    jfieldID vidWidthId = env->GetFieldID(classRef, "videoWidth", "I");
                    env->SetIntField(callerObject, vidWidthId, natSize.width);
                    jfieldID vidHeightId = env->GetFieldID(classRef, "videoHeight", "I");
                    env->SetIntField(callerObject, vidHeightId, natSize.height);
                }
            }
            
            AVAssetImageGenerator *imageGenerator = [AVAssetImageGenerator assetImageGeneratorWithAsset:urlAsset];
            imageGenerator.requestedTimeToleranceBefore = kCMTimeZero;
            imageGenerator.requestedTimeToleranceAfter = kCMTimeZero;
            // check
            //NSLog(@"AVF:initNativeAsset: image generator time tolerance before: %d", CMTimeCompare([imageGenerator requestedTimeToleranceBefore], kCMTimeZero));
//            NSString *nextIdString = [NSString stringWithFormat:@"%ld", nextId];
//            [grabberDict setObject:imageGenerator forKey:nextIdString];
            
            // extract a random image to initialize some fields
            CMTime reqTime = CMTimeMake(cmDuration.value / 2, cmDuration.timescale);
            CMTime retTime;
            NSError *imageError = nil;
            
            CGImage *image = [imageGenerator copyCGImageAtTime:reqTime actualTime:&retTime error:&imageError];
            if (imageError == nil) {
                NSString *nextIdString = [NSString stringWithFormat:@"%ld", nextId];
                [grabberDict setObject:imageGenerator forKey:nextIdString];
                // bits per component
                size_t bitsPerComp = CGImageGetBitsPerComponent(image);
                NSLog(@"AVF:Image Info: bitsPerComponent: %ld", bitsPerComp);
                jfieldID bitsPerCompId = env->GetFieldID(classRef, "numBitsPerPixelComponent", "I");
                env->SetIntField(callerObject, bitsPerCompId, bitsPerComp);
                // bits per pixel
                size_t bitsPerPixel = CGImageGetBitsPerPixel(image);
                NSLog(@"AVF:Image Info: bitsPerPixel: %ld", bitsPerPixel);
                jfieldID bitsPerPixId = env->GetFieldID(classRef, "numBitsPerPixel", "I");
                env->SetIntField(callerObject, bitsPerPixId, bitsPerPixel);
                // bytes per image row
                size_t bytesPerRow = CGImageGetBytesPerRow(image);
                NSLog(@"AVF:Image Info: bytesPerRow: %ld", bytesPerRow);
                jfieldID bytesPerRowId = env->GetFieldID(classRef, "numBytesPerRow", "I");
                env->SetIntField(callerObject, bytesPerRowId, bytesPerRow);
                // image width and height
                size_t imageWidth = CGImageGetWidth(image);
                size_t imageHeight = CGImageGetHeight(image);
                NSLog(@"AVF:Image Info: imageWidth: %ld : imageHeight: %ld", imageWidth, imageHeight);
                jfieldID imgWidthId = env->GetFieldID(classRef, "imageWidth", "I");
                env->SetIntField(callerObject, imgWidthId, imageWidth);
                jfieldID imgHeightId = env->GetFieldID(classRef, "imageHeight", "I");
                env->SetIntField(callerObject, imgHeightId, imageHeight);
                
                // alpha info
                CGImageAlphaInfo alpha = CGImageGetAlphaInfo(image);
                jfieldID alphInfoId = env->GetFieldID(classRef, "alphaInfo", "Ljava/lang/String;");
                jstring alphaInfoString;
                if (alpha == kCGImageAlphaNone) {
                    NSLog(@"AVF:Image Info: Alpha Info: kCGImageAlphaNone (RGB)");
                    alphaInfoString = env->NewStringUTF("kCGImageAlphaNone");
                } else if (alpha == kCGImageAlphaPremultipliedFirst) {
                    NSLog(@"AVF:Image Info: Alpha Info: kCGImageAlphaPremultipliedFirst (ARGB)");
                    alphaInfoString = env->NewStringUTF("kCGImageAlphaPremultipliedFirst");
                } else if (alpha == kCGImageAlphaPremultipliedLast) {
                    NSLog(@"AVF:Image Info: Alpha Info: kCGImageAlphaPremultipliedLast (RGBA)");
                    alphaInfoString = env->NewStringUTF("kCGImageAlphaPremultipliedLast");
                } else if (alpha == kCGImageAlphaFirst) {
                    NSLog(@"AVF:Image Info: Alpha Info: kCGImageAlphaFirst");
                    alphaInfoString = env->NewStringUTF("kCGImageAlphaFirst");
                } else if (alpha == kCGImageAlphaLast) {
                    NSLog(@"AVF:Image Info: Alpha Info: kCGImageAlphaLast");
                    alphaInfoString = env->NewStringUTF("kCGImageAlphaLast");
                }
                if (alphaInfoString != NULL) {
                    env->SetObjectField(callerObject, alphInfoId, alphaInfoString);
                    // the following results in a crash:
                    // Invalid memory access of location 0x7f8d83809a8 rip=0x7fff8bb740dd
                    //env->DeleteLocalRef(alphaInfoString);
                }
                // bitmap info
                CGBitmapInfo bitmapInfo = CGImageGetBitmapInfo(image);
                jfieldID bitmapInfoId = env->GetFieldID(classRef, "bitmapInfo", "Ljava/lang/String;");
                jstring bitmapInfoString;
                if (bitmapInfo == kCGBitmapAlphaInfoMask) {
                    NSLog(@"AVF:Image Info: Bitmap Info: kCGBitmapAlphaInfoMask");
                    bitmapInfoString = env->NewStringUTF("kCGBitmapAlphaInfoMask");
                } else if (bitmapInfo == kCGBitmapFloatComponents) {
                    NSLog(@"AVF:Image Info: Bitmap Info: kCGBitmapFloatComponents");
                    bitmapInfoString = env->NewStringUTF("kCGBitmapFloatComponents");
                } else if (bitmapInfo == kCGBitmapByteOrderMask) {
                    NSLog(@"AVF:Image Info: Bitmap Info: kCGBitmapByteOrderMask");
                    bitmapInfoString = env->NewStringUTF("kCGBitmapByteOrderMask");
                } else if (bitmapInfo == kCGBitmapByteOrderDefault) {
                    NSLog(@"AVF:Image Info: Bitmap Info: kCGBitmapByteOrderDefault");
                    bitmapInfoString = env->NewStringUTF("kCGBitmapByteOrderDefault");
                } else if (bitmapInfo == kCGBitmapByteOrder16Little) {
                    NSLog(@"AVF:Image Info: Bitmap Info: kCGBitmapByteOrder16Little");
                    bitmapInfoString = env->NewStringUTF("kCGBitmapByteOrder16Little");
                } else if (bitmapInfo == kCGBitmapByteOrder32Little) {
                    NSLog(@"AVF:Image Info: Bitmap Info: kCGBitmapByteOrder32Little");
                    bitmapInfoString = env->NewStringUTF("kCGBitmapByteOrder32Little");
                } else if (bitmapInfo == kCGBitmapByteOrder16Big) {
                    NSLog(@"AVF:Image Info: Bitmap Info: kCGBitmapByteOrder16Big");
                    bitmapInfoString = env->NewStringUTF("kCGBitmapByteOrder16Big");
                } else if (bitmapInfo == kCGBitmapByteOrder32Big) {
                    NSLog(@"AVF:Image Info: Bitmap Info: kCGBitmapByteOrder32Big");
                    bitmapInfoString = env->NewStringUTF("kCGBitmapByteOrder32Big");
                } else {
                    NSLog(@"AVF:Image Info: Bitmap Info Other: %d", bitmapInfo);
                    bitmapInfoString = env->NewStringUTF("Other Bitmap Info");
                }
                if (bitmapInfoString != NULL) {
                    env->SetObjectField(callerObject, bitmapInfoId, bitmapInfoString);
                    //env->DeleteLocalRef(bitmapInfoString);
                }
                
                // color space
                CGColorSpaceRef colorSpace = CGImageGetColorSpace(image);
                if (colorSpace != NULL) {
                    CGColorSpaceModel csModel = CGColorSpaceGetModel(colorSpace);
                    CFStringRef spaceName = CGColorSpaceCopyName(colorSpace);
                    NSLog(@"AVF:Color Space: Name: %@ Model: %d", spaceName, csModel);
                    if (spaceName != NULL) {
                        CFRelease(spaceName);
                    }
                    jfieldID colorModelId = env->GetFieldID(classRef, "colorModelCG", "Ljava/lang/String;");
                    jstring colorModelString;
                    if (csModel == kCGColorSpaceModelUnknown) {
                        NSLog(@"AVF:Space Model: kCGColorSpaceModelUnknown");
                        colorModelString = env->NewStringUTF("kCGColorSpaceModelUnknown");
                    } else if (csModel == kCGColorSpaceModelMonochrome) {
                        NSLog(@"AVF:Space Model: kCGColorSpaceModelMonochrome");
                        colorModelString = env->NewStringUTF("kCGColorSpaceModelMonochrome");
                    } else if (csModel == kCGColorSpaceModelRGB) {
                        NSLog(@"AVF:Space Model: kCGColorSpaceModelRGB");
                        colorModelString = env->NewStringUTF("kCGColorSpaceModelRGB");
                    } else if (csModel == kCGColorSpaceModelCMYK) {
                        NSLog(@"AVF:Space Model: kCGColorSpaceModelCMYK");
                        colorModelString = env->NewStringUTF("kCGColorSpaceModelCMYK");
                    } else if (csModel == kCGColorSpaceModelLab) {
                        NSLog(@"AVF:Space Model: kCGColorSpaceModelLab");
                        colorModelString = env->NewStringUTF("kCGColorSpaceModelLab");
                    } else if (csModel == kCGColorSpaceModelDeviceN) {
                        NSLog(@"AVF:Space Model: kCGColorSpaceModelDeviceN");
                        colorModelString = env->NewStringUTF("kCGColorSpaceModelDeviceN");
                    } else if (csModel == kCGColorSpaceModelIndexed) {
                        NSLog(@"AVF:Space Model: kCGColorSpaceModelIndexed");
                        colorModelString = env->NewStringUTF("kCGColorSpaceModelIndexed");
                    } else if (csModel == kCGColorSpaceModelPattern) {
                        NSLog(@"AVF:Space Model: kCGColorSpaceModelPattern");
                        colorModelString = env->NewStringUTF("kCGColorSpaceModelPattern");
                    }
                    if (colorModelString != NULL) {
                        env->SetObjectField(callerObject, colorModelId, colorModelString);
                        //env->DeleteLocalRef(colorModelString);
                    }
                    //NSLog(@"Color space retain count: %ld", CFGetRetainCount(colorSpace));// mostly 2 
                    // we got this through a Get, no need to release following the Get Rule?
                    //CGColorSpaceRelease(colorSpace);
                } else {
                    NSLog(@"AVF:Color Space is NULL. Image Mask");
                }
                
                //NSLog(@"Image retain count: %ld", CFGetRetainCount(image));
                // finally release the image
                // some crashes have been observed at this point
                // invalid memory access, even if retaincount was 1
                CGImageRelease(image);
            } else {
                // give up on this image generator?
                nextId = -1;
                NSLog(@"AVF:initNativeAsset: Error while extracting image: %@", [imageError localizedDescription]);
            }
            
        } else {
            nextId = -1;
        }
        
        env->ReleaseStringUTFChars(videoURL, videoURLChars);
        return nextId;
    }

/*
 * Variant of grabbing the bytes of a frame image based on a java.nio.ByteBuffer.
 * Class:     nl_mpi_avf_frame_AVFFrameGrabber
 * Method:    grabVideoFrame
 * Signature: (JJLjava/nio/ByteBuffer;)I
 */
JNIEXPORT jint JNICALL Java_nl_mpi_avf_frame_AVFFrameGrabber_grabVideoFrame
    (JNIEnv *env, jobject callerObject, jlong grabberId, jlong mediaTime, jobject buffer) {
        if (grabberDict == NULL) {
            NSLog(@"AVF:grabVideoFrame: No image generator Dictionary");
            return 0;
        }
        
        NSString *idString = [NSString stringWithFormat:@"%ld", grabberId];
        AVAssetImageGenerator *imageGenerator = (AVAssetImageGenerator *)[grabberDict valueForKey:idString];
        
        if (imageGenerator == NULL) {
            NSLog(@"AVF:grabVideoFrame: No image generator in the Dictionary with that key");
            return 0;
        }
        
        jbyte *bufAddress = (jbyte*) env->GetDirectBufferAddress(buffer);
        jlong bufCapacity = env->GetDirectBufferCapacity(buffer);
        NSLog(@"AVF:grabVideoFrame: ByteBuffer capacity: %ld", bufCapacity);
        // check size of buffer
        CMTime reqTime = CMTimeMake(mediaTime,  1000);
        CMTime retTime;
        NSError *imageError = nil;
        
        CGImage *image = [imageGenerator copyCGImageAtTime:reqTime actualTime:&retTime error:&imageError];
        if (imageError == nil) {
            NSLog(@"AVF:grabVideoFrame: request time: %ld, return time: %ld", mediaTime, (long) CMTimeGetSeconds(retTime) * 1000);
            // convert to jbyte array
            CGDataProviderRef imageProvider = CGImageGetDataProvider(image);
            CFDataRef dataRef = CGDataProviderCopyData(imageProvider);
            const UInt8 *dataBuffer = CFDataGetBytePtr(dataRef);
            long length = CFDataGetLength(dataRef);
            
            if (bufAddress != NULL){
                //env->SetByteArrayRegion((jbyteArray) bufAddress, 0, length, (const jbyte*) dataBuffer);
//                for (int j = 0; j < length && j < bufCapacity; j++) {
//                    bufAddress[j] = dataBuffer[j];
//                }
                memcpy(bufAddress, dataBuffer, length);
            }
            CFRelease(dataRef);
            CGImageRelease(image);
            
            return length;
        } else {
            NSLog(@"AVF:grabVideoFrame: Error while extracting image: %@", [imageError localizedDescription]);

        }
        
        return 0;
    }

/*
 * Variant of grabbing the bytes of a frame image based on a Java byte[] array.
 * Class:     nl_mpi_avf_frame_AVFFrameGrabber
 * Method:    grabVideoFrameBA
 * Signature: (JJ[B)I
 */
JNIEXPORT jint JNICALL Java_nl_mpi_avf_frame_AVFFrameGrabber_grabVideoFrameBA
    (JNIEnv *env, jobject callerObject, jlong grabberId, jlong mediaTime, jbyteArray byteArr)  {
        if (grabberDict == NULL) {
            NSLog(@"AVF:grabVideoFrameBA: No image generator Dictionary");
            return 0;
        }
        
        NSString *idString = [NSString stringWithFormat:@"%ld", grabberId];
        AVAssetImageGenerator *imageGenerator = (AVAssetImageGenerator *)[grabberDict valueForKey:idString];
        
        if (imageGenerator == NULL) {
            NSLog(@"AVF:grabVideoFrameBA: No image generator in the Dictionary with that key");
            return 0;
        }
        
        jlong arrLength = env->GetArrayLength(byteArr);
        NSLog(@"AVF:grabVideoFrameBA: Byte array length: %ld", arrLength);
        // check size of buffer
        CMTime reqTime = CMTimeMake(mediaTime,  1000);
        CMTime retTime;
        NSError *imageError = nil;
        
        CGImage *image = [imageGenerator copyCGImageAtTime:reqTime actualTime:&retTime error:&imageError];
        if (imageError == nil) {
            NSLog(@"AVF:grabVideoFrame: request time: %ld, return time: %ld", mediaTime, (long) CMTimeGetSeconds(retTime) * 1000);
            // convert to jbyte array
            CGDataProviderRef imageProvider = CGImageGetDataProvider(image);
            CFDataRef dataRef = CGDataProviderCopyData(imageProvider);
            const UInt8 *dataBuffer = CFDataGetBytePtr(dataRef);
            long length = CFDataGetLength(dataRef);
            
            env->SetByteArrayRegion(byteArr, 0, length, (const jbyte*) dataBuffer);

            CFRelease(dataRef);
            CGImageRelease(image);
            
            return length;
        } else {
            NSLog(@"AVF:grabVideoFrameBA: Error while extracting image: %@", [imageError localizedDescription]);
            
        }
        
        return 0;
    }

/*
 * Saves the frame at the specified mediaTime to the specified imagePath using the CGImageDestination classes 
 * and functions. The format of the image is based on the extension of the specified imagePath 
 * (png (default), jpg, bmp)
 * Class:     nl_mpi_avf_frame_AVFFrameGrabber
 * Method:    saveFrameNativeAVF
 * Signature: (JLjava/lang/String;J)Z
 */
JNIEXPORT jboolean JNICALL Java_nl_mpi_avf_frame_AVFFrameGrabber_saveFrameNativeAVF
    (JNIEnv *env, jobject callerObject, jlong grabberId, jstring imagePath, jlong mediaTime) {
        if (grabberDict == NULL) {
            NSLog(@"AVF:saveFrameNativeAVF: No image generator Dictionary");
            return JNI_FALSE;
        }
        
        NSString *idString = [NSString stringWithFormat:@"%ld", grabberId];
        AVAssetImageGenerator *imageGenerator = (AVAssetImageGenerator *)[grabberDict valueForKey:idString];
        
        if (imageGenerator == NULL) {
            NSLog(@"AVF:saveFrameNativeAVF: No image generator in the Dictionary with that key");
            return JNI_FALSE;
        }
        
        const char *imageURLChars = env->GetStringUTFChars(imagePath, NULL);
        // convert jstring to NSString
        NSString *urlImgString = [NSString stringWithUTF8String:imageURLChars];
        NSLog(@"AVF:saveFrameNativeAVF: Image URL: %@", urlImgString);
        NSURL *imgNSURL = [NSURL fileURLWithPath:urlImgString isDirectory:NO];
        
        //CMTime reqTime = CMTimeMake((mediaTime / 1000) * cmDuration.timescale, cmDuration.timescale);
        CMTime reqTime = CMTimeMake(mediaTime,  1000);
        NSLog(@"AVF:saveFrameNativeAVF: Requested image time: %lld / %d", reqTime.value, reqTime.timescale);
        
        if (CMTimeCompare(reqTime, [[imageGenerator asset] duration]) == 1) {
            NSLog(@"AVF:saveFrameNativeAVF: The requested time is greater than the media duration");
            env->ReleaseStringUTFChars(imagePath, imageURLChars);
            return JNI_FALSE;
        }
        
        CMTime retTime;
        NSError *imageError = nil;
        jboolean writeSuccess = JNI_FALSE;
        CGImage *image = [imageGenerator copyCGImageAtTime:reqTime actualTime:&retTime error:&imageError];
        if (imageError == nil) {
            NSLog(@"AVF:grabVideoFrame: request time: %ld, return time: %lld", mediaTime, retTime.value);
            CFStringRef imageType = kUTTypePNG;
            const char *jpegType = ".jpg";
            NSString *jpgString = [NSString stringWithUTF8String:jpegType];
            const char *bmpType = ".bmp";
            NSString *bmpString = [NSString stringWithUTF8String:bmpType];
            if ([urlImgString hasSuffix:jpgString]) {
                imageType = kUTTypeJPEG;
            } else if ([urlImgString hasSuffix:bmpString]) {
                imageType = kUTTypeBMP;
            }
            // save to file
            CGImageDestinationRef destRef = CGImageDestinationCreateWithURL((CFURLRef)imgNSURL, imageType, 1, NULL);
            CGImageDestinationAddImage(destRef, image, NULL);
            writeSuccess = CGImageDestinationFinalize(destRef);
            NSLog(@"AVF:saveFrameNativeAVF: Image stored: %i", writeSuccess);
            
            CGImageRelease(image);
            CFRelease(destRef);
        } else {
            NSLog(@"AVF:saveFrameNativeAVF: Error extracting image from the video: %@", [imageError description]);
        }

        env->ReleaseStringUTFChars(imagePath, imageURLChars);
        return writeSuccess;
    }

/*
 * Removes the ImageGenerator with the specified id from the Dictionary.
 * Class:     nl_mpi_avf_frame_AVFFrameGrabber
 * Method:    release
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_nl_mpi_avf_frame_AVFFrameGrabber_release
    (JNIEnv *env, jobject callerObject, jlong grabberId) {
        if (grabberDict == NULL) {
            NSLog(@"AVF:release: No image generator Dictionary");
            return;
        }
        
        NSString *idString = [NSString stringWithFormat:@"%ld", grabberId];
        AVAssetImageGenerator *imageGenerator = (AVAssetImageGenerator *)[grabberDict valueForKey:idString];
        
        if (imageGenerator == NULL) {
            NSLog(@"AVF:release: No image generator in the Dictionary with that key");
            return;
        }
        
        [grabberDict removeObjectForKey:idString];
        delete(&imageGenerator);
    }
