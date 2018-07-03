//
//  DrawingView.m
//  JavaQTMovieView
//
//  Created by Albert Russel on 2/19/09.
//  Copyright 2008 MPI - Max Planck Institute for Psycholinguistics, Nijmegen
//

#import "DrawingView.h"

#define LINE 0
#define RECT 1
#define ELLIPSE 2
#define STRING 3

#define ID 0
#define TYPE 1
#define BEGIN_TIME 2
#define END_TIME 3
#define X 4
#define Y 5
#define X2 6
#define R1 6
#define WIDTH 6
#define Y2 7
#define R2 7
#define HEIGHT 7
#define RED 8
#define GREEN 9
#define BLUE 10
#define ALPHA 11
#define THICKNESS 12
#define FILLED 13

#define MAX_SIMULTANIOUS_VISIBLE_ELEMENTS 100

@implementation DrawingView

- (id)initWithFrame:(NSRect)frame {
	//NSLog(@"INIT!!");
	maxNumberOfDrawingElements = 0;
	numberOfDrawingElements = 0;
	visibleIndices = (int*) malloc(MAX_SIMULTANIOUS_VISIBLE_ELEMENTS * sizeof(int));  // max MAX_SIMULTANIOUS_VISIBLE_ELEMENTS visible elements
	drawingStrings = [NSMutableDictionary dictionaryWithCapacity:100];
	[drawingStrings retain];
	drawingFontNames = [NSMutableDictionary dictionaryWithCapacity:100];
	[drawingFontNames retain];
	
	drawingPeriod = 1;  // draw every frame while playing
	
	return [super initWithFrame:frame];
}


/*
    The drawing elements are encoded as an array of floats (float*) that are kept in an array of float* (float**).
    NSArray is not used because of speed reasons, indexing an element in NSArray costs a method call.
    These arrays have different lengths for different shapes but they all start with id, type, beginTime and endTime.
    Each time a new element is added it has a new higher id than the previous ones therefore the elements are in order
    when new elements are added to the end of the drawingElements (float**)
    
 
							 id, type, beginTime, endTime, .....
									   
	drawingElements ->	 -> {0.0, 0.0,  2000.0,    3000.0, 0.2, 0.4, ...}
						 -> {1.0, 1.0,  2300.0,    2400.0, 0.7, 0.125, .......}
						 -> {2.0, 1.0,  6000.0,    9000.0, 0.66, 0.33, .......}
						 etc. etc.
 
 
	This data structure is chosen to get optimal performance for adding and removing individual elements.
    Removing ranges of elements is more expensive but probably needed less often.
 
	Performance seems no issue even if a few million drawing elements are involved. More efficient data structures
    like segment trees could be considered if performance is an issue because of slow hardware or too many drawing elements.

 */


