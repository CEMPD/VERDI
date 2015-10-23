package saf.core.ui;


import info.clearthought.layout.TableLayout;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Describes the setup info for the creation of a status bar.
 *
 * @author Nick Collier
 * @version $Revision: 1.4 $ $Date: 2006/06/12 21:48:51 $
 */
public class StatusBarDescriptor {

	private static class Data {
		String name;
		BigDecimal percentage;

		public Data(String name, BigDecimal percentage) {
			this.name = name;
			this.percentage = percentage;
		}
	}

	private List<Data> fields = new ArrayList<Data>();
	private BigDecimal runningSum = new BigDecimal("0");

	/**
	 * Adds a status field.
	 *
	 * @param name the name of the field
	 * @param percentage the percentage of the total space of the status bar that the field should fill.
	 * @throws IllegalArgumentException if the total of all the percentages after this is added is
	 * greater than 1.
	 */
	public void addField(String name, float percentage) {
		BigDecimal bdPer = new BigDecimal(String.valueOf(percentage));
		runningSum = runningSum.add(bdPer);
		if (runningSum.floatValue() > 1) throw new IllegalArgumentException("Status bar percentage totals are greater " +
						"than 1");
		fields.add(new Data(name, bdPer));
	}

	/**
	 * Fills the GUIBarManager with the fields that have been added to this descriptor.
	 *
	 * @param barManager
	 */
	public void fillBar(GUIBarManager barManager) {
		if (fields.size() == 0) return;
		// check to see if add up to 1.0
		BigDecimal sum = new BigDecimal("0");
		for (Data data : fields) {
			sum = data.percentage.add(sum);
		}

		if (sum.floatValue() < 1) {
			float dif = 1 - sum.floatValue();
			BigDecimal last = fields.get(fields.size() - 1).percentage;
			last = last.add(new BigDecimal(String.valueOf(dif)));
			fields.get(fields.size() - 1).percentage = last;
		}

		JPanel panel = barManager.getStatusBar();
		double[][] sizes = new double[2][];
		sizes[1] = new double[]{TableLayout.PREFERRED};
		sizes[0] = new double[fields.size() + fields.size() - 1];

		int i = 0;
		int fieldIndex = 0;
		for (i = 0; i < sizes[0].length; i++) {

			if (i % 2 == 0) {
				double val = fields.get(fieldIndex++).percentage.floatValue();
				if (val == 1) val = TableLayout.FILL;
				sizes[0][i] = val;
			} else {
				sizes[0][i] = 4;
			}
		}
		
		panel.setLayout(new TableLayout(sizes));
		i = 0;
		Map<String, StatusBarItem> map = new HashMap<String, StatusBarItem>();
		for (Data data : fields) {
			JTextField field = new JTextField();
			field.setEditable(false);
			String layoutString = i + ", 0";
			map.put(data.name, new StatusBarItem(data.name, layoutString, field));
			panel.add(field, layoutString);
			i += 2;
		}

		barManager.setStatusBar(panel, map);
	}

	public static void main(String[] args) {
		double[][] sizes = {{.75, 4, .25}, {TableLayout.PREFERRED}};
		System.out.println("sizes[0].length = " + sizes[0].length);
		System.out.println("sizes[1].length = " + sizes[1].length);

		TableLayout layout = new TableLayout(sizes);
		JPanel panel = new JPanel(layout);
		panel.add(new JTextField("foo"), "0, 0");
		panel.add(new JTextField("bar"), "2, 0");

		JFrame frame = new JFrame();
		frame.setLayout(new BorderLayout());
		frame.add(panel, BorderLayout.CENTER);

		frame.setVisible(true);

	}
}
