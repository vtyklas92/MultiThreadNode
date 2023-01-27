import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DatabaseNode implements Runnable {
    //parametry wywołania
    private static final String PORT = "-tcpport";
    private static final String RECORD = "-record";
    private static final String CONNECT = "-connect";
    private static final String NEWCONNECT = "newconnect";
    private static final String REMOVE = "remove";
    private static final String GETVALUE = "get-value";
    private static Integer nodePort;
    private final static String OK = "OK";
    private final static String ERROR = "ERROR";
    private static final List<Database> database = new ArrayList<>();
    private static final Graph<Integer> mapOfNodes = new Graph<>();

    private static Set<Integer> visitedNodes = new HashSet<>();



    DatabaseNode(int nodePort) {
        DatabaseNode.nodePort = nodePort;
    }

    private static final String PORTLOG = "["+nodePort+"]: ";

    public synchronized static void main(String[] args) {
        try {
            parseArgs(args);
            DatabaseNode node = new DatabaseNode(nodePort);
            new Thread(node).start();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private synchronized static void parseArgs(String[] args) throws IOException {

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case PORT -> {
                    //parametry połączenia
                    nodePort = Integer.parseInt(args[i + 1]);
                    mapOfNodes.addVertex(nodePort);
//                    log(nodePort + "ADDED");


                }
                case RECORD -> {
                    String[] record = args[i + 1].split(":");
                    database.add(new Database(Integer.parseInt(record[0]), Integer.parseInt(record[1])));
                }
                case CONNECT -> {
                    String[] gateway = args[i + 1].split(":");
                    log(nodePort + " Adding vertex: " + gateway[0] + " " + gateway[1]);
                    mapOfNodes.addVertex(Integer.parseInt(gateway[1]));
                    log(gateway[1] + "ADDED");
                    log(nodePort + " Adding edge: " + nodePort + " " + gateway[1]);
                    mapOfNodes.addEdge(nodePort, Integer.parseInt(gateway[1]));
                    log("Handshake with " + gateway[0] + ":" + gateway[1]);
                    connectToOtherNode(Integer.parseInt(gateway[1]));
                }
            }
        }
    }


    public void run(){
        log("[" + nodePort + "]");
        ServerSocket server = null;

        try {

            // Serwer nasłuchuje na porcie x
            log("Opening node");

            server = new ServerSocket(nodePort);
            server.setReuseAddress(true);
            String ip = String.valueOf(Inet4Address.getByName("localhost"));

            log("Node opened at " + ip + ":" + nodePort);

            // zbieramy klientów w nieskończonej pętli
            while(true) {
                log("Waiting for connections...");
                // zwracamy new socket
                Socket client = server.accept();

                // wypisanie na konsoli infromacji o nowym kliencie
                log("New client connected from " + client.getRemoteSocketAddress());

                // tworzymenie nowego wątku dla każdego klienta
                ClientHandler clientSock = new ClientHandler(client);

                // Każdy wątek obsługi każdego klienta indiwidualnie
                new Thread(clientSock).start();

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (server != null) {
                try {
                    server.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }//Methods for connecting to other nodes
    private synchronized static void connectToOtherNode(int port) throws IOException {
        Socket socket = new Socket(InetAddress.getByName("localhost"), port);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        log("Connecting with: " + InetAddress.getByName("localhost") + " at port " + port);
        out.println(NEWCONNECT + " " + nodePort);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String response = in.readLine();
        if (response.equals(OK)) {
            System.out.println("Connected to node: " + port);
            out.close();
            in.close();
            socket.close();
        } else {
            System.out.println("Connection failed");
            out.close();
            in.close();

        }
    }
    public synchronized void connect(PrintWriter out,String port)  {
        try {
            mapOfNodes.addVertex(Integer.valueOf(port));
            mapOfNodes.addEdge(nodePort, Integer.valueOf(port));

        }catch(Exception e){
            log("Error while connecting to node: " + port);
        }
        out.println(OK);
        Thread.currentThread().interrupt();
    }


    public synchronized void terminate(PrintWriter out, String port) throws InterruptedException, IOException {
        log("Closing node...");
        if (mapOfNodes.size() > 1) {
            mapOfNodes.getVertices().forEach(neighbor -> {
                if (mapOfNodes.isAdjacent(Integer.valueOf(port), neighbor)) {
                    try {
                        log(REMOVE + " a " + neighbor);
                        Socket socket = new Socket(InetAddress.getByName("localhost"), neighbor);
                        PrintWriter out1 = new PrintWriter(socket.getOutputStream(), true);
                        out1.println(REMOVE + " " + port);
                        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        String response = in.readLine();
                        if (response.equals(OK)) {
                            System.out.println("Node " + nodePort + " delated from " + neighbor);
                            out1.close();
                            in.close();
                        } else {
                            System.out.println("Termination failed");
                            out1.close();
                            in.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        out.println(OK);
        Thread.currentThread().interrupt();
    }

    public synchronized void delate(PrintWriter out, String source, String dest) throws IOException {
        mapOfNodes.removeEdge(Integer.valueOf(source), Integer.valueOf(dest));
        mapOfNodes.removeVertex(Integer.valueOf(dest));
        out.println(OK);
        log("Node " + dest + " deleted from connections");
        Thread.currentThread().interrupt();
    }

    public synchronized void getValue(PrintWriter out, String key) throws IOException {
        log("Getting value for key: " + key);
        if(!mapOfNodes.isEmpty()) {
            for (Database record : database) {
                if (record.getKey() == Integer.valueOf(key)) {
                    out.println(record.getKey() + ":" + record.getValue());
                    log("Value for key: " + key + " is: " + record.getValue());
                    Thread.currentThread().interrupt();
                }
            }
        }else  {

            mapOfNodes.getNeighbors(nodePort).forEach(neighbor -> {
                try {
                    Socket socket = new Socket(InetAddress.getByName("localhost"), neighbor);
                    PrintWriter out1 = new PrintWriter(socket.getOutputStream(), true);
                    visitedNodes.add(nodePort);
                    out1.println(GETVALUE + " " + key + " " + visitedNodes);
                    log(visitedNodes + "");
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String response = in.readLine();
                    if (response.equals() {

                    } else {

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private static void log(String msg){
        System.out.println("[" + PORTLOG + msg);
    }

}