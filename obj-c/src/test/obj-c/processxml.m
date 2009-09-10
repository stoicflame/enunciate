#define DEBUG_ENUNCIATE 0 //set to '1' or '2' for output debugging.
#include <enunciate.m>
#include <stdio.h>

int main ( int argc, char *argv[] ) {
  xmlTextReaderPtr reader;
  xmlTextWriterPtr writer;
  int status;
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

  reader = xmlReaderForFile(argv[2], NULL, 0);
  writer = xmlNewTextWriterFilename(argv[3], 0);
  if (strcmp("circle", argv[1]) == 0) {
    circle = (ENUNCIATENS0Circle *) [ENUNCIATENS0Circle readXMLElement: reader];
    [circle writeXMLElement: writer];
    if (status < 0) {
      //panic
      printf("Problem writing circle.");
      return 1;
    }

    [circle dealloc];
    xmlFreeTextWriter(writer); //free the writer
    xmlFreeTextReader(reader); //free the reader
  }
  else if (strcmp("triangle", argv[1]) == 0) {
    triangle = (ENUNCIATENS0Triangle *) [ENUNCIATENS0Triangle readXMLElement: reader];
    [triangle writeXMLElement: writer];
    if (status < 0) {
      //panic
      printf("Problem writing triangle.");
      return 1;
    }

    [triangle dealloc]; //free the triangle.
    xmlFreeTextWriter(writer); //free the writer
    xmlFreeTextReader(reader); //free the reader
  }
  else if (strcmp("rectangle", argv[1]) == 0) {
    rectangle = (ENUNCIATENS0Rectangle *) [ENUNCIATENS0Rectangle readXMLElement: reader];
    [rectangle writeXMLElement: writer];
    if (status < 0) {
      //panic
      printf("Problem writing rectangle.");
      return 1;
    }

    [rectangle dealloc]; //free the rectangle.
    xmlFreeTextWriter(writer); //free the writer
    xmlFreeTextReader(reader); //free the reader
  }
  else if (strcmp("cat", argv[1]) == 0) {
    cat = (ENUNCIATENS2Cat *) [ENUNCIATENS2Cat readXMLElement: reader];
    [cat writeXMLElement: writer];
    if (status < 0) {
      //panic
      printf("Problem writing cat.");
      return 1;
    }

    [cat dealloc]; //free the cat.
    xmlFreeTextWriter(writer); //free the writer
    xmlFreeTextReader(reader); //free the reader
  }
  else if (strcmp("canvas", argv[1]) == 0) {
    canvas = (ENUNCIATENS3Canvas *) [ENUNCIATENS3Canvas readXMLElement: reader];
    [canvas writeXMLElement: writer];
    if (status < 0) {
      //panic
      printf("Problem writing canvas.");
      return 1;
    }

    [canvas dealloc]; //free the canvas.
    xmlFreeTextWriter(writer); //free the writer
    xmlFreeTextReader(reader); //free the reader
  }
  else if (strcmp("house", argv[1]) == 0) {
    house = (ENUNCIATENS4House *) [ENUNCIATENS4House readXMLElement: reader];
    [house writeXMLElement: writer];
    if (status < 0) {
      //panic
      printf("Problem writing house.");
      return 1;
    }

    [house dealloc]; //free the house.
    xmlFreeTextWriter(writer); //free the writer
    xmlFreeTextReader(reader); //free the reader
  }
  else if (strcmp("bus", argv[1]) == 0) {
    bus = (ENUNCIATENS5Bus *) [ENUNCIATENS5Bus readXMLElement: reader];
    [bus writeXMLElement: writer];
    if (status < 0) {
      //panic
      printf("Problem writing bus.");
      return 1;
    }

    [bus dealloc]; //free the bus.
    xmlFreeTextWriter(writer); //free the writer
    xmlFreeTextReader(reader); //free the reader
  }
  else {
    printf("Unrecognized xml type: %s\nUsage: %s [cat|canvas|house|bus||circle|triangle|rectangle] [infile] [outfile]", argv[1], argv[0]);
    return 1;
  }

// we're deallocating our own instances, so don't release the pool.
//  [pool release];
  return 0;
}