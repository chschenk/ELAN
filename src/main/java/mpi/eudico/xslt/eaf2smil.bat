@echo off
if not [%3]==[] set tier=-param tier %3
if not [%4]==[] set media_url=-param media_url %4
if not [%5]==[] set media_start_time=-param media_start_time %5
if not [%6]==[] set media_stop_time=-param media_stop_time %6
java -classpath xalan.jar;xercesImpl.jar;xmlParserAPIs.jar org.apache.xalan.xslt.Process -in %1 -xsl eaf2smil.xsl -out %2 %tier% %media_url% %media_start_time% %media_stop_time%
set tier=
set media_url=
set media_start_time=
set media_stop_time=
