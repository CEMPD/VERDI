package saf.core.ui.msg;

import simphony.util.messages.MessageEvent;

import javax.swing.table.AbstractTableModel;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.io.StringWriter;
import java.io.PrintWriter;

import org.apache.log4j.Level;


/**
 * TableModel for the table of message logs. 
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class MessageTableModel extends AbstractTableModel {

	private static class DatedEvent {

		private static DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss,SSS");

		private String time;
		private String msg;
		private MessageEvent evt;


		public DatedEvent(Date date, MessageEvent evt) {
			this.time = df.format(date);
			this.evt = evt;
			this.msg = evt.getMessage() == null ? "null" : evt.getMessage().toString();
		}


		public MessageEvent getEvt() {
			return evt;
		}

		public Level getLevel() {
			return evt.getLevel();
		}

		public String getMessage() {
			return msg;
		}

		public String getTime() {
			return time;
		}
	}

	private static final String[] COLUMN_NAMES = {"Time", "Level", "Message"};

	private List<DatedEvent> events = new ArrayList<DatedEvent>();


	public int getColumnCount() {
		return COLUMN_NAMES.length;
	}


	public String getColumnName(int column) {
		return COLUMN_NAMES[column];
	}

	public int getRowCount() {
		return events.size();
	}

	public void addMessageEvent(MessageEvent evt) {
		events.add(new DatedEvent(new Date(), evt));
		fireTableRowsInserted(events.size(), events.size());
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		DatedEvent evt = events.get(rowIndex);
		if (columnIndex == 0) {
			return evt.getTime();
		} else if (columnIndex == 1) {
			return evt.getLevel();
		} else {
			return evt.getEvt().getMessage();
		}
	}

	public String getFullMessage(int row) {
		DatedEvent evt = events.get(row);
		StringWriter writer = new StringWriter();
		StringBuffer buf = writer.getBuffer();
		buf.append(evt.getTime());
		buf.append(": ");
		buf.append(evt.getMessage());
		Throwable throwable = evt.getEvt().getThrowable();
		if (throwable != null) {
			buf.append("\n");
			PrintWriter pWriter = new PrintWriter(writer);
			throwable.printStackTrace(pWriter);
			pWriter.flush();
		}
		return writer.toString();
	}
}