- (void)addElement:(NSString*)elementString {
	// make the element array
	//NSLog(@"add element: %@", elementString);
	
	// make sure the new element can be added
	if (numberOfDrawingElements == maxNumberOfDrawingElements) {
		float** temp = drawingElements;
		int newMaxNumberOfDrawingElements = maxNumberOfDrawingElements + 100000; 
		drawingElements = (float**) malloc(newMaxNumberOfDrawingElements * sizeof(float*));
		// copy the old values
		int i;
		for (i = 0; i < maxNumberOfDrawingElements; i++) {
			drawingElements[i] = temp[i];
		}
		// free the old array
		free(temp);
		
		maxNumberOfDrawingElements = newMaxNumberOfDrawingElements;
		//NSLog(@"MALLOC max elements = %d", maxNumberOfDrawingElements);
	}
	
	
	NSArray* components = [elementString componentsSeparatedByString:@" "];
	if ([components count] < 4) return; // can not be a valid element string
	
	float* shapeData = nil;
	int shapeType = [[components objectAtIndex:1] intValue];
	switch(shapeType) {
		case LINE:
			//NSLog(@"add a line"); 
			// a line must have an id plus 12 data elements
			if ([components count] == 13) {
				shapeData = (float*) malloc(13 * sizeof(float));
				shapeData[ID]         = [[components objectAtIndex:0] floatValue];
				shapeData[TYPE]       = [[components objectAtIndex:1] floatValue];
				shapeData[BEGIN_TIME] = [[components objectAtIndex:2] floatValue];
				shapeData[END_TIME]   = [[components objectAtIndex:3] floatValue];
				shapeData[X]          = [[components objectAtIndex:4] floatValue];
				shapeData[Y]          = [[components objectAtIndex:5] floatValue];
				shapeData[X2]         = [[components objectAtIndex:6] floatValue];
				shapeData[Y2]         = [[components objectAtIndex:7] floatValue];
				shapeData[RED]        = [[components objectAtIndex:8] floatValue];
				shapeData[GREEN]      = [[components objectAtIndex:9] floatValue];
				shapeData[BLUE]       = [[components objectAtIndex:10] floatValue];
				shapeData[ALPHA]      = [[components objectAtIndex:11] floatValue];
				shapeData[THICKNESS]  = [[components objectAtIndex:12] floatValue];
			}
			break;
		case RECT:
			//NSLog(@"add a rect"); 
			// a rect must have an id plus 13 data elements
			if ([components count] == 14) {
				shapeData = (float*) malloc(14 * sizeof(float));
				shapeData[ID]         = [[components objectAtIndex:0] floatValue];
				shapeData[TYPE]       = [[components objectAtIndex:1] floatValue];
				shapeData[BEGIN_TIME] = [[components objectAtIndex:2] floatValue];
				shapeData[END_TIME]   = [[components objectAtIndex:3] floatValue];
				shapeData[X]          = [[components objectAtIndex:4] floatValue];
				shapeData[Y]          = [[components objectAtIndex:5] floatValue];
				shapeData[WIDTH]      = [[components objectAtIndex:6] floatValue];
				shapeData[HEIGHT]     = [[components objectAtIndex:7] floatValue];
				shapeData[RED]        = [[components objectAtIndex:8] floatValue];
				shapeData[GREEN]      = [[components objectAtIndex:9] floatValue];
				shapeData[BLUE]       = [[components objectAtIndex:10] floatValue];
				shapeData[ALPHA]      = [[components objectAtIndex:11] floatValue];
				shapeData[THICKNESS]  = [[components objectAtIndex:12] floatValue];
				shapeData[FILLED]     = [[components objectAtIndex:13] floatValue];
			}
			break;
		case ELLIPSE:
			//NSLog(@"add an ellipse"); 
			// an ellipse must have an id plus 13 data elements
			if ([components count] == 14) {
				shapeData = (float*) malloc(14 * sizeof(float));
				shapeData[ID]         = [[components objectAtIndex:0] floatValue];
				shapeData[TYPE]       = [[components objectAtIndex:1] floatValue];
				shapeData[BEGIN_TIME] = [[components objectAtIndex:2] floatValue];
				shapeData[END_TIME]   = [[components objectAtIndex:3] floatValue];
				shapeData[X]          = [[components objectAtIndex:4] floatValue];
				shapeData[Y]          = [[components objectAtIndex:5] floatValue];
				shapeData[R1]         = [[components objectAtIndex:6] floatValue];
				shapeData[R2]         = [[components objectAtIndex:7] floatValue];
				shapeData[RED]        = [[components objectAtIndex:8] floatValue];
				shapeData[GREEN]      = [[components objectAtIndex:9] floatValue];
				shapeData[BLUE]       = [[components objectAtIndex:10] floatValue];
				shapeData[ALPHA]      = [[components objectAtIndex:11] floatValue];
				shapeData[THICKNESS]  = [[components objectAtIndex:12] floatValue];
				shapeData[FILLED]     = [[components objectAtIndex:13] floatValue];
			}
			break;
		case STRING:
			//NSLog(@"add a string");
			;//
			NSString* id = [components objectAtIndex:0];
			NSString* delimiter = [components objectAtIndex:2];
			
			//NSLog(@"delimiter %@", delimiter);
			components = [elementString componentsSeparatedByString:delimiter];
			
			if ([components count] == 13) {
				// keep the strings in the dictionaries with key ID
				[drawingStrings setValue:[components objectAtIndex:2] forKey:id];
				// check the fontname
				NSFont* f = [NSFont fontWithName:[components objectAtIndex:11] size:12.0];
				if (f != NULL) {
					[drawingFontNames setValue:[components objectAtIndex:11] forKey:id];
				} else {
					[drawingFontNames setValue:@"Monaco" forKey:id];
				}
				
				
				shapeData = (float*) malloc(13 * sizeof(float));
				shapeData[ID]         = [id floatValue];
				shapeData[TYPE]       = STRING;
				shapeData[BEGIN_TIME] = [[components objectAtIndex:3] floatValue];
				shapeData[END_TIME]   = [[components objectAtIndex:4] floatValue];
				shapeData[X]          = [[components objectAtIndex:5] floatValue];
				shapeData[Y]          = [[components objectAtIndex:6] floatValue];
				shapeData[RED]        = [[components objectAtIndex:7] floatValue];
				shapeData[GREEN]      = [[components objectAtIndex:8] floatValue];
				shapeData[BLUE]       = [[components objectAtIndex:9] floatValue];
				shapeData[ALPHA]      = [[components objectAtIndex:10] floatValue];
				shapeData[THICKNESS]  = [[components objectAtIndex:12] floatValue];
			}
			break;
	}
	
	// see if there is something to add
	if (shapeData != nil) {
		// add the new element to the end of the (float**)
		drawingElements[numberOfDrawingElements] = shapeData;
		numberOfDrawingElements++;
	}
	//NSLog(@"elements: %d", numberOfDrawingElements);
}

