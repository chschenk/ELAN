//
//  JavaQTMovieView.h
//  JavaQTMovieView
//
//  Created by Albert Russel on 12/30/08.
//  Copyright 2008 MPI - Max Planck Institute for Psycholinguistics, Nijmegen
//

#import <Cocoa/Cocoa.h>
#import <QTKit/QTKit.h>
#import <QTKit/QTMovieView.h>
#import <QTKit/QTMovie.h>
#import <QTKit/QTMedia.h>
#import <JavaVM/AWTCocoaComponent.h>
//#import "JavaQTMovieView.h"
#import "video.h"
#import <stdio.h>
#import <string.h>
#import "DrawingView.h"


@interface JavaQTMovieView : QTMovieView <AWTCocoaComponent> {
	@public 
		QTMovie* movie;
		jobject javaPeer;
		NSPoint frameOrigin;
		NSWindow* drawingWindow;
		DrawingView* drawingView;
		NSMutableArray* stopSlaves;
		//QTCallBack stopCallBack;
		NSTimer	*drawTimer;
		BOOL initialized;
	    BOOL frameDurationDetected;
		double statFrameDuration;
        long movieTimeScale;
		float rate;
		long offset;
		int encWidth;
		int encHeight;
        float videoScaleFactor;
        NSPoint videoDestOrigin;
        NSSize videoDestSize;
}

+ (id) JavaQTMovieViewWithCaller:(jobject) caller env:(JNIEnv *)env;
- (id) initWithCaller:(jobject)caller env:(JNIEnv *)env;
//- (void) viewWillMoveToSuperview:(NSView *)newSuperview;
- (void) keyDown: (NSEvent *)event;
- (void) mouseDown:(NSEvent *)event;
- (void) mouseUp:(NSEvent *)event;
- (void) rightMouseDown:(NSEvent *)event;
//- (void)mouseDragged:(NSEvent *)theEvent; // needed here?
- (NSMenu*) menuForEvent: (NSEvent*)event;
- (void) scrollWheel:(NSEvent *)event;
- (BOOL) isPlaying;
- (void) setFrame:(NSRect)frameRect;
- (double) calcFrameDuration;
- (void) updateDrawingView:(NSTimer *) tTimer;
- (void) movieDidEndNotification:(NSNotification *)notification;
- (void) movieTimeChangedNotification:(NSNotification *)notification;

@end
