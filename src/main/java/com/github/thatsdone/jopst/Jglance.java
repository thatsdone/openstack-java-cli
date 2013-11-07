/**
 * Name : Jglance.java
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

import com.woorea.openstack.glance.Glance;
import com.woorea.openstack.glance.model.Images;

import java.lang.System;

import  com.github.thatsdone.jopst.Jopst;
import  com.github.thatsdone.jopst.Utils;

public class Jglance {

    private static Jopst jopst;
    private static Utils util;

    public static void image(String[] args) {
        if(jopst.isDebug()) {
            System.out.println("images() called."); 
        }

        String command = args[0];

        if (command.equals("image-list")) {
            Keystone keystoneClient = new Keystone(jopst.getOsAuthUrl());

            // Set account information, and issue an authentication request.
            Access access = keystoneClient.tokens()
                .authenticate(new UsernamePassword(jopst.getOsUsername(),
                                                   jopst.getOsPassword()))
                .withTenantName(jopst.getOsTenantName())
                .execute();

            String glanceEndpoint = KeystoneUtils
                .findEndpointURL(access.getServiceCatalog(),
                                 "image", null, "public");
            if (jopst.isDebug()) {
                System.out.println("DEBUG: " + glanceEndpoint);
            }
            // Create a Glance client object.
            Glance glanceClient = new Glance(glanceEndpoint);
            glanceClient.token(access.getToken().getId());

            Images images;
            images = glanceClient.images().list(true).execute();

            util.printJson(images);
        }
    }
}
