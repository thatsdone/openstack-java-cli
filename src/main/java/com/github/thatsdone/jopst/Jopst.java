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

import com.woorea.openstack.cinder.Cinder;
//import com.woorea.openstack.cinder.model.Volumes;

import com.woorea.openstack.keystone.utils.KeystoneUtils;
//import com.woorea.openstack.nova.api.QuotaSetsResource;
//import com.woorea.openstack.nova.api.ServersResource;

import java.lang.System;
import java.io.PrintStream;
import java.lang.Integer;

import java.util.List;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;

import java.lang.reflect.Method;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.JavaType;
//import org.codehaus.jackson.impl.DefaultPrettyPrinter;
import java.util.logging.*;
import org.codehaus.jackson.map.annotate.JsonRootName;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;



public class Jopst {

    private static String commandName = "jopst";

    //work around for 'validate'
    private static String adminTokenId;
    public static Nova novaClient;

    private static boolean debug = false;
    private static boolean logMessage = false;

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

    public static Map<String, String> hArray = new LinkedHashMap<String, String>();

    public static Map<String, String> cArray = new LinkedHashMap<String, String>();

    public static Map<String, String> cinderCmds = new LinkedHashMap<String, String>();
    public static Map<String, String> keystoneCmds = new LinkedHashMap<String, String>();

    public static Map<String, Map<String, String>> cmMap = new LinkedHashMap<String, Map<String, String>>();


    static {
        cArray.put("list", "server");
        cArray.put("show", "server");
        cArray.put("host-list", "host");
        cArray.put("host-describe", "host");
        cArray.put("hypervisor-list", "hypervisor");
        cArray.put("hypervisor-show", "hypervisor");
        cArray.put("hypervisor-stats", "hypervisor");
        cArray.put("hypervisor-servers", "hypervisor");
        cArray.put("service-list", "service");
        cArray.put("service-enable", "service");
        cArray.put("service-disable", "service");
        cArray.put("usage-list", "quotaSet");
        cArray.put("aggregate-list", "aggregate");
        cArray.put("aggregate-details", "aggregate");
        cArray.put("aggregate-create", "aggregate");
        cArray.put("aggregate-delete", "aggregate");
        cArray.put("aggregate-add-host", "aggregate");
        cArray.put("aggregate-remove-host", "aggregate");
        cArray.put("aggregate-update", "aggregate");
        cArray.put("aggregate-set-metadata", "aggregate");
        cArray.put("flavor-list", "flavor");
        cArray.put("live-migration", "server");
        cArray.put("availability-zone-list", "availabilityZone");
        cArray.put("list-extensions", "extensions");
        cArray.put("image-list", "image");
        cArray.put("volume-list", "volume");
        cArray.put("rate-limits", "quotaSet");
        //        cArray.put("validate", "quotaSet");
        //        cArray.put("cinder-list", "volumes");

        hArray.put("nova", "Jnova");
        hArray.put("cinder", "Jcinder");
        hArray.put("keystone", "Jkeystone");

        cinderCmds.put("list", "volumes");
        keystoneCmds.put("validate", "validate");

        cmMap.put("nova", cArray);
        cmMap.put("cinder", cinderCmds);
        cmMap.put("keystone", keystoneCmds);

    }

    public static void printJson(Object o) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            //System.out.println(mapper.writeValueAsString(o));
            /*
              DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
              pp.indentArrayWith(new Lf2SpacesIndenter());
              System.out.println(mapper.writer(pp).writeValueAsString(o));
            */
            System.out.println(mapper.writerWithDefaultPrettyPrinter()
                               .writeValueAsString(o));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * NullFilter for disabling log output
     */
    private static class NullFilter implements Filter {

        // isLoggable says everything is NOT logabble.
        public boolean isLoggable(LogRecord record) {
            //System.out.println("DEBUG: " + record.getLevel());
            return false;
        }
    }

    private static void setupLog() {

        /*
         * research purpose code chunk to see all log handlers in the system.
         * LogManager lm  = LogManager.getLogManager();
         * for (Enumeration l = lm.getLoggerNames();l.hasMoreElements();) {
         *    String s = (String) l.nextElement();
         *    System.out.println(s);
         * }
         */
        if (!isLogMessage()) {
            // openstack-java-sdk gets/creates a logger named "os" internally.
            Logger l = Logger.getLogger("os");
            l.setFilter(new NullFilter());
            if (isDebug()) {
                System.out.println("DEBUG: Filter : " + l.getFilter());
                for (Handler h : l.getHandlers()) {
                    System.out.println("DEBUG: Handlers: " + h);
                }
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

        if(!hArray.containsKey(component)) {
            System.out.println("Unknown component: " + component);
            printUsage();
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
        //        if(!cArray.containsKey(command)) {
        if(!cmMap.get(component).containsKey(command)) {
            System.out.println("Unknown command: "+
                               component + " " + command);
            printUsage();
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

    public static boolean isDebug() {
        return debug;
    }

    public static boolean isLogMessage() {
        return logMessage;
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

            setupLog();

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
             * The a    bove contains TENANT_ID like:
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

    private static void printUsage() {
        System.out.println("Usage: ");

        for (Map.Entry<String, Map<String, String>> component : cmMap.entrySet()) {
            String c = component.getKey();
            for (Map.Entry<String, String> entry : component.getValue().entrySet()) {
                System.out.println("    " + commandName + " " +
                                   c + " " + entry.getKey());
            }            
        }
    }

    /**
     * main() : the main routine
     *
     * @param args
     */
    public static void main(String[] args) {

        if (args.length == 0) {
            printUsage();
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
                .getMethod(cmMap.get(component).get(command), String[].class);
            /*
            m = Class.forName("com.github.thatsdone.jopst.J" + component)
                .getMethod(cArray.get(command), String[].class);
            */
            /*
            m = Jnova.class.getMethod(cArray.get(command),
                                             String[].class);
            */
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
            printJson(validation);

        }
        /* else {
            System.out.println("Unknown command :" + command);

            }
        */
    }
}
