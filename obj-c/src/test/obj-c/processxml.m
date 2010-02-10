#define DEBUG_ENUNCIATE 0 //set to '1' or '2' for output debugging.
#include <enunciate.m>
#include <stdio.h>

int main ( int argc, char *argv[] ) {
  NSData *in;
  NSData *out;
  ENUNCIATENS0Circle *circle;
  ENUNCIATENS0Triangle *triangle;
  ENUNCIATENS0Rectangle *rectangle;
  ENUNCIATENS2Cat *cat;
  ENUNCIATENS3Canvas *canvas;
  ENUNCIATENS4House *house;
  ENUNCIATENS5Bus *bus;
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

    [circle dealloc];
    [in release];
    [out release];
  }
  else if (strcmp("triangle", argv[1]) == 0) {
    triangle = (ENUNCIATENS0Triangle *) [ENUNCIATENS0Triangle readFromXML: in];
    out = [triangle writeToXML];
    [out retain];
    [out writeToFile: [NSString stringWithCString: argv[3] encoding: NSUTF8StringEncoding] atomically: NO];

    [triangle dealloc]; //free the triangle.
    [in release];
    [out release];
  }
  else if (strcmp("rectangle", argv[1]) == 0) {
    rectangle = (ENUNCIATENS0Rectangle *) [ENUNCIATENS0Rectangle readFromXML: in];
    out = [rectangle writeToXML];
    [out retain];
    [out writeToFile: [NSString stringWithCString: argv[3] encoding: NSUTF8StringEncoding] atomically: NO];

    [rectangle dealloc]; //free the rectangle.
    [in release];
    [out release];
  }
  else if (strcmp("cat", argv[1]) == 0) {
    cat = (ENUNCIATENS2Cat *) [ENUNCIATENS2Cat readFromXML: in];
    out = [cat writeToXML];
    [out retain];
    [out writeToFile: [NSString stringWithCString: argv[3] encoding: NSUTF8StringEncoding] atomically: NO];

    [cat dealloc]; //free the cat.
    [in release];
    [out release];
  }
  else if (strcmp("canvas", argv[1]) == 0) {
    canvas = (ENUNCIATENS3Canvas *) [ENUNCIATENS3Canvas readFromXML: in];
    out = [canvas writeToXML];
    [out retain];
    [out writeToFile: [NSString stringWithCString: argv[3] encoding: NSUTF8StringEncoding] atomically: NO];

    [canvas dealloc]; //free the canvas.
    [in release];
    [out release];
  }
  else if (strcmp("house", argv[1]) == 0) {
    house = (ENUNCIATENS4House *) [ENUNCIATENS4House readFromXML: in];
    out = [house writeToXML];
    [out retain];
    [out writeToFile: [NSString stringWithCString: argv[3] encoding: NSUTF8StringEncoding] atomically: NO];

    [house dealloc]; //free the house.
    [in release];
    [out release];
  }
  else if (strcmp("bus", argv[1]) == 0) {
    bus = (ENUNCIATENS5Bus *) [ENUNCIATENS5Bus readFromXML: in];
    out = [bus writeToXML];
    [out retain];
    [out writeToFile: [NSString stringWithCString: argv[3] encoding: NSUTF8StringEncoding] atomically: NO];

    [bus dealloc]; //free the bus.
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