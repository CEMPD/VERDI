// NOTE: This class uses GeoTools and OpenGis for Coordinate Referencing System (crs).
package anl.verdi.area.target;

import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.swing.DefaultComboBoxModel;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.geotools.referencing.CRS;
//import javax.swing.JFrame;
import org.geotools.swing.JMapFrame;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
	/**
	 * Generic GUI widget that allows user to enter parameters for the selected projection.<br>
	 * Supported projections:
	 * <li>Lambert Conformal Conic</li>
	 * <li>Mercator</li>
	 * <li>Transverse Mercator</li>
	 * <li>Polar Stereographic</li>
	 * <li>XY (Simple Scaling)</li>
	 */
	public class ProjectionInfo extends javax.swing.JPanel {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1156146804881440587L;
		static final Logger Logger = LogManager.getLogger(ProjectionInfo.class.getName());
		final static int XYProjection=0;
		final static int PSProjection=1;
		final static int LambertCCProjection=2;
		final static int MercatorProjection=3;
		final static int TransverseMercatorProjection=4;
		
		/**
		 * Lambert adaptor
		 * @author Gordon Lurie
		 * @version $Revision: 1.3 $
		 */
		public class LambertCCProjectionGUIAdaptor implements ProjectionGUIAdaptor {
			final Logger Logger = LogManager.getLogger(LambertCCProjectionGUIAdaptor.class.getName());
			/**
			 * Constructor for GeoLambertCCProjectionGUIAdaptor
			 */
			public LambertCCProjectionGUIAdaptor() {
				super();
			}

			/**
			 * Method initialize
			 * @param d ProjectionInfo
			 * @see anl.jeoviewer.gui.ProjectionInfo$ProjectionGUIAdaptor#initialize(ProjectionInfo)
			 */
			public void initialize(ProjectionInfo d) {
				getDataComboBox2().setVisible(true); getBooleanCheckBox().setVisible(d.isAllowAutoCM());
				getDataLabel1().setVisible(true); getDataTextField1().setVisible(true);
				getDataLabel2().setVisible(true); getDataTextField2().setVisible(false);
				getDataLabel3().setVisible(true); getDataTextField3().setVisible(true);
				getDataLabel4().setVisible(true); getDataTextField4().setVisible(true);
				getDataLabel5().setVisible(true); getDataTextField5().setVisible(true);
				getDataLabel6().setVisible(true); getDataTextField6().setVisible(true);
				getDataLabel7().setVisible(true); getDataTextField7().setVisible(true);
				getBooleanCheckBox().setText("Auto");
				getDataLabel1().setText("Central Meridian");
				getDataLabel2().setText("Ellipsoid Name");
				getDataLabel3().setText("Origin Lat.");
				getDataLabel4().setText("False Easting");
				getDataLabel5().setText("False Northing");
				getDataLabel6().setText("Standard Parallel 1");
				getDataLabel7().setText("Standard Parallel 2");
				DefaultComboBoxModel m=new DefaultComboBoxModel();
				for (Iterator i=new TreeSet(getAllEllipseData()).iterator();i.hasNext();)
					m.addElement(i.next());
				getDataComboBox2().setModel(m);
			}

			

			/**
			 * Method validate
			 * @param d ProjectionInfo
			 * @return boolean
			 * @see anl.jeoviewer.gui.ProjectionInfo$ProjectionGUIAdaptor#validate(ProjectionInfo)
			 */
			public boolean validate(ProjectionInfo d) {
				boolean autoCM=getBooleanCheckBox().isSelected();
				getDataTextField1().setEnabled(!autoCM);
				if (!autoCM) {
					Double cm=doubleValueOf(getDataTextField1().getText());
					if (cm==null || cm.doubleValue()<-180 || cm.doubleValue()>180) {
						d.getStatusTextField().setText("Central Meridian not valid (-180 to 180 is valid)");
						return false;
					}
				}
				Double trueLat=doubleValueOf(getDataTextField3().getText());
				if (trueLat==null || trueLat.doubleValue()<-90 || trueLat.doubleValue()>90) {
					d.getStatusTextField().setText("Latitude of Origin not valid (-90 to 90 is valid)");
					return false;
				}
				Double fe=doubleValueOf(getDataTextField4().getText());
				if (fe==null) {
					d.getStatusTextField().setText("False Easting not valid (must be a number)");
					return false;
				}
				Double fn=doubleValueOf(getDataTextField5().getText());
				if (fn==null) {
					d.getStatusTextField().setText("False Northing not valid (must be a number)");
					return false;
				}
				Double sp1=doubleValueOf(getDataTextField6().getText());
				if (sp1==null || sp1.doubleValue()<-90 || sp1.doubleValue()>90) {
					d.getStatusTextField().setText("Standard Parallel 1 not valid (-90 to 90 is valid)");
					return false;
				}
				Double sp2=doubleValueOf(getDataTextField7().getText());
				if (sp2==null || sp2.doubleValue()<-90 || sp2.doubleValue()>90) {
					d.getStatusTextField().setText("Standard Parallel 2 not valid (-90 to 90 is valid)");
					return false;
				}
				d.getStatusTextField().setText("Valid");
				return true;
			}
//			/**
//			 * Method getArgs
//			 * @param d ProjectionInfo
//			 * @return Object[]
//			 */
//			private Object[] getArgs(ProjectionInfo d) {
//				Object[] args=new Object[7];
//				args[0]=((EllipseData)getDataComboBox2().getSelectedItem()).getName();
//				args[1]=getDataTextField6().getText();
//				args[2]=getDataTextField7().getText();
//				if (getBooleanCheckBox().isSelected())
//					args[3]="auto";
//				else args[3]=getDataTextField1().getText();
//				args[4]=getDataTextField3().getText();
//				args[5]=getDataTextField4().getText();
//				args[6]=getDataTextField5().getText();
//				return args;
//			}
			
			/**
			 * Method getProjection
			 * @param d ProjectionInfo
			 * @return GeoProjection
			 * @see anl.jeoviewer.gui.ProjectionInfo$ProjectionGUIAdaptor#getProjection(ProjectionInfo)
			 */
			
			public CoordinateReferenceSystem getProjection(ProjectionInfo d) {
				try {
					String coordString = createWKT();
					if(coordString==null)return null;
					
					return CRS.parseWKT(coordString);
				} catch (Exception ex) {
					Logger.error("Error returning CRS: " + getClass().getName()+">>getProjection() "+projectionClass);
					ex.printStackTrace(System.err);
				}
				
				return null;
			}
			public String createWKT() throws IOException {
				VelocityContext context = new VelocityContext();
				String template = getClass().getPackage().getName();
				ClassLoader loader = Thread.currentThread().getContextClassLoader();
				Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
				template = template.replace('.', '/');
				template = template + "/lambert_full_wkt.vt";
				
				 
				EllipseData data=(EllipseData)getDataComboBox2().getSelectedItem();
				String ellipseValue = data.getWKTString();
				
				Double cm=doubleValueOf(getDataTextField1().getText());
				Double trueLat=doubleValueOf(getDataTextField3().getText());
				Double fe=doubleValueOf(getDataTextField4().getText());
				Double fn=doubleValueOf(getDataTextField5().getText());
				Double sp1=doubleValueOf(getDataTextField6().getText());
				Double sp2=doubleValueOf(getDataTextField7().getText());
				
				context.put("geogcs", ellipseValue);
				context.put("central_meridian", cm.doubleValue());
				context.put("latitude_of_origin", trueLat.doubleValue());
				context.put("false_easting", fe.doubleValue());
				context.put("false_northing", fn.doubleValue());
				context.put("standard_parallel_1", sp1.doubleValue());
				context.put("standard_parallel_2", sp2.doubleValue());

				Writer writer = new StringWriter();
				try {
					Velocity.mergeTemplate(template, "UTF-8", context, writer);
				} catch (Exception ex) {
					throw new IOException("Error merging template", ex);
				} finally {
					if (writer != null) writer.close();
					Thread.currentThread().setContextClassLoader(loader);
				}

				return writer.toString();
			}
		}

		/**
		 * Mercator adaptor
		 * @author Gordon Lurie
		 * @version $Revision: 1.3 $
		 */
		public class MercatorProjectionGUIAdaptor implements ProjectionGUIAdaptor {
			final Logger Logger = LogManager.getLogger(MercatorProjectionGUIAdaptor.class.getName());
			/**
			 * Constructor for GeoMercatorProjectionGUIAdaptor
			 */
			public MercatorProjectionGUIAdaptor() {
				super();
			}

			/**
			 * Method initialize
			 * @param d ProjectionInfo
			 * @see anl.jeoviewer.gui.ProjectionInfo$ProjectionGUIAdaptor#initialize(ProjectionInfo)
			 */
			public void initialize(ProjectionInfo d) {
				getDataComboBox2().setVisible(true); getBooleanCheckBox().setVisible(d.isAllowAutoCM());
				getDataLabel1().setVisible(true); getDataTextField1().setVisible(true);
				getDataLabel2().setVisible(true); getDataTextField2().setVisible(false);
				getDataLabel3().setVisible(true); getDataTextField3().setVisible(true);
				getDataLabel4().setVisible(true); getDataTextField4().setVisible(true);
				getDataLabel5().setVisible(true); getDataTextField5().setVisible(true);
				getDataLabel6().setVisible(false); getDataTextField6().setVisible(false);
				getDataLabel7().setVisible(false); getDataTextField7().setVisible(false);
				getBooleanCheckBox().setText("Auto");
				getDataLabel1().setText("Central Meridian");
				getDataLabel2().setText("Ellipsoid Name");
				getDataLabel3().setText("Standard Parallel");
				getDataLabel4().setText("False Easting");
				getDataLabel5().setText("False Northing");
				
				DefaultComboBoxModel m=new DefaultComboBoxModel();
				for (Iterator i=new TreeSet(getAllEllipseData()).iterator();i.hasNext();)
					m.addElement(i.next());
				getDataComboBox2().setModel(m);
			}

			
			/**
			 * Method validate
			 * @param d ProjectionInfo
			 * @return boolean
			 * @see anl.jeoviewer.gui.ProjectionInfo$ProjectionGUIAdaptor#validate(ProjectionInfo)
			 */
			public boolean validate(ProjectionInfo d) {
				boolean autoCM=getBooleanCheckBox().isSelected();
				getDataTextField1().setEnabled(!autoCM);
				if (!autoCM) {
					Double cm=doubleValueOf(getDataTextField1().getText());
					if (cm==null || cm.doubleValue()<-180 || cm.doubleValue()>180) {
						d.getStatusTextField().setText("Central Meridian not valid (-180 to 180 is valid)");
						return false;
					}
				}
				Double trueLat=doubleValueOf(getDataTextField3().getText());
				if (trueLat==null || trueLat.doubleValue()<-90 || trueLat.doubleValue()>90) {
					d.getStatusTextField().setText("Latitude of Origin not valid (-90 to 90 is valid)");
					return false;
				}
				Double fe=doubleValueOf(getDataTextField4().getText());
				if (fe==null) {
					d.getStatusTextField().setText("False Easting not valid (must be a number)");
					return false;
				}
				Double fn=doubleValueOf(getDataTextField5().getText());
				if (fn==null) {
					d.getStatusTextField().setText("False Northing not valid (must be a number)");
					return false;
				}
				d.getStatusTextField().setText("Valid");
				
				
				return true;
			}
//			/**
//			 * Method getArgs
//			 * @param d ProjectionInfo
//			 * @return Object[]
//			 */
//			private Object[] getArgs(ProjectionInfo d) {
//				Object[] args=new Object[5];
//				args[0]=((EllipseData)getDataComboBox2().getSelectedItem()).getName();
//				if (getBooleanCheckBox().isSelected())
//					args[1]="auto";
//				else args[1]=getDataTextField1().getText();
//				args[2]=getDataTextField3().getText();
//				args[3]=getDataTextField4().getText();
//				args[4]=getDataTextField5().getText();
//				return args;
//			}
			
			/**
			 * Method getProjection
			 * @param d ProjectionInfo
			 * @return GeoProjection
			 * @see anl.jeoviewer.gui.ProjectionInfo$ProjectionGUIAdaptor#getProjection(ProjectionInfo)
			 */
			
			public CoordinateReferenceSystem getProjection(ProjectionInfo d) {
				try {
					String coordString = createWKT();
					if(coordString==null)return null;
					
					return CRS.parseWKT(coordString);
				} catch (Exception ex) {
					System.err.println(getClass().getName()+">>getProjection() "+projectionClass);
					Logger.error("Error getting CRS: " + getClass().getName()+">>getProjection() "+projectionClass);
					ex.printStackTrace(System.err);
				}
				//return d.getProjection(GeoLambertCCProjection.class,getArgs(d));
				return null;
			}
			public String createWKT() throws IOException {
				VelocityContext context = new VelocityContext();
				String template = getClass().getPackage().getName();
				ClassLoader loader = Thread.currentThread().getContextClassLoader();
				Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
				template = template.replace('.', '/');
				template = template + "/mercator_full_wkt.vt";
				
				EllipseData data=(EllipseData)getDataComboBox2().getSelectedItem();
				String ellipseValue = data.getWKTString();
				
				Double cm=doubleValueOf(getDataTextField1().getText());
				Double trueLat=doubleValueOf(getDataTextField3().getText());
				Double fe=doubleValueOf(getDataTextField4().getText());
				Double fn=doubleValueOf(getDataTextField5().getText());
								
				context.put("geogcs", ellipseValue);
				context.put("central_meridian", cm.doubleValue());
				context.put("standard_parallel_1", trueLat.doubleValue());
				context.put("false_easting", fe.doubleValue());
				context.put("false_northing", fn.doubleValue());
				
				Writer writer = new StringWriter();
				try {
					Velocity.mergeTemplate(template, "UTF-8", context, writer);
				} catch (Exception ex) {
					throw new IOException("Error merging template", ex);
				} finally {
					if (writer != null) writer.close();
					Thread.currentThread().setContextClassLoader(loader);
				}

				return writer.toString();
			}

		}

		/**
		 * Polar stereographic adaptor
		 * @author Gordon Lurie
		 * @version $Revision: 1.3 $
		 */
		public class PSProjectionGUIAdaptor implements ProjectionGUIAdaptor {
			final Logger Logger = LogManager.getLogger(PSProjectionGUIAdaptor.class.getName());
			/**
			 * Constructor for GeoPSProjectionGUIAdaptor
			 */
			public PSProjectionGUIAdaptor() {
				super();
			}

			/**
			 * Method initialize
			 * @param d ProjectionInfo
			 * @see anl.jeoviewer.gui.ProjectionInfo$ProjectionGUIAdaptor#initialize(ProjectionInfo)
			 */
			public void initialize(ProjectionInfo d) {
				getDataComboBox2().setVisible(true); getBooleanCheckBox().setVisible(d.isAllowAutoCM());
				getDataLabel1().setVisible(true); getDataTextField1().setVisible(true);
				getDataLabel2().setVisible(true); getDataTextField2().setVisible(false);
				getDataLabel3().setVisible(true); getDataTextField3().setVisible(true);
				getDataLabel4().setVisible(true); getDataTextField4().setVisible(true);
				getDataLabel5().setVisible(true); getDataTextField5().setVisible(true);
				getDataLabel6().setVisible(false); getDataTextField6().setVisible(false);
				getDataLabel7().setVisible(false); getDataTextField7().setVisible(false);
				getBooleanCheckBox().setText("Auto");
				getDataLabel1().setText("Central Meridian");
				getDataLabel2().setText("Ellipsoid Name");
				getDataLabel3().setText("Latitude of Origin");
				getDataLabel4().setText("False Easting");
				getDataLabel5().setText("False Northing");
				DefaultComboBoxModel m=new DefaultComboBoxModel();
				for (Iterator i=new TreeSet(getAllEllipseData()).iterator();i.hasNext();)
					m.addElement(i.next());
				getDataComboBox2().setModel(m);
			}

			
			/**
			 * Method validate
			 * @param d ProjectionInfo
			 * @return boolean
			 * @see anl.jeoviewer.gui.ProjectionInfo$ProjectionGUIAdaptor#validate(ProjectionInfo)
			 */
			public boolean validate(ProjectionInfo d) {
				boolean autoCM=getBooleanCheckBox().isSelected();
				getDataTextField1().setEnabled(!autoCM);
				if (!autoCM) {
					Double cm=doubleValueOf(getDataTextField1().getText());
					if (cm==null || cm.doubleValue()<-180 || cm.doubleValue()>180) {
						d.getStatusTextField().setText("Central Meridian not valid (-180 to 180 is valid)");
						return false;
					}
				}
				Double trueLat=doubleValueOf(getDataTextField3().getText());
				if (trueLat==null || trueLat.doubleValue()<-90 || trueLat.doubleValue()>90) {
					d.getStatusTextField().setText("Latitude of Origin not valid (-90 to 90 is valid)");
					return false;
				}
				Double fe=doubleValueOf(getDataTextField4().getText());
				if (fe==null) {
					d.getStatusTextField().setText("False Easting not valid (must be a number)");
					return false;
				}
				Double fn=doubleValueOf(getDataTextField5().getText());
				if (fn==null) {
					d.getStatusTextField().setText("False Northing not valid (must be a number)");
					return false;
				}
				d.getStatusTextField().setText("Valid");
				return true;
			}
//			/**
//			 * Method getArgs
//			 * @param d ProjectionInfo
//			 * @return Object[]
//			 */
//			private Object[] getArgs(ProjectionInfo d) {
//				Object[] args=new Object[5];
//				args[0]=((EllipseData)getDataComboBox2().getSelectedItem()).getName();
//				if (getBooleanCheckBox().isSelected())
//					args[1]="auto";
//				else args[1]=getDataTextField1().getText();
//				args[2]=getDataTextField3().getText();
//				args[3]=getDataTextField4().getText();
//				args[4]=getDataTextField5().getText();
//				return args;
//			}
			
			/**
			 * Method getProjection
			 * @param d ProjectionInfo
			 * @return GeoProjection
			 * @see anl.jeoviewer.gui.ProjectionInfo$ProjectionGUIAdaptor#getProjection(ProjectionInfo)
			 */
			
			public CoordinateReferenceSystem getProjection(ProjectionInfo d) {
				try {
					String coordString = createWKT();
					if(coordString==null)return null;
					
					return CRS.parseWKT(coordString);
				} catch (Exception ex) {
					System.err.println(getClass().getName()+">>getProjection() "+projectionClass);
					Logger.error("Error getting CRS: " + getClass().getName()+">>getProjection() "+projectionClass);
					ex.printStackTrace(System.err);
				}
				//return d.getProjection(GeoLambertCCProjection.class,getArgs(d));
				return null;
			}
			public String createWKT() throws IOException {
				VelocityContext context = new VelocityContext();
				String template = getClass().getPackage().getName();
				ClassLoader loader = Thread.currentThread().getContextClassLoader();
				Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
				template = template.replace('.', '/');
				template = template + "/polar_full_wkt.vt";
				
				EllipseData data=(EllipseData)getDataComboBox2().getSelectedItem();
				String ellipseValue = data.getWKTString();
				
				Double cm=doubleValueOf(getDataTextField1().getText());
				Double trueLat=doubleValueOf(getDataTextField3().getText());
				Double fe=doubleValueOf(getDataTextField4().getText());
				Double fn=doubleValueOf(getDataTextField5().getText());
				//Double sp1=doubleValueOf(getDataTextField6().getText());
				//Double sp2=doubleValueOf(getDataTextField7().getText());
				
				context.put("geogcs", ellipseValue);
				context.put("central_meridian", cm.doubleValue());
				context.put("latitude_of_origin", trueLat.doubleValue());
				context.put("false_easting", fe.doubleValue());
				context.put("false_northing", fn.doubleValue());
				//context.put("standard_parallel_1", sp1.doubleValue());
				//context.put("standard_parallel_2", sp2.doubleValue());

				Writer writer = new StringWriter();
				try {
					Velocity.mergeTemplate(template, "UTF-8", context, writer);
				} catch (Exception ex) {
					throw new IOException("Error merging template", ex);
				} finally {
					if (writer != null) writer.close();
					Thread.currentThread().setContextClassLoader(loader);
				}

				return writer.toString();
			}
	}

		/**
		 * Transverse mercator adaptor
		 * @author Gordon Lurie
		 * @version $Revision: 1.3 $
		 */
		public class TransverseMercatorProjectionGUIAdaptor implements ProjectionGUIAdaptor {
			final Logger Logger = LogManager.getLogger(TransverseMercatorProjectionGUIAdaptor.class.getName());
			/**
			 * Constructor for GeoTransverseMercatorProjectionGUIAdaptor
			 */
			public TransverseMercatorProjectionGUIAdaptor() {
				super();
			}

			/**
			 * Method initialize
			 * @param d ProjectionInfo
			 * @see anl.jeoviewer.gui.ProjectionInfo$ProjectionGUIAdaptor#initialize(ProjectionInfo)
			 */
			public void initialize(ProjectionInfo d) {
				getDataComboBox2().setVisible(true); getBooleanCheckBox().setVisible(d.isAllowAutoCM());
				getDataLabel1().setVisible(true); getDataTextField1().setVisible(true);
				getDataLabel2().setVisible(true); getDataTextField2().setVisible(false);
				getDataLabel3().setVisible(true); getDataTextField3().setVisible(true);
				getDataLabel4().setVisible(true); getDataTextField4().setVisible(true);
				getDataLabel5().setVisible(true); getDataTextField5().setVisible(true);
				getDataLabel6().setVisible(true); getDataTextField6().setVisible(true);
				getDataLabel7().setVisible(false); getDataTextField7().setVisible(false);
				getBooleanCheckBox().setText("Auto");
				getDataLabel1().setText("Central Meridian");
				getDataLabel2().setText("Ellipsoid Name");
				getDataLabel3().setText("Latitude of Origin");
				getDataLabel4().setText("False Easting");
				getDataLabel5().setText("False Northing");
				getDataLabel6().setText("Scale Factor");
				DefaultComboBoxModel m=new DefaultComboBoxModel();
				for (Iterator i=new TreeSet(getAllEllipseData()).iterator();i.hasNext();)
					m.addElement(i.next());
				getDataComboBox2().setModel(m);
			}

			
			/**
			 * Method validate
			 * @param d ProjectionInfo
			 * @return boolean
			 * @see anl.jeoviewer.gui.ProjectionInfo$ProjectionGUIAdaptor#validate(ProjectionInfo)
			 */
			public boolean validate(ProjectionInfo d) {
				boolean autoCM=getBooleanCheckBox().isSelected();
				getDataTextField1().setEnabled(!autoCM);
				if (!autoCM) {
					Double cm=doubleValueOf(getDataTextField1().getText());
					if (cm==null || cm.doubleValue()<-180 || cm.doubleValue()>180) {
						d.getStatusTextField().setText("Central Meridian not valid (-180 to 180 is valid)");
						return false;
					}
				}
				Double trueLat=doubleValueOf(getDataTextField3().getText());
				if (trueLat==null || trueLat.doubleValue()<-90 || trueLat.doubleValue()>90) {
					d.getStatusTextField().setText("Latitude of Origin not valid (-90 to 90 is valid)");
					return false;
				}
				Double fe=doubleValueOf(getDataTextField4().getText());
				if (fe==null) {
					d.getStatusTextField().setText("False Easting not valid (must be a number)");
					return false;
				}
				Double fn=doubleValueOf(getDataTextField5().getText());
				if (fn==null) {
					d.getStatusTextField().setText("False Northing not valid (must be a number)");
					return false;
				}
				Double sf=doubleValueOf(getDataTextField6().getText());
				if (sf==null) {
					d.getStatusTextField().setText("Scale Factor not valid (must be a number)");
					return false;
				}
				d.getStatusTextField().setText("Valid");
				return true;
			}
			
			
//			/**
//			 * Method getArgs
//			 * @param d ProjectionInfo
//			 * @return Object[]
//			 */
//			private Object[] getArgs(ProjectionInfo d) {
//				Object[] args=new Object[6];
//				args[0]=((EllipseData)getDataComboBox2().getSelectedItem()).getName();
//				if (getBooleanCheckBox().isSelected())
//					args[1]="auto";
//				else args[1]=getDataTextField1().getText();
//				args[2]=getDataTextField3().getText();
//				args[3]=getDataTextField6().getText();
//				args[4]=getDataTextField4().getText();
//				args[5]=getDataTextField5().getText();
//				return args;
//			}
			/**
			 * Method getProjection
			 * @param d ProjectionInfo
			 * @return GeoProjection
			 * @see anl.jeoviewer.gui.ProjectionInfo$ProjectionGUIAdaptor#getProjection(ProjectionInfo)
			 */
			
			public CoordinateReferenceSystem getProjection(ProjectionInfo d) {
				try {
					String coordString = createWKT();
					if(coordString==null)return null;
					
					return CRS.parseWKT(coordString);
				} catch (Exception ex) {
					System.err.println(getClass().getName()+">>getProjection() "+projectionClass);
					Logger.error("Error getting CRS: " + getClass().getName()+">>getProjection() "+projectionClass);
					ex.printStackTrace(System.err);
				}
				//return d.getProjection(GeoLambertCCProjection.class,getArgs(d));
				return null;
			}
			public String createWKT() throws IOException {
				VelocityContext context = new VelocityContext();
				String template = getClass().getPackage().getName();
				ClassLoader loader = Thread.currentThread().getContextClassLoader();
				Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
				template = template.replace('.', '/');
				template = template + "/tranMercator_full_wkt.vt";
				
				EllipseData data=(EllipseData)getDataComboBox2().getSelectedItem();
				String ellipseValue = data.getWKTString();
				
				Double cm=doubleValueOf(getDataTextField1().getText());
				Double trueLat=doubleValueOf(getDataTextField3().getText());
				Double fe=doubleValueOf(getDataTextField4().getText());
				Double fn=doubleValueOf(getDataTextField5().getText());
				Double scale=doubleValueOf(getDataTextField6().getText());
				//Double sp2=doubleValueOf(getDataTextField7().getText());
				
				context.put("geogcs", ellipseValue);
				context.put("central_meridian", cm.doubleValue());
				context.put("latitude_of_origin", trueLat.doubleValue());
				context.put("false_easting", fe.doubleValue());
				context.put("false_northing", fn.doubleValue());
				context.put("scale_factor", scale.doubleValue());

				Writer writer = new StringWriter();
				try {
					Velocity.mergeTemplate(template, "UTF-8", context, writer);
				} catch (Exception ex) {
					throw new IOException("Error merging template", ex);
				} finally {
					if (writer != null) writer.close();
					Thread.currentThread().setContextClassLoader(loader);
				}

				return writer.toString();
			}
		}

		
		/**
		 * XY adaptor
		 * @author Gordon Lurie
		 * @version $Revision: 1.3 $
		 */
		public class XYProjectionGUIAdaptor implements ProjectionGUIAdaptor {
			final Logger Logger = LogManager.getLogger(XYProjectionGUIAdaptor.class.getName());
			/**
			 * Constructor for GeoXYProjectionGUIAdaptor
			 */
			public XYProjectionGUIAdaptor() {
				super();
			}

			/**
			 * Method initialize
			 * @param d ProjectionInfo
			 * @see anl.jeoviewer.gui.ProjectionInfo$ProjectionGUIAdaptor#initialize(ProjectionInfo)
			 */
			public void initialize(ProjectionInfo d) {
				getDataComboBox2().setVisible(false); getBooleanCheckBox().setVisible(d.isAllowAutoCM());
				getDataLabel1().setVisible(false); getDataTextField1().setVisible(false);
				getDataLabel2().setVisible(false); getDataTextField2().setVisible(false);
				getDataLabel3().setVisible(false); getDataTextField3().setVisible(false);
				getDataLabel4().setVisible(false); getDataTextField4().setVisible(false);
				getDataLabel5().setVisible(false); getDataTextField5().setVisible(false);
				getDataLabel6().setVisible(false); getDataTextField6().setVisible(false);
				getDataLabel7().setVisible(false); getDataTextField7().setVisible(false);
				getBooleanCheckBox().setText("Auto Wrap");
			}

			

			/**
			 * Method validate
			 * @param d ProjectionInfo
			 * @return boolean
			 * @see anl.jeoviewer.gui.ProjectionInfo$ProjectionGUIAdaptor#validate(ProjectionInfo)
			 */
			public boolean validate(ProjectionInfo d) {
				// no parameters, always valid
				d.getStatusTextField().setText("Valid");
				return true;
			}

			
//			/**
//			 * Method getArgs
//			 * @param d ProjectionInfo
//			 * @return Object[]
//			 */
//			private Object[] getArgs(ProjectionInfo d) {
//				Object[] args=new Object[1];
//				if (getBooleanCheckBox().isSelected())
//					args[0]="auto";
//				else args[0]="";
//				return args;
//			}

			/**
			 * Method getProjection
			 * @param d ProjectionInfo
			 * @return GeoProjection
			 * @see anl.jeoviewer.gui.ProjectionInfo$ProjectionGUIAdaptor#getProjection(ProjectionInfo)
			 */
			
			public CoordinateReferenceSystem getProjection(ProjectionInfo d) {
				try {
					String coordString = createWKT();
					if(coordString==null)return null;
					
					return CRS.parseWKT(coordString);
				} catch (Exception ex) {
					System.err.println(getClass().getName()+">>getProjection() "+projectionClass);
					Logger.error("Error getting CRS: " + getClass().getName()+">>getProjection() "+projectionClass);
					ex.printStackTrace(System.err);
				}
				//return d.getProjection(GeoLambertCCProjection.class,getArgs(d));
				return null;
			}
			public String createWKT() throws IOException {
				
				VelocityContext context = new VelocityContext();
				String template = getClass().getPackage().getName();
				ClassLoader loader = Thread.currentThread().getContextClassLoader();
				Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
				template = template.replace('.', '/');
				template = template + "/latlon_full_wkt.vt";

				Writer writer = new StringWriter();
				try {
					Velocity.mergeTemplate(template, "UTF-8", context, writer);
				} catch (Exception ex) {
					throw new IOException("Error merging template", ex);
				} finally {
					if (writer != null) writer.close();
					Thread.currentThread().setContextClassLoader(loader);
				}

				return writer.toString();
				
			}

	}

		/**
		 * Adaptor interface
		 * @author Gordon Lurie
		 * @version $Revision: 1.3 $
		 */
		public interface ProjectionGUIAdaptor {
			/**
			 * Method initialize
			 * @param d ProjectionInfo
			 */
			public void initialize(ProjectionInfo d);
			/**
			 * Method validate
			 * @param d ProjectionInfo
			 * @return boolean
			 */
			public boolean validate(ProjectionInfo d);
			/**
			 * Method update
			 * @param d ProjectionInfo
			 * @param proj GeoProjection
			 */
			
			
			/**
			 * Method getProjection
			 * @param d ProjectionInfo
			 * @return GeoProjection
			 */
			public CoordinateReferenceSystem getProjection(ProjectionInfo d);
			public String createWKT() throws IOException;
		}
		// support to notify changes
		/**
		 * Field pcs
		 */
		private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
		// Properties that may changes
		/**
		 * Field PARAMETERS_CHANGED_PROPERTY
		 */
		public static final String PARAMETERS_CHANGED_PROPERTY="ParametersChanged";
		/**
		 * Field adaptors
		 */
		private HashMap adaptors=new HashMap();
		/**
		 * Field mod
		 */
		private boolean mod=false;
		/**
		 * Field projectionClass
		 */
		private int projectionClass=0;
		/**
		 * Field ivjDataLabel1
		 */
		private javax.swing.JLabel ivjDataLabel1 = null;
		/**
		 * Field ivjDataLabel2
		 */
		private javax.swing.JLabel ivjDataLabel2 = null;
		/**
		 * Field ivjDataLabel3
		 */
		private javax.swing.JLabel ivjDataLabel3 = null;
		/**
		 * Field ivjDataLabel4
		 */
		private javax.swing.JLabel ivjDataLabel4 = null;
		/**
		 * Field ivjDataLabel5
		 */
		private javax.swing.JLabel ivjDataLabel5 = null;
		/**
		 * Field ivjDataLabel6
		 */
		private javax.swing.JLabel ivjDataLabel6 = null;
		/**
		 * Field ivjDataLabel7
		 */
		private javax.swing.JLabel ivjDataLabel7 = null;
		/**
		 * Field ivjDataTextField1
		 */
		private javax.swing.JTextField ivjDataTextField1 = null;
		/**
		 * Field ivjDataTextField2
		 */
		private javax.swing.JTextField ivjDataTextField2 = null;
		/**
		 * Field ivjDataTextField3
		 */
		private javax.swing.JTextField ivjDataTextField3 = null;
		/**
		 * Field ivjDataTextField4
		 */
		private javax.swing.JTextField ivjDataTextField4 = null;
		/**
		 * Field ivjDataTextField5
		 */
		private javax.swing.JTextField ivjDataTextField5 = null;
		/**
		 * Field ivjDataTextField6
		 */
		private javax.swing.JTextField ivjDataTextField6 = null;
		/**
		 * Field ivjDataTextField7
		 */
		private javax.swing.JTextField ivjDataTextField7 = null;
		/**
		 * Field ivjEventHandler
		 */
		IvjEventHandler ivjEventHandler = new IvjEventHandler();
		/**
		 * Field ivjBooleanCheckBox
		 */
		private javax.swing.JCheckBox ivjBooleanCheckBox = null;
		/**
		 * Field ivjDataComboBox2
		 */
		private javax.swing.JComboBox ivjDataComboBox2 = null;
	/**
	 * Field StatusField
	 */
	private javax.swing.JTextField StatusField = null;

	/**
	 * Field allowAutoCM
	 */
	private boolean allowAutoCM = true;
	/**
	 * Field allowAutoZone
	 */
	private boolean allowAutoZone = true;

	/**
	 * Event handler
	 * @author Gordon Lurie
	 * @version $Revision: 1.3 $
	 */
	class IvjEventHandler implements java.awt.event.ItemListener, java.awt.event.KeyListener {
		final Logger Logger = LogManager.getLogger(IvjEventHandler.class.getName());
			/**
			 * Method itemStateChanged
			 * @param e java.awt.event.ItemEvent
			 * @see java.awt.event.ItemListener#itemStateChanged(ItemEvent)
			 */
			public void itemStateChanged(java.awt.event.ItemEvent e) {
				if (e.getSource() == ProjectionInfo.this.getBooleanCheckBox()) 
					connEtoC8(e);
				if (e.getSource() == ProjectionInfo.this.getDataComboBox2()) 
					connEtoC9(e);
			};
			/**
			 * Method keyPressed
			 * @param e java.awt.event.KeyEvent
			 * @see java.awt.event.KeyListener#keyPressed(KeyEvent)
			 */
			public void keyPressed(java.awt.event.KeyEvent e) {};
			/**
			 * Method keyReleased
			 * @param e java.awt.event.KeyEvent
			 * @see java.awt.event.KeyListener#keyReleased(KeyEvent)
			 */
			public void keyReleased(java.awt.event.KeyEvent e) {
				if (e.getSource() == ProjectionInfo.this.getDataTextField2()) 
					connEtoC1(e);
				if (e.getSource() == ProjectionInfo.this.getDataTextField1()) 
					connEtoC2(e);
				if (e.getSource() == ProjectionInfo.this.getDataTextField3()) 
					connEtoC3(e);
				if (e.getSource() == ProjectionInfo.this.getDataTextField4()) 
					connEtoC4(e);
				if (e.getSource() == ProjectionInfo.this.getDataTextField5()) 
					connEtoC5(e);
				if (e.getSource() == ProjectionInfo.this.getDataTextField6()) 
					connEtoC6(e);
				if (e.getSource() == ProjectionInfo.this.getDataTextField7()) 
					connEtoC7(e);
			};
			/**
			 * Method keyTyped
			 * @param e java.awt.event.KeyEvent
			 * @see java.awt.event.KeyListener#keyTyped(KeyEvent)
			 */
			public void keyTyped(java.awt.event.KeyEvent e) {};
		};

	/**
	 * Constructor for ProjectionInfo
	 */
	public ProjectionInfo() {
		super();
		initialize();
	}


	/**
	 * Method addPropertyChangeListener
	 * @param propertyName String
	 * @param listener PropertyChangeListener
	 */
	public synchronized void addPropertyChangeListener(
				String propertyName,
				PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(propertyName, listener);
	}


	/**
	 * User released a key
	 * @param keyEvent java.awt.event.KeyEvent
	 */
	public void any_KeyReleased(java.awt.event.KeyEvent keyEvent) {
		ProjectionGUIAdaptor adaptor=getAdaptor(projectionClass);
		if (adaptor==null) return;
		adaptor.validate(this);
		mod=true;
		update();
		firePropertyChange(PARAMETERS_CHANGED_PROPERTY,null);
		return;
	}


	/**
	 * Boolean checkbox changed
	 * @param itemEvent java.awt.event.ItemEvent
	 */
	public void booleanCheckBox_ItemStateChanged(java.awt.event.ItemEvent itemEvent) {
		mod=true;
		validate();
		update();
		firePropertyChange(PARAMETERS_CHANGED_PROPERTY,null);
		return;
	}


	/**
	 * connEtoC1:  (DataTextField2.key.keyReleased(java.awt.event.KeyEvent) --> JeoProjectionParameterDialog.any_KeyReleased(Ljava.awt.event.KeyEvent;)V)
	 * @param arg1 java.awt.event.KeyEvent
	 */
	/* WARNING: THIS METHOD WILL BE REGENERATED. */
	private void connEtoC1(java.awt.event.KeyEvent arg1) {
		try {
			// user code begin {1}
			// user code end
			this.any_KeyReleased(arg1);
			// user code begin {2}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {3}
			// user code end
			handleException(ivjExc);
		}
	}


	/**
	 * connEtoC2:  (DataTextField1.key.keyReleased(java.awt.event.KeyEvent) --> JeoProjectionParameterDialog.any_KeyReleased(Ljava.awt.event.KeyEvent;)V)
	 * @param arg1 java.awt.event.KeyEvent
	 */
	/* WARNING: THIS METHOD WILL BE REGENERATED. */
	private void connEtoC2(java.awt.event.KeyEvent arg1) {
		try {
			// user code begin {1}
			// user code end
			this.any_KeyReleased(arg1);
			// user code begin {2}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {3}
			// user code end
			handleException(ivjExc);
		}
	}


	/**
	 * connEtoC3:  (DataTextField3.key.keyReleased(java.awt.event.KeyEvent) --> JeoProjectionParameterDialog.any_KeyReleased(Ljava.awt.event.KeyEvent;)V)
	 * @param arg1 java.awt.event.KeyEvent
	 */
	/* WARNING: THIS METHOD WILL BE REGENERATED. */
	private void connEtoC3(java.awt.event.KeyEvent arg1) {
		try {
			// user code begin {1}
			// user code end
			this.any_KeyReleased(arg1);
			// user code begin {2}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {3}
			// user code end
			handleException(ivjExc);
		}
	}


	/**
	 * connEtoC4:  (DataTextField4.key.keyReleased(java.awt.event.KeyEvent) --> JeoProjectionParameterDialog.any_KeyReleased(Ljava.awt.event.KeyEvent;)V)
	 * @param arg1 java.awt.event.KeyEvent
	 */
	/* WARNING: THIS METHOD WILL BE REGENERATED. */
	private void connEtoC4(java.awt.event.KeyEvent arg1) {
		try {
			// user code begin {1}
			// user code end
			this.any_KeyReleased(arg1);
			// user code begin {2}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {3}
			// user code end
			handleException(ivjExc);
		}
	}


	/**
	 * connEtoC5:  (DataTextField5.key.keyReleased(java.awt.event.KeyEvent) --> JeoProjectionParameterDialog.any_KeyReleased(Ljava.awt.event.KeyEvent;)V)
	 * @param arg1 java.awt.event.KeyEvent
	 */
	/* WARNING: THIS METHOD WILL BE REGENERATED. */
	private void connEtoC5(java.awt.event.KeyEvent arg1) {
		try {
			// user code begin {1}
			// user code end
			this.any_KeyReleased(arg1);
			// user code begin {2}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {3}
			// user code end
			handleException(ivjExc);
		}
	}


	/**
	 * connEtoC6:  (DataTextField6.key.keyReleased(java.awt.event.KeyEvent) --> JeoProjectionParameterDialog.any_KeyReleased(Ljava.awt.event.KeyEvent;)V)
	 * @param arg1 java.awt.event.KeyEvent
	 */
	/* WARNING: THIS METHOD WILL BE REGENERATED. */
	private void connEtoC6(java.awt.event.KeyEvent arg1) {
		try {
			// user code begin {1}
			// user code end
			this.any_KeyReleased(arg1);
			// user code begin {2}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {3}
			// user code end
			handleException(ivjExc);
		}
	}


	/**
	 * connEtoC7:  (DataTextField7.key.keyReleased(java.awt.event.KeyEvent) --> JeoProjectionParameterDialog.any_KeyReleased(Ljava.awt.event.KeyEvent;)V)
	 * @param arg1 java.awt.event.KeyEvent
	 */
	/* WARNING: THIS METHOD WILL BE REGENERATED. */
	private void connEtoC7(java.awt.event.KeyEvent arg1) {
		try {
			// user code begin {1}
			// user code end
			this.any_KeyReleased(arg1);
			// user code begin {2}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {3}
			// user code end
			handleException(ivjExc);
		}
	}


	/**
	 * connEtoC8:  (BooleanCheckBox.item.itemStateChanged(java.awt.event.ItemEvent) --> JeoProjectionParameterDialog.booleanCheckBox_ItemStateChanged(Ljava.awt.event.ItemEvent;)V)
	 * @param arg1 java.awt.event.ItemEvent
	 */
	/* WARNING: THIS METHOD WILL BE REGENERATED. */
	private void connEtoC8(java.awt.event.ItemEvent arg1) {
		try {
			// user code begin {1}
			// user code end
			this.booleanCheckBox_ItemStateChanged(arg1);
			// user code begin {2}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {3}
			// user code end
			handleException(ivjExc);
		}
	}


	/**
	 * connEtoC9:  (DataComboBox2.item.itemStateChanged(java.awt.event.ItemEvent) --> JeoProjectionParameterDialog.dataComboBox2_ItemStateChanged(Ljava.awt.event.ItemEvent;)V)
	 * @param arg1 java.awt.event.ItemEvent
	 */
	/* WARNING: THIS METHOD WILL BE REGENERATED. */
	private void connEtoC9(java.awt.event.ItemEvent arg1) {
		try {
			// user code begin {1}
			// user code end
			this.dataComboBox2_ItemStateChanged(arg1);
			// user code begin {2}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {3}
			// user code end
			handleException(ivjExc);
		}
	}


	/**
	 * Data 2 changed
	 * @param itemEvent java.awt.event.ItemEvent
	 */
	public void dataComboBox2_ItemStateChanged(java.awt.event.ItemEvent itemEvent) {
		mod=true;
		update();
		firePropertyChange(PARAMETERS_CHANGED_PROPERTY,null);
		return;
	}


	/**
	 * MConvert string to a double
	 * @param str String
	 * @return Double
	 */
	public Double doubleValueOf(String str) {
		try {
			Double v=Double.valueOf(str);
			return v;
		}
		catch (NumberFormatException e) {return null;}
	}


	/**
	 * Report a bound property update to any registered listeners.
	 * No event is fired if old and new are equal and non-null.
	 *
	 * @param propertyName  The programmatic name of the property
	 *		that was changed.
	 * @param newValue  The new value of the property.
	 */
	private void firePropertyChange(String propertyName, Object newValue) {
		pcs.firePropertyChange(propertyName, null, newValue);
	}


	/**
	 * Get the adaptor for the current projection class
	 * @return ProjectionGUIAdaptor
	 */
//	public ProjectionGUIAdaptor getAdaptor() {
//		return getAdaptor(projectionClass);
//	}

	/**
	 * Get the adaptor for a class
	 * @param projType
	 * @return ProjectionGUIAdaptor
	 */
	public ProjectionGUIAdaptor getAdaptor(int projType) {
		return (ProjectionGUIAdaptor)adaptors.get(projType);
	}


	/**
	 * Return the BooleanJCheckBox property value.
	 * @return javax.swing.JCheckBox
	 */
	/* WARNING: THIS METHOD WILL BE REGENERATED. */
	private javax.swing.JCheckBox getBooleanCheckBox() {
		if (ivjBooleanCheckBox == null) {
			try {
				ivjBooleanCheckBox = new javax.swing.JCheckBox();
				ivjBooleanCheckBox.setName("BooleanCheckBox");
				ivjBooleanCheckBox.setText("BooleanCheckBox");
				// user code begin {1}
				// user code end
			} catch (java.lang.Throwable ivjExc) {
				// user code begin {2}
				// user code end
				handleException(ivjExc);
			}
		}
		return ivjBooleanCheckBox;
	}


	/**
	 * VisualAge VCE data
	 */
//	/* WARNING: THIS METHOD WILL BE REGENERATED. */
//	private static void getBuilderData() {
//	/*V1.1
//	**start of data**
//		D0CB838494G88G88GA0DA7EAEGGGGGGGGGGGG8CGGGE2F5E9ECE4E5F2A0E4E1F4E145DC8BD4D47719F7FBCC62B656B5BD36B5DD53D2C3DBEC5C2CEDC857F45DBD34EBDB5A522DCD1B561E101436F6EBDA1B35DB3A6B1E66F4AF8FA394840C13041C5084E48601C1DE728A223C86E5D01488A2D69299188B0C8EB3634C65E1036DFE7F47771FBBF36F9D40186AB91FF76E6FFB7C3F7F6B7BBFAF126975283DA929A6C9CA59AB71FF0DA912346C12A43D791D5FE6A216D1E84A12D47F0A008EC8
//		554D69704C875E26B9D3D61E343EB7835EAB60FD6D0CA96BE578FDD05ADCB4B288BF8C52E9821E59A45D9DB91D7B0BB01DFD747D714EECF866GD583B6G94D5C8527FF9E7BE476760BD077FA61412AAC9FDC5C4E627B5DF056F60F2E660B581559DA2B2FBED851C3F9630CDC03B01528FB15982F83202392B39094B8D5F37C6CA7EFAFF6AFE12164ABE52BC570D624FC269C9DA90E9528FBDF570EC693BB6F14261F1D7DE153DB3AEF9D676D7CECC3B1A1BFBE4EF1F5FFBD59ED3DCDECF1F436F18129559EFF3B9
//		5C5E093CE262EBCEC58635A3C95B4176BA0A5DDDC14AE7GFE3B011697233F5F5E5F427DD9F3ED0D745C588369C67EA695B3BF534DC1FFB3E6FF179AEBA676ABED59DC4F866F73C0ACAF75070EAEB9AFFD8E0F6C16D8DEE6EC1817BDE64C4BE70AC9DA5596D3D6B7500885716BE6024F417BEA20988B667178E14C6350BB8FCAC9D3E752F38D7298E7E1F95BE98966F1F772BD692F50B6E6A9A66D44DC8269828D83AD830A820A83FA15366D2422F2F8360FAA1D5E29A92F275D676BF0BB7D3DDE276C065F4D4D40
//		6158F9F94C6BF7CA92EB3F87ABAAF47CC15ADE925AF689313CDD14303DEDBC7FD1A9EE66E369FBF572B6B9753174DD5C06F4B818B765B0A937D5C7C8399542EF22E781E278860AEFD6612C2ED75236C45E33CB210F14E1B98F6520AFE752D7C9F9B5F3297BF4FCD97B2529D4E7B973E13EBC688BAB637FB313B20E81FCABD0A2D0B210498A7D500AE5DCCE5BD444B236FB209DF99C6E7E69D11F432FF4CC3B5CCE594F4B3A4E96292C750869F5ACD90F15CB4CD1F59DA5BEB22BDBAE09B14329FDD6D69E1356E039
//		30345FB1CCFB05D55BB6F2C49D3EC67B44BAAB2B43B40117D2FC238AE7F53B0976AD723E85F8059526ACA63F5FC2706595CC7E203073BA45E3D5B833B3CB6D13779D40DBF64414456A77019292C78BAA215DG7581CD822DG0A865ADC09F5BCFD28EA117D28CFD53FCC373278A5F84AF30A5FE177849407E7CC9EF478DD0ED1379C1038CCE3B1A95751903D965A3F47A00DD332C776BBC202D20049F4959FA0B2FE19F287DCD3B2A4B0651350EEEFB149335B91D0AE781C8EC58ECAB07E05E252A6E69C03E0918C
//		406F0CAAD2DE65B4E6EF077CA7D7E2D9FD7DB0414EG5FD90565322D7885F81E0646AD3B1B1BEDAE9AB99D7E9B58C6579C6906679C48E7D7C3EC822A2BB1E545838D57421885347CA8E0F5262CA4A0255E141551G7193283A917A9C50D013A9EB97503AB730DFBFBB046D57367FFE296209995D763B95E2CCF7B3EB0BA34D616D779F29DFABG0F810A875A8914F14C14D58A548DB4893482A886A89EE8A7D0C68B70013A01A601D6G453460187F410A429416B64917D19FA479A3790F54CFC9F99971C8D911F2
//		0924CF4A0C0E11F57AF224FC556F248E70B7A9F7D2D7116C133A5B55E4AC0375CC6A0C767D7A20ACAB2B27C4D965C6C807343B2C887C9C9E9B0CF8246D3F3E00FE91FF3EE840FFB56C7D006A779F0076AEE01F4CDF0AA370C9F910F6ED616FAF07657F0054D4C6E2D7BC34339DC06947316FE5198BA59A13BA9C81992350B9BF849059996C09CCB643C24AD91E11BD0ACD76499EA77C88709810EBA1697378B46E15B0F63CC8F1B2DEF5C2G89887CD54B8106038DF534C3FEB165BB94456F9A1DD664FED9D1DC1E
//		09203E157A9288998A83BC2F4D65A42F851047BAA01B48EB2C0554A54619B3DE6F35E95FG0920BCDEFD104E4B06C05EF79C7B2359CC6664EB44FBA5FD8F72BFDFBC4479116A6FD6092571AD1AC624B67A6E53B8E8B1CBAC7E25717608EDAA0B373FDC5EEE193C054AEF01FC6FG4ABE81E58DB48C349CE8A350F7011EBB0165F4533CFA11765F0BBCC64EFBB475EDA13F8E7E15FDE3ED7653A546FD6E61927DBE97DB225F673ED09239CFFD29C42FCFB15E57CAC859F8E7BD2ABE7A6FA5E45C9E73FABCEC31A3D4
//		A436CEFAF600644365F1A997DD4E89D969F77DC906313F157435F6BAB7B0436F26D69C13E70BF1CCB67F69C3D25C15B7D375667DEBBF69CBDD5D4A7ADBF4EBF0CCE6BEFD6BB0697B32DB1E02026A74CEFB9406BFC1679D9C8F24D12C64788A22DBEAF3A80E81A84613AE596D2C2D38A65F2884BA8F4290931D25D79DB30ECA472CD2C9EB217234FC230B7CA8ABBCF264C4E12415EB0543F3634AE91525B386260E158D16D5B31F46BBF4FC2A39C3BE55B01F1E16F5FCBAFA07FC2AE5BEBD552D63D35D9D7269A873
//		69D13F0ECF75F74827BA66537BAF687854F007FC2AE7BEE58E6A7854F807FCEAE0BEFD28EDBD7529436B85A5CF6724BCF62D43BBD761D26429CA72DF3F82AB1F4EC907E7C26B1A9D780B75EDB4B401AB76F05B614E8A63445BC6B6CE9D14265ABEA64A90161323DE30D5FB7B3DF52A4DAF5157A674356679FB5E23B8CBF7551696E639CF09A7F4FC3AADF1F669BE55F01FE65CBABE5D16B83BF41FEA39CFF60B0ECF37A54EAE5D27235C278D57F5FC3AADF1F669BE55F11F9E356A78F4DB626C52FD2A67BE8DDE52
//		7169364459257B5440FDEAFD69936FF11CE53E3D6BB04B7DFDE26A0177B44EB2EF5FE518656B5EA9AB197BEE05B971F6205D2D386EFD331470F15D4B97771DDE371B79823C49B6B2A77F559113EA1E1D51464E1C866049787ECABD7EA6417F35AEBFC758FF3DCA0F3FD568C76972378BFED1359EFF17602F5765CB6D48AF2E51631B84BFDA17DFAA78165A4879FBF3817E4D85789B9660BF31GBF65E8E47EA3CD1179BFDC001F7506911F353393E5F9708C485727E54F18CCB6D6A082609EF3598BE13CC107FBBA
//		38F75DDBC6565BEE551E8C435B4A7557F3A74B49BE4CB5E13F277CF9096CC1B31BEC7D3949C1EC7A7C2EA9077F8659E462E7239BEC6A33519628DFF20EBA82CF259D4F78BE6FA0FB7A1166C6559D44565B96BCC3DD893A51C0DB393DBD6D2CEC185C6BA5A8B78F6FEBBA0C643EA364E261BD41D06E1E43A8378B5E53BB0D640E15221C99E4DA8C657ED3488D030CE2A877F0994A2D81190D06F22365A817GB2BB0052EDCC2E4D261633D8C579819E8D34154BC9DDCC2E3B0B488D09735FAA01F9A9F69FD47C3967
//		71DC2AB7CFFDD63D71EBDBF54F8EE9FA5CFED4D778F95DFAEAB78E70EDDD98C337162B778E5B83816F180B9E1770F69A302A779798B6E77DBDBC47DD7E00E277B865B9EC6BFF3692DF49F98A07F9FBFD48FADC521E8FF5B9DD0A570FDB331D33B4708FF8BB3DCE399B3ACB2758FE99F44933204F4E18CBAB313CF6F4617E4A4D136AF371AD39EF27E69914CD41C9D6A6ADA703E5D3711BFB52196D8D4276BC7099B6D805D82C403EAF30DD829B2AC64CFC8A319F88ECD8E0FF2CC1EC0D401E94D842A96C273FAEA3
//		FD191E4513C1F22EB2B06B72CCB4B777500115699D6C4A60E51C5E0D676A2D5DA42E34407BG109B081D715DC34F7E086CF24036F4E36C0FADD6473E64EE36C7DCE0B7E531F9423316D2960F96B535E23EEFFB598A4FA54C22F89BDFFF308D7B56432B24163F34266A1D7D2F483527D65BD97D8D5903753879633FCBCD78B85EF168AE417CC603CC229D733B2BDCAF3F269EAC276704DEA9E0A3BD287739A2BD3D35C2EF53E1545B88588E217761FA5D72BD0DFA4ED2542B86EC72B46A7DC4B73DA82117DF06FA31
//		006D92FA17BB74744CE7D06F7BC22F89B05F99543B296BE70C503B379C7536G16AC74F6352A75AAFA195EC0AF361F7FAAA7F1ED0973CA9EB7FE90A0F163DD4DF8A5BC63BEEBC57FA7413728DE74DFE9D47B3F1D7BAF1DC57F9F355E3A7FC57EE370FC97F3E03CB7D22B0E7D89F37A77C688194E327ED0FAB6BC765FCDE31B8D702133987B7F57CA764DB1761F07E88BAE418CC56C51D724313E4F5F9F2AA06952390804E78CC7AA17F24F42FD964F4B7F0D4E0917417BBA20D8208420DD42375AA38B5E9B691A0B
//		F0EF6461A57915FD8E4FF3AED7ADC52FD068A5AEC92FCE683DD53D943DEE217774127406055E151A25687904DE521274168B3D7C2EF03D356702E70A9B4FE94F9759DD15AABB692FF16758DD15A201F7D3BC3BAF08337E5DC547BD72BE84BC671B187E87AC61697B4E072639B26C1D697D3DC66F353077CDE1FA2C4FDB68FCD3E75C969B95EC6C7EC5B10E79716731CF3CD2C3F425FE989F006A007A00942055C0317D58B77E66085EFCAA34EF98CD26026751AC1FE7CBEE2DFC06B5FA6165735D7ED0BD166FE22B
//		18BB75E39D259E3EB59F72B4FA61BE188774FCF82688FDE89990675D3768435685FDD0F4FD78E6BD7A30D278F0357456FC185368057B30D55707A4B37A10A8FCF82D6C56FC285068057BE03A20674387BB50070A8B6843D3376843AF967441296BC32C280BF96143FD6537664347B5FA61BE44DE500B9BA786C95F56099BB86F417E5306F37E84616B007556FC9D5568057B1ABE28676B045B48D7BE4D613EBE540865DAB008B16ED2AD4186605DB908716C69A364BE824EC37A3CAE0FA2619C645ECA327F61753B
//		A6DC9E44D6D3EC4C6B2707766C0E0B0D4EEF943039DA587DC615719D03472B746F98EC2F523FE370F4D564FBBD7FD1E5F487017977E561DFB490437CD508ED96582F8516A0B0259A31A4017DC6E049821B29C1AC63A2E27FA3308201D551F5E3B53C5B8416FD0AE013703E6CA2362BFAFB9BBC2361FD8B50F620E4206CA1D8CFG35G8DG39011683C581ED815A8E148C14F74914D5F189ED1D25363AAF31BDA232EFB2893FD7G45G45836D844A78A35BFBC960FBBFADFC4F066CBF11BD94E66BF43714925ACE
//		89917F4231F00A0E1087626BC27A371BC8399A155DC23A2CACD66916453EC524CD7A2551FD2022C5685FEEA231055CB3A4F77CDE617D0A5CA1DC680EE28E17CB133E769249038F3A2D9C2093A0F23841F9BFD308ED4D1D1E330EA9D942B3112D74CE9F1169D23C1DB5220CF797C71A7B167ED1DBD77C2EDF0DF17CA92F510FBF55B524AE825EE97F189C9EE39AEB7285AFB823A38BC6128D3E2FCC759507FFC2564428D3D41F710C747BEA0C6298AB233BCA54E5D4AB4A28D4B3162829C5270C183DB78E2B6D9D95
//		761EAD0AE42F4F505EAFC36C5589FB0F57C73267B4347709D2353DFAE16F2766C876668C6DDDA8D35BEB9076BE5291495ECAC3FB26F2353DC6E16F3108790DB234979FE22FA9D83FEDFA76E2391DF8C3FB77DA55765E9076B6B5C672AF5130CF3DDDCB56FDF362FD143E5F10F0EEA23793D956463BBCCEBE19E07B6A1769981C158E6DDBC2AC11E219003DA1303F25E370BE409E93586F2B91FBC6E0778A6CC08AE259B5E86F3840BEC031DEF030DD686650F9D0ABE0FF91E5F5320B4CEB5C0E9B5EE970F978B211
//		FB2B8B4FF4F6C319E4GD68DE43B4C7629D81EBFBBCBE278385FB185DD355FA70FC9CD43F42EF88AF523C1FEB350F620BDC07943383F35AA156C6513C0F946B1AA3BEB50977F319C0E87EF11A06FB21DD7051DF4F35E16ABA176443D1E07B3C270DA44A5E788FE94719F0F0560F5087F3CA9842FC77C1143A1F8836257AEEB72A1FCCA685270C47A53A7B5BC61032DD94393FE9C4F567004AF26DE8DCF1443FD7E7D1CA7768AA5BC1FDCE5D76FD1AE9BE6F59FBD0CFB181F485063EF967C773D2447CF907C8D73FA
//		7CA441FFFF0E9EBFD9704B5CFA7C0C37107F16CD0FDF70964EBD7719F16ED9AD30D62B180F8AEC6FA14436896C19FA448A5FC66CB9E14FA73007BB900B9358B6211BF7C524CB57D7A3706E93D8D49B4A4D8B6C2B0D08A5DC41BD723B3A703B4A029118D70EE07F2C99895993A21B3D4D4D277CAEE707E3628C6D6DFCCF004E4B097E4A91BC4F79AD3539955E930112C7704CEB2E844F3CB6FEEA1514B12E7FADE852405B29598EFEAE69886E89A7A66319177D94266986FE3403FE8FF6CADDDF24FDDE8701CA92C7
//		A8FFC3881F4C974AA4B57F4990BE7177E21A1A3F3FDB4D5F2B52E7F97B068573765CB756C96EC7E5436FF2F771BC650D32274F7FCE4A7C0E0AECFE06B6EEF006C6C3844B7F176D381E6E9E4573386D9908AD9358E3AFA136D9E0C973086D965897F390AB9DC3EC440D580840565890DB8BD83E13E02F19EDBCDF5B467447D33C4BCF4FBFC1E68FD0B147321DECCF149033B7E8C5FBA3CEFDFBA9AA3B0AB3545E8A0D3D02C3E8EF0D013D3DAA7FE2426CED5158139A50DE3C013DFDAAFB09E176F6EB6C9D34203D67
//		8C6CE52A6C654921764CF2383DAFF7203DEAD95FDE164A1EAD4C5E10465E4F6A511E53405EFE153DB910D989E461D8144A9E9BB3765849DD918546120DB2796E7EB1B20561E97DD39B2635DBA72D3DD2E85D6FD135A502270F077B7EF4A35A4B9BDF38ED1A47193D97B95624324766FF77EA4E8596A226F77FAD6ABD3EE4BDF65E704D13A47E5AD47EB37CFB946FD361EC8CC8B909E340B0706246F15FEBDDB37D9EFD42145586B4846483DA8D34F182772BFED6DBADAD302FBF60751D018525DB35854577D7CE5D
//		5A774EDF5C0731B6768B9F16B65FEDB13CBB91B7413F1B644F0C6B6F2410A77FB62D0564AFEF5214D5815487B489B48F34E69273D7F53402650FDC7DE8D7944758A43DD50219A397BD02B01948323CFD5D2E173718C9664716C9EDFB647B4899FAFA5B387C8EC3BDF33E1E5E9EAE1F6DB2527B693C1EDEA1172FB3547BCA0E1EDEB7179FB654135DFAFABEAE3F5CD06FFE5D6F5357F2790DBAFAEC4CBAE746B1AB8EE4DE1758DED106047FF637EAB130C7E3532BA7A45166FE140DED2E60213F13EC5F3724E25C51
//		14B977257BEAF06E32EE64DF5218BF77D845B8FDD55F9F1676DD2D1248C36FA1B10EC754F934AD28D353A0467908BAD59DC19D1BC54C832EC552B95F9954796F8EB1CF0828B3ED8B6A648B5F569A68985B79072EE01DFC6B327A0EC15EC7FBD3B12EE97A4FB5D697095702F542FC59A94EA6725C9057E89AAF1E8A7A1A5BA466DA6EC879BBCBBEBF67ED75BBA7033F93D53F771D9C52716DF691CB2370BDCD034544CFB69B6F91EEEC565FA37CDC337E99456666485FC146B59BFF877955E675F710AC263FC6571D
//		4B202E56814581FD97E8279B63794831C3920D67BA313C4BB9C176A849DFF4D0B07D4E96633C7634AC7CF7A07ADB489AFF542BA85EA9E29F71CBAD243E942F0F02FC1F280514115FB5B129B0D10EBBDBC8FBF34B6394E6EB399F59F14D17BEA55610BDE2EDA830648C149B915805CC14F38B6CD8BE4AED91583AFC140B8F26B10FF2799E445E3A09F2E601BD1503F20A40CE640A753140665DA837AD286BC639C401ED3221DC05973183DDA8576495670AA1FB1CABGDF670DFCAE415A4DFC8F89D01BC0B681E8B7
//		C943876BFD20BA20EE20E1A09F508A20A820D820F820C420DDC052F5D316693AE8E7A7487AF050F1D5FBF394C03E10260D4D6FE0E7074C7F15A1FB5225E02F65FAE47F195E79C3EA3DBE50F1AECA2F21C12DB787BAAB9725E78F39EB9285BA310B527BED481EF1BC68A4AECA6F8FA1FE6E861DF47FE26A754727A5510FFE55E5B12A93687DBD305CBEB7CA6E178FB8A6C43A3F8C59C34E07B4AB96C81769191A54FAED20B330009E733144EE9CEF0E58970EB78DB4AE4890A786EE78787948B1BBBD0F9E9F8F48A2
//		2E35597384E68F74F983AEF22E4246320EDED2E6FCFDFA9A57D013BA7E2B57F6FAEB32F9FFF02D41EC4BE703365D19E8FB05016D7DE1365779595A1270B6E9EC2FB897347DC8BE5A5EE2E0BBAD025F5BB4369F6A8B5AD6EE226DC4835BFBA3585E2D31BDFBBEE87BC22E588FB0303DAF026D3CC0386DE77A0336135DE83BA020EFBBB3026DEA0D6D6C8BC15B25DDE83B4540F6D6845BFDAA5BAC36DF237376E10EB34CC7B105E37CDEC50FF1FF79C84F427DA52AC7FFFC7ED40FF6FCFE28C7FFFC7EEC0FFAFCE658
//		67FBC89B1800B8BFA4647ED9E0178496C7ED926C224B29CC8A7CAB54AF02F74BA4C1449328DF33949B625837D558A50EFD2F0774F9487224570F3ACF76103D17F1175B4D5EFFC8E5DC9E888122BCF6D09FDDBE074A6FE784F6C9544503D6ED7D44D8557543B057A12D1C6710D62E37C1ABF73EC1ABB7E2514A49962D5C8B1DDA3917BB35F2F5BA693E2113EEFA3BD6AE33DDABE7ED524AD5B7296578DA6BB42EABD687F05DD1C52EFEF37E8319480F8FC4DC0BDD8C6A6C48C79DC9C11B53C3C17EA7EFA23FC97057
//		DE8A726F4EC53ECF70138E867916A9644788FE5A0BC17EA571EDC712605710AE48632103FE27C07E86094B61264D1D6185F37B03B8B6F71CE71C8E99FF27E17C1DDE4C783BBEB3E47C851D01C569BD93320FB289BA730B527B4CCD355EEA5009DE145E3D39EA3D4D201330A83D72A935DE9268A4AFCA6F6D10BD450C9918634EC456637D6E34CE3FBB2DED7F0FE5EA657EB5D3ABA73D22155B7B0AD66E0BB735F2DF392915DB1F2B157BF42ED62EFDCAABF7F2CAABB75D25157BD317DA0E7D2DD9F7D3E6AA0C073F
//		1476AAD2EE5871F240AF650AAFA56CB44CA64D9CEDCE7B4903E929D23ADDFA1E8DBBF6A91D0C9DF6E9AF9DDA6CD29A8CBBF364CF7E11E4D2B925842ECBFF96932EBB0DCAAAD45217D5245F870F6855AF0D6A17DA754BD175CB1D7A25DE7D52308417537F3A85A4615ED459DF54FBD3FB6E32697E7DA4C249F3FCEF7AC67877AF35476877AF000F5C40F5657B1A0FC846EB187EA9075B2DB85C575ABDCE321E69382148413D14C6FABFA200B21C493F97BEC67C3E2A5279FFD0CB8788A6A38AC44698GG28D9GG
//		D0CB818294G94G88G88GA0DA7EAEA6A38AC44698GG28D9GG8CGGGGGGGGGGGGGGGGGE2F5E9ECE4E5F2A0E4E1F4E1D0CB8586GGGG81G81GBAGGGG99GGGG
//	**end of data**/
//	}
	/**
	 * Gets the the projection instance.
	 * @param projectionClass The projectionClass to set
	 * @param args Object[]
	 * @return GeoProjection
	 */
//	private CoordinateReferenceSystem getProjection(Class projectionClass, Object[] args) {
//		try {
////			Class[] classes=null;
////			if (args!=null && args.length>0) {
////				classes=new Class[args.length];
////				for (int idx=0;idx<args.length;idx++)
////					classes[idx]=args[idx].getClass();
////			}
////			Constructor c=projectionClass.getConstructor(classes);
////			return (GeoProjection)c.newInstance(args);
//		}
//		catch (Exception e) {
//			System.err.println(getClass().getName()+">>getProjection() error getting projection instances "+projectionClass);
//			e.printStackTrace(System.err);
//		}
//		return null;
//	}

	/**
	 * Return the DataComboBox2 property value.
	 * @return javax.swing.JComboBox
	 */
	/* WARNING: THIS METHOD WILL BE REGENERATED. */
	private javax.swing.JComboBox getDataComboBox2() {
		if (ivjDataComboBox2 == null) {
			try {
				ivjDataComboBox2 = new javax.swing.JComboBox();
				ivjDataComboBox2.setName("DataComboBox2");
				// user code begin {1}
				// user code end
			} catch (java.lang.Throwable ivjExc) {
				// user code begin {2}
				// user code end
				handleException(ivjExc);
			}
		}
		return ivjDataComboBox2;
	}


	/**
	 * Return the DataLabel1 property value.
	 * @return javax.swing.JLabel
	 */
	/* WARNING: THIS METHOD WILL BE REGENERATED. */
	private javax.swing.JLabel getDataLabel1() {
		if (ivjDataLabel1 == null) {
			try {
				ivjDataLabel1 = new javax.swing.JLabel();
				ivjDataLabel1.setName("DataLabel1");
				ivjDataLabel1.setText("DataLabel1");
				// user code begin {1}
				// user code end
			} catch (java.lang.Throwable ivjExc) {
				// user code begin {2}
				// user code end
				handleException(ivjExc);
			}
		}
		return ivjDataLabel1;
	}


	/**
	 * Return the DataLabel2 property value.
	 * @return javax.swing.JLabel
	 */
	/* WARNING: THIS METHOD WILL BE REGENERATED. */
	private javax.swing.JLabel getDataLabel2() {
		if (ivjDataLabel2 == null) {
			try {
				ivjDataLabel2 = new javax.swing.JLabel();
				ivjDataLabel2.setName("DataLabel2");
				ivjDataLabel2.setText("DataLabel2");
				// user code begin {1}
				// user code end
			} catch (java.lang.Throwable ivjExc) {
				// user code begin {2}
				// user code end
				handleException(ivjExc);
			}
		}
		return ivjDataLabel2;
	}


	/**
	 * Return the DataLabel3 property value.
	 * @return javax.swing.JLabel
	 */
	/* WARNING: THIS METHOD WILL BE REGENERATED. */
	private javax.swing.JLabel getDataLabel3() {
		if (ivjDataLabel3 == null) {
			try {
				ivjDataLabel3 = new javax.swing.JLabel();
				ivjDataLabel3.setName("DataLabel3");
				ivjDataLabel3.setText("DataLabel3");
				// user code begin {1}
				// user code end
			} catch (java.lang.Throwable ivjExc) {
				// user code begin {2}
				// user code end
				handleException(ivjExc);
			}
		}
		return ivjDataLabel3;
	}


	/**
	 * Return the DataLabel4 property value.
	 * @return javax.swing.JLabel
	 */
	/* WARNING: THIS METHOD WILL BE REGENERATED. */
	private javax.swing.JLabel getDataLabel4() {
		if (ivjDataLabel4 == null) {
			try {
				ivjDataLabel4 = new javax.swing.JLabel();
				ivjDataLabel4.setName("DataLabel4");
				ivjDataLabel4.setText("DataLabel4");
				// user code begin {1}
				// user code end
			} catch (java.lang.Throwable ivjExc) {
				// user code begin {2}
				// user code end
				handleException(ivjExc);
			}
		}
		return ivjDataLabel4;
	}


	/**
	 * Return the DataLabel5 property value.
	 * @return javax.swing.JLabel
	 */
	/* WARNING: THIS METHOD WILL BE REGENERATED. */
	private javax.swing.JLabel getDataLabel5() {
		if (ivjDataLabel5 == null) {
			try {
				ivjDataLabel5 = new javax.swing.JLabel();
				ivjDataLabel5.setName("DataLabel5");
				ivjDataLabel5.setText("DataLabel5");
				// user code begin {1}
				// user code end
			} catch (java.lang.Throwable ivjExc) {
				// user code begin {2}
				// user code end
				handleException(ivjExc);
			}
		}
		return ivjDataLabel5;
	}


	/**
	 * Return the DataLabel6 property value.
	 * @return javax.swing.JLabel
	 */
	/* WARNING: THIS METHOD WILL BE REGENERATED. */
	private javax.swing.JLabel getDataLabel6() {
		if (ivjDataLabel6 == null) {
			try {
				ivjDataLabel6 = new javax.swing.JLabel();
				ivjDataLabel6.setName("DataLabel6");
				ivjDataLabel6.setText("DataLabel6");
				// user code begin {1}
				// user code end
			} catch (java.lang.Throwable ivjExc) {
				// user code begin {2}
				// user code end
				handleException(ivjExc);
			}
		}
		return ivjDataLabel6;
	}


	/**
	 * Return the DataLabel7 property value.
	 * @return javax.swing.JLabel
	 */
	/* WARNING: THIS METHOD WILL BE REGENERATED. */
	private javax.swing.JLabel getDataLabel7() {
		if (ivjDataLabel7 == null) {
			try {
				ivjDataLabel7 = new javax.swing.JLabel();
				ivjDataLabel7.setName("DataLabel7");
				ivjDataLabel7.setText("DataLabel7");
				// user code begin {1}
				// user code end
			} catch (java.lang.Throwable ivjExc) {
				// user code begin {2}
				// user code end
				handleException(ivjExc);
			}
		}
		return ivjDataLabel7;
	}


	/**
	 * Return the DataTextField1 property value.
	 * @return javax.swing.JTextField
	 */
	/* WARNING: THIS METHOD WILL BE REGENERATED. */
	private javax.swing.JTextField getDataTextField1() {
		if (ivjDataTextField1 == null) {
			try {
				ivjDataTextField1 = new javax.swing.JTextField();
				ivjDataTextField1.setName("DataTextField1");
				// user code begin {1}
				// user code end
			} catch (java.lang.Throwable ivjExc) {
				// user code begin {2}
				// user code end
				handleException(ivjExc);
			}
		}
		return ivjDataTextField1;
	}


	/**
	 * Return the DataTextField2 property value.
	 * @return javax.swing.JTextField
	 */
	/* WARNING: THIS METHOD WILL BE REGENERATED. */
	private javax.swing.JTextField getDataTextField2() {
		if (ivjDataTextField2 == null) {
			try {
				ivjDataTextField2 = new javax.swing.JTextField();
				ivjDataTextField2.setName("DataTextField2");
				// user code begin {1}
				// user code end
			} catch (java.lang.Throwable ivjExc) {
				// user code begin {2}
				// user code end
				handleException(ivjExc);
			}
		}
		return ivjDataTextField2;
	}


	/**
	 * Return the DataTextField3 property value.
	 * @return javax.swing.JTextField
	 */
	/* WARNING: THIS METHOD WILL BE REGENERATED. */
	private javax.swing.JTextField getDataTextField3() {
		if (ivjDataTextField3 == null) {
			try {
				ivjDataTextField3 = new javax.swing.JTextField();
				ivjDataTextField3.setName("DataTextField3");
				// user code begin {1}
				// user code end
			} catch (java.lang.Throwable ivjExc) {
				// user code begin {2}
				// user code end
				handleException(ivjExc);
			}
		}
		return ivjDataTextField3;
	}


	/**
	 * Return the DataTextField4 property value.
	 * @return javax.swing.JTextField
	 */
	/* WARNING: THIS METHOD WILL BE REGENERATED. */
	private javax.swing.JTextField getDataTextField4() {
		if (ivjDataTextField4 == null) {
			try {
				ivjDataTextField4 = new javax.swing.JTextField();
				ivjDataTextField4.setName("DataTextField4");
				// user code begin {1}
				// user code end
			} catch (java.lang.Throwable ivjExc) {
				// user code begin {2}
				// user code end
				handleException(ivjExc);
			}
		}
		return ivjDataTextField4;
	}


	/**
	 * Return the DataTextField5 property value.
	 * @return javax.swing.JTextField
	 */
	/* WARNING: THIS METHOD WILL BE REGENERATED. */
	private javax.swing.JTextField getDataTextField5() {
		if (ivjDataTextField5 == null) {
			try {
				ivjDataTextField5 = new javax.swing.JTextField();
				ivjDataTextField5.setName("DataTextField5");
				// user code begin {1}
				// user code end
			} catch (java.lang.Throwable ivjExc) {
				// user code begin {2}
				// user code end
				handleException(ivjExc);
			}
		}
		return ivjDataTextField5;
	}


	/**
	 * Return the DataTextField6 property value.
	 * @return javax.swing.JTextField
	 */
	/* WARNING: THIS METHOD WILL BE REGENERATED. */
	private javax.swing.JTextField getDataTextField6() {
		if (ivjDataTextField6 == null) {
			try {
				ivjDataTextField6 = new javax.swing.JTextField();
				ivjDataTextField6.setName("DataTextField6");
				// user code begin {1}
				// user code end
			} catch (java.lang.Throwable ivjExc) {
				// user code begin {2}
				// user code end
				handleException(ivjExc);
			}
		}
		return ivjDataTextField6;
	}


	/**
	 * Return the DataTextField7 property value.
	 * @return javax.swing.JTextField
	 */
	/* WARNING: THIS METHOD WILL BE REGENERATED. */
	private javax.swing.JTextField getDataTextField7() {
		if (ivjDataTextField7 == null) {
			try {
				ivjDataTextField7 = new javax.swing.JTextField();
				ivjDataTextField7.setName("DataTextField7");
				// user code begin {1}
				// user code end
			} catch (java.lang.Throwable ivjExc) {
				// user code begin {2}
				// user code end
				handleException(ivjExc);
			}
		}
		return ivjDataTextField7;
	}


		/**
		 * @return Class
		 */
		public int getProjectionClass() {
			return projectionClass;
		}


	/**
	 * Return status field
	 * @return javax.swing.JTextField
	 */
	public javax.swing.JTextField getStatusTextField() {
		return StatusField;
	}


	/**
	 * Called whenever the part throws an exception.
	 * @param exception java.lang.Throwable
	 */
	private void handleException(java.lang.Throwable exception) {

		Logger.error("--------- EXCEPTION in anl.verdi.area.target.ProjectInfo ---------");
		exception.printStackTrace(); 				// 2014 sends to stderr (normal)
		exception.printStackTrace(System.out);
	}


	/**
	 * Initializes connections
	 * @exception java.lang.Exception The exception description.
	 */
	/* WARNING: THIS METHOD WILL BE REGENERATED. */
	private void initConnections() throws java.lang.Exception {
		// user code begin {1}
		// user code end
		getDataTextField2().addKeyListener(ivjEventHandler);
		getDataTextField1().addKeyListener(ivjEventHandler);
		getDataTextField3().addKeyListener(ivjEventHandler);
		getDataTextField4().addKeyListener(ivjEventHandler);
		getDataTextField5().addKeyListener(ivjEventHandler);
		getDataTextField6().addKeyListener(ivjEventHandler);
		getDataTextField7().addKeyListener(ivjEventHandler);
		getBooleanCheckBox().addItemListener(ivjEventHandler);
		getDataComboBox2().addItemListener(ivjEventHandler);
	}


	/**
	 * Initialize the class.
	 */
	/* WARNING: THIS METHOD WILL BE REGENERATED. */
	private void initialize() {
		try {
			// user code begin {1}
			// user code end
			setName("JeoProjectionParameterDialog");
			setLayout(new java.awt.GridBagLayout());
			setSize(427, 184);

			java.awt.GridBagConstraints constraintsDataLabel1 = new java.awt.GridBagConstraints();
			constraintsDataLabel1.gridx = 0; constraintsDataLabel1.gridy = 0;
			constraintsDataLabel1.anchor = java.awt.GridBagConstraints.WEST;
			constraintsDataLabel1.insets = new java.awt.Insets(1, 2, 1, 2);
			add(getDataLabel1(), constraintsDataLabel1);

			java.awt.GridBagConstraints constraintsDataTextField1 = new java.awt.GridBagConstraints();
			constraintsDataTextField1.gridx = 1; constraintsDataTextField1.gridy = 0;
			constraintsDataTextField1.fill = java.awt.GridBagConstraints.HORIZONTAL;
			constraintsDataTextField1.anchor = java.awt.GridBagConstraints.WEST;
			constraintsDataTextField1.weightx = 1.0;
			constraintsDataTextField1.insets = new java.awt.Insets(1, 2, 1, 2);
			add(getDataTextField1(), constraintsDataTextField1);

			java.awt.GridBagConstraints constraintsBooleanCheckBox = new java.awt.GridBagConstraints();
			constraintsBooleanCheckBox.gridx = 2; constraintsBooleanCheckBox.gridy = 0;
			constraintsBooleanCheckBox.anchor = java.awt.GridBagConstraints.WEST;
			constraintsBooleanCheckBox.insets = new java.awt.Insets(0, 2, 0, 2);
			add(getBooleanCheckBox(), constraintsBooleanCheckBox);

			java.awt.GridBagConstraints constraintsDataLabel7 = new java.awt.GridBagConstraints();
			constraintsDataLabel7.gridx = 0; constraintsDataLabel7.gridy = 6;
			constraintsDataLabel7.anchor = java.awt.GridBagConstraints.NORTHWEST;
			constraintsDataLabel7.weighty = 1.0;
			constraintsDataLabel7.insets = new java.awt.Insets(4, 2, 1, 2);
			add(getDataLabel7(), constraintsDataLabel7);

			java.awt.GridBagConstraints constraintsDataLabel2 = new java.awt.GridBagConstraints();
			constraintsDataLabel2.gridx = 0; constraintsDataLabel2.gridy = 1;
			constraintsDataLabel2.anchor = java.awt.GridBagConstraints.WEST;
			constraintsDataLabel2.insets = new java.awt.Insets(1, 2, 1, 2);
			add(getDataLabel2(), constraintsDataLabel2);

			java.awt.GridBagConstraints constraintsDataLabel3 = new java.awt.GridBagConstraints();
			constraintsDataLabel3.gridx = 0; constraintsDataLabel3.gridy = 2;
			constraintsDataLabel3.anchor = java.awt.GridBagConstraints.WEST;
			constraintsDataLabel3.insets = new java.awt.Insets(1, 2, 1, 2);
			add(getDataLabel3(), constraintsDataLabel3);

			java.awt.GridBagConstraints constraintsDataLabel4 = new java.awt.GridBagConstraints();
			constraintsDataLabel4.gridx = 0; constraintsDataLabel4.gridy = 3;
			constraintsDataLabel4.anchor = java.awt.GridBagConstraints.WEST;
			constraintsDataLabel4.insets = new java.awt.Insets(1, 2, 1, 2);
			add(getDataLabel4(), constraintsDataLabel4);

			java.awt.GridBagConstraints constraintsDataLabel5 = new java.awt.GridBagConstraints();
			constraintsDataLabel5.gridx = 0; constraintsDataLabel5.gridy = 4;
			constraintsDataLabel5.anchor = java.awt.GridBagConstraints.WEST;
			constraintsDataLabel5.insets = new java.awt.Insets(1, 2, 1, 2);
			add(getDataLabel5(), constraintsDataLabel5);

			java.awt.GridBagConstraints constraintsDataLabel6 = new java.awt.GridBagConstraints();
			constraintsDataLabel6.gridx = 0; constraintsDataLabel6.gridy = 5;
			constraintsDataLabel6.anchor = java.awt.GridBagConstraints.WEST;
			constraintsDataLabel6.insets = new java.awt.Insets(1, 2, 1, 2);
			add(getDataLabel6(), constraintsDataLabel6);

			java.awt.GridBagConstraints constraintsDataTextField7 = new java.awt.GridBagConstraints();
			constraintsDataTextField7.gridx = 1; constraintsDataTextField7.gridy = 6;
			constraintsDataTextField7.gridwidth = 2;
			constraintsDataTextField7.fill = java.awt.GridBagConstraints.HORIZONTAL;
			constraintsDataTextField7.anchor = java.awt.GridBagConstraints.NORTHWEST;
			constraintsDataTextField7.weightx = 1.0;
			constraintsDataTextField7.weighty = 1.0;
			constraintsDataTextField7.insets = new java.awt.Insets(1, 2, 1, 2);
			add(getDataTextField7(), constraintsDataTextField7);

			java.awt.GridBagConstraints constraintsDataTextField3 = new java.awt.GridBagConstraints();
			constraintsDataTextField3.gridx = 1; constraintsDataTextField3.gridy = 2;
			constraintsDataTextField3.gridwidth = 2;
			constraintsDataTextField3.fill = java.awt.GridBagConstraints.HORIZONTAL;
			constraintsDataTextField3.anchor = java.awt.GridBagConstraints.WEST;
			constraintsDataTextField3.weightx = 1.0;
			constraintsDataTextField3.insets = new java.awt.Insets(1, 2, 1, 2);
			add(getDataTextField3(), constraintsDataTextField3);

			java.awt.GridBagConstraints constraintsDataTextField4 = new java.awt.GridBagConstraints();
			constraintsDataTextField4.gridx = 1; constraintsDataTextField4.gridy = 3;
			constraintsDataTextField4.gridwidth = 2;
			constraintsDataTextField4.fill = java.awt.GridBagConstraints.HORIZONTAL;
			constraintsDataTextField4.anchor = java.awt.GridBagConstraints.WEST;
			constraintsDataTextField4.weightx = 1.0;
			constraintsDataTextField4.insets = new java.awt.Insets(1, 2, 1, 2);
			add(getDataTextField4(), constraintsDataTextField4);

			java.awt.GridBagConstraints constraintsDataTextField5 = new java.awt.GridBagConstraints();
			constraintsDataTextField5.gridx = 1; constraintsDataTextField5.gridy = 4;
			constraintsDataTextField5.gridwidth = 2;
			constraintsDataTextField5.fill = java.awt.GridBagConstraints.HORIZONTAL;
			constraintsDataTextField5.anchor = java.awt.GridBagConstraints.WEST;
			constraintsDataTextField5.weightx = 1.0;
			constraintsDataTextField5.insets = new java.awt.Insets(1, 2, 1, 2);
			add(getDataTextField5(), constraintsDataTextField5);

			java.awt.GridBagConstraints constraintsDataTextField6 = new java.awt.GridBagConstraints();
			constraintsDataTextField6.gridx = 1; constraintsDataTextField6.gridy = 5;
			constraintsDataTextField6.gridwidth = 2;
			constraintsDataTextField6.fill = java.awt.GridBagConstraints.HORIZONTAL;
			constraintsDataTextField6.anchor = java.awt.GridBagConstraints.WEST;
			constraintsDataTextField6.weightx = 1.0;
			constraintsDataTextField6.insets = new java.awt.Insets(1, 2, 1, 2);
			add(getDataTextField6(), constraintsDataTextField6);

			java.awt.GridBagConstraints constraintsDataTextField2 = new java.awt.GridBagConstraints();
			constraintsDataTextField2.gridx = 1; constraintsDataTextField2.gridy = 1;
			constraintsDataTextField2.gridwidth = 2;
			constraintsDataTextField2.fill = java.awt.GridBagConstraints.HORIZONTAL;
			constraintsDataTextField2.anchor = java.awt.GridBagConstraints.WEST;
			constraintsDataTextField2.weightx = 1.0;
			constraintsDataTextField2.insets = new java.awt.Insets(1, 2, 1, 2);
			add(getDataTextField2(), constraintsDataTextField2);

			java.awt.GridBagConstraints constraintsDataComboBox2 = new java.awt.GridBagConstraints();
			constraintsDataComboBox2.gridx = 1; constraintsDataComboBox2.gridy = 1;
			constraintsDataComboBox2.gridwidth = 2;
			constraintsDataComboBox2.fill = java.awt.GridBagConstraints.HORIZONTAL;
			constraintsDataComboBox2.weightx = 1.0;
			constraintsDataComboBox2.insets = new java.awt.Insets(1, 2, 1, 2);
			add(getDataComboBox2(), constraintsDataComboBox2);
			initConnections();
		} catch (java.lang.Throwable ivjExc) {
			handleException(ivjExc);
		}
		// user code begin {2}
		
		adaptors.put(XYProjection,new XYProjectionGUIAdaptor());
		adaptors.put(PSProjection,new PSProjectionGUIAdaptor());
		adaptors.put(LambertCCProjection,new LambertCCProjectionGUIAdaptor());
		adaptors.put(MercatorProjection,new MercatorProjectionGUIAdaptor());
		adaptors.put(TransverseMercatorProjection,new TransverseMercatorProjectionGUIAdaptor());
		// user code end
	}


	/**
	 * Initialize
	 * @param proj GeoProjection
	 */
	public void initialize(int proj) {
		ProjectionGUIAdaptor adaptor=getAdaptor(proj);
		if (adaptor==null) return;
		adaptor.initialize(this);
	}


	/**
	 * Convert string to integer
	 * @param str String
	 * @return Integer
	 */
	public Integer integerValueOf(String str) {
		try {
			Integer v=Integer.valueOf(str);
			return v;
		}
		catch (NumberFormatException e) {return null;}
	}


		/**
		 * Return true if modified
		 * @return boolean
		 */
		public boolean isMod() {
			return mod;
		}


	/**
	 * main entrypoint - starts the part when it is run as an application
	 * @param args java.lang.String[]
	 */
	public static void main(java.lang.String[] args) {
		try {
//			JFrame frame = new JFrame();
			JMapFrame frame = new JMapFrame();
			ProjectionInfo aJeoProjectionParameterDialog;
			aJeoProjectionParameterDialog = new ProjectionInfo();
			frame.setContentPane(aJeoProjectionParameterDialog);
			frame.setSize(aJeoProjectionParameterDialog.getSize());
			frame.addWindowListener(new java.awt.event.WindowAdapter() {
				public void windowClosing(java.awt.event.WindowEvent e) {
					System.exit(0);
				};
			});
//			frame.show();	// show is deprecated
			frame.setVisible(true);
			java.awt.Insets insets = frame.getInsets();
			frame.setSize(frame.getWidth() + insets.left + insets.right, frame.getHeight() + insets.top + insets.bottom);
			frame.setVisible(true);
		} catch (Throwable exception) {
			System.err.println("Exception occurred in main() of javax.swing.JPanel");
			Logger.error("Exception occurred in main() of javax.swing.JPanel.");
			exception.printStackTrace(System.out);
		}
	}


	/**
	 * Remove a PropertyChangeListener for a specific property.
	 *
	 * @param propertyName  The name of the property that was listened on.
	 * @param listener  The PropertyChangeListener to be removed
	 */

	public synchronized void removePropertyChangeListener(
				String propertyName,
				PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(propertyName, listener);
	}


		/**
		 * Set modified
		 * @param b
		 */
		public void setMod(boolean b) {
			mod = b;
		}


		/**
		 * Set projection class
		 * @param class1
		 */
		public void setProjectionClass(int class1) {
			projectionClass = class1;
		}


	/**
	 * Set status field
	 * @param field
	 */
	public void setStatusTextField(javax.swing.JTextField field) {
		StatusField = field;
	}


	/**
	 * Convert string to short
	 * @param str String
	 * @return Short
	 */
	public Short shortValueOf(String str) {
		try {
			Short v=Short.valueOf(str);
			return v;
		}
		catch (NumberFormatException e) {return null;}
	}


	/**
	 * update state of GUI, return true if valid
	 * @return boolean
	 */
	public boolean update() {
		boolean valid=false;
		ProjectionGUIAdaptor adaptor=getAdaptor(projectionClass);
		if (adaptor!=null)
			valid=adaptor.validate(this);
		return valid;
	}
	public CoordinateReferenceSystem getProjection() {
		CoordinateReferenceSystem proj=null;
		ProjectionGUIAdaptor adaptor=getAdaptor(projectionClass);
		if (adaptor!=null)
			proj=adaptor.getProjection(this);
		return proj;
	}
	

	/**
	 * Return true if projection parameters are valid
	 * @param proj GeoProjection
	 * @return boolean
	 */
	public boolean validate(int proj) {
		ProjectionGUIAdaptor adaptor=getAdaptor(proj);
		if (adaptor==null) return false;
		return adaptor.validate(this);
	}
	/**
	 * Return true if auto central meridian
	 * @return boolean
	 */
	public boolean isAllowAutoCM() {
		return allowAutoCM;
	}

	/**
	 * Return true if auto zone
	 * @return boolean
	 */
	public boolean isAllowAutoZone() {
		return allowAutoZone;
	}

	/**
	 * Set allow auto central meridian
	 * @param b
	 */
	public void setAllowAutoCM(boolean b) {
		allowAutoCM = b;
	}

	/**
	 * Set allow auto zone
	 * @param b
	 */
	public void setAllowAutoZone(boolean b) {
		allowAutoZone = b;
	}
	static class EllipseData implements Comparable {
		static final Logger Logger = LogManager.getLogger(EllipseData.class.getName());
		/** The ellipsoid name */
		String name;
		/** The ellipsoid name - long version */
		String longName;
		/** The ellipsoid major axis - meters */
		double majorAxis;
		/** The ellipsoid minor axis - meters */
		double minorAxis;
		/** The reciprocal flattening */
		double flattening;
		
		/**
		 * Construct a new EllipseData instance from the specified parameters
		 * @param nm The name
		 * @param longNm The long name
		 * @param major The major axis
		 * @param minor The minor axis
		 */
		EllipseData(String nm, String longNm, double major, double minor, double flat)
		{
			name = nm;
			majorAxis = major;
			minorAxis = minor;
			longName = longNm;
			flattening = flat;
		}
		/**
		 * Return the major axis value
		 * @return The major axis value
		 */
		public double getMajorAxis()
		{
			return majorAxis;
		}
		/**
		 * Return the minor axis value
		 * @return The minor axis value
		 */
		public double getMinorAxis()
		{
			return minorAxis;
		}
		/**
		 * Return the short name of this ellipse
		 * @return The short name
		 */
		public String getName()
		{
			return name;
		}
		/**
		 * Return the long name of this ellipse
		 * @return The long name
		 */
		public String getLongName() {
			return longName;
		}
		/** {@inheritDoc} */
		public String toString() {
			return getLongName();
		}
		/**
		 * {@inheritDoc}
		 * <p>
		 * Compare based on long name
		 */
		public int compareTo(Object that) {
			return longName.compareTo(((EllipseData)that).longName);
		}
		public String getWKTString(){
			if(flattening==0){
				return "GEOGCS[\""+name+"\",     DATUM[\""+name+"\",       SPHEROID[\""+name+"\", "+majorAxis+", "+minorAxis+"]]";
			}
			return "GEOGCS[\""+name+"\",     DATUM[\""+name+"\",       SPHEROID[\""+name+"\", "+majorAxis+", "+flattening+"]]";
		}
		public double getFlattening() {
			return flattening;
		}
		public void setFlattening(double flattening) {
			this.flattening = flattening;
		}

	}
	/**
	 * Initialize the ellipse data
	 */
	private static void initializeEllipseData() {
		ellipseData = new HashMap<String,EllipseData>();
		EllipseData ed;
		ed = new EllipseData("CLARKE66","Clarke 1866",6378206.4,6356583.8,294.9786982);		/* 0: Clarke 1866 (default) */
		ellipseData.put(ed.getName(),ed);
		ed = new EllipseData("CLARKE80","Clarke 1880",6378249.145,6356514.86955,293.465);		/* 1: Clarke 1880 */
		ellipseData.put(ed.getName(),ed);
		ed = new EllipseData("BESSEL","Bessel",6377397.155,6356078.96284,299.1528128);		/* 2: Bessel */
		ellipseData.put(ed.getName(),ed);
		ed = new EllipseData("INT1967","International 1967",6378157.5,6356772.2,297);		/* 3: International 1967 */
		ellipseData.put(ed.getName(),ed);
		ed = new EllipseData("INT1909","International 1909",6378388.0,6356911.94613,297);		/* 4: International 1909 */
		ellipseData.put(ed.getName(),ed);
		ed = new EllipseData("WGS72","WGS 72",6378135.0,6356750.519915,298.26);		/* 5: WGS 72 */
		ellipseData.put(ed.getName(),ed);
		ed = new EllipseData("EVEREST","Everest",6377276.3452,6356075.4133,300.8017);		/* 6: Everest */
		ellipseData.put(ed.getName(),ed);
		ed = new EllipseData("WGS66","WGS 66",6378145.0,6356759.769356,0);		/* 7: WGS 66 */
		ellipseData.put(ed.getName(),ed);
		ed = new EllipseData("GRS80","GRS 1980",6378137.0,6356752.31414,298.257222101);		/* 8: GRS 1980 */
		ellipseData.put(ed.getName(),ed);
		ed = new EllipseData("AIRY","Airy",6377563.396,6356256.91,299.3249646);		/* 9: Airy */
		ellipseData.put(ed.getName(),ed);
		ed = new EllipseData("MOD_EVEREST","Modified Everest",6377304.063,6356103.039,300.8017);		/* 10: Modified Everest */
		ellipseData.put(ed.getName(),ed);
		ed = new EllipseData("MOD_AIRY","Modified Airy",6377340.189,6356034.448,299.3249646);		/* 11: Modified Airy */
		ellipseData.put(ed.getName(),ed);
		ed = new EllipseData("WGS84","WGS 84",6378137.0,6356752.314245,298.257223563);		/* 12: WGS 84 */
		ellipseData.put(ed.getName(),ed);
		ed = new EllipseData("SE_ASIA","Southeast Asia",6378155.0,6356773.3205,0);		/* 13: Southeast Asia */
		ellipseData.put(ed.getName(),ed);
		ed = new EllipseData("AUS_NATIONAL","Australian National",6378160.0,6356774.719,298.25);		/* 14: Australian National */
		ellipseData.put(ed.getName(),ed);
		ed = new EllipseData("KRASSOVSKY","Krassovsky",6378245.0,6356863.0188,298.3);		/* 15: Krassovsky */
		ellipseData.put(ed.getName(),ed);
		ed = new EllipseData("HOUGH","Hough",6378270.0,6356794.343479,297);		/* 16: Hough */
		ellipseData.put(ed.getName(),ed);
		ed = new EllipseData("MERC1960","Mercury 1960",6378166.0,6356784.283666,0);		/* 17: Mercury 1960 */
		ellipseData.put(ed.getName(),ed);
		ed = new EllipseData("MOD_MERC1968","Modified Mercury 1968",6378150.0,6356768.337303,0);		/* 18: Modified Mercury 1968 */
		ellipseData.put(ed.getName(),ed);
//		ed = new EllipseData("SPHERE","Sphere of Radius 6370997 meters",6370997.0,6370997.0,0);		/* 19: Sphere of Radius 6370997 meters*/
		ed = new EllipseData("SPHERE","Sphere of Radius 6370000 meters",6370000.0,6370000.0,0); // NOTE: Changed according to the communication between Todd Plessel and Donna Schwede on 	April 26, 2011
		ellipseData.put(ed.getName(),ed);
		ed = new EllipseData("BESSEL1841","Bessel 1841(Namibia)",6377483.865,6356165.382966,299.1528128);		/* 20: Bessel 1841(Namibia) */
		ellipseData.put(ed.getName(),ed);
		ed = new EllipseData("EVEREST_SABAH","Everest (Sabah & Sarawak)",6377298.556,6356097.571445,300.8017);		/* 21: Everest (Sabah & Sarawak) */
		ellipseData.put(ed.getName(),ed);
		ed = new EllipseData("EVEREST_INDIA56","Everest (India 1956)",6377301.243,6356100.228368,300.8017);		/* 22: Everest (India 1956) */
		ellipseData.put(ed.getName(),ed);
		ed = new EllipseData("EVEREST_MALAYSIA69","Everest (Malaysia 1969)",6377295.664,6356094.667915,300.8017);		/* 23: Everest (Malaysia 1969) */
		ellipseData.put(ed.getName(),ed);
		ed = new EllipseData("EVEREST_MALAY48","Everest (Malay & Singapore 1948)",6377304.063,6356103.038993,300.8017);		/* 24: Everest (Malay & Singapr 1948)*/
		ellipseData.put(ed.getName(),ed);
		ed = new EllipseData("EVEREST_PAKISTAN","Everest (Pakistan)",6377309.613,6356108.570542,300.8017);		/* 25: Everest (Pakistan) */
		ellipseData.put(ed.getName(),ed);
		ed = new EllipseData("HAYFORD","Hayford",6378388.0,6356911.946128,0);		/* 26: Hayford */
		ellipseData.put(ed.getName(),ed);
		ed = new EllipseData("HELMERT06","Helmert 1906",6378200.0,6356818.169,298.3);		/* 27: Helmert 1906 */
		ellipseData.put(ed.getName(),ed);
		ed = new EllipseData("INDONESIAN74","Indonesian 1974",6378160.000,6356774.504086,298.247);		/* 28: Indonesian 1974 */
		ellipseData.put(ed.getName(),ed);
		ed = new EllipseData("SO_AMER69","South American 1969",6378160.0,6356774.719,0);		/* 29: South American 1969 */
		ellipseData.put(ed.getName(),ed);
		ed = new EllipseData("WGS60","WGS 60",6378165.0,6356783.287,0);		/* 30: WGS 60 */
		ellipseData.put(ed.getName(),ed);
	}
	/**
	 * return all the ellipse data
	 * @return all Ellipse data as a List
	 */
	public static List getAllEllipseData() {
		return new ArrayList(ellipseData.values());
	}
	transient static Map<String,EllipseData> ellipseData;
	static {
		initializeEllipseData();
	}
	// write a prj file if needed
	public static void writePRJFile(String fileName,CoordinateReferenceSystem crs, boolean overwrite){
		String projString = crs.toWKT();
		 // for compatibility with ESRI 
        projString=projString.replaceAll("\\r","").replaceAll("\\n","");
        projString=projString.replaceAll("Mercator_2SP","Mercator");
        try{
        	File file = new File(fileName);
        	if((!overwrite) && file.exists())return;
        	
	        // create the file
	        FileWriter fstream = new FileWriter(file);
	        BufferedWriter out = new BufferedWriter(fstream);
	       	out.write(projString);
	       	//Close the output stream
	       	out.close();
	       	
	    }catch (Exception e){//Catch exception if any
	    	Logger.error("Error: " + e.getMessage());
	      System.err.println("Error: " + e.getMessage());
	    }
	}
}
