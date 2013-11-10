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

import com.github.thatsdone.jopst.Jopst;
import com.github.thatsdone.jopst.Utils;

import org.codehaus.jackson.type.TypeReference;
import org.codehaus.jackson.map.ObjectMapper;

public class Jswift {

    private static Jopst jopst;
    private static Utils util;

    public static void swift(String[] args) {
        if(jopst.isDebug()) {
            System.out.println("token() called."); 
        }

        String command = args[0];

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

        if (command.equals("list")) {
            /*
            Container container;
            container = swiftClient.containers().show(args[1]).execute();
            */
            /*
             * FIXME(thatsdone):
             * A POC implementation using a work around in
             * openstack-java-sdk layer which returns a String, not
             * a List<Container>. The below does deserialization by itself.
             */
            String container = swiftClient.containers().list().execute();
            //System.out.println("result: " + container);
            //util.printJson(container);
            try {
                List<Container>containerObj =
                    new ObjectMapper()
                    .readValue(container,
                               new TypeReference<List<Container>>(){});
                // Note that the below dows not work.
                //              .readValue(container, List<Container>);
                util.printJson(containerObj);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