// depending on the elementString different kinds of removal operations are executed
- (void)removeElements:(NSString*)elementString {
	NSArray* components = [elementString componentsSeparatedByString:@" "];
	
	// see if all elements are to be deleted
	if ([elementString isEqualToString:@"ALL"]) {
		if (numberOfDrawingElements > 0) {
			int i;
			for (i = 0; i < numberOfDrawingElements; i++) {
				free(drawingElements[i]);
			}
			free(drawingElements);
			maxNumberOfDrawingElements = 0;
			numberOfDrawingElements = 0;
		}
	} else if ([elementString hasPrefix:@"INTERVAL"]) {
		// remove all elements between two times
		long beginTime = (long) [[components objectAtIndex:1] longLongValue];
		long endTime = (long) [[components objectAtIndex:2] longLongValue];
		int i;
		for (i = 0; i < numberOfDrawingElements; i++) {
			long begin = drawingElements[i][BEGIN_TIME];
			long end   = drawingElements[i][END_TIME];
			if ((begin >= beginTime && begin < endTime) || (end >= beginTime && end < endTime)) {
				// free the floats
				free(drawingElements[i]);
				
				// remove it from the float* array
				int j;
				for (j = i + 1; j < numberOfDrawingElements; j++) {
					drawingElements[j - 1] = drawingElements[j];
				}
				numberOfDrawingElements--;
				i--; //make sure we continue at the right element
			}
		}
		
	} else if ([elementString hasPrefix:@"ID"]) {
		// remove a specific element
		int id = [[components objectAtIndex:1] intValue];
		//NSLog(@"remove id: %d", id);
		NSString* idString = [NSString stringWithFormat: @"%d", id];
		[drawingStrings removeObjectForKey:idString];
		[drawingFontNames removeObjectForKey:idString];
		
		// find the index of the element to be removed, binary search not realy needed, could be done by scanning all elements
		int index = [self indexForId:id];
		//NSLog(@"index: %d", index);
		
		// remove it if found by moving the tail after index of the (float**) one element to the left
		if (index >= 0) {
			// free the floats
			free(drawingElements[index]);
			
			// remove it from the float* array
			int i;
			for (i = index + 1; i < numberOfDrawingElements; i++) {
				drawingElements[i - 1] = drawingElements[i];
			}
			numberOfDrawingElements--;
		}
	}
}


- (int)indexForId:(int)id {
	// special case
	if (numberOfDrawingElements == 0) return -1;
	
	// because all id's are in ascending order in the (float**)  binary search is possible
	int lowerBoundary = 0;
	int upperBoundary = numberOfDrawingElements;
	int index;
	int currentId;
	do {
		index = lowerBoundary + (upperBoundary - lowerBoundary) / 2;
		currentId = drawingElements[index][ID];
		
		if (currentId < id) {
			lowerBoundary = index + 1;
		} else if (currentId > id) {
			upperBoundary = index - 1;
		} else {
			// we have got him, currentId == id
			return index;
		}
	} while (lowerBoundary < upperBoundary);
	
	if (lowerBoundary >= 0 && lowerBoundary < numberOfDrawingElements && id == drawingElements[lowerBoundary][ID]) {
		return lowerBoundary;
	}
	
	// we did not find an entry with the id
	return -1;
}
			
	

- (void)organizeElements {
	/**
	 * Not used now but could be used if internal datastructures like a segment tree must be build for efficiency reasons
	 * First tests suggest that one needs not to worry about performance problems when there are less than a few million drawing elements.
	 */
	
	
}



- (void)getVisibleIndices {
	int i;
	// mark al indices as invisible (-1)
	for (i = 0; i < MAX_SIMULTANIOUS_VISIBLE_ELEMENTS; i++) {
		visibleIndices[i] = -1;
	}
	
	// loop over all entries and see if bt <= t < et
	// simple method seems to be fast enough on 2.8Ghz dual core for up to 10000000 elements, 
	// a one houre movie with 25fps and 10 elements in each frame has 3600 * 25 * 10 = 900000 elements.
	// if performance is an issue here the visible elements should be kept in a segment tree
	int n = 0;
	for (i = 0; i < numberOfDrawingElements && n < MAX_SIMULTANIOUS_VISIBLE_ELEMENTS; i++) {
		if (drawingElements[i][BEGIN_TIME] <= drawingTime && drawingTime < drawingElements[i][END_TIME]) {
			visibleIndices[n++] = i;
		}
	}
}


