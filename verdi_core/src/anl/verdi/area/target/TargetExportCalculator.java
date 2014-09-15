package anl.verdi.area.target;

import java.util.ArrayList;

/**
 * 
 * File Name:TargetExportCalculator.java
 * Description:
 * Calculates grid intersections and depositions for targets
 * 
 * @version June 2004
 * @author Mary Ann Bitz
 * @author Argonne National Lab
 *
 */
public class TargetExportCalculator extends TargetCalculator {
  ArrayList baseFiles, projectedFiles, variables, resultants, outFiles;
  public boolean loadFiles=true;
  public void setFilesAndVariables(ArrayList b, ArrayList p, ArrayList v, ArrayList eq, ArrayList f) {
    baseFiles = b;
    projectedFiles = p;
    variables = v;
    resultants = eq;
    outFiles = f;
  }
  void cleanUp(){
//    if(loadFiles)ModelData.unloadAllData(true);
  }
  /**
   * Called by the progress monitor to start the task
   */
  public void doWork() {
//    int size=1;
//    if(baseFiles!=null)size=baseFiles.size();
//    for (int i = 0; i < size; i++) {
//      String outFileName = (String)outFiles.get(i);
//      if(loadFiles){
//        ModelData base = (ModelData)baseFiles.get(i);
//        ModelData projected = (ModelData)projectedFiles.get(i);
//        ArrayList vars = (ArrayList)variables.get(i);
//        String eqName = (String)resultants.get(i);
//        
//  System.out.println(""+base+" "+projected+" "+vars+" "+outFileName);
//        base.load(ModelData.BASE, vars, eqName, false);
//        if (projected != null)
//          projected.load(ModelData.PROJECTED, vars, eqName, false);
//      }
//      // calculate the grid intersections
//      if (!Target.allIndexFilesLoaded()) {
//        // try to read them in 
////System.out.println("trying to read intersections");
//        //readInIntersections();
//
//        // see if they broke out of it
//        if (canceled){
//          cleanUp();
//          return;
//        }
//
//        // if they did not load all of them in
//        if (!Target.allIndexFilesLoaded()) {
//System.out.println("calculating intersections");
//          // calculate where the grid cell intersections are located
//          boolean didCalcs = calculateIntersections(Target.getTargets());
//          if (didCalcs == false){
//            cleanUp();
//            return;
//          }
//
//          // see if they broke out of it
//          if (canceled){
//            cleanUp();
//            return;
//          }
//        }
//      }
//      // calculate the depositions
//      ArrayList results = Target.calculateAllDepositions();
//      Target.calculateDepositions();
//System.out.println("writing out data");
//      FileHelper.writeDataLines(outFileName, new Vector(results));
//    }
//    cleanUp();
//    done = true;
  }
  /**
   * An estimate of the length of the task
   */
  public int getLengthOfTask() {
	  
    ArrayList targets = Target.getTargets();
    if(baseFiles!=null)return targets.size()*baseFiles.size();
    return targets.size();
    
  }

}
