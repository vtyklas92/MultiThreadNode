import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class DatabaseClient implements Runnable {
    static String gateway = null;
    static int port = 0;
    static String identifier = null;
    static String command = null;
    private static final String PORTLOG = "["+identifier+"]: ";
    public synchronized static void main(String[] args) {
        parseArgs(args);
        new Thread(new DatabaseClient()).start();
    }
    public static void parseArgs(String[] args) {

        // Parameter scan loop
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-gateway":
                    String[] gatewayArray = args[++i].split(":");
                    gateway = gatewayArray[0];
                    port = Integer.parseInt(gatewayArray[1]);
                    break;
                case "-operation":
                    break;

                default:
                    if (command == null) command = args[i];
                    else if (!"TERMINATE".equals(command)) command += " " + args[i];
            }
        }
    }

    public synchronized void run() {
        // communication socket and streams
        Socket netSocket;
        PrintWriter out;
        BufferedReader in;

        try {
            log("Connecting with: " + gateway + " at port " + port);
            netSocket = new Socket(gateway, port);
            out = new PrintWriter(netSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(netSocket.getInputStream()));
            identifier = String.valueOf(netSocket.getLocalPort());
            log("Connected");

            log("Sending: " + command);
            out.println(command);
            // Read and print out the response
            String response;
            while ((response = in.readLine()) != null) {
                log(response + "!!!!!!!!!!!!!!!!!!!!!");
//                if(response.equals("OK") && command.equals("terminate")) break;

            }

            // Terminate - close all the streams and the socket
            out.close();
            in.close();
            netSocket.close();
        } catch (UnknownHostException e) {
            log("Unknown host: " + gateway + ".");
            System.exit(1);
        } catch (IOException e) {
            log("No connection with " + gateway + ".");
            System.exit(1);
        }
    }
    private synchronized static void log (String msg){
        System.out.println("[" + Thread.currentThread().getName() + "]: " + msg );



    }
}