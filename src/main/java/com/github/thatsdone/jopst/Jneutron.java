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
import com.woorea.openstack.quantum.model.Network;
import com.woorea.openstack.quantum.model.Networks;
import com.woorea.openstack.quantum.model.Subnet;
import com.woorea.openstack.quantum.model.Subnets;
import com.woorea.openstack.quantum.model.Router;
import com.woorea.openstack.quantum.model.Routers;
import com.woorea.openstack.quantum.model.Port;
import com.woorea.openstack.quantum.model.Ports;
import com.woorea.openstack.quantum.model.NetworkForCreate;
import com.woorea.openstack.quantum.model.SubnetForCreate;
import com.woorea.openstack.quantum.model.PortForCreate;
import com.woorea.openstack.quantum.model.RouterForCreate;
import com.woorea.openstack.quantum.model.GatewayInfo;
import com.woorea.openstack.quantum.model.RouterInterface;
import com.woorea.openstack.quantum.model.HostRoute;
import com.woorea.openstack.quantum.model.Agent;
import com.woorea.openstack.quantum.model.Agents;

import java.lang.System;

import  com.github.thatsdone.jopst.Jopst;
import  com.github.thatsdone.jopst.Utils;

public class Jneutron {

    private static Jopst jopst;
    private static Utils util;

    /**
     * getNeutronClient() : returns a valid Neutron client class instance.
     *
     * @param   osAuthUrl    OS_AUTH_URL
     * @param   osPassword   OS_PASSWORD
     * @param   osTenantName OS_TENANT_NAME
     * @param   osUsername   OS_USERNAME
     * @return  Quantum class (of openstack-java-sdk) instance
     */
    public static Quantum getNeutronClient() {
        return getNeutronClient(jopst.getOsAuthUrl(),
                            jopst.getOsPassword(),
                            jopst.getOsTenantName(),
                            jopst.getOsUsername());
    }

    public static Quantum getNeutronClient(String osAuthUrl, String osPassword,
                                     String osTenantName, String osUsername) {
        // Set account information, and issue an authentication request.
        Keystone keystoneClient = new Keystone(jopst.getOsAuthUrl());

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
        //tenantId = access.getToken().getTenant().getId();
        return neutronClient;
    }

    public static void network(String[] args) {
        if(jopst.isDebug()) {
            System.out.println("network() called."); 
        }

        String command = args[0];

        Quantum neutronClient = getNeutronClient();

        if (command.equals("net-list")) {
            // quantum/neutron net-list
            Networks networks = neutronClient.networks().list().execute();
            util.printJson(networks);

        } else if (command.equals("subnet-list")) {
            Subnets subnets = neutronClient.subnets().list().execute();
            util.printJson(subnets);

        } else if (command.equals("port-list")) {
            Ports ports = neutronClient.ports().list().execute();
            util.printJson(ports);

        } else if (command.equals("router-list")) {
            Routers routers = neutronClient.routers().list().execute();
            util.printJson(routers);

        } else if (command.equals("router-list-on-l3-agent")) {
            Routers routers = neutronClient.routers().listOnL3Agent(args[1]).execute();
            util.printJson(routers);

        } if (command.equals("net-show")) {
            // quantum/neutron  net-list
            Network network = neutronClient.networks().show(args[1]).execute();
            util.printJson(network);

        } if (command.equals("subnet-show")) {
            ;

        } if (command.equals("port-show")) {
            ;

        } if (command.equals("router-show")) {
            ;

        } if (command.equals("net-create")) {
            Network network;
            NetworkForCreate param = new NetworkForCreate();

            param.setAdminStateUp(true);
            param.setTenantId(null);

            for (int i = 1; i < args.length; i++) {
                if (args[i].equals("--tenant-id")) {
                    param.setTenantId(args[++i]);

                } else if (args[i].equals("--shared")) {
                    param.setShared(true);

                } else if (args[i].equals("--admin-state-down")) {
                    param.setAdminStateUp(false);

                } else if (args[i].equals("--provider:network_type")) {
                    //admin only
                    param.setProviderNetworkType(args[++i]);

                } else if (args[i].equals("--provider:physical_network")) {
                    //admin only
                    param.setProviderPhysicalNetwork(args[++i]);

                } else if (args[i].equals("--provider:segmentation_id")) {
                    //admin only
                    param.setProviderSegmentationId(new Integer(args[++i]));
                    i++;

                } else {
                    // handle the first non '--XXXX' parameter as 'name'
                    param.setName(args[i]);
                    break;
                }
            }
            if (param.getName() == null) {
                System.out.println("Specify name.");
                System.exit(0);
            }

            if (jopst.isDebug()) {
                util.printJson(param);
            }
            network = neutronClient.networks().create(param).execute();
            util.printJson(network);

        } if (command.equals("subnet-create")) {
            ;
        } if (command.equals("port-create")) {
            ;
        } if (command.equals("router-create")) {
            ;

        } if (command.equals("net-delete")) {
            ;
        } if (command.equals("subnet-delete")) {
            ;
        } if (command.equals("port-delete")) {
            ;
        } if (command.equals("router-delete")) {
            ;

        } if (command.equals("router-interface-add")) {
            ;
        } if (command.equals("router-interface-delete")) {
            ;
        } if (command.equals("router-gateway-set")) {
            // PUT /v2/routers
            GatewayInfo gateway = new GatewayInfo();
            String routerId = null;
            String externalNetworkId = null;

            gateway.setEnableSnat(true);

            for (int i = 1; i < args.length; i++) {
                if (args.equals("--disable-snat")) {
                    gateway.setEnableSnat(false);

                } else {
                    if (i == (args.length - 2)) {
                        routerId = args[i];

                    } else if (i == (args.length - 1)) {
                        externalNetworkId = args[i];
                    }
                }
            }

        } if (command.equals("router-gateway-clear")) {
            ;

        } else if (command.equals("agent-list")) {
            Agents agents = neutronClient.agents().list().execute();
            util.printJson(agents);

        }
        //FIXME(thatsdone): 
        // security-group-*
        // ceilometer support (meter-*) in havana
    }
}
