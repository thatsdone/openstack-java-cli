/**
 * Name : Jneutron.java
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

import com.woorea.openstack.quantum.Quantum;
import com.woorea.openstack.quantum.model.Networks;
import com.woorea.openstack.quantum.model.Subnets;
import com.woorea.openstack.quantum.model.Routers;
import com.woorea.openstack.quantum.model.Ports;

import java.lang.System;

import  com.github.thatsdone.jopst.Jopst;
import  com.github.thatsdone.jopst.Utils;

public class Jneutron {

    private static Jopst jopst;
    private static Utils util;

    public static void network(String[] args) {
        if(jopst.isDebug()) {
            System.out.println("network() called."); 
        }

        String command = args[0];
            Keystone keystoneClient = new Keystone(jopst.getOsAuthUrl());

            // Set account information, and issue an authentication request.
            Access access = keystoneClient.tokens()
                .authenticate(new UsernamePassword(jopst.getOsUsername(),
                                                   jopst.getOsPassword()))
                .withTenantName(jopst.getOsTenantName())
                .execute();

            String neutronEndpoint = KeystoneUtils
                .findEndpointURL(access.getServiceCatalog(),
                                 "network", null, "public");
            if (jopst.isDebug()) {
                System.out.println("DEBUG: " + neutronEndpoint);
            }
            // Create a Quantum/Neutron client object.
            Quantum neutronClient = new Quantum(neutronEndpoint);
            neutronClient.token(access.getToken().getId());

        if (command.equals("net-list")) {
            // quantum/neutron  net-list
            Networks networks;
            networks = neutronClient.networks().list().execute();
            util.printJson(networks);

        } else if (command.equals("subnet-list")) {
            Subnets subnets;
            subnets = neutronClient.subnets().list().execute();
            util.printJson(subnets);

        } else if (command.equals("port-list")) {
            Ports ports;
            ports = neutronClient.ports().list().execute();
            util.printJson(ports);

        } else if (command.equals("router-list")) {
            Routers routers;
            routers = neutronClient.routers().list().execute();
            util.printJson(routers);
        }
    }
}
