import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class hex_checksum {
    public static void main(String[] args) throws Exception {
        Socket socket = new Socket("36.50.135.242", 2206);
        InputStream in = socket.getInputStream();
        OutputStream out = socket.getOutputStream();
        
        out.write("B22DCVT090;JcskmqZe".getBytes());
        out.flush();
        
        byte[] buffer = new byte[4096];
        int bytesRead = in.read(buffer);
        if (bytesRead <= 0) {
            socket.close();
            return;
        }
        
        String response = new String(buffer, 0, bytesRead).trim();
        int validPackets = 0, totalPayloadBytes = 0;
        
        for (String packet : response.split("\\|")) {
            if (packet.trim().isEmpty()) continue;
            
            String[] hexBytes = packet.trim().split("-");
            if (hexBytes.length < 2) continue;
            
            int sum = 0;
            for (int i = 0; i < hexBytes.length - 1; i++) {
                sum += Integer.parseInt(hexBytes[i].trim(), 16);
            }
            
            int expectedChecksum = Integer.parseInt(hexBytes[hexBytes.length - 1].trim(), 16);
            if ((sum % 256) == expectedChecksum) {
                validPackets++;
                totalPayloadBytes += (hexBytes.length - 1);
            }
        }
        
        out.write(("valid=" + validPackets + ";payloadBytes=" + totalPayloadBytes).getBytes());
        out.flush();
        
        in.close();
        out.close();
        socket.close();
    }
}
// Một chương trình server cho phép kết nối qua giao thức TCP tại cổng 2206, sử dụng luồng byte dữ liệu (InputStream/OutputStream) để trao đổi thông tin.
// Yêu cầu
// a. Gửi chuỗi chứa mã sinh viên và mã câu hỏi theo định dạng studentCode;qCode qua OutputStream. Ví dụ: B21DCCN001;A1B2C3D4.
// b. Nhận từ server nhiều gói hex, các gói phân tách bởi ký tự |, mỗi gói gồm các byte hex phân tách bởi dấu -; byte cuối là checksum. Ví dụ: 0A-1F-29|10-20-31.
// c. Một gói hợp lệ khi tổng các byte payload trước byte cuối, lấy modulo 256, bằng checksum của gói.
// d. Gửi số gói hợp lệ và tổng số byte payload của các gói hợp lệ theo định dạng valid=<n>;payloadBytes=<m>. Ví dụ: valid=1;payloadBytes=2.
// e. Đóng kết nối hoặc kết thúc client sau khi nộp kết quả.