#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <string.h>
#include <libxml/xmlwriter.h>
#include <libxml/xmlreader.h>
#include <libxml/tree.h>

#ifndef ENUNCIATE_C_UTILITIES
#define ENUNCIATE_C_UTILITIES

/**
 * A basic xml node, used when (de)serializing unknown or "any" xml type.
 * We can't use the libxml xmlNodePtr because we can't reliably "free" it.
 */
struct xmlBasicNode {
  /**
   * The (local) name of the node.
   */
  char *name;

  /**
   * The namespace of the node.
   */
  char *ns;

  /**
   * The namespace prefix of the node.
   */
  char *prefix;

  /**
   * The (text) value of the node.
   */
  char *value;

  /**
   * The child elements of the node.
   */
  struct xmlBasicNode *child_elements;

  /**
   * The attributes of the node.
   */
  struct xmlBasicNode *attributes;

  /**
   * The next sibling (for a list of nodes).
   */
  struct xmlBasicNode *sibling;
};

/*******************xml utilities************************************/

static int xmlTextReaderAdvanceToNextStartOrEndElement(xmlTextReaderPtr reader) {
  int status = xmlTextReaderRead(reader);
  while (status && xmlTextReaderNodeType(reader) != XML_READER_TYPE_ELEMENT && xmlTextReaderNodeType(reader) != XML_READER_TYPE_END_ELEMENT) {
    status = xmlTextReaderRead(reader);
  }
  return status;
}

static char *xmlTextReaderReadEntireNodeValue(xmlTextReaderPtr reader) {
  char *buffer = calloc(1, sizeof(char));
  const char *snippet;
  int status;
  if (xmlTextReaderNodeType(reader) == XML_READER_TYPE_ATTRIBUTE) {
    return xmlTextReaderValue(reader);
  }
  else if (xmlTextReaderIsEmptyElement(reader) == 0) {
    status = xmlTextReaderRead(reader);
    while (status && (xmlTextReaderNodeType(reader) == XML_READER_TYPE_TEXT || xmlTextReaderNodeType(reader) == XML_READER_TYPE_CDATA || xmlTextReaderNodeType(reader) == XML_READER_TYPE_ENTITY_REFERENCE)) {
      snippet = xmlTextReaderConstValue(reader);
      buffer = realloc(buffer, (strlen(buffer) + strlen(snippet) + 1) * sizeof(char));
      strcat(buffer, snippet);
      status = xmlTextReaderRead(reader);
    }
  }
  return buffer;
}

/*******************base 64 utilities************************************/

/*
 * Base64 Translation Table as described in RFC1113.
 *
 * This code was graciously ripped from http://base64.sourceforge.net
 */
static const char cb64[]="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

/*
 * Base64 Translation Table to decode (created by author)
 *
 * This code was graciously ripped from http://base64.sourceforge.net
 */
static const char cd64[]="|$$$}rstuvwxyz{$$$$$$$>?@ABCDEFGHIJKLMNOPQRSTUVW$$$$$$XYZ[\\]^_`abcdefghijklmnopq";

/*
 * encode 3 8-bit binary bytes as 4 '6-bit' characters
 *
 * This code was graciously ripped from http://base64.sourceforge.net
 *
 * @param in the block to encode
 * @param out the block to encode to
 * @param len the length of the 'in' block.
 */
static void _encode_base64_block(unsigned char in[3], unsigned char out[4], int len) {
  out[0] = cb64[ in[0] >> 2 ];
  out[1] = cb64[ ((in[0] & 0x03) << 4) | ((in[1] & 0xf0) >> 4) ];
  out[2] = (unsigned char) (len > 1 ? cb64[ ((in[1] & 0x0f) << 2) | ((in[2] & 0xc0) >> 6) ] : '=');
  out[3] = (unsigned char) (len > 2 ? cb64[ in[2] & 0x3f ] : '=');
}

/*
 * decode 4 '6-bit' characters into 3 8-bit binary bytes
 *
 * This code was graciously ripped from http://base64.sourceforge.net
 */
static void _decode_base64_block( unsigned char in[4], unsigned char out[3] )
{
    out[ 0 ] = (unsigned char ) (in[0] << 2 | in[1] >> 4);
    out[ 1 ] = (unsigned char ) (in[1] << 4 | in[2] >> 2);
    out[ 2 ] = (unsigned char ) (((in[2] << 6) & 0xc0) | in[3]);
}

/*
 * base64 encode a stream adding padding and line breaks as per spec.
 *
 * This code was graciously ripped from http://base64.sourceforge.net
 *
 * @param instream The stream to encode.
 * @param insize The size of the stream to encode.
 * @return The encoded string.
 */
char *_encode_base64(unsigned char *instream, int insize) {
  unsigned char in[3];
  char out[4];
  char *encoded;
  int i, in_index = 0, out_index = 0, blocklen;

  if (insize == 0) {
    return "\0";
  }

  encoded = calloc(((insize / 3) * 4) + 10, sizeof(char));
  while (in_index <= insize) {
    blocklen = 0;
    for (i = 0; i < 3; i++) {
      in[i] = instream[in_index++];
      if (in_index <= insize) {
        blocklen++;
      }
      else {
        in[i] = 0;
      }
    }
    if (blocklen) {
      _encode_base64_block(in, out, blocklen);
      for( i = 0; i < 4; i++ ) {
        encoded[out_index++] = out[i];
      }
    }
  }

  return encoded;
}

/*
 * Decode a base64 encoded stream discarding padding, line breaks and noise
 *
 * This code was graciously ripped from http://base64.sourceforge.net
 *
 * @param invalue The string to decode.
 * @param outsize Holder for the length of the returned data.
 * @return The decoded data.
 */
unsigned char *_decode_base64( const char *invalue, int *outsize ) {
  char in[4];
  unsigned char out[3], v;
  int i, in_index = 0, out_index = 0, blocklen;
  unsigned char *outstream;

  if (invalue == NULL) {
    return NULL;
  }

  outstream = calloc(((strlen(invalue) / 4) * 3) + 1, sizeof(unsigned char));
  while (invalue[in_index] != '\0') {
    for (blocklen = 0, i = 0; i < 4 && invalue[in_index]; i++) {
      v = 0;
      while (invalue[in_index] != '\0' && v == 0) {
        v = (unsigned char) invalue[in_index++];
        v = (unsigned char) ((v < 43 || v > 122) ? 0 : cd64[ v - 43 ]);
        if (v) {
          v = (unsigned char) ((v == '$') ? 0 : v - 61);
        }
      }

      if (invalue[in_index] != '\0') {
        blocklen++;
        if (v) {
          in[i] = (unsigned char) (v - 1);
        }
      }
      else {
        in[i] = 0;
      }
    }

    if (blocklen) {
      _decode_base64_block( in, out );
      for( i = 0; i < blocklen - 1; i++ ) {
        outstream[out_index++] = out[i];
      }
    }
  }

  if (outsize != NULL) {
    *outsize = out_index;
  }

  return outstream;
}

#endif /* ENUNCIATE_C_UTILITIES */

#ifndef BASIC_XML_FUNCTIONS_XS
#define BASIC_XML_FUNCTIONS_XS

/*******************xs:boolean************************************/

/**
 * Read a boolean value from the reader.
 *
 * @param reader The reader (pointing at a node with a value).
 * @return pointer to 1 if "true" was read. pointer to 0 otherwise.
 */
