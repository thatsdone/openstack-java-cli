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

import com.woorea.openstack.keystone.Keystone;
import com.woorea.openstack.keystone.model.Access;
import com.woorea.openstack.keystone.model.authentication.UsernamePassword;
import com.woorea.openstack.keystone.utils.KeystoneUtils;

import com.woorea.openstack.swift.Swift;
import com.woorea.openstack.swift.model.Account;
import com.woorea.openstack.swift.model.Container;
import com.woorea.openstack.swift.model.Object;
import com.woorea.openstack.swift.model.ObjectDownload;
import com.woorea.openstack.swift.model.ObjectForUpload;

import java.lang.System;
import java.util.List;
import java.util.Map;
import java.io.InputStream;
import java.io.FileInputStream;

import com.github.thatsdone.jopst.Jopst;
import com.github.thatsdone.jopst.Utils;

import org.codehaus.jackson.type.TypeReference;
import org.codehaus.jackson.map.ObjectMapper;

public class Jswift {

    private static Jopst jopst;
    private static Utils util;


    private static Swift getSwiftClient() {

        Keystone keystoneClient = new Keystone(jopst.getOsAuthUrl());

        // Set account information, and issue an authentication request.
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

        } else if (command.equals("stat")) {

            Swift swiftClient = getSwiftClient();

            try {
                // FIXME(thatsdone):
                Map<String, String> res = null;

                // HEAD /v1/{account}
                if (args.length == 1) {
                    res = swiftClient.account()
                        .showAccount().getResponse().headers();

                // HEAD /v1/{account}/{container}
                } else if (args.length == 2) {
                    res = swiftClient.containers()
                        .show(args[1]).getResponse().headers();

                // HEAD /v1/{account}/{container}/{object}
                } else if (args.length == 3) {
                    res = swiftClient.containers()
                        .container(args[1])
                        .show(args[2])
                        .getResponse().headers();
                }

                for (String key : res.keySet()) {
                    System.out.println(String.format("%s : %s",
                                                     key, res.get(key)));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (command.equals("post")) {

            Swift swiftClient = getSwiftClient();

            // NOTE(thatsdone): swift post is not POST but PUT.
            // See API references. http://api.openstack.org/
            try {
                Map<String, String> res = null;

                // PUT /v1/{account}/{conntainer}
                if (args.length == 2) {
                    res = swiftClient.containers()
                        .create(args[1]).getResponse().headers();

                // PUT /v1/{account}/{conntainer}/{object}
                } else if (args.length == 3) {
                    res = swiftClient.containers()
                        .container(args[1])
                        .create(args[2]).getResponse().headers();
                }

                for (String key : res.keySet()) {
                    System.out.println(String.format("%s : %s",
                                                     key, res.get(key)));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (command.equals("delete")) {

            Swift swiftClient = getSwiftClient();

            try {
                Map<String, String> res = null;

                // DELETE /v1/{account}/{conntainer}
                if (args.length == 2) {
                    res = swiftClient.containers()
                        .delete(args[1]).getResponse().headers();

                // DELETE /v1/{account}/{conntainer}/{object}
                } else if (args.length == 3) {
                    res = swiftClient.containers()
                        .container(args[1])
                        .delete(args[2]).getResponse().headers();
                }
                for (String key : res.keySet()) {
                    System.out.println(String.format("%s : %s",
                                                     key, res.get(key)));
                }


            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (command.equals("upload")) {

            Swift swiftClient = getSwiftClient();

            try {
                Map<String, String> res = null;

                // PUT /v1/{account}/{conntainer}/{object}
                // Only a single file upload at a time is supported currently.
                if (args.length >= 3) {

                    ObjectForUpload upload = new ObjectForUpload();
                    InputStream is = new FileInputStream(args[2]);

                    upload.setContainer(args[1]);
                    upload.setName(args[2]);
                    upload.setInputStream(is);
                    
                    res = swiftClient.containers()
                        .container(args[1])
                        .upload(upload).getResponse().headers();

                    for (String key : res.keySet()) {
                        System.out.println(String.format("%s : %s",
                                                         key, res.get(key)));
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (command.equals("download")) {

            Swift swiftClient = getSwiftClient();

            try {
                Map<String, String> headers = null;
                
                // GET /v1/{account}/{conntainer}/{object}
                // Only a single file download at a time is supported currently.
                if (args.length >= 3) {

                    ObjectDownload download = null;

                    download = swiftClient.containers()
                        .container(args[1])
                        .download(args[2]).execute();

                    util.write(download.getInputStream(), args[2]);

                    /*
                    for (String key : res.keySet()) {
                        System.out.println(String.format("%s : %s",
                                                         key, res.get(key)));
                    }
                    */
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
