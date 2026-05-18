import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class Che_log {
    public static void main(String[] args) throws Exception {
        Socket socket = new Socket("36.50.135.242", 2208);
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
        
        writer.write("B22DCVT090;XvIPVdoh");
        writer.newLine();
        writer.flush();
        
        String logData = reader.readLine();
        if (logData == null) {
            socket.close();
            return;
        }
        
        String finalResult = logData
                .replaceAll("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}", "[EMAIL]")
                .replaceAll("\\b0\\d{9}\\b", "[PHONE]")
                .replaceAll("token=[a-zA-Z0-9]+", "token=[TOKEN]");
        
        writer.write(finalResult);
        writer.newLine();
        writer.flush();
        
        reader.close();
        writer.close();
        socket.close();
    }
}