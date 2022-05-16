EIONET Data Dictionary
======================

Introduction
------------
The Data Dictionary (DD) is used to support the delivery of environmental data by countries to Reportnet.
The contents in the data dictionary are used to generate pdfs of the technical specifications for dataflows as well as Excel templates.
The Data Dictionary also introduces the possibility of simple automated validation of deliveries by countries and facilitates the development of data submission interfaces.

Data Dictionary holds definitions of datasets, tables and data elements. Each of these three levels is defined by a set of attributes, the core set of which corresponds to ISO 11179 standard for describing data elements.
The whole attribute set is flexible and attributes can be added / removed from/to the system.

For doing a quick-start installation, read the following instructions. They give some hints on how to quickly get DD code from github, and how to set it up as a Tomcat web application. 

Installation
------------

### Prerequisites
DD runs on Java platform, and has been tested and run on Tomcat Java Servlet Container. DD source code is built with Maven.

Please download all of these software and install them according to the instructions found at their websites:
Java, Tomcat, Maven and GIT client.

The necessary versions are as follows:
* Java 8
* Maven 3.6.1 or higher
* Tomcat 8.5.45 or higher
* GIT 2.17.1 or higher
* Mariadb 5.5.63  or higher
* (Optional) Docker 1.6 or higher

### Download DD source code

Create build directory for source code and get code from GitHub
```sh
$ cd /var/local/build
$ git clone https://github.com/eea/eionet.datadict.git
```

NB! The resulting /var/local/build/eionet.datadict directory will be denoted  below as $CHECKOUT_HOME

### Build the application

```sh
$ cd $CHECKOUT_HOME
$ mvn -Denv=local -Dmaven.test.skip=true clean install
```

##### Custom headers and footers configuration (OPTIONAL):

Revise $CHECKOUT_HOME/custom/*.txt files for modifying the content of headers and footers in DD web pages texts and links.  You will find guidelines from inside $CHECKOUT_HOME/custom/README.txt.


### Local tomcat deployment

##### 1. Set up DB and DB user

Create the DataDict database and the DataDict user identified by a password.

```sh
$ mysql -u root -p
```

```sql
CREATE DATABASE DataDict;
CREATE USER 'dduser-here'@'localhost' IDENTIFIED BY 'password-here';
GRANT ALL PRIVILEGES ON DataDict.* TO 'dduser-here'@'localhost';
```

##### 2. Configure environment variables for tomcat:

###### 2.a Local properties configuration

In the env_setup/local/catalina_opts.txt file you will find all the CATALINA_OPTS that need to be configured with 
the specific values for your environment. Set these variables for your local tomcat installation.

Alternatively you can copy the default.properties file and create a local.properties file for development purposes.

###### 2.b Log4j RollingFile appender configuration

Add the following environment variables in tomcat for log4j2 configuration (these variables are used in log4j2.xml):
A variable with name logFilePath and value a path to a folder where log files will be stored. 
A variable with name queryLogRetainAll and value=false if log files should be deleted, true otherwise.
A variable with name queryLogRetentionDays and value the duration for which log files will be retained. Log files older than the specified duration will be deleted.

##### 3. Deploy on tomcat

Place the resulting $CHECKOUT_HOME/target/datadict.war into Tomcat's webapps directory, and start Tomcat.
At tomcat startup liquibase will create the initial DB structure.

### Unit tests

The unit test mechanism will install its own embedded database and create the tables when you execute them. Note that the embedded database will keep running afterwards. You can run individual tests with: -Dtest=DatasetImportHandlerTest
```sh
$ cd $CHECKOUT_HOME
$ mvn -Denv=unittest -Dmaven.test.skip=false test
```

### Integration Tests only

If you wish to skip unit tests and run only integration tests you may do so using the flag: -DskipUTs=true as shown below:
```sh
$mvn clean verify -DskipUTs=true
```

### Build for docker

Build the application as already described and then create a docker image by executing the docker build command in a directory containing both the Dockerfile and the datadict.war. If no tag is provided then the image will be tagged as "latest".
The docker file can be found at the env_setup/docker_build directory.

```sh
$ mkdir docker_build
$ cd docker_build
$ cp $CHECKOUT_HOME/target/datadict.war .
$ cp $CHECKOUT_HOME/env_setup/docker_build/* .
$ docker build -t eeacms/datadict:tag-here .
```
[Optional] In case you need to push the image you have created to DockerHub:

```sh
$ docker push eeacms/datadict:tag-here
```

### Rancher/Docker deployment

In the env_setup/docker_rancher directory you can find example files to be renamed and used as docker-compose.yml files for docker/rancher.
Both use the docker.env file for runtime properties configuration.

*Note: After running the docker-compose up or the rancher-compose up commands you have to create the DB and the corresponding user-password and restart the tomcat container.*

### Additional applications for modifying DD access permissions and help texts

Additional web applications are available for modifying user privileges and help texts in DD:
AclAdmin tool: https://svn.eionet.europa.eu/repositories/Reportnet/acladmin/trunk
HelpAdmin tool: https://svn.eionet.europa.eu/repositories/Reportnet/helpadmin/trunk

Installing these applications is done by similar steps:

1. Checkout from SVN
2. Create local.properties from default.properties
3. Modify environment specific properties in local.properties file
4. call "mvn install"
5. Rename acladmin.properties.dist/helpadmin.properties.dist to acladmin.properties/helpadmin.properties in ROOT/WEB-INF/classes folder and revise the content of the properties files
6. Rename *.acl.dist files to *.acl files in acladmin_contents/acls folder (only in AclAdminTool) and revise the contents of acladmin.group file
  More detailed information about ACL's is available at: http://taskman.eionet.europa.eu/projects/reportnet/wiki/AccessControlLists
7. Copy the created war files to tomcat webapps folder and start tomcat
Please find more detailed information in documentation of these applications.

### Logging to Sentry [Production env]

The file responsible for configuring logging in general and SENTRY integration specifically, is log4j2.xml.
The default place to put such a file is on container folder /opt/datadict.
The path for logging to that file can be set through CATALINA_OPTS environment variables mechanism by using this flag combined with the file location -Dlog4j.configurationFile=/opt/datadict/log4j2.xml

### Class shadowing
Classes org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate and org.springframework.jdbc.core.namedparam.JdbcTemplate have been shadowed. Library implementation is ignored
and local implementation is used instead.