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
// Một chương trình server cho phép kết nối qua giao thức TCP tại cổng 2206, sử dụng luồng byte dữ liệu (InputStream/OutputStream) để trao đổi thông tin.
// Yêu cầu
// a. Gửi một chuỗi chứa mã sinh viên và mã câu hỏi theo định dạng "studentCode;qCode" qua OutputStream. Ví dụ: "B15DCCN999;1D25ED92".
// b. Nhận từ server một chuỗi gồm nhiều đoạn nhị phân, các đoạn được phân tách bởi ký tự |. Ví dụ: 1010|111|0001.
// c. Đếm tổng số bit 1 và tổng số bit 0 trong toàn bộ chuỗi dữ liệu nhận được.
// d. Gửi kết quả theo định dạng ones=<số_lượng_bit_1>;zeros=<số_lượng_bit_0>. Ví dụ: ones=6;zeros=5.
// e. Đóng kết nối hoặc kết thúc client sau khi nộp kết quả.
// Ví dụ: nếu dữ liệu nhận được là 000111000|101101|0110010|101101100|011110101|0110010 thì dữ liệu nộp lại là ones=24;zeros=23.