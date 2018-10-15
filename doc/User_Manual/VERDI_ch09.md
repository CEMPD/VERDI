<!-- BEGIN COMMENT -->
  
[<< Previous Chapter](VERDI_ch08.md) - [Home](README.md) - [Next Chapter >>](VERDI_ch10.md)

<!-- END COMMENT -->

`Subsetting Spatial and Temporal Data
====================================

Both the **Dataset** pane and the **Formula** pane include the three panels discussed in Sections 9.1 through 9.3: **Time Steps, Layers,** and **Domain**, respectively. Section 9.4 then discusses the precedence rules for subsetting data that determine whether **Datasets** or **Formulas** take priority.

Specify Time Step Range
-----------------------

The **Time Steps** panel ([Figure 9‑1](#Figure9-1)) displays the range of time steps included in a dataset. The maximum time-step range that can be used for a dataset or formula is specified in the **Min** and **Max** spinner controls. You can use these controls to select a subset of the available time-step range for plotting. Check the **Use Time Range** box above the spinner controls to tell VERDI to use the time-step range values you have specified when it creates a plot. By default, a plot initially displays data for the minimum time step specified in the **Time Steps** panel. The range of time steps shown in the **Time Step** spinner control at the top of the plot reflects the subset of time steps specified when the **Use Time Range** box is checked. The date and time of the time step displayed in the plot are shown below the x-axis labels. Subsetting a dataset’s or formula’s time-step range affect plots produced with those data. Section 9.4 describes the precedence rules.

<a id=Figure9-1></a>
Figure 9‑1. Specify Time Step Range<br>
<img src="media/image024.png"/>

Specify Layer Range
-------------------

Information on the range of vertical model layers included in a dataset is displayed in the **Layers** panel (Figure 9‑2). Use the **Min** and **Max** spinner controls to select a subset of the available layer data for plotting. Check the **Use Layer Range** box above the spinner controls to tell VERDI to use the layers you have specified. By default, a plot will initially display data for the minimum layer chosen in the **Layers** panel. The range of the layers available in the **Layer** spinner control at the top of the plot matches the subset of layers specified when the **Use Layer Range** box is checked. Subsetting a dataset’s or formula’s layer range affects plots produced with those data. Section 9.4 describes the precedence rules.

<a id=Figure9-2></a>
Figure 9‑2. Edit Layer Range in Formula Pane<br>
<img src="media/image025.png"/>

Specify Domain Range
--------------------

Datasets contain data for cells over a particular geographic area. VERDI refers to this area as a *domain*. By default, the entire domain contained in a dataset is used in creating plots. Use the **Controls** menu and then the **Set Row and Column Ranges** selection to define a subset of this domain for plotting.

Rules of Precedence for Subsetting Data
---------------------------------------

Use the subsetting feature to combine variables from two or more datasets that originally contained different but overlapping time steps and layers. Select identical subsets of the data available in each dataset such that their time steps and layers match. Then you can select variables from those datasets to create a formula and plot the data. Because both the **Dataset** pane and the **Formula** pane have the **Time Steps** and **Layers** panels described above, precedence rules determine which pane’s settings take priority. It is important to understand these rules.

NOTE: You must check the appropriate boxes to have your selected ranges take effect.

-   Dataset precedence: A subset of data specified in the **Dataset** pane takes precedence over any subset specified in the **Formula** pane. However, VERDI does not change the ranges displayed in the **Formula** pane when you select a subset on the **Datasets** pane. For example, if a dataset has a full time-step range of 0-48 and you select a time-step range of 2-40 on the **Dataset** pane, then the 0-48 time-step range that is listed for the formula in the **Formula** pane is not applicable. When you subsequently create a plot, the time-step range subset you chose in the **Dataset** pane (2-40) is displayed.

-   Formula ranges: If you do not specify a subset for a particular data type (i.e., time steps or layers) in the **Dataset** pane, then any subsetting for that data type in the **Formula** pane takes effect.

<!-- BEGIN COMMENT -->

[<< Previous Chapter](VERDI_ch08.md) - [Home](README.md) - [Next Chapter >>](VERDI_ch10.md)<br>
VERDI User Manual (c) 2018<br>

<!-- END COMMENT -->