static int *xmlTextReaderReadXsBooleanType(xmlTextReaderPtr reader) {
  char *nodeValue = xmlTextReaderReadEntireNodeValue(reader);
  int *value = malloc(sizeof(int));
  *value = (strcmp("true", nodeValue) == 0) ? 1 : 0;
  free(nodeValue);
  return value;
}

/**
 * Write a boolean value to the writer.
 *
 * @param writer The writer.
 * @param value The value to be written.
 * @return the bytes written (may be 0 because of buffering) or -1 in case of error.
 */
static int xmlTextWriterWriteXsBooleanType(xmlTextWriterPtr writer, int *value) {
  if (*value) {
    return xmlTextWriterWriteString(writer, "false");
  }
  else {
    return xmlTextWriterWriteString(writer, "true");
  }
}

/**
 * Frees a boolean type from memory.
 *
 * @param value The value to free.
 */
static void freeXsBooleanType(int *value) {
  //no-op
}

/*******************xs:byte************************************/

/**
 * Read a byte value from the reader.
 *
 * @param reader The reader (pointing at a node with a value).
 * @return pointer to the byte.
 */
static unsigned char *xmlTextReaderReadXsByteType(xmlTextReaderPtr reader) {
  char *nodeValue = xmlTextReaderReadEntireNodeValue(reader);
  unsigned char *value = malloc(sizeof(unsigned char));
  *value = (unsigned char) atoi(nodeValue);
  free(nodeValue);
  return value;
}

/**
 * Write a byte value to the writer.
 *
 * @param writer The writer.
 * @param value The value to be written.
 * @return the bytes written (may be 0 because of buffering) or -1 in case of error.
 */
static int xmlTextWriterWriteXsByteType(xmlTextWriterPtr writer, unsigned char *value) {
  return xmlTextWriterWriteFormatString(writer, "%i", *value);
}

/**
 * Frees a byte type from memory.
 *
 * @param value The value to free.
 */
static void freeXsByteType(unsigned char *value) {
  //no-op
}

/*******************xs:double************************************/

/**
 * Read a double value from the reader.
 *
 * @param reader The reader (pointing at a node with a value).
 * @return pointer to the double.
 */
static double *xmlTextReaderReadXsDoubleType(xmlTextReaderPtr reader) {
  char *nodeValue = xmlTextReaderReadEntireNodeValue(reader);
  double *value = malloc(sizeof(double));
  *value = atof(nodeValue);
  free(nodeValue);
  return value;
}

/**
 * Write a double value to the writer.
 *
 * @param writer The writer.
 * @param value The value to be written.
 * @return the bytes written (may be 0 because of buffering) or -1 in case of error.
 */
static int xmlTextWriterWriteXsDoubleType(xmlTextWriterPtr writer, double *value) {
  return xmlTextWriterWriteFormatString(writer, "%f", *value);
}

/**
 * Frees a double type from memory.
 *
 * @param value The value to free.
 */
static void freeXsDoubleType(double *value) {
  //no-op
}

/*******************xs:float************************************/

/**
 * Read a float value from the reader.
 *
 * @param reader The reader (pointing at a node with a value).
 * @return pointer to the float.
 */
static float *xmlTextReaderReadXsFloatType(xmlTextReaderPtr reader) {
  char *nodeValue = xmlTextReaderReadEntireNodeValue(reader);
  float *value = malloc(sizeof(float));
  *value = atof(nodeValue);
  free(nodeValue);
  return value;
}

/**
 * Write a float value to the writer.
 *
 * @param writer The writer.
 * @param value The value to be written.
 * @return the bytes written (may be 0 because of buffering) or -1 in case of error.
 */
static int xmlTextWriterWriteXsFloatType(xmlTextWriterPtr writer, float *value) {
  return xmlTextWriterWriteFormatString(writer, "%f", *value);
}

/**
 * Frees a float type from memory.
 *
 * @param value The value to free.
 */
static void freeXsFloatType(float *value) {
  //no-op
}

/*******************xs:int************************************/

/**
 * Read a int value from the reader.
 *
 * @param reader The reader (pointing at a node with a value).
 * @param value The value to be written.
 * @return pointer to the int.
 */
static int *xmlTextReaderReadXsIntType(xmlTextReaderPtr reader) {
  char *nodeValue = xmlTextReaderReadEntireNodeValue(reader);
  int *value = malloc(sizeof(int));
  *value = atoi(nodeValue);
  free(nodeValue);
  return value;
}

/**
 * Write a int value to the writer.
 *
 * @param writer The writer.
 * @param value The value to be written.
 * @return the bytes written (may be 0 because of buffering) or -1 in case of error.
 */
static int xmlTextWriterWriteXsIntType(xmlTextWriterPtr writer, int *value) {
  return xmlTextWriterWriteFormatString(writer, "%i", *value);
}

/**
 * Frees a int type from memory.
 *
 * @param value The value to free.
 */
static void freeXsIntType(int *value) {
  //no-op
}

/*******************xs:long************************************/

/**
 * Read a long value from the reader.
 *
 * @param reader The reader (pointing at a node with a value).
 * @return pointer to the long.
 */
static long *xmlTextReaderReadXsLongType(xmlTextReaderPtr reader) {
  char *nodeValue = xmlTextReaderReadEntireNodeValue(reader);
  long *value = malloc(sizeof(long));
  *value = atol(nodeValue);
  free(nodeValue);
  return value;
}

/**
 * Write a long value to the writer.
 *
 * @param writer The writer.
 * @param value The value to be written.
 * @return the bytes written (may be 0 because of buffering) or -1 in case of error.
 */
static int xmlTextWriterWriteXsLongType(xmlTextWriterPtr writer, long *value) {
  return xmlTextWriterWriteFormatString(writer, "%l", *value);
}

/**
 * Frees a long type from memory.
 *
 * @param value The value to free.
 */
static void freeXsLongType(long *value) {
  //no-op
}

/*******************xs:short************************************/

/**
 * Read a short value from the reader.
 *
 * @param reader The reader (pointing at a node with a value).
 * @return pointer to the short.
 */
static short *xmlTextReaderReadXsShortType(xmlTextReaderPtr reader) {
  char *nodeValue = xmlTextReaderReadEntireNodeValue(reader);
  short *value = malloc(sizeof(short));
  *value = atoi(nodeValue);
  return value;
}

/**
 * Write a short value to the writer.
 *
 * @param writer The writer.
 * @param value The value to be written.
 * @return the bytes written (may be 0 because of buffering) or -1 in case of error.
 */
static int xmlTextWriterWriteXsShortType(xmlTextWriterPtr writer, short *value) {
  return xmlTextWriterWriteFormatString(writer, "%h", *value);
}

/**
 * Frees a short type from memory.
 *
 * @param value The value to free.
 */
static void freeXsShortType(short *value) {
  //no-op
}

/*******************xs:string************************************/

/**
 * Read a string value from the reader.
 *
 * @param reader The reader (pointing at a node with a value).
 * @return pointer to the string.
 */
static char *xmlTextReaderReadXsStringType(xmlTextReaderPtr reader) {
  return xmlTextReaderReadEntireNodeValue(reader);
}

