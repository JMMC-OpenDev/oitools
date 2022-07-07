#!/bin/bash
#
# Convert DataModelV?.xml to html pages (needs xsltproc)
#

xsltproc datamodel2html.xsl DataModelV1.xml > DataModelV1_output.html
xsltproc datamodel2html.xsl DataModelV1-all.xml > DataModelV1-all_output.html

xsltproc datamodel2html.xsl DataModelV2.xml > DataModelV2_output.html
xsltproc datamodel2html.xsl DataModelV2-all.xml > DataModelV2-all_output.html

