/**
 * Name : Jheat.java
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

import com.woorea.openstack.heat.Heat;
import com.woorea.openstack.heat.model.Stacks;

import java.lang.System;

import  com.github.thatsdone.jopst.Jopst;
import com.github.thatsdone.jopst.Utils;

public class Jheat {

    private static Jopst jopst;
    private static Utils util;
    /*
    private static String osAuthUrl = jopst.getOsAuthUrl();
    private static String osPassword = jopst.getOsPassword();
    private static String osTenantName = jopst.getOsTenantName();
    private static String osUsername = jopst.getOsUsername();
    */
    public static void stack(String[] args) {
        if(jopst.isDebug()) {
            System.out.println("stack() called."); 
        }

        String command = args[0];

        if (command.equals("stack-list")) {

            Keystone keystoneClient = new Keystone(jopst.getOsAuthUrl());

            Access access = keystoneClient.tokens()
                .authenticate(new UsernamePassword(jopst.getOsUsername(),
                                                   jopst.getOsPassword()))
                .withTenantName(jopst.getOsTenantName())
                .execute();
        
            String heatEndpoint = KeystoneUtils
                .findEndpointURL(access.getServiceCatalog(),
                                 "orchestration", null, "public");
            if (jopst.isDebug()) {
                System.out.println("DEBUG: " + heatEndpoint);
            }
            Heat heatClient = new Heat(heatEndpoint);
            heatClient.token(access.getToken().getId());

            // heat stack-list
            Stacks stacks = heatClient.stacks().list().execute();
            util.printJson(stacks);
            if (jopst.isDebug()) {
                System.out.println(stacks);
            }
        }
    }
}
