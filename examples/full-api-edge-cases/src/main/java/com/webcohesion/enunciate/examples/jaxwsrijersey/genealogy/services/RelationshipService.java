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
package com.webcohesion.enunciate.examples.jaxwsrijersey.genealogy.services;

import com.webcohesion.enunciate.examples.jaxwsrijersey.genealogy.data.Relationship;

import jakarta.jws.WebService;
import java.util.List;
import java.util.ArrayList;

/**
 * Test of a basic class-based service with no interface.
 *
 * @author Ryan Heaton
 */
@WebService
public class RelationshipService {

  public List<Relationship> getRelationships(String personId) throws RelationshipException {
    if ("throw".equals(personId)) {
      throw new RelationshipException("hi");
    }

//    todo: uncomment when CXF handles this
//    if ("outthrow".equals(personId)) {
//      throw new OutsideException("outside message");
//    }
    
    ArrayList<Relationship> list = new ArrayList<Relationship>();
    for (int i = 0; i < 3; i++) {
      Relationship relationship = new Relationship();
      relationship.setId(String.valueOf(i));
      list.add(relationship);
    }
    return list;
  }

  public void touch() {
    
  }
}
