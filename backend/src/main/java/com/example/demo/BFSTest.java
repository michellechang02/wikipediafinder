package com.example.demo;

import org.junit.Test;

import java.util.*;

import static junit.framework.TestCase.assertTrue;

public class BFSTest {


    @Test
    public void testPageNodeWithMockLinks() {
        // Mock PageNode behavior
        PageNode start = new PageNode("https://en.wikipedia.org/wiki/Albert_Einstein") {
            @Override
            public void findOutgoingLinks() {
                // Mock outgoing links
                PageNode sanrio = new PageNode("https://en.wikipedia.org/wiki/Sanrio");
                PageNode helloKitty = new PageNode("https://en.wikipedia.org/wiki/Hello_Kitty");
                PageNode japaneseCulture = new PageNode("https://en.wikipedia.org/wiki/Japanese_culture");
                PageNode kawaii = new PageNode("https://en.wikipedia.org/wiki/Kawaii");

                // Create a map to store outgoing links
                Map<String, PageNode> outLinks = new HashMap<>();

                // Add each PageNode to the map
                outLinks.put(sanrio.getURL(), sanrio);
                outLinks.put(helloKitty.getURL(), helloKitty);
                outLinks.put(japaneseCulture.getURL(), japaneseCulture);
                outLinks.put(kawaii.getURL(), kawaii);

                // Set the outgoing links
                setOutLinks(outLinks);
            }
        };

        // Simulate calling findOutgoingLinks
        start.findOutgoingLinks();

        // Validate the mocked behavior
        Set<String> links = start.getOutLinks();
        int i = 0;
        for (String e: links) {
            System.out.printf("Link %d: %s", i++, e);
        }
        assertTrue(links.contains("https://en.wikipedia.org/wiki/Sanrio"));
    }
}
