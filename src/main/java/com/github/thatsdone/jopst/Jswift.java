/**
 * Name : Jswift.java
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

import org.openstack4j.api.OSClient.OSClientV2;
import org.openstack4j.openstack.OSFactory;

import org.openstack4j.api.storage.*;
import org.openstack4j.model.storage.object.*;

//import org.codehaus.jackson.map.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper;



import java.lang.System;
import java.util.List;
import java.util.Map;
import java.io.InputStream;
import java.io.FileInputStream;

import com.github.thatsdone.jopst.Jopst;
import com.github.thatsdone.jopst.Utils;

//import org.codehaus.jackson.type.TypeReference;
//import org.codehaus.jackson.map.ObjectMapper;

public class Jswift {

    private static Jopst jopst;
    private static Utils util;


    private static ObjectStorageService getSwiftClient() {

        //Keystone keystoneClient = new Keystone(jopst.getOsAuthUrl());

        // Set account information, and issue an authentication request.
	/*
        Access access = keystoneClient.tokens()
            .authenticate(new UsernamePassword(jopst.getOsUsername(),
                                                   jopst.getOsPassword()))
            .withTenantName(jopst.getOsTenantName())
            .execute();

        String swiftEndpoint = KeystoneUtils
            .findEndpointURL(access.getServiceCatalog(),
                                 "object-store", null, "public");
        if (jopst.isDebug()) {
            System.out.println("DEBUG: " + swiftEndpoint);
        }
        // Create a Nova client object.
        Swift swiftClient = new Swift(swiftEndpoint);
        swiftClient.token(access.getToken().getId());

        return swiftClient;
	*/
	OSClientV2 os = OSFactory.builderV2()
	    .endpoint(jopst.getOsAuthUrl())
	    .credentials(jopst.getOsUsername(), jopst.getOsPassword())
	    .tenantName(jopst.getOsTenantName())
	    .authenticate();
	return os.objectStorage();
    }

    public static void swift(String[] args) {
        if(jopst.isDebug()) {
            System.out.println("swift() called."); 
        }

        String command = args[0];


        if (command.equals("list")) {
            /*
             * NOTE(thatsdone):
             * The below assumes a special version of Swift client class
             * which returns List<Container> class.
             */
	    /*
            if (args.length == 1) {

                Swift swiftClient = getSwiftClient();
                try {
                    List<Container> containers = swiftClient.containers()
                        .list().queryParam("format", "json").execute();
                    util.printJson(containers);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (args.length >= 2) {
                Swift swiftClient = getSwiftClient();
                try {
                    List<com.woorea.openstack.swift.model.Object> objects =
                        swiftClient.containers()
                        .container(args[1])
                        .list().queryParam("format", "json").execute();
                    util.printJson(objects);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
	    */

        } else if (command.equals("stat")) {

            ObjectStorageService swiftClient = getSwiftClient();
	    /*
	    SwiftAccount account = os.objectStorage().account().get();
	    System.out.println(account);
	    printJson(account);
	    */

            try {
                // FIXME(thatsdone):
                Map<String, String> res = null;

		//account is different from containers/objects.
                // HEAD /v1/{account}
                if (args.length == 1) {
                    util.printJson(swiftClient.account().get());
                    return;
                // HEAD /v1/{account}/{container}
                } else if (args.length == 2) {
                    res = swiftClient.containers().getMetadata(args[1]);

                // HEAD /v1/{account}/{container}/{object}
                } else if (args.length == 3) {
                    res = swiftClient.objects().getMetadata(args[1], args[2]);
                }

                for (String key : res.keySet()) {
                    System.out.println(String.format("%s : %s",
                                                     key, res.get(key)));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (command.equals("post")) {

            ObjectStorageService swiftClient = getSwiftClient();

        } else if (command.equals("delete")) {

            ObjectStorageService swiftClient = getSwiftClient();

        } else if (command.equals("upload")) {

            ObjectStorageService swiftClient = getSwiftClient();

        } else if (command.equals("download")) {

            ObjectStorageService swiftClient = getSwiftClient();

        }
    }
}
