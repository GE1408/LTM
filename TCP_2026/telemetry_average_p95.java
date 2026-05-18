import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.Locale;

public class telemetry_average_p95 {
    public static void main(String[] args) throws Exception {
        Socket socket = new Socket("36.50.135.242", 2207);
        DataInputStream dis = new DataInputStream(socket.getInputStream());
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        
        dos.writeUTF("B22DCVT090;zZNJkzqe");
        dos.flush();
        
        int n = dis.readInt();
        double[] arr = new double[n];
        double sum = 0;
        
        for (int i = 0; i < n; i++) {
            arr[i] = dis.readDouble();
            sum += arr[i];
        }
        
        double average = sum / n;
        Arrays.sort(arr);
        double p95 = arr[(int) Math.ceil(n * 0.95) - 1];
        
        int aboveAvgCount = 0;
        for (double num : arr) {
            if (num > average) aboveAvgCount++;
        }
        
        String result = String.format(Locale.US, "%.2f;%.2f;%d", average, p95, aboveAvgCount);
        dos.writeUTF(result);
        dos.flush();
        
        dis.close();
        dos.close();
        socket.close();
    }
}