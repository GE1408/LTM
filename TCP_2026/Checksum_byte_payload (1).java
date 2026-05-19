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
// Một chương trình server cho phép kết nối qua giao thức TCP tại cổng 2206, sử dụng luồng byte dữ liệu (InputStream/OutputStream) để trao đổi thông tin.
// Yêu cầu
// a. Gửi một chuỗi chứa mã sinh viên và mã câu hỏi theo định dạng "studentCode;qCode" qua OutputStream. Ví dụ: "B15DCCN999;1D25ED92".
// b. Nhận từ server một chuỗi các giá trị byte không dấu, mỗi giá trị nằm trong khoảng 0..255 và được phân tách bởi dấu phẩy. Ví dụ: 12,45,255,8.
// c. Tính tổng tất cả các giá trị byte nhận được theo modulo 256.
// d. Gửi kết quả checksum lên server dưới dạng một chuỗi số nguyên. Ví dụ: 64.
// e. Đóng kết nối hoặc kết thúc client sau khi nộp kết quả.
// Ví dụ: nếu dữ liệu nhận được là 75,68,12,158,115,63,175,113,189,91,158,56,29,215,122 thì dữ liệu nộp lại là 103.