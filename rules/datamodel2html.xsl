<?xml version="1.0" encoding="utf-8"?>
<!--
Copyright (C) 2018 CNRS - JMMC project ( http://www.jmmc.fr )

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
-->

<xsl:stylesheet version="1.0"
                xmlns="http://www.w3.org/1999/xhtml"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:exsl="http://exslt.org/common"
                xmlns:dyn="http://exslt.org/dynamic"
                extension-element-prefixes="exsl dyn xsi"
                xsi:schemaLocation="http://www.w3.org/1999/xhtml http://www.w3.org/MarkUp/SCHEMA/xhtml11.xsd">

    <xsl:output method="html" indent="yes" omit-xml-declaration="no" media-type="text/html" encoding="utf-8"
                doctype-public="-//W3C//DTD XHTML 1.1//EN"
                doctype-system="http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd"/>

    <!-- base template -->
    <xsl:template match="/">
        <xsl:variable name="isV2" select="count(datamodel/table[@name = 'OI_FLUX']) = 1"/>

        <html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
            <head>
                <meta charset="utf-8"/>
                <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
                <meta name="viewport" content="width=device-width, initial-scale=1"/>
                <!-- The above 3 meta tags *must* come first in the head; any other head content must come *after* these tags -->
                <meta http-equiv="Content-Type" content="text/html;charset=utf-8"/>

                <title>OI-FITS Data Model &amp; Rules</title>

                <!-- Latest compiled and minified CSS -->
                <link href="bootstrap-3.3.7-dist/css/bootstrap.min.css" rel="stylesheet"/>
                <!-- Custom styles for this template -->
                <link href="DataModel.css" rel="stylesheet"/>

                <!-- HTML5 shim and Respond.js for IE8 support of HTML5 elements and media queries -->
                <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
                <xsl:text disable-output-escaping="yes">
                <![CDATA[
                <!-- HTML5 shim and Respond.js for IE8 support of HTML5 elements and media queries -->
                <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
                <!--[if lt IE 9]>
                <script src="https://oss.maxcdn.com/html5shiv/3.7.2/html5shiv.min.js"></script>
                <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
                <![endif]-->
                ]]></xsl:text>
            </head>
            <body data-spy="scroll" data-target="#navbar">
                <nav class="navbar navbar-inverse navbar-fixed-top">
                    <div class="container">
                        <div class="navbar-header">
                            <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar" aria-expanded="false" aria-controls="navbar">
                                <span class="sr-only">Toggle navigation</span>
                                <span class="icon-bar">-</span>
                                <span class="icon-bar">-</span>
                                <span class="icon-bar">-</span>
                            </button>
                            <a class="navbar-brand" href="http://www.jmmc.fr" target="_blank">JMMC OITools</a>
                        </div>
                        <div id="navbar" class="collapse navbar-collapse">
                            <ul class="nav navbar-nav">
                                <li>
                                    <a href="#Rules">OIFITS Rules</a>
                                </li>
                                <li class="dropdown">
                                    <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false">OIFITS Data Model<span class="caret"></span></a>
                                    <ul class="dropdown-menu">
                                        <xsl:if test="$isV2">
                                            <li>
                                                <a href="#TABLE_MAIN_HEADER">MAIN HEADER</a>
                                            </li>
                                            <li role="separator" class="divider"></li>
                                        </xsl:if>
                                        <li>
                                            <a href="#TABLE_OI_TARGET">OI_TARGET</a>
                                        </li>
                                        <li>
                                            <a href="#TABLE_OI_ARRAY">OI_ARRAY</a>
                                        </li>
                                        <li>
                                            <a href="#TABLE_OI_WAVELENGTH">OI_WAVELENGTH</a>
                                        </li>
                                        <xsl:if test="$isV2">
                                            <li>
                                                <a href="#TABLE_OI_CORR">OI_CORR</a>
                                            </li>
                                            <li>
                                                <a href="#TABLE_OI_INSPOL">OI_INSPOL</a>
                                            </li>
                                        </xsl:if>
                                        <li role="separator" class="divider"></li>
                                        <li>
                                            <a href="#TABLE_OI_VIS">OI_VIS</a>
                                        </li>
                                        <li>
                                            <a href="#TABLE_OI_VIS2">OI_VIS2</a>
                                        </li>
                                        <li>
                                            <a href="#TABLE_OI_T3">OI_T3</a>
                                        </li>
                                        <xsl:if test="$isV2">
                                            <li>
                                                <a href="#TABLE_OI_FLUX">OI_FLUX</a>
                                            </li>
                                        </xsl:if>
                                    </ul>
                                </li>
                                <li>
                                    <a href="http://www.jmmc.fr/twiki/pub/Jmmc/OIFITSTwoProject/oifits_norm_v2.pdf" target="_blank">OIFITS 2 standard</a>
                                </li>
                            </ul>
                        </div>
                    </div>
                </nav>

                <div class="container mycontainer" id="Rules">
                    <h1>OIFITS Rules</h1>
                    <button type="button" class="btn btn-info" onclick="toggleApplyTo()">Show / Hide 'Apply To'</button>
                    <xsl:apply-templates select="datamodel/rules"/>
                </div>
                <div class="container mycontainer" id="DataModel">
                    <h1>OIFITS DataModel</h1>
                    <xsl:apply-templates select="datamodel/table"/>
                </div>

                <script src="js/jquery-1.12.4.min.js"></script>
                <script src="bootstrap-3.3.7-dist/js/bootstrap.min.js"></script>
                <script>
                    function toggleApplyTo() {
                    $('.myhide').toggle();
                    }
                </script>

            </body>
        </html>
    </xsl:template>



    <xsl:template match="rules">
        <div class="panel panel-default">
            <div class="panel-heading">
                <h3 class="panel-title">Rules</h3>
            </div>
            <table class="table table-condensed table-striped">
                <thead>
                    <tr>
                        <th>Name</th>
                        <th>Description</th>
                        <th>Paragraph</th>
                        <th>Standard</th>
                    </tr>
                </thead>
                <tbody>
                    <xsl:apply-templates select="rule"/>
                </tbody>
            </table>
        </div>
    </xsl:template>

    <xsl:template match="rule">
        <tr id="RULE_{name}">
            <td>
                <b>
                    <xsl:value-of select="name" />
                </b>
            </td>
            <td>
                <xsl:value-of select="description" />
            </td>
            <td>
                <xsl:value-of select="paragraph" />
            </td>
            <td>
                <xsl:if test="standards">
                    <ul>
                        <xsl:for-each select="standards/standard">
                            <li>
                                <xsl:value-of select="text()" />
                            </li>
                        </xsl:for-each>
                    </ul>
                </xsl:if>
            </td>
        </tr>
        <tr>
            <td colspan="4" class="noborder myhide">
                <xsl:variable name="applylist">
                    <xsl:copy-of select="subjects/apply" />
                </xsl:variable>
                <xsl:variable name="alltableprefixes">
                    <xsl:for-each select="subjects/apply">
                        <xsl:choose>
                            <xsl:when test="contains(., '.')">
                                <e>
                                    <xsl:value-of select="substring-before(.,'.')" />
                                </e>
                            </xsl:when>
                            <xsl:otherwise>
                                <e>
                                    <xsl:value-of select="." />
                                </e>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:for-each>
                </xsl:variable>
                <xsl:variable name="distincttableprefixes">
                    <xsl:for-each select="exsl:node-set($alltableprefixes)/*[not(preceding-sibling::*=.)]">
                        <xsl:copy-of select="." />
                    </xsl:for-each>
                </xsl:variable>
                <div class="container-fluid applyTo">
                    <div class="panel-heading">
                        <h4 class="panel-title">Apply To</h4>
                    </div>
                    <div class="row flex-row">
                        <xsl:for-each select="exsl:node-set($distincttableprefixes)/*">
                            <div class="panel panel-default oitable col-xs-12 col-ms-4 col-md-3 col-lg-2">
                                <b>
                                    <xsl:value-of select="." />
                                </b>
                                <xsl:variable name="prefix">
                                    <!-- add the dot so vis2 will not matche with vis prefix -->
                                    <xsl:value-of select="." />
                                    <xsl:value-of select="'.'" />
                                </xsl:variable>

                                <xsl:for-each select="exsl:node-set($applylist)/*[starts-with(., $prefix)]">
                                    <br/>
                                    <a href="#TABLE_{text()}">
                                        <xsl:value-of select="text()" />
                                    </a>
                                </xsl:for-each>
                            </div>
                        </xsl:for-each>
                    </div>
                </div>
            </td>
        </tr>
    </xsl:template>


    <xsl:template match="table">
        <div class="container mycontainer" id="TABLE_{@name}">
            <h2>
                <span class="label label-primary">
                    <xsl:if test="starts-with(@name, 'OI_')">Table </xsl:if>
                    <xsl:value-of select="@name" />
                </span>
            </h2>
            <div class="panel panel-default">
                <div class="panel-heading">
                    <h3 class="panel-title">Keywords</h3>
                </div>
                <table class="table table-condensed table-striped">
                    <!-- keywords -->
                    <thead>
                        <tr>
                            <th>Name</th>
                            <th>Data type</th>
                            <th></th>
                            <th>Mandatory</th>
                            <th>(Unit)</th>
                            <th>Description</th>
                            <th>(Values)</th>
                            <th class="myhide">Rules</th>
                        </tr>
                    </thead>
                    <xsl:apply-templates select="keyword">
                        <xsl:with-param name="tablename" select = "./@name" />
                    </xsl:apply-templates>
                </table>
            </div>
            <xsl:if test="column">
                <div class="panel panel-default">
                    <div class="panel-heading">
                        <h3 class="panel-title">Columns</h3>
                    </div>
                    <table class="table table-condensed table-striped">
                        <!-- columns -->
                        <thead>
                            <tr>
                                <th>Name</th>
                                <th>Data type</th>
                                <th>Dims</th>
                                <th>Mandatory</th>
                                <th>(Unit)</th>
                                <th>Description</th>
                                <th>(Values)</th>
                                <th class="myhide">Rules</th>
                            </tr>
                        </thead>
                        <xsl:apply-templates select="column">
                            <xsl:with-param name="tablename" select = "./@name" />
                        </xsl:apply-templates>
                    </table>
                </div>
            </xsl:if>
        </div>
    </xsl:template>


    <xsl:template match="keyword">
        <xsl:param name = "tablename" />
        <tbody>
            <tr id="TABLE_{$tablename}.{name}">
                <td>
                    <b>
                        <xsl:value-of select="name" />
                    </b>
                </td>
                <td>
                    <xsl:value-of select="substring(datatype, 6)" />
                </td>
                <td></td>
                <td>
                    <xsl:choose>
                        <xsl:when test="optional = 'false'">Y</xsl:when>
                        <xsl:otherwise>N</xsl:otherwise>
                    </xsl:choose>
                </td>
                <td>
                    <xsl:value-of select="substring(unit, 6)" />
                </td>
                <td>
                    <xsl:value-of select="description" />
                </td>
                <td>
                    <xsl:if test="values">
                        <ul>
                            <xsl:for-each select="values/string">
                                <li>
                                    <xsl:value-of select="text()" />
                                </li>
                            </xsl:for-each>
                            <xsl:for-each select="values/short">
                                <li>
                                    <xsl:value-of select="text()" />
                                </li>
                            </xsl:for-each>
                        </ul>
                    </xsl:if>
                </td>
                <td class="myhide">
                    <xsl:if test="rules">
                        <ul>
                            <xsl:for-each select="rules/applyrule">
                                <li>
                                    <a href="#RULE_{text()}">
                                        <xsl:value-of select="text()" />
                                    </a>
                                </li>
                            </xsl:for-each>
                        </ul>
                    </xsl:if>
                </td>
            </tr>
        </tbody>
    </xsl:template>


    <xsl:template match="column">
        <xsl:param name = "tablename" />
        <tbody>
            <tr id="TABLE_{$tablename}.{name}">
                <td>
                    <b>
                        <xsl:value-of select="name" />
                    </b>
                </td>
                <td>
                    <xsl:value-of select="substring(datatype, 6)" />
                </td>
                <td>
                    <xsl:value-of select="repeat" />
                </td>
                <td>
                    <xsl:choose>
                        <xsl:when test="optional = 'false'">Y</xsl:when>
                        <xsl:otherwise>N</xsl:otherwise>
                    </xsl:choose>
                </td>
                <td>
                    <xsl:value-of select="substring(unit, 6)" />
                </td>
                <td>
                    <xsl:value-of select="description" />
                </td>
                <td>
                    <xsl:if test="values">
                        <ul>
                            <xsl:for-each select="values/string">
                                <li>
                                    <xsl:value-of select="text()" />
                                </li>
                            </xsl:for-each>
                            <xsl:for-each select="values/short">
                                <li>
                                    <xsl:value-of select="text()" />
                                </li>
                            </xsl:for-each>
                        </ul>
                    </xsl:if>
                </td>
                <td class="myhide">
                    <xsl:if test="rules">
                        <ul>
                            <xsl:for-each select="rules/applyrule">
                                <li>
                                    <a href="#RULE_{text()}">
                                        <xsl:value-of select="text()" />
                                    </a>
                                </li>
                            </xsl:for-each>
                        </ul>
                    </xsl:if>
                </td>
            </tr>
        </tbody>
    </xsl:template>

</xsl:stylesheet>

