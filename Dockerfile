FROM tomcat:9.0.118-jre11
RUN rm -rf /usr/local/tomcat/conf/logging.properties /usr/local/tomcat/webapps/*
COPY target/datadict.war /usr/local/tomcat/webapps/ROOT.war
