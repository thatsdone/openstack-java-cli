/**
 * Name : Jopst.java
 * 
 * Description: 
 *  An integrated OpenStack client command using:
 *    https://github.com/woorea/openstack-java-sdk
 *
 *  Note that this program uses some extended features of the java sdk
 *  of a forked version available below:
 *    https://github.com/thatsdone/openstack-java-sdk 
 * 
 *  Currently the following sub command equivalents are implemented.
 *    jopst nova list
 *    jopst nova show
 *    jopst nova host-list
 *    jopst nova host-describe
 *    jopst nova hypervisor-list
 *    jopst nova hypervisor-show
 *    jopst nova hypervisor-stats
 *    jopst nova hypervisor-servers
 *    jopst nova service-list
 *    jopst nova service-enable
 *    jopst nova service-disable
 *    jopst nova usage-list
 *    jopst nova aggregate-list
 *    jopst nova aggregate-details
 *    jopst nova aggregate-create
 *    jopst nova aggregate-delete
 *    jopst nova aggregate-add-host
 *    jopst nova aggregate-remove-host
 *    jopst nova aggregate-update
 *    jopst nova aggregate-set-metadata
 *    jopst nova flavor-list
 *    jopst nova live-migration
 *    jopst nova availability-zone-list
 *    jopst nova list-extensions
 *    jopst nova image-list
 *    jopst nova volume-list
 *    jopst nova rate-limits
 *    jopst cinder list
 *    jopst keystone validate
 * 
 *  Author: Masanori Itoh <masanori.itoh@gmail.com>
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

import com.woorea.openstack.keystone.utils.KeystoneUtils;

import java.lang.System;
import java.lang.Integer;
import java.lang.reflect.Method;

import java.util.List;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.logging.*;

import com.github.thatsdone.jopst.Utils;


public class Jopst {

    private static String commandName = "jopst";

    public static String getCommandName() {
        return commandName;
    }

    //work around for 'validate'
    private static String adminTokenId;

    public static Nova novaClient;
    private static Utils util;

    private static boolean debug = false;
    private static boolean logMessage = false;

    public static boolean isDebug() {
        return debug;
    }

    public static boolean isLogMessage() {
        return logMessage;
    }

    // Get account informatoin from environment variables.
    private static String osAuthUrl = System.getenv("OS_AUTH_URL");
    private static String osPassword = System.getenv("OS_PASSWORD");
    private static String osTenantName = System.getenv("OS_TENANT_NAME");
    private static String osUsername = System.getenv("OS_USERNAME");

    public static String getOsAuthUrl() {
        return osAuthUrl;
    }
    public static String getOsPassword() {
        return osPassword;
    }
    public static String getOsTenantName() {
        return osTenantName;
    }
    public static String getOsUsername() {
        return osUsername;
    }

    public static Map<String, String> components =
        new LinkedHashMap<String, String>();

    public static Map<String, String> novaCmds =
        new LinkedHashMap<String, String>();

    public static Map<String, String> cinderCmds =
        new LinkedHashMap<String, String>();
    public static Map<String, String> keystoneCmds =
        new LinkedHashMap<String, String>();

    public static Map<String, Map<String, String>> cmdMap =
        new LinkedHashMap<String, Map<String, String>>();

    static {
        novaCmds.put("list", "server");
        novaCmds.put("show", "server");
        novaCmds.put("host-list", "host");
        novaCmds.put("host-describe", "host");
        novaCmds.put("hypervisor-list", "hypervisor");
        novaCmds.put("hypervisor-show", "hypervisor");
        novaCmds.put("hypervisor-stats", "hypervisor");
        novaCmds.put("hypervisor-servers", "hypervisor");
        novaCmds.put("service-list", "service");
        novaCmds.put("service-enable", "service");
        novaCmds.put("service-disable", "service");
        novaCmds.put("usage-list", "quotaSet");
        novaCmds.put("aggregate-list", "aggregate");
        novaCmds.put("aggregate-details", "aggregate");
        novaCmds.put("aggregate-create", "aggregate");
        novaCmds.put("aggregate-delete", "aggregate");
        novaCmds.put("aggregate-add-host", "aggregate");
        novaCmds.put("aggregate-remove-host", "aggregate");
        novaCmds.put("aggregate-update", "aggregate");
        novaCmds.put("aggregate-set-metadata", "aggregate");
        novaCmds.put("flavor-list", "flavor");
        novaCmds.put("live-migration", "server");
        novaCmds.put("availability-zone-list", "availabilityZone");
        novaCmds.put("list-extensions", "extensions");
        novaCmds.put("image-list", "image");
        novaCmds.put("volume-list", "volume");
        novaCmds.put("rate-limits", "quotaSet");

        cinderCmds.put("list", "volumes");

        keystoneCmds.put("validate", "validate");

        cmdMap.put("nova", novaCmds);
        cmdMap.put("cinder", cinderCmds);
        cmdMap.put("keystone", keystoneCmds);

        components.put("nova", "Jnova");
        components.put("cinder", "Jcinder");
        components.put("keystone", "Jkeystone");
    }

    public static void printUsage(Map<String, Map<String, String>> cmdMap) {
        System.out.println("Usage: ");

        for (Map.Entry<String, Map<String, String>> component : cmdMap.entrySet()) {
            String c = component.getKey();
            for (Map.Entry<String, String> entry : component.getValue().entrySet()) {
                System.out.println("    " + getCommandName() + " " +
                                   c + " " + entry.getKey());
            }            
        }
    }

    /**
     * parseCommon() : parse the top level command line arguments.
     *
     * @param   args : the same as args of main()
     * @return  LinkedHashMap of handler method and arguments for the command.
     */
    private static String[] parseCommon(String[] args) {
        String command = null;
        String component = args[0];

        if(!components.containsKey(component)) {
            System.out.println("Unknown component: " + component);
            printUsage(cmdMap);
            System.exit(0);
        }

        int idx;
        for(idx = 1; idx < args.length; idx++) {
            //System.out.println("i = " + idx + " args[i] = " + args[idx]);
            if (args[idx].equals("--debug")) {
                debug = true;

            } else if (args[idx].equals("--log-message")) {
                logMessage = true;

            } else if (args[idx].equals("--os-username")) {
                idx++;
                osUsername = args[idx];
                continue;

            } else if (args[idx].equals("--os-password")) {
                idx++;
                osPassword = args[idx];
                continue;

            } else if (args[idx].equals("--os-tenant-name")) {
                idx++;
                osTenantName = args[idx];
                continue;

            } else if (args[idx].equals("--os-auth-url")) {
                idx++;
                osAuthUrl = args[idx];
                continue;

            } else if (!args[idx].startsWith("--")) {
                command = args[idx];
                break;
            }
        }
        //FIXME(thatsdone): look up nova/cinder...Cmds
        //        if(!novaCmds.containsKey(command)) {
        if(!cmdMap.get(component).containsKey(command)) {
            System.out.println("Unknown command: "+
                               component + " " + command);
            printUsage(cmdMap);
            System.exit(0);
        }
        String subargs[] = Arrays.copyOfRange(args, idx, args.length);

        if (isDebug()) {
            System.out.println("DEBUG: command is: " + command);
            for (String s : subargs) {
                System.out.println("DEBUG: subargs: " + s);
            }
        }

        if (isDebug()) {
            System.out.println("OS_AUTH_URL    : " + osAuthUrl);
            System.out.println("OS_PASSWORD    : " + osPassword);
            System.out.println("OS_TENANT_NAME : " + osTenantName);
            System.out.println("OS_USERNAME    : " + osUsername);
        }
        return subargs;
    }

    /**
     * getNovaClient() : returns a valid Nova client class instance.
     *
     * @param   osAuthUrl    OS_AUTH_URL
     * @param   osPassword   OS_PASSWORD
     * @param   osTenantName OS_TENANT_NAME
     * @param   osUsername   OS_USERNAME
     * @return  Nova class (of openstack-java-sdk) instance
     */
    public static Nova getNovaClient(String osAuthUrl, String osPassword,
                                     String osTenantName, String osUsername) {
        try {
            // First, create a Keystone cliet class instance.
            Keystone keystoneClient = new Keystone(osAuthUrl);

            util.setupLog();

            // Set account information, and issue an authentication request.
            Access access = keystoneClient.tokens()
                .authenticate(new UsernamePassword(osUsername, osPassword))
                .withTenantName(osTenantName)
                .execute();
        
            String novaEndpoint = KeystoneUtils
                .findEndpointURL(access.getServiceCatalog(),
                                 "compute", null, "public");
            if (isDebug()) {
                System.out.println("DEBUG: " + novaEndpoint);
            }
            /*  
             * The above contains TENANT_ID like:
             *   http://SERVICE_HOST:PORT/v1.1/TENANT_ID
             * according to endpoints definition in keystone configuration.
             * It's the same as keystone endpoint-list.
             *
             * Note that we don't need to append a '/' to the URL because
             * openstack-java-sdk library codes add it.
             *   Nova novaClient = new Nova(novaEndpoint.concat("/"));
             */

            // Create a Nova client object.
            novaClient = new Nova(novaEndpoint);

            /*
             * Set the token now we got for the following requests.
             * Note that we can use the same token in the above keystone 
             * response unless it's not expired.
             */
            novaClient.token(access.getToken().getId());

            //work around for 'validate'
            adminTokenId = access.getToken().getId();
            return novaClient;

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to create/initialize a Nova client.");
            System.exit(0);
        }
        // never here
        return null;
    }


    /**
     * main() : the main routine
     *
     * @param args
     */
    public static void main(String[] args) {

        if (args.length == 0) {
            printUsage(cmdMap);
            System.exit(0);
        }

        // Parse comnand line arguments.
        String[] c = parseCommon(args);

        if (osAuthUrl == null || osPassword == null ||
            osTenantName == null || osUsername == null) {
            System.out.println("specify account information.");
            System.exit(0);
        }

        String command = c[0];

        String component = args[0];

        Method m = null;
        try {
            m = Class.forName("com.github.thatsdone.jopst.J" + component)
                .getMethod(cmdMap.get(component).get(command), String[].class);

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }

        // getNovaClient() succeeds, or aborts the process.
        Nova novaClient = getNovaClient(osAuthUrl, osPassword,
                                        osTenantName, osUsername);

        try {
            // Note(thatsdone):
            // Without the cast (Object) below, elements of cmdargs[]
            // will be handled as independent classes and causes an error.
            // Could be a pitfall.
            m.invoke(null, (Object)c);

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }

        /*
         * command handlers
         */

        if (command.equals("validate")) {
            // this is a poc code to validate a token using an admin token
            // using an extended feature of openstack-java-sdk.
            //
            // First, create a non-administrative token.
            Keystone keystoneClient = new Keystone(osAuthUrl);
            // replace user, password and tenant below.
            Access access = keystoneClient.tokens()
                .authenticate(new UsernamePassword("demo", "demo"))
                .withTenantName("demo")
                .execute();

            // Second, call validate() method.
            Access validation = keystoneClient.tokens()
                .validate(access.getToken().getId(), adminTokenId)
                .execute();
            util.printJson(validation);

        }
        /* else {
            System.out.println("Unknown command :" + command);

            }
        */
    }
}
