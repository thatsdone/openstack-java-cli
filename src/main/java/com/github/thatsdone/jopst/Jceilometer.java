/**
 * Name : Jceilometer.java
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

import com.woorea.openstack.ceilometer.Ceilometer;
import com.woorea.openstack.ceilometer.v2.model.Resource;
import com.woorea.openstack.ceilometer.v2.model.Meter;
import com.woorea.openstack.ceilometer.v2.model.Sample;
import com.woorea.openstack.ceilometer.v2.model.Statistics;

import java.lang.System;
import java.util.List;

import  com.github.thatsdone.jopst.Jopst;
import  com.github.thatsdone.jopst.Utils;

public class Jceilometer {

    private static Jopst jopst;
    private static Utils util;

    public static void ceilometer(String[] args) {
        if(jopst.isDebug()) {
            System.out.println("ceilometer() called."); 
        }

        String command = args[0];


            Keystone keystoneClient = new Keystone(jopst.getOsAuthUrl());

            // Set account information, and issue an authentication request.
            Access access = keystoneClient.tokens()
                .authenticate(new UsernamePassword(jopst.getOsUsername(),
                                                   jopst.getOsPassword()))
                .withTenantName(jopst.getOsTenantName())
                .execute();

            String ceilometerEndpoint = KeystoneUtils
                .findEndpointURL(access.getServiceCatalog(),
                                 "metering", null, "public");
            if (jopst.isDebug()) {
                System.out.println("DEBUG: " + ceilometerEndpoint);
            }
            // Create a Nova client object.
            Ceilometer ceilometerClient = new Ceilometer(ceilometerEndpoint);
            ceilometerClient.token(access.getToken().getId());

        if (command.equals("meter-list")) {
            //List<Meter> meters;
            //meters = ceilometerClient.meters().list().execute();
            //util.printJson(meters);

        } else if (command.equals("resource-list")) {
            //List<Resource> resources;
            //meters = (List<Resource>)ceilometerClient.resources().list().execute();
            //util.printJson(resources);
        }
    }
}
