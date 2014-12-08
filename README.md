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
* Java 1.6 or higher
* Maven 3.0.2 or higher
* Tomcat 6 or higher
* GIT 1.8.4 or higher
* MySql 5.1.71 or higher

#### Download DD source code

Create build directory for source code and get code from GitHub
```sh
$ cd /var/local/build
$ git clone https://github.com/eea/eionet.datadict.git
```

NB! The resulting /var/local/build/eionet.datadict directory will be denoted  below as $CHECKOUT_HOME

#### Adjust properties
Create _local.properties_ file by making a copy of _default.properties_.
```sh
$ cd $CHECKOUT_HOME
$ cp default.properties local.properties
```

In the freshly created _local.properties_ file, change property values as  appropriate for your environment. You will find meanings of every property  from inside the file as comments.

#### Database
Create DD database and database user in MySql matching the db configuration values in _local.properties_
```sh
$ mysql -u root -p
```

```sql
create database DataDict;
CREATE USER 'dduser'@'localhost' IDENTIFIED BY 'password-here';
GRANT ALL PRIVILEGES ON DataDict.* TO 'dduser'@'localhost';
```

#### Unit testing

The unit test mechanism will install its own embedded  database and create the tables when you execute them. Note that the MySQL database will keep running afterwards. You can run individual tests with: -Dtest=DatasetImportHandlerTest
```sh
$ cd $CHECKOUT_HOME
$ mvn -Denv=unittest -Dmaven.test.skip=false test
```

#### Custom headers and footers configuration (OPTIONAL)
Revise $CHECKOUT_HOME/custom/*.txt files for modifying the content of headers and footers in DD web pages texts and links.  You will find guidelines from inside $CHECKOUT_HOME/custom/README.txt.

#### Build the DD web application

The application install package is built with maven
```sh
$ cd $CHECKOUT_HOME
$ mvn -Dmaven.test.skip=true clean install
```

#### Import initial Seed data

Create  initial database structure
```sh
$ cd $CHECKOUT_HOME
$ mvn liquibase:update
```
Import seed data required for DD to operate
```sh
$ mvn -Dliquibase.changeLogFile=sql/dd-seeddata.xml liquibase:update
```

#### Register Eionet's GlobalSign CA certificates in your JVM. (OPTIONAL)

This step is required for making the EEA's Central Authentication Service (CAS) work with your DD. You need to register Eionet certificates in the JVM that runs the Tomcat where you deploy the DD. A small Java executable that does it, and a README on how to use it can be found here: https://svn.eionet.europa.eu/repositories/Reportnet/CASServer/contrib/installcert


#### Deployment
Place the resulting $CHECKOUT_HOME/target/datadict.war into Tomcat's webapps directory, and start Tomcat.

For more detailed instructions see "docs/DD Installation Manual.odt"

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
Please find more detailed information in documentation of these applications

REFERENCES for unit testing:
 - http://realsolve.co.uk/site/tech/dbunit-quickstart.php
 - http://www.onjava.com/pub/a/onjava/2004/01/21/dbunit.html
 - http://www.dbunit.org/

