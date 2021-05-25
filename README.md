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

    * HINT: you can define aliases in your (bashrc) as of:

        ```bash
        alias oip="java -cp _path_/_to_/oitools-TRUNK.jar fr.jmmc.oitools.OIFitsProcessor"
        alias oilist="oip list"
        alias oiconvert="oip convert"
        alias oimerge="oip merge"
        ```

## Usage

To get the command help, just type `oip`:

```
--------------------------------------------------------------------------------------
Usage: fr.jmmc.oitools.OIFitsProcessor command -o <path_output_file> <file locations>
------------- Arguments help ---------------------------------------------------------
| Key          Value           Description                                           |
|------------------------------------------------------------------------------------|
| command      help           Show this help                                         |
| command      list           List content of several oifits files                   |
| command      list_baselines List baselines and triplets used by several oifits files      |
| command      dump           Dump the given oifits files                            |
| command      convert        Convert the given input file                           |
| command      merge          Merge several oifits files                             |
|------------------------------------------------------------------------------------|
| [-l] or [-log]              Enable logging (quiet by default)                      |
| [-c] or [-check]            Check output file before writing                       |
| [-separation] <value>       Separation in arcsec for the target matcher            |
| [-o] or [-output] <file_path> Complete path, absolute or relative, for output file |
| [-target] <value>           Filter result on given Target                          |
| [-insname] <value>          Filter result on given InsName                         |
| [-night] <value>            Filter result on given Night (integer)                 |
| [-baselines] <values>       Filter result on given Baselines or Triplets (comma-separated) |
| [-mjds] <values>            Filter result on given MJD ranges (comma-separated pairs) |
| [-wavelengths] <values>     Filter result on given wavelength ranges (comma-separated pairs) |
--------------------------------------------------------------------------------------
```

* `oilist input_file_location [input_file_location …]`
display the file content of input file(s) in CSV format files contents  (granules like OiDB):

| target name | target right ascension | target declination | exposure time | t_min (mjd) | t_max (mjd)| spectral resolution  | shorter wavelength | larger wavelength | facility name  | instrument name | number of visibilities | number of squared visibilities | number of bispectra  | number of spectral channels  |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |

* `oip list_baselines`

* `oip dump` print headers of all HDUs.

* `oiconvert -o <output_file_path> input_file_location` load the input file in memory and write directly to the output file. By this way some cleanup (keywords, columns) is done.

* `oimerge -o <output_file_path> input_file_location [input_file_location …]` merge multiple files with filter support, targets are gathered and merged into a single OI_TARGET table.

    Available filters are:
  * `-target`
  * `-insname`
  * `-night`
  * `-mjds`
  * `-baselines`
  * `-wavelength`

  Targets are gathered according to their name and positions, the separation for the target matcher can be tune by  the `-separation <value>` keyword (default: 10? ).

## [Developers corner](doc/DevelopersCorner.md)
