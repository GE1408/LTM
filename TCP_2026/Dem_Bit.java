package tcp;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Dem_Bit {
    public static void main(String[] args) throws Exception {
        Socket socket = new Socket("36.50.135.242", 2206);
        InputStream in = socket.getInputStream();
        OutputStream out = socket.getOutputStream();
        
        out.write("B22DCVT090;PuURwEQf".getBytes());
        out.flush();
        
        byte[] buffer = new byte[2048];
        int bytesRead = in.read(buffer);
        if (bytesRead <= 0) {
            socket.close();
            return;
        }
        
        String responseStr = new String(buffer, 0, bytesRead).trim();
        int countOnes = 0, countZeros = 0;
        
        for (int i = 0; i < responseStr.length(); i++) {
            char ch = responseStr.charAt(i);
            if (ch == '1') countOnes++;
            else if (ch == '0') countZeros++;
        }
        
        out.write(("ones=" + countOnes + ";zeros=" + countZeros).getBytes());
        out.flush();
        
        in.close();
        out.close();
        socket.close();
    }
}