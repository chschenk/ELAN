//
//  JavaQTMovieView.m
//  JavaQTMovieView
//
//  Created by Albert Russel on 12/30/08.
//  Copyright 2008 MPI - Max Planck Institute for Psycholinguistics, Nijmegen
//

#import "JavaQTMovieView.h"
#import "player_JavaQTMoviePlayer.h"
#import <Foundation/NSAutoreleasePool.h>

#define CREATE 0
#define START 1
#define STOP 2
#define FORWARD 3
#define BACKWARD 4
#define PLAY_SELECTION 5
#define SET_MEDIA_TIME 6
#define SET_RATE 7
#define SET_VOLUME 8
#define SET_SCREEN_XY 9
#define SET_DRAWING_VISIBLE 10
#define SET_DRAWING_PERIOD 11
#define ADD_DRAWING_ELEMENT 12
#define ADD_DRAWING_ELEMENT_LIST 13
#define REMOVE_DRAWING_ELEMENTS 14
#define ORGANIZE_DRAWING_ELEMENTS 15
#define SET_OFFSET 16
#define CREATE_DRAWING_VIEW 17
#define SET_MEDIA_TIME_DOUBLE 18
#define SET_VIDEO_SCALE_FACTOR 19
#define SET_VIDEO_DESTINATION_BOUNDS 20

#define CLEAN_UP 99



/*
 *  The callback function for the SetMovieDrawingCompleteProc from the CREATE block below
 *  This function gets called each time there is something changed in the video image.
 *  By informing the DrawingView that something has changed it makes sure the drawings are up to date.
 */
// replace with QTMovie etc. variants
/*
OSErr movieDrawingCompleteProc(Movie movie, DrawingView* drawingView) {
	long time = GetMovieTime(movie, nil);
	long scale = GetMovieTimeScale(movie);
	long millis = (1000 * time) / scale;
	
	// while the media player is playing only draw every drawingView->drawingPeriod frame
	if (drawingView->drawingPeriod > 1) {
		double frameRate;
		MovieGetStaticFrameRate(movie, &frameRate);
		long frameNumber = (long)(millis / (1000.0 / frameRate));
		if (GetMovieRate(movie) != 0 && frameNumber % drawingView->drawingPeriod != 0) {
			return noErr;
		}
	}
	
	drawingView->drawingTime = millis;
	[drawingView setNeedsDisplay:YES];
	return noErr;
}
*/

/**
 * called when a player stops because the end of movie is reached
 * If the player has slave players they are also stopped
 */
// replace with QTMovie etc. variants
/*
void atStopCallback(QTCallBack callBack, id *javaQTMovieView) {
	NSMutableArray* slaves = ((JavaQTMovieView*)javaQTMovieView)->stopSlaves;
	int nSlaves =[slaves count];
	int i;
	for (i = 0; i < nSlaves; i++) {
		JavaQTMovieView* view = [slaves objectAtIndex:i];
		[[view movie] stop];
	}
	
	// must reinstall the callback because it gets lost after usage
	CallMeWhen(((JavaQTMovieView*)javaQTMovieView)->stopCallBack, NewQTCallBackUPP(atStopCallback), (long)javaQTMovieView, triggerAtStop, 0, 0);
}
*/

@implementation JavaQTMovieView

+ (id) JavaQTMovieViewWithCaller:(jobject) caller env:(JNIEnv *)env {
	return [[[JavaQTMovieView alloc] initWithCaller:caller env:env] autorelease];
}

- (id) initWithCaller:(jobject)caller env:(JNIEnv *)env {	
	javaPeer = (*env)->NewGlobalRef(env, caller);
	
	NSRect rect = NSMakeRect(0.0, 0.0, 300.0, 300.0);
	self = [super initWithFrame: rect];
	return self;
}

//- (void) viewWillMoveToSuperview:(NSView *)newSuperview {
	// do anything?? set Movie null?
	//[super viewWillMoveToSuperview:newSuperview];//??
//}

- (void) keyDown: (NSEvent *)event {
	//NSLog(@"key down");
	[[self superview] keyDown: event];
}

- (void) mouseDown:(NSEvent *)event {
	//NSLog(@"mouse down");
	[[self superview] mouseDown: event];
}

- (void) mouseUp:(NSEvent *)event {
	//NSLog(@"mouse up");
	[[self superview] mouseUp: event];
}

- (void) rightMouseDown:(NSEvent *)event {
	//NSLog(@"right mouse down");
	[[self superview] rightMouseDown: event];
}

- (void)rightMouseUp:(NSEvent *)event {
    [[self superview] rightMouseUp: event];
}

- (void) mouseDragged:(NSEvent *)event {
    //NSLog(@"mouse dragged");
	[[self superview] mouseDragged: event];
}

- (NSMenu*) menuForEvent: (NSEvent*)theEvent {
	//NSLog(@"menu for event");
	return NULL;
}

- (void) scrollWheel:(NSEvent *)theEvent {
	//NSLog(@"scroll wheel event");
}

- (BOOL) isPlaying {
	if ([self movie] == nil) {
		return NO;
	}
	
	return [movie rate] != 0;
}

/*
 * By default the "frame" and the "bounds" are the same, coincide. When the video is zoomed into the "frame" becomes 
 * bigger than the bounds and the frame origin no longer necessarily coincides with the bounds' origin.
 * This function is called when the view is resized.
 
 * The frameRect parameter in fact sets the bounds of the view
 */
- (void) setFrame:(NSRect)frameRect {//NSLog(@"setFrame");
    //NSLog(@"setFrame %f, %f", frameRect.size.width, frameRect.size.height);
        
    if (videoScaleFactor >= 1.0f) {
        float cw  = [self frame].size.width; // current frame width
        float ch  = [self frame].size.height; // current frame height
        float cox = [self frame].origin.x; // current x origin of the frame
        float coy = [self frame].origin.y; // current y origin of the frame
        
        float nw = videoScaleFactor * frameRect.size.width;
        float nh = videoScaleFactor * frameRect.size.height;
        videoDestSize.width = nw;
        videoDestSize.height = nh;
        //[super setFrameSize:NSMakeSize(nw, nh)];
        [super setFrameSize:videoDestSize];
        
        // recalculate the origin as well
        if (cw != 0 && ch != 0) {
            float shiftFact = nw / cw;
            float nox = cox * shiftFact;
            float noy = coy * shiftFact;
            
            videoDestOrigin.x = roundf(nox);// round to an integer value?
            videoDestOrigin.y = roundf(noy);
            [super setFrameOrigin:videoDestOrigin];
        }
    } else {
        [super setFrame:frameRect];
    }
    // if drawing elements are actually used, the videoScaleFactor has to be taken into account as well
	if (drawingWindow != NULL) {
		[drawingWindow setFrame:NSMakeRect(frameOrigin.x, frameOrigin.y, frameRect.size.width, frameRect.size.height) display:YES];
	}
}

