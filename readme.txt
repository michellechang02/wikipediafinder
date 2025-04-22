Name: Wikipedia Path Finder

Description:
Wikipedia Path Finder is a web application that finds the shortest hyperlink path between two Wikipedia articles using a Breadth-First Search (BFS) algorithm.
Users provide a starting and a target article, and the backend scrapes and traverses Wikipedia pages to determine the shortest connection path.
The application features a modern React TypeScript frontend and a Java Spring Boot backend.
To ensure performance, the search is limited to 1000 nodes per query with a cap of 10 outgoing links per page.
The tool showcases the interplay between graph traversal algorithms and information retrieval via real-time web scraping.

Categories:
- Graph and Graph Algorithms
- Document Search (aka Information Retrieval)

Work Breakdown:
- Michelle Chang: 
    - Implemented the BFS algorithm and PageNode class in Java
    - Created the Spring Boot backend API endpoints
    - Developed the React frontend with Tailwind CSS
    - Integrated the frontend and backend components
    - Set up the deployment pipeline to Vercel for the frontend
    - Configured the project documentation