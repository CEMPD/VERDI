package saf.core.ui.util;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

public class IntegerDocument extends PlainDocument {

	public IntegerDocument() {
		super();
		// TODO Auto-generated constructor stub
	}

	public IntegerDocument(Content c) {
		super(c);
		// TODO Auto-generated constructor stub
	}

	public void insertString(int offs, String string, AttributeSet att)
			throws BadLocationException {
		try {
			Integer.parseInt(string);
		} catch (Exception e) {
			return;
		}
		super.insertString(offs, string, att);
	}

}
