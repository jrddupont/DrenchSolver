package com.github.jrddupont.solver;

import com.github.jrddupont.BoardNode;

import java.util.*;

public class HeuristicSearch extends Solver{
    @Override
    public List<BoardNode> solve(int steps, BoardNode node) {
        BoardNode endNode = directedSearch(steps, node);
        return getPathFromEndNode(endNode);
    }

    private static BoardNode directedSearch(int steps, BoardNode startNode) {
        PriorityQueue<BoardNode> openSet = new PriorityQueue<>(Comparator.comparing(BoardNode::getHeuristic));
        Set<BoardNode> closedSet = new HashSet<>();

        openSet.add(startNode);
        BoardNode endNode = null;

        int counter = 0;

        while(!openSet.isEmpty()) {

            if(counter >= 1000){
                counter = 0;
                System.out.println("O: " + openSet.size() + ", C: " + closedSet.size());
            }
            counter++;


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

                boolean nodeNotAlreadyInvestigated = !closedSet.contains(neighbor) && !openSet.contains(neighbor);

                if (nodeNotAlreadyInvestigated) {
                    neighbor.parent = currentNode;
                    neighbor.clicksFromStart = currentNode.clicksFromStart + 1;
                    openSet.add(neighbor);
                }
            }
        }
        return endNode;
    }
}
