/**
 * Name : Jcinder.java
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

import com.woorea.openstack.cinder.Cinder;
import com.woorea.openstack.cinder.model.Volumes;

import com.woorea.openstack.keystone.utils.KeystoneUtils;

import java.lang.System;

import  com.github.thatsdone.jopst.Jopst;

public class Jcinder {

    private static Jopst jopst;

    private static String osAuthUrl = jopst.getOsAuthUrl();
    private static String osPassword = jopst.getOsPassword();
    private static String osTenantName = jopst.getOsTenantName();
    private static String osUsername = jopst.getOsUsername();


    public static void volumes(String[] args) {
        if(jopst.isDebug()) {
            System.out.println("volumes() called."); 
        }

        String command = args[0];

        if (command.equals("list")) {
            boolean allTenants = false;
            for(int i = 0; i < args.length; i++) {
                if (args[i].equals("--all-tenants")) {
                    if (i + 1 == args.length) {
                        System.out.println("Specify 0 or 1.");
                        System.exit(0);
                    } else if (args[i + 1].equals("1")){
                        allTenants = true;
                        break;
                    }
                }
            }
            Keystone keystoneClient = new Keystone(osAuthUrl);

            // Set account information, and issue an authentication request.
            Access access = keystoneClient.tokens()
                .authenticate(new UsernamePassword(osUsername, osPassword))
                .withTenantName(osTenantName)
                .execute();

            String cinderEndpoint = KeystoneUtils
                .findEndpointURL(access.getServiceCatalog(),
                                 "volume", null, "public");
            if (jopst.isDebug()) {
                System.out.println("DEBUG: " + cinderEndpoint);
            }
            // Create a Nova client object.
            Cinder cinderClient = new Cinder(cinderEndpoint);
            cinderClient.token(access.getToken().getId());

            Volumes volumes;
            if (allTenants) {
                volumes = cinderClient.volumes()
                    .list(true).queryParam("all_tenants", "1").execute();
            } else {
                volumes = cinderClient.volumes().list(true).execute();
            }
            jopst.printJson(volumes);
        }
    }
}