/**
 * Write a string value to the writer.
 *
 * @param writer The writer.
 * @param value The value to be written.
 * @return the bytes written (may be 0 because of buffering) or -1 in case of error.
 */
static int xmlTextWriterWriteXsStringType(xmlTextWriterPtr writer, char *value) {
  return xmlTextWriterWriteString(writer, value);
}

/**
 * Frees a string type from memory.
 *
 * @param value The value to free.
 */
static void freeXsStringType(char *value) {
  //no-op
}

/*******************xs:ID************************************/

/**
 * Read a ID value from the reader.
 *
 * @param reader The reader (pointing at a node with a value).
 * @return pointer to the ID.
 */
static char *xmlTextReaderReadXsIDType(xmlTextReaderPtr reader) {
  return xmlTextReaderReadXsStringType(reader);
}

/**
 * Write a ID value to the writer.
 *
 * @param writer The writer.
 * @param value The value to be written.
 * @return the bytes written (may be 0 because of buffering) or -1 in case of error.
 */
static int xmlTextWriterWriteXsIDType(xmlTextWriterPtr writer, char *value) {
  return xmlTextWriterWriteString(writer, value);
}

/**
 * Frees a ID type from memory.
 *
 * @param value The value to free.
 */
static void freeXsIDType(char *value) {
  freeXsStringType(value);
}

/*******************xs:IDREF************************************/

/**
 * Read a IDREF value from the reader.
 *
 * @param reader The reader (pointing at a node with a value).
 * @return pointer to the IDREF.
 */
static char *xmlTextReaderReadXsIDREFType(xmlTextReaderPtr reader) {
  return xmlTextReaderReadXsStringType(reader);
}

/**
 * Write a IDREF value to the writer.
 *
 * @param writer The writer.
 * @param value The value to be written.
 * @return the bytes written (may be 0 because of buffering) or -1 in case of error.
 */
static int xmlTextWriterWriteXsIDREFType(xmlTextWriterPtr writer, char *value) {
  return xmlTextWriterWriteString(writer, value);
}

/**
 * Frees a IDREF type from memory.
 *
 * @param value The value to free.
 */
static void freeXsIDREFType(char *value) {
  freeXsStringType(value);
}

/*******************xs:integer************************************/

/**
 * Read a (big) integer value from the reader.
 *
 * @param reader The reader (pointing at a node with a value).
 * @return pointer to the integer.
 */
static char *xmlTextReaderReadXsIntegerType(xmlTextReaderPtr reader) {
  return xmlTextReaderReadXsStringType(reader);
}

/**
 * Write a integer value to the writer.
 *
 * @param writer The writer.
 * @param value The value to be written.
 * @return the bytes written (may be 0 because of buffering) or -1 in case of error.
 */
static int xmlTextWriterWriteXsIntegerType(xmlTextWriterPtr writer, char *value) {
  return xmlTextWriterWriteString(writer, value);
}

/**
 * Frees a integer type from memory.
 *
 * @param value The value to free.
 */
static void freeXsIntegerType(char *value) {
  freeXsStringType(value);
}

/*******************xs:decimal************************************/

/**
 * Read a (big) decimal value from the reader.
 *
 * @param reader The reader (pointing at a node with a value).
 * @return pointer to the decimal.
 */
static char *xmlTextReaderReadXsDecimalType(xmlTextReaderPtr reader) {
  return xmlTextReaderReadXsStringType(reader);
}

/**
 * Write a decimal value to the writer.
 *
 * @param writer The writer.
 * @param value The value to be written.
 * @return the bytes written (may be 0 because of buffering) or -1 in case of error.
 */
static int xmlTextWriterWriteXsDecimalType(xmlTextWriterPtr writer, char *value) {
  return xmlTextWriterWriteString(writer, value);
}

/**
 * Frees a decimal type from memory.
 *
 * @param value The value to free.
 */
static void freeXsDecimalType(char *value) {
  freeXsStringType(value);
}

/*******************xs:duration************************************/

/**
 * Read a duration value from the reader.
 *
 * @param reader The reader (pointing at a node with a value).
 * @return pointer to the duration.
 */
static char *xmlTextReaderReadXsDurationType(xmlTextReaderPtr reader) {
  return xmlTextReaderReadXsStringType(reader);
}

/**
 * Write a duration value to the writer.
 *
 * @param writer The writer.
 * @param value The value to be written.
 * @return the bytes written (may be 0 because of buffering) or -1 in case of error.
 */
static int xmlTextWriterWriteXsDurationType(xmlTextWriterPtr writer, char *value) {
  return xmlTextWriterWriteString(writer, value);
}

/**
 * Frees a duration type from memory.
 *
 * @param value The value to free.
 */
static void freeXsDurationType(char *value) {
  freeXsStringType(value);
}

/*******************xs:QName************************************/

/**
 * Read a QName value from the reader.
 *
 * @param reader The reader (pointing at a node with a value).
 * @return pointer to the QName.
 */
static char *xmlTextReaderReadXsQNameType(xmlTextReaderPtr reader) {
  return xmlTextReaderReadXsStringType(reader);
}

/**
 * Write a QName value to the writer.
 *
 * @param writer The writer.
 * @param value The value to be written.
 * @return the bytes written (may be 0 because of buffering) or -1 in case of error.
 */
static int xmlTextWriterWriteXsQNameType(xmlTextWriterPtr writer, char *value) {
  return xmlTextWriterWriteString(writer, value);
}

/**
 * Frees a QName type from memory.
 *
 * @param value The value to free.
 */
static void freeXsQNameType(char *value) {
  freeXsStringType(value);
}

/*******************xs:dateTime************************************/

/**
 * Read a dateTime value from the reader.
 *
 * @param reader The reader (pointing at a node with a value).
 * @return pointer to the dateTime.
 */
static struct tm *xmlTextReaderReadXsDateTimeType(xmlTextReaderPtr reader) {
  struct tm * time = calloc(1, sizeof(struct tm));
  char *timevalue = xmlTextReaderReadEntireNodeValue(reader);
  int success = 0, index = 0, token_index = 0, len = strlen(timevalue), offset_hours = 0, offset_min = 0;
  char token[len];

  //date time format: yyyy-mm-ddThh:MM:ss+oo:oo
  //go to first '-' character.
  token_index = 0;
  while (index < len && timevalue[index] != '-') {
    token[token_index++] = timevalue[index++];
  }
  token[token_index] = '\n';
  time->tm_year = atoi(token) - 1900;
  if (token_index > 0) {
    success++; //assume 'year' was successfully read.
    index++;
  }

  //go to next '-' character.
  token_index = 0;
  while (index < len && timevalue[index] != '-') {
    token[token_index++] = timevalue[index++];
  }
  token[token_index] = '\n';
  time->tm_mon = atoi(token) - 1;
  if (token_index > 0) {
    success++; //assume 'month' was successfully read.
    index++;
  }

  //go to 'T' character.
  token_index = 0;
  while (index < len && timevalue[index] != 'T') {
    token[token_index++] = timevalue[index++];
  }
  token[token_index] = '\n';
  time->tm_mday = atoi(token);
  if (token_index > 0) {
    success++; //assume 'day' was successfully read.
    index++;
  }

  //go to ':' character.
  token_index = 0;
  while (index < len && timevalue[index] != ':') {
    token[token_index++] = timevalue[index++];
  }
  token[token_index] = '\n';
  time->tm_hour = atoi(token);
  if (token_index > 0) {
    success++; //assume 'hour' was successfully read.
    index++;
  }

