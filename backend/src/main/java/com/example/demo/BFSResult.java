package com.example.demo;

import java.util.List;

public class BFSResult {
    private final List<String> path;
    private final int nodesExplored;

    public BFSResult(List<String> path, int nodesExplored) {
        this.path = path;
        this.nodesExplored = nodesExplored;
    }

    public List<String> getPath() {
        return path;
    }

    public int getNodesExplored() {
        return nodesExplored;
    }
} 