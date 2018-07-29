/**
 * Name : Utils.java
 * 
 * Author: Masanori Itoh <masanori.itoh@gmail.com>
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.thatsdone.jopst;

import java.lang.System;
import java.util.Map;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.logging.*;

//import org.codehaus.jackson.map.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.github.thatsdone.jopst.Jopst;

public class Utils {

    private static Jopst jopst;

    /*
     * NullFilter for disabling log output
     */
    private static class NullFilter implements Filter {
        // isLoggable says everything is NOT logabble.
        public boolean isLoggable(LogRecord record) {
            //System.out.println("DEBUG: " + record.getLevel());
            return false;
        }
    }

    public static void setupLog() {
        /*
         * research purpose code chunk to see all log handlers in the system.
         * LogManager lm  = LogManager.getLogManager();
         * for (Enumeration l = lm.getLoggerNames();l.hasMoreElements();) {
         *    String s = (String) l.nextElement();
         *    System.out.println(s);
         * }
         */
        if (!jopst.isLogMessage()) {
            // openstack-java-sdk gets/creates a logger named "os" internally.
            Logger l = Logger.getLogger("os");
            l.setFilter(new NullFilter());
            if (jopst.isDebug()) {
                System.out.println("DEBUG: Filter : " + l.getFilter());
                for (Handler h : l.getHandlers()) {
                    System.out.println("DEBUG: Handlers: " + h);
                }
            }

        }
    }

    public static void printJson(Object o) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            //System.out.println(mapper.writeValueAsString(o));
            /*
              DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
              pp.indentArrayWith(new Lf2SpacesIndenter());
              System.out.println(mapper.writer(pp).writeValueAsString(o));
            */
            System.out.println(mapper.writerWithDefaultPrettyPrinter()
                               .writeValueAsString(o));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //from openstack-java-sdk/openstack-examples/../SwiftExample.java
    public static void write(InputStream is, String path) {
        try {
            OutputStream stream =
                new BufferedOutputStream(new FileOutputStream(path));
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            int len = 0;
            while ((len = is.read(buffer)) != -1) {
                stream.write(buffer, 0, len);
            }
            stream.close();
        } catch(IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
