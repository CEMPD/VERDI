package anl.verdi.plot.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ImageResolutionDialog extends JDialog implements FocusListener {
		private static final long serialVersionUID = -1110292652911018568L;
		
		static final Logger Logger = LogManager.getLogger(ImageResolutionDialog.class);
		public static final int CANCEL_OPTION = -1;
		public static final int YES_OPTION = 1;
		public static final int ERROR = 0;
		private JTextField xResField;
		private JTextField yResField;
		private JLabel resLabel;
		private boolean cancelled = false;
		private int xRes, yRes;
		double aspect;
		private Object lastSelectedField = null;

		public ImageResolutionDialog(Plot plot) {
			super.setTitle("Image Dimensions");
			super.setLocation(getCenterPoint(plot.getPanel()));
			super.setModal(true);
			super.setPreferredSize(new Dimension(400, 300));

			this.resLabel = new JLabel("Please enter the dimensions of the image:");
			xRes = plot.getPanel().getWidth();
			yRes = plot.getPanel().getHeight();
			aspect = ((double)xRes) / ((double)yRes);
			this.xResField = new JTextField("1", 4);
			this.yResField = new JTextField("1", 4);
			xResField.addFocusListener(this);
			yResField.addFocusListener(this);

			this.getContentPane().add(createLayout());
		}

		public int showDialog() {
			this.pack();
			Point p = getLocation();
			setLocation(0, p.y);
			this.setVisible(true);

			if (this.cancelled)
				return CANCEL_OPTION;

			try {
				xRes = Integer.valueOf(xResField.getText());
				yRes = Integer.valueOf(yResField.getText());

				return YES_OPTION;
			} catch (NumberFormatException e) {
				Logger.error("Number Format Exception in ImageResolutionDialog.showDialog", e);
			}

			return ERROR;
		}

		private JPanel createLayout() {
			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
			panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

			panel.add(createMiddlePanel());
			panel.add(createButtonsPanel());

			return panel;
		}

		private JPanel createMiddlePanel() {
			GridBagLayout gridbag = new GridBagLayout();
			GridBagConstraints c = new GridBagConstraints();
			JPanel contentPanel = new JPanel(gridbag);

			c.fill = GridBagConstraints.HORIZONTAL;
			c.weightx = 1.0;
			c.insets = new Insets(1, 1, 7, 5);

			JPanel labelPanel = new JPanel();
			labelPanel.add(resLabel, BorderLayout.LINE_START);
			//labelPanel.add(new JLabel("..."));
			//labelPanel.add(lLonField, BorderLayout.LINE_END);
			JLabel holder1 = new JLabel();

			gridbag.setConstraints(resLabel, c);
			gridbag.setConstraints(labelPanel, c);
			c.gridwidth = GridBagConstraints.REMAINDER; // end row
			gridbag.setConstraints(holder1, c);
			contentPanel.add(resLabel);
			contentPanel.add(labelPanel);
			contentPanel.add(holder1);

			c.gridwidth = 1; // next-to-last in row

			//JLabel latLabel = new JLabel("Latitude:");
			JPanel dimPanel = new JPanel();
			dimPanel.setLayout( new FlowLayout(FlowLayout.LEFT));
			dimPanel.add(xResField);
			xResField.setText(Integer.toString(this.xRes));
			dimPanel.add(new JLabel("x"));
			dimPanel.add(yResField);
			dimPanel.add(new JLabel("px"));
			yResField.setText(Integer.toString(this.yRes));
			JLabel holder2 = new JLabel();

			//gridbag.setConstraints(latLabel, c);
			gridbag.setConstraints(dimPanel, c);
			c.gridwidth = GridBagConstraints.REMAINDER;
			gridbag.setConstraints(holder2, c);
			//contentPanel.add(latLabel);
			contentPanel.add(dimPanel);
			contentPanel.add(holder2);

			return contentPanel;
		}
		
		public int getXRes() {
			return xRes;
		}
		
		public int getYRes() {
			return yRes;
		}

		private JPanel createButtonsPanel() {
			JPanel container = new JPanel();
			FlowLayout layout = new FlowLayout();
			layout.setHgap(20);
			layout.setVgap(2);
			container.setLayout(layout);

			JButton okButton = new JButton("OK");
			okButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					updateAspectRatio(lastSelectedField);
					cancelled = false;
					dispose();
				}
			});

			container.add(okButton);
			getRootPane().setDefaultButton(okButton);

			JButton cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					cancelled = true;
					dispose();
				}
			});
			container.add(cancelButton);
			container.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

			return container;
		}

		private Point getCenterPoint(Component comp) {
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

			if (comp == null) {
				return new Point((int) screenSize.getWidth() / 2,
						(int) screenSize.getHeight() / 2);
			}

			Dimension frameSize = comp.getSize();

			if (frameSize.height > screenSize.height) {
				frameSize.height = screenSize.height;
			}

			if (frameSize.width > screenSize.width) {
				frameSize.width = screenSize.width;
			}

			return new Point((screenSize.width - frameSize.width) / 2,
					(screenSize.height - frameSize.height) / 2);
		}

		@Override
		public void focusGained(FocusEvent e) {	
			lastSelectedField = e.getSource();
		}

		@Override
		public void focusLost(FocusEvent e) { //Ensure aspect ratio is preserved
			updateAspectRatio(e.getSource());
		}
		
		private void updateAspectRatio(Object source) {
			if (source == null)
				return;
			boolean isX = source.equals(xResField);
			int x = 0;
			int y = 0;
			try {
				if (isX) {
					x = Integer.valueOf(xResField.getText());
					y = (int)Math.round(((double)x) / aspect);
					yResField.setText(Integer.toString(y));
					yRes = y;
				} else {
					y = Integer.valueOf(yResField.getText());
					x = (int)Math.round(aspect * ((double)y));
					xResField.setText(Integer.toString(x));
					xRes = x;
				}
			} catch (NumberFormatException ex) {}

		}
	}
