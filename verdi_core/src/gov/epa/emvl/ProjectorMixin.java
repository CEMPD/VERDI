/** ProjectorMixin - Partial implementation of cartographic Projectors.
* 2008-09-01 plessel.todd@epa.gov
**/

// 2014 appears to not be used. Trying to remove it.
//package gov.epa.emvl;
//
//public class ProjectorMixin {
//
//  protected final double theMajorSemiaxis; // Mean equitorial radius 6370997m, should be 6370000m according to the communication between Todd Plessel and Donna Schwede on 	April 26, 2011
//  protected final double theMinorSemiaxis; // Mean polar      radius 6370997m, should be 6370000m according to the communication between Todd Plessel and Donna Schwede on 	April 26, 2011
//  protected final double theFalseEasting;  // Skew offset 0m.
//  protected final double theFalseNorthing; // Skew offset 0m.
//
//  static final protected double PROJECTION_TOLERANCE  = 1e-10;
//  static final protected double CONVERGENCE_TOLERANCE = 1e-12;
//  static final protected int    MAXIMUM_ITERATIONS    = 15;
//
//  /** Constructor
//  * @pre ! Numerics.isNan( newMajorSemiaxis )
//  * @pre ! Numerics.isNan( newMinorSemiaxis )
//  * @pre ! Numerics.isNan( newFalseEasting )
//  * @pre ! Numerics.isNan( newFalseNorthing )
//  * @pre isValidEllipsoid( newMajorSemiaxis, newMinorSemiaxis )
//  * @post majorSemiaxis() == newMajorSemiaxis
//  * @post minorSemiaxis() == newMinorSemiaxis
//  * @post falseEasting()  == newFalseEasting
//  * @post falseNorthing() == newFalseNorthing
//  */
//
//  protected ProjectorMixin( double newMajorSemiaxis, double newMinorSemiaxis,
//                            double newFalseEasting,  double newFalseNorthing) {
//
//    theMajorSemiaxis = newMajorSemiaxis;
//    theMinorSemiaxis = newMinorSemiaxis;
//    theFalseEasting  = newFalseEasting;
//    theFalseNorthing = newFalseNorthing;
//  }
//
//  // Queries:
//
//  public double majorSemiaxis() {
//    final double result = theMajorSemiaxis;
//    return result;
//  }
//
//  public double minorSemiaxis() {
//    final double result = theMinorSemiaxis;
//    return result;
//  }
//
//  public double falseEasting() {
//    final double result = theFalseEasting;
//    return result;
//  }
//
//  public double falseNorthing() {
//    final double result = theFalseNorthing;
//    return result;
//  }
//
//
//  /**
//  * ssfn - See USGS PROJ Library.
//  * @pre ! Numerics.isNan( phi )
//  * @pre ! Numerics.isNan( sinePhi )
//  * @pre ! Numerics.isNan( ellipsoidEccentricity )
//  * @pre Numerics.withinTolerance( sinePhi, Math.sin( phi ), PROJECTION_TOLERANCE )
//  * @pre sinePhi > -1.0
//  * @pre sinePhi <  1.0
//  * @pre Numerics.inRange( ellipsoidEccentricity, 0.0, 1.0 )
//  * @post ! Numerics.isNan( return )
//  */
//
//  protected static double ssfn( double phi, double sinePhi,
//                                double ellipsoidEccentricity ) {
//
//    final double eccentricitySinePhi = ellipsoidEccentricity * sinePhi;
//    final double exponent = ellipsoidEccentricity * 0.5;
//    final double factor1 = Math.tan( ( Numerics.PI_OVER_2 + phi ) * 0.5 );
//    final double factor2 = Math.pow( ( ( 1.0 - eccentricitySinePhi ) /
//                                       ( 1.0 + eccentricitySinePhi ) ),
//                                     exponent );
//    final double result = factor1 * factor2;
//
//    return result;
//  }
//
//
//  /**
//  * msfn - See USGS PROJ Library.
//  * @pre ! Numerics.isNan( sinePhi )
//  * @pre ! Numerics.isNan( cosinePhi )
//  * @pre ! Numerics.isNan( eccentricitySquared )
//  * @pre Numerics.withinTolerance( sinePhi, Math.sqrt( 1.0 - Numerics.square( cosinePhi ) ), PROJECTION_TOLERANCE )
//  * @pre sinePhi > -1.0
//  * @pre sinePhi <  1.0
//  * @pre cosinePhi > -1.0
//  * @pre cosinePhi <  1.0
//  * @pre Numerics.inRange( eccentricitySquared, 0.0, 1.0 )
//  * @pre eccentricitySquared * sinePhi * sinePhi < 1.0
//  * @pre Math.sqrt( 1.0 - eccentricitySquared * Numerics.square( sinePhi ) ) != 0.0
//  * @post ! Numerics.isNan( return )
//  * @post return != 0.0
//  */
//
//  protected static double msfn( double sinePhi, double cosinePhi,
//                                double eccentricitySquared ) {
//
//    final double result =
//      cosinePhi /
//      Math.sqrt( 1.0 - eccentricitySquared * Numerics.square( sinePhi ) );
//
//    return result;
//  }
//
//
//  /**
//  * tsfn - See USGS PROJ Library.
//  * @pre ! Numerics.isNan( phi )
//  * @pre ! Numerics.isNan( sinePhi )
//  * @pre ! Numerics.isNan( ellipsoidEccentricity )
//  * @pre Numerics.withinTolerance( sinePhi, Math.sin( phi ), PROJECTION_TOLERANCE )
//  * @pre sinePhi > -1.0
//  * @pre sinePhi <  1.0
//  * @pre Numerics.inRange( ellipsoidEccentricity, 0.0, 1.0 )
//  * @pre Math.tan( ( Numerics.PI_OVER_2 - phi ) * 0.5 ) != 0.0
//  * @pre Math.abs( ellipsoidEccentricity * sinePhi ) != 1.0
//  * @pre 1.0 + ellipsoidEccentricity * sinePhi != 0.0
//  * @post ! Numerics.isNan( return )
//  * @post return != 0.0
//  */
//
//  protected static double tsfn( double phi, double sinePhi,
//                                double ellipsoidEccentricity ) {
//
//    final double eccentricitySinePhi = ellipsoidEccentricity * sinePhi;
//    final double exponent = ellipsoidEccentricity * 0.5;
//    final double numerator = Math.tan( ( Numerics.PI_OVER_2 - phi ) * 0.5 );
//    final double denominator = Math.pow( ( ( 1.0 - eccentricitySinePhi ) /
//                                           ( 1.0 + eccentricitySinePhi ) ),
//                                         exponent );
//    final double result = numerator / denominator;
//
//    return result;
//  }
//
//
//
//  /**
//  * phi2Iterate - Iterate on unprojected y coordinate.
//  * RETURNS: double converged phi.
//  * @pre ! Numerics.isNan( ts )
//  * @pre Numerics.inRange( theEccentricity, 0.0, 1.0 )
//  * @post ! Numerics.isNan( return )
//  */
//
//  protected static double phi2Iterate( double ts, double theEccentricity ) {
//
//    final int maximumIterations = MAXIMUM_ITERATIONS;
//    final double convergenceTolerance = CONVERGENCE_TOLERANCE;
//    final double halfEccentricity     = theEccentricity * 0.5;
//    double deltaPhi = 0.0;
//    int iteration = 0;
//    double result = Numerics.PI_OVER_2 - 2.0 * Math.atan( ts );
//
//    do {
//      final double con = theEccentricity * Math.sin( result );
//      assert con != -1.0;
//      deltaPhi =
//        Numerics.PI_OVER_2 -
//        2.0 * Math.atan( ts * Math.pow( ( 1.0 - con ) / ( 1.0 + con ),
//                                        halfEccentricity ) )
//        - result;
//      result += deltaPhi;
//      ++iteration;
//    } while ( Math.abs( deltaPhi ) >= convergenceTolerance &&
//              iteration < maximumIterations );
//
//    return result;
//  }
//
//
//}
//
//
