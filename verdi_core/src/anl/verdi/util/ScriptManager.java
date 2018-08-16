package anl.verdi.util;

public class ScriptManager {
	
	static final String SUP_BEGIN_DELIM = "{";
	static final String SUP_END_DELIM = "}";
	static final String SUB_BEGIN_DELIM = "}";
	static final String SUB_END_DELIM = "{";
	
	interface StringConverter {
		public String convert(String str);
	};
	
	private static final StringConverter SUPERSCRIPT = new StringConverter() {
		public String convert(String str) {
		    str = str.replaceAll("0", "⁰");
		    str = str.replaceAll("1", "¹");
		    str = str.replaceAll("2", "²");
		    str = str.replaceAll("3", "³");
		    str = str.replaceAll("4", "⁴");
		    str = str.replaceAll("5", "⁵");
		    str = str.replaceAll("6", "⁶");
		    str = str.replaceAll("7", "⁷");
		    str = str.replaceAll("8", "⁸");
		    str = str.replaceAll("9", "⁹");         
		    return str;
		}
	};

	private static final StringConverter SUBSCRIPT = new StringConverter() {

		public String convert(String str) {
		    str = str.replaceAll("0", "₀");
		    str = str.replaceAll("1", "₁");
		    str = str.replaceAll("2", "₂");
		    str = str.replaceAll("3", "₃");
		    str = str.replaceAll("4", "₄");
		    str = str.replaceAll("5", "₅");
		    str = str.replaceAll("6", "₆");
		    str = str.replaceAll("7", "₇");
		    str = str.replaceAll("8", "₈");
		    str = str.replaceAll("9", "₉");
		    return str;
		}
	};
	
	public static String parseScript(String str) {
		str = parseScript(str, SUP_BEGIN_DELIM, SUP_END_DELIM, SUPERSCRIPT);
		str = parseScript(str, SUB_BEGIN_DELIM, SUB_END_DELIM, SUBSCRIPT);
		return str;
	}
	
	public static String parseScript(String str, String beginDelim, String endDelim, StringConverter converter) {
		int stringStart = 0;
		int startIdx = str.indexOf(beginDelim, stringStart);
		int endIdx = -1;
		StringBuffer sb = new StringBuffer();
		while (startIdx > -1 && str.indexOf(endDelim, startIdx) > startIdx) {
			endIdx = str.indexOf(endDelim, startIdx);
			String s1 = str.substring(stringStart, startIdx);
			String s2 = converter.convert(str.substring(startIdx + beginDelim.length(), endIdx));
			stringStart = endIdx + endDelim.length();
			startIdx = str.indexOf(beginDelim, stringStart);
			sb.append(s1);
			sb.append(s2);
		}
		if (stringStart < str.length())
			sb.append(str.substring(stringStart));
		return sb.toString();
		
		
	}
	
	public static void main(String[] args) {
		String testString = "script format 1: e=mc{2} and CO}2{";
		
		String style2 = "test format 2: e=mc<sup>2</sup> and CO<sub>2</sub>";
		
		String converted = ScriptManager.parseScript(testString);
		System.out.println(converted);
		System.out.println("done");
	}

}
