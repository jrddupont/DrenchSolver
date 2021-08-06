package com.github.jrddupont.solver;

import com.github.jrddupont.BoardNode;

import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.*;

public class MultithreadedGreedyDepthFirst extends Solver{

    private final int threads;
    public MultithreadedGreedyDepthFirst(int threads){
        this.threads = threads;
    }

    @Override
    public List<BoardNode> solve(int steps, BoardNode node) {
        List<BoardNode> initialNodes = getInitialNodes(threads, node);
//        List<BoardNode> initialNodes = new ArrayList<>();
//        initialNodes.add(node);
        BoardNode endNode = startMultithreading(steps, initialNodes);

        return getPathFromEndNode(endNode);
    }

    private static List<BoardNode> getInitialNodes(int threads, BoardNode initialNode){
        ArrayList<BoardNode> finalList = new ArrayList<>();
        for(BoardNode currentNode : initialNode.getNeighbors(false)){
            currentNode.parent = initialNode;
            currentNode.clicksFromStart = 1;
            for(BoardNode nextLevelNode : currentNode.getNeighbors(false)){
                nextLevelNode.parent = currentNode;
                nextLevelNode.clicksFromStart = 2;

                finalList.add(nextLevelNode);
            }
        }
        return finalList;
    }

    private static BoardNode startMultithreading(int steps, List<BoardNode> initialNodes){
        int poolSize = initialNodes.size();
        ExecutorService pool = Executors.newFixedThreadPool(poolSize);
        CompletionService<BoardNode> service = new ExecutorCompletionService<>(pool);
        List<Future<BoardNode>> futures = new ArrayList<>(poolSize);
        count = 1;

        BoardNode result = null;

        try {
            for (BoardNode node : initialNodes) {
                Callable<BoardNode> s = () -> greedyDepthFirst(steps, node);
                futures.add(service.submit(s));
            }
            for (int i = 0; i < poolSize; ++i) {
                try {
                    BoardNode res = service.take().get();
                    if (res != null) {
                        result = res;
                        break;
                    }
                } catch (ExecutionException | InterruptedException ignore) {}
            }
        } finally {
            for (Future<BoardNode> f : futures) {
                f.cancel(true);
            }
        }

        return result;
    }

    static long count = 1;
    static synchronized void printUpdate(){
        if(count % 100000 == 0){
            System.out.println("Investigated nodes: " + NumberFormat.getInstance().format(count));
        }
        count++;
    }


    private static BoardNode greedyDepthFirst(int steps, BoardNode node){
        printUpdate();
        if(Thread.interrupted()){
            throw new RuntimeException();
        }
        if(node.getNumberOfNodes() == 1){
            return node;
        }

        if(node.clicksFromStart >= steps){
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
