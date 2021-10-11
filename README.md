# OITools    ![JMMC logo](doc/JMMC-logo.jpg)

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](LICENSE)  ![build](https://travis-ci.org/JMMC-OpenDev/OITools.svg)

This is a java library dedicated to reading / writing OIFITS files (based on the nom.tam.fits library) developed by the JMMC technical team.

See [Developers corner](doc/DevelopersCorner.md)


## OIFits standard versions:
* [OIFITS V1](https://jmmc-opendev.github.io/oitools/rules/DataModelV1_output.html)
* [OIFITS V2](https://jmmc-opendev.github.io/oitools/rules/DataModelV2_output.html)


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


## Usage & Examples

Examples are taken from the [test folder](src/test/resources/oifits/)

To get the command help, just type `oip`:

```text
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

<!-- | target name | target right ascension | target declination | exposure time | t_min (mjd) | t_max (mjd)| spectral resolution  | shorter wavelength | larger wavelength | facility name  | instrument name | number of visibilities | number of squared visibilities | number of bispectra  | number of spectral channels  |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
 -->

*Example where two instruments are detected: the fringe tracker `GRAVITY_FT` with 5 spectral channels and the science detector `GRAVITY_SC` with 210 spectral channels*

```bash
oilist GRAVI.2016-06-23T03:10:17.458_singlesciviscalibrated.fits
target_name     s_ra    s_dec   t_exptime       t_min   t_max   em_res_power    em_min  em_max  facility_name   instrument_name nb_vis  nb_vis2 nb_t3   nb_channels
IRAS17216-3801  261.2771541666667       -38.066788888888894     36.54999999999172       57562.13387079336       57562.134832901625      25.75830610631207       2.0264997147023678E-6   2.347295094295987E-6       VLTI    GRAVITY_FT      6       6       4       5
IRAS17216-3801  261.2771541666667       -38.066788888888894     0.0     57562.1339081338        57562.135323171286      1008.6521574069673      1.990000100704492E-6    2.4500000108673703E-6   VLTI    GRAVITY_SC 6       6       4       210
```

&#9888; Warning: Targets are gathered according to their name and positions. Due to some uncertainties from files to files, the same target maybe duplicated. In this example, the target HD45677 is miss-identified  as 3 different targets with varying right ascension.

```bash
 % oilist *.fits
target_name	s_ra	s_dec	t_exptime	t_min	t_max	em_res_power	em_min	em_max	facility_name	instrument_name	nb_vis	nb_vis2	nb_t3	nb_channels
HD45677	97.071446	-13.05308	0.07500000298023224	9743.3964126119	9743.39698346838	47.39237355339858	2.841224613803206E-6	4.209076450933935E-6	VLTI	MATISSE	0	12	8	64
HD45677	97.071446	-13.05308	0.07500000298023224	29230.186422152714	29230.186875347168	47.39237355339858	2.841224613803206E-6	4.209076450933935E-6	VLTI	MATISSE	0	12	8	64
HD45677	97.071446	-13.05308	0.07500000298023224	58454.27455231501	58454.35846547426	47.39237355339858	2.841224613803206E-6	4.209076450933935E-6	VLTI	MATISSE	72	72	48	64
HD45677	97.071446	-13.05308	0.07500000298023224	58455.17619694426	58455.2241775925	47.39237355339858	2.841224613803206E-6	4.209076450933935E-6	VLTI	MATISSE	36	36	24	64
HD45677	97.071446	-13.05308	0.07500000298023224	58458.17127300924	58458.3521850002	47.39237355339858	2.841224613803206E-6	4.209076450933935E-6	VLTI	MATISSE	126	186	124	64
HD45677	97.071446	-13.05308	0.07500000298023224	58459.1170581483	58459.32373210101	47.39237355339858	2.841224613803206E-6	4.209076450933935E-6	VLTI	MATISSE	108	114	76	64
HD45677	97.071446	-13.05308	0.07500000298023224	58460.19688902766	58460.37100263876	47.39237355339858	2.841224613803206E-6	4.209076450933935E-6	VLTI	MATISSE	48	72	48	64
HD45677_1	97.072017	-13.05308	0.07500000298023224	11692.253139791668	11692.271517135014	47.39237355339858	2.841224613803206E-6	4.209076450933935E-6	VLTI	MATISSE	0	36	24	64
HD45677_1	97.072017	-13.05308	0.07500000298023224	58461.259056527546	58461.35039300915	47.39237355339858	2.841224613803206E-6	4.209076450933935E-6	VLTI	MATISSE	36	36	24	64
HD45677_1	97.072017	-13.05308	0.07500000298023224	58464.21925374992	58464.296853532614	47.39237355339858	2.841224613803206E-6	4.209076450933935E-6	VLTI	MATISSE	48	48	32	64
HD45677_1	97.072017	-13.05308	0.07500000298023224	58465.21247333342	58465.35976816349	47.39237355339858	2.841224613803206E-6	4.209076450933935E-6	VLTI	MATISSE	90	96	64	64
HD45677_1	97.072017	-13.05308	0.07500000298023224	58466.225046249856	58466.2779251908	47.39237355339858	2.841224613803206E-6	4.209076450933935E-6	VLTI	MATISSE	48	48	32	64
HD45677_2	97.072587	-13.05308	0.07500000298023224	58462.2034310185	58462.37512025083	47.39237355339858	2.841224613803206E-6	4.209076450933935E-6	VLTI	MATISSE	114	120	80	64
HD45677_2	97.072587	-13.05308	0.07500000298023224	58463.31136550928	58463.36339001964	47.39237355339858	2.841224613803206E-6	4.209076450933935E-6	VLTI	MATISSE	30	48	32	64
````

This can be fixed by ajusting the separation of the target matcher using   the keyword `-separation <value>`  (default: `1 arcsec` ):

```bash
% oilist -separation 10  *.fits
target_name	s_ra	s_dec	t_exptime	t_min	t_max	em_res_power	em_min	em_max	facility_name	instrument_name	nb_vis	nb_vis2	nb_t3	nb_channels
HD45677	97.071446	-13.05308	0.07500000298023224	9743.3964126119	9743.39698346838	47.39237355339858	2.841224613803206E-6	4.209076450933935E-6	VLTI	MATISSE	0	12	8	64
HD45677	97.071446	-13.05308	0.07500000298023224	11692.253139791668	11692.271517135014	47.39237355339858	2.841224613803206E-6	4.209076450933935E-6	VLTI	MATISSE	0	36	24	64
HD45677	97.071446	-13.05308	0.07500000298023224	29230.186422152714	29230.186875347168	47.39237355339858	2.841224613803206E-6	4.209076450933935E-6	VLTI	MATISSE	0	12	8	64
HD45677	97.071446	-13.05308	0.07500000298023224	58454.27455231501	58454.35846547426	47.39237355339858	2.841224613803206E-6	4.209076450933935E-6	VLTI	MATISSE	72	72	48	64
HD45677	97.071446	-13.05308	0.07500000298023224	58455.17619694426	58455.2241775925	47.39237355339858	2.841224613803206E-6	4.209076450933935E-6	VLTI	MATISSE	36	36	24	64
HD45677	97.071446	-13.05308	0.07500000298023224	58458.17127300924	58458.3521850002	47.39237355339858	2.841224613803206E-6	4.209076450933935E-6	VLTI	MATISSE	126	186	124	64
HD45677	97.071446	-13.05308	0.07500000298023224	58459.1170581483	58459.32373210101	47.39237355339858	2.841224613803206E-6	4.209076450933935E-6	VLTI	MATISSE	108	114	76	64
HD45677	97.071446	-13.05308	0.07500000298023224	58460.19688902766	58460.37100263876	47.39237355339858	2.841224613803206E-6	4.209076450933935E-6	VLTI	MATISSE	48	72	48	64
HD45677	97.071446	-13.05308	0.07500000298023224	58461.259056527546	58461.35039300915	47.39237355339858	2.841224613803206E-6	4.209076450933935E-6	VLTI	MATISSE	36	36	24	64
HD45677	97.071446	-13.05308	0.07500000298023224	58462.2034310185	58462.37512025083	47.39237355339858	2.841224613803206E-6	4.209076450933935E-6	VLTI	MATISSE	114	120	80	64
HD45677	97.071446	-13.05308	0.07500000298023224	58463.31136550928	58463.36339001964	47.39237355339858	2.841224613803206E-6	4.209076450933935E-6	VLTI	MATISSE	30	48	32	64
HD45677	97.071446	-13.05308	0.07500000298023224	58464.21925374992	58464.296853532614	47.39237355339858	2.841224613803206E-6	4.209076450933935E-6	VLTI	MATISSE	48	48	32	64
HD45677	97.071446	-13.05308	0.07500000298023224	58465.21247333342	58465.35976816349	47.39237355339858	2.841224613803206E-6	4.209076450933935E-6	VLTI	MATISSE	90	96	64	64
HD45677	97.071446	-13.05308	0.07500000298023224	58466.225046249856	58466.2779251908	47.39237355339858	2.841224613803206E-6	4.209076450933935E-6	VLTI	MATISSE	48	48	32	64
```


* `oip list_baselines` display similar information as `oilist` plus the station/baselines used.

*Example showing that the stations used are: `A0` `B2` `C1` `D0` and the file contains all combinaison of baselines and triplets:*

```bash
oip list_baselines GRAVI.2016-06-23T03:10:17.458_singlesciviscalibrated.fits
instrument_name	em_min	em_max	night_id	target_name	s_ra	s_dec	mjds	baselines
GRAVITY_FT	2.0264997147023678E-6	2.347295094295987E-6	57562	IRAS17216-3801	261.2771541666667	-38.066788888888894	[57562.133866,57562.136111] [57562.133947,57562.136748] [57562.13412,57562.13522] [57562.13412,57562.135231] [57562.13412,57562.135243] [57562.134745,57562.135336] [57562.13478,57562.135243] [57562.13478,57562.135498] [57562.134815,57562.135266] [57562.134826,57562.135266] 	A0 B2 C1 D0 B2-A0 C1-A0 C1-B2 C1-D0 D0-A0 D0-B2 C1-B2-A0 C1-D0-A0 C1-D0-B2 D0-B2-A0
GRAVITY_SC	1.990000100704492E-6	2.4500000108673703E-6	57562	IRAS17216-3801	261.2771541666667	-38.066788888888894	[57562.13390,57562.136343] [57562.13397,57562.133981] [57562.13397,57562.137454] [57562.135312,57562.13544] 	A0 B2 C1 D0 B2-A0 C1-A0 C1-B2 C1-D0 D0-A0 D0-B2 C1-B2-A0 C1-D0-A0 C1-D0-B2 D0-B2-A0
```


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


* Extracting flux from Gravity science instrument at wavelengths between 2.15 and 2.2  &#956;m*:

```bash
% oimerge -insname GRAVITY_SC  -baselines A0,B2,C1,D0 -wavelengths 2.15E-6,2.20E-6 -o output.fits  GRAVI.2016-06-23T03:10:17.458_singlesciviscalibrated.fits
Writing: output.fits
% oilist output.fits
target_name	s_ra	s_dec	t_exptime	t_min	t_max	em_res_power	em_min	em_max	facility_name	instrument_name	nb_vis	nb_vis2	nb_t3	nb_channels
IRAS17216-3801	261.2771541666667	-38.066788888888894	300.0	57562.133972453696	57562.133972453696	988.1521592210469	2.1506698431039695E-6	2.1990908862790093E-6	VLTI	GRAVITY_SC	0	0	0	23
```
