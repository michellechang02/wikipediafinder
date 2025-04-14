package com.example.demo;

import java.util.*;

public class BFS {
    /**
     * Find the path from the start node to the end node using Breadth-First Search (BFS).
     *
     * @param start - the PageNode of the beginning node
     * @param end   - the PageNode of the target
     * @return a list of the path to get from the start node to the end node
     * @return null if the query takes too long (>=1000 nodes considered) or if no path exists
     */

    public static List<String> getPath(PageNode start, PageNode end) {
        // check that start and end nodes are different
        if (start == null || end == null) {
            throw new IllegalArgumentException("Start and end nodes cannot be null.");
        }

        // Queue for nodes
        Queue<PageNode> q = new LinkedList<>();

        // keep track of discovered nodes
        Set<String> discovered = new HashSet<>();

        // Keep track of parents: <K, V> = <NODE, PARENT>
        Map<String, String> parents = new HashMap<>();

        // default pagenode
        PageNode marker = new PageNode("https://en.wikipedia.org");
        q.add(start);
        q.add(marker);
        Set<PageNode> neighbors;

        /*
         * Create BFS Tree
         *
         * NOTE: We put a hard cap of 1000 considered nodes for a single query. Any queries
         * that require more than 1000 considered nodes will not return an answer.
         */
        int nodeCnt = 1;
        boolean hasFinished = false;
        while (!q.isEmpty() && nodeCnt <= 1000) {
            // remove the first element in the queue
            PageNode temp = q.remove();

            if (temp.equals(marker)) {
                if (q.isEmpty()) {
                    break;
                }
                temp = q.remove();
                q.add(marker);
            }

            // add it to discovered
            discovered.add(temp.getURL());

            // get its neighbors
            temp.findOutgoingLinks();
            neighbors = temp.getOutNodes();

            // discover its neighbors and put them in the queue
            for (PageNode neighbor : neighbors) {
                if (!discovered.contains(neighbor.getURL())) {
                    discovered.add(neighbor.getURL());
                    parents.put(neighbor.getURL(), temp.getURL());
                    q.add(neighbor);

                    nodeCnt++;
                }
            }

            // if neighbors contains your end node, break.
            if (discovered.contains(end.getURL())) {
                hasFinished = true;
                break;
            }
        }

        if (hasFinished) {
            // Create result list
            List<String> result = new LinkedList<>();
            String temp = end.getURL();
            while (!temp.equals(start.getURL())) {
                result.add(temp);
                temp = parents.get(temp);
            }
            result.add(start.getURL());

            Collections.reverse(result);

            // Note that all the paths start with https://en.wikipedia.org/wiki/
            return result;
        } else {
            // this means that the query took too long
            return null;
        }
    }

    public static BFSResult getPathWithStats(PageNode start, PageNode end) {
        // check that start and end nodes are different
        if (start == null || end == null) {
            throw new IllegalArgumentException("Start and end nodes cannot be null.");
        }

        // Queue for nodes
        Queue<PageNode> q = new LinkedList<>();

        // keep track of discovered nodes
        Set<String> discovered = new HashSet<>();

        // Keep track of parents: <K, V> = <NODE, PARENT>
        Map<String, String> parents = new HashMap<>();

        // default pagenode
        PageNode marker = new PageNode("https://en.wikipedia.org");
        q.add(start);
        q.add(marker);
        Set<PageNode> neighbors;

        int nodeCnt = 1;
        boolean hasFinished = false;
        while (!q.isEmpty() && nodeCnt <= 1000) {
            // remove the first element in the queue
            PageNode temp = q.remove();

            if (temp.equals(marker)) {
                if (q.isEmpty()) {
                    break;
                }
                temp = q.remove();
                q.add(marker);
            }

            // add it to discovered
            discovered.add(temp.getURL());

            // get its neighbors
            temp.findOutgoingLinks();
            neighbors = temp.getOutNodes();

            // discover its neighbors and put them in the queue
            for (PageNode neighbor : neighbors) {
                if (!discovered.contains(neighbor.getURL())) {
                    discovered.add(neighbor.getURL());
                    parents.put(neighbor.getURL(), temp.getURL());
                    q.add(neighbor);

                    nodeCnt++;
                }
            }

            // if neighbors contains your end node, break.
            if (discovered.contains(end.getURL())) {
                hasFinished = true;
                break;
            }
        }

        if (hasFinished) {
            // Create result list
            List<String> result = new LinkedList<>();
            String temp = end.getURL();
            while (!temp.equals(start.getURL())) {
                result.add(temp);
                temp = parents.get(temp);
            }
            result.add(start.getURL());

            Collections.reverse(result);

            // Note that all the paths start with https://en.wikipedia.org/wiki/
            return new BFSResult(result, nodeCnt);
        } else {
            // this means that the query took too long
            return new BFSResult(null, nodeCnt);
        }
    }
}
