package com.github.jrddupont;

import com.github.jrddupont.board.Board;

import java.util.*;

import static com.github.jrddupont.FloodSolver.getUniqueBoardNode;

public class BoardNode {

	public int clicksFromStart = -1;

	public BoardNode parent = null;
	Board board;
	int numberOfColors;
	public int clickedColor;

	public BoardNode(Board board, int clickedColor, int numberOfColors){
		this.board = board;
		this.clickedColor = clickedColor;
		this.numberOfColors = numberOfColors;
	}

	List<BoardNode> getNeighbors(boolean cacheBoards){
		ArrayList<BoardNode> neighbors = new ArrayList<>(numberOfColors);

		for(int colorID = 0; colorID < numberOfColors; colorID++){
			Board newBoard = board.colorRegion(board.getTopLeftRegion(), colorID);
			if(newBoard != null){
				BoardNode newBoardNode = new BoardNode(newBoard, colorID, numberOfColors);
				BoardNode uniqueBoardNode = newBoardNode;
				if(cacheBoards){
					uniqueBoardNode = getUniqueBoardNode(newBoardNode);
				}
				neighbors.add(uniqueBoardNode);
			}
		}

		return neighbors;
	}

	public int getHeuristic(){
		return (14*14) - board.getVolumeOfUpperLeftRegion();
	}

	public int getNumberOfNodes(){
		return board.getNumberOfNodes();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		BoardNode boardNode = (BoardNode) o;

		return board.equals(boardNode.board);
	}

	@Override
	public int hashCode() {
		return board.hashCode();
	}
}
