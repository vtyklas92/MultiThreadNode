import java.io.*;
import java.net.Socket;
//TODO: Przesyłanie nazwy portu do metody log
class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private static final String TERMINATE = "terminate";
    private static final String OK = "OK";
    private static final String NEWCONNECT = "newconnect";
    private static final String REMOVE = "remove";

    private static final String GET_VALUE = "get-value";

    private static boolean isTerminated = false;


    //Odpwiedzi dla klienta

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
       log("["+clientSocket.getLocalPort()+"]: ");
    }

    public synchronized void run() {
        PrintWriter out = null;
        BufferedReader in = null;

        try{

            // zbieramy stream dla klienta
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            // zbieramy stream od klienta
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String line;
            log("Reading answer:");
            while ((line = in.readLine()) != null) {
                // Wypisujemy stream otrzymany od klienta
                log( clientSocket.getLocalPort() + " --> " + "Message sent from the client: " + line);
//                out.println(line);
                String[] commandArray = line.split(" ");
                log("Command: " + commandArray[0]);
                log("Doing task: ");

                switch (commandArray[0]) {
                    case TERMINATE -> {
                        log("Wykonuję " + commandArray[0]);
                        new DatabaseNode(clientSocket.getPort()).terminate(out,String.valueOf(clientSocket.getLocalPort()));
                        log("Node closed");
                        Thread.currentThread().join();
                        isTerminated = true;
                    }
                    case NEWCONNECT -> {
                        log("Wykonuję " + commandArray[0]);
                        new DatabaseNode(clientSocket.getLocalPort()).connect(out, commandArray[1]);
                        log("Node connected");
                        Thread.currentThread().join();
                    }
                    case REMOVE -> {
                        log("Wykonuję " + commandArray[0]);
                        new DatabaseNode(Integer.parseInt(commandArray[1])).delate(out, String.valueOf(clientSocket.getLocalPort()),commandArray[1]);
                        log("Node removed");
                        Thread.currentThread().join();
                    }
                    case GET_VALUE -> {
                        log("Wykonuję " + commandArray[0]);
                        new DatabaseNode(clientSocket.getLocalPort()).getValue(out, commandArray[1]);
                        log("Value returned");
                        Thread.currentThread().join();
                    }
                }
            }
        } catch (IOException | InterruptedException e) {
            log("Client Handler closed");
            if(isTerminated){
                System.exit(0);
            }

        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
    private synchronized static void log (String msg){
    //TODO: Przesyłanie nazwy portu do metody log;
        //Print current port number
        System.out.println("[ClientHandler] " + msg);

    }

}