// only draw the visible elements when frameNumber % drawingPeriod == 0
// the value of drawingPeriod must be kept here but the logic is for efficiency reasons in JavaQTMovie.m in movieDrawingCompleteProc
- (void)setDrawingPeriod:(long)period {
	drawingPeriod = period;
}

- (void)drawRect:(NSRect)dirtyRect {
	if (drawingElements == NULL) return;
	
	long currentDrawingTime = drawingTime;
	
	// get the drawing elements that are visible at the current drawing time
	[self getVisibleIndices];
	
	// get the dimensions of the video in which the drawing elements must be drawn
	NSRect bounds = [self bounds];
	int width = bounds.size.width;
	int height = bounds.size.height;
	int x, y, w, h, x2, y2;
	
	// only keep drawing as long as the drawing time did not change and there is still something to draw
	int i = 0;
	while (currentDrawingTime == drawingTime && i < MAX_SIMULTANIOUS_VISIBLE_ELEMENTS && visibleIndices[i] >= 0) {
		float* element = drawingElements[visibleIndices[i]];
		switch((int)element[TYPE]) {
			case LINE:
				x = width * element[X];
				y = height - height * element[Y]; // Java coordinates (0, 0) in upper left corner
				x2 = width * element[X2];
				y2 = height - height * element[Y2]; // Java coordinates (0, 0) in upper left corner
				[[NSColor colorWithDeviceRed:element[RED] green:element[GREEN] blue:element[BLUE] alpha:element[ALPHA]] set];
				[NSBezierPath setDefaultLineWidth:element[THICKNESS]];
				[NSBezierPath strokeLineFromPoint:NSMakePoint(x, y) toPoint:NSMakePoint(x2, y2)];
				break;
			case RECT:
				x = width * element[X];
				y = height - height * element[Y] - height * element[HEIGHT]; // Java coordinates (0, 0) in upper left corner
				w = width * element[WIDTH];
				h = height * element[HEIGHT];
				NSRect rBounds = NSMakeRect(x, y, w, h);
				[[NSColor colorWithDeviceRed:element[RED] green:element[GREEN] blue:element[BLUE] alpha:element[ALPHA]] set];
				if (element[FILLED] != 0) {
					[NSBezierPath fillRect:rBounds];
				} else {
					[NSBezierPath setDefaultLineWidth:element[THICKNESS]];
					[NSBezierPath strokeRect:rBounds];
				}
				break;
			case ELLIPSE:
				w = 2 * width * element[R1];
				h = 2 * height * element[R2];
				x = width * element[X] - w / 2;
				y = element[Y] * height - h / 2; // Java coordinates (0, 0) in upper left corner
				y = height - y - h;
				NSRect eBounds = NSMakeRect(x, y, w, h);
				[[NSColor colorWithDeviceRed:element[RED] green:element[GREEN] blue:element[BLUE] alpha:element[ALPHA]] set];
				if (element[FILLED] != 0) {
					[[NSBezierPath bezierPathWithOvalInRect:eBounds] fill];
				} else {
					[NSBezierPath setDefaultLineWidth:element[THICKNESS]];
					[[NSBezierPath bezierPathWithOvalInRect:eBounds] stroke];
				}
				break;
			case STRING:
				w = 4;
				NSString* id = [NSString stringWithFormat: @"%d", (int)element[ID]];
				NSString* text = [drawingStrings valueForKey:id];
				x = width * element[X];
				y = height - height * element[Y]; // Java coordinates (0, 0) in upper left corner
				NSString* fontName = [drawingFontNames valueForKey:id];
				int fontSize = (int) element[THICKNESS];
				NSColor* fontColor = [NSColor colorWithDeviceRed:element[RED] green:element[GREEN] blue:element[BLUE] alpha:element[ALPHA]];
				
				NSMutableDictionary* attr = [[NSMutableDictionary alloc] init];
				[attr setObject:[NSFont fontWithName:fontName size:fontSize] forKey:NSFontAttributeName];
				[attr setObject:fontColor forKey:NSForegroundColorAttributeName];
				[text drawAtPoint:NSMakePoint(x, y) withAttributes:attr];
				break;
		}
		
		i++;
	}
}

@end