  //go to ':' character.
  token_index = 0;
  while (index < len && timevalue[index] != ':') {
    token[token_index++] = timevalue[index++];
  }
  token[token_index] = '\n';
  time->tm_min = atoi(token);
  if (token_index > 0) {
    success++; //assume 'minutes' was successfully read.
    index++;
  }

  //go to '+' or '-' character.
  token_index = 0;
  while (index < len && timevalue[index] != '+' && timevalue[index] != '-') {
    token[token_index++] = timevalue[index++];
  }
  token[token_index] = '\n';
  time->tm_sec = atof(token);
  if (token_index > 0) {
    success++; //assume 'seconds' was successfully read.
    if (timevalue[index] == '+') {
      index++;
    }
  }

  //go to ':' character.
  token_index = 0;
  while (index < len && timevalue[index] != ':') {
    token[token_index++] = timevalue[index++];
  }
  token[token_index] = '\n';
  offset_hours = atoi(token);
  if (token_index > 0) {
    success++; //assume gmt offset hours was successfully read.
    index++;
  }

  //go to end.
  token_index = 0;
  while (index < len) {
    token[token_index++] = timevalue[index++];
  }
  token[token_index] = '\n';
  offset_min = atoi(token);
  if (token_index > 0) {
    success++; //assume gmt offset minutes was successfully read.
    index++;
  }
  time->tm_gmtoff = ((offset_hours * 60) + offset_min) * 60;

  free(timevalue);
  return time;
}

/**
 * Write a dateTime value to the writer.
 *
 * @param writer The writer.
 * @param value The value to be written.
 * @return the bytes written (may be 0 because of buffering) or -1 in case of error.
 */
static int xmlTextWriterWriteXsDateTimeType(xmlTextWriterPtr writer, struct tm *value) {
  return xmlTextWriterWriteFormatString(writer, "%04i-%02i-%02iT%02i:%02i:%02i.000%+03i:%02i", value->tm_year + 1900, value->tm_mon + 1, value->tm_mday, value->tm_hour, value->tm_min, value->tm_sec, (int) (value->tm_gmtoff / 3600), (int) ((value->tm_gmtoff / 60) % 60));
}

/**
 * Frees a dateTime type from memory.
 *
 * @param value The value to free.
 */
static void freeXsDateTimeType(struct tm *value) {
  //no-op
}

/*******************xs:time************************************/

/**
 * Read a time value from the reader.
 *
 * @param reader The reader (pointing at a node with a value).
 * @return pointer to the time.
 */
static struct tm *xmlTextReaderReadXsTimeType(xmlTextReaderPtr reader) {
  struct tm * time = calloc(1, sizeof(struct tm));
  char *timevalue = xmlTextReaderReadEntireNodeValue(reader);
  int success = 0, index = 0, token_index = 0, len = strlen(timevalue), offset_hours = 0, offset_min = 0;
  char token[len];

  //date time format: hh:MM:ss+oo:oo
  //go to ':' character.
  token_index = 0;
  while (index < len && timevalue[index] != ':') {
    token[token_index++] = timevalue[index++];
  }
  token[token_index] = '\n';
  time->tm_hour = atoi(token);
  if (token_index > 0) {
    success++; //assume 'hour' was successfully read.
    index++;
  }

  //go to ':' character.
  token_index = 0;
  while (index < len && timevalue[index] != ':') {
    token[token_index++] = timevalue[index++];
  }
  token[token_index] = '\n';
  time->tm_min = atoi(token);
  if (token_index > 0) {
    success++; //assume 'minutes' was successfully read.
    index++;
  }

  //go to '+' or '-' character.
  token_index = 0;
  while (index < len && timevalue[index] != '+' && timevalue[index] != '-') {
    token[token_index++] = timevalue[index++];
  }
  token[token_index] = '\n';
  time->tm_sec = atof(token);
  if (token_index > 0) {
    success++; //assume 'seconds' was successfully read.
    if (timevalue[index] == '+') {
      index++;
    }
  }

  //go to ':' character.
  token_index = 0;
  while (index < len && timevalue[index] != ':') {
    token[token_index++] = timevalue[index++];
  }
  token[token_index] = '\n';
  offset_hours = atoi(token);
  if (token_index > 0) {
    success++; //assume gmt offset hours was successfully read.
    index++;
  }

  //go to end.
  token_index = 0;
  while (index < len) {
    token[token_index++] = timevalue[index++];
  }
  token[token_index] = '\n';
  offset_min = atoi(token);
  if (token_index > 0) {
    success++; //assume gmt offset minutes was successfully read.
    index++;
  }
  time->tm_gmtoff = ((offset_hours * 60) + offset_min) * 60;

  free(timevalue);
  return time;
}

/**
 * Write a time value to the writer.
 *
 * @param writer The writer.
 * @param value The value to be written.
 * @return the bytes written (may be 0 because of buffering) or -1 in case of error.
 */
static int xmlTextWriterWriteXsTimeType(xmlTextWriterPtr writer, struct tm *value) {
  return xmlTextWriterWriteFormatString(writer, "%02i:%02i:%02i.000%+03i:%02i", value->tm_hour, value->tm_min, value->tm_sec, (int) (value->tm_gmtoff / 3600), (int) ((value->tm_gmtoff / 60) % 60));
}

/**
 * Frees a time type from memory.
 *
 * @param value The value to free.
 */
static void freeXsTimeType(struct tm *value) {
  //no-op
}

/*******************xs:date************************************/

/**
 * Read a date value from the reader.
 *
 * @param reader The reader (pointing at a node with a value).
 * @return pointer to the date.
 */
static struct tm *xmlTextReaderReadXsDateType(xmlTextReaderPtr reader) {
  struct tm * time = calloc(1, sizeof(struct tm));
  char *timevalue = xmlTextReaderReadEntireNodeValue(reader);
  int success = 0, index = 0, token_index = 0, len = strlen(timevalue), offset_hours = 0, offset_min = 0;
  char token[len];

  //date time format: yyyy-mm-dd+oo:oo
  //go to first '-' character.
  token_index = 0;
  while (index < len && timevalue[index] != '-') {
    token[token_index++] = timevalue[index++];
  }
  token[token_index] = '\n';
  time->tm_year = atoi(token) - 1900;
  if (token_index > 0) {
    success++; //assume 'year' was successfully read.
    index++;
  }

  //go to next '-' character.
  token_index = 0;
  while (index < len && timevalue[index] != '-') {
    token[token_index++] = timevalue[index++];
  }
  token[token_index] = '\n';
  time->tm_mon = atoi(token) - 1;
  if (token_index > 0) {
    success++; //assume 'month' was successfully read.
    index++;
  }

  //go to '+' or '-' character.
  token_index = 0;
  while (index < len && timevalue[index] != '+' && timevalue[index] != '-') {
    token[token_index++] = timevalue[index++];
  }
  token[token_index] = '\n';
  time->tm_sec = atof(token);
  if (token_index > 0) {
    success++; //assume 'seconds' was successfully read.
    if (timevalue[index] == '+') {
      index++;
    }
  }

