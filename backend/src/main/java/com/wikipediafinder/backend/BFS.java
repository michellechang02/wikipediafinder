package com.wikipediafinder.backend;

import java.util.*;
import java.util.function.Function;

public class BFS {
    // Default factory for production
    private static final Function<String, PageNode> DEFAULT_FACTORY = PageNode::new;

    /**
     * Optimized BFS: Use a queue of URLs, cache outgoing links, and only create PageNode when expanding.
     */
    public static List<String> getPath(PageNode start, PageNode end) {
        return getPath(start, end, DEFAULT_FACTORY);
    }

    public static List<String> getPath(PageNode start, PageNode end, Function<String, PageNode> nodeFactory) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Start and end nodes cannot be null.");
        }
        String startUrl = start.getURL();
        String endUrl = end.getURL();
        if (startUrl.equals(endUrl)) {
            return Collections.singletonList(startUrl);
        }
        Queue<String> queue = new LinkedList<>();
        Set<String> discovered = new HashSet<>();
        Map<String, String> parents = new HashMap<>();
        Map<String, Set<String>> linkCache = new HashMap<>();
        queue.add(startUrl);
        discovered.add(startUrl);
        int nodeCnt = 0;
        while (!queue.isEmpty() && nodeCnt < 1000) {
            String currentUrl = queue.poll();
            nodeCnt++;
            // Fetch outgoing links (cache per URL)
            Set<String> neighbors = linkCache.get(currentUrl);
            if (neighbors == null) {
                PageNode node = nodeFactory.apply(currentUrl);
                node.findOutgoingLinks();
                neighbors = new HashSet<>();
                for (PageNode n : node.getOutNodes()) {
                    neighbors.add(n.getURL());
                }
                linkCache.put(currentUrl, neighbors);
            }
            for (String neighborUrl : neighbors) {
                if (!discovered.contains(neighborUrl)) {
                    discovered.add(neighborUrl);
                    parents.put(neighborUrl, currentUrl);
                    queue.add(neighborUrl);
                    if (neighborUrl.equals(endUrl)) {
                        // Early exit: reconstruct path
                        List<String> result = new LinkedList<>();
                        String temp = endUrl;
                        while (!temp.equals(startUrl)) {
                            result.add(temp);
                            temp = parents.get(temp);
                        }
                        result.add(startUrl);
                        Collections.reverse(result);
                        return result;
                    }
                }
            }
        }
        return null;
    }

    public static BFSResult getPathWithStats(PageNode start, PageNode end) {
        return getPathWithStats(start, end, DEFAULT_FACTORY);
    }

    public static BFSResult getPathWithStats(PageNode start, PageNode end, Function<String, PageNode> nodeFactory) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Start and end nodes cannot be null.");
        }
        String startUrl = start.getURL();
        String endUrl = end.getURL();
        if (startUrl.equals(endUrl)) {
            return new BFSResult(Collections.singletonList(startUrl), 1);
        }
        Queue<String> queue = new LinkedList<>();
        Set<String> discovered = new HashSet<>();
        Map<String, String> parents = new HashMap<>();
        Map<String, Set<String>> linkCache = new HashMap<>();
        queue.add(startUrl);
        discovered.add(startUrl);
        int nodeCnt = 0;
        while (!queue.isEmpty() && nodeCnt < 1000) {
            String currentUrl = queue.poll();
            nodeCnt++;
            Set<String> neighbors = linkCache.get(currentUrl);
            if (neighbors == null) {
                PageNode node = nodeFactory.apply(currentUrl);
                node.findOutgoingLinks();
                neighbors = new HashSet<>();
                for (PageNode n : node.getOutNodes()) {
                    neighbors.add(n.getURL());
                }
                linkCache.put(currentUrl, neighbors);
            }
            for (String neighborUrl : neighbors) {
                if (!discovered.contains(neighborUrl)) {
                    discovered.add(neighborUrl);
                    parents.put(neighborUrl, currentUrl);
                    queue.add(neighborUrl);
                    if (neighborUrl.equals(endUrl)) {
                        // Early exit: reconstruct path
                        List<String> result = new LinkedList<>();
                        String temp = endUrl;
                        while (!temp.equals(startUrl)) {
                            result.add(temp);
                            temp = parents.get(temp);
                        }
                        result.add(startUrl);
                        Collections.reverse(result);
                        return new BFSResult(result, nodeCnt);
                    }
                }
            }
        }
        // Not found or cap reached
        return new BFSResult(null, nodeCnt);
    }
}