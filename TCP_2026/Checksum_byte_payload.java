import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Checksum_byte_payload {
    public static void main(String[] args) throws Exception {
        Socket socket = new Socket("36.50.135.242", 2206);
        InputStream in = socket.getInputStream();
        OutputStream out = socket.getOutputStream();
        
        out.write("B22DCVT090;DHaCowsf".getBytes());
        out.flush();
        
        byte[] buffer = new byte[1024];
        int bytesRead = in.read(buffer);
        if (bytesRead <= 0) {
            socket.close();
            return;
        }
        
        int checksum = 0;
        for (String num : new String(buffer, 0, bytesRead).trim().split(",")) {
            if (!num.isEmpty()) {
                checksum = (checksum + Integer.parseInt(num.trim())) % 256;
            }
        }
        
        out.write(String.valueOf(checksum).getBytes());
        out.flush();
        
        in.close();
        out.close();
        socket.close();
    }
}