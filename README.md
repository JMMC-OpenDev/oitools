# OITools

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](LICENSE)  <img src="https://travis-ci.org/JMMC-OpenDev/OITools.svg" alt="build status"/>

This is a java library dedicated to reading / writing OIFITS files (based on the nom.tam.fits library) developped by the JMMC technical team.

See:
<http://www.jmmc.fr/twiki/bin/view/Jmmc/Software/JmmcOiTools>

OIFits validation rules:

* [OIFITS V1](http://htmlpreview.github.io/?https://github.com/JMMC-OpenDev/OITools/blob/master/rules/DataModelV1_output.html)
* [OIFITS V2](http://htmlpreview.github.io/?https://github.com/JMMC-OpenDev/OITools/blob/master/rules/DataModelV2_output.html)

## Setup

1. _Download_ the last jar (`oitools-TRUNK.jar`) from release place <https://github.com/JMMC-OpenDev/OITools/releases/>

1. _Call_:
    * type in shell:

        ```bash
        java -cp _path_/_to_/oitools-TRUNK.jar fr.jmmc.oitools.OIFitsProcessor <parameters>
        ```

    * HINT: you can define aliases as of:

        ```bash
        alias oip="java -cp _path_/_to_/oitools-TRUNK.jar fr.jmmc.oitools.OIFitsProcessor"
        alias oilist="oip list"
        alias oiconvert="oip convert"
        alias oimerge="oip merge"
        ```

## Usage

## [Developers corner](doc/DevelopersCorner.md)
