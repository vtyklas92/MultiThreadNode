import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
//TODO: Opracować connect dla nowych nodów
public class DatabaseNode implements Runnable {
    //parametry wywołania
    private static final String PORT = "-tcpport";
    private static final String RECORD = "-record";
    private static final String CONNECT = "-connect";
    private static final String NEWCONNECT = "newconnect";
    private static int nodePort;
    private final static String OK = "OK";
    private static final List<Database> database = new ArrayList<>();
    private static Graph mapOfNodes = new Graph();

    DatabaseNode(int nodePort) {
        this.nodePort = nodePort;
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

                }
                case RECORD -> {
                    String[] record = args[i + 1].split(":");
                    database.add(new Database(Integer.parseInt(record[0]), Integer.parseInt(record[1])));
                }
                case CONNECT -> {
                    String[] gateway = args[i + 1].split(":");
                    mapOfNodes.addVertex(Integer.parseInt(gateway[1]));
                    mapOfNodes.addEdge(nodePort, Integer.parseInt(gateway[1]));
                    log("Handshake with " + gateway[0] + ":" + gateway[1]);
                    connectToOtherNode(Integer.parseInt(gateway[1]));
                    }
                }
            }
        }

        private synchronized static void connectToOtherNode(int port) throws IOException {
            log("Connecting with: " + "localhost" + " at port " + port);
            Socket socket = new Socket("localhost", port);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(NEWCONNECT + " " + nodePort);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String response = in.readLine();
            log("Response: " + response);
            if (response.equals(OK)) {
                System.out.println("Connected to node: " + port);
            } else {
                System.out.println("Connection failed");
                out.close();
                in.close();
                socket.close();
            }
        }
        public synchronized void connect(PrintWriter out,String port)  {
        try {
            log("CONNECT " + port);
            mapOfNodes.addVertex(Integer.valueOf(port));
            mapOfNodes.addEdge(nodePort, port);
        }catch(Exception e){
            log("Sending ERROR");
            out.write("ERROR");
            out.flush();
        }
            log("Sending OK");
            out.write(OK);
            out.flush();
        }


    public void run(){
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
                System.out.println("New client connected from " + client.getRemoteSocketAddress());

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

    public synchronized void terminate(PrintWriter out) throws InterruptedException {
        log("Closing node");
        log("Wysłam odpowiedź do klienta");
        out.write(OK);
        out.flush();
        Thread.sleep(1000);
        System.exit(0);

    }


    private static void log(String msg){
        System.out.println("[" + Thread.currentThread().getName() + "]: " +msg);
    }

    public static int getPort(){
        return nodePort;
    }

}