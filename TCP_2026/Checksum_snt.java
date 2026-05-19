import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Checksum_snt {
    public static void main(String[] args) throws Exception {
        String serverHost = "36.50.135.242";
        int serverPort = 2206;
        String studentCode = "B22DCVT090";
        String qCode = "z88zpEfh";
        
        Socket socket = new Socket(serverHost, serverPort);
        
        InputStream in = socket.getInputStream();
        OutputStream out = socket.getOutputStream();
        
        String requestStr = studentCode + ";" + qCode;
        out.write(requestStr.getBytes());
        out.flush();
        
        byte[] buffer = new byte[2048];
        int bytesRead = in.read(buffer);
        
        if (bytesRead <= 0) {
            in.close();
            out.close();
            socket.close();
            return;
        }
        
        String responseStr = new String(buffer, 0, bytesRead).trim();
        String[] numberStrings = responseStr.split(",");
        
        int primeSum = 0;
        int xorAll = 0;
        
        for (int i = 0; i < numberStrings.length; i++) {
            if (!numberStrings[i].isEmpty()) {
                int value = Integer.parseInt(numberStrings[i].trim());
                int position = i + 1;
                
                if (isPrime(position)) {
                    primeSum = (primeSum + value) % 65536;
                }
                
                if (i == 0) {
                    xorAll = value;
                } else {
                    xorAll = xorAll ^ value;
                }
            }
        }
        
        String resultStr = "primeSum=" + primeSum + ";xor=" + xorAll;
        out.write(resultStr.getBytes());
        out.flush();
        
        in.close();
        out.close();
        socket.close();
    }
    
    private static boolean isPrime(int n) {
        if (n < 2) return false;
        for (int i = 2; i <= Math.sqrt(n); i++) {
            if (n % i == 0) {
                return false;
            }
        }
        return true;
    }
}
// Một chương trình server cho phép kết nối qua giao thức TCP tại cổng 2206, sử dụng luồng byte dữ liệu (InputStream/OutputStream) để trao đổi thông tin.
// Yêu cầu
// a. Gửi chuỗi chứa mã sinh viên và mã câu hỏi theo định dạng studentCode;qCode qua OutputStream. Ví dụ: B21DCCN001;A1B2C3D4.
// b. Nhận từ server một chuỗi số byte không dấu phân tách bằng dấu phẩy. Ví dụ: 12,45,255,8,19,20.
// c. Tính primeSum là tổng các giá trị ở vị trí nguyên tố theo chỉ số bắt đầu từ 1, lấy modulo 65536; đồng thời tính xor của toàn bộ giá trị.
// d. Gửi kết quả theo định dạng primeSum=<n>;xor=<m>. Ví dụ: primeSum=319;xor=217.
// e. Đóng kết nối hoặc kết thúc client sau khi nộp kết quả.