package com.github.jrddupont.solver;

import com.github.jrddupont.BoardNode;

import java.util.ArrayList;
import java.util.List;

public abstract class Solver {
    public abstract List<BoardNode> solve(int steps, BoardNode node);

    public static List<BoardNode> getPathFromEndNode(BoardNode endNode){
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

        return forwardsList;
    }
}
