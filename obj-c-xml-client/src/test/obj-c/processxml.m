/**
 * Copyright © 2006-2016 Web Cohesion (info@webcohesion.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
//#define DEBUG_ENUNCIATE 2 //set to '1' or '2' for output debugging in gcc-compile.xml
#import <stdio.h>
#import "api.h"

int main ( int argc, char *argv[] ) {
  NSData *in;
  NSData *out;
  APISHAPESCircle *circle;
  APISHAPESTriangle *triangle;
  APISHAPESRectangle *rectangle;
  APIANIMALSCat *cat;
  APIDRAWCanvas *canvas;
  APISTRUCTURESHouse *house;
  APIVEHICLESBus *bus;
  NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];
  if (argc != 4) {
    printf("Usage: %s [cat|canvas|house|bus|circle|triangle|rectangle] [infile] [outfile]", argv[0]);
    return 1;
  }

  in = [NSData dataWithContentsOfFile: [NSString stringWithCString: argv[2] encoding: NSUTF8StringEncoding]];
  [in retain];
  if (strcmp("circle", argv[1]) == 0) {
    circle = (APISHAPESCircle *) [APISHAPESCircle readFromXML: in];
    out = [circle writeToXML];
    [out retain];
    [out writeToFile: [NSString stringWithCString: argv[3] encoding: NSUTF8StringEncoding] atomically: NO];

    [circle release];
    [in release];
    [out release];
  }
  else if (strcmp("triangle", argv[1]) == 0) {
    triangle = (APISHAPESTriangle *) [APISHAPESTriangle readFromXML: in];
    out = [triangle writeToXML];
    [out retain];
    [out writeToFile: [NSString stringWithCString: argv[3] encoding: NSUTF8StringEncoding] atomically: NO];

    [triangle release]; //free the triangle.
    [in release];
    [out release];
  }
  else if (strcmp("rectangle", argv[1]) == 0) {
    rectangle = (APISHAPESRectangle *) [APISHAPESRectangle readFromXML: in];
    out = [rectangle writeToXML];
    [out retain];
    [out writeToFile: [NSString stringWithCString: argv[3] encoding: NSUTF8StringEncoding] atomically: NO];

    [rectangle release]; //free the rectangle.
    [in release];
    [out release];
  }
  else if (strcmp("cat", argv[1]) == 0) {
    cat = (APIANIMALSCat *) [APIANIMALSCat readFromXML: in];
    out = [cat writeToXML];
    [out retain];
    [out writeToFile: [NSString stringWithCString: argv[3] encoding: NSUTF8StringEncoding] atomically: NO];

    [cat release]; //free the cat.
    [in release];
    [out release];
  }
  else if (strcmp("canvas", argv[1]) == 0) {
    canvas = (APIDRAWCanvas *) [APIDRAWCanvas readFromXML: in];
    out = [canvas writeToXML];
    [out retain];
    [out writeToFile: [NSString stringWithCString: argv[3] encoding: NSUTF8StringEncoding] atomically: NO];

    [canvas release]; //free the canvas.
    [in release];
    [out release];
  }
  else if (strcmp("house", argv[1]) == 0) {
    house = (APISTRUCTURESHouse *) [APISTRUCTURESHouse readFromXML: in];
    [house setKnownStyle: [house knownStyle]];
    [house setKnownType: [house knownType]];
    out = [house writeToXML];
    [out retain];
    [out writeToFile: [NSString stringWithCString: argv[3] encoding: NSUTF8StringEncoding] atomically: NO];

    [house release]; //free the house.
    [in release];
    [out release];
  }
  else if (strcmp("bus", argv[1]) == 0) {
    bus = (APIVEHICLESBus *) [APIVEHICLESBus readFromXML: in];
    out = [bus writeToXML];
    [out retain];
    [out writeToFile: [NSString stringWithCString: argv[3] encoding: NSUTF8StringEncoding] atomically: NO];

    [bus release]; //free the bus.
    [in release];
    [out release];
  }
  else {
    printf("Unrecognized xml type: %s\nUsage: %s [cat|canvas|house|bus||circle|triangle|rectangle] [infile] [outfile]", argv[1], argv[0]);
    return 1;
  }

// we're deallocating our own instances, so don't release the pool.
//  [pool release];
  return 0;
}