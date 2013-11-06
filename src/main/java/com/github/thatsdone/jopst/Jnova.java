/**
 * Name : Jnova.java
 * 
 * Author: Masanori Itoh <masanori.itoh@gmail.com>
 * 
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
import com.woorea.openstack.nova.Nova;
import com.woorea.openstack.nova.model.Server;
import com.woorea.openstack.nova.model.Servers;
import com.woorea.openstack.nova.model.Host;
import com.woorea.openstack.nova.model.Hosts;
import com.woorea.openstack.nova.model.Service;
import com.woorea.openstack.nova.model.Services;
import com.woorea.openstack.nova.model.Hypervisor;
import com.woorea.openstack.nova.model.Hypervisors;
import com.woorea.openstack.nova.model.HypervisorStatistics;
import com.woorea.openstack.nova.model.HypervisorServers;
import com.woorea.openstack.nova.model.QuotaSet;
import com.woorea.openstack.nova.model.SimpleTenantUsage;
import com.woorea.openstack.nova.model.HostAggregate;
import com.woorea.openstack.nova.model.HostAggregates;
import com.woorea.openstack.nova.model.AvailabilityZoneInfo;
import com.woorea.openstack.nova.model.Flavor;
import com.woorea.openstack.nova.model.Flavors;
import com.woorea.openstack.nova.model.Extensions;
import com.woorea.openstack.nova.model.Images;
import com.woorea.openstack.nova.model.Volumes;
import com.woorea.openstack.nova.model.Limits;


import java.lang.System;
import java.lang.Integer;

import com.github.thatsdone.jopst.Jopst;
import com.github.thatsdone.jopst.Utils;

public class Jnova {

    private static Jopst jopst;
    private static Utils util;

    /*
     * per resource method
     */
    public static void server(String[] args) {

        if(jopst.isDebug()) {
            System.out.println("server() called.");
        }

        String command = args[0];

        if (command.equals("list")) {
            //servers :
            boolean allTenants = false;
            for(int i = 0; i < args.length; i++) {
                if (args[i].equals("--all-tenants")) {
                    allTenants = true;
                }
            }
            Servers servers;
            if (allTenants) {
                // nova list --all-tenants
                // get servers of all tenants.
                // (want to use pagination if possible... ) 
                 servers = jopst.novaClient.servers()
                    .list(true).queryParam("all_tenants", "1").execute();
            } else {
                // Note that 'true' of list(true) appends 'detail'
                // path element like:  GET /v1.1/TENANT_ID/servers/detail
                // Simple 'nova list' does not use it.
                servers = jopst.novaClient.servers().list(true).execute();
            }
            util.printJson(servers);
            if (jopst.isDebug()) {
                for (Server s : servers) {
                    System.out.println("Hypervisor     : "
                                       + s.getHypervisorHostname());
                    System.out.println("VM Name        : "
                                       + s.getInstanceName());
                    System.out.println("Flavor         : " +
                                       s.getFlavor().getId());
                    System.out.println("Instance Id    : " + s.getId());
                    System.out.println("Image Id       : " +
                                       s.getImage().getId());
                    System.out.println("Keypair Name   : " + s.getKeyName());
                    System.out.println("Instance Name  : " + s.getName());
                    System.out.println("Instance Status: " + s.getStatus());
                    System.out.println("Tenant Id      : " + s.getTenantId());
                    System.out.println("User Id        : " + s.getUserId());
                    System.out.println("Task State     : " + s.getTaskState());
                    System.out.println("VM State       : " + s.getVmState());
                    System.out.println("");
                }
            }

            //
            if (jopst.isDebug()) {
                for(Server server : servers) {
                    System.out.println(server);
                }
            }
            // "list"

        } else if (command.equals("show")) {
            if (args.length >= 2) {
                Server server = jopst.novaClient.servers().show(args[1]).execute();
                util.printJson(server);
            } else {
                System.out.println("Specify server id");
            }
            // "show"

        } else if (command.equals("live-migration")) {
            boolean block = false;
            boolean disk = false;
            if (args.length >= 3) {
                //System.out.println("len: " + args.length);
                for (int i = 3; i < args.length; i++) {
                    if (args[i].equals("--block-migrate")) {
                        block = true;
                    } else if (args[i].equals("--disk-over-commit")) {
                        disk = true;
                    } else {
                        System.out.println("Unknown option: " + args[i]);
                    }
                }
                //System.out.println("block: " + block + ", disk: " + disk);
                //System.exit(0);
                jopst.novaClient.servers()
                    .migrateLive(args[1], args[2], block, disk)
                    .execute();

            } else {
                System.out.println("Specify server_id and hostname");
            }
        }
    }

    public static void host(String[] args) {

        if(jopst.isDebug()) {
            System.out.println("host() called.");
        }

        String command = args[0];

        // os-hosts : get per-host informatoin using /os-hosts extension
        if (command.equals("host-list")) {
            // nova host-list
            Hosts hosts = jopst.novaClient.hosts().list().execute();
            if (jopst.isDebug()) {
                System.out.println(hosts);
            }
            util.printJson(hosts);
            if (jopst.isDebug()) {
                for(Hosts.Host host : hosts) {
                    System.out.println(host);
                    if (host.getService().equals("compute")) {
                        String hostname = host.getHostName();
                        //System.out.println(hostname);
                        Host h = jopst.novaClient.hosts().show(hostname)
                            .execute();
                        System.out.println(h);
                    }
                }
            }

        } else if (command.equals("host-describe")) {
            // nova host-describe HOSTNAME
            if (args.length >= 2) {
                Host h = jopst.novaClient.hosts().show(args[1]).execute();
                util.printJson(h);
                if (jopst.isDebug()) {
                    System.out.println(h);
                }
            } else {
                System.out.println("Specify hostname");
            }
        }
    }

    public static void hypervisor(String[] args) {

        if(jopst.isDebug()) {
            System.out.println("hypervisor() called.");
        }

        String command = args[0];

        // os-hypervisors :
        if (command.equals("hypervisor-list")) {
            // nova hypervisor-list
            Hypervisors hypervisors = jopst.novaClient.hypervisors().list()
                .execute();
            if (jopst.isDebug()) {
                System.out.println(hypervisors);
            }
            util.printJson(hypervisors);

        } else if (command.equals("hypervisor-show")) {
            // nova hypervisor-show
            if (args.length < 2) {
                System.out.println("Specify hypervisor id");
                System.exit(0);
            }
            Hypervisor hv = jopst.novaClient.hypervisors()
                .show(new Integer(args[1])).execute();
            util.printJson(hv);
            if (jopst.isDebug()) {
                System.out.println(hv);
            }

        } else if (command.equals("hypervisor-stats")) {
            // nova hypervisor-stats
            HypervisorStatistics stat = jopst.novaClient.hypervisors()
                .showStats().execute();
            util.printJson(stat);
            if (jopst.isDebug()) {
                System.out.println(stat);
            }
        } else if (command.equals("hypervisor-servers")) {
            // nova hypervisor-servers
            if (args.length < 2) {
                System.out.println("Specify hypervisor name pattern");
                System.exit(0);
            }
            HypervisorServers hs = jopst.novaClient.hypervisors()
                .showServers(args[1]).execute();
            util.printJson(hs);
            if (jopst.isDebug()) {
                System.out.println(hs);
            }
        }
    }

    public static void service(String[] args) {

        if(jopst.isDebug()) {
            System.out.println("service() called.");
        }

        String command = args[0];

        if (command.startsWith("service")) {
            // os-services
            if (command.equals("service-list")) {
                // nova service-list
                Services services = jopst.novaClient.services().list().execute();
                util.printJson(services);
                if (jopst.isDebug()) {
                    for(Service service : services) {
                            System.out.println(service); 
                    } 
                }

            } else if (command.equals("service-disable")) {
                // nova service-disable HOST SERVIVCE
                if (args.length >= 3) {
                    Service resp = jopst.novaClient.services()
                        .disableService(args[1], args[2]).execute();
                    util.printJson(resp);    
                    if (jopst.isDebug()) {
                        System.out.println(resp);
                    }
                } else {
                    System.out.println("Specify host name and service binary name");
                }

            } else if (command.equals("service-enable")) { 
                // nova service-enable HOST SERVIVCE
                if (args.length >= 3) {
                    Service resp = jopst.novaClient.services()
                        .enableService(args[1], args[2]).execute();
                    util.printJson(resp);
                    if (jopst.isDebug()) {
                        System.out.println(resp);
                    }
                } else {
                    System.out.println("Specify host name and service binary name");
                }
            }
        }
    }

    public static void quotaSet(String[] args) {

        if(jopst.isDebug()) {
            System.out.println("quotaset() called.");
        }

        String command = args[0];

        if (command.equals("usage-list")) {
            /// os-simple-tenant-usage
            if (args.length >= 2) {
                // nova usage-list
                SimpleTenantUsage stu = jopst.novaClient.quotaSets()
                    .showUsage(args[1]).execute();
                util.printJson(stu);
                if (jopst.isDebug()) {
                    System.out.println(stu);
                }
            } else {
                System.out.println("Specify tenant id");
            }

        } else if (command.equals("rate-limits")) {
            // limits
            // nova rate-limits
            Limits limits = jopst.novaClient.quotaSets().showUsedLimits().execute();
            util.printJson(limits);
            if (jopst.isDebug()) {
                System.out.println(limits);
            }
        }
    }

    public static void flavor(String[] args) {

        if(jopst.isDebug()) {
            System.out.println("    flavor() called.");
        }

        String command = args[0];

        if (command.equals("flavor-list")) {
            // flavors
            // nova flavor-list
            Flavors flavors = jopst.novaClient.flavors().list(true).execute();
            util.printJson(flavors);
            if (jopst.isDebug()) {
                System.out.println(flavors);
            }
        }
    }

    public static void aggregate(String[] args) {

        if(jopst.isDebug()) {
            System.out.println("aggregate() called.");
        }

        String command = args[0];

        if (command.startsWith("aggregate")) {
            // os-aggregates
            if (command.equals("aggregate-list")) {
                // nova aggregate-list
                HostAggregates ags = jopst.novaClient.aggregates().list().execute();
                util.printJson(ags);
                if (jopst.isDebug()) {
                    System.out.println(ags);
                }

            } else if (command.equals("aggregate-details")) {
                // nova aggregate-details AGGREGATE_ID
                // does not work currently because of sdk (probably...)
                if (args.length >= 2) {
                    HostAggregate ag = jopst.novaClient.aggregates().
                        showAggregate(args[1]).execute();
                    util.printJson(ag);
                    if (jopst.isDebug()) {
                        System.out.println(ag);
                    }

                } else {
                    System.out.println("Specify aggregate id");
                }

            } else if (command.equals("aggregate-create")) {
                //NOTE(itoumsn): availability_zone is optional!
                if (args.length >= 2) {
                    HostAggregate ag = jopst.novaClient.aggregates().
                        createAggregate(args[1], (args.length == 2) ? null : args[2])
                        .execute();
                    util.printJson(ag);
                    if (jopst.isDebug()) {
                        System.out.println(ag);
                    }

                } else {
                    System.out.println("Specify aggregate name and availability zone name(optionally)");
                }

            } else if (command.equals("aggregate-delete")) {
                if (args.length >= 2) {
                    jopst.novaClient.aggregates().
                        deleteAggregate(args[1]).execute();

                } else {
                    System.out.println("Specify aggregate id");
                }

            } else if (command.equals("aggregate-add-host")) {
                if (args.length >= 3) {
                    HostAggregate ag = jopst.novaClient.aggregates().
                        addHost(args[1], args[2]).execute();
                    util.printJson(ag);
                    if (jopst.isDebug()) {
                        System.out.println(ag);
                    }

                } else {
                    System.out.println("Specify aggregate id and host name");
                }

            } else if (command.equals("aggregate-remove-host")) {
                if (args.length >= 3) {
                    HostAggregate ag = jopst.novaClient.aggregates()
                        .removeHost(args[1], args[2]).execute();
                    util.printJson(ag);
                    if (jopst.isDebug()) {
                        System.out.println(ag);
                    }

                } else {
                    System.out.println("Specify aggregate id and host name");
                }

            } else if (command.equals("aggregate-update")) {
                if (args.length >= 3) {
                    HostAggregate ag = jopst.novaClient.aggregates()
                        .updateAggregateMetadata(args[1], args[2], (args.length == 3 ? null : args[3])).execute();
                    util.printJson(ag);
                    if (jopst.isDebug()) {
                        System.out.println(ag);
                    }

                } else {
                    System.out.println("Specify aggregate id, name and availability_zone(optional)");
                }

            } else if (command.equals("aggregate-set-metadata")) {
                if (args.length >= 3) {
                    String[] kv = args[2].split("=");
                    if (jopst.isDebug()) {
                        System.out.println("key / value = " + kv[0] + " / " + kv[1]);
                    }

                    HostAggregate ag = jopst.novaClient.aggregates()
                        .setMetadata(args[1], kv[0], kv[1]).execute();
                    util.printJson(ag);
                    if (jopst.isDebug()) {
                        System.out.println(ag);
                    }

                } else {
                    System.out.println("Specify aggregate id and 'key=value' pair");
                }
            }
        }
    }

    public static void availabilityZone(String[] args) {

        if(jopst.isDebug()) {
            System.out.println("availabilityZone() called.");
        } 

        String command = args[0];

        if (command.equals("availability-zone-list")) {
            // os-availability-zone
            // nova availability-zone-list
            AvailabilityZoneInfo az = jopst.novaClient.availabilityZoneInfo()
                .show(true).execute();
            util.printJson(az);
            if (jopst.isDebug()) {
                System.out.println(az);
            }

        }
    }

    public static void extensions(String[] args) {

        if(jopst.isDebug()) {
            System.out.println("extensions() called."); 
        }

        String command = args[0];

        if (command.equals("list-extensions")) {
            // extensions
            // nova list-extensions
            /*
             * NOTE(thatsdone): list(true) causes an error.
             * Looks like '/v2/TENANT_ID/extensions/detail' is not supported.
             */
            Extensions ex = jopst.novaClient.extensions().list(false).execute();
            util.printJson(ex);
            if (jopst.isDebug()) {
                System.out.println(ex);
            }
        }
    }

    public static void image(String[] args) {

        if(jopst.isDebug()) {
            System.out.println("image() called.");
        }

        String command = args[0];

        if (command.equals("image-list")) {
            // images
            // nova image-list
            Images  img = jopst.novaClient.images().list(true).execute();
            util.printJson(img);
            if (jopst.isDebug()) {
                System.out.println(img);
            }
        }
    }

    public static void volume(String[] args) {

        if(jopst.isDebug()) {
            System.out.println("volume() called."); 
        }

        String command = args[0];

        if (command.equals("volume-list")) {
            // os-volumes
            // nova volume-list
            // Note that nova command uses 'volumes' instead of 'os-volumes'.
            boolean allTenants = false;
            for(int i = 0; i < args.length; i++) {
                if (args[i].equals("--all-tenants")) {
                    allTenants = true;
                }
            }
            Volumes volumes;
            // Note that 'true' of list(true) appends 'detail'
            // path element like:  GET /v1.1/TENANT_ID/volumes/detail.

            if (allTenants) {
                // nova volume-list --all-tenants
                 volumes = jopst.novaClient.volumes()
                    .list(true).queryParam("all_tenants", "1").execute();
            } else {
                volumes = jopst.novaClient.volumes().list(true).execute();
            }
            util.printJson(volumes);
            if (jopst.isDebug()) {
                System.out.println(volumes);
            }
        }
    }

}
