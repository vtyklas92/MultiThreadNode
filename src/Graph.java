import java.util.*;

public class Graph<T> {
    final private HashMap<T, Set<T>> adjacencyList;

    public Graph() {
        this.adjacencyList = new HashMap<>();
    }

    public void addVertex(T vertex) {
        adjacencyList.putIfAbsent(vertex, new HashSet<>());
    }

    public void removeVertex(T vertex) {
        adjacencyList.values().forEach(e -> e.remove(vertex));
        adjacencyList.remove(vertex);
    }

    public void addEdge(T source, T destination) {
        if (!adjacencyList.containsKey(source))
            addVertex(source);
        if (!adjacencyList.containsKey(destination))
            addVertex(destination);
        adjacencyList.get(source).add(destination);
    }

    public void removeEdge(T source, T destination) {
        Set<T> dlist = adjacencyList.get(source);
        if (dlist != null)
            dlist.remove(destination);
    }

    public boolean isAdjacent(T source, T destination) {
        Set<T> dlist = adjacencyList.get(source);
        if (dlist == null)
            return false;
        return dlist.contains(destination);
    }

    public Iterable<T> getNeighbors(T vertex) {
        Set<T> neighbors = adjacencyList.get(vertex);
        if (neighbors == null)
            return new HashSet<>();
        return neighbors;
    }

    public Iterable<T> getVertices() {
        return adjacencyList.keySet();
    }

    public boolean isEmpty() {
        return adjacencyList.isEmpty();
    }

    public int size() {
        return adjacencyList.size();
    }

   //method for bfs traversal
    public void bfs(T source) {
        Set<T> visited = new HashSet<>();
        Queue<T> queue = new LinkedList<>();
        queue.add(source);
        visited.add(source);
        while (!queue.isEmpty()) {
            T vertex = queue.poll();
            System.out.print(vertex + " ");
            for (T v : getNeighbors(vertex)) {
                if (!visited.contains(v)) {
                    visited.add(v);
                    queue.add(v);
                }
            }
        }
    }

}
