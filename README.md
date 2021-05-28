# OITools    ![JMMC logo](doc/JMMC-logo.jpg)

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](LICENSE)  ![build](https://travis-ci.org/JMMC-OpenDev/OITools.svg)

This is a java library dedicated to reading / writing OIFITS files (based on the nom.tam.fits library) developed by the JMMC technical team.

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

Examples
========
All examples are made using data selected from the [test folder](src/test/resources/oifits/)

* `oilist input_file_location [input_file_location …]`
display the file content of input file(s) in CSV format files contents  (granules like OiDB):

| target name | target right ascension | target declination | exposure time | t_min (mjd) | t_max (mjd)| spectral resolution  | shorter wavelength | larger wavelength | facility name  | instrument name | number of visibilities | number of squared visibilities | number of bispectra  | number of spectral channels  |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |

```bash
oilist GRAVI.2016-06-23T03:10:17.458_singlesciviscalibrated.fits
target_name     s_ra    s_dec   t_exptime       t_min   t_max   em_res_power    em_min  em_max  facility_name   instrument_name nb_vis  nb_vis2 nb_t3   nb_channels
IRAS17216-3801  261.2771541666667       -38.066788888888894     36.54999999999172       57562.13387079336       57562.134832901625      25.75830610631207       2.0264997147023678E-6   2.347295094295987E-6       VLTI    GRAVITY_FT      6       6       4       5
IRAS17216-3801  261.2771541666667       -38.066788888888894     0.0     57562.1339081338        57562.135323171286      1008.6521574069673      1.990000100704492E-6    2.4500000108673703E-6   VLTI    GRAVITY_SC 6       6       4       210
```

* `oip list_baselines`

* `oip dump` print headers of all HDUs.

<!-- * `oiconvert -o <output_file_path> input_file_location` load the input file in memory and write directly to the output file. By this way some cleanup (keywords, columns) is done.
 -->
* `oimerge -o <output_file_path> input_file_location [input_file_location …]` merge multiple files with filter support, targets are gathered and merged into a single OI_TARGET table.

    Available filters are (multiple arguments must be comma separated):
  * `-target`
  * `-insname`
  * `-night`
  * `-mjds`
  * `-baselines`
  * `-wavelength`

  Targets are gathered according to their name and positions, the separation for the target matcher can be tune by  the keyword `-separation <value>`  (default: `1 arcsec` ).

## [Developers corner](doc/DevelopersCorner.md)
