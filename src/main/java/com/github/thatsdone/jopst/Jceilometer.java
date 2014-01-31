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

    public static Ceilometer getCeilometerClient() {

        return getCeilometerClient(jopst.getOsAuthUrl(),
                                   jopst.getOsPassword(),
                                   jopst.getOsUsername(),
                                   jopst.getOsTenantName());
        
    }

    public static Ceilometer getCeilometerClient(String osAuthUrl,
                                                 String osPassword,
                                                 String osTenantName,
                                                 String osUsername) {

        Keystone keystoneClient = new Keystone(osAuthUrl);

        // Set account information, and issue an authentication request.
        Access access = keystoneClient.tokens()
            .authenticate(new UsernamePassword(osUsername, osPassword))
            .withTenantName(osTenantName)
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
        return ceilometerClient;
    }

    public static void ceilometer(String[] args) {
        if(jopst.isDebug()) {
            System.out.println("ceilometer() called."); 
        }

        String command = args[0];

        Ceilometer ceilometerClient = getCeilometerClient();

        if (command.equals("meter-list")) {
            try {
                List<Meter> meters;
                meters = ceilometerClient.meters().list().execute();
                util.printJson(meters);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (command.equals("statistics")) {

            if (args.length < 2) {
                System.out.println("Specify meter name");
                System.exit(0);
            }

            try {
                List<Statistics> statistics;
                statistics = ceilometerClient.meters().statistics(args[1]).execute();
                util.printJson(statistics);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (command.equals("resource-list")) {
            try {
                List<Resource> resources;
                resources = ceilometerClient.resources().list().execute();
                util.printJson(resources);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (command.equals("resource-show")) {
            try {
                Resource resource;
                resource = ceilometerClient.resources().show(args[1]).execute();
                util.printJson(resource);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (command.equals("sample-list")) {
            if (args.length < 2) {
                System.out.println("Specify meter name");
                System.exit(0);
            }

            try {
                List<Sample> samples;
                samples = ceilometerClient.meters().show(args[1]).execute();
                util.printJson(samples);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