- (void) movieDidEndNotification:(NSNotification *)notification {
// unused(notification)
	//NSLog(@"Movie did end...");
	int nSlaves =[stopSlaves count];
	int i;
	for (i = 0; i < nSlaves; i++) {
		JavaQTMovieView* view = [stopSlaves objectAtIndex:i];
		[[view movie] stop];
	}
	[self movieTimeChangedNotification:notification];
}
/*
- (void)movieRateChangedNotification:(NSNotification *)notification {
	NSLog(@"Movie rate changed... %@", [movie attributeForKey:@"QTMovieURLAttribute"]);
	NSNumber *num = [[notification userInfo] objectForKey:@"QTMovieRateDidChangeNotificationParameter"];
	float rt = (float)[num floatValue];
	NSLog(@"Rate: %f", rt);
}
*/
/**
 * Fired at start, stop , forward, backward and a jump in time, BUT not at end of file or selection
 */
- (void) movieTimeChangedNotification:(NSNotification *)notification {
	// unused(notification)
	//NSLog(@"Movie time changed... %@", [movie attributeForKey:@"QTMovieURLAttribute"]);
	if (drawingView != NULL) {
		float curRate = [movie rate];
		//NSLog(@"Rate: %f", curRate);
		if (curRate == 0.0) {
			if (drawTimer == NULL || [drawTimer isValid] == NO) {
				[self updateDrawingView:nil];
			} else {
				[drawTimer invalidate];
				drawTimer = NULL;
				//[self updateDrawingView:nil];//?? force an update
			}
		} else {
			if (drawTimer == NULL || [drawTimer isValid] == NO) {
				// create a new timer and add it to the run loop, time interval is in seconds
				drawTimer = [NSTimer timerWithTimeInterval:statFrameDuration/1000 target:self selector:@selector(updateDrawingView:) userInfo:nil repeats:YES];				
				[[NSRunLoop currentRunLoop] addTimer:drawTimer forMode:(NSString *)kCFRunLoopCommonModes];				
			}
		}		
	}
}

- (double) calcFrameDuration {
	if (frameDurationDetected == YES) {
		return statFrameDuration;
	}
	
	//NSLog(@"Movie info: %@", [movie attributeForKey:@"QTMovieURLAttribute"]);
	//QTTime dur = [movie duration];
	NSArray *tracks = [movie tracks];
	QTTrack *track = nil;
	
	NSEnumerator *enumerator = [tracks objectEnumerator];
	while (track = [enumerator nextObject]) {
		// use the first enabled track with video
		if (! [track attributeForKey:QTTrackEnabledAttribute]) {
			continue;
		}
		
		if ([[track media] hasCharacteristic:QTMediaCharacteristicVisual]) {
			NSString *trackFormat = [track attributeForKey:QTTrackMediaTypeAttribute];
			if ([trackFormat isEqualToString:QTMediaTypeMPEG]) {
				// MPEG has a sample count of 1, detect otherwise
				NSLog(@"MPEG file, detect rate...");
				Data *data = (Data *) malloc(sizeof(Data));
				NSString * urlString = [[movie attributeForKey:@"QTMovieURLAttribute"] path];
				const char * fileNm = [urlString UTF8String];
				//NSLog(@"File name: %s", fileNm);
				FILE *file = fopen(fileNm, "r");
				int version = parse_mpeg(file, data);
				NSLog(@"MPEG version: %d", version);
				unsigned int fps = data->fps;
				if (fps != 0) {
					statFrameDuration = (double) 1000 / fps;
				}
				encWidth = data->width;
				encHeight = data->height;
				//NSLog(@"W: %d H: %d", encWidth, encHeight);
				//NSSize size;
				//[[movie attributeForKey: QTMovieNaturalSizeAttribute] getValue: &size];
				//NSLog(@"Natural W: %f H: %f", size.width, size.height);
				// log
				free(data);
				break;
			} else {
				NSNumber *sampleCount = [[track media] attributeForKey:QTMediaSampleCountAttribute];
				long scount = [sampleCount longValue];
				if (scount <= 1) {
					NSLog(@"Warning: sample count <= 1, cannot properly detect frame duration");
					statFrameDuration = 0;
					break;//?? or continue
				}
				//NSLog(@"Has Frame Rate: %d", [[track media] hasCharacteristic:QTMediaCharacteristicHasVideoFrameRate]);// YES = 1
				NSValue *rangevalue = [track attributeForKey:QTTrackRangeAttribute]; 
				QTTimeRange range = [rangevalue QTTimeRangeValue];
				QTTime durTime = range.duration; // ignore the begin time
				//NSLog(@"Track range %d, %d", durTime.timeValue, durTime.timeScale);
				long duration = 0;
				if (durTime.timeScale != 0) {
					duration = (1000 * durTime.timeValue) / durTime.timeScale;
				} else {
					duration = durTime.timeValue;
				}
                NSLog(@"Movie duration and sample count: %ld, %ld", duration, scount);
				statFrameDuration = (double) duration / scount;
				break;
			}
		}
		// prints all attributes
		//NSDictionary * alltribs = [track trackAttributes];
		//for (id key in alltribs) {
		//	NSLog(@"key: %@, value: %@", key, [alltribs objectForKey:key]);
		//}
		
		// prints all media attributes
		//NSDictionary * allattribs = [media mediaAttributes];
		//for (id key in allattribs) {
		//	NSLog(@"key: %@, value: %@", key, [allattribs objectForKey:key]);
		//}
	}
    // April 2014 store movie timescale
    movieTimeScale = [[movie attributeForKey:QTMovieTimeScaleAttribute] longValue];
    
	NSLog(@"Frame duration: %f", statFrameDuration);
	frameDurationDetected = YES;
	return statFrameDuration;
	//return 0;	
}

