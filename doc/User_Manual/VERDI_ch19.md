<!-- BEGIN COMMENT -->
  
[<< Previous Chapter](VERDI_ch18.md) - [Home](README.md) - [Next Chapter >>](VERDI_ch20.md)

<!-- END COMMENT -->
 

Areal Interpolation Calculations
=================================

Before calculating the average value for a polygon segment, the area for each polygon is calculated using the projection of the grid system loaded. The system then calculates the area of overlay between each grid cell and the polygon segment.

The total contribution of a value (concentration, deposition, rainfall, etc.) from each cell for a given polygon segment is calculated using the following equation:

TV <sub>i</sub> = sum (O<sub>rci</sub> * V<sub>rc</sub>) where

O<sub>rci</sub> = Area of overlay of cell at row r and column c with segment i,

V<sub>rc</sub> = value of cell at row r and column c, and

r and c iterate across the rows and columns of the grid.

The Average Value is calculated by dividing the total value by the area of the polygon segment:

AverageV<sub>i</sub> = TV<sub>i</sub> / A<sub>i</sub> where

A<sub>i</sub> = Area of the polygon segment i

<!-- BEGIN COMMENT -->

[<< Previous Chapter](VERDI_ch18.md) - [Home](README.md) - [Next Chapter >>](VERDI_ch20.md)<br>
VERDI User Manual (c) 2018<br>

<!-- END COMMENT -->
