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
// Một chương trình server cho phép kết nối qua giao thức TCP tại cổng 2207, sử dụng DataInputStream và DataOutputStream để trao đổi dữ liệu số thực.
// Yêu cầu
// a. Gửi một chuỗi chứa mã sinh viên và mã câu hỏi bằng phương thức writeUTF theo định dạng "studentCode;qCode". Ví dụ: "B15DCCN999;1D25ED92".
// b. Nhận từ server một số nguyên n, sau đó nhận tiếp n giá trị double.
// c. Tính giá trị trung bình, giá trị p95 tại vị trí ceil(n * 0.95) - 1 sau khi sắp xếp tăng dần, và số phần tử lớn hơn giá trị trung bình.
// d. Gửi kết quả bằng writeUTF theo định dạng average;p95;aboveAvg, trong đó các giá trị số thực được làm tròn 02 chữ số thập phân. Ví dụ: 18.45;30.12;4.
// e. Đóng kết nối hoặc kết thúc client sau khi nộp kết quả.
// Ví dụ: nếu dữ liệu nhận được là 90.0,47.66,81.95,93.56,9.72,77.04,13.72,88.62,5.34,85.55 thì dữ liệu nộp lại là 59.32;93.56;6.