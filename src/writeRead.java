
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public interface writeRead {
    default String wr(String msg,Integer destinationPort) throws IOException, IOException {
        Socket socket = new Socket("localhost", destinationPort);
        PrintWriter out = new PrintWriter(socket.getOutputStream(),true);
        out.println(msg);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String response = in.readLine();
        out.println(response);
        out.close();
        return response;
        }
    }