- (void) updateDrawingView:(NSTimer *) tTimer {
	if (drawingView == NULL) {
		return;
	}
	// tTimer unused
	//NSLog(@"Update view");
	long time = [movie currentTime].timeValue;
	long scale = [[movie attributeForKey:QTMovieTimeScaleAttribute] longValue];
	long millis = ((1000 * time) / scale) - offset;//??
	// while the media player is playing only draw every drawingView->drawingPeriod frame
	if (drawingView->drawingPeriod > 1) {
		long frameNumber = (long)(millis / statFrameDuration);
		if ([movie rate] != 0.0 && frameNumber % drawingView->drawingPeriod != 0) {
			return;//don't paint
		}
	}
	
	drawingView->drawingTime = millis;
	[drawingView setNeedsDisplay:YES];
}

// Messages from Java peer via CocoaComponent.sendMessage
// These messages are received asynchronously from Java on the main AppKit thread
- (void) awtMessage:(jint)messageID message:(jobject)message env:(JNIEnv *)env {
	switch (messageID) {
		case CREATE:
			; // needed for syntactic reasons
			//NSLog(@"create");
			const jchar* chars = (*env)->GetStringChars(env, message, NULL);
			NSString *cocoaPath = [NSString stringWithCharacters:(unichar *)chars length:(*env)->GetStringLength(env, message)];
			//NSLog(@"path: %@", cocoaPath);
			//BOOL canInit = [QTMovie canInitWithFile:cocoaPath]; // this does not detect invalid file types etc 
			NSError *initError = nil;
			movie = [[QTMovie alloc] initWithFile: cocoaPath error:&initError];
			//NSLog(@"Error code %d %@", [initError code], initError); //-2048 = "The  file is not a movie file"
			//movie = [[QTMovie alloc] initWithFile: cocoaPath error:nil];
			if ([initError code] == 0) {
				[self setMovie: movie];
				[self calcFrameDuration];
                //[self setControllerVisible:YES];// works
                //[self setZoomButtonsVisible:YES];// doesn't work
				initialized = YES;
			
				stopSlaves = [[NSMutableArray alloc] initWithCapacity:10]; 
				//NSLog(@"slave id=%ld", stopSlaves);
				//NSLog(@"slave ct=%ld", [stopSlaves count]);
			
				// Cocoa notifications, in 64 bits applications the the underlying quicktime movie of QTMovie is not accessible
				NSNotificationCenter *nc = [NSNotificationCenter defaultCenter];
			
				[nc addObserver:self selector:@selector(movieDidEndNotification:) name:@"QTMovieDidEndNotification" object:nil];
				//[nc addObserver:self selector:@selector(movieRateChangedNotification:) name:@"QTMovieRateDidChangeNotification" object:nil];
				[nc addObserver:self selector:@selector(movieTimeChangedNotification:) name:@"QTMovieTimeDidChangeNotification" object:nil];
			} else {
				NSLog(@"Error code %ld %@", (long)[initError code], initError); //-2048 = "The  file is not a movie file"
				[self setMovie: nil];// needed
				//initialized = NO;//?? or just remember that initialization has been attempted, but failed
				initialized = YES;
				[self setFillColor:[NSColor redColor]];
				/* this doesn't work obviously
				NSString* text = [NSString stringWithString:@"The  file is not supported"];
				
				NSString* fontName = [NSString stringWithString:@"Times-Roman"];
				CGFloat fontSize = 0.0;
				NSColor* fontColor = [NSColor yellowColor];
				NSFont* font = [NSFont fontWithName:fontName size:fontSize];
				NSLog(@"Font %@\n", font);
				
				NSMutableDictionary* attr = [[NSMutableDictionary alloc] init];
				[attr setObject:font forKey:NSFontAttributeName];
				[attr setObject:fontColor forKey:NSForegroundColorAttributeName];
				[text drawAtPoint:NSMakePoint(20, 20) withAttributes:attr];
				[self updateDrawingView:nil];
				[self setNeedsDisplay:YES];
				 */
			}
            videoScaleFactor = 1.0f;
						
			break;
		case START:
			; // needed for syntactic reasons
			//NSLog(@"start"); 
			if ([self movie] == nil) {
				return;
			}
			
			jsize len = (*env)->GetArrayLength(env, message);
			jlong* startParameterArr = (*env)->GetLongArrayElements(env, message, 0);
			long startTime = startParameterArr[0];
	
			// build the list of stop slaves to be stopped at the end of the movie
			[self->stopSlaves removeAllObjects];
			int i;
			for (i = 2; i < len; i++) {
				[self->stopSlaves addObject:(JavaQTMovieView*) startParameterArr[i]];
			}
			
			if (![self isPlaying]) {
				// start the listeners
				for (i = 1; i < len; i++) {
					QTMovieView* player = (QTMovieView*) startParameterArr[i];
					[[player movie] setAttribute: [NSNumber numberWithBool:NO] forKey:QTMoviePlaysSelectionOnlyAttribute];
					[[player movie] setCurrentTime: QTMakeTime(startTime + ((JavaQTMovieView*) player)->offset, 1000)];
				} 
				
				for (i = 1; i < len; i++) {
					QTMovieView* player = (QTMovieView*) startParameterArr[i];
					[[player movie] setRate: ((JavaQTMovieView*) player)->rate];
				} 
			}
			
			(*env)->ReleaseLongArrayElements(env, message, startParameterArr, 0);
			break;
		case STOP:
			; // needed for syntactic reasons
			//NSLog(@"stop");
			if ([self movie] == nil) {
				return;
			}
			
			
			len = (*env)->GetArrayLength(env, message);
			jlong* stopParameterArr = (*env)->GetLongArrayElements(env, message, 0);
			
			for (i = 0; i < len; i++) {
				QTMovieView* player = (QTMovieView*) stopParameterArr[i];
				[[player movie] stop];
			}
			
			QTMovieView* player = (QTMovieView*) stopParameterArr[0];
			QTTime frameTime = [[player movie] currentTime];
			// compensate for offset
			long frameTimeMs = ((1000 * frameTime.timeValue) / frameTime.timeScale) - ((JavaQTMovieView*) player)->offset;
			//[[player movie] stepForward];
			//[[player movie] stepBackward];

			for (i = 1; i < len; i++) {
				QTMovieView* player = (QTMovieView*) stopParameterArr[i];
				QTTime nextFrameTime = QTMakeTime(frameTimeMs + ((JavaQTMovieView*) player)->offset, 1000);
				[[player movie] setCurrentTime: nextFrameTime];
				//[[player movie] stepForward];
				//[[player movie] stepBackward];

			}
			
			(*env)->ReleaseLongArrayElements(env, message, stopParameterArr, 0);
			break;
		case FORWARD:
			//; // needed for syntactic reasons
			//NSLog(@"forward"); 
			if ([self movie] == nil) {
				return;
			}
			
			len = (*env)->GetArrayLength(env, message);
			jlong* forwardParameterArr = (*env)->GetLongArrayElements(env, message, 0);
			
			if (![self isPlaying]) {
				QTMovieView* player = (QTMovieView*) forwardParameterArr[0];
				[[player movie] stepForward];
				QTTime frameTime = [[player movie] currentTime];
                //NSLog(@"Frame forward from %lld, %ld", frameTime.timeValue, frameTime.timeScale);
				// compensate for offset
				long frameTimeMs = ((1000 * frameTime.timeValue) / frameTime.timeScale) - ((JavaQTMovieView*) player)->offset;
				
				int i;
				for (i = 1; i < len; i++) {
					QTMovieView* player = (QTMovieView*) forwardParameterArr[i];
					QTTime nextFrameTime = QTMakeTime(frameTimeMs + ((JavaQTMovieView*) player)->offset, 1000);
					[[player movie] setCurrentTime: nextFrameTime];
					//[[player movie] stepForward];
				}
			}
			
			(*env)->ReleaseLongArrayElements(env, message, forwardParameterArr, 0);
			break;
		case BACKWARD:
			//; // needed for syntactic reasons
			//NSLog(@"backward");
			if ([self movie] == nil) {
				return;
			}
			
			len = (*env)->GetArrayLength(env, message);
			jlong* backwardParameterArr = (*env)->GetLongArrayElements(env, message, 0);
			
			if (![self isPlaying]) {
				QTMovieView* player = (QTMovieView*) backwardParameterArr[0];
				[[player movie] stepBackward];
				QTTime frameTime = [[player movie] currentTime];
				// compensate for offset
				long frameTimeMs = ((1000 * frameTime.timeValue) / frameTime.timeScale) - ((JavaQTMovieView*) player)->offset;
				//[[player movie] stepBackward];
				
				int i;
				for (i = 1; i < len; i++) {
					QTMovieView* player = (QTMovieView*) backwardParameterArr[i];
					QTTime nextFrameTime = QTMakeTime(frameTimeMs + ((JavaQTMovieView*) player)->offset, 1000);
					[[player movie] setCurrentTime: nextFrameTime];
					//[[player movie] stepBackward];
				}
			}
			
			(*env)->ReleaseLongArrayElements(env, message, backwardParameterArr, 0);
			break;
		case PLAY_SELECTION:
			//; // needed for syntactic reasons
			//NSLog(@"play selection");
			if ([self movie] == nil) {
				return;
			}
			
			if ([self isPlaying]) {
				[movie stop];
			}
			
			len = (*env)->GetArrayLength(env, message);			
			jlong* selectionParameterArr = (*env)->GetLongArrayElements(env, message, 0);
			startTime = selectionParameterArr[0];
			long stopTime = selectionParameterArr[1];
			//NSLog(@"start-stop time %d - %d", startTime, stopTime);
			QTTime qtStartTime;
			QTTime qtDuration;
			QTTimeRange range;	
			
			for (i = 2; i < len; i++) {
				QTMovieView* player = (QTMovieView*) selectionParameterArr[i];
	
				qtStartTime = QTMakeTime((startTime + ((JavaQTMovieView*) player)->offset), 1000);
				qtDuration = QTMakeTime((stopTime - startTime), 1000);
				range = QTMakeTimeRange(qtStartTime, qtDuration);
				
				[[player movie] setSelection: range];
				[[player movie] setAttribute: [NSNumber numberWithBool:YES] forKey:QTMoviePlaysSelectionOnlyAttribute];
				[[player movie] setAttribute: [NSValue valueWithQTTimeRange:range] forKey:QTMovieSelectionAttribute];
				[[player movie] setCurrentTime: qtStartTime];
				
				//QTTime curTime = [[player movie] currentTime];
				//NSLog(@"current media time %d", ((1000 * curTime.timeValue) / curTime.timeScale));
				
				[(JavaQTMovieView *)player updateDrawingView:nil];
			}
			
			for (i = 2; i < len; i++) {
				QTMovieView* player = (QTMovieView*) selectionParameterArr[i];
				
				[[player movie] setRate: self->rate]; // starts the movie!!
			}
			
			(*env)->ReleaseLongArrayElements(env, message, selectionParameterArr, 0);
			break;
		case SET_MEDIA_TIME:
			//; // needed for syntactic reasons
			//NSLog(@"set media time");
			if ([self movie] == nil) {
				return;
			}
			len = (*env)->GetArrayLength(env, message);
			
			jlong* smtParameterArr = (*env)->GetLongArrayElements(env, message, 0);
			startTime = smtParameterArr[0];
			for (i = 1; i < len; i++) { 
				QTMovieView* player = (QTMovieView*) smtParameterArr[i];
				[[player movie] setCurrentTime: QTMakeTime(startTime + ((JavaQTMovieView*) player)->offset, 1000)]; 
			} 
			
			(*env)->ReleaseLongArrayElements(env, message, smtParameterArr, 0);
			break;
        case SET_MEDIA_TIME_DOUBLE:
			//; // needed for syntactic reasons
			//NSLog(@"set media time double");
			if ([self movie] == nil) {
				return;
			}
			len = (*env)->GetArrayLength(env, message);
			
			jlong* smtdParameterArr = (*env)->GetLongArrayElements(env, message, 0);
			startTime = smtdParameterArr[0];// the time in ms has been multiplied by 1000 by the caller
			for (i = 1; i < len; i++) {
				QTMovieView* player = (QTMovieView*) smtdParameterArr[i];
                if (movieTimeScale == 0) {
                    [[player movie] setCurrentTime: QTMakeTime(startTime + ((JavaQTMovieView*) player)->offset * 1000, 1000 * 1000)];
                } else {
                    double tvDouble = ((startTime + ((JavaQTMovieView*) player)->offset * 1000) / (double)1000000) * movieTimeScale;
                    //NSLog(@"Set Media Double, double time value %f", tvDouble);
                    long tvLong = (long) (ceil(tvDouble));
                    //NSLog(@"Set Media Double, long time value %ld", tvLong);
                    [[player movie] setCurrentTime: QTMakeTime(tvLong, movieTimeScale)];
                }
			}
			
			(*env)->ReleaseLongArrayElements(env, message, smtdParameterArr, 0);
			break;
		case SET_RATE:
			//; // needed for syntactic reasons
			//NSLog(@"rate");
			if ([self movie] == nil) {
				return;
			}
			
			len = (*env)->GetArrayLength(env, message);
			jlong* rateArr = (*env)->GetLongArrayElements(env, message, 0);
			float newRate = 100000.0f / rateArr[0];
			
			for (i = 1; i < len; i++) { 
				QTMovieView* player = (QTMovieView*) rateArr[i];
				((JavaQTMovieView*) player)->rate = newRate;
				if ([((JavaQTMovieView*) player) isPlaying]) {
					[[player movie] setRate: newRate];
				}
			}
			
			(*env)->ReleaseLongArrayElements(env, message, rateArr, 0);
			break;
		case SET_VOLUME:
			; // needed for syntactic reasons
			//NSLog(@"volume");
			if ([self movie] == nil) {
				return;
			}
			
			jfloat* volumeArr = (*env)->GetFloatArrayElements(env, message, 0);
			
			[movie setVolume: volumeArr[0]];
			
			(*env)->ReleaseFloatArrayElements(env, message, volumeArr, 0);
			break;
		case SET_SCREEN_XY:
			; // needed for syntactic reasons
			if ([self movie] == nil) {
				return;
			}
			
			jlong* xyParameterArr = (*env)->GetLongArrayElements(env, message, 0);	
			frameOrigin.x = xyParameterArr[0];
			frameOrigin.y = xyParameterArr[1];

			if (drawingWindow != NULL) {
				[drawingWindow setFrame:NSMakeRect(frameOrigin.x, frameOrigin.y, [self frame].size.width, [self frame].size.height) display:YES];
			}
        
			(*env)->ReleaseLongArrayElements(env, message, xyParameterArr, 0);
			break;
		case SET_DRAWING_VISIBLE:
			if (drawingView == NULL) return;
			
			jboolean* isVisible = (*env)->GetBooleanArrayElements(env, message, 0);
			if (isVisible[0]) {
				[drawingView setHidden:NO];
			} else {
				[drawingView setHidden:YES];
			}
			
			(*env)->ReleaseBooleanArrayElements(env, message, isVisible, 0);
			break;
		case SET_DRAWING_PERIOD:
			if (drawingView == NULL) return;
			
			jlong* periodParameterArr = (*env)->GetLongArrayElements(env, message, 0);	
			[drawingView setDrawingPeriod:periodParameterArr[0]];

			(*env)->ReleaseLongArrayElements(env, message, periodParameterArr, 0);
			break;
		case ADD_DRAWING_ELEMENT:
			if (drawingView == NULL) return;
			
			const jchar* addElementChars = (*env)->GetStringChars(env, message, NULL);
			NSString *addElementString = [NSString stringWithCharacters:(unichar *)addElementChars length:(*env)->GetStringLength(env, message)];
			
			[drawingView addElement:addElementString];
			
			(*env)->ReleaseStringChars(env, message, addElementChars);
			break;
		case ADD_DRAWING_ELEMENT_LIST:
			if (drawingView == NULL) return;
			
			//NSLog(@"add list");
						
			jsize nElements = (*env)->GetArrayLength(env, message);
			
			jboolean isCopy;
			int elementIndex;
			for (elementIndex = 0; elementIndex < nElements; elementIndex++) {
				jstring jstr = (jstring)((*env)->GetObjectArrayElement(env, message, elementIndex));
				const char* utf_string = (*env)->GetStringUTFChars(env, jstr, &isCopy);
				NSString *elementString = [NSString stringWithUTF8String:utf_string];
				
				[drawingView addElement:elementString];
				
				//if(isCopy == JNI_TRUE) {always release
					(*env)->ReleaseStringUTFChars(env, jstr, utf_string);
				//}
				(*env)->DeleteLocalRef(env, jstr);
			}
			
			break;
		case REMOVE_DRAWING_ELEMENTS:
			if (drawingView == NULL) return;
			
			const jchar* removeElementChars = (*env)->GetStringChars(env, message, NULL);
			NSString *removeElementString = [NSString stringWithCharacters:(unichar *)removeElementChars length:(*env)->GetStringLength(env, message)];
			
			[drawingView removeElements:removeElementString];
			
			(*env)->ReleaseStringChars(env, message, removeElementChars);
			break;
		case ORGANIZE_DRAWING_ELEMENTS: // not used now but could be necessary in the future
			if (drawingView == NULL) return;
			
			[drawingView organizeElements];
			
			break;
		case SET_OFFSET:
			; // needed for syntactic reasons
			if ([self movie] == nil) {
				return;
			}
			
			jlong* offsetParameterArr = (*env)->GetLongArrayElements(env, message, 0);
			
			offset = offsetParameterArr[0] > 0 ? offsetParameterArr[0] : 0;
			//NSLog(@"offset: %d", offset);
			(*env)->ReleaseLongArrayElements(env, message, offsetParameterArr, 0);
			break;
		case CREATE_DRAWING_VIEW:
			if (drawingView != NULL) {
				return;
			}
			if ([self movie] == nil) {
				return;
			}

			// try to create and add the drawing view
			if (initialized == YES) {
				 //NSLog(@"Create drawing view...");
				 NSPoint baseOrigin, screenOrigin;
				NSRect baseRect;
				 baseOrigin = NSMakePoint([self frame].origin.x, [self frame].origin.y);
				 screenOrigin = [[self window] convertBaseToScreen:baseOrigin];
				baseRect = [self frame];
				 //NSLog(@"x: %f   y: %f    w: %f    h: %f", screenOrigin.x, screenOrigin.y, [self frame].size.width, [self frame].size.height);
				 //adjust to size of movie view 
				 drawingWindow = [[[NSWindow alloc] initWithContentRect:NSMakeRect(screenOrigin.x, screenOrigin.y, baseRect.size.width, baseRect.size.height) 
				 styleMask:NSBorderlessWindowMask
				 backing:NSBackingStoreBuffered 
				 defer:NO] autorelease];
				 
				 
				 [drawingWindow setOpaque:NO];
				 [drawingWindow setHasShadow:NO];
				 [drawingWindow setIgnoresMouseEvents:YES];
				 [drawingWindow setBackgroundColor:[NSColor colorWithDeviceWhite:0 alpha:0.0]];
				 
				 //adjust to size of movie view
				 drawingView = [[[DrawingView alloc] initWithFrame:NSMakeRect(screenOrigin.x, screenOrigin.y, baseRect.size.width, baseRect.size.height)] autorelease];
				 [drawingView setAlphaValue: 0.0f];
				 
				 [drawingWindow setContentView:drawingView];
				 
				 [drawingWindow orderFront:self];
				 [[self window] addChildWindow:drawingWindow ordered:NSWindowAbove];
				 
				//[drawingView setNeedsDisplay:YES]; 
				// turning it on, set up a timer and add it to the run loop, time interval is in seconds, do this when starting the player
				//drawTimer = [NSTimer timerWithTimeInterval:statFrameDuration/1000 target:self selector:@selector(updateDrawingView:) userInfo:nil repeats:YES];				
				//[[NSRunLoop currentRunLoop] addTimer:drawTimer forMode:(NSString *)kCFRunLoopCommonModes];
				[self updateDrawingView:nil];
			}
			
			break;
        case SET_VIDEO_SCALE_FACTOR:
            ;
            jfloat* scaleParameterArr = (*env)->GetFloatArrayElements(env, message, JNI_FALSE);
            // try scaling from the center of the current visible video area
            float curVoX = videoDestOrigin.x;
            float curVoY = videoDestOrigin.y;
            float curVW = videoDestSize.width;
            float curVH = videoDestSize.height;
        
            float visRectW = [self visibleRect].size.width;
            float visRectH = [self visibleRect].size.height;
            // the point in video image coordinate sytem that's in the center of the visible rect
            float curCX = visRectW / 2 - curVoX; // curVoX <= 0
            float curCY = visRectH / 2 - curVoY; // curVoY <= 0
            float cxRatio = curVW > 0 ? (curCX / curVW) : 0.5;
            float cyRatio = curVH > 0 ? (curCY / curVH) : 0.5;
        
            if (scaleParameterArr[0] >= 1) {
                videoScaleFactor = scaleParameterArr[0];
                // resize and reposition the video
                if (videoScaleFactor == 1) {
                    videoDestOrigin.x = 0;
                    videoDestOrigin.y = 0;
                    videoDestSize.width = visRectW;
                    videoDestSize.height = visRectH;
                } else {
                    videoDestSize.width = roundf(visRectW * videoScaleFactor);
                    videoDestSize.height = roundf(visRectH * videoScaleFactor);
                    float nextCX = cxRatio * videoDestSize.width;
                    float nextCY = cyRatio * videoDestSize.height;
                    videoDestOrigin.x = roundf((visRectW / 2) - nextCX);
                    videoDestOrigin.y = roundf((visRectH / 2) - nextCY);
                    
                    // not sure if following checks are necessary here
                    if (videoDestOrigin.x > 0) {
                        videoDestOrigin.x = 0;
                    }
                    if (videoDestOrigin.y > 0) {
                        videoDestOrigin.y = 0;
                    }
                    if (videoDestOrigin.x + videoDestSize.width < visRectW) {
                        videoDestOrigin.x = visRectW - videoDestSize.width;
                    }
                    if (videoDestOrigin.y + videoDestSize.height < visRectH) {
                        videoDestOrigin.y = visRectH - videoDestSize.height;
                    }
                }
                
                [self setFrameOrigin:videoDestOrigin];
                [self setFrameSize:videoDestSize];
            }
            //NSLog(@"Set video scale factor: %f", videoScaleFactor);
                  
            (*env)->ReleaseFloatArrayElements(env, message, scaleParameterArr, JNI_FALSE);
            break;
        case SET_VIDEO_DESTINATION_BOUNDS:
            ;
            jint* boundsParameterArr = (*env)->GetIntArrayElements(env, message, JNI_FALSE);
            // expects x, y, w, h values for the video frame
            int nextX = boundsParameterArr[0];
            int nextY = boundsParameterArr[1];
            int nextW = boundsParameterArr[2];
            int nextH = boundsParameterArr[3];
            //NSLog(@"Set video frame bounds: %d, %d, %d ,%d", nextX, nextY, nextW, nextH);
            // perform checks. make sure the video frame always completely covers the view's bounds
            // or don't do this here, let the caller decided whether or not the video can be (partially) moved out of the bounds
            nextX = nextX > 0 ? 0 : nextX;
            if (nextX + nextW < [self visibleRect].size.width) {
                nextX = [self visibleRect].size.width - nextW;
            }
            nextY = nextY > 0 ? 0 : nextY;
            if (nextY + nextH < [self visibleRect].size.height) {
                nextY = [self visibleRect].size.height - nextH;
            }
            
            videoDestOrigin.x = nextX;
            videoDestOrigin.y = nextY;
            videoDestSize.width = nextW;
            videoDestSize.height = nextH;
            
            [self setFrameOrigin:videoDestOrigin];
            [self setFrameSize:videoDestSize];
            //[self setFrameOrigin:NSMakePoint(nextX, nextY)];
            //[self setFrameSize:NSMakeSize(nextW, nextH)];
            
            (*env)->ReleaseIntArrayElements(env, message, boundsParameterArr, JNI_FALSE);
            break;
		case CLEAN_UP:
			;
			//NSLog(@"clean");
			// HS:check this, is not really needed? [self setMovie: NULL];
			// remove notification observer, for all notifications
			[[NSNotificationCenter defaultCenter] removeObserver:self];

			if (movie != NULL) {
				[movie release];
				movie = NULL;
			}
			if (drawingView != NULL) {
				[drawingView removeElements:@"ALL"];
				[drawingView setHidden:YES]; // release gave exceptions
				// When handling CREATE_DRAWING_VIEW,
				// drawingView was autoreleased, but attached
				// to drawingWindow (also autoreleased), which
				// was attached to [self window]. Hopefully
				// each of those properly took owenership.
				// If not, this leaks.
				if (drawTimer != NULL) {
					[drawTimer invalidate];
				}
			}
			if (stopSlaves != NULL) {
				[stopSlaves release];
				stopSlaves = NULL;
			}
			if (javaPeer != NULL) {
				(*env)->DeleteGlobalRef(env, javaPeer);
				javaPeer = NULL;
			}
			[self autorelease];
			break;
		default:
			break;
	}
}

@end


jlong wrapped_createNSViewLongNative(JNIEnv *env, jobject caller) {
	JavaQTMovieView *view = [JavaQTMovieView JavaQTMovieViewWithCaller:caller env:env];

	// By convention, the view was autoreleased. However it will need to
	// exist for longer than the autorelease pool, so we must retain it. We
	// have our own temporary pool, because we're being called from the
	// Java GUI thread.  It will be released when handling the CLEAN_UP
	// message (on the AppKit thread).
	[view retain];
	
	[view setControllerVisible: NO];
	view->movie = NULL;
	view->rate = 1.0f;
	view->initialized = NO;
	jlong retValue = (jlong) view;
	
	return retValue;
}

/*
 * Class:     JavaQTMovieView
 * Method:    nativeCreateNSViewLong
 * Signature: ()J
 *
 * Use a NSAutoreleasePool to prevent multiple log messages like
 * 2014-12-02 10:45:25.152 java[3217:10403] *** __NSAutoreleaseNoPool(): Object 0x1006af580 of class JavaQTMovieView autoreleased with no pool in place - just leaking
 * To find out where they come from, set the environment variable
 * export NSAutoreleaseHaltOnNoPool=YES
 * to get a breakpoint or crash dump (hopefully with stack trace).
 * See http://stackoverflow.com/questions/2841808/how-to-break-on-nsautoreleasenopool
 * https://developer.apple.com/library/IOs/documentation/Cocoa/Reference/Foundation/Classes/NSAutoreleasePool_Class/index.html
 *
 * The pool is needed because this code is called from the Java GUI thread, not
 * the AppKit thread. Therefore it does not have a pool installed already.
 * Nevertheless several objects would be autoreleased in this method.
 *
 * The JavaQTMovieView is autoreleased by convention, but releasing this
 * temporary pool would release it prematurely. (Without the pool it would
 * simply never be released, and leak -- just like the error message says).
 * Therefore it is explicitly retain'ed and later explicitly [self release]ed
 * when handling the CLEAN_UP message.
 *
 * Just omitting this pool and hoping the JavaQTMovieView leaks is not
 * sufficient, since there are several other objects being autoreleased as
 * well.
 */
