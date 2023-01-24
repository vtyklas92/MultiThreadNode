import javax.xml.crypto.Data;
import java.io.*;
import java.net.Socket;
//TODO: Przesyłanie nazwy portu do metody log
class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private static final String TERMINATE = "terminate";
    private static final String OK = "OK";
    private static final String NEWCONNECT = "newconnect";


    //Odpwiedzi dla klienta

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
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
                System.out.printf( clientSocket.getLocalPort() + " --> " + "Message sent from the client: " + line + "\n");
//                out.println(line);
                String[] commandArray = line.split(" ");
                log("Command: " + commandArray[0]);
                log("Doing task: ");

                switch(commandArray[0]) {

                    case TERMINATE:
                        log("Wykonuję " + commandArray[0]);
                        new DatabaseNode(clientSocket.getPort()).terminate(out);
                        log("Node closed");
                        break;

                    case NEWCONNECT:
                        log("Wykonuję " + commandArray[0]);
                        new DatabaseNode(Integer.parseInt(commandArray[1])).connect(out,commandArray[1]);
                        log("Node connected");
                        break;
                }


            }
        } catch (IOException e) {
            e.printStackTrace();

        } catch (InterruptedException e) {
            e.printStackTrace();
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
        System.out.println("[" + Thread.currentThread().getName() + "]: " + msg);

    }

}