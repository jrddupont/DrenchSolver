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

    // Recursive method of solving the puzzle. Basically we do a depth first search biased towards nodes that
    // make the expanding region bigger
    private static BoardNode startGreedyDepthFirst(int steps, BoardNode node){
        count = 1;
        return greedyDepthFirst(steps, node);
    }

    private static BoardNode greedyDepthFirst(int steps, BoardNode node){
        printUpdate();
        // Base case
        if(node.getNumberOfNodes() == 1){
            return node;
        }

        // Prune if the path is too long
        if(node.clicksFromStart == steps){
            return null;
        }

        // Get neighbors and put them in a priority queue
        PriorityQueue<BoardNode> priorityNeighbors = new PriorityQueue<>(Comparator.comparing(BoardNode::getHeuristic));
        priorityNeighbors.addAll( node.getNeighbors(false) );

        // Go one by one pulling the neighbors and recursively calling this method
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
