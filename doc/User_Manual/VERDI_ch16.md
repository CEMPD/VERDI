<!-- BEGIN COMMENT -->
  
[<< Previous Chapter](VERDI_ch15.md) - [Home](README.md) - [Next Chapter >>](VERDI_ch17.md)

<!-- END COMMENT -->

Mathematical Functions
=======================

All VERDI visualizations are the result of a formula evaluation. Formulas operate on the variables provided by the datasets. The simplest valid formula consists of a single variable; for example, “O3[1]” is the parameter O3 from current dataset 1. Using infix notation, you can construct more complicated formulas using the mathematical operators and functions listed below. (Note that the documentation below derives from the equivalent documentation for the Package for Analysis and Visualization of Environmental data [PAVE], which is available at <http://www.ie.unc.edu/cempd/EDSS/pave_doc/EntirePaveManual.html>.)

Note that the Batch Script method does not support all of the mathematical functions that are supported within the VERDI GUI and/or the command line script options.

Listed in order of precedence, the functions and operators are:

1.  abs, sqr, sqrt, exp, log, ln, sin, cos, tan, sind, cosd, tand, mean, sum, min, max

2.  **\*\*** (power)

3.  **/, \***

4.  **+, -**

5.  **&lt;, &lt;=, &gt;, &gt;=**

6.  ==, !=

7.  &&

8.  **||**

VERDI also supports the following constants:

1.  E 2.7182818284590452354

2.  PI 3.14159265358979323846

3.  NROWS Number of rows in the formula’s currently selected domain

4.  NCOLS Number of columns in the formula’s currently selected domain

5.  NLEVELS Number of levels in the formula’s currently selected domain

Unary Functions
---------------

Unary functions are passed a single argument. Depending on the argument and the function type, the function returns a single value or a matrix of data by performing the function on each cell of the arguments array. For example:

-   **sqrt(4):** Returns 2.0.

-   **sqrt(O3[1]):** Returns a matrix containing the square root of each value in the O3[1] variable’s array.

The following functions return a matrix when passed a dataset variable:

-   **abs:** Returns the absolute value of the argument

-   **sqrt:** Returns the square root of the argument.

-   **sqr:** Returns the square of the argument.

-   **log:** Returns the base 10 logarithm of the argument.

-   **exp:** Returns Euler’s number raised the power of the argument.

-   **ln:** Returns the natural logarithm of the argument.

-   **sin:** Returns the sine of the argument. The argument is in **radians**.

-   **cos:** Returns the cosine of the argument. The argument is in **radians**.

-   **tan:** Returns the tangent of the argument. The argument is in **radians**.

-   **sind:** Returns the sine of the argument. The argument is in **degrees**.

-   **cosd:** Returns the cosine of the argument. The argument is in **degrees**.

-   **tand:** Returns the tangent of the argument. The argument is in **degrees**.

The following functions return a single number in all cases when passed a dataset variable:

-   **mean:** Average cell value for all cells in currently selected domain.

-   **sum:** Sum of all cell values in currently selected domain.

-   **min:** For each cell (*i,j,k*) in the currently selected domain, this calculates the minimum value for that cell over the currently selected time steps. In other words, the minimum value in cells (*i,j,k,*tmin..tmax).

-   **max**: For each cell (*i,j,k*) in the currently selected domain, this calculates the maximum value for that cell over the currently selected time steps. In other words, the maximum value in cells (*i,j,k,*tmin..tmax).

Binary Operators
----------------

Binary operators are not passed a value but operate on the operands to their left and right. Typically, they return a matrix of data by performing the operation on each cell of the operand’s arrays. If both of the operands are single numbers then these binary operators return a single number. For example:

-   **O3[1] * 2:** multiplies each item in the O3[1] array by 2 and returns the result.

-   **O3[1] * O3[3]:** multiplies each item in the O3[1] array by the corresponding item in the O3[3] array and returns the result. (Note that this assumes that the arrays are of equivalent shape.)

-   **3 * 2:** multiplies 3 by 2.

The binary operators:

-   **+** Returns the sum of the operands

-   **-** Returns the difference of the operands

-   **\*** Returns the product of the operands

-   **/** Returns the ratio of the operands

-   **\*\*** Returns the left operand raised to the power of the right operand

Boolean Operators
-----------------

Boolean binary operators return either 1 or 0 in each cell of the resulting matrix. If the operands are single numbers, then a single 1 or 0 is returned. The Boolean binary operators:

-   **&lt;** Returns 1 if the left operand is less than the right operand, else 0

-   **&lt;=** Returns 1 if the left operand is less than or equal to the right operand, else 0

-   **&gt;** Returns 1 if the left operand is greater than the right operand, else 0

-   **&gt;=** Returns 1 if the left operand is greater than or equal to the right operand, else 0

-   **!=** Returns 1 if the left operand is not equal to the right operand, else 0

-   **==** Returns 1 if the left operand is equal to the right operand, else 0

-   **&&** Returns 1 if both operands are nonzero, else 0

-   **||** Returns 1 if either operand is nonzero, else 0

Time Step Index
---------------

A time step index can be specified after a variable name. For example, “O3[1]:0” is the value of the O3[1] variable at the first time step.

<!-- BEGIN COMMENT -->

[<< Previous Chapter](VERDI_ch15.md) - [Home](README.md) - [Next Chapter >>](VERDI_ch17.md)<br>
VERDI User Manual (c) 2024<br>

<!-- END COMMENT -->

