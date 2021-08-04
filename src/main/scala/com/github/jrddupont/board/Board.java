package com.github.jrddupont.board;

import java.awt.*;
import java.util.List;
import java.util.*;

public class Board {

	private final boolean[][] regionAdjacencyMatrix;
	private final int[] regionColorIDs;
	private final int[] regionVolumes;

	public Board(int[][] colorMap) {
		// Generate the list of regions and a map of which region each pixel belongs to
		int[][] regionMap = new int[colorMap[0].length][colorMap.length];
		for(int x = 0; x < regionMap.length; x++){
			for(int y = 0; y < regionMap[0].length; y++){
				regionMap[x][y] = -1;
			}
		}
		Set<Point> edges = new HashSet<>();

		List<Region> regionList = new ArrayList<>();
		int regionID = 0;
		while (true){
			Point newRegionStart = findNewRegion(regionMap);
			if(newRegionStart == null){
				break;
			}
			int regionColor = colorMap[newRegionStart.x][newRegionStart.y];
			Region region = new Region(regionColor, regionID, newRegionStart);


			Stack<Point> openSet = new Stack<>();
			openSet.add(newRegionStart);
			regionMap[newRegionStart.x][newRegionStart.y] = regionID;
			int volume = 0;
			while(!openSet.isEmpty()){
				Point currentPoint = openSet.pop();
				volume++;
				int x = currentPoint.x;
				int y = currentPoint.y;

				investigatePoint(x - 1, y, openSet, colorMap, regionMap, region, edges);
				investigatePoint(x + 1, y, openSet, colorMap, regionMap, region, edges);
				investigatePoint(x, y - 1, openSet, colorMap, regionMap, region, edges);
				investigatePoint(x, y + 1, openSet, colorMap, regionMap, region, edges);

			}
			region.volume = volume;
			regionList.add(region);
			regionID++;
		}
		int regions = regionList.size();

		regionColorIDs = new int[regions];
		regionVolumes = new int[regions];
		for(Region region : regionList){
			regionColorIDs[region.regionID] = region.color;
			regionVolumes[region.regionID] = region.volume;
		}

		regionAdjacencyMatrix = new boolean[regions][regions];


		// Connect the regions
		for(Point edge : edges){
			int x = edge.x;
			int y = edge.y;

			connectRegions(x, y, x + 1, y, regionMap, regionAdjacencyMatrix);
			connectRegions(x, y, x - 1, y, regionMap, regionAdjacencyMatrix);
			connectRegions(x, y, x, y + 1, regionMap, regionAdjacencyMatrix);
			connectRegions(x, y, x, y - 1, regionMap, regionAdjacencyMatrix);
		}
	}

	public Board(boolean[][] regionAdjacencyMatrix, int[] regionColorIDs, int[] regionVolumes){
		this.regionAdjacencyMatrix = regionAdjacencyMatrix;
		this.regionColorIDs = regionColorIDs;
		this.regionVolumes = regionVolumes;
	}

	public Board colorRegion(int regionID, int colorID){
		// If the color is already the requested color, return null
		if(regionColorIDs[regionID] == colorID){
			return null;
		}

		// Determine if the color change will merge any connected regions
		ArrayList<Integer> matchingRegions = new ArrayList<>();
		int newVolume = regionVolumes[regionID];
		for(int i = 0; i < regionAdjacencyMatrix[regionID].length; i++){
			if(regionAdjacencyMatrix[regionID][i]){
				if(regionColorIDs[i] == colorID){
					matchingRegions.add(i);
					newVolume += regionVolumes[i];
				}
			}
		}
		// If not, simply return null
		if(matchingRegions.isEmpty()){
			return null;
		}

		int oldRegionCount = regionColorIDs.length;
		int newRegionCount = oldRegionCount - matchingRegions.size();

		// Construct a new board with the updated graph
		boolean[][] newRegionAdjacencyMatrix = new boolean[newRegionCount][newRegionCount];
		int[] newRegionColorIDs = new int[newRegionCount];
		int[] newRegionVolumes = new int[newRegionCount];

		// Since the number of regions is shrinking, we need to map all the regions to new numbers
		// The newly merged region is always '0' and the rest are assigned incrementally
		int[] transformationTable = new int[oldRegionCount];
		int currentRegionIndex = 1;
		for(int i = 0; i < oldRegionCount; i++){
			if(matchingRegions.contains(i) || i == regionID){
				transformationTable[i] = 0;
			} else {
				transformationTable[i] = currentRegionIndex;
				currentRegionIndex++;
			}
		}

		// Loop through the old adjacency matrix to find connections and connect the new ones using the transformation table
		for(int regionA = 0; regionA < oldRegionCount; regionA++){
			for(int regionB = 0; regionB < oldRegionCount; regionB++){
				if(regionAdjacencyMatrix[regionA][regionB]){
					newRegionAdjacencyMatrix[transformationTable[regionA]][transformationTable[regionB]] = true;
				}
			}
		}
		// Ensure no regions are connected to themselves
		for(int region = 0; region < newRegionCount; region++){
			newRegionAdjacencyMatrix[region][region] = false;
		}

		// Remap the color IDs accordingly
		for (int oldRegion = 0; oldRegion < oldRegionCount; oldRegion++) {
			if(oldRegion == regionID){
				newRegionColorIDs[transformationTable[oldRegion]] = colorID;
				newRegionVolumes[transformationTable[oldRegion]] = newVolume;
			} else {
				newRegionColorIDs[transformationTable[oldRegion]] = regionColorIDs[oldRegion];
				newRegionVolumes[transformationTable[oldRegion]] = regionVolumes[oldRegion];
			}
		}

		return new Board(newRegionAdjacencyMatrix, newRegionColorIDs, newRegionVolumes);
	}

//	public static Board copy(Board board){
//		int[] regionColors = board.regionColorIDs.clone();
//		boolean[][] regionAdjacencyMatrix = new boolean[board.regionAdjacencyMatrix.length][];
//		for(int i = 0; i < board.regionAdjacencyMatrix.length; i++){
//			regionAdjacencyMatrix[i] = board.regionAdjacencyMatrix[i].clone();
//		}
//
//		return new Board(regionAdjacencyMatrix, regionColors);
//	}

	private static void connectRegions(
			int startX, int startY,
			int endX, int endY,
			int[][] regionMap,
			boolean[][] regionAdjacencyMatrix
	){
		try{
			int startRegion = regionMap[startX][startY];
			int endRegion = regionMap[endX][endY];

			if(startRegion != endRegion){
				regionAdjacencyMatrix[startRegion][endRegion] = true;
				regionAdjacencyMatrix[endRegion][startRegion] = true;
			}
		} catch (IndexOutOfBoundsException ignored){}
	}

	private static void investigatePoint(
			int x, int y,
			Stack<Point> openSet,
			int[][] colorMap,
			int[][] regionMap,
			Region region,
			Set<Point> edges
	){
		try{
			if(colorMap[x][y] == region.color){
				if(regionMap[x][y] == -1){
					regionMap[x][y] = region.regionID;
					openSet.push(new Point(x, y));
				}
			} else {
				edges.add(new Point(x, y));
			}
		} catch (IndexOutOfBoundsException ignored){}
	}

	private static Point findNewRegion(int[][] regionMap){
		for(int x = 0; x < regionMap.length; x++){
			for(int y = 0; y < regionMap[0].length; y++){
				if(regionMap[x][y] == -1){
					return new Point(x, y);
				}
			}
		}
		return null;
	}



	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Board board = (Board) o;
		return Arrays.deepEquals(regionAdjacencyMatrix, board.regionAdjacencyMatrix) && Arrays.equals(regionColorIDs, board.regionColorIDs);
	}

	@Override
	public int hashCode() {
		int result = Arrays.deepHashCode(regionAdjacencyMatrix);
		result = 31 * result + Arrays.hashCode(regionColorIDs);
		return result;
	}

	public int[] getRegionColorIDs() {
		return regionColorIDs;
	}
	public int getNumberOfNodes(){
		return regionAdjacencyMatrix.length;
	}

	public boolean[][] getRegionAdjacencyMatrix() {
		return regionAdjacencyMatrix;
	}

	public int[] getRegionVolumes() {
		return regionVolumes;
	}

	public int getTopLeftRegion(){
		return 0; // Please god work
	}

	public int getNumberOfUniqueColors(){
		Set<Integer> set = new HashSet<>();
		for(Integer i : regionColorIDs){
			set.add(i);
		}
		return set.size();
	}
	public int getVolumeOfUpperLeftRegion(){
		return regionVolumes[getTopLeftRegion()];
	}

}
