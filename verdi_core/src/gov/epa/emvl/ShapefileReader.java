/**
 * ShapefileReader - Debugging class used to inspect and verify structure of shapefiles
 * @author Tony Howard
 * @version $Revision$ $Date$
 **/

package gov.epa.emvl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ShapefileReader {
	
	
	int fileIndex = 0;
	State state = State.CHOOSE_FILE;
	BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
	File inputFile = null;
	byte[] inputData;
	
	private static final int BIG = 1;
	private static final int LITTLE = 2;
	
	public static final String[] LOAD_FILE_OPTIONS = new String[] {
		"Choose File: "
	};
	
	public static final String[] FILE_ACTION_OPTIONS = new String[] {
		"Print Double (Big)",
		"Print Double (Little)",
		"Print Int (Big)",
		"Print Int (Little)",
		"Count Records",
		"Jump to Record",
		"Move to Previous Record",
		"Move to Next Record",
		"Jump to Byte",
		"Advance Words",
		"Print record size",
		"Close File"
	};
	
	public static final String[] ENTER_LOCATION_OPTION = new String[] {
		"Enter Location"
	};
	
	public enum State {
		CHOOSE_FILE, FILE_ACTION
	}
	
	public enum Command {
		CHOOSE_FILE, PRINT_DOUBLE_BIG, PRINT_DOUBLE_LITTE, 
		PRINT_INT_BIG, PRINT_INT_LITTLE, JUMP_TO_RECORD,
		JUMP_TO_BYTE, ADVANCE_BYTES
		
	}
	
	public void runMenu() throws IOException {
		String resp = null;
		String[] menu;
		String prefix;
		
		while (true) {
			prefix = ">";
			
			if (state == State.CHOOSE_FILE)
				menu = LOAD_FILE_OPTIONS;
			else if (state == State.FILE_ACTION) {
				menu = FILE_ACTION_OPTIONS;
				prefix = "(" + fileIndex + ", " + (inputFile.length() - fileIndex - 1) + ")>";
			}
			else {
				return;
			}
			
			for (int i = 0; i < menu.length; ++i) {
				System.out.println((i+1) + ".  " + menu[i]);
			}
			System.out.println((menu.length + 1) + ".  Quit");
			System.out.println(prefix);
			resp = console.readLine();
			if (Integer.toString(menu.length + 1).equals("resp"))
				return;
	
			if (state == State.CHOOSE_FILE) {
				System.out.println("Enter Filename> ");
				resp = console.readLine();
				try {
	
					if (!resp.startsWith("/")) {
						resp = System.getProperty("user.home") + resp;
					}
					inputFile = new File(resp);
					FileInputStream reader = new FileInputStream(inputFile);
					fileIndex = 0;
					inputData = new byte[(int)inputFile.length()];
					byte[] buf = new byte[2048];
					int bytesRead = 0;
					do {
						bytesRead = reader.read(buf, 0, buf.length);
						if (bytesRead > 0) {
							System.arraycopy(buf, 0, inputData, fileIndex, bytesRead);
							fileIndex += bytesRead;
						}
					} while (bytesRead > 0);
					reader.close();
					fileIndex = 0;
					state = State.FILE_ACTION;
					
				} catch (IOException e) {
					System.out.println("Could not read " + resp);
					e.printStackTrace();
				}
			}
			else if (state == State.FILE_ACTION) {
				if (resp.equals("1")) {
					System.out.println(readDouble(fileIndex, BIG));
					fileIndex += 8;
				}
				else if (resp.equals("2")) {
					System.out.println(readDouble(fileIndex, LITTLE));
					fileIndex += 8;
				}
				else if (resp.equals("3")) {
					System.out.println(readInt(fileIndex, BIG));
					fileIndex += 4;
				}
				else if (resp.equals("4")) {
					System.out.println(readInt(fileIndex, LITTLE));
					fileIndex += 4;
				}
				else if (resp.equals("5")) {
					System.out.println(getRecordCount() + " records");
				}
				else if (resp.equals("6")) {//jump to record
					System.out.println("Enter Record Number> ");
					resp = console.readLine();
					try {
						int newIndex = Integer.parseInt(resp);
						if (!advanceToRecord(newIndex))
							System.out.println("Could not advance to index " + newIndex);
					}
					catch (NumberFormatException e) {
						System.err.println("Could not read " + resp);
					}
							
				}
				else if (resp.equals("7")) { //jump to previous record
					int newIndex = findPreviousRecord(fileIndex);
					if (newIndex == -1)
						System.out.println("Could not move to previous record");
					else
						fileIndex = newIndex;
				}
				else if (resp.equals("8")) { //jump to next record
					int newIndex = findNextRecord(fileIndex);
					if (newIndex == -1)
						System.out.println("Already at end");
					else
						fileIndex = newIndex;
				}
				else if (resp.equals("9")) { //jump to byte
					System.out.println("Enter Byte> ");
					resp = console.readLine();
					try {
						int newIndex = Integer.parseInt(resp);
						if (newIndex < inputFile.length())
							fileIndex = newIndex;
						else
							System.out.println("Input file only " + inputFile.length() + " bytes");
					} catch (NumberFormatException e) {
						System.out.println("Could not read " + resp);
					}
				}
				else if (resp.equals("10")) { //advance words
					System.out.println("Enter Distance> ");
					resp = console.readLine();
					try {
						int advance = Integer.parseInt(resp);
						if (advance * 2 + fileIndex < inputFile.length())
							fileIndex += advance *2;
						else
							System.out.println("Input file only " + inputFile.length() + " bytes");
					} catch (NumberFormatException e) {
						System.out.println("Could not read " + resp);
					}
				}
				else if (resp.equals("11")) { //print record size
					int recordSize = -1;
					/*if (fileIndex <= 100)
						recordSize = getRecordLength(100);
					else {*/
						int lastPosition = findPreviousRecord(fileIndex);
						int newPosition = findNextRecord(fileIndex);
						if (newPosition > fileIndex)
							newPosition = lastPosition;
						recordSize = getRecordLength(newPosition);
					//}
					System.out.println("Record is " + recordSize + " bytes");							
				}
				else if (resp.equals("12")) { //close file
					state = State.CHOOSE_FILE;
				}
	
			}
		}
	
	}
	
	private int getRecordCount() {
		int count = 0;
		int index=100;
		int recordLength = getRecordLength(index);
		while (recordLength > 0) {
			++count;
			index += recordLength;
			recordLength = getRecordLength(index);
		}
		return count;
	}
	
	private int findNextRecord(int fromIndex) {
		if (fromIndex < 100)
			return 100;
		int newIndex = 100;
		int recLength = -1;
		while (newIndex <= fromIndex && recLength != 0 ) {
			recLength = getRecordLength(newIndex);
			newIndex += recLength;
		}
		if (recLength == 0)
			return -1;
		return newIndex;		
	}
	
	private int findPreviousRecord(int fromIndex) {
		int currentIndex = 100;
		int newIndex = currentIndex;
		int previousIndex = currentIndex;
		int recLength = 1;
		while (newIndex < fromIndex && recLength > 0 ) {
			previousIndex = currentIndex;
			currentIndex = newIndex;
			recLength = getRecordLength(newIndex);
			newIndex += recLength;
		}
		if (recLength == 0)
			return -1;
		if (newIndex == fileIndex)
			return currentIndex;
		else
			return previousIndex;		
	}
	
	private boolean advanceToRecord(int recordNum) {
		int newIndex = 100;
		int recLength = -1;
		while (--recordNum > 0 && recLength != 0 ) {
			recLength = getRecordLength(newIndex);
			newIndex += recLength;
		}
		if (recLength == 0)
			return false;
		fileIndex = newIndex;
		return true;
		
	}
	
	private int getRecordLength(int offset) {
		if (offset + 8 > inputFile.length())
			return 0;
		
		return readInt(offset + 4, BIG) * 2 + 8;
	}
	
	private int readInt(int index, final int endian) {
		byte[] result = new byte[4];
		for (int i = 0; i < result.length; ++i)
			result[i] = inputData[index + i];
		if (endian == BIG)
			return ByteBuffer.wrap(result).getInt();
		return ByteBuffer.wrap(result).order(ByteOrder.LITTLE_ENDIAN).getInt();
	}
	
	private double readDouble(int index, final int endian) {
		byte[] result = new byte[8];
		for (int i = 0; i < result.length; ++i)
			result[i] = inputData[index + i];
		if (endian == BIG)
			return ByteBuffer.wrap(result).getDouble();
		return ByteBuffer.wrap(result).order(ByteOrder.LITTLE_ENDIAN).getDouble();
	}
	

	public static void main(String[] args) {
		ShapefileReader reader = new ShapefileReader();
		try {
			reader.runMenu();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
