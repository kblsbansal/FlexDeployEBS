javac XXMBRice183CO.java
java oracle.jrad.tools.xml.importer.XMLImporter $JAVA_TOP/mbci/oracle/apps/xxmb/rice183/webui/XXMBRice183PG.xml -username apps -password APPS -dbconnection "(description = (address_list = (address =(community = tcp.world)(protocol = tcp)(host=mbci-365-apdb01.itciss.com)(port=1521)))(connect_data =(sid = ASCPTEST)))" -rootdir $JAVA_TOP
