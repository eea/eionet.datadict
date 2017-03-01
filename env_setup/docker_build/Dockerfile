FROM tomcat:8.0-jre8
RUN rm -rf /usr/local/tomcat/conf/logging.properties /usr/local/tomcat/webapps/*
COPY server.xml /usr/local/tomcat/conf
COPY datadict.war /usr/local/tomcat/webapps/ROOT.war