JNIEXPORT jlong JNICALL Java_player_JavaQTMoviePlayer_createNSViewLongNative(JNIEnv *env, jobject caller) {
    //NSLog(@"Entering Java_player_JavaQTMoviePlayer_createNSViewLongNative");
    NSAutoreleasePool *mPool = [[NSAutoreleasePool alloc] init];

    jlong result = wrapped_createNSViewLongNative(env, caller);

    [mPool release];
    //NSLog(@"Returning from Java_player_JavaQTMoviePlayer_createNSViewLongNative");
    return result;
}

JNIEXPORT jboolean JNICALL Java_player_JavaQTMoviePlayer_isMovieCreated(JNIEnv *env, jobject caller, jlong playerId) {
	JavaQTMovieView* view = (JavaQTMovieView*) playerId;
	
	return (jboolean) (view->initialized == YES);
}

JNIEXPORT jboolean JNICALL Java_player_JavaQTMoviePlayer_isMovieValid(JNIEnv *env, jobject caller, jlong playerId) {
	JavaQTMovieView* view = (JavaQTMovieView*) playerId;
	
	return (jboolean) ([view movie] != NULL);
}

// experimental: this doesn't work under Java < 1.6 and in 1.6 or higher leads to memory leaks and error messages
JNIEXPORT jboolean JNICALL Java_player_JavaQTMoviePlayer_isFileSupported(JNIEnv *env, jobject caller, jstring path) {
	long retValue = 0L;// no error = true
	/*
	NSArray* fileTypes = [QTMovie movieFileTypes:QTIncludeCommonTypes];
	NSEnumerator* objectEnumerator = [fileTypes objectEnumerator];
	id nextObject;
	while(nextObject = [objectEnumerator nextObject]) {
		NSLog(@"type: %@\n", nextObject);
	}
	*/
	return (jboolean) (retValue == 0);// if not 0 the file is invalid
}

JNIEXPORT jboolean JNICALL Java_player_JavaQTMoviePlayer_isPlaying(JNIEnv *env, jobject caller, jlong playerId) {
	JavaQTMovieView* view = (JavaQTMovieView*) playerId;
	if ([view movie] == NULL) {
		return (jboolean) NO;
	}
	
	return (jboolean) [view isPlaying]; 
}

JNIEXPORT jboolean JNICALL Java_player_JavaQTMoviePlayer_hasVideo(JNIEnv *env, jobject caller, jlong playerId) {
	QTMovieView* view = (QTMovieView*) playerId;
	if ([view movie] == NULL) {
		return (jboolean) NO;
	}
	
	return (jboolean) [[[view movie] attributeForKey: QTMovieHasVideoAttribute] boolValue];
}

JNIEXPORT jdouble JNICALL Java_player_JavaQTMoviePlayer_getFrameDuration(JNIEnv *env, jobject caller, jlong playerId) {
	JavaQTMovieView* view = (JavaQTMovieView*) playerId;
	if ([view movie] == NULL) {
		return (jdouble) 0;
	}
	
	if (view->frameDurationDetected) {
		return (jdouble) view->statFrameDuration;
	} else {
		return (jdouble) [view calcFrameDuration];
	}
}

JNIEXPORT jlong JNICALL Java_player_JavaQTMoviePlayer_getMediaDuration(JNIEnv *env, jobject caller, jlong playerId) {
	QTMovieView* view = (QTMovieView*) playerId;
	if ([view movie] == NULL) {
		return (jlong) 0;
	}
	
	QTTime time = [[view movie] duration];
	
	if (time.timeScale == 0) {
		return (jlong) 0;
	}

	return (jlong) (1000 * time.timeValue) / time.timeScale - ((JavaQTMovieView*) view)->offset;
}

JNIEXPORT jlong JNICALL Java_player_JavaQTMoviePlayer_getMediaTime(JNIEnv *env, jobject caller, jlong playerId) {
	QTMovieView* view = (QTMovieView*) playerId;
	if ([view movie] == NULL) {
		return (jlong) 0;
	}
	
	QTTime time = [[view movie] currentTime];
	
	if (time.timeScale == 0) {
		return (jlong) 0;
	}
	
	return (jlong) (1000 * time.timeValue) / time.timeScale - ((JavaQTMovieView*) view)->offset;
}


JNIEXPORT jdouble JNICALL Java_player_JavaQTMoviePlayer_getMediaTimeDouble(JNIEnv *env, jobject caller, jlong playerId) {
    QTMovieView* view = (QTMovieView*) playerId;
	if ([view movie] == NULL) {
		return (jlong) 0;
	}
	
	QTTime time = [[view movie] currentTime];
	
	if (time.timeScale == 0) {
		return (jdouble) 0;
	}
    //NSLog(@"Get media time double %lld, %ld", time.timeValue, time.timeScale);
    return (jdouble) ((1000 * time.timeValue) / (double) time.timeScale) - ((JavaQTMovieView*) view)->offset;
}

JNIEXPORT jfloat JNICALL Java_player_JavaQTMoviePlayer_getRate(JNIEnv *env, jobject caller, jlong playerId) {
	JavaQTMovieView* view = (JavaQTMovieView*) playerId;
	if ([view movie] == NULL) {
		return (jfloat) 0;
	}
	
	return (jfloat) view->rate;
}

JNIEXPORT jfloat JNICALL Java_player_JavaQTMoviePlayer_getVolume(JNIEnv *env, jobject caller, jlong playerId) {
	QTMovieView* view = (QTMovieView*) playerId;
	if ([view movie] == NULL) {
		return (jfloat) 0;
	}
	
	return (jfloat) [[view movie] volume]; 
}

JNIEXPORT jfloat JNICALL Java_player_JavaQTMoviePlayer_getNaturalWidth(JNIEnv *env, jobject caller, jlong playerId) {
	JavaQTMovieView* view = (JavaQTMovieView*) playerId;
	if ([view movie] == NULL) {
		return (jfloat) 0;
	}
	if (view->encWidth != 0) {
		return (jfloat) view->encWidth;
	}
	
	//NSLog(@"Entering Java_player_JavaQTMoviePlayer_getNaturalWidth");
	NSAutoreleasePool *mPool = [[NSAutoreleasePool alloc] init];
	NSSize size;

	[[[view movie] attributeForKey: QTMovieNaturalSizeAttribute] getValue: &size];

	[mPool release];
	//NSLog(@"Returning from Java_player_JavaQTMoviePlayer_getNaturalWidth %p %d", view, (int)size.width);
	view->encWidth = size.width; // cache the value, it won't change

	return (jfloat) size.width;
}

JNIEXPORT jfloat JNICALL Java_player_JavaQTMoviePlayer_getNaturalHeight(JNIEnv *env, jobject caller, jlong playerId) {
	JavaQTMovieView* view = (JavaQTMovieView*) playerId;
	if ([view movie] == NULL) {
		return (jfloat) 0;
	}
	if (view->encHeight != 0) {
		return (jfloat) view->encHeight;
	}
	
	//NSLog(@"Entering Java_player_JavaQTMoviePlayer_getNaturalHeight");
	NSAutoreleasePool *mPool = [[NSAutoreleasePool alloc] init];
	NSSize size;

	[[[view movie] attributeForKey: QTMovieNaturalSizeAttribute] getValue: &size];

	[mPool release];
	//NSLog(@"Returning from Java_player_JavaQTMoviePlayer_getNaturalHeight %p %d", view, (int)size.height);
	view->encHeight = size.height; // cache the value, it won't change

	return (jfloat) size.height;
}

/*
 * HS July 2015: removed pre Java 1.6 code. The width and height parameters are not used anymore in the current implementation.
 */
JNIEXPORT jbyteArray JNICALL Java_player_JavaQTMoviePlayer_getFrameImage(JNIEnv *env, jobject caller, jlong playerId, jlong time, jint width, jint height) {
	QTMovieView* view = (QTMovieView*) playerId;
	if ([view movie] == NULL) {
		return (jbyteArray) NULL;
	}
	NSAutoreleasePool *mPool = [[NSAutoreleasePool alloc] init];
	NSImage *image = NULL;
	QTTime t = QTMakeTime(time + ((JavaQTMovieView*) view)->offset, 1000);	
	NSError *imgError = nil;
	//NSSize imageSize = NSMakeSize(width, height);
	//NSValue *sizeValue = [NSValue valueWithSize:imageSize];
	
    image = [[view movie] frameImageAtTime:t];
	
	if (imgError != NULL) {
		if ([imgError code] != 0) {
			NSLog(@"Image error code %ld %@", (long)[imgError code], imgError);
		}
	}
	
	NSBitmapImageRep *rep = [[image representations] objectAtIndex: 0];
	NSData *data = [rep representationUsingType: NSPNGFileType properties: nil];
	
	const void *bytes = [data bytes];
	long size = [data length];
	
	jbyteArray jbuf = (*env)->NewByteArray(env, size);
	if(jbuf!=NULL){
		(*env)->SetByteArrayRegion(env, jbuf, 0, size, (jbyte*) bytes);
	}
	
	[mPool release];
	
	return jbuf;
}

/*
 * Class:     player_JavaQTMoviePlayer
 * Method:    getVideoScaleFactor
 * Signature: (J)F
 */
JNIEXPORT jfloat JNICALL Java_player_JavaQTMoviePlayer_getVideoScaleFactor(JNIEnv *env, jobject caller, jlong playerId) {
    JavaQTMovieView* view = (JavaQTMovieView*) playerId;
	if ([view movie] == NULL) {
		return (jfloat) 0;
	}
    //NSLog(@"Get video scale factor");
    return (jfloat) view->videoScaleFactor;
}

/*
 * Class:     player_JavaQTMoviePlayer
 * Method:    getVideoDestinationBounds
 * Signature: (J)[I
 */
JNIEXPORT jintArray JNICALL Java_player_JavaQTMoviePlayer_getVideoDestinationBounds(JNIEnv *env, jobject caller, jlong playerId) {
    JavaQTMovieView* view = (JavaQTMovieView*) playerId;
	if ([view movie] == NULL) {
		return NULL;
	}
    //NSLog(@"Get video frame bounds");
    int len = 4;
    jint vbounds[len];
    vbounds[0] = (jint) view->videoDestOrigin.x;
    vbounds[1] = (jint) view->videoDestOrigin.y;
    vbounds[2] = (jint) view->videoDestSize.width;
    vbounds[3] = (jint) view->videoDestSize.height;
    
    jintArray boundsArray = (*env)->NewIntArray(env, len);
    if (boundsArray != NULL) {
        (*env)->SetIntArrayRegion(env, boundsArray, 0, len, vbounds);
    }
    
    return boundsArray;
}
