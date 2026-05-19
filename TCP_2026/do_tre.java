package tcp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Locale;

public class do_tre {
    public static void main(String[] args) throws Exception {
        Socket socket = new Socket("36.50.135.242", 2207);
        DataInputStream dis = new DataInputStream(socket.getInputStream());
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        
        dos.writeUTF("B22DCVT090;7VUez5c0");
        dos.flush();
        
        int n = dis.readInt();
        double[] arr = new double[n];
        double sumAll = 0;
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        
        for (int i = 0; i < n; i++) {
            arr[i] = dis.readDouble();
            sumAll += arr[i];
            if (arr[i] < min) min = arr[i];
            if (arr[i] > max) max = arr[i];
        }
        
        double trimmedAvg = (sumAll - min - max) / (n - 2);
        
        double varianceSum = 0;
        for (double num : arr) {
            varianceSum += Math.pow(num - trimmedAvg, 2);
        }
        double stddev = Math.sqrt(varianceSum / n);
        
        int outliers = 0;
        double threshold = trimmedAvg + stddev;
        for (double num : arr) {
            if (num > threshold) outliers++;
        }
        
        String result = String.format(Locale.US, "trimmedAvg=%.2f;stddev=%.2f;outliers=%d", trimmedAvg, stddev, outliers);
        dos.writeUTF(result);
        dos.flush();
        
        dis.close();
        dos.close();
        socket.close();
    }
}
// Một chương trình server cho phép kết nối qua giao thức TCP tại cổng 2207, sử dụng DataInputStream và DataOutputStream để trao đổi dữ liệu.
// Yêu cầu
// a. Gửi chuỗi chứa mã sinh viên và mã câu hỏi bằng phương thức writeUTF theo định dạng studentCode;qCode. Ví dụ: B21DCCN001;A1B2C3D4.
// b. Nhận từ server một số nguyên n, sau đó nhận n giá trị double biểu diễn độ trễ. Ví dụ: 12.50,30.00,45.25,80.00.
// c. Tính trung bình sau khi loại một giá trị nhỏ nhất và một giá trị lớn nhất, tính độ lệch chuẩn trên toàn bộ dãy, và đếm số giá trị lớn hơn average + stddev.
// d. Gửi kết quả bằng writeUTF theo định dạng trimmedAvg=<avg>;stddev=<stddev>;outliers=<n>, các số thực làm tròn 02 chữ số thập phân. Ví dụ: trimmedAvg=37.63;stddev=24.65;outliers=1.
// e. Đóng kết nối hoặc kết thúc client sau khi nộp kết quả.