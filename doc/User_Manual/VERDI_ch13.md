<!-- BEGIN COMMENT -->
  
[<< Previous Chapter](VERDI_ch12.md) - [Home](README.md) - [Next Chapter >>](VERDI_ch14.md)

<!-- END COMMENT -->

Models-3 I/O API Utilities, Data Conversion Programs, and Libraries
============================================

As discussed in Section 6.1, routines are available to convert gridded input data to Models-3 I/O API format or new code can be written and contributed to VERDI for use by the community. The Models-3 I/O API routines that have been written to convert data into this format are discussed in this section. If you are unable to use the available routines to convert your data and have a gridded dataset that VERDI is unable to read, please contact VERDI support via the CMAS User Forum <https://forum.cmascenter.org/c/verdi/20> with a description of the dataset.

The Models-3 I/O API Interface contains an extensive set of utility routines. There are example conversion programs to convert data from different data formats into the Models-3 I/O API format. The Models-3 I/O API Utilities are command line programs that are easy to script for automating analysis and post processing. An example of a Models-3 I/O API Utility that may be useful to VERDI users is m3merge. This utility merges selected variables from a set of input files for a specified time period, and writes them to a single output file, with optional variable-renaming in the process. Another utility that you may find useful is m3xtract. This program allows you to extract a few species from a large file and save them to a smaller file on your local computer so you can explore them using VERDI. The Models-3 I/O API Related Programs and Examples can be found at the following web site: https://www.cmascenter.org/ioapi/documentation/all_versions/html/.

Airs2m3 is an example of a data conversion program that converts the standard AIRS AMP350 observational data format to the Models-3 I/O API format. The airs2m3 program requires the following inputs:

-   The input AIRS AMP350 print format file name.

-   The time zone conversion file (provided with the obs2api program - tzt.dat).

-   Additional hour shift variable. The AIRS data are hourly averaged, and a 00 time flag represents the hour 00-01. You may wish to represent that data segment by the ending hour. In that case, a 1 should be entered here.

-   Starting year, month, day, hour (GMT) (e.g., 1997 07 10 12).

-   Ending year, month, day, hour (GMT) (e.g., 1997 07 16 12).

-   Name of output variable (8 characters max) (e.g., O3_OBS).

<!-- BEGIN COMMENT -->

[<< Previous Chapter](VERDI_ch12.md) - [Home](README.md) - [Next Chapter >>](VERDI_ch14.md)<br>
VERDI User Manual (c) 2025<br>

<!-- END COMMENT -->
