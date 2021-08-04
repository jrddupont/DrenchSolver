package com.github.jrddupont;

import com.github.jrddupont.board.Board;

import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

public class FloodSolver {
	public static final String PROFILER_MAIN = "PROFILER_MAIN";
	public static final String PROFILER_OPENLIST = "PROFILER_OPENLIST";
	public static List<BoardNode> solve(Board board, int numberOfColors, int steps){
		nodeCache = new HashMap<>();



		BoardNode initialNode = new BoardNode(board, -1, numberOfColors);
		initialNode.clicksFromStart = 0;


		//BoardNode endNode = directedSearch(steps, initialNode);
		BoardNode endNode = startGreedyDepthFirst(steps, initialNode);

		if(endNode == null){
			return null;
		} else {
			ArrayList<BoardNode> backwardsList = new ArrayList<>();
			BoardNode backtrackNode = endNode;
			while(backtrackNode.clickedColor != -1){
				backwardsList.add(backtrackNode);
				backtrackNode = backtrackNode.parent;
			}
			backwardsList.add(backtrackNode);

			ArrayList<BoardNode> forwardsList = new ArrayList<>(backwardsList.size());
			for (int i = backwardsList.size() - 1; i >= 0; i--) {
				forwardsList.add(backwardsList.get(i));
			}
			System.out.println("Number of steps: " + (forwardsList.size() - 1));
			return forwardsList;
		}
	}

	static long count = 1;
	static void printUpdate(){
		if(count % 100000 == 0){
			System.out.println("Investigated nodes: " + NumberFormat.getInstance().format(count));
		}
		count++;
	}

	private static BoardNode startGreedyDepthFirst(int steps, BoardNode node){
		count = 1;
		return greedyDepthFirst(steps, node);
	}

	private static BoardNode greedyDepthFirst(int steps, BoardNode node){
		printUpdate();
		if(node.getNumberOfNodes() == 1){
			return node;
		}

		if(node.clicksFromStart == steps){
			return null;
		}

		PriorityQueue<BoardNode> priorityNeighbors = new PriorityQueue<>(Comparator.comparing(BoardNode::getHeuristic));
		priorityNeighbors.addAll( node.getNeighbors(false) );

		while(!priorityNeighbors.isEmpty()){
			BoardNode currentNode = priorityNeighbors.poll();

			currentNode.parent = node;
			currentNode.clicksFromStart = node.clicksFromStart + 1;

			BoardNode returnValue = greedyDepthFirst(steps, currentNode);
			if(returnValue != null){
				return returnValue;
			}
		}
		return null;
	}

	private static BoardNode directedSearch(int steps, BoardNode startNode) {
		PriorityQueue<BoardNode> openSet = new PriorityQueue<>(Comparator.comparing(BoardNode::getHeuristic));
		Set<BoardNode> closedSet = new HashSet<>();

		Main.profiler.put(PROFILER_MAIN, 0L);
		Main.profiler.put(PROFILER_OPENLIST, 0L);

		openSet.add(startNode);
		BoardNode endNode = null;

		int counter = 0;

		while(!openSet.isEmpty()) {

			if(counter >= 1000){
				counter = 0;
				System.out.println("O: " + openSet.size() + ", C: " + closedSet.size() + ", POLL/MAIN: " + 100*(Main.profiler.get(PROFILER_OPENLIST) / (1.0*Main.profiler.get(PROFILER_MAIN))));
			}
			counter++;


			long profilerMainStart = System.currentTimeMillis();

			BoardNode currentNode = openSet.poll();
			closedSet.add(currentNode);


			if(currentNode.getNumberOfNodes() == 1){
				endNode = currentNode;
				break;
			}

			if(currentNode.clicksFromStart == steps){
				continue;
			}

			List<BoardNode> neighbors = currentNode.getNeighbors(true);
			for(BoardNode neighbor : neighbors) {

				long profilerContainsStart = System.currentTimeMillis();
				boolean nodeNotAlreadyInvestigated = !closedSet.contains(neighbor) && !openSet.contains(neighbor);
				Main.updateProfiler(PROFILER_OPENLIST, profilerContainsStart);

				if (nodeNotAlreadyInvestigated) {
					neighbor.parent = currentNode;
					neighbor.clicksFromStart = currentNode.clicksFromStart + 1;
					openSet.add(neighbor);
				}
			}
			Main.updateProfiler(PROFILER_MAIN, profilerMainStart);
		}
		return endNode;
	}

	private static Map<BoardNode, BoardNode> nodeCache = new HashMap<>();
	public static BoardNode getUniqueBoardNode(BoardNode boardNode) {
		if(nodeCache.containsKey(boardNode)){
			return nodeCache.get(boardNode);
		} else {
			nodeCache.put(boardNode,boardNode);
			return boardNode;
		}
	}
}
