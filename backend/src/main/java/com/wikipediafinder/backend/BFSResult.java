package com.wikipediafinder.backend;

import java.util.List;
import java.util.Collections;
import java.util.ArrayList;

public class BFSResult {
    private final List<String> path;
    private final int nodesExplored;

    public BFSResult(List<String> path, int nodesExplored) {
        if (path == null) {
            this.path = null;
        } else {
            this.path = Collections.unmodifiableList(new ArrayList<>(path));
        }
        this.nodesExplored = nodesExplored;
    }

    public List<String> getPath() {
        return path;
    }

    public int getNodesExplored() {
        return nodesExplored;
    }
}