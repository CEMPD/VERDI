package anl.verdi.area.target;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages

/**
 * 
 * File Name:TargetSet.java
 * Description:
 * A set of related targets
 * 
 * @version April 2004
 * @author Mary Ann Bitz
 * @author Argonne National Lab
 *
 */
public class TargetSet extends ArrayList {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5612848337395477817L;
	static final Logger Logger = LogManager.getLogger(TargetSet.class.getName());
	// name of the target set
	String name;
	// the list of all sets of targets
	static ArrayList targetSets;

	// initializations
	static {
		targetSets = new ArrayList();
	}
	/**
	 * Copy constructor for a set of targets
	 * @param data target set to copy
	 */
	public TargetSet(TargetSet data) {
		this(data.name);
		addAll(data);

	}
	/**
	 * Constructor for a set of targets
	 * @param n name of the target set
	 */
	public TargetSet(String n) {
		super();
		name = n;
	}
	/**
	 * Sets the list of targets in the target set
	 * @param list the targets
	 */
	public void setTargets(ArrayList list) {
		clear();
		addAll(list);
	}
	/**
	 * Get the list of all target sets
	 * @return all target sets
	 */
	public static ArrayList getTargetSets() {
		return targetSets;
	}
	/**
	 * Find a target set with the given name
	 * @param name the name of the target set to look for
	 * @return the target set that matches
	 */
	public static TargetSet findTargetSet(String name) {
		for(int i=0;i<targetSets.size();i++){
			TargetSet set=(TargetSet)targetSets.get(i);
			if(set.name.equals(name))return set;
		}
		return null;
	}
	/**
	 * Get the first target set containing the target
	 * @param data
	 * @return
	 */
	public static TargetSet getTargetSetWithin(Target data) {
		for (int i = 0; i < targetSets.size(); i++) {
			TargetSet targetSet = (TargetSet)targetSets.get(i);
			if (targetSet.contains(data))
				return targetSet;
		}
		return null;
	}
	/**
	 * Set the target set list to the passed in one
	 * does a deep copy
	 * @param list list of targets to be copied
	 */
	public static void setTargetSets(ArrayList list) {
		targetSets = new ArrayList(list.size());
		for (int i = 0; i < list.size(); i++) {
			targetSets.add(new TargetSet((TargetSet)list.get(i)));
		}
	}
	/**
	 * Copy all target sets to a new list of them
	 * @return the copy list
	 */
	public static ArrayList copyTargetSets() {
		ArrayList list = new ArrayList(targetSets.size());
		for (int i = 0; i < targetSets.size(); i++) {
			list.add(new TargetSet((TargetSet)targetSets.get(i)));
		}
		return list;
	}
	/**
	 * Returns a list of all targets in the passed in list
	 * @param inList list of target sets and targets
	 * @return list of included targets
	 */
	public static ArrayList includedTargets(ArrayList inList){
		ArrayList list = new ArrayList();
		for(int i=0;i<inList.size();i++){
			Object obj=inList.get(i);
			if(obj instanceof TargetSet)list.addAll((TargetSet)obj);
			else list.add(obj);
		}
		return list;
	}
	/**
	 * Unload all target sets
	 *
	 */
	public static void unload(){
		targetSets.clear();
	}
	/**
	 * Load the target sets from a vector
	 * @param data the vector of data
	 * @param offset the current place in the vector
	 * @return the new offset
	 * @throws IOException
	 */
	public static int load(Vector data, int offset) throws IOException{
		try{
			// get the number of target sets
			Number nums = (Number)((Vector)data.get(offset)).get(0);
			offset=offset+1;

			// load the target sets
			for(int i=0;i<nums.intValue();i++){
				String name = (String)((Vector)data.get(offset)).get(0);
				// if it does not exist
				if(TargetSet.findTargetSet(name)==null){
					TargetSet set = new TargetSet(name);
					targetSets.add(set);

					Number numTargets = (Number)((Vector)data.get(offset)).get(1);
					offset=offset+1;
					for(int j=0;j<numTargets.intValue();j++){
						String targetName = (String)((Vector)data.get(offset)).get(0);
						offset=offset+1;
						// get the matching target
						Target target=Target.findTarget(targetName);
						if(target==null){
							Logger.error("Load Error: Cannot find target "+targetName);
							continue;
						}
						set.add(target);
					}
				}else{
					// skip target list
					Logger.error("Load Error: Duplicate set "+name);
					Number numTargets = (Number)((Vector)data.get(offset)).get(1);
					offset=offset+1;
					for(int j=0;j<numTargets.intValue();j++){
						String targetName = (String)((Vector)data.get(offset)).get(0);
						offset=offset+1;
					}
				}
			}

		}catch(ClassCastException ex){
			throw(new IOException("Load Error: Incorrect format reading target set data"));
		}
		return offset;
	}
	/**
	 * Save the target sets to a vector
	 * @param data
	 */
	/*
	public static void save(Vector data){
		data.add(Integer.toString(targetSets.size()));
		// save sets
		for(int i=0;i<targetSets.size();i++){
			TargetSet set = (TargetSet)targetSets.get(i);
			// save its name and size
			data.add(FileHelper.formString(set.toString())+", "+set.size());
			for(int j=0;j<set.size();j++){
              Object obj = set.get(j);
              if(obj instanceof TargetSet)data.add(FileHelper.formString(obj.toString()));
              else data.add(FileHelper.formString(((Target)obj).keyName));
			}
		}
	}
	 */
	/**
	 * Gets a string representing the target set
	 */
	public String toString() {
		return name;
	}
	/**
	 * Get the name of the target set
	 * @return
	 */
	public String getName() {
		return name;
	}

}
