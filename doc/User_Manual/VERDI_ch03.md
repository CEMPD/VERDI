<!-- BEGIN COMMENT -->

[<< Previous Chapter](VERDI_ch02.md) - [Home](README.md) - [Next Chapter >>](VERDI_ch04.md)<br>

<!-- END COMMENT -->

VERDI Installation Instructions
===============================

<span id="_Toc197166117" class="anchor"><span id="_Toc292295001" class="anchor"></span></span>This chapter provides instructions for installing VERDI 2.0 on a variety of computer platforms. The supporting libraries required by VERDI are included in the installation, along with a version of the JRE 7 for your convenience. If you already have JRE 7 installed on your computer, you will not need to uninstall it and you can choose to use that one.

VERDI 2.0 Beta is distributed as a zip or gzip file, as appropriate, for each of the following supported platforms:

-   64-bit Windows 8

-   64-bit Linux

-   Mac

If you have a different computer system, select the distribution for a computer system as close to yours as possible and proceed with the installation. Although Java is considered a write-once, run-anywhere computer language, that is not necessarily true for graphical software. Therefore, an appropriate version of some graphics libraries is included in each of the above VERDI distributions.

Installation Instructions for Linux and Mac
-------------------------------------------

Follow these instructions to install VERDI:

1.  tar -xvf verdi_2.0_beta.tar.gz into a location where you would like to install VERDI

2.  Edit verdi_2.0_beta/verdi.sh: Change the path for the DIR variable to reflect the location where VERDI was installed (e.g., DIR=/proj/ie/apps/longleaf/VERDI_2.0_jun_23/VERDI_2.0_beta)

3.  Create a directory *verdi* under your home directory.

4.  Create an empty text file, name it *verdi.alias* and save it in your *verdi* directory. When you look at the directory listing for this *verdi* directory, you should see the *verdi.alias* file with a length of 0.

5.  Locate the file *config.properties.TEMPLATE* that is in your installation directory. Copy *config.properties.TEMPLATE* to your *verdi* directory and rename that file *config.properties* only.

    VERDI should now run if you execute the verdi.sh executable script (e.g., ./verdi.sh).

Please continue with [verdi_preferences](#verdi_preferences).

Installation Instructions for Windows
--------------------------------------

To install VERDI for Windows, unzip the file to a local directory on your Windows 7 computer. NOTE: You do not need to install VERDI under a Program Files directory or in the root directory on one of your hard disk drives. Therefore, you should not need Administrator rights to install VERDI 2.0 beta. If your system is under strict control from your Administrator, you may be able to unzip the VERDI distribution under your home directory or your documents directory; however, you may have problems if there is a space in the path to your VERDI installation directory.

If you are unable to install VERDI on your computer, please check to see whether your user account is authorized to install software. You may need to request that a user with a computer administrator account install VERDI, or provide you with an account that has permission to install software. For more information about user account types, click Start and select Control Panel and then click on the User Account icon.

After successfully installing VERDI you need to perform the following tasks under your home directory.

1.  Locate your home directory. Your home directory is typically under ```csh C: //Users//yourloginid ```. So, if your login id is *staff*, your home directory is probably *C: //Users//staff*.

2.  Create a new directory *verdi* under your home directory (e.g., *C: //Users//staff//verdi*).

3.  Create an empty text file, name it *verdi.alias* and save it in your *verdi* directory. When you look at the directory listing for this *verdi* directory, you should see the *verdi.alias* file with a length of 0.

4.  A text file named *config.properties.TEMPLATE* was installed into your VERDI installation directory. Copy *config.properties.TEMPLATE* to your *verdi* directory and rename that file *config.properties* only.

Note that VERDI writes a log file (i.e., *verdi.log*) as-needed to your *verdi* directory. This log file should remain small. However, if you need technical support we may ask for your log file. It will be a text file named verdi.log located in this verdi directory.

Please continue with [verdi_preferences](#verdi_preferences).

Installation Instructions for computer that that requires a JRE<sup>TM</sup> 7 other than what was provided in the distribution
-------------------------------------------------------------------------------------------------------------------------------

1.  Download Java SE 7 or 8 for your platform from <http://www.java.com/en/download/manual.jsp>

2.  Follow the installation instructions.

<a id="verdi_preferences"></a>
Setting VERDI Preferences
-------------------------

VERDI is configured via the config.properties file that you copied to your home/verdi directory. Edit this file to specify default directories for saving files, for placing the location of configuration files, and for saving project files. Contents of config.properties.TEMPLATE:

```csh
# This file should be put in $USER_HOME/verdi/ subdirectory
# Please use double backslash for Windows platform or slash for UNIX-like platforms
# Please uncomment the following lines and modify them to suit your local settings
# Windows example settings format
# verdi.project.home=C:// Users\\yourusername\\VERDI_2.0_beta\\project
# verdi.config.home=C:// Users\\yourusername\\VERDI_2.0_beta\\config
# Linux example settings format

verdi.project.home=../../data/project
verdi.config.home=../../data/configs
verdi.user.home=../../data/model
verdi.dataset.home=../../data/model
verdi.script.home=../../data/scripts

# file folder used as default location of HUC datasets for areal interpolation

verdi.hucData=../../data/hucRegion/

# For VERDI to access remote big netCDF data files

verdi.remote.hosts=terrae.nesc.epa.gov,vortex.rtpnc.epa.gov,garnet01.rtpnc.epa.gov,tulip.rtpnc.epa.gov
remote.file.util=/usr/local/bin/RemoteFileUtility
verdi.remote.ssh=/usr/bin/ssh

# on local machine where VERDI is running. Used to hold temporary data file downloaded from a remote machine

verdi.temporary.dir=C:\\ Users\username\temp
```

The items in the config.properties.TEMPLATE file that is installed with VERDI are commented out. To specify default directories, uncommented these lines by removing the starting ‘#’ sign. Example settings that are provided in the default file show how to specify the paths to these locations, depending on whether the installation is for a Windows or Linux platform. Here are how the settings are used by VERDI. Note that VERDI stores the most recently used directory for each of these functions and will go to that directory when you repeat the load or save in the same session.

-   verdi.project.home: Default location from which to load and save projects
-   verdi.config.home: Default location from which to load and save plot configuration files
-   verdi.dataset.home: Default location from which to load datasets
-   verdi.script.home: Default location from which to load and save batch scripts
-   verdi.hucData: Default location where area shapefiles are located; VERDI navigates to this directory when the user selects to add a dataset in the Area pane.
-   verdi.remote.hosts: Contains a list of machines that the user can select to browse when adding a remote dataset using VERDI’s Remote File Access capability
-   verdi.remote.util: Location of the RemoteFileUtility script for Linux and Mac installations of VERDI.

Starting in VERDI version 1.4, the ui.properties file was removed and the user-configurable settings, such as the default directory locations, were moved to the config.properties file.

<!-- BEGIN COMMENT -->

[<< Previous Chapter](VERDI_ch02.md) - [Home](README.md) - [Next Chapter >>](VERDI_ch04.md)<br>
VERDI User Manual (c) 2018<br>

<!-- END COMMENT -->