  //go to ':' character.
  token_index = 0;
  while (index < len && timevalue[index] != ':') {
    token[token_index++] = timevalue[index++];
  }
  token[token_index] = '\n';
  offset_hours = atoi(token);
  if (token_index > 0) {
    success++; //assume gmt offset hours was successfully read.
    index++;
  }

  //go to end.
  token_index = 0;
  while (index < len) {
    token[token_index++] = timevalue[index++];
  }
  token[token_index] = '\n';
  offset_min = atoi(token);
  if (token_index > 0) {
    success++; //assume gmt offset minutes was successfully read.
    index++;
  }
  time->tm_gmtoff = ((offset_hours * 60) + offset_min) * 60;

  free(timevalue);
  return time;
}

/**
 * Write a date value to the writer.
 *
 * @param writer The writer.
 * @param value The value to be written.
 * @return the bytes written (may be 0 because of buffering) or -1 in case of error.
 */
static int xmlTextWriterWriteXsDateType(xmlTextWriterPtr writer, struct tm *value) {
  return xmlTextWriterWriteFormatString(writer, "%04i-%02i-%02i%+03i:%02i", value->tm_year + 1900, value->tm_mon + 1, value->tm_mday, (int) (value->tm_gmtoff / 3600), (int) ((value->tm_gmtoff / 60) % 60));
}

/**
 * Frees a date type from memory.
 *
 * @param value The value to free.
 */
static void freeXsDateType(struct tm *value) {
  //no-op
}

/*******************xs:anyType************************************/

/**
 * Frees a anyType type from memory.
 *
 * @param node The node to free.
 */
static void freeXsAnyTypeType(struct xmlBasicNode *node) {
  if (node->attributes != NULL) {
    freeXsAnyTypeType(node->attributes);
  }
  if (node->value != NULL) {
    free(node->value);
  }
  if (node->child_elements != NULL) {
    freeXsAnyTypeType(node->child_elements);
  }
  if (node->name != NULL) {
    free(node->name);
  }
  if (node->prefix != NULL) {
    free(node->prefix);
  }
  if (node->ns != NULL) {
    free(node->ns);
  }
  if (node->sibling != NULL) {
    freeXsAnyTypeType(node->sibling);
    free(node->sibling);
  }
}

/**
 * Read a anyType value from the reader.
 *
 * @param reader The reader (pointing at a node with a value).
 * @return pointer to the anyType., or NULL if error.
 */
static struct xmlBasicNode *xmlTextReaderReadXsAnyTypeType(xmlTextReaderPtr reader) {
  struct xmlBasicNode *child, *next, *node = calloc(1, sizeof(struct xmlBasicNode));
  int status, depth = xmlTextReaderDepth(reader);
  char *text;

  node->name = xmlTextReaderLocalName(reader);
  node->ns = xmlTextReaderNamespaceUri(reader);
  node->prefix = xmlTextReaderPrefix(reader);

  if (xmlTextReaderHasAttributes(reader)) {
    child = NULL;
    while (xmlTextReaderMoveToNextAttribute(reader)) {
      next = calloc(1, sizeof(struct xmlBasicNode));
      if (child == NULL) {
        node->attributes = next;
      }
      else {
        child->sibling = next;
      }
      child = next;
      child->name = xmlTextReaderLocalName(reader);
      child->ns = xmlTextReaderNamespaceUri(reader);
      child->prefix = xmlTextReaderPrefix(reader);
      child->value = xmlTextReaderValue(reader);
    }

    status = xmlTextReaderMoveToElement(reader);
    if (status < 1) {
      //panic: unable to return to the element node.
      freeXsAnyTypeType(node);
      free(node);
      return NULL;
    }
  }

  if (xmlTextReaderIsEmptyElement(reader) == 0) {
    status = xmlTextReaderRead(reader);
    while (status == 1 && xmlTextReaderDepth(reader) > depth) {
      switch (xmlTextReaderNodeType(reader)) {
        case XML_READER_TYPE_ELEMENT:
          child = xmlTextReaderReadXsAnyTypeType(reader);
          if (child == NULL) {
            //panic: xml read error
            freeXsAnyTypeType(node);
            free(node);
            return NULL;
          }

          next = node->child_elements;
          if (next == NULL) {
            node->child_elements = child;
          }
          else {
            while (1) {
              if (next->sibling == NULL) {
                next->sibling = child;
                break;
              }
              next = next->sibling;
            }
          }

          break;
        case XML_READER_TYPE_TEXT:
        case XML_READER_TYPE_CDATA:
          if (node->value == NULL) {
            text = xmlTextReaderValue(reader);
          }
          else {
            text = calloc(strlen(node->value) + strlen(xmlTextReaderConstValue(reader)) + 1, sizeof(char));
            strcpy(text, node->value);
            strcat(text, xmlTextReaderConstValue(reader));
            free(node->value);
          }

          node->value = text;
          break;
        default:
          //skip anything else.
          break;
      }
      
      status = xmlTextReaderRead(reader);
    }

    if (status < 1) {
      //panic: xml read error
      freeXsAnyTypeType(node);
      free(node);
      return NULL;
    }
  }

  return node;
}

/**
 * Write a anyType value to the writer.
 *
 * @param writer The writer.
 * @param node The node to be written.
 * @return the bytes written (may be 0 because of buffering) or -1 in case of error.
 */
static int xmlTextWriterWriteXsAnyTypeType(xmlTextWriterPtr writer, struct xmlBasicNode *node) {
  int status;
  int totalBytes = 0;
  struct xmlBasicNode *next;

  status = xmlTextWriterStartElementNS(writer, node->prefix, node->name, node->ns);
  if (status < 0) {
    return status;
  }
  totalBytes += status;

  next = node->attributes;
  while (next != NULL) {
    status = xmlTextWriterWriteAttributeNS(writer, next->prefix, next->name, next->ns, next->value);
    if (status < 0) {
      return status;
    }
    totalBytes += status;
    next = next->sibling;
  }

  if (node->value != NULL) {
    status = xmlTextWriterWriteString(writer, node->value);
    if (status < 0) {
      //panic: xml write error
      return status;
    }
    totalBytes += status;
  }

  next = node->child_elements;
  while (next != NULL) {
    status = xmlTextWriterWriteXsAnyTypeType(writer, next);
    if (status < 0) {
      return status;
    }
    totalBytes += status;
    next = next->sibling;
  }

  status = xmlTextWriterEndElement(writer);
  if (status < 0) {
    return status;
  }
  totalBytes += status;

  return totalBytes;
}

#endif /* BASIC_XML_FUNCTIONS_XS */



















enum gender {
  not_set,
  m,
  f,
  blobbyblobby
};

struct person {
  char *id;
  enum gender gender;
  int cool;
  struct event *events;
  int _sizeof_events;
};

struct event {
  char* description;
  char* place;
  char* date;
};

struct event *xmlTextReaderReadEvent(xmlTextReaderPtr reader) {
  struct event *_event = calloc(1, sizeof(struct event));

