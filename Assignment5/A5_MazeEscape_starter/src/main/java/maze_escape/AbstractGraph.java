package maze_escape;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public abstract class AbstractGraph<V> {

    /**
     * Graph representation:
     * this class implements graph search algorithms on a graph with abstract vertex type V
     * for every vertex in the graph, its neighbours can be found by use of abstract method getNeighbours(fromVertex)
     * this abstraction can be used for both directed and undirected graphs
     **/

    public AbstractGraph() {
    }

    /**
     * retrieves all neighbours of the given fromVertex
     * if the graph is directed, the implementation of this method shall follow the outgoing edges of fromVertex
     *
     * @param fromVertex
     * @return
     */
    public abstract Set<V> getNeighbours(V fromVertex);

    /**
     * retrieves all vertices that can be reached directly or indirectly from the given firstVertex
     * if the graph is directed, only outgoing edges shall be traversed
     * firstVertex shall be included in the result as well
     * if the graph is connected, all vertices shall be found
     *
     * @param firstVertex the start vertex for the retrieval
     * @return
     */
    public Set<V> getAllVertices(V firstVertex) {

        Set<V> verticesUntilNow = new HashSet<>();
        verticesUntilNow.add(firstVertex);

        return new HashSet<>(getAllVerticesHelperFunction(firstVertex, verticesUntilNow));
    }

    private Set<V> getAllVerticesHelperFunction(V vertice, Set<V> verticesUntilNow) {

        //loops deeper for more neighbours and adds the found vertices from the neighbours to the 'verticesUntilNow'
        for (V neighbour : getNeighbours(vertice)) {
            if (!verticesUntilNow.contains(neighbour)) {
                verticesUntilNow.add(neighbour);
                getAllVerticesHelperFunction(neighbour, verticesUntilNow);
            }
        }

        return verticesUntilNow;
    }


    /**
     * Formats the adjacency list of the subgraph starting at the given firstVertex
     * according to the format:
     * vertex1: [neighbour11,neighbour12,…]
     * vertex2: [neighbour21,neighbour22,…]
     * …
     * Uses a pre-order traversal of a spanning tree of the sub-graph starting with firstVertex as the root
     * if the graph is directed, only outgoing edges shall be traversed
     * , and using the getNeighbours() method to retrieve the roots of the child subtrees.
     *
     * @param firstVertex
     * @return
     */
    public String formatAdjacencyList(V firstVertex) {
        StringBuilder stringBuilder = new StringBuilder("Graph adjacency list:\n");

        //adds this vertex to the adjacency list
        stringBuilder.append(firstVertex).append(": ");
        stringBuilder.append(getNeighbours(firstVertex).toString().replaceAll("\\s+", "")).append("\n");

        //new set for traversed vertices
        Set<V> traversedVertices = new HashSet<>();
        traversedVertices.add(firstVertex);

        // return the result
        return adjecencyListHelper(stringBuilder, traversedVertices, firstVertex);
    }

    public String adjecencyListHelper(StringBuilder stringBuilder, Set<V> traversedVertices, V rootVertex) {
        //loop through neighbours and add everything to the adjacency list
        for (V vertex : getNeighbours(rootVertex)) {
            if (!traversedVertices.contains(vertex)) {
                //adds to the adjacency list
                stringBuilder.append(vertex).append(": ");
                stringBuilder.append(getNeighbours(vertex).toString().replaceAll("\\s+", "")).append("\n");
                //add it to the traversed list
                traversedVertices.add(vertex);

                //recursive call
                adjecencyListHelper(stringBuilder, traversedVertices, vertex);
            }
        }
        return stringBuilder.toString();
    }


    /**
     * represents a directed path of connected vertices in the graph
     */
    public class GPath {
        private Deque<V> vertices = new LinkedList<>();
        private double totalWeight = 0.0;
        private Set<V> visited = new HashSet<>();

        /**
         * representation invariants:
         * 1. vertices contains a sequence of vertices that are neighbours in the graph,
         * i.e. FOR ALL i: 1 < i < vertices.length: getNeighbours(vertices[i-1]).contains(vertices[i])
         * 2. a path with one vertex equal start and target vertex
         * 3. a path without vertices is empty, does not have a start nor a target
         * totalWeight is a helper attribute to capture total path length from a function on two neighbouring vertices
         * visited is a helper set to be able to track visited vertices in searches, only for analysis purposes
         **/
        private static final int DISPLAY_CUT = 10;

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(
                    String.format("Weight=%.2f Length=%d visited=%d (",
                            this.totalWeight, this.vertices.size(), this.visited.size()));
            String separator = "";
            int count = 0;
            final int tailCut = this.vertices.size() - 1 - DISPLAY_CUT;
            for (V v : this.vertices) {
                // limit the length of the text representation for long paths.
                if (count < DISPLAY_CUT || count > tailCut) {
                    sb.append(separator).append(v.toString());
                    separator = ", ";
                } else if (count == DISPLAY_CUT) {
                    sb.append(separator).append("...");
                }
                count++;
            }
            sb.append(")");
            return sb.toString();
        }

        /**
         * recalculates the total weight of the path from a given weightMapper that calculates the weight of
         * the path segment between two neighbouring vertices.
         *
         * @param weightMapper
         */
        public void reCalculateTotalWeight(BiFunction<V, V, Double> weightMapper) {
            this.totalWeight = 0.0;
            V previous = null;
            for (V v : this.vertices) {
                // the first vertex of the iterator has no predecessor and hence no weight contribution
                if (previous != null) this.totalWeight += weightMapper.apply(previous, v);
                previous = v;
            }
        }

        public Deque<V> getVertices() {
            return this.vertices;
        }

        public double getTotalWeight() {
            return this.totalWeight;
        }

        public Set<V> getVisited() {
            return this.visited;
        }
    }

    /**
     * Uses a depth-first search algorithm to find a path from the startVertex to targetVertex in the subgraph
     * All vertices that are being visited by the search should also be registered in path.visited
     *
     * @param startVertex
     * @param targetVertex
     * @return the path from startVertex to targetVertex
     * or null if target cannot be matched with a vertex in the sub-graph from startVertex
     */
    public GPath depthFirstSearch(V startVertex, V targetVertex) {

        if (startVertex == null || targetVertex == null) return null;

        //initialize the path
        GPath path = new GPath();

        //call recursive
        depthFirstSearchRecursive(startVertex, targetVertex, path);

        //if the target vertex is not found, return null
        if (!path.getVertices().contains(targetVertex)) {
            return null;
        }

        return path;
    }

    private void depthFirstSearchRecursive(V currentVertex, V targetVertex, GPath path) {

        //mark current vertex as visited
        path.getVertices().add(currentVertex);
        path.getVisited().add(currentVertex);

        //checks if the current vertex is the target, so we stop
        if (currentVertex.equals(targetVertex)) {
            return;
        }

        //traverse neighbour vertices
        for (V vertex : getNeighbours(currentVertex)) {
            if (!path.getVisited().contains(vertex)) {
                depthFirstSearchRecursive(vertex, targetVertex, path);
                if(path.getVertices().contains(targetVertex)){
                    return;
                }
            }
        }

        //traverse back if target vertex is not found
        path.getVertices().removeLast();
    }

    /**
     * Uses a breadth-first search algorithm to find a path from the startVertex to targetVertex in the subgraph
     * All vertices that are being visited by the search should also be registered in path.visited
     *
     * @param startVertex
     * @param targetVertex
     * @return the path from startVertex to targetVertex
     * or null if target cannot be matched with a vertex in the sub-graph from startVertex
     */
    public GPath breadthFirstSearch(V startVertex, V targetVertex) {

        if (startVertex == null || targetVertex == null) return null;

        GPath path = new GPath();

        //initialise the queue with the start vertex and mark it as visited
        path.getVertices().addLast(targetVertex);
        path.getVisited().add(startVertex);
        if (startVertex.equals(targetVertex)) return path;
        Queue<V> fifoQueue = new LinkedList<>();
        Map<V, V> visitedFrom = new HashMap<>();

        fifoQueue.offer(startVertex);
        visitedFrom.put(startVertex, null);

        V currentVertex = fifoQueue.poll();

        while (currentVertex != null) {
            //looping through all neighbors
            for (V neighbour : this.getNeighbours(currentVertex)) {
                //if target vertex is found, bail out.
                if (neighbour.equals(targetVertex)) {
                    while (currentVertex != null) {
                        path.getVertices().addFirst(currentVertex);
                        currentVertex = visitedFrom.get(currentVertex);
                        path.getVisited().add(currentVertex);
                    }
                    return path;
                } else if (!visitedFrom.containsKey(neighbour)) {
                    //mark and remember the new vertex for later processing
                    visitedFrom.put(neighbour, currentVertex);
                    path.getVisited().add(currentVertex);
                    fifoQueue.offer(neighbour);
                }
            }
            //pick the next vertex according to breadth first order
            currentVertex = fifoQueue.poll();
        }

        //no path found from start to target
        return null;
    }

    // helper class to build the spanning tree of visited vertices in dijkstra's shortest path algorithm
    // your may change this class or delete it altogether follow a different approach in your implementation
    private class MSTNode implements Comparable<MSTNode> {
        protected V vertex;                // the graph vertex that is concerned with this MSTNode
        protected V parentVertex = null;     // the parent's node vertex that has an edge towards this node's vertex
        protected boolean marked = false;  // indicates DSP processing has been marked complete for this vertex
        protected double weightSumTo = Double.MAX_VALUE;   // sum of weights of current shortest path towards this node's vertex

        private MSTNode(V vertex) {
            this.vertex = vertex;
        }

        // comparable interface helps to find a node with the shortest current path, sofar
        @Override
        public int compareTo(MSTNode otherMSTNode) {
            return Double.compare(weightSumTo, otherMSTNode.weightSumTo);
        }
    }

    /**
     * Calculates the edge-weighted shortest path from the startVertex to targetVertex in the subgraph
     * according to Dijkstra's algorithm of a minimum spanning tree
     *
     * @param startVertex
     * @param targetVertex
     * @param weightMapper provides a function(v1,v2) by which the weight of an edge from v1 to v2
     *                     can be retrieved or calculated
     * @return the shortest path from startVertex to targetVertex
     * or null if target cannot be matched with a vertex in the sub-graph from startVertex
     */
    public GPath dijkstraShortestPath(V startVertex, V targetVertex,
                                      BiFunction<V, V, Double> weightMapper) {

        if (startVertex == null || targetVertex == null) return null;

        // initialise the result path of the search
        GPath path = new GPath();
        path.visited.add(startVertex);

        // easy target
        if (startVertex.equals(targetVertex)) {
            path.vertices.add(startVertex);
            return path;
        }

        // a minimum spanning tree which tracks for every visited vertex:
        //   a) its (parent) predecessor in the currently shortest path towards this visited vertex
        //   b) the total weight of the currently shortest path towards this visited vertex
        //   c) a mark, indicating whether the current path towards this visited vertex is the final shortest.
        // (you may choose a different approach of tracking the MST of the algorithm, if you wish)
        Map<V, MSTNode> minimumSpanningTree = new HashMap<>();

        // initialise the minimum spanning tree with the startVertex
        MSTNode nearestMSTNode = new MSTNode(startVertex);
        nearestMSTNode.weightSumTo = 0.0;
        minimumSpanningTree.put(startVertex, nearestMSTNode);

        // TODO maybe more helper variables or data structures, if needed


        while (nearestMSTNode != null) {


            //mark curent node as processed
            nearestMSTNode.marked = true;
            path.visited.add(nearestMSTNode.vertex);

            //checks if current node is target vertex
            if(nearestMSTNode.vertex.equals(targetVertex)) {
                buildPathFromMST(path, minimumSpanningTree, startVertex, targetVertex);
                return path;
            }

            //loops through neighbors
            for(V neighbor : getNeighbours(nearestMSTNode.vertex)) {
                if(!path.visited.contains(neighbor)) {
                    //calculates total weight
                    double weightToNeighbor = nearestMSTNode.weightSumTo
                            + weightMapper.apply(nearestMSTNode.vertex, neighbor);

                    //adds new node to the spanning tree if the weight is lower.
                    MSTNode neighborNode = minimumSpanningTree.getOrDefault(neighbor, new MSTNode(neighbor));
                    if (weightToNeighbor < neighborNode.weightSumTo) {
                        neighborNode.weightSumTo = weightToNeighbor;
                        neighborNode.parentVertex = nearestMSTNode.vertex;
                        minimumSpanningTree.put(neighbor, neighborNode);
                    }
                }
            }

            //find the next nearest MSTNode that is not marked yet
            nearestMSTNode = null;
            double minWeight = Double.MAX_VALUE;
            for (MSTNode node : minimumSpanningTree.values()) {
                if (!node.marked && node.weightSumTo < minWeight) {
                    nearestMSTNode = node;
                    minWeight = node.weightSumTo;
                }
            }
        }

        //no path has been found
        return null;
    }

    private void buildPathFromMST(GPath path, Map<V, MSTNode> minimumSpanningTree, V startVertex, V targetVertex) {
        MSTNode targetNode = minimumSpanningTree.get(targetVertex);
        if (targetNode == null) return; //no path to the target

        if (!targetNode.vertex.equals(startVertex)) {
            buildPathFromMST(path, minimumSpanningTree, startVertex, targetNode.parentVertex);
        }

        path.vertices.add(targetNode.vertex);
        path.totalWeight = targetNode.weightSumTo;
    }
}
