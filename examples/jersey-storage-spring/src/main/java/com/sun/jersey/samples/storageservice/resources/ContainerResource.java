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

package com.sun.jersey.samples.storageservice.resources;

import com.sun.jersey.api.NotFoundException;
import com.sun.jersey.samples.storageservice.Container;
import com.sun.jersey.samples.storageservice.Item;
import com.sun.jersey.samples.storageservice.MemoryStore;
import java.net.URI;
import java.util.Iterator;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
@Produces("application/xml")
public class ContainerResource {
    @Context UriInfo uriInfo;
    @Context Request request;
    String container;
    
    ContainerResource(UriInfo uriInfo, Request request, String container) {
        this.uriInfo = uriInfo;
        this.request = request;
        this.container = container;
    }
    
    @GET
    public Container getContainer(@QueryParam("search") String search) {
        System.out.println("GET CONTAINER " + container + ", search = " + search);

        Container c = MemoryStore.MS.getContainer(container);
        if (c == null)
            throw new NotFoundException("Container not found");
        
        
        if (search != null) {
            c = c.clone();
            Iterator<Item> i = c.getItem().iterator();
            byte[] searchBytes = search.getBytes();
            while (i.hasNext()) {
                if (!match(searchBytes, container, i.next().getName()))
                    i.remove();
            }
        }
        
        return c;
    }    

    @PUT
    public Response putContainer() {
        System.out.println("PUT CONTAINER " + container);
        
        URI uri =  uriInfo.getAbsolutePath();
        Container c = new Container(container, uri.toString());
        
        Response r;
        if (!MemoryStore.MS.hasContainer(c)) {
            r = Response.created(uri).build();
        } else {
            r = Response.noContent().build();
        }
        
        MemoryStore.MS.createContainer(c);
        return r;
    }
    
    @DELETE
    public void deleteContainer() {
        System.out.println("DELETE CONTAINER " + container);
        
        Container c = MemoryStore.MS.deleteContainer(container);
        if (c == null)
            throw new NotFoundException("Container not found");
    } 
    
    
    @Path(value="{item}")
    public ItemResource getItemResource(@PathParam("item") String item) {
        return new ItemResource(uriInfo, request, container, item);
    }
    
    private boolean match(byte[] search, String container, String item) {
        byte[] b = MemoryStore.MS.getItemData(container, item);
        
        OUTER: for (int i = 0; i < b.length - search.length; i++) {
            int j = 0;
            for (; j < search.length; j++) {
                if (b[i + j] != search[j])
                    continue OUTER;
            }
            
            return true;
        }
        
        return false;
    }
}
