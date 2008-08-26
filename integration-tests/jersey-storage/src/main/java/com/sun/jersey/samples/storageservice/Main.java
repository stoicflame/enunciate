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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class Main {
    
    public static void main(String[] args) throws Exception {
        if (args.length == 2) {
            // <"GET" | "DELETE" | "PUT"> <URI>
            String uri = args[1];
            if (args[0].equalsIgnoreCase("GET")) {
                // Collection operations
                // Get all collections
                byte[] content = get(uri);
                System.out.println(new String(content));
            } else if (args[0].equalsIgnoreCase("DELETE")) {
                // Collection operation
                // Delete a collection
                delete(uri);
            } else if (args[0].equalsIgnoreCase("PUT")) {
                // Collection operation
                // Create a collection (if not already created)
                put(uri);
            } else {
                throw new IllegalArgumentException();
            }
        } else if (args.length == 3) {
            // PUT <URI> <mediaType>
            String uri = args[1];
            if (args[0].equalsIgnoreCase("PUT")) {
                // Item operation
                // Create an item, or update if already created
                put(uri, args[2], new BufferedInputStream(System.in));                
            } else if (args[0].equalsIgnoreCase("POST")) {
                post(uri, args[2], new BufferedInputStream(System.in));                
            } else {
                throw new IllegalArgumentException();
            }
        } else if (args.length == 4) {
            // PUT <URI> <mediaType> <file>
            String uri = args[1];
            if (args[0].equalsIgnoreCase("PUT")) {
                // Item operation
                // Create an item, or update if already created
                put(uri, args[2], new BufferedInputStream(new FileInputStream(args[3])));                
            } else if (args[0].equalsIgnoreCase("POST")) {
                post(uri, args[2], new BufferedInputStream(new FileInputStream(args[3])));                
            } else {
                throw new IllegalArgumentException();
            }
        } else {
            throw new IllegalArgumentException();
        }
    }
    
    private void printUsage() {
        
    }
    
    private static byte[] get(String uri) throws IOException {
        URL u = new URL(uri);
        HttpURLConnection uc = (HttpURLConnection)u.openConnection();
        uc.setRequestMethod("GET");
        
        int status = uc.getResponseCode();
        String mediaType = uc.getContentType();
        
        InputStream in = uc.getInputStream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int r;
        while ((r = in.read(buffer)) != -1) {
            baos.write(buffer, 0, r);
        }
        
        return baos.toByteArray();        
    }    
    
    private static void put(String uri) throws IOException {
        URL u = new URL(uri);
        HttpURLConnection uc = (HttpURLConnection)u.openConnection();
        uc.setRequestMethod("PUT");
        
        int status = uc.getResponseCode();
        System.out.println("Status: " + status);
    }    
    
    private static void put(String uri, String mediaType, InputStream in) throws IOException {
        URL u = new URL(uri);
        HttpURLConnection uc = (HttpURLConnection)u.openConnection();
        uc.setRequestMethod("PUT");
        uc.setRequestProperty("Content-Type", mediaType);        
        uc.setDoOutput(true);
        
        OutputStream out = uc.getOutputStream();
        
        byte[] data = new byte[2048];
        int read;
        while ((read = in.read(data)) != -1)
            out.write(data, 0, read);
        out.close();
        
        int status = uc.getResponseCode();
        System.out.println("Status: " + status);
    }    
    
    private static void post(String uri, String mediaType, InputStream in) throws IOException {
        URL u = new URL(uri);
        HttpURLConnection uc = (HttpURLConnection)u.openConnection();
        uc.setRequestMethod("POST");
        uc.setRequestProperty("Content-Type", mediaType);        
        uc.setDoOutput(true);
        
        OutputStream out = uc.getOutputStream();
        
        byte[] data = new byte[2048];
        int read;
        while ((read = in.read(data)) != -1)
            out.write(data, 0, read);
        out.close();
        
        int status = uc.getResponseCode();
        System.out.println("Status: " + status);
    }    
    
    private static void delete(String uri) throws IOException {
        URL u = new URL(uri);
        HttpURLConnection uc = (HttpURLConnection)u.openConnection();
        uc.setRequestMethod("DELETE");
        
        int status = uc.getResponseCode();
        System.out.println("Status: " + status);
    }    
    
}
