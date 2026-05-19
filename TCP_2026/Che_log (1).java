import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class Che_log {
    public static void main(String[] args) throws Exception {
        Socket socket = new Socket("36.50.135.242", 2208);
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
        
        writer.write("B22DCVT090;XvIPVdoh");
        writer.newLine();
        writer.flush();
        
        String logData = reader.readLine();
        if (logData == null) {
            socket.close();
            return;
        }
        
        String finalResult = logData
                .replaceAll("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}", "[EMAIL]")
                .replaceAll("\\b0\\d{9}\\b", "[PHONE]")
                .replaceAll("token=[a-zA-Z0-9]+", "token=[TOKEN]");
        
        writer.write(finalResult);
        writer.newLine();
        writer.flush();
        
        reader.close();
        writer.close();
        socket.close();
    }
}
// Một chương trình server cho phép kết nối qua giao thức TCP tại cổng 2208, sử dụng BufferedReader và BufferedWriter để trao đổi chuỗi ký tự.
// Yêu cầu
// a. Gửi một dòng chứa mã sinh viên và mã câu hỏi theo định dạng "studentCode;qCode" bằng BufferedWriter, sau đó kết thúc dòng. Ví dụ: "B15DCCN999;BAA62945".
// b. Nhận từ server một chuỗi log gồm nhiều dòng được nối bằng ký tự ||. Mỗi dòng có thể chứa email, số điện thoại Việt Nam và token.
// c. Thay mọi email bằng [EMAIL], mọi số điện thoại 10 chữ số bắt đầu bằng 0 bằng [PHONE], và mọi token dạng token=<giá_trị> bằng token=[TOKEN].
// d. Gửi chuỗi log đã được che dữ liệu nhạy cảm, giữ nguyên thứ tự các dòng và ký tự phân tách ||.
// e. Đóng kết nối hoặc kết thúc client sau khi nộp kết quả.
// Ví dụ: nếu dữ liệu nhận được là INFO user=cuong email=cuong0@example.com phone=0684775637 token=ftrTU9XmBX action=shipping thì dữ liệu nộp lại là INFO user=cuong email=[EMAIL] phone=[PHONE] token=[TOKEN] action=shipping.