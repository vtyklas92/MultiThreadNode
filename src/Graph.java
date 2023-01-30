import java.io.IOException;
import java.util.*;

public class Graph<T> implements writeRead{
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

    public void dfs(int start, String msg) throws IOException {
        boolean[] visited = new boolean[adjacencyList.size()];
        dfsRecursive(start, visited, msg);
    }
    private void dfsRecursive(int current, boolean[] visited, String msg) throws IOException {
      visited[current] = true;
      write(msg,current);
      for(T dest : adjacencyList.get(current)){
          if(!visited[current]){
              dfsRecursive(current, visited, msg);
          }

        }
    }

}
