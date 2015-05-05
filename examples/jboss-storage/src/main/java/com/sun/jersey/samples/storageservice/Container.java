/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://jersey.dev.java.net/CDDL+GPL.html
 * or jersey/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at jersey/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.jersey.samples.storageservice;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Container {
    private String name;
    
    private String uri;
    
    private List<Item> item; 
    
    public Container() {
    }
    
    public Container(String name, String uri) {
        setName(name);
        setUri(uri);
        setItem(new ArrayList<Item>());
    }
    
    public Container(String name, String uri, List<Item> items) {
        setName(name);
        setUri(uri);
        setItem(items);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
    
    public List<Item> getItem() {
        return item;
    }

    
    public void setItem(List<Item> items) {
        this.item = items;
    }
    
    public Item getItem(String name) {
        for (Item i : item)
            if (i.getName().equals(name))
                return i;
    
        return null;
    }
    
    public void putItem(Item item) {
        ListIterator<Item> i = getItem().listIterator();
        while (i.hasNext())
            if (i.next().getName().equals(item.getName())) {
                i.set(item);
                return;
            }
        
        getItem().add(item);
    }
    
    public Item removeItem(String name) {
        ListIterator<Item> i = getItem().listIterator();
        while (i.hasNext()) {
            Item item = i.next();
            if (item.getName().equals(name)) {
                i.remove();
                return item;
            }
        }
        
        return null;
    }

    public Container clone() {
        Container that = new Container(this.name, this.uri);
        that.setItem(new ArrayList<Item>(this.item));
        
        return that;
    }
}