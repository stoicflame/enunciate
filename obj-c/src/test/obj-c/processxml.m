#define DEBUG_ENUNCIATE 2 //set to '1' or '2' for output debugging.
#import <stdio.h>
#import "enunciate.h"

int main ( int argc, char *argv[] ) {
  NSData *in;
  NSData *out;
  ENUNCIATENS0Circle *circle;
  ENUNCIATENS0Triangle *triangle;
  ENUNCIATENS0Rectangle *rectangle;
  ENUNCIATEANIMALSCat *cat;
  ENUNCIATEDRAWCanvas *canvas;
  ENUNCIATESTRUCTURESHouse *house;
  ENUNCIATEVEHICLESBus *bus;
  NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];
  if (argc != 4) {
    printf("Usage: %s [cat|canvas|house|bus|circle|triangle|rectangle] [infile] [outfile]", argv[0]);
    return 1;
  }

  in = [NSData dataWithContentsOfFile: [NSString stringWithCString: argv[2] encoding: NSUTF8StringEncoding]];
  [in retain];
  if (strcmp("circle", argv[1]) == 0) {
    circle = (ENUNCIATENS0Circle *) [ENUNCIATENS0Circle readFromXML: in];
    out = [circle writeToXML];
    [out retain];
    [out writeToFile: [NSString stringWithCString: argv[3] encoding: NSUTF8StringEncoding] atomically: NO];

    [circle release];
    [in release];
    [out release];
  }
  else if (strcmp("triangle", argv[1]) == 0) {
    triangle = (ENUNCIATENS0Triangle *) [ENUNCIATENS0Triangle readFromXML: in];
    out = [triangle writeToXML];
    [out retain];
    [out writeToFile: [NSString stringWithCString: argv[3] encoding: NSUTF8StringEncoding] atomically: NO];

    [triangle release]; //free the triangle.
    [in release];
    [out release];
  }
  else if (strcmp("rectangle", argv[1]) == 0) {
    rectangle = (ENUNCIATENS0Rectangle *) [ENUNCIATENS0Rectangle readFromXML: in];
    out = [rectangle writeToXML];
    [out retain];
    [out writeToFile: [NSString stringWithCString: argv[3] encoding: NSUTF8StringEncoding] atomically: NO];

    [rectangle release]; //free the rectangle.
    [in release];
    [out release];
  }
  else if (strcmp("cat", argv[1]) == 0) {
    cat = (ENUNCIATEANIMALSCat *) [ENUNCIATEANIMALSCat readFromXML: in];
    out = [cat writeToXML];
    [out retain];
    [out writeToFile: [NSString stringWithCString: argv[3] encoding: NSUTF8StringEncoding] atomically: NO];

    [cat release]; //free the cat.
    [in release];
    [out release];
  }
  else if (strcmp("canvas", argv[1]) == 0) {
    canvas = (ENUNCIATEDRAWCanvas *) [ENUNCIATEDRAWCanvas readFromXML: in];
    out = [canvas writeToXML];
    [out retain];
    [out writeToFile: [NSString stringWithCString: argv[3] encoding: NSUTF8StringEncoding] atomically: NO];

    [canvas release]; //free the canvas.
    [in release];
    [out release];
  }
  else if (strcmp("house", argv[1]) == 0) {
    house = (ENUNCIATESTRUCTURESHouse *) [ENUNCIATESTRUCTURESHouse readFromXML: in];
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
    bus = (ENUNCIATEVEHICLESBus *) [ENUNCIATEVEHICLESBus readFromXML: in];
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