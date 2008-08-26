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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class MemoryStore implements Store {
    public static final JAXBContext CONTEXT = getContext();
    
    private static JAXBContext getContext() {
        try {
            return JAXBContext.newInstance(Containers.class, Container.class, Item.class);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    public static final MemoryStore MS = new MemoryStore();
    
    private Map<String, Container> containerMap = new HashMap<String, Container>();
    
    private Map<String, Map<String, byte[]>> dataMap = new HashMap<String, Map<String, byte[]>>();
    
    /** Creates a new instance of MemoryStore */
    public MemoryStore() {
    }

    public Containers getContainers() {
        Containers c = new Containers();
        
        List<Container> l = new ArrayList<Container>();
        l.addAll(containerMap.values());
        c.setContainer(l);
        
        return c;
    }

    public Container getContainer(String container) {
        return containerMap.get(container);
    }

    public boolean hasContainer(Container container) {
        return  containerMap.get(container.getName()) != null;
    }
    
    public Container createContainer(Container container) {
        Container c = containerMap.get(container.getName());
        if (c != null) return null;
        
        containerMap.put(container.getName(), container);
        
        dataMap.put(container.getName(), new HashMap<String, byte[]>());
        return c;
    }

    public Container deleteContainer(String container) {
        Container c = containerMap.remove(container);
        if (c == null) return null;
        
        dataMap.remove(container);
        return c;
    }

    public boolean hasItem(String container, String item) {
        Container c = containerMap.get(container);
        if (c == null) return false;
        
        return c.getItem(item) != null;
    }
    
    public Item getItem(String container, String item) {
        Container c = containerMap.get(container);
        if (c == null) return null;
        
        return c.getItem(item);
    }
    
    public byte[] getItemData(String container, String item) {
        Container c = containerMap.get(container);
        if (c == null) return null;
        
        Map<String, byte[]> data = dataMap.get(container);
        return (data != null) ? data.get(item) : null;
    }

    public Item createOrUpdateItem(String container, Item item, byte[] content) {
        Container c = containerMap.get(container);
        if (c == null) return null;
    
        c.putItem(item);
        
        Map<String, byte[]> data = dataMap.get(container);
        data.put(item.getName(), content);
        
        return item;
    }

    public Item deleteItem(String container, String item) {
        Container c = containerMap.get(container);
        if (c == null) return null;
        
        Item i = c.removeItem(item);
        if (i != null) {
            Map<String, byte[]> data = dataMap.get(container);
            data.remove(item);
        }
        return i;
    }
}
