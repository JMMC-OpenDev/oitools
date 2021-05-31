<!--
 Copyright (c) 2021 Ferreol Soulez

 This software is released under the MIT License.
 https://opensource.org/licenses/MIT
-->

OIFits validation rules
================

* [OIFITS V1](https://jmmc-opendev.github.io/OITools/rules/DataModelV1_output.html)
* [OIFITS V2](https://jmmc-opendev.github.io/OITools/rules/DataModelV2_output.html)

Code conventions
================

Use Netbeans's java formatting settings with the file header from licenseheader.txt (GPL)

Development wiki
================

<http://www.jmmc.fr/twiki/bin/view/Jmmc/Software/JmmcOiTools>

Building instruction
====

```bash
cd parent-pom
mvn -Dassembly.skipAssembly -Djarsigner.skip=true clean install
cd ..
mvn process-resources
mvn -Djarsigner.skip=true clean install
```

[Javadoc](https://ferreolS.github.io/OITools/index.html)
====

Shell scripts
=============

Several shell scripts are used to handle the reference & test files (regression) & update the documentation (rules):

| Script | Description |
| --- | --- |
| `./mergeOITools.sh` | Git commands to update your forked repository with the JMMC-OpenDev/OITools master |
| `./update.sh` | Compare test changes & Update reference files |
| `./copyTestOIFits.sh` | Update the created OIFits REFERENCE files (from TEST files) |
| `./copyTestXmlCsv.sh` | Update the REFERENCE xml & csv files (from TEST files) |
| `./copyTestProperties.sh` | Sort TEST property files + Update the REFERENCE property files (from TEST files) |
| `./sortTestProperties.sh` | Clean & Sort (alphabetically) the TEST property files (used by copyTestProperties.sh) |
| `./cleanTestProperties.sh` | Clean the TEST property files i.e. remove commented lines in the file header (# ...) to ignore such differences (used by sortTestProperties.sh) |
| `./sortRefProperties.sh` | Sort (alphabetically) the REFERENCE property files |
| `./rules/toHtml.sh` | Convert DataModelV?.xml to html pages |

*Update procedure*:

```bash
mvn clean install
update.sh # (check diff) use Ctrl+C to interrupt the shell script before overriding reference files
mvn test # (again, to ascertain reference files are up-to-date)
```
