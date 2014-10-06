package anl.verdi.area.target;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages

import anl.verdi.area.FileHelper;

class Region{
  String name;
  String id;
  int keyType;
  public String toString(){
    return name;
  }
}
/**
 * 
 * File Name:RegionNames.java Description: A list of watershed regions
 * 
 * @version February 22, 2006
 * @author Mary Ann Bitz
 * @author Argonne National Lab
 * 
 */
public class RegionNames extends HashMap {

  /**
	 * 
	 */
	private static final long serialVersionUID = -252102419026607921L;
	static final Logger Logger = LogManager.getLogger(RegionNames.class.getName());

/**
   * Construct a new list of species
   * 
   */
  public RegionNames() {}

	/**
   * Copy the passed in list to this one
   * 
   * @param list
   *          the list to copy
   */
  public void copyList(RegionNames list) {
    clear();
    for (int i = 0; i < list.size(); i++) {
 // add(new Species((Species)list.get(i)));
    }
  }
  /**
   * Load in default chemical species weights
   * 
   */
  public void loadDefaultData() {
    String dir = System.getProperty("speciesData");
    if(dir==null)return;
    if (!loadData(FileHelper.readDataLines(dir+"/watershedSegments.txt"))) {
      Logger.error("Unable to load watershed segments file ");
    }
  }
  /**
   * Load in a given filename with species weights
   * 
   * @param fileName
   *          the file to read
   */
  public void loadData(String fileName) {
    if (!loadData(FileHelper.readDataLines(fileName))) {
      Logger.error("Unable to load watershed file " + fileName);
    }
  }
  static String[] keys = {"Region","Subregion","Accounting","Cataloging"};
  static final int REGION=0;
  static final int SUBREGION=1;
  static final int ACCOUNTING=2;
  static final int CATALOGING=3;
  
  public static int getKeyType(String key){
    switch(key.charAt(0)){
    case 'R':
      if(key.equals("Region"))return REGION;
      return -1;
    case 'S':
      if(key.equals("Subregion"))return SUBREGION;
      return -1;
    case 'A':
      if(key.equals("Accounting"))return ACCOUNTING;
      return -1;
    case 'C':
      if(key.equals("Cataloging"))return CATALOGING;
      return -1;
    }
    return -1;
  }
  static ArrayList<String> getAllTokens(String line, String pattern){
    ArrayList<String> results=new ArrayList<String>();
    StringTokenizer token= new StringTokenizer(line,pattern);
    while(token.hasMoreTokens()){
      results.add(token.nextToken());
    }
    return results;
  }
  public void addRegion(String id,int keyType,String fullName){
    Region region = new Region();
    region.id=id;
    region.keyType=keyType;
    region.name=fullName;
//Logger.debug(fullName);  
    put(id,region);
  }
  /**
   * Create species data from the given data
   * 
   * @param data
   *          the list of data from the file
   * @return whether it was successfully loaded
   */
  public boolean loadData(Vector data) {
    // open up the file and read it in to arrays
    if (data == null || data.isEmpty())
      return false;
    Pattern dubDash = Pattern.compile("--");
    Pattern colon = Pattern.compile(":");
    // create species data
    // clear the array
    clear();
    try {

    for(int lineNum=5;lineNum<data.size();lineNum++){
      String line=(String)data.get(lineNum);
      line = line.trim();
      
      // split on the double dash
      String[] matches = dubDash.split(line);
      if(matches.length<2)continue;
      // then split the left side on spaces
      ArrayList<String> tokens = getAllTokens(matches[0]," ");
      
      if(tokens.isEmpty())continue;
      String firstName = tokens.get(0);
      int keyType=getKeyType(firstName);
      if(keyType<0){
        // see if it is a cataloging unit anyway
        try{
          if(tokens.size()>0 && matches.length>1){
 //           Integer val=new Integer(tokens.get(0).trim());
            String name = matches[1].trim();
            if(lineNum+1<data.size()){
            String nextLine = ((String)data.get(lineNum+1)).trim();
            if(!nextLine.startsWith("Area"))name = name+nextLine;
            }
            // see if next line starts with word Area
            // then read it as a catalog value
            addRegion(tokens.get(0),CATALOGING,name);
          }
          continue;
        
        }catch(NumberFormatException e){}
      }

      if(keyType>=0){
        String id = tokens.get(1);
        String fullName="";
        
        // get the key
        if(keyType==REGION){
          fullName=tokens.get(2);
          for(int i=3;i<tokens.size();i++){
            fullName=fullName+" "+tokens.get(i);
          }
        }else if(keyType==SUBREGION){
          String[] matches2 = colon.split(matches[1]);
          if(matches.length==0)fullName=matches[1].trim();
          else fullName=matches2[0].trim();
        }else if(keyType==ACCOUNTING||keyType==CATALOGING){
          id = tokens.get(2);
          String[] matches2 = colon.split(matches[1]);
          // if there is no colon in the line
          if(matches2.length<2){
            fullName=matches[1].trim();
            if(lineNum+1<data.size()){
              String nextLine = ((String)data.get(lineNum+1)).trim();
              if(nextLine.length()>0&& !nextLine.startsWith("Area"))fullName = fullName+" "+nextLine;
            }
          }
          else fullName=matches2[0].trim();
          
        }
        addRegion(id,keyType,fullName);
        
    }
    } 
    
    } catch (Exception e) {
      Logger.error("Format Error in Region file");
      return false;
    }
    return true;
  }
	
  /**
   * Get a species matching a given name
   * 
   * @return the desired species from the global list
   */
  public static String getRegionName(String keyName) {
    // add missing leading 0s
    if(keyName.length()%2==1)keyName="0"+keyName;
    Region region = (Region)regions.get(keyName);
    if(region==null)return null;
    return region.name;
  }
  static RegionNames regions;
  static{
    regions=new RegionNames();
    regions.loadDefaultData();
  }
  public static RegionNames getAllRegions() {
    return regions;
  }
}