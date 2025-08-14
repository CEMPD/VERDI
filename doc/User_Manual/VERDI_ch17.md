<!-- BEGIN COMMENT -->
  
[<< Previous Chapter](VERDI_ch16.md) - [Home](README.md) - [Next Chapter >>](VERDI_ch18.md)

<!-- END COMMENT -->

 VERDI Batch Script Editor
==========================

To open the Script Editor, use File&gt;View Script Editor ([Figure 89](#Figure89)). Prior running a batch script, remove all datasets from the dataset list. To remove a dataset, click on each dataset in the dataset panel and press the yellow minus button.

Figure 90. File: View Script Editor<br>

![File: View Script Editor](./media/image085.png){#fig:Figure90}


An **Open** popup window will be displayed, click on a sample script file in the VERDI_2.2/data/scripts directory ([Figure 100](#Figure100)).


Figure 86. Open Popup Window<br>

![Open Popup Window](./media/image086.png){#fig:Figure100}

After you select a script file and click Open in the Open popup window, the Script Editor window ([Figure 91](#Figure91)), the Batch Script File format consists of two blocks – a Global block and a Task Block. The Global block allows you to specify a set of parameters (such as the file and directory names) on which all other tasks are performed. In this block you can specify any parameters that are used to run any other tasks. If the same parameters are specified with different values in a subsequent Task block, those values will overwrite the values specified in the Global block. One Global Block specifies the common parameters shared by all Task blocks, and multiple task blocks can be defined to specify the type of batch operations that will be performed (e.g., defining formulas and creating plots).

Unload all datasets before running a batch script within the Script Editor. If any dataset is not unloaded a warning message will pop up see Figure 85. requesting that you close all datasets before running your batch script.

Figure 91. Top of Sample Script File – VERDI_2.2/data/scripts/file_patterns.txt<br>

![Top of Sample Script File of file_patterns.txt](./media/image087.png){#fig:Figure91}

Figure 92 appears in the right-hand side of VERDI. Use the Script Editor to edit, save, and run batch scripts within VERDI. The Batch Scripting Language used for the VERDI Script Editor is described in the header of the sample text format script files.

Figure 92. Bottom of Sample Script File – VERDI_2.2/data/scripts/tile_patterns.txt

![Bottom of Sample Script File: file_patterns.txt](./media/image088.png){#fig:Figure92}

Figure 93. Script Editor Dataset Warning

![Script Editor Dataset Warning](./media/image089.png){#fig:Figure93}


The Batch Script File format consists of two blocks – a Global block and a Task Block. The Global block allows you to specify a set of parameters (such as the file and directory names) on which all other tasks are performed. In this block you can specify any parameters that are used to run any other tasks. If the same parameters are specified with different values in a subsequent Task block, those values will overwrite the values specified in the Global block. One Global Block specifies the common parameters shared by all Task blocks, and multiple task blocks can be defined to specify the type of batch operations that will be performed (e.g., defining formulas and creating plots).

The multifiles.txt sample script that is provided as part of the VERDI release demonstrates how to create a tile plot using a mathematical combination of variables. An excerpt of that script is shown below.

&lt;Task&gt;
dir=D:\\verdi-dist2\\data\\model
f=copy.36k.O3MAX
f=CCTM46_P16.baseO2a.36k.O3MAX
f=another.36k.O3MAX
s=O3[1]-O3[2]+O3[3]*2
gtype=tile
saveImage=jpeg
imageDir=D:\\verdi-dist2
imageFile=three_components_36k.O3MAX
&lt;/Task&gt;

The above task specifies the name of three input files. The input files are assigned a number based on the order in which they are specified.

[1]=Copy.36k.O3MAX

[2]=CCTM46_P16.baseO2a.36k.O3MAX

[3]=another.36k.O3MAX

s=O3[1]-O3[2]+O3[3]*2 defines a formula that uses variables from the three filenames

This formula takes ozone in file 1 and subtracts the ozone in file 2 and adds two times the ozone in file 3.

The type of plot is specified as a tile plot by setting the parameter *gtype* to tile (i.e., gtype=tile).

The image file format is specified by setting the parameter *saveImage* to jpeg (i.e., saveImage=jpeg).

The output directory where the images will be stored is specified by setting the parameter *imageDir* (i.e., imageDir=D:\\verdi-dis2).

The image file name is specified by setting the parameter *imageFile*; imageFile=three_components_36k.O3MAX.

Use the left mouse button to highlight the task that you would like to run and then click **Run** in the Script Editor window. A popup window then appears to indicate the task ran successfully (Figure 94). In this example the title and subtitle were obtained from the definition in the global block. Aspects of the plot defined in the global block are used for multiple tasks and are applied even if only a highlighted task is run.

![Script Editor ](./media/image087.png){#fig:Figure94}

Figure 91. Highlight Text to Select Task and Click Run

If you select Run without highlighting a Text Block, then the entire batch script executes and generates the plots. To edit the batch script, highlight a segment that you would like to copy and use Ctrl-C to copy the text; then click in an area where you want to paste the text and use Ctrl-V to insert the copied text. Test your changes to the script by highlighting the text block and click run. When your script executes successfully VERDI displays the popup window shown in If the user has specified an incorrect path, or incorrect filename for the input dataset, then a series of error messages will appear, starting with the message shown in Figure 88.


NOTE: Click either the Save or the Save As… button to save your edits before exiting the Script Editor.

After saving the script file (e.g. C:\\verdi-script\\myscript.txt), you can run the batch script directly from command lines without invoking the VERDI GUI. On a Windows computer, start a command window, navigate to the directory containing your run.bat file, and then run this command:

run.bat –batch C:\\verdi-script\\myscript.txt

On Linux/Mac platforms, change directory to where the Verdi.sh is located and execute this command (assuming your script file myscript.txt is saved in /home/user/verdi-script directory):

./verdi.sh –batch /home/user/verdi-script/myscript.txt

(Note: the full path to the batch script must be specified. Neglecting to provide the full path along with the batch script name generates the following error: No such file or directory.) The batch script usage will also be displayed from the command line after typing the following command:

(Windows)

run.bat –batch

(Linux/Mac)

./verdi.sh –batch

![Successful Batch Script Run Message](./media/image089.png){#fig:Figure92}


If the user has specified an incorrect path, or incorrect filename for the input dataset, then a series of error messages will appear, starting with the message shown in Figure 88.

![Unsuccessful Batch Script Message: File not found](./media/image088.png){#fig:Figure91}


The VERDI Batch Editor checks to see if the path specified by the user as the imageDir exists. If the path does not exist, VERDI displays the error message:

“java.io.FileNotFoundException: with the path and filename listed”

followed by the message “(No such file or directory).” Verify that you supplied the correct path and filename. The directory specified as the image directory must exist prior to running the batch command. Double-click on the file in the imageDir directory to load and view the image file in your default visualization software. Figure 90 illustrates the tile plot image that was generated by running the highlighted text block.

Figure 93. Plot Image Generated by Task Block

![Plot Image Generated by Task Block](./media/image090.png){#fig:Figure93}


Specify hour/time step formula in batch script mode
---------------------------------------------------

Specify the timestep using the format:

VARIABLE[dataset number]:timestep.

The batch script notation used to specify an hour/time step involves specifying the formula then the hour: O3[1]:17 will result in Ozone for hour 17 from a given file in scripting mode (see Figure 94).

The batch script can be used to generate plots of a specific hour or time step using the formula

**s=Variable[dataset#]:hour**

for example:

**s=O3[1]:17 to plot the Temperature in first dataset for hour 17**

Batch Script Example:

**&lt;Global&gt;**

**dir=$LOCAL_DIR/verdi_2.2/data/model/**

**imageDir=$LOCAL_DIR/verdi_2.2/data/images**

**saveImage=jpeg**

**&lt;/Global&gt;**

**&lt;Task&gt;**

**gtype=tile**

**f= CCTM46_P16.baseO2a.36k.O3MAX**

**imageFile= CCTM46_P16.baseO2a.36k.O3MAX.tstep.17**

**s=O3[1]:17**

**&lt;/Task&gt;**

Mathematical function capability in batch script mode
-----------------------------------------------------

This update provides the user the ability to perform mathematical functions in VERDI using the scripting mode. For example, Find maximum over all time steps at each grid cells.

The batch script can be used to generate plots for each mathematical function by using the task block to define each function. The notation used within the task block is:

**s=Formula(Variable[dataset#])**

For example:

**s=max(O3[1]) to plot the Maximum value over all timesteps for each grid cell in the domain.**

**s=min(O3[1]) to plot the Minimum value over all timesteps for each grid cell**

**s=mean(O3[1]) to plot the Mean value over all timesteps for each grid cell**

**s=sum(O3[1]) to plot the Sum of the variable over all timesteps for each grid cell**

The mathematical functions operate over all time steps at each grid cell. Examples for the batch script notation and the images produced are provided in the following sections.

### Batch Script Example: Maximum Ozone – layer 1 (Figure 94)

**&lt;Global&gt;**

**dir=$LOCAL_DIR/verdi_2.2/data/model/**

**gtype=tile**

**imageDir=$LOCAL_DIR/verdi_2.2/data/images**

**saveImage=jpeg**

**&lt;/Global&gt;**

**&lt;Task&gt;**

**f= CCTM46_P16.baseO2a.36k.O3MAX**

**imageFile=CCTM46_P16.baseO2a.36k.O3MAX.tstepmax.layer1**

**s=max(O3[1])**

**&lt;/Task&gt;**

Figure 94. Tile Plot of Ozone at Time step 17, Layer 1

![Tile Plot of Ozone at Time step 17, Layer 1](./media/image092.png){#fig:Figure94}


### Batch Script Example : Minimum Ozone – layer 1 (Figure 95)

**&lt;Global&gt;**

**dir=$LOCAL_DIR/verdi_2.2/data/model/**

**gtype=tile**

**imageDir=$LOCAL_DIR/verdi_2.2/data/images**

**saveImage=jpeg**

**&lt;/Global&gt;**

**&lt;Task&gt;**

**f= CCTM46_P16.baseO2a.36k.O3MAX**

**imageFile=CCTM46_P16.baseO2a.36k.O3MAX.tstepmin.layer1**

**s=min(O3[1])**

**&lt;/Task&gt;**

Figure 95. Tile Plot of Maximum Ozone (aggregated over 25 time steps)

![Tile Plot of Ozone (aggregated over 25 time steps)](./media/image093.png){#fig:Figure95}

### Batch Script Example : Mean of Ozone – layer 1 (Figure 96)

**&lt;Global&gt;**

**dir=$LOCAL_DIR/verdi_2.2/data/model/**

**gtype=tile**

**imageDir=$LOCAL_DIR/verdi_2.2/data/images**

**saveImage=jpeg**

**&lt;/Global&gt;**

**&lt;Task&gt;**

**f= CCTM46_P16.baseO2a.36k.O3MAX**

**imageFile=CCTM46_P16.baseO2a.36k.O3MAX.tstepmean.layer1**

**s=mean(O3[1])**

**&lt;/Task&gt;**

![Tile Plot of Minimum Ozone (aggregated over 25 time steps)](./media/image094.png){#fig:Figure96}

Figure 97. Tile Plot of Minimum Ozone (aggregated over 25 time steps)

### Batch Script Example : Sum of Ozone – layer 1 (Figure 97)

**&lt;Global&gt;**

**dir=$LOCAL_DIR/verdi_2.2/data/model/**

**gtype=tile**

**imageDir=$LOCAL_DIR/verdi_2.2/data/images**

**saveImage=jpeg**

**&lt;/Global&gt;**

**&lt;Task&gt;**

f= CCTM46_P16.baseO2a.36k.O3MAX

imageFile=CCTM46_P16.baseO2a.36k.O3MAX.tstepsum.layer1

s=sum(O3[1])

&lt;/Task&gt;

Figure 97. Tile Plot of Mean Ozone (aggregated over 25 time steps)

![Tile Plot of Mean Ozone (aggregated over 25 time steps)](./media/image095.png){#fig:Figure97}

Figure 98. Tile Plot of Sum Ozone (aggregated over 25 time steps)

![Tile Plot of Sum Ozone (aggregated over 25 time steps)](./media/image096.png){#fig:Figure98}


<!-- BEGIN COMMENT -->

[<< Previous Chapter](VERDI_ch16.md) - [Home](README.md) - [Next Chapter >>](VERDI_ch18.md)<br>
VERDI User Manual (c) 2025<br>

<!-- END COMMENT -->
