package com.github.jrddupont.solver;

import com.github.jrddupont.BoardNode;

import java.text.NumberFormat;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class GreedyDepthFirst extends Solver {
    @Override
    public List<BoardNode> solve(int steps, BoardNode node) {
        BoardNode endNode = startGreedyDepthFirst(steps, node);
        return getPathFromEndNode(endNode);
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
}
