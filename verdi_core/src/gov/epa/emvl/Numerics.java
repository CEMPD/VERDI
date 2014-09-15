/** Numerics - Numeric utility routines.
* 2008-09-01 plessel.todd@epa.gov
**/

package gov.epa.emvl;

public final class Numerics {

  private Numerics() {} // Non-instantiable utility class like java.lang.Math.

  public static final double DBL_MIN    = 2.2250738585072014E-308;
  public static final double DBL_MAX    = 1.7976931348623157E+308;
  public static final double PI_OVER_2  = 1.57079632679489661923;
  public static final double PI_OVER_4  = 0.78539816339744830962;
  public static final double TOLERANCE  = 1e-6;

  // Queries:

  /**
  * isNan - is x not-a-number?
  */

  public static boolean isNan( double x ) {
    final double copy = x;
    final boolean result = ( copy != x );
    return result;
  }


  /**
  * inRange - is minimum <= x <= maximum?
  * @pre ! isNan( x )
  * @pre ! isNan( minimum )
  * @pre ! isNan( maximum )
  */

  public static boolean inRange( double value,
                                  double minimum, double maximum ) {
    final boolean result = value >= minimum && value <= maximum;
    return result;
  }


  /**
  * square - x * x.
  * @pre ! isNan( x )
  * @post ! isNan( return )
  */

  public static double square( double x ) {
    final double result = x * x;
    return result;
  }


  /**
  * sign - sign (1.0 or -1.0) of x.
  * @pre ! isNan( x )
  * @post x >= 0.0 implies return == 1.0
  * @post x <  0.0 implies return == -1.0
  */

  public static double sign( double x ) {
    final double result = x < 0.0 ? -1.0 : 1.0;
    return result;
  }


  /**
   * clamp - clamp value between minimum and maximum.
   * @pre ! isNan( value )
   * @pre ! isNan( minimum )
   * @pre ! isNan( maximum )
   * @pre minimum <= maximum
   * @post return >= minimum
   * @post return <= maximum
   */

   public static double clamp( double value, double minimum, double maximum ) {
     double result = value;

     if ( result > maximum ) {
       result = maximum;
     } else if ( result < minimum ) {
       result = minimum;
     }

     return result;
   }


  /**
   * clamp - clamp value between minimum and maximum.
   * @pre minimum <= maximum
   * @post return >= minimum
   * @post return <= maximum
   */

   public static int clampInt( int value, int minimum, int maximum ) {
     int result = value;
 
      if ( result > maximum ) {
       result = maximum;
     } else if ( result < minimum ) {
       result = minimum;
     }
      return result;
   }


  /**
  * round1 - round x to 1 decimal place.
  * @pre ! isNan( value )
  * @post ! isNan( return )
  * @post Math.abs( value - return ) < 0.11
  */

  public static double round1( double value ) {
    double result = (int) ( value * 10.0 );
    result = result / 10.0;
    return result;
  }


  /**
  * safeSum - x + y.
  * @pre ! isNan( x )
  * @pre ! isNan( y )
  * @post ! isNan( return )
  * @post x == -y implies return == 0.0
  */

  public static double safeSum( double x, double y ) {
    final double result = x == -y ? 0.0 : x + y;
    return result;
  }


  /**
  * safeDifference - x - y.
  * @pre ! isNan( x )
  * @pre ! isNan( y )
  * @post ! isNan( return )
  * @post x == y implies return == 0.0
  */

  public static double safeDifference( double x, double y ) {
    final double result = x == y ? 0.0 : x - y;
    return result;
  }


  /**
  * safeProduct - x * y.
  * @pre ! isNan( x )
  * @pre ! isNan( y )
  * @post ! isNan( return )
  * @post x == 0.0 implies return == 0.0
  * @post y == 0.0 implies return == 0.0
  */

  public static double safeProduct( double x, double y ) {
    final double result = x == 0.0 ? 0.0 : y == 0.0 ? 0.0 : x * y;
    return result;
  }


  /**
  * safeQuotient - numerator / denominator.
  * @pre ! isNan( numerator )
  * @pre ! isNan( denominator )
  * @pre denominator != 0.0
  * @post ! isNan( return )
  * @post numerator == 0.0 implies return == 0.0
  * @post numerator == denominator implies return == 1.0
  * @post numerator == -denominator implies return == -1.0
  */

  public static double safeQuotient( double numerator, double denominator ) {
    final double result =
        numerator   ==  0.0 ? 0.0
      : denominator ==  1.0 ? numerator
      : denominator == -1.0 ? -numerator
      : numerator == denominator ? 1.0
      : numerator == -denominator ? -1.0
      : numerator / denominator;
    return result;
  }


  /**
  * withinTolerance - Do x and y differ by less than tolerance?
  * @pre ! isNan( tolerance )
  * @pre tolerance <= 0.1
  * @pre tolerance >= 0.0
  */

  public static boolean withinTolerance( double x, double y,
                                            double tolerance ) {

    boolean result = isNan( x ) && isNan( y );

    if ( ! result ) {

      if ( x == 0.0 ) {
        result = inRange( y, -tolerance, tolerance ); // Close enough to 0?
      } else if ( y == 0.0 ) {
        result = inRange( x, -tolerance, tolerance ); // Close enough to 0?
      } else if ( inRange( x, y - tolerance, y + tolerance)) {//Or each other?
        result = true;
      } else if ( inRange( y, x - tolerance, x + tolerance)) {//Or each other?
        result = true;
      } else { // Ratio handles cases of large values differing in last digits.
        final double ax = Math.abs( x );
        final double ay = Math.abs( y );

        if ( ay < 1.0 && ax > ay * DBL_MAX ) { // Avoid overflow.
          result = false;
        } else if ( ay > 1.0 && ax < ay * DBL_MIN ) { // Avoid underflow.
          result = false;
        } else {
          final double ratio = x / y;
          result = inRange( ratio, 1.0 - tolerance, 1.0 + tolerance );
        }
      }
    }

    return result;
  }


  public static boolean aboutEqual( double x, double y ) {
    final boolean result = withinTolerance( x, y, TOLERANCE );
    return result;
  }

}


