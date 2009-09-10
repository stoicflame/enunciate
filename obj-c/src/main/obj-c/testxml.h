#define NO_GNUSTEP

#include <libxml/xmlwriter.h>
#include <libxml/xmlreader.h>
#include <Foundation/Foundation.h>

#ifndef ENUNCIATE_OBJC_TYPES
#define ENUNCIATE_OBJC_TYPES

/**
 * Protocol defining a JAXB (see https://jaxb.dev.java.net/) type.
 */
@protocol JAXBType

/**
 * Read an XML type from an XML reader.
 *
 * @param reader The reader.
 * @return An instance of the object defining the JAXB type.
 */
+ (id<JAXBType>) readXMLType: (xmlTextReaderPtr) reader;

/**
 * Initialize the object with an XML reader.
 *
 * @param reader The XML reader from which to initialize the values of this type.
 */
- (void) initWithReader: (xmlTextReaderPtr) reader;

/**
 * Write this instance of a JAXB type to a writer.
 *
 * @param writer The writer.
 */
- (void) writeXMLType: (xmlTextWriterPtr) writer;

@end /*protocol JAXBType*/



/**
 * Protocol defining a JAXB (see https://jaxb.dev.java.net/) element.
 */
@protocol JAXBElement

/**
 * Read the XML element from an XML reader. It is assumed
 * that the reader is pointing at the start element (be careful
 * that it's not still pointing to the XML document).
 *
 * @param reader The reader.
 * @return An instance of the object defining the JAXB element.
 */
+ (id<JAXBElement>) readXMLElement: (xmlTextReaderPtr) reader;

/**
 * Write this instance of a JAXB element to a writer.
 *
 * @param writer The writer.
 */
- (void) writeXMLElement: (xmlTextWriterPtr) writer;

/**
 * Write this instance of a JAXB element to a writer.
 *
 * @param writer The writer.
 * @param writeNs Whether to write the namespaces for this element to the xml writer.
 */
- (void) writeXMLElement: (xmlTextWriterPtr) writer writeNamespaces: (BOOL) writeNs;

@end /*protocol JAXBElement*/



/**
 * A basic XML node. Can be an element or an attribute. Used
 * instead of NSXMLElement because it's not supported on all
 * platforms yet.
 */
@interface JAXBBasicXMLNode : NSObject <JAXBType, JAXBElement>
{
  @private
    NSString *_name;
    NSString *_ns;
    NSString *_prefix;
    NSString *_value;
    NSArray  *_childElements;
    NSArray  *_attributes;
}

/**
 * Accessor for the (local) name of the XML node.
 *
 * @return The (local) name of the XML node.
 */
- (NSString *) name;

/**
 * Accessor for the (local) name of the XML node.
 *
 * @param newName The (local) name of the XML node.
 */
- (void) setName: (NSString *) newName;

/**
 * Accessor for the namespace of the XML node.
 *
 * @return The namespace of the XML node.
 */
- (NSString *) ns;

/**
 * Accessor for the namespace of the XML node.
 *
 * @param newNs The namespace of the XML node.
 */
- (void) setNs: (NSString *) newNs;

/**
 * Accessor for the namespace prefix of the XML node.
 *
 * @return The namespace prefix of the XML node.
 */
- (NSString *) prefix;

/**
 * Accessor for the namespace prefix of the XML node.
 *
 * @param newPrefix The namespace prefix of the XML node.
 */
- (void) setPrefix: (NSString *) newPrefix;

/**
 * Accessor for the value of the XML node.
 *
 * @return The value of the XML node.
 */
- (NSString *) value;

/**
 * Accessor for the value of the XML node.
 *
 * @param newValue The value of the XML node.
 */
- (void) setValue: (NSString *) newValue;

/**
 * Accessor for the child elements of the XML node.
 *
 * @return The child elements of the XML node.
 */
- (NSArray *) childElements;

/**
 * Accessor for the child elements of the XML node.
 *
 * @param newValue The child elements of the XML node.
 */
- (void) setChildElements: (NSArray *) newChildElements;

/**
 * Accessor for the attributes of the XML node.
 *
 * @return The attributes of the XML node.
 */
- (NSArray *) attributes;

/**
 * Accessor for the attributes of the XML node.
 *
 * @param newAttributes The attributes of the XML node.
 */
- (void) setAttributes: (NSArray *) newAttributes;
@end /*interface JAXBBasicXMLNode*/

#endif /* ENUNCIATE_OBJC_TYPES */