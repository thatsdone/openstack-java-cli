openstack-java-cli
==================

What is this?
-------------

Simply speaking, this is a Java version of OpenStack client program.

It's implemented as a single jar file, and now subsets of nova, keystone and cinder commands are available.

I began to write this small program for invoking the OpenStack Java SDK API methods
which I extended mainly. Thus, please do not expect that this program has production quality. :o

Installation
-------------
0. Prerequistes

 You need to install at least JDK and Maven.
 * Java
   - I use Java7 on CentOS 6.4(x86_64). But, I believe that Java6 should also work.
 * Maven
   - I use apache-maven-3.1.0

1. Install openstack-java-sdk

  'openstack-java-cli' depends on some extended features of a forked version
  of the OpenStack Java SDK. Please clone from the following repository,
  checkout 'dev' branch, and install it.
<pre>
  $ git clone https://github.com/thatsdone/openstack-java-sdk.git sdk
  $ cd sdk
  $ git checkout dev
  $ mvn install
</pre>
 The above will install jar files to your local Maven repository under:
<pre>
  ~/.m2/
</pre>
  Note that the upstream OpenStack Java SDK repository is:
    https://github.com/woorea/openstack-java-sdk


2. Install openstack-java-cli
<pre>
 $ git clone https://github.com/thatsdone/openstack-java-cli.git sdk
 $ mvn install
</pre>

 The above will install a jar file to your local Maven repository under:
<pre>
  ~/.m2/
</pre>

Usage
-------------

Synopsis is like the following:
<pre>
jopst COMPONENTS [COMMON OPTIONS] SUB-COMMAND [SUB-COMMAND SPECIFIC OPTIONS]
</pre>

'jopst' is a simple shell script, and if you want to change versions of dependency libraries,
please modify that.

**COMPONENTS**

Either of the following (currently):

 * nova
 * keystone
 * cinder

**COMMON OPTIONS**

 * --debug
 * --log-message

**SUB-COMMAND**

Almost the same as nova/keystone/cinder command.

Please find the currently implemented sub commands below.

**SUB-COMMAND SPECIFIC ARGUMENTS/OPTIONS**

Almost the same as (supported) nova/keystone/cinder command.

**NOTES**

1. Among common options, '--log-message' is an extended option to log API messages.
2. keystone validate is an extended subcommand to invoke token validation.


Also, please find supported subcommands in the following usage output.
<pre>
$ bin/jopst
Usage:
    jopst nova list
    jopst nova show
    jopst nova host-list
    jopst nova host-describe
    jopst nova hypervisor-list
    jopst nova hypervisor-show
    jopst nova hypervisor-stats
    jopst nova hypervisor-servers
    jopst nova service-list
    jopst nova service-enable
    jopst nova service-disable
    jopst nova usage-list
    jopst nova aggregate-list
    jopst nova aggregate-details
    jopst nova aggregate-create
    jopst nova aggregate-delete
    jopst nova aggregate-add-host
    jopst nova aggregate-remove-host
    jopst nova aggregate-update
    jopst nova aggregate-set-metadata
    jopst nova flavor-list
    jopst nova live-migration
    jopst nova availability-zone-list
    jopst nova list-extensions
    jopst nova image-list
    jopst nova volume-list
    jopst nova rate-limits
    jopst cinder list
    jopst keystone validate
</pre>

TODO
-------------

* Implement more sub-commands.

LICENSE
-------------

This program is licensed under the Apache License 2.0.
