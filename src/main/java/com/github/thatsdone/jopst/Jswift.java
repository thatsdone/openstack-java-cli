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
            System.out.println("swift() called."); 
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
             * NOTE(thatsdone):
             * The below assumes a special version of Swift client class
             * which returns List<Container> class.
             */
            try {
                List<Container> containers = swiftClient.containers()
                    .list().queryParam("format", "json").execute();
                util.printJson(containers);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
