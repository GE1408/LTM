import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Map;
import java.util.TreeMap;

public class tan_suat {
    public static void main(String[] args) throws Exception {
        Socket socket = new Socket("36.50.135.242", 2208);
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
        
        writer.write("B22DCVT090;o8Bhnd0r");
        writer.newLine();
        writer.flush();
        
        String raw = reader.readLine();
        if (raw == null) {
            socket.close();
            return;
        }
        
        Map<String, Integer> map = new TreeMap<>();
        for (String w : raw.toLowerCase().replaceAll("[^a-z0-9]", " ").replaceAll("\\s+", " ").trim().split(" ")) {
            if (!w.isEmpty()) {
                map.put(w, map.getOrDefault(w, 0) + 1);
            }
        }
        
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            if (sb.length() > 0) sb.append("|");
            sb.append(entry.getKey()).append("=").append(entry.getValue());
        }
        
        writer.write(sb.toString());
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
// b. Nhận từ server một câu tiếng Anh ngẫu nhiên.
// c. Chuẩn hóa chuỗi bằng cách chuyển về chữ thường, loại bỏ ký tự không thuộc [a-z0-9 ], gom nhiều khoảng trắng thành một khoảng trắng, sau đó đếm tần suất xuất hiện của từng từ.
// d. Gửi kết quả theo thứ tự từ tăng dần theo từ điển, định dạng word=count|word=count. Ví dụ: account=2|payment=1.
// e. Đóng kết nối hoặc kết thúc client sau khi nộp kết quả.
// Ví dụ: nếu dữ liệu nhận được là Payment Account Account ticket REFUND REFUND customer Payment ticket customer ticket. thì dữ liệu nộp lại là account=2|customer=2|payment=2|refund=2|ticket=3.