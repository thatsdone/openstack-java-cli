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
import com.woorea.openstack.keystone.utils.KeystoneUtils;

import com.woorea.openstack.cinder.Cinder;
import com.woorea.openstack.cinder.model.Volume;
import com.woorea.openstack.cinder.model.Volumes;
import com.woorea.openstack.cinder.model.VolumeForCreate;
import com.woorea.openstack.cinder.model.Snapshot;
import com.woorea.openstack.cinder.model.Snapshots;

import java.lang.System;
import java.lang.Integer;

import  com.github.thatsdone.jopst.Jopst;
import  com.github.thatsdone.jopst.Utils;

public class Jcinder {

    private static Jopst jopst;
    private static Utils util;

    /**
     * getCinderClient() : returns a valid Cinder client class instance.
     *
     * @param   osAuthUrl    OS_AUTH_URL
     * @param   osPassword   OS_PASSWORD
     * @param   osTenantName OS_TENANT_NAME
     * @param   osUsername   OS_USERNAME
     * @return  Cinder class (of openstack-java-sdk) instance
     */
    public static Cinder getCinderClient() {
        return getCinderClient(jopst.getOsAuthUrl(),
                            jopst.getOsPassword(),
                            jopst.getOsTenantName(),
                            jopst.getOsUsername());
    }

    public static Cinder getCinderClient(String osAuthUrl, String osPassword,
                                     String osTenantName, String osUsername) {
        Keystone keystoneClient = new Keystone(jopst.getOsAuthUrl());

        // Set account information, and issue an authentication request.
        Access access = keystoneClient.tokens()
            .authenticate(new UsernamePassword(jopst.getOsUsername(),
                                               jopst.getOsPassword()))
            .withTenantName(jopst.getOsTenantName())
            .execute();

        String cinderEndpoint = KeystoneUtils
            .findEndpointURL(access.getServiceCatalog(),
                                 "volume", null, "public");
        if (jopst.isDebug()) {
            System.out.println("DEBUG: " + cinderEndpoint);
        }
        // Create a Cinder client object.
        Cinder cinderClient = new Cinder(cinderEndpoint);
        cinderClient.token(access.getToken().getId());

        return cinderClient;
    }

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
            Cinder cinderClient = getCinderClient();

            Volumes volumes;
            if (allTenants) {
                volumes = cinderClient.volumes()
                    .list(true).queryParam("all_tenants", "1").execute();
            } else {
                volumes = cinderClient.volumes().list(true).execute();
            }
            util.printJson(volumes);

        } else if (command.equals("create")) {
            VolumeForCreate v = new VolumeForCreate();

            for(int i = 0; i < args.length; i++) {
                if (args[i].equals("--display-name")) {
                    if (args.length > i + 1) {
                        v.setName(args[i + 1]);
                        i++;
                    }

                } else if (args[i].equals("--snapshot-id")) {
                    //FIXME(thatsdone): snapshot-id is uuid, I think...
                    i++;

                } else if (args[i].equals("--source-volid")) {
                    //FIXME(thatsdone): enhance sdk
                    i++;

                } else if (args[i].equals("--image-id")) {
                    //FIXME(thatsdone): enhance sdk
                    i++;

                } else if (args[i].equals("--display-description")) {
                    v.setDescription(args[i + 1]);
                    i++;

                } else if (args[i].equals("--volume-type")) {
                    //FIXME(thatsdone): enhance sdk
                    i++;

                } else if (args[i].equals("--availability-zone")) {
                    v.setAvailabilityZone(args[i + 1]);
                    i++;
                } else if (args[i].equals("--metadata")) {
                    //FIXME(thatsdone): TBD..
                }
            }
            v.setSize(new Integer(args[args.length - 1]));

            Cinder cinderClient = getCinderClient();
            Volume volume = cinderClient.volumes().create(v).execute();
            util.printJson(volume);

        } else if (command.equals("show")) {

            if (args.length <= 1) {
                System.out.println("Specify volume id.");
                System.exit(0);
            }
            Cinder cinderClient = getCinderClient();
            Volume volume;
            volume = cinderClient.volumes().show(args[1]).execute();
            util.printJson(volume);

        } else if (command.equals("delete")) {
            if (args.length <= 1) {
                System.out.println("Specify volume id.");
                System.exit(0);
            }
            Cinder cinderClient = getCinderClient();
            cinderClient.volumes().delete(args[1]).execute();

        } else if (command.equals("snapshot-list")) {
            boolean allTenants = true;
            Cinder cinderClient = getCinderClient();

            Snapshots snapshots;
            if (allTenants) {
                snapshots= cinderClient.snapshots()
                    .list(true).queryParam("all_tenants", "1").execute();
            } else {
                snapshots = cinderClient.snapshots().list(true).execute();
            }
            util.printJson(snapshots);

        }
    }
}
