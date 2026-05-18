import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Map;
import java.util.TreeMap;

public class tan_suat {
    public static void main(String[] args) throws Exception {
        Socket socket = new Socket("36.50.135.242", 2208);
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
        
        writer.write("B22DCVT090;o8Bhnd0r");
        writer.newLine();
        writer.flush();
        
        String raw = reader.readLine();
        if (raw == null) {
            socket.close();
            return;
        }
        
        Map<String, Integer> map = new TreeMap<>();
        for (String w : raw.toLowerCase().replaceAll("[^a-z0-9]", " ").replaceAll("\\s+", " ").trim().split(" ")) {
            if (!w.isEmpty()) {
                map.put(w, map.getOrDefault(w, 0) + 1);
            }
        }
        
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            if (sb.length() > 0) sb.append("|");
            sb.append(entry.getKey()).append("=").append(entry.getValue());
        }
        
        writer.write(sb.toString());
        writer.newLine();
        writer.flush();
        
        reader.close();
        writer.close();
        socket.close();
    }
}