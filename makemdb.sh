#!/bin/sh

topdir=@WEBAPP.HOME@
java=/usr/bin/java

cd $topdir
ad=$topdir/WEB-INF
cp=@MYSQL.JAR@
cp=$cp:$ad/classes
cp=$cp:$ad/lib/activation.jar
cp=$cp:$ad/lib/castor.jar
cp=$cp:$ad/lib/commons-codec-1.3.jar
cp=$cp:$ad/lib/commons-collections-3.0.jar
cp=$cp:$ad/lib/commons-lang-1.0.jar
cp=$cp:$ad/lib/commons-logging-api.jar
cp=$cp:$ad/lib/eionet-dir.jar
cp=$cp:$ad/lib/itext-0.99.jar
cp=$cp:$ad/lib/jackcess-1.1.2.jar
cp=$cp:$ad/lib/log4j.jar
cp=$cp:$ad/lib/pja.jar
cp=$cp:$ad/lib/poi-2.5-final-20040302.jar
cp=$cp:$ad/lib/tomcat-util.jar
cp=$cp:$ad/lib/uit-client.jar
cp=$cp:$ad/lib/uit-definition.jar
cp=$cp:$ad/lib/uit-help.jar
cp=$cp:$ad/lib/uit-security.jar
cp=$cp:$ad/lib/uit-server.jar
cp=$cp:$CLASSPATH

$java -cp $cp eionet.meta.exports.mdb.MdbFile $*
