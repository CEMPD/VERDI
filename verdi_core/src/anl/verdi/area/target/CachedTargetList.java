package anl.verdi.area.target;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ucar.unidata.geoloc.Projection;

public class CachedTargetList {
	
	String filename = null;
	int rows = 0;
	int columns = 0;
	double westEdge = 0;
	double southEdge = 0;
	double cellWidth = 0;
	double cellHeight = 0;
	String idString = null;
	
	private static Map<String, CachedTargetList> CACHED_TARGET_LIST = new HashMap<String, CachedTargetList>();
	
	public synchronized static CachedTargetList getCachedTargetList(Target target, int rows, int columns, double westEdge, double southEdge, double cellWidth, double cellHeight) {
		String sourcePath = target.getSource().getURL().getPath();
		CachedTargetList listKey = new CachedTargetList(sourcePath, rows, columns, westEdge, southEdge, cellWidth, cellHeight);
		
		if (CACHED_TARGET_LIST.containsKey(listKey.idString))
			return CACHED_TARGET_LIST.get(listKey.idString);
		CACHED_TARGET_LIST.put(listKey.idString, listKey);
		Set<String> cachesForPath = FILE_ID_MAP.get(sourcePath);
		if (cachesForPath == null) {
			cachesForPath = new HashSet<String>();
			FILE_ID_MAP.put(sourcePath,  cachesForPath);
		}
		cachesForPath.add(listKey.idString);
		
		return listKey;				
	}
	
	private CachedTargetList(String filename, int rows, int columns, double westEdge, double southEdge, double cellWidth, double cellHeight) {
		this.filename = filename;
		this.rows = rows;
		this.columns = columns;
		this.westEdge = westEdge;
		this.southEdge = southEdge;
		this.cellWidth = cellWidth;
		this.cellHeight = cellHeight;
		idString = filename + "." + rows + "." + columns + "." + westEdge + "." + southEdge + "." + cellWidth + "." + cellHeight;

	}

	public int hashCode() {
		return idString.hashCode();
	}
	
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof CachedTargetList))
			return false;
		CachedTargetList source = (CachedTargetList)obj;
		return source.idString.equals(idString);
	}

	static Map<String, Map<String, TargetAreaInfo>> ID_TYPE_MAP = new HashMap<String, Map<String, TargetAreaInfo>>();
	static Map<String, Set<String>> FILE_ID_MAP = new HashMap<String, Set<String>>();
	
	public TargetAreaInfo getCachedAreaInfo(Target target, Projection projection, int rows, int columns, double westEdge, double southEdge, double cellWidth, double cellHeight) {
		
		String mapPath = target.getSource().getURL().getPath();
		String idField = target.getSource().getName();
		String targetId = target.getName();
		
		Map<String, TargetAreaInfo> areaInfoMap = ID_TYPE_MAP.get(idField);
		if (areaInfoMap == null) {
			areaInfoMap = new HashMap<String, TargetAreaInfo>();
			ID_TYPE_MAP.put(idField, areaInfoMap);
			Set<String> fileMap = FILE_ID_MAP.get(mapPath);
			if (fileMap == null) {
				fileMap = new HashSet<String>();
				FILE_ID_MAP.put(mapPath, fileMap);
			}
			fileMap.add(idField);
		}
		
		return areaInfoMap.get(targetId);
		
	}
	
	public static void closeFile(String path) {
		Set<String> caches = FILE_ID_MAP.get(path);
		if (caches == null)
			return;
		for (String id : caches) {
			CACHED_TARGET_LIST.remove(id);
			ID_TYPE_MAP.remove(id);
		}
		FILE_ID_MAP.remove(path);
	}
	
    public void cacheAreaInfo(Target target, int[] rowIndex, int[] colIndex, double[] overlapArea) {
		String idField = target.getSource().getName();
		String targetId = target.getName();
		
		Map<String, TargetAreaInfo> areaInfoMap = ID_TYPE_MAP.get(idField);
		if (areaInfoMap == null) {
			areaInfoMap = new HashMap<String, TargetAreaInfo>();
			ID_TYPE_MAP.put(idField, areaInfoMap);
		}
		
		TargetAreaInfo areaInfo = new TargetAreaInfo(target.area, rowIndex, colIndex, overlapArea);
		
		areaInfoMap.put(targetId, areaInfo);
    }


}
