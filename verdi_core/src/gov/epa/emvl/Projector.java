/** Projector.java - Simplifying wrapper for project method of UCAR Projection.
* 2008-11-21 plessel.todd@epa.gov
*/

package gov.epa.emvl;

import ucar.unidata.geoloc.Projection;
import ucar.unidata.geoloc.LatLonPointImpl;
import ucar.unidata.geoloc.ProjectionPointImpl;

public class Projector {

  private final Projection projection;
  private final LatLonPointImpl latLon = new LatLonPointImpl();
  private ProjectionPointImpl xyPoint = new ProjectionPointImpl();
  private final double scale = 1000.0; // Must scale projected points! UGLY!

  public Projector( Projection projection ) {
    this.projection = projection;
  }
  // synchronized keywords added to avoid threading problems with draws - Mary Ann Bitz 6/09/09
  public synchronized void project( double longitude, double latitude, double[] xy ) {
    latLon.set( latitude, longitude );
    projection.latLonToProj( latLon, xyPoint );
    xy[ 0 ] = xyPoint.getX() * scale; 
    xy[ 1 ] = xyPoint.getY() * scale; 
  }

  public synchronized void unproject( double x, double y, double[] lonlat ) {
    xyPoint = new ProjectionPointImpl( x / scale, y / scale );
    projection.projToLatLon( xyPoint, latLon );
    lonlat[ 0 ] = latLon.getLongitude(); 
    lonlat[ 1 ] = latLon.getLatitude(); 
  }

  public Projection getProjection() {
	  return projection;
  }
}
