package anl.verdi.area;

/******************************************************/
/* Developed by Mary Ann Bitz                       */ 
/* Argonne National Laboratory                        */
/* changed strings to stringbuffers:  KLS             */ 
/*                                                    */
/* File Name: FileHelper.java                         */ 
/* Description: This class reads in data for the      */
/* different objects in the program.                  */
/*                                                    */
/******************************************************/
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.text.NumberFormat;
import java.util.Enumeration;
import java.util.Vector;

public class FileHelper{
	static public String convertMultiLineString(String string){
		StringBuffer sb=new StringBuffer();
		for(int i=0;i<string.length();i++){
			if(string.charAt(i)=='\\'){
				if((i+1)<string.length()){
					if(string.charAt(i+1)=='n'){
						sb.append('\n');
						i++;
						continue;
					}
				}
			}
			sb.append(string.charAt(i));
		}
		return sb.toString();
	}
	static public String convertDoubleToString(double number){
		// use decimal format to avoid exponential notation
		NumberFormat format = NumberFormat.getNumberInstance();
		format.setMinimumFractionDigits(0);
		format.setMaximumFractionDigits(100);
		format.setGroupingUsed(false); 
		return format.format(number);
	}

	/**
	 * Gets the main part of the filename without path or final extension
	 * @param fileName String holding the complete name of the file
	 * @return the basename of the filename 
	 */
	static public String getFileBaseName(String fileName){
		int len = fileName.length();
		int beginIndex=0;
		// strip off the path
		for(int i=len-2;i>=0;i--){
			char val = fileName.charAt(i);
			if(val=='/'||val=='\\'){
				beginIndex=i+1;
				break;
			}
		}

		// strip off the last index
		int endIndex = fileName.lastIndexOf((int)'.',len);
		// if no extension, go to the end
		if(endIndex<=0)endIndex=len;
		return fileName.substring(beginIndex,endIndex);
	}
	static public String formDatabaseString(String string){
		return formQuoteString(string,'\'');
	}
	static public String formQuoteString(String string,char quoteChar){
		if(string==null)return "null";
		StringBuffer sb=new StringBuffer();
		sb.append(quoteChar);
		for(int i=0;i<string.length();i++){
			if(string.charAt(i)=='\n'){
				sb.append('\\');
				sb.append('\n');
			}
			else if(string.charAt(i)=='\\'){
				sb.append('\\');
				sb.append('\\');
			}
			else{
				sb.append(string.charAt(i));
				if(string.charAt(i)==quoteChar){
					sb.append(quoteChar);
				}
			}
		}
		sb.append(quoteChar);
		return sb.toString();
	}
	// convert the string to one with quotes
	static public String formString(String string){
		return formQuoteString(string,'"');
	}
	static public Vector readData(File filename){
		try{
			FileReader fr = new FileReader(filename);
			return readDataStream(fr);
		}
		catch(FileNotFoundException e){
			System.out.println("Read Error: File "+filename+" not found");
			System.out.println(e.getMessage());
			return null;
		}
	}
	static public Vector readData(String filename){
		try{
			FileReader fr = new FileReader(filename);
			return readDataStream(fr);
		}
		catch(FileNotFoundException e){
			System.out.println("Read Error: File "+filename+" not found");
			System.out.println(e.getMessage());
			return null;
		}
	}
	static public Vector readDataLines(String filename){
		BufferedReader input = null;
		try{
			input = new BufferedReader(new FileReader(filename),40960);
			return readDataLines(input);
		}
		catch(FileNotFoundException e){
			System.out.println("Read Error: File "+filename+" not found");
			System.out.println(e.getMessage());
			return null;
		}

	}
	static public Vector readDataLines(File filename){
		BufferedReader input = null;
		try{
			input = new BufferedReader(new FileReader(filename),40960);
			return readDataLines(input);
		}
		catch(FileNotFoundException e){
			System.out.println("Read Error: File "+filename+" not found");
			System.out.println(e.getMessage());
			return null;
		}

	}
	static public Vector readDataLines(BufferedReader input){
		String line;
		Vector stringList = null;
		try{
			if((line = input.readLine())!=null){
				stringList = new Vector(500,100);
				stringList.addElement(line);
				while((line = input.readLine())!=null){
					//if(stringList==null) stringList = new Vector();
					stringList.addElement(line);
				}
			}
		}
		catch(IOException e){
			System.out.println("File error");
			System.out.println(e.getMessage());
		}
		finally{
			try{
				input.close();
			}
			catch(IOException e){
				System.out.println("File could not be closed");
				System.out.println(e.getMessage());
			}
		}
		return stringList;
	}
	static public Vector readDataStream(Reader is){
//		String line;
//		int offset,startPos;
//		char currentChar;
		Vector lineData = new Vector();

		BufferedReader input = null;
		input = new BufferedReader(is,40960);

		try{
			StreamTokenizer st = new StreamTokenizer(input);
			st.eolIsSignificant(true);
			st.ordinaryChar('\\');
			//			st.parseNumbers();
			int lastTokenType=0;
			// do first line
			Vector currentLine = new Vector();
			lineData.addElement(currentLine);
			while(st.nextToken()!=StreamTokenizer.TT_EOF){
				switch(st.ttype){
				case(StreamTokenizer.TT_NUMBER):
					//System.out.println("Number -"+st.nval+"-");
					currentLine.addElement(new Double(st.nval));
				break;

				case(StreamTokenizer.TT_WORD):
					//                    System.out.println("Word -"+st.sval+"-");
					if(lastTokenType==StreamTokenizer.TT_WORD){
						String lastString=(String)currentLine.lastElement();
						lastString = lastString + ' '+st.sval;
						currentLine.setElementAt(lastString,currentLine.size()-1);
					}
					else{
						currentLine.addElement(st.sval);
					}
				break;

				case(StreamTokenizer.TT_EOL):
					//System.out.println("end of line");
					currentLine=new Vector();
				lineData.addElement(currentLine);
				break;

				case('"'):
					if(lastTokenType=='"'){
						//System.out.println("string -"+st.sval+"-");
						String lastString=(String)currentLine.lastElement();
						lastString = lastString + '"'+st.sval;
						currentLine.setElementAt(lastString,currentLine.size()-1);
					}
					else{
						currentLine.addElement(st.sval);
					}
				break;
				}

				//System.out.println("token "+st.ttype+" -"+st.sval+"-");
				lastTokenType=st.ttype;
			}
			// remove empty last line
			Vector lastLine = (Vector)lineData.lastElement();
			if(lastLine.isEmpty()){
				lineData.removeElementAt(lineData.size()-1);
			}
			/*
		if((line = input.readLine())!=null){
			stringList = new Vector(500,100);
			StringTokenizer tokenizer = new StringTokenizer(line,",");
			Vector subStrings = new Vector(tokenizer.countTokens());
			stringList.addElement(subStrings);
			while(tokenizer.hasMoreTokens()){
				subStrings.addElement(new String(tokenizer.nextToken()));
			}
			while((line = input.readLine())!=null){
				tokenizer = new StringTokenizer(line,",");
				subStrings = new Vector(tokenizer.countTokens());
				stringList.addElement(subStrings);
				while(tokenizer.hasMoreTokens()){
					subStrings.addElement(new String(tokenizer.nextToken()));
				}
			}
		}
			 */
		}
		catch(IOException e){
			System.out.println("File error");
			System.out.println(e.getMessage());
		}
		finally{
			try{
				input.close();
			}
			catch(IOException e){
				System.out.println("BufferedReader could not be closed");
				System.out.println(e.getMessage());
			}
		}
		return lineData;
	}
	static public void writeDataLines(String filename,Vector data){
		BufferedWriter output = null;
//		String line;
//		Vector stringList = null;
		try{
			output = new BufferedWriter(new FileWriter(filename));
		}
		catch(IOException e){
			System.out.println("IO error");
			System.out.println(e.getMessage());
		}
		try{
			for(Enumeration e=data.elements();e.hasMoreElements();){
				String str = (String)e.nextElement();
				output.write(str,0,str.length());
				output.newLine();
			}
		}
		catch(IOException e){
			System.out.println("File error");
			System.out.println(e.getMessage());
		}
		finally{
			try{
				output.close();
			}
			catch(IOException e){
				System.out.println("File could not be closed");
				System.out.println(e.getMessage());
			}
		}
	}
	static public void writeFixedDataLines(String filename,Vector data){
		BufferedWriter output = null;
//		String line;
//		Vector stringList = null;
		try{
			output = new BufferedWriter(new FileWriter(filename));
		}
		catch(IOException e){
			System.out.println("IO error");
			System.out.println(e.getMessage());
		}
		try{
			for(Enumeration e=data.elements();e.hasMoreElements();){
				String str = (String)e.nextElement();
				output.write(str,0,str.length());
				output.newLine();
			}
		}
		catch(IOException e){
			System.out.println("File error");
			System.out.println(e.getMessage());
		}
		finally{
			try{
				output.close();
			}
			catch(IOException e){
				System.out.println("File could not be closed");
				System.out.println(e.getMessage());
			}
		}
	}
}
