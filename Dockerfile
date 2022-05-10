FROM tomcat:8.5.78-jre8
RUN rm -rf /usr/local/tomcat/conf/logging.properties /usr/local/tomcat/webapps/*
COPY target/datadict.war /usr/local/tomcat/webapps/ROOT.war