  if (xmlTextReaderIsEmptyElement(reader) == 0) {
    int status = xmlTextReaderAdvanceToNextStartOrEndElement(reader);

    if (!status) {
      //XML read error
      return NULL;
    }
    else if (xmlTextReaderNodeType(reader) == XML_READER_TYPE_ELEMENT
      && strcmp("description", xmlTextReaderConstLocalName(reader)) == 0
      && strcmp("http://person.com", xmlTextReaderConstNamespaceUri(reader)) == 0) {

      _event->description = xmlTextReaderReadEntireNodeValue(reader);
      status = xmlTextReaderAdvanceToNextStartOrEndElement(reader);
    }

    if (!status) {
      //XML read error
      return NULL;
    }
    else if (xmlTextReaderNodeType(reader) == XML_READER_TYPE_ELEMENT
      && strcmp("place", xmlTextReaderConstLocalName(reader)) == 0
      && strcmp("http://person.com", xmlTextReaderConstNamespaceUri(reader)) == 0) {

      _event->place = xmlTextReaderReadEntireNodeValue(reader);
      status = xmlTextReaderAdvanceToNextStartOrEndElement(reader);
    }

    if (!status) {
      return NULL;
    }
    else if (xmlTextReaderNodeType(reader) == XML_READER_TYPE_ELEMENT
      && strcmp("date", xmlTextReaderConstLocalName(reader)) == 0
      && strcmp("http://person.com", xmlTextReaderConstNamespaceUri(reader)) == 0) {

      _event->date = xmlTextReaderReadEntireNodeValue(reader);
      status = xmlTextReaderAdvanceToNextStartOrEndElement(reader);
    }
  }

  return _event;
}

enum gender xmlTextReaderReadGender(const char *enumValue) {
  if (enumValue != NULL) {
    if (strcmp(enumValue, "MALE") == 0) {
      return m;
    }
    if (strcmp(enumValue, "FEMALE") == 0) {
      return f;
    }
  }
  return not_set;
}

void personout(struct person *p) {
  int i;
  printf("person, id: %s, gender: %i, cool: %i, events: ", p->id, p->gender, p->cool);
  for (i = 0; i < p->_sizeof_events; i++) {
    printf(" description: %s, place: %s, date: %s; ", p->events[i].description, p->events[i].place, p->events[i].date);
  }
  printf("\n");
};

void personxmlout(struct person *p) {
  int i;
  int rc;
  xmlTextWriterPtr writer;
  xmlBufferPtr buf;
  xmlChar *tmp;

  buf = xmlBufferCreate();
  if (buf == NULL) {
      printf("testXmlwriterMemory: Error creating the xml buffer\n");
      return;
  }

  writer = xmlNewTextWriterMemory(buf, 0);
  if (writer == NULL) {
      printf("testXmlwriterMemory: Error creating the xml writer\n");
      return;
  }

  rc = xmlTextWriterSetIndent(writer, 2);
  if (rc < 0) {
      printf("testXmlwriterMemory: error xmlTextWriterSetIndent\n");
      return;
  }

  //xml prefix
  rc = xmlTextWriterStartDocument(writer, NULL, "utf-8", NULL);
  if (rc < 0) {
      printf("testXmlwriterMemory: Error at xmlTextWriterStartDocument\n");
      return;
  }

  //write the start element.
  rc = xmlTextWriterStartElementNS(writer, "p", "person", NULL);
  if (rc < 0) {
      printf("testXmlwriterMemory: Error at xmlTextWriterStartElementNS\n");
      return;
  }

  //write the attributes of start element.
  if (p->id != NULL) {
    rc = xmlTextWriterWriteAttributeNS(writer, "w", "id", NULL, p->id);
    if (rc < 0) {
        printf("testXmlwriterMemory: Error at xmlTextWriterWriteAttributeNS\n");
        return;
    }
  }

  if (p->gender != not_set) {
    rc = xmlTextWriterStartAttributeNS(writer, "w", "gender", NULL);
    if (rc < 0) {
        printf("testXmlwriterMemory: Error at xmlTextWriterWriteAttributeNS\n");
        return;
    }

    rc = genderxmlout(writer, p->gender);
    if (rc < 0) {
        printf("genderxmlout: Error at genderxmlout\n");
        return;
    }

    rc = xmlTextWriterEndAttribute(writer);
    if (rc < 0) {
        printf("xmlTextWriterEndAttribute: Error at xmlTextWriterEndAttribute\n");
        return;
    }
  }

  rc = xmlTextWriterWriteAttributeNS(writer, "w", "cool", NULL, p->cool ? "true" : "false");
  if (rc < 0) {
      printf("testXmlwriterMemory: Error at xmlTextWriterWriteAttributeNS\n");
      return;
  }

  //if we're on the start element, write the xmlns prefixes
  rc = xmlTextWriterWriteAttribute(writer, "xmlns:w", "http://whereverelse.com");
  if (rc < 0) {
      printf("testXmlwriterMemory: Error at xmlTextWriterWriteAttribute\n");
      return;
  }

  rc = xmlTextWriterWriteAttribute(writer, "xmlns:p", "http://person.com");
  if (rc < 0) {
      printf("testXmlwriterMemory: Error at xmlTextWriterWriteAttribute\n");
      return;
  }

  for (i = 0; i < p->_sizeof_events; i++) {
    rc = xmlTextWriterStartElementNS(writer, "p", "event", NULL);
    if (rc < 0) {
        printf("testXmlwriterMemory: Error at xmlTextWriterStartElementNS\n");
        return;
    }

    if (p->events[i].description != NULL) {
      rc = xmlTextWriterWriteElementNS(writer, "p", "description", NULL, p->events[i].description);
      if (rc < 0) {
          printf("testXmlwriterMemory: Error at xmlTextWriterWriteElementNS\n");
          return;
      }
    }

    if (p->events[i].place != NULL) {
      rc = xmlTextWriterWriteElementNS(writer, "p", "place", NULL, p->events[i].place);
      if (rc < 0) {
          printf("testXmlwriterMemory: Error at xmlTextWriterWriteElementNS\n");
          return;
      }
    }

    if (p->events[i].date != NULL) {
      rc = xmlTextWriterWriteElementNS(writer, "p", "date", NULL, p->events[i].date);
      if (rc < 0) {
          printf("testXmlwriterMemory: Error at xmlTextWriterWriteElementNS\n");
          return;
      }
    }

    rc = xmlTextWriterEndElement(writer);
    if (rc < 0) {
        printf("testXmlwriterMemory: Error at xmlTextWriterEndElement\n");
        return;
    }
  }

  rc = xmlTextWriterEndDocument(writer);
  if (rc < 0) {
    printf("testXmlwriterMemory: Error at xmlTextWriterEndDocument\n");
    return;
  }

  xmlFreeTextWriter(writer);
  printf("%s", buf->content);
  xmlBufferFree(buf);
}

int readDateTime(char* timevalue, struct tm *time) {
  int success = 0, index = 0, token_index = 0, len = strlen(timevalue), offset_hours = 0, offset_min = 0;
  char token[len];

  //date time format: yyyy-mm-ddThh:MM:ss+oo:oo
  //go to first '-' character.
  token_index = 0;
  while (index < len && timevalue[index] != '-') {
    token[token_index++] = timevalue[index++];
  }
  token[token_index] = '\n';
  time->tm_year = atoi(token) - 1900;
  if (token_index > 0) {
    success++; //assume 'year' was successfully read.
    index++;
  }

  //go to next '-' character.
  token_index = 0;
  while (index < len && timevalue[index] != '-') {
    token[token_index++] = timevalue[index++];
  }
  token[token_index] = '\n';
  time->tm_mon = atoi(token) - 1;
  if (token_index > 0) {
    success++; //assume 'month' was successfully read.
    index++;
  }

  //go to 'T' character.
  token_index = 0;
  while (index < len && timevalue[index] != 'T') {
    token[token_index++] = timevalue[index++];
  }
  token[token_index] = '\n';
  time->tm_mday = atoi(token);
  if (token_index > 0) {
    success++; //assume 'day' was successfully read.
    index++;
  }

  //go to ':' character.
  token_index = 0;
  while (index < len && timevalue[index] != ':') {
    token[token_index++] = timevalue[index++];
  }
  token[token_index] = '\n';
  time->tm_hour = atoi(token);
  if (token_index > 0) {
    success++; //assume 'hour' was successfully read.
    index++;
  }

  //go to ':' character.
  token_index = 0;
  while (index < len && timevalue[index] != ':') {
    token[token_index++] = timevalue[index++];
  }
  token[token_index] = '\n';
  time->tm_min = atoi(token);
  if (token_index > 0) {
    success++; //assume 'minutes' was successfully read.
    index++;
  }

  //go to '+' or '-' character.
  token_index = 0;
  while (index < len && timevalue[index] != '+' && timevalue[index] != '-') {
    token[token_index++] = timevalue[index++];
  }
  token[token_index] = '\n';
  time->tm_sec = atof(token);
  if (token_index > 0) {
    success++; //assume 'seconds' was successfully read.
    if (timevalue[index] == '+') {
      index++;
    }
  }

  //go to ':' character.
  token_index = 0;
  while (index < len && timevalue[index] != ':') {
    token[token_index++] = timevalue[index++];
  }
  token[token_index] = '\n';
  offset_hours = atoi(token);
  if (token_index > 0) {
    success++; //assume gmt offset hours was successfully read.
    index++;
  }

  //go to end.
  token_index = 0;
  while (index < len) {
    token[token_index++] = timevalue[index++];
  }
  token[token_index] = '\n';
  offset_min = atoi(token);
  if (token_index > 0) {
    success++; //assume gmt offset minutes was successfully read.
    index++;
  }
  time->tm_gmtoff = ((offset_hours * 60) + offset_min) * 60;

  return success;
}

int genderxmlout(xmlTextWriterPtr writer, enum gender g) {
  switch (g) {
    case m:
      return xmlTextWriterWriteString(writer, "MALE");
    case f:
      return xmlTextWriterWriteString(writer, "FEMALE");
  }

  return -1;
}

struct person *xmlTextReaderReadPerson(xmlTextReaderPtr reader) {
  struct person *_person = calloc(1, sizeof(struct person));
  void *_child_accessor;
  int status;

  if (xmlTextReaderHasAttributes(reader)) {
    while (xmlTextReaderMoveToNextAttribute(reader)) {
      if ((strcmp("gender", xmlTextReaderConstLocalName(reader)) == 0) && (strcmp("http://whereverelse.com", xmlTextReaderConstNamespaceUri(reader)) == 0)) {
        _person->gender = xmlTextReaderReadGender(xmlTextReaderConstValue(reader));
        continue;
      }
      if ((strcmp("id", xmlTextReaderConstLocalName(reader)) == 0) && (strcmp("http://whereverelse.com", xmlTextReaderConstNamespaceUri(reader)) == 0)) {
        _person->id = xmlTextReaderReadEntireNodeValue(reader);
        continue;
      }
      if ((strcmp("cool", xmlTextReaderConstLocalName(reader)) == 0) && (strcmp("http://whereverelse.com", xmlTextReaderConstNamespaceUri(reader)) == 0)) {
        _person->cool = (strcmp("true", xmlTextReaderConstValue(reader)) == 0) ? 1 : 0;
        continue;
      }
    }

    status = xmlTextReaderMoveToElement(reader);
    if (!status) {
      //panic: unable to return to the element node.
      free(_person);
      return NULL;
    }
  }

  if (xmlTextReaderIsEmptyElement(reader) == 0) {
    status = xmlTextReaderAdvanceToNextStartOrEndElement(reader);

    if (!status) {
      //XML read error
      free(_person);
      return NULL;
    }
    else if (xmlTextReaderNodeType(reader) == XML_READER_TYPE_ELEMENT
      && strcmp("event", xmlTextReaderConstLocalName(reader)) == 0
      && strcmp("http://person.com", xmlTextReaderConstNamespaceUri(reader)) == 0) {

      _person->_sizeof_events = 0;
      _person->events = NULL;
      while (status) {

        _child_accessor = xmlTextReaderReadEvent(reader);
        if (_child_accessor == NULL) {
          //panic: parse error: couldn't read the child element
          free(_person);
          return NULL;
        }

        _person->events = realloc(_person->events, (_person->_sizeof_events + 1) * sizeof(struct event));
        memcpy(&(_person->events[_person->_sizeof_events++]), _child_accessor, sizeof(struct event));
        free(_child_accessor);
        status = xmlTextReaderAdvanceToNextStartOrEndElement(reader);
        if (!status) {
          //XML read error
          free(_person);
          return NULL;
        }

        status = xmlTextReaderNodeType(reader) == XML_READER_TYPE_ELEMENT
          && strcmp("event", xmlTextReaderConstLocalName(reader)) == 0
          && strcmp("http://person.com", xmlTextReaderConstNamespaceUri(reader)) == 0;
      }
    }
  }

  return _person;
}

static int *methodThatReturnsIntPointer() {
  int *someint = malloc(sizeof(int));
  *someint = 90;
  return someint;
}

