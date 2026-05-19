import java.io.*;
import java.net.*;
import java.util.*;

public class DemCumTu {
    public static void main(String[] args) throws Exception {
        Socket socket = new Socket("36.50.135.242", 2208);
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
        
        writer.write("B22DCVT090;dQQHDZDD");
        writer.newLine();
        writer.flush();
        
        String raw = reader.readLine();
        if (raw == null) {
            socket.close();
            return;
        }
        
        String clean = raw.toLowerCase().replaceAll("[^a-z0-9]", " ").replaceAll("\\s+", " ").trim();
        if (clean.isEmpty()) {
            socket.close();
            return;
        }
        
        String[] tokens = clean.split(" ");
        List<String> wordsList = new ArrayList<>();
        for (String t : tokens) {
            if (!t.trim().isEmpty()) {
                wordsList.add(t.trim());
            }
        }
        
        Map<String, Integer> map = new TreeMap<>();
        for (int i = 0; i < wordsList.size() - 1; i++) {
            String bigram = wordsList.get(i) + "_" + wordsList.get(i + 1);
            map.put(bigram, map.getOrDefault(bigram, 0) + 1);
        }
        
        List<Map.Entry<String, Integer>> list = new ArrayList<>(map.entrySet());
        list.sort((e1, e2) -> {
            int cmp = e2.getValue().compareTo(e1.getValue());
            if (cmp != 0) return cmp;
            return e1.getKey().compareTo(e2.getKey());
        });
        
        StringBuilder sb = new StringBuilder();
        int limit = Math.min(3, list.size());
        for (int i = 0; i < limit; i++) {
            if (sb.length() > 0) sb.append("|");
            sb.append(list.get(i).getKey()).append("=").append(list.get(i).getValue());
        }
        
        writer.write(sb.toString().trim()); // Sử dụng thêm .trim() để triệt tiêu ký tự trắng thừa phát sinh
        writer.newLine();
        writer.flush();
        
        reader.close();
        writer.close();
        socket.close();
    }
}
// Một chương trình server cho phép kết nối qua giao thức TCP tại cổng 2208, sử dụng BufferedReader và BufferedWriter để trao đổi chuỗi ký tự.
// Yêu cầu
// a. Gửi một dòng chứa mã sinh viên và mã câu hỏi theo định dạng studentCode;qCode bằng BufferedWriter, sau đó kết thúc dòng. Ví dụ: B21DCCN001;BAA62945.
// b. Nhận từ server một câu tiếng Anh. Ví dụ: payment ticket payment ticket refund..
// c. Chuẩn hóa câu về chữ thường, loại bỏ ký tự không thuộc [a-z0-9 ], sau đó đếm tần suất các cụm hai từ liên tiếp.
// d. Gửi tối đa 03 cụm có tần suất cao nhất theo định dạng word_word=count|word_word=count; nếu bằng tần suất thì sắp xếp tăng dần theo từ điển. Ví dụ: payment_ticket=2|ticket_payment=1|ticket_refund=1.
// e. Đóng kết nối hoặc kết thúc client sau khi nộp kết quả.