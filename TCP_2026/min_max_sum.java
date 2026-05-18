import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class min_max_sum {
    public static void main(String[] args) throws Exception {
        Socket socket = new Socket("36.50.135.242", 2207);
        DataInputStream dis = new DataInputStream(socket.getInputStream());
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        
        dos.writeUTF("B22DCVT090;qCvupUb8");
        dos.flush();
        
        int n = dis.readInt();
        int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE, sum = 0;
        
        for (int i = 0; i < n; i++) {
            int num = dis.readInt();
            if (num < min) min = num;
            if (num > max) max = num;
            sum += num;
        }
        
        dos.writeUTF(min + ";" + max + ";" + sum);
        dos.flush();
        
        dis.close();
        dos.close();
        socket.close();
    }
}