FROM tomcat:9.0.99-jre8
RUN rm -rf /usr/local/tomcat/conf/logging.properties /usr/local/tomcat/webapps/*
COPY target/datadict.war /usr/local/tomcat/webapps/ROOT.war
