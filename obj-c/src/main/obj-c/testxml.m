#define NO_GNUSTEP
#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <string.h>
#include <libxml/xmlwriter.h>
#include <libxml/xmlreader.h>
#include <libxml/tree.h>
#include <Foundation/Foundation.h>

#ifndef ENUNCIATE_OBJC_UTILITIES
#define ENUNCIATE_OBJC_UTILITIES

/**
 * A basic xml node, used when (de)serializing unknown or "any" xml type.
 * We can't use the libxml xmlNodePtr because we can't reliably "free" it.
 */
struct xmlBasicNode {
  /**
   * The (local) name of the node.
   */
  xmlChar *name;

  /**
   * The namespace of the node.
   */
  xmlChar *ns;

  /**
   * The namespace prefix of the node.
   */
  xmlChar *prefix;

  /**
   * The (text) value of the node.
   */
  xmlChar *value;

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

static int xmlTextReaderSkipElement(xmlTextReaderPtr reader) {
  int status = xmlTextReaderNext(reader);
  while (status && xmlTextReaderNodeType(reader) != XML_READER_TYPE_ELEMENT && xmlTextReaderNodeType(reader) != XML_READER_TYPE_END_ELEMENT) {
    status = xmlTextReaderRead(reader);
  }
  return status;
}

static xmlChar *xmlTextReaderReadEntireNodeValue(xmlTextReaderPtr reader) {
  xmlChar *buffer = calloc(1, sizeof(xmlChar));
  const xmlChar *snippet;
  int status;
  if (xmlTextReaderNodeType(reader) == XML_READER_TYPE_ATTRIBUTE) {
    return xmlTextReaderValue(reader);
  }
  else if (xmlTextReaderIsEmptyElement(reader) == 0) {
    status = xmlTextReaderRead(reader);
    while (status && (xmlTextReaderNodeType(reader) == XML_READER_TYPE_TEXT || xmlTextReaderNodeType(reader) == XML_READER_TYPE_CDATA || xmlTextReaderNodeType(reader) == XML_READER_TYPE_ENTITY_REFERENCE)) {
      snippet = xmlTextReaderConstValue(reader);
      buffer = realloc(buffer, (xmlStrlen(buffer) + xmlStrlen(snippet) + 1) * sizeof(xmlChar));
      xmlStrcat(buffer, snippet);
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
xmlChar *_encode_base64(unsigned char *instream, int insize) {
  unsigned char in[3];
  unsigned char out[4];
  xmlChar *encoded;
  int i, in_index = 0, out_index = 0, blocklen;

  if (insize == 0) {
    return BAD_CAST "\0";
  }

  encoded = calloc(((insize / 3) * 4) + 10, sizeof(xmlChar));
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
unsigned char *_decode_base64( const xmlChar *invalue, int *outsize ) {
  xmlChar in[4];
  unsigned char out[3], v;
  int i, in_index = 0, out_index = 0, blocklen;
  unsigned char *outstream;

  if (invalue == NULL) {
    return NULL;
  }

  outstream = calloc(((xmlStrlen(invalue) / 4) * 3) + 1, sizeof(unsigned char));
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

#endif /* ENUNCIATE_OBJC_UTILITIES */

#ifndef BASIC_XML_OBJC_FUNCTIONS_XS
#define BASIC_XML_OBJC_FUNCTIONS_XS

/*******************xs:boolean************************************/

/**
 * Read a boolean value from the reader.
 *
 * @param reader The reader (pointing at a node with a value).
 * @return pointer to 1 if "true" was read. pointer to 0 otherwise.
 */
static int *xmlTextReaderReadXsBooleanType(xmlTextReaderPtr reader) {
  xmlChar *nodeValue = xmlTextReaderReadEntireNodeValue(reader);
  int *value = malloc(sizeof(int));
  *value = (xmlStrcmp(BAD_CAST "true", nodeValue) == 0) ? 1 : 0;
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
    return xmlTextWriterWriteString(writer, BAD_CAST "false");
  }
  else {
    return xmlTextWriterWriteString(writer, BAD_CAST "true");
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
  xmlChar *nodeValue = xmlTextReaderReadEntireNodeValue(reader);
  unsigned char *value = malloc(sizeof(unsigned char));
  *value = (unsigned char) atoi((char *) nodeValue);
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
  xmlChar *nodeValue = xmlTextReaderReadEntireNodeValue(reader);
  double *value = malloc(sizeof(double));
  *value = atof((char *) nodeValue);
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
  xmlChar *nodeValue = xmlTextReaderReadEntireNodeValue(reader);
  float *value = malloc(sizeof(float));
  *value = atof((char *)nodeValue);
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
  xmlChar *nodeValue = xmlTextReaderReadEntireNodeValue(reader);
  int *value = malloc(sizeof(int));
  *value = atoi((char *)nodeValue);
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
  xmlChar *nodeValue = xmlTextReaderReadEntireNodeValue(reader);
  long *value = malloc(sizeof(long));
  *value = atol((char *)nodeValue);
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
  xmlChar *nodeValue = xmlTextReaderReadEntireNodeValue(reader);
  short *value = malloc(sizeof(short));
  *value = atoi((char *)nodeValue);
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
static xmlChar *xmlTextReaderReadXsStringType(xmlTextReaderPtr reader) {
  return xmlTextReaderReadEntireNodeValue(reader);
}

/**
 * Write a string value to the writer.
 *
 * @param writer The writer.
 * @param value The value to be written.
 * @return the bytes written (may be 0 because of buffering) or -1 in case of error.
 */
static int xmlTextWriterWriteXsStringType(xmlTextWriterPtr writer, xmlChar *value) {
  return xmlTextWriterWriteString(writer, value);
}

/**
 * Frees a string type from memory.
 *
 * @param value The value to free.
 */
static void freeXsStringType(xmlChar *value) {
  //no-op
}

/*******************xs:ID************************************/

/**
 * Read a ID value from the reader.
 *
 * @param reader The reader (pointing at a node with a value).
 * @return pointer to the ID.
 */
static xmlChar *xmlTextReaderReadXsIDType(xmlTextReaderPtr reader) {
  return xmlTextReaderReadXsStringType(reader);
}

/**
 * Write a ID value to the writer.
 *
 * @param writer The writer.
 * @param value The value to be written.
 * @return the bytes written (may be 0 because of buffering) or -1 in case of error.
 */
static int xmlTextWriterWriteXsIDType(xmlTextWriterPtr writer, xmlChar *value) {
  return xmlTextWriterWriteString(writer, value);
}

/**
 * Frees a ID type from memory.
 *
 * @param value The value to free.
 */
static void freeXsIDType(xmlChar *value) {
  freeXsStringType(value);
}

/*******************xs:IDREF************************************/

/**
 * Read a IDREF value from the reader.
 *
 * @param reader The reader (pointing at a node with a value).
 * @return pointer to the IDREF.
 */
static xmlChar *xmlTextReaderReadXsIDREFType(xmlTextReaderPtr reader) {
  return xmlTextReaderReadXsStringType(reader);
}

/**
 * Write a IDREF value to the writer.
 *
 * @param writer The writer.
 * @param value The value to be written.
 * @return the bytes written (may be 0 because of buffering) or -1 in case of error.
 */
static int xmlTextWriterWriteXsIDREFType(xmlTextWriterPtr writer, xmlChar *value) {
  return xmlTextWriterWriteString(writer, value);
}

/**
 * Frees a IDREF type from memory.
 *
 * @param value The value to free.
 */
static void freeXsIDREFType(xmlChar *value) {
  freeXsStringType(value);
}

/*******************xs:integer************************************/

/**
 * Read a (big) integer value from the reader.
 *
 * @param reader The reader (pointing at a node with a value).
 * @return pointer to the integer.
 */
static xmlChar *xmlTextReaderReadXsIntegerType(xmlTextReaderPtr reader) {
  return xmlTextReaderReadXsStringType(reader);
}

/**
 * Write a integer value to the writer.
 *
 * @param writer The writer.
 * @param value The value to be written.
 * @return the bytes written (may be 0 because of buffering) or -1 in case of error.
 */
static int xmlTextWriterWriteXsIntegerType(xmlTextWriterPtr writer, xmlChar *value) {
  return xmlTextWriterWriteString(writer, value);
}

/**
 * Frees a integer type from memory.
 *
 * @param value The value to free.
 */
static void freeXsIntegerType(xmlChar *value) {
  freeXsStringType(value);
}

/*******************xs:decimal************************************/

/**
 * Read a (big) decimal value from the reader.
 *
 * @param reader The reader (pointing at a node with a value).
 * @return pointer to the decimal.
 */
static xmlChar *xmlTextReaderReadXsDecimalType(xmlTextReaderPtr reader) {
  return xmlTextReaderReadXsStringType(reader);
}

/**
 * Write a decimal value to the writer.
 *
 * @param writer The writer.
 * @param value The value to be written.
 * @return the bytes written (may be 0 because of buffering) or -1 in case of error.
 */
static int xmlTextWriterWriteXsDecimalType(xmlTextWriterPtr writer, xmlChar *value) {
  return xmlTextWriterWriteString(writer, value);
}

/**
 * Frees a decimal type from memory.
 *
 * @param value The value to free.
 */
static void freeXsDecimalType(xmlChar *value) {
  freeXsStringType(value);
}

/*******************xs:duration************************************/

/**
 * Read a duration value from the reader.
 *
 * @param reader The reader (pointing at a node with a value).
 * @return pointer to the duration.
 */
static xmlChar *xmlTextReaderReadXsDurationType(xmlTextReaderPtr reader) {
  return xmlTextReaderReadXsStringType(reader);
}

/**
 * Write a duration value to the writer.
 *
 * @param writer The writer.
 * @param value The value to be written.
 * @return the bytes written (may be 0 because of buffering) or -1 in case of error.
 */
static int xmlTextWriterWriteXsDurationType(xmlTextWriterPtr writer, xmlChar *value) {
  return xmlTextWriterWriteString(writer, value);
}

/**
 * Frees a duration type from memory.
 *
 * @param value The value to free.
 */
static void freeXsDurationType(xmlChar *value) {
  freeXsStringType(value);
}

/*******************xs:QName************************************/

/**
 * Read a QName value from the reader.
 *
 * @param reader The reader (pointing at a node with a value).
 * @return pointer to the QName.
 */
static xmlChar *xmlTextReaderReadXsQNameType(xmlTextReaderPtr reader) {
  return xmlTextReaderReadXsStringType(reader);
}

/**
 * Write a QName value to the writer.
 *
 * @param writer The writer.
 * @param value The value to be written.
 * @return the bytes written (may be 0 because of buffering) or -1 in case of error.
 */
static int xmlTextWriterWriteXsQNameType(xmlTextWriterPtr writer, xmlChar *value) {
  return xmlTextWriterWriteString(writer, value);
}

/**
 * Frees a QName type from memory.
 *
 * @param value The value to free.
 */
static void freeXsQNameType(xmlChar *value) {
  freeXsStringType(value);
}

/*******************xs:anyURI************************************/

/**
 * Read a anyURI value from the reader.
 *
 * @param reader The reader (pointing at a node with a value).
 * @return pointer to the anyURI.
 */
static xmlChar *xmlTextReaderReadXsAnyURIType(xmlTextReaderPtr reader) {
  return xmlTextReaderReadXsStringType(reader);
}

/**
 * Write a anyURI value to the writer.
 *
 * @param writer The writer.
 * @param value The value to be written.
 * @return the bytes written (may be 0 because of buffering) or -1 in case of error.
 */
static int xmlTextWriterWriteXsAnyURIType(xmlTextWriterPtr writer, xmlChar *value) {
  return xmlTextWriterWriteString(writer, value);
}

/**
 * Frees a anyURI type from memory.
 *
 * @param value The value to free.
 */
static void freeXsAnyURIType(xmlChar *value) {
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
  xmlChar *timevalue = xmlTextReaderReadEntireNodeValue(reader);
  int success = 0, index = 0, token_index = 0, len = xmlStrlen(timevalue), offset_hours = 0, offset_min = 0;
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
  xmlChar *timevalue = xmlTextReaderReadEntireNodeValue(reader);
  int success = 0, index = 0, token_index = 0, len = xmlStrlen(timevalue), offset_hours = 0, offset_min = 0;
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
  xmlChar *timevalue = xmlTextReaderReadEntireNodeValue(reader);
  int success = 0, index = 0, token_index = 0, len = xmlStrlen(timevalue), offset_hours = 0, offset_min = 0;
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
  const xmlChar *text;

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
          text = xmlTextReaderConstValue(reader);
          node->value = xmlStrncat(node->value, text, xmlStrlen(text));
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

/**
 * Read a anyType element value from the reader.
 *
 * @param reader The reader (pointing at a node with a value).
 * @return pointer to the anyType., or NULL if error.
 */
static struct xmlBasicNode *xmlTextReaderReadXsAnyTypeElement(xmlTextReaderPtr reader) {
  return xmlTextReaderReadXsAnyTypeType(reader);
}

/**
 * Write a anyType element value to the writer.
 *
 * @param writer The writer.
 * @param node The node to be written.
 * @return the bytes written (may be 0 because of buffering) or -1 in case of error.
 */
static int xmlTextWriterWriteXsAnyTypeElement(xmlTextWriterPtr writer, struct xmlBasicNode *node) {
  return xmlTextWriterWriteXsAnyTypeType(writer, node);
}

/**
 * Write a anyType element value to the writer.
 *
 * @param writer The writer.
 * @param node The node to be written.
 * @return the bytes written (may be 0 because of buffering) or -1 in case of error.
 */
static int xmlTextWriterWriteXsAnyTypeElementNS(xmlTextWriterPtr writer, struct xmlBasicNode *node, int writeNamespaces) {
  return xmlTextWriterWriteXsAnyTypeType(writer, node);
}

/**
 * Free a anyType element value.
 *
 * @param node The node.
 */
static void freeXsAnyTypeElement(struct xmlBasicNode *node) {
  freeXsAnyTypeType(node);
}

/*******************xs:anySimpleType************************************/

/**
 * Frees a anyType type from memory.
 *
 * @param node The node to free.
 */
static void freeXsAnySimpleTypeType(struct xmlBasicNode *node) {
  freeXsAnyTypeType(node);
}

/**
 * Read a anyType value from the reader.
 *
 * @param reader The reader (pointing at a node with a value).
 * @return pointer to the anyType., or NULL if error.
 */
static struct xmlBasicNode *xmlTextReaderReadXsAnySimpleTypeType(xmlTextReaderPtr reader) {
  struct xmlBasicNode *node = calloc(1, sizeof(struct xmlBasicNode));

  node->name = xmlTextReaderLocalName(reader);
  node->ns = xmlTextReaderNamespaceUri(reader);
  node->prefix = xmlTextReaderPrefix(reader);
  node->value = xmlTextReaderReadEntireNodeValue(reader);

  return node;
}

/**
 * Write a anyType value to the writer.
 *
 * @param writer The writer.
 * @param node The node to be written.
 * @return the bytes written (may be 0 because of buffering) or -1 in case of error.
 */
static int xmlTextWriterWriteXsAnySimpleTypeType(xmlTextWriterPtr writer, struct xmlBasicNode *node) {
  if (node->value != NULL) {
    return xmlTextWriterWriteXsStringType(writer, node->value);
  }

  return 0;
}

#endif /* BASIC_XML_OBJC_FUNCTIONS_XS */

















enum gender {
  not_set,
  m,
  f,
  blobbyblobby
};

int genderxmlout(xmlTextWriterPtr writer, enum gender g) {
  switch (g) {
    case m:
      return xmlTextWriterWriteString(writer, BAD_CAST "MALE");
    case f:
      return xmlTextWriterWriteString(writer, BAD_CAST "FEMALE");
    case blobbyblobby:
    case not_set:
      return -1;
  }

  return -1;
}

enum gender xmlTextReaderReadGender(const xmlChar *enumValue) {
  if (enumValue != NULL) {
    if (xmlStrcmp(enumValue, BAD_CAST "MALE") == 0) {
      return m;
    }
    if (xmlStrcmp(enumValue, BAD_CAST "FEMALE") == 0) {
      return f;
    }
  }
  return not_set;
}

@protocol JAXBType
  + (id<JAXBType>) readXMLType: (xmlTextReaderPtr) reader;
  - (void) writeXMLType: (xmlTextWriterPtr) writer;
@end

@protocol JAXBElement
@end

@interface NSString (JAXBType) <JAXBType>
@end

@implementation NSString (JAXBType)

+ (id<JAXBType>) readXMLType: (xmlTextReaderPtr) reader
{
  return [NSString stringWithUTF8String: (const char *) xmlTextReaderReadXsStringType(reader)];
}

- (void) writeXMLType: (xmlTextWriterPtr) writer
{
  xmlTextWriterWriteXsStringType(writer, BAD_CAST [self UTF8String]);
}

@end

@interface Person : NSObject <JAXBType>
{
  @private
    NSString *_id;
    enum gender _gender;
    BOOL _cool;
    NSArray *_events;
}

- (NSString*) id;
- (void) setId: (NSString*) newId;
- (enum gender) gender;
- (void) setGender: (enum gender) newGender;
- (BOOL) cool;
- (void) setCool: (BOOL) newCool;
- (NSArray*) events;
- (void) setEvents: (NSArray*) newEvents;
@end

@interface Event : NSObject <JAXBType>
{
  @private
    NSString *_description;
    NSString *_place;
    NSString *_date;
}

- (NSString*) description;
- (void) setDescription: (NSString*) newDescription;
- (NSString*) place;
- (void) setPlace: (NSString*) newPlace;
- (NSString*) date;
- (void) setDate: (NSString*) newDate;

@end

@implementation Person

- (NSString*) id
{
  return _id;
}

- (void) setId: (NSString*) newId
{
  _id = newId;
}

- (enum gender) gender
{
  return _gender;
}

- (void) setGender: (enum gender) newGender
{
  _gender = newGender;
}

- (BOOL) cool
{
  return _cool;
}

- (void) setCool: (BOOL) newCool
{
  _cool = newCool;
}

- (NSArray*) events
{
  return _events;
}

- (void) setEvents: (NSArray*) newEvents
{
  _events = newEvents;
}

+ (id<JAXBType>) readXMLType: (xmlTextReaderPtr) reader
{
  Person *_person = [Person new];
  id __child;
  NSMutableArray *__children;
  int status;

  if (xmlTextReaderHasAttributes(reader)) {
    while (xmlTextReaderMoveToNextAttribute(reader)) {
      if ((xmlStrcmp(BAD_CAST "gender", xmlTextReaderConstLocalName(reader)) == 0) && (xmlStrcmp(BAD_CAST "http://whereverelse.com", xmlTextReaderConstNamespaceUri(reader)) == 0)) {
        [_person setGender: xmlTextReaderReadGender(xmlTextReaderConstValue(reader))];
        continue;
      }
      if ((xmlStrcmp(BAD_CAST "id", xmlTextReaderConstLocalName(reader)) == 0) && (xmlStrcmp(BAD_CAST "http://whereverelse.com", xmlTextReaderConstNamespaceUri(reader)) == 0)) {
        [_person setId: (NSString*) [NSString readXMLType: reader]];
        continue;
      }
      if ((xmlStrcmp(BAD_CAST "cool", xmlTextReaderConstLocalName(reader)) == 0) && (xmlStrcmp(BAD_CAST "http://whereverelse.com", xmlTextReaderConstNamespaceUri(reader)) == 0)) {
        [_person setCool: (xmlStrcmp(BAD_CAST "true", xmlTextReaderConstValue(reader)) == 0) ? YES : NO];
        continue;
      }
    }

    status = xmlTextReaderMoveToElement(reader);
    if (!status) {
      //panic: unable to return to the element node.
      [_person dealloc];
      [NSException raise: @"XMLReadError"
                   format: @"Error moving to element of type 'person'."];
    }
  }

  if (xmlTextReaderIsEmptyElement(reader) == 0) {
    status = xmlTextReaderAdvanceToNextStartOrEndElement(reader);

    if (!status) {
      //XML read error
      [_person dealloc];
      [NSException raise: @"XMLReadError"
                   format: @"Error reading 'person' type."];
    }
    else if (xmlTextReaderNodeType(reader) == XML_READER_TYPE_ELEMENT
      && xmlStrcmp(BAD_CAST "event", xmlTextReaderConstLocalName(reader)) == 0
      && xmlStrcmp(BAD_CAST "http://person.com", xmlTextReaderConstNamespaceUri(reader)) == 0) {

      __children = [NSMutableArray new];
      while (status) {

        __child = [Event readXMLType: reader];
        [__children addObject: __child];
        status = xmlTextReaderAdvanceToNextStartOrEndElement(reader);
        if (!status) {
          //XML read error
          [_person dealloc];
          [NSException raise: @"XMLReadError"
                       format: @"Error reading 'person' type."];
        }

        status = xmlTextReaderNodeType(reader) == XML_READER_TYPE_ELEMENT
          && xmlStrcmp(BAD_CAST "event", xmlTextReaderConstLocalName(reader)) == 0
          && xmlStrcmp(BAD_CAST "http://person.com", xmlTextReaderConstNamespaceUri(reader)) == 0;
      }
      [_person setEvents: __children];
    }
  }

  return _person;
}

- (void) writeXMLType: (xmlTextWriterPtr) writer
{
  int rc;
  id __item;
  NSEnumerator *__enumerator;

  //write the attributes of start element.
  if (_id != NULL) {
    rc = xmlTextWriterWriteAttributeNS(writer, BAD_CAST "w", BAD_CAST "id", NULL, BAD_CAST [_id UTF8String]);
    if (rc < 0) {
        [NSException raise: @"XMLWriteError"
                     format: @"Error writing attribute 'id' on 'person'."];
    }
  }

  if (_gender != not_set) {
    rc = xmlTextWriterStartAttributeNS(writer, BAD_CAST "w", BAD_CAST "gender", NULL);
    if (rc < 0) {
        [NSException raise: @"XMLWriteError"
                     format: @"Error writing attribute 'gender' on 'person'."];
    }

    rc = genderxmlout(writer, _gender);
    if (rc < 0) {
        [NSException raise: @"XMLWriteError"
                     format: @"Error writing attribute 'gender' on 'person'."];
    }

    rc = xmlTextWriterEndAttribute(writer);
    if (rc < 0) {
        [NSException raise: @"XMLWriteError"
                     format: @"Error writing attribute 'gender' on 'person'."];
        return;
    }
  }

  rc = xmlTextWriterWriteAttributeNS(writer, BAD_CAST "w", BAD_CAST "cool", NULL, _cool ? BAD_CAST "true" : BAD_CAST "false");
  if (rc < 0) {
      [NSException raise: @"XMLWriteError"
                   format: @"Error writing attribute 'cool' on 'person'."];
      return;
  }

  //if we're on the start element, write the xmlns prefixes
  rc = xmlTextWriterWriteAttribute(writer, BAD_CAST "xmlns:w", BAD_CAST "http://whereverelse.com");
  if (rc < 0) {
      [NSException raise: @"XMLWriteError"
                   format: @"Error writing attribute 'xmlns:w' on 'person'."];
      return;
  }

  rc = xmlTextWriterWriteAttribute(writer, BAD_CAST "xmlns:p", BAD_CAST "http://person.com");
  if (rc < 0) {
      [NSException raise: @"XMLWriteError"
                   format: @"Error writing attribute 'xmlns:p' on 'person'."];
      return;
  }

  if (_events != NULL) {
    __enumerator = [_events objectEnumerator];

    while ( (__item = [__enumerator nextObject]) ) {
      rc = xmlTextWriterStartElementNS(writer, BAD_CAST "p", BAD_CAST "event", NULL);
      if (rc < 0) {
        [NSException raise: @"XMLWriteError"
                     format: @"Error writing element 'event' on 'person'."];
          return;
      }

      [__item writeXMLType: writer];

      rc = xmlTextWriterEndElement(writer);
      if (rc < 0) {
        [NSException raise: @"XMLWriteError"
                     format: @"Error writing end element 'event' on 'person'."];
          return;
      }
    }
  }
}

@end

@implementation Event

- (NSString*) description
{
  return _description;
}

- (void) setDescription: (NSString*) newDescription
{
  _description = newDescription;
}

- (NSString*) place
{
  return _place;
}

- (void) setPlace: (NSString*) newPlace
{
  _place = newPlace;
}

- (NSString *) date
{
  return _date;
}

- (void) setDate: (NSString*) newDate
{
  _date = newDate;
}

+ (id<JAXBType>) readXMLType: (xmlTextReaderPtr) reader
{
  Event *_event = [Event new];

  if (xmlTextReaderIsEmptyElement(reader) == 0) {
    int status = xmlTextReaderAdvanceToNextStartOrEndElement(reader);

    if (!status) {
      //XML read error
      [NSException raise: @"XMLReadError"
                   format: @"Error reading event type."];
    }
    else if (xmlTextReaderNodeType(reader) == XML_READER_TYPE_ELEMENT
      && xmlStrcmp(BAD_CAST "description", xmlTextReaderConstLocalName(reader)) == 0
      && xmlStrcmp(BAD_CAST "http://person.com", xmlTextReaderConstNamespaceUri(reader)) == 0) {

      [_event setDescription: (NSString*)[NSString readXMLType: reader]];
      status = xmlTextReaderAdvanceToNextStartOrEndElement(reader);
    }

    if (!status) {
      //XML read error
      [NSException raise: @"XMLReadError"
                   format: @"Error reading event type."];
    }
    else if (xmlTextReaderNodeType(reader) == XML_READER_TYPE_ELEMENT
      && xmlStrcmp(BAD_CAST "place", xmlTextReaderConstLocalName(reader)) == 0
      && xmlStrcmp(BAD_CAST "http://person.com", xmlTextReaderConstNamespaceUri(reader)) == 0) {

      [_event setPlace: (NSString*)[NSString readXMLType: reader]];
      status = xmlTextReaderAdvanceToNextStartOrEndElement(reader);
    }

    if (!status) {
      [NSException raise: @"XMLReadError"
                   format: @"Error reading event type."];
    }
    else if (xmlTextReaderNodeType(reader) == XML_READER_TYPE_ELEMENT
      && xmlStrcmp(BAD_CAST "date", xmlTextReaderConstLocalName(reader)) == 0
      && xmlStrcmp(BAD_CAST "http://person.com", xmlTextReaderConstNamespaceUri(reader)) == 0) {

      [_event setDate: (NSString*)[NSString readXMLType: reader]];
      status = xmlTextReaderAdvanceToNextStartOrEndElement(reader);
    }
  }

  return _event;

}

- (void) writeXMLType: (xmlTextWriterPtr) writer
{
  int rc;

  if ( _description != NULL ) {
    rc = xmlTextWriterWriteElementNS(writer, BAD_CAST "p", BAD_CAST "description", NULL, BAD_CAST [_description UTF8String]);
    if (rc < 0) {
      [NSException raise: @"XMLWriteError"
                   format: @"Error writing element 'description' on 'event'."];
      return;
    }
  }

  if (_place != NULL) {
    rc = xmlTextWriterWriteElementNS(writer, BAD_CAST "p", BAD_CAST "place", NULL, BAD_CAST [_place UTF8String]);
    if (rc < 0) {
      [NSException raise: @"XMLWriteError"
                   format: @"Error writing element 'place' on 'event'."];
      return;
    }
  }

  if (_date != nil) {
    rc = xmlTextWriterWriteElementNS(writer, BAD_CAST "p", BAD_CAST "date", NULL, BAD_CAST [_date UTF8String]);
    if (rc < 0) {
      [NSException raise: @"XMLWriteError"
                   format: @"Error writing element 'date' on 'event'."];
      return;
    }
  }

}
@end


int main() {
  Person *person = [Person new];
  Person *readperson;
  NSString *pid = @"1234567890";
  NSString *event1description = @"birth";
  NSString *event1place = @"event one place";
  NSString *event1date = @"event one date";
  NSString *event2description = @"death";
  NSString *event2place = @"event two place";
  NSString *event2date = @"event two date";
  Event *event1, *event2;
  NSArray *events;
  char * examplexml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<p:person w:id=\"1234567890\" w:gender=\"FEMALE\" w:cool=\"true\" xmlns:w=\"http://whereverelse.com\" xmlns:p=\"http://person.com\">\n <p:event>\n  <p:description>birth</p:description>\n  <p:place>event one city &amp; state</p:place>\n  <p:date>event one date</p:date>\n </p:event>\n <p:event>\n  <p:description>death</p:description>\n  <p:place>event two place</p:place>\n  <p:date>event two date</p:date>\n </p:event>\n</p:person>";
  xmlBufferPtr buf;
  xmlTextReaderPtr reader;
  xmlTextWriterPtr writer;
  int rc;

  [person setId: pid];
  [person setGender: f];
  [person setCool: YES];
  
  event1 = [Event new];
  [event1 setDescription: event1description];
  [event1 setPlace: event1place];
  [event1 setDate: event1date];
  event2 = [Event new];
  [event2 setDescription: event2description];
  [event2 setPlace: event2place];
  [event2 setDate: event2date];

  events = [[NSArray alloc] initWithObjects: event1, event2, nil];
  [person setEvents: events];

  buf = xmlBufferCreate();
  writer = xmlNewTextWriterMemory(buf, 0);
  rc = xmlTextWriterSetIndent(writer, 2);
  rc = xmlTextWriterStartDocument(writer, NULL, "utf-8", NULL);
  rc = xmlTextWriterStartElementNS(writer, BAD_CAST "p", BAD_CAST "person", NULL);
  [person writeXMLType: writer];
  rc = xmlTextWriterEndDocument(writer);
  xmlFreeTextWriter(writer);
  printf("%s", buf->content);
  xmlBufferFree(buf);

  return 0;
};