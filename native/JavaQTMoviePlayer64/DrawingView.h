//
//  DrawingView.h
//  JavaQTMovieView
//
//  Created by Albert Russel on 2/19/09.
//  Copyright 2008 MPI - Max Planck Institute for Psycholinguistics, Nijmegen
//

#import <Cocoa/Cocoa.h>
#import <QTKit/QTMovieView.h>
//#import <Quicktime/Movies.h>

@interface DrawingView : NSView {
	@public 
		QTMovieView *movieView;
		long drawingTime;
		long drawingPeriod;
		int maxNumberOfDrawingElements;
		int numberOfDrawingElements;
		float** drawingElements;
		NSMutableDictionary* drawingStrings;
		NSMutableDictionary* drawingFontNames;
		int* visibleIndices;
}

- (id)initWithFrame:(NSRect)Frame;
- (void)addElement:(NSString*)element;
- (void)removeElements:(NSString*)element;
- (int)indexForId:(int)id;
- (void)organizeElements;
- (void)getVisibleIndices;
- (void)setDrawingPeriod:(long)period;
- (void)drawRect:(NSRect)dirtyRect;

@end
