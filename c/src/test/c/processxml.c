#define DEBUG_ENUNCIATE 1
#include <enunciate.c>
#include <stdio.h>

int main ( int argc, char *argv[] ) {
  xmlTextReaderPtr reader;
  xmlTextWriterPtr writer;
  int status;
  struct enunciate_ns0_circle *circle;
  struct enunciate_ns0_triangle *triangle;
  struct enunciate_ns0_rectangle *rectangle;
  struct enunciate_ns2_cat *cat;
  struct enunciate_ns3_canvas *canvas;
  struct enunciate_ns4_house *house;
  struct enunciate_ns5_bus *bus;
  if (argc != 4) {
    printf("Usage: %s [cat|canvas|house|bus|circle|triangle|rectangle] [infile] [outfile]", argv[0]);
    return 1;
  }

  reader = xmlReaderForFile(argv[2], NULL, 0);
  writer = xmlNewTextWriterFilename(argv[3], 0);
  if (strcmp("circle", argv[1]) == 0) {
    circle = xml_read_enunciate_ns0_circle(reader);
    status = xml_write_enunciate_ns0_circle(writer, circle);
    if (status < 0) {
      //panic
      printf("Problem writing circle.");
      return 1;
    }

    free_enunciate_ns0_circle(circle); //free the circle.
    xmlFreeTextWriter(writer); //free the writer
    xmlFreeTextReader(reader); //free the reader
  }
  else if (strcmp("triangle", argv[1]) == 0) {
    triangle = xml_read_enunciate_ns0_triangle(reader);
    status = xml_write_enunciate_ns0_triangle(writer, triangle);
    if (status < 0) {
      //panic
      printf("Problem writing triangle.");
      return 1;
    }

    free_enunciate_ns0_triangle(triangle); //free the triangle.
    xmlFreeTextWriter(writer); //free the writer
    xmlFreeTextReader(reader); //free the reader
  }
  else if (strcmp("rectangle", argv[1]) == 0) {
    rectangle = xml_read_enunciate_ns0_rectangle(reader);
    status = xml_write_enunciate_ns0_rectangle(writer, rectangle);
    if (status < 0) {
      //panic
      printf("Problem writing rectangle.");
      return 1;
    }

    free_enunciate_ns0_rectangle(rectangle); //free the rectangle.
    xmlFreeTextWriter(writer); //free the writer
    xmlFreeTextReader(reader); //free the reader
  }
  else if (strcmp("cat", argv[1]) == 0) {
    cat = xml_read_enunciate_ns2_cat(reader);
    status = xml_write_enunciate_ns2_cat(writer, cat);
    if (status < 0) {
      //panic
      printf("Problem writing cat.");
      return 1;
    }

    free_enunciate_ns2_cat(cat); //free the cat.
    xmlFreeTextWriter(writer); //free the writer
    xmlFreeTextReader(reader); //free the reader
  }
  else if (strcmp("canvas", argv[1]) == 0) {
    canvas = xml_read_enunciate_ns3_canvas(reader);
    status = xml_write_enunciate_ns3_canvas(writer, canvas);
    if (status < 0) {
      //panic
      printf("Problem writing canvas.");
      return 1;
    }

    free_enunciate_ns3_canvas(canvas); //free the canvas.
    xmlFreeTextWriter(writer); //free the writer
    xmlFreeTextReader(reader); //free the reader
  }
  else if (strcmp("house", argv[1]) == 0) {
    house = xml_read_enunciate_ns4_house(reader);
    status = xml_write_enunciate_ns4_house(writer, house);
    if (status < 0) {
      //panic
      printf("Problem writing house.");
      return 1;
    }

    free_enunciate_ns4_house(house); //free the house.
    xmlFreeTextWriter(writer); //free the writer
    xmlFreeTextReader(reader); //free the reader
  }
  else if (strcmp("bus", argv[1]) == 0) {
    bus = xml_read_enunciate_ns5_bus(reader);
    status = xml_write_enunciate_ns5_bus(writer, bus);
    if (status < 0) {
      //panic
      printf("Problem writing bus.");
      return 1;
    }

    free_enunciate_ns5_bus(bus); //free the bus.
    xmlFreeTextWriter(writer); //free the writer
    xmlFreeTextReader(reader); //free the reader
  }
  else {
    printf("Unrecognized xml type: %s\nUsage: %s [cat|canvas|house|bus||circle|triangle|rectangle] [infile] [outfile]", argv[1], argv[0]);
    return 1;
  }

  return 0;
}