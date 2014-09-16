package saf.core.ui.util;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

public class LongDocument extends PlainDocument {

	public LongDocument() {
		super();
		// TODO Auto-generated constructor stub
	}

	public LongDocument(Content c) {
		super(c);
		// TODO Auto-generated constructor stub
	}

	public void insertString(int offs, String string, AttributeSet att)
			throws BadLocationException {
		try {
			Long.parseLong(string);
		} catch (Exception e) {
			return;
		}
		super.insertString(offs, string, att);
	}

}
