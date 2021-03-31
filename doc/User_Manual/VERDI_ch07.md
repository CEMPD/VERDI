<!-- BEGIN COMMENT -->
  
[<< Previous Chapter](VERDI_ch06.md) - [Home](README.md) - [Next Chapter >>](VERDI_ch08.md)

<!-- END COMMENT -->

Working with Formulas
=====================

All plots in VERDI are generated from formulas. A formula is used to compare or manipulate variables in one or more gridded datasets. A formula can be as simple as a single variable from one gridded dataset or it can be an equation that uses variable(s) from one or more gridded datasets. Formulas are used to create visualizations that can assist with model performance evaluations, for example, or can help in comparing model results with observations.

Adding and Removing a Formula
-----------------------------

After loading the desired gridded datasets, you can use the variables in them to create formulas. To use a variable to create a simple formula, double click on the name of the variable. This will add the formula &lt;Variable Name&gt;[&lt;Dataset Number&gt;] to the formula list in the **Formulas** pane—for example, O3[1]. To add a variable to the formula editor window, highlight the variable, **right click** on the variable name in the **Datasets** pane, and select **Add Variable(s) to Formula Editor**. To add all or a subset of variables from the **Dataset** pane to the formula editor window, click on the first variable to highlight it, hold the Shift key down and click at the last variable that you want to include, then right click and select **Add Variables(s)**. The formulas that are highlighted using this method will be added to the formula editor PDF:([Fig-@fig:Figure19])) or GitHub:([Figure 19](#Figure19)).

<!-- BEGIN COMMENT -->

<a id=Figure19></a>
Figure 19. Adding Multiple Variables to Formula Editor<br>

<!-- END COMMENT -->

![Adding Multiple Variables to Formula Editor](./media/image019.png){#fig:Figure19}

After the variable names are added to the Formula Editor, click on the formula pane and use the cursor and the keyboard to type in the mathematical functions and operators where needed to create a valid formula (see Section 7.2 and Chapter 16). After the formula has been created in the Formula Editor, click the **Add** button to place it in the list of formulas available in the **Formula** pane.

To remove a formula from the formulas list, highlight the name in the list and press the yellow **minus** button. Note that removing a formula from the formula list does not remove plots that were created prior to the deletion of the formula.

Example Formulas
----------------

To examine the values of ozone in dataset 1, the formula would be “O3[1]”.

To examine the difference in ozone between datasets 1 and 2, the formula would be “O3[1]-O3[2]”.

To calculate the percent difference in ozone between datasets 1 and 2, the formula would be “(O3[1]-O3[2])*100/(O3[2])”.To identify all cells where the ozone concentration exceeds a certain value, you can use the Boolean operators to focus on ranges of your data that are of particular interest. A Boolean expression will evaluate to either True = 1 or False = 0. For example, to plot the cells in which the ozone values in dataset 1 exceed 0.080 ppm, you could use the formula “(O3[1]&gt;0.080)*O3[1]”. In the resulting plot, each cell where O3[1] exceeds 0.080 will show the value of O3[1] for that cell; for all other cells the value shown will be zero.

The notations that can be used in formulas to represent various mathematical functions, and the order of precedence of these functions, are listed in Chapter 16, “Mathematical Functions.”

Selecting a Formula for Plotting
--------------------------------

You must select a formula before creating a plot. Check to see which formula is highlighted in the **Formula** pane, or look to the right of the plot buttons above the plots area of the main window to see the selected formula. By default, VERDI designates the most recently added formula as the selected formula. To change the selected formula to a different one in the list, click on a formula in the list on the **Formulas** pane, and you will then see it displayed as the selected formula above the plots area.

Saving Formulas
---------------

Both formulas and datasets can be saved using the **Save Project** item in the **File** pull-down menu on the VERDI main window. Saving new projects and loading existing projects were discussed in Section 5.1.

Time Step and Layer Ranges
--------------------------

Instructions for using the **Time Steps** and **Layers** panels are discussed in Chapter 9 Subsetting Spatial and Temporal Data.


<!-- BEGIN COMMENT -->

[<< Previous Chapter](VERDI_ch06.md) - [Home](README.md) - [Next Chapter >>](VERDI_ch08.md)<br>
VERDI User Manual (c) 2021<br>

<!-- END COMMENT -->
