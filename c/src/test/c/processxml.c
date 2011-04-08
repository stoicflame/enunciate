#define DEBUG_ENUNCIATE 0 //set to '1' or '2' for output debugging.
#include <enunciate.c>
#include <stdio.h>

int main ( int argc, char *argv[] ) {
  xmlTextReaderPtr reader;
  xmlTextWriterPtr writer;
  int status;
  struct enunciate_ns0_circle *circle;
  struct enunciate_ns0_triangle *triangle;
  struct enunciate_ns0_rectangle *rectangle;
  struct enunciate_animals_cat *cat;
  struct enunciate_draw_canvas *canvas;
  struct enunciate_structures_house *house;
  struct enunciate_vehicles_bus *bus;
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
#if DEBUG_ENUNCIATE
    else {
      printf("Successfully wrote the circle with status: %i\n", status);
    }
#endif

    free_enunciate_ns0_circle(circle); //free the circle.
#if DEBUG_ENUNCIATE
    printf("Successfully freed the circle from memory.\n");
#endif
    xmlFreeTextWriter(writer); //free the writer
#if DEBUG_ENUNCIATE
    printf("Successfully freed the writer.\n");
#endif
    xmlFreeTextReader(reader); //free the reader
#if DEBUG_ENUNCIATE
    printf("Successfully freed the reader.\n");
#endif
  }
  else if (strcmp("triangle", argv[1]) == 0) {
    triangle = xml_read_enunciate_ns0_triangle(reader);
    status = xml_write_enunciate_ns0_triangle(writer, triangle);
    if (status < 0) {
      //panic
      printf("Problem writing triangle.");
      return 1;
    }
#if DEBUG_ENUNCIATE
    else {
      printf("Successfully wrote the triangle with status: %i\n", status);
    }
#endif

    free_enunciate_ns0_triangle(triangle); //free the triangle.
#if DEBUG_ENUNCIATE
    printf("Successfully freed the triangle from memory.\n");
#endif
    xmlFreeTextWriter(writer); //free the writer
#if DEBUG_ENUNCIATE
    printf("Successfully freed the writer.\n");
#endif
    xmlFreeTextReader(reader); //free the reader
#if DEBUG_ENUNCIATE
    printf("Successfully freed the reader.\n");
#endif
  }
  else if (strcmp("rectangle", argv[1]) == 0) {
    rectangle = xml_read_enunciate_ns0_rectangle(reader);
    status = xml_write_enunciate_ns0_rectangle(writer, rectangle);
    if (status < 0) {
      //panic
      printf("Problem writing rectangle.");
      return 1;
    }
#if DEBUG_ENUNCIATE
    else {
      printf("Successfully wrote the rectangle with status: %i\n", status);
    }
#endif

    free_enunciate_ns0_rectangle(rectangle); //free the rectangle.
#if DEBUG_ENUNCIATE
    printf("Successfully freed the rectangle from memory.\n");
#endif
    xmlFreeTextWriter(writer); //free the writer
#if DEBUG_ENUNCIATE
    printf("Successfully freed the writer.\n");
#endif
    xmlFreeTextReader(reader); //free the reader
#if DEBUG_ENUNCIATE
    printf("Successfully freed the reader.\n");
#endif
  }
  else if (strcmp("cat", argv[1]) == 0) {
    cat = xml_read_enunciate_animals_cat(reader);
    status = xml_write_enunciate_animals_cat(writer, cat);
    if (status < 0) {
      //panic
      printf("Problem writing cat.");
      return 1;
    }
#if DEBUG_ENUNCIATE
    else {
      printf("Successfully wrote the cat with status: %i\n", status);
    }
#endif

    free_enunciate_animals_cat(cat); //free the cat.
#if DEBUG_ENUNCIATE
    printf("Successfully freed the cat from memory.\n");
#endif
    xmlFreeTextWriter(writer); //free the writer
#if DEBUG_ENUNCIATE
    printf("Successfully freed the writer.\n");
#endif
    xmlFreeTextReader(reader); //free the reader
#if DEBUG_ENUNCIATE
    printf("Successfully freed the reader.\n");
#endif
  }
  else if (strcmp("canvas", argv[1]) == 0) {
    canvas = xml_read_enunciate_draw_canvas(reader);
    status = xml_write_enunciate_draw_canvas(writer, canvas);
    if (status < 0) {
      //panic
      printf("Problem writing canvas.");
      return 1;
    }
#if DEBUG_ENUNCIATE
    else {
      printf("Successfully wrote the canvas with status: %i\n", status);
    }
#endif

    free_enunciate_draw_canvas(canvas); //free the canvas.
#if DEBUG_ENUNCIATE
    printf("Successfully freed the canvas from memory.\n");
#endif
    xmlFreeTextWriter(writer); //free the writer
#if DEBUG_ENUNCIATE
    printf("Successfully freed the writer.\n");
#endif
    xmlFreeTextReader(reader); //free the reader
#if DEBUG_ENUNCIATE
    printf("Successfully freed the reader.\n");
#endif
  }
  else if (strcmp("house", argv[1]) == 0) {
    house = xml_read_enunciate_structures_house(reader);
    house->style = xml_convert_known_enunciate_structures_houseStyle(xml_get_known_enunciate_structures_houseStyle(house->style));
    house->type = xml_convert_known_enunciate_structures_houseType(xml_get_known_enunciate_structures_houseType(house->type));
    status = xml_write_enunciate_structures_house(writer, house);
    if (status < 0) {
      //panic
      printf("Problem writing house.");
      return 1;
    }
#if DEBUG_ENUNCIATE
    else {
      printf("Successfully wrote the house with status: %i\n", status);
    }
#endif

    free_enunciate_structures_house(house); //free the house.
#if DEBUG_ENUNCIATE
    printf("Successfully freed the house from memory.\n");
#endif
    xmlFreeTextWriter(writer); //free the writer
#if DEBUG_ENUNCIATE
    printf("Successfully freed the writer.\n");
#endif
    xmlFreeTextReader(reader); //free the reader
#if DEBUG_ENUNCIATE
    printf("Successfully freed the reader.\n");
#endif
  }
  else if (strcmp("bus", argv[1]) == 0) {
    bus = xml_read_enunciate_vehicles_bus(reader);
    status = xml_write_enunciate_vehicles_bus(writer, bus);
    if (status < 0) {
      //panic
      printf("Problem writing bus.");
      return 1;
    }
#if DEBUG_ENUNCIATE
    else {
      printf("Successfully wrote the bus with status: %i\n", status);
    }
#endif

    free_enunciate_vehicles_bus(bus); //free the bus.
#if DEBUG_ENUNCIATE
    printf("Successfully freed the bus from memory.\n");
#endif
    xmlFreeTextWriter(writer); //free the writer
#if DEBUG_ENUNCIATE
    printf("Successfully freed the writer.\n");
#endif
    xmlFreeTextReader(reader); //free the reader
#if DEBUG_ENUNCIATE
    printf("Successfully freed the reader.\n");
#endif
  }
  else {
    printf("Unrecognized xml type: %s\nUsage: %s [cat|canvas|house|bus||circle|triangle|rectangle] [infile] [outfile]", argv[1], argv[0]);
    return 1;
  }

  return 0;
}