/**
 * Name : Jkeystone.java
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

import java.lang.System;

import com.github.thatsdone.jopst.Jopst;
import com.github.thatsdone.jopst.Utils;

public class Jkeystone {

    private static Jopst jopst;
    private static Utils util;

    public static void token(String[] args) {
        if(jopst.isDebug()) {
            System.out.println("token() called."); 
        }

        String command = args[0];
        if (command.equals("token-get")) {
            Keystone keystoneClient = new Keystone(jopst.getOsAuthUrl());

            Access access = keystoneClient.tokens()
                .authenticate(new UsernamePassword(jopst.getOsUsername(),
                                                   jopst.getOsPassword()))
                .withTenantName(jopst.getOsTenantName())
                .execute();
            util.printJson(access);


        } if (command.equals("token-validate")) {

            // this is a poc code to validate a token using an admin token
            // using an extended feature of openstack-java-sdk.
            //
            Keystone keystoneClient = new Keystone(jopst.getOsAuthUrl());

            // First, create an administrative token.
            Access access = keystoneClient.tokens()
                .authenticate(new UsernamePassword(jopst.getOsUsername(),
                                                   jopst.getOsPassword()))
                .withTenantName(jopst.getOsTenantName())
                .execute();
            String adminTokenId = access.getToken().getId();

            // Second, create a non-administrative token.
            // replace user, password and tenant below.
            access = keystoneClient.tokens()
                .authenticate(new UsernamePassword("demo", "demo"))
                .withTenantName("demo")
                .execute();

            // Third, call validate() method.
            Access validation = keystoneClient.tokens()
                .validate(access.getToken().getId(), adminTokenId)
                .execute();
            util.printJson(validation);
        }
    }
}