int main() {
  struct person person;
  struct person *readperson;
  char id[11] = "1234567890";
  char event1description[6] = "birth";
  char event1place[16] = "event one place";
  char event1date[15] = "event one date";
  char event2description[6] = "death";
  char event2place[16] = "event two place";
  char event2date[15] = "event two date";
  struct event events[2];
  time_t *now_t = malloc(sizeof(time_t));
  struct tm othertime;
  struct tm *now;
  int success = blobbyblobby;
  char * examplexml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<p:person w:id=\"1234567890\" w:gender=\"FEMALE\" w:cool=\"true\" xmlns:w=\"http://whereverelse.com\" xmlns:p=\"http://person.com\">\n <p:event>\n  <p:description>birth</p:description>\n  <p:place>event one city &amp; state</p:place>\n  <p:date>event one date</p:date>\n </p:event>\n <p:event>\n  <p:description>death</p:description>\n  <p:place>event two place</p:place>\n  <p:date>event two date</p:date>\n </p:event>\n</p:person>";
  xmlBufferPtr buf;
  xmlTextReaderPtr reader;
  xmlTextWriterPtr writer;
  struct xmlBasicNode *personNode;
  struct event someevent, otherevent, *eventptr;
  int someint;
  char *dummystring;
  int *bunchofints;

  person.id = id;
  person.gender = f;
  person.cool = 1;
  person.events = events;
  events[0].description = event1description;
  events[0].place = event1place;
  events[0].date = event1date;
  events[1].description = event2description;
  events[1].place = event2place;
  events[1].date = event2date;
  person._sizeof_events = 2;

  personout(&person);
  personxmlout(&person);

  printf("\n");
  printf("\n");
  reader = xmlReaderForMemory(examplexml, strlen(examplexml), NULL, NULL, 0);
  if (reader == NULL) {
    printf("BAD READER!");
    return 1;
  }
  success = xmlTextReaderRead(reader);
  if (success != 1) {
    printf("BAD READ!");
  }
  readperson = xmlTextReaderReadPerson(reader);
  personout(readperson);
  personxmlout(readperson);

  printf("\n");
  printf("\n");
  examplexml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<p:person w:id=\"1234567890\" w:gender=\"FEMALE\" w:cool=\"true\" xmlns:w=\"http://whereverelse.com\" xmlns:p=\"http://person.com\"/>";
  reader = xmlReaderForMemory(examplexml, strlen(examplexml), NULL, NULL, 0);
  if (reader == NULL) {
    printf("BAD READER!");
    return 1;
  }
  success = xmlTextReaderRead(reader);
  if (success != 1) {
    printf("BAD READ!");
  }
  readperson = xmlTextReaderReadPerson(reader);
  personout(readperson);
  personxmlout(readperson);

  printf("\n");
  printf("\n");
  examplexml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<p:person w:id=\"1234567890\" w:gender=\"FEMALE\" w:cool=\"true\" xmlns:w=\"http://whereverelse.com\" xmlns:p=\"http://person.com\">\n <p:event>\n  <p:description></p:description>\n  <p:place/>\n  <p:date>event one date</p:date>\n </p:event>\n <p:event>\n  <p:description>death</p:description>\n  <p:place>event two place</p:place>\n  </p:event>\n</p:person>";
  reader = xmlReaderForMemory(examplexml, strlen(examplexml), NULL, NULL, 0);
  if (reader == NULL) {
    printf("BAD READER!");
    return 1;
  }
  success = xmlTextReaderRead(reader);
  if (success != 1) {
    printf("BAD READ!");
  }
  readperson = xmlTextReaderReadPerson(reader);
  personout(readperson);
  personxmlout(readperson);

  printf("\n");
  printf("\n");
  examplexml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<person></person>";
  reader = xmlReaderForMemory(examplexml, strlen(examplexml), NULL, NULL, 0);
  if (reader == NULL) {
    printf("BAD READER!");
    return 1;
  }
  success = xmlTextReaderRead(reader);
  if (success != 1) {
    printf("BAD READ!");
  }
  xmlTextReaderAdvanceToNextStartOrEndElement(reader);
  printf("namspace uri: [%i]", (xmlTextReaderConstNamespaceUri(reader) == NULL));
//  readperson = xmlTextReaderReadPerson(reader);
//  personout(readperson);
//  personxmlout(readperson);

  eventptr = malloc(sizeof(struct event));

  someevent.description = "hello, there!";
  someevent.place = "hello, place!";
  someevent.date = "hello, date!";

  otherevent = someevent;
  *eventptr = someevent;

  someevent.description = "changed description";
  eventptr->description = "pointer source";
  printf("%s = %s = %s?\n", otherevent.description, someevent.description, eventptr->description);
  otherevent = *eventptr;
  printf("%s = %s = %s?\n", otherevent.description, someevent.description, eventptr->description);

  someint = *methodThatReturnsIntPointer();
  printf("some int: %i\n\n\n", someint);

  dummystring = "123456789\01234567890";
  dummystring = _encode_base64(dummystring, 20);
  printf("encoded: %s\n", dummystring);
  dummystring = _decode_base64(dummystring, &someint);
  printf("decoded: %s (len: %i)\n\n\n", dummystring, someint);

  examplexml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<p:person w:id=\"1234567890\" w:gender=\"FEMALE\" w:cool=\"true\" xmlns:w=\"http://whereverelse.com\" xmlns:p=\"http://person.com\">\n <p:event>\n  <p:description>birth</p:description>\n  <p:place>event one city &amp; state</p:place>\n  <p:date>event one date</p:date>\n </p:event>\n <p:event>\n  <p:description>death</p:description>\n  <p:place>event two place</p:place>\n  <p:date>event two date</p:date>\n </p:event>\n</p:person>";
  reader = xmlReaderForMemory(examplexml, strlen(examplexml), NULL, NULL, 0);
  success = xmlTextReaderRead(reader);
  if (success != 1) {
    printf("BAD READ!");
  }
  printf("reading any type...\n");
  personNode = xmlTextReaderReadXsAnyTypeType(reader);
  printf("personnode: %p\n", personNode);
  buf = xmlBufferCreate();
  writer = xmlNewTextWriterMemory(buf, 0);
  xmlTextWriterSetIndent(writer, 2);
  xmlTextWriterStartDocument(writer, NULL, "utf-8", NULL);
  success = xmlTextWriterWriteXsAnyTypeType(writer, personNode);
  xmlTextWriterEndDocument(writer);
  printf("STATUS: %i\n%s", success, buf->content);
  freeXsAnyTypeType(personNode);
  free(personNode);

  bunchofints = calloc(10, sizeof(int));
  bunchofints[1] = 1;
  bunchofints[2] = 2;
  free(bunchofints);

  printf("\n");
  printf("\n");
  printf("DATE EXPERIMENTS:\n");
  time(now_t);
  now = localtime(now_t);
  printf("Now: %04i-%02i-%02iT%02i:%02i:%02i%+02i:%02i\n", now->tm_year + 1900, now->tm_mon + 1, now->tm_mday, now->tm_hour, now->tm_min, now->tm_sec, (int) now->tm_gmtoff / 3600, (int) (now->tm_gmtoff / 60) % 60);
  success = readDateTime("2007-01-01T12:12:12", &othertime);
  printf("parsed 2007-01-01T12:12:12 to %04i-%02i-%02iT%02i:%02i:%02i%+02i:%02i\n", othertime.tm_year + 1900, othertime.tm_mon + 1, othertime.tm_mday, othertime.tm_hour, othertime.tm_min, othertime.tm_sec, (int) othertime.tm_gmtoff / 3600, (int) (othertime.tm_gmtoff / 60) % 60);
  success = readDateTime("2007-01-01", &othertime);
  printf("parsed 2007-01-01 to %04i-%02i-%02iT%02i:%02i:%02i%+02i:%02i\n", othertime.tm_year + 1900, othertime.tm_mon + 1, othertime.tm_mday, othertime.tm_hour, othertime.tm_min, othertime.tm_sec, (int) othertime.tm_gmtoff / 3600, (int) (othertime.tm_gmtoff / 60) % 60);
  success = readDateTime("12:20:30", &othertime);
  printf("parsed 12:20:30 to %04i-%02i-%02iT%02i:%02i:%02i%+02i:%02i\n", othertime.tm_year + 1900, othertime.tm_mon + 1, othertime.tm_mday, othertime.tm_hour, othertime.tm_min, othertime.tm_sec, (int) othertime.tm_gmtoff / 3600, (int) (othertime.tm_gmtoff / 60) % 60);
  success = readDateTime("2009-08-04T10:11:38.428-06:00", &othertime);
  printf("parsed 2009-08-04T10:11:38.428-06:00 to %04i-%02i-%02iT%02i:%02i:%02i.000%+03i:%02i\n", othertime.tm_year + 1900, othertime.tm_mon + 1, othertime.tm_mday, othertime.tm_hour, othertime.tm_min, othertime.tm_sec, (int) (othertime.tm_gmtoff / 3600), (int) ((othertime.tm_gmtoff / 60) % 60));
};

void testFunc() {
  void * something;
  int value;
  value = *((int*)something);
}