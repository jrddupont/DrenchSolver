package com.github.jrddupont;

import com.github.jrddupont.board.Board;
import com.github.jrddupont.solver.GreedyDepthFirst;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FloodSolver {
	public static List<BoardNode> solve(Board board, int numberOfColors, int steps){
		nodeCache = new HashMap<>();

		BoardNode initialNode = new BoardNode(board, -1, numberOfColors);
		initialNode.clicksFromStart = 0;

		System.out.println();
		System.out.println("Starting " + steps + ": ");
		return new GreedyDepthFirst().solve(steps, initialNode);
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
