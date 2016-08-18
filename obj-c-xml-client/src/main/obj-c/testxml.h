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
#define NO_GNUSTEP

#import <libxml/xmlwriter.h>
#import <libxml/xmlreader.h>
#import <Foundation/Foundation.h>

#ifndef ENUNCIATE_OBJC_TYPES
#define ENUNCIATE_OBJC_TYPES

/**
 * Protocol defining an Enunciate XML I/O methods.
 */
@protocol EnunciateXML

/**
 * Read an instance from XML.
 *
 * @param xml The XML to read.
 */
+ (id<EnunciateXML>) readFromXML: (NSData *) xml;

/**
 * Write this instance as XML.
 *
 * @return The XML.
 */
- (NSData *) writeToXML;

@end /*protocol EnunciateXML*/


/**
 * A basic XML node. Can be an element or an attribute. Used
 * instead of NSXMLElement because it's not supported on all
 * platforms yet.
 */
@interface JAXBBasicXMLNode : NSObject
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