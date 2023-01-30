
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public interface writeRead {
    default void write(String msg,Integer destinationPort) throws IOException, IOException {
        Socket socket = new Socket("localhost", destinationPort);
        PrintWriter out = new PrintWriter(socket.getOutputStream(),true);
        out.println(msg);
    }
    default String read(Integer destinationPort) throws IOException {
        Socket socket = new Socket("localhost", destinationPort);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        return in.readLine();
    }
}
