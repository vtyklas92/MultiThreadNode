import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class DatabaseNode implements Runnable {
    //parametry wywołania
    private static final String PORT = "-tcpport";
    private static final String RECORD = "-record";
    private static final String CONNECT = "-connect";
    private static final String NEWCONNECT = "newconnect";
    private static final String REMOVE = "remove";
    private static Integer nodePort;
    private final static String OK = "OK";
    private final static String ERROR = "ERROR";
    private static final List<Database> database = new ArrayList<>();
    private static final Graph<Integer> mapOfNodes = new Graph<>();

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
            log("CONNECTED " + port);
            mapOfNodes.addVertex(Integer.valueOf(port));
            mapOfNodes.addEdge(nodePort, Integer.valueOf(port));

            mapOfNodes.getNeighbors(nodePort).forEach(neighbor -> {
                log("Connected to " + neighbor);});

        }catch(Exception e){
            out.println();
        }

        out.println(OK);
        Thread.currentThread().interrupt();
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

            mapOfNodes.getNeighbors(nodePort).forEach(neighbor -> {
                log("Connected to " + neighbor);
            });

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
    }

    public synchronized void terminate(PrintWriter out, String port) throws InterruptedException, IOException {
        //send OK to DatabaseClient
        log("Closing node...");
        log("Check for neighbours");

        if(mapOfNodes.size() > 1){
            mapOfNodes.getVertices().forEach(neighbor -> {
                log("Neighbour: " + neighbor);
                log("nodePort: " + port );
              if(mapOfNodes.isAdjacent(Integer.valueOf(port),neighbor)) {
                  try {
                      log("Sending REMOVE to " + neighbor);
                      Socket socket = new Socket(InetAddress.getByName("localhost"), neighbor);
                      log("Made new socket with " + neighbor);
                      PrintWriter out1 = new PrintWriter(socket.getOutputStream(), true);
                      out1.println(REMOVE + " " + port);
                      BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                      String response = in.readLine();
                      log("Response: " + response);
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
        log("Wysłam odpowiedź do klienta");
        out.println(OK);
        Thread.currentThread().interrupt();


    }

    public synchronized void delate(PrintWriter out, String source, String dest) throws IOException {
        log("DELATE " + dest);
        mapOfNodes.removeEdge(Integer.valueOf(source), Integer.valueOf(dest));
        mapOfNodes.removeVertex(Integer.valueOf(dest));
        out.println(OK);
        log("Node " + dest + " deleted from connections");
        log("Pozostałe połączenia");
        mapOfNodes.getNeighbors(Integer.valueOf(source)).forEach(n ->{
            log(n + "");
        });
        Thread.currentThread().interrupt();
    }

    private static void log(String msg){
        System.out.println("[" + PORTLOG + msg);
    }

    public static int getPort(){
        return nodePort;
    }

}