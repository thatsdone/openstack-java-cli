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

import com.woorea.openstack.cinder.Cinder;
import com.woorea.openstack.cinder.model.Volumes;

import com.woorea.openstack.keystone.utils.KeystoneUtils;

import java.lang.System;

import  com.github.thatsdone.jopst.Jopst;

public class Jkeystone {

    private static Jopst jopst;

    private static String osAuthUrl = jopst.getOsAuthUrl();
    private static String osPassword = jopst.getOsPassword();
    private static String osTenantName = jopst.getOsTenantName();
    private static String osUsername = jopst.getOsUsername();

    public static void validate(String[] args) {
        if(jopst.isDebug()) {
            System.out.println("validate() called."); 
        }

        String command = args[0];

    }
}
