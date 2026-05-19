import java.io.*;
import java.net.*;
import java.util.*;

public class LocGiaoDich {
    public static void main(String[] args) throws Exception {
        Socket socket = new Socket("36.50.135.242", 2208);
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
        
        writer.write("B22DCVT090;g06G2rFl");
        writer.newLine();
        writer.flush();
        
        String logData = reader.readLine();
        if (logData == null) {
            socket.close();
            return;
        }
        
        List<String> validIds = new ArrayList<>();
        double totalAmount = 0;
        
        for (String line : logData.split("\\|\\|")) {
            if (line.trim().isEmpty()) continue;
            
            String[] tokens = line.trim().split("\\s+");
            if (tokens.length < 4) continue;
            
            String user = tokens[0].trim();
            String riskStr = "", amountStr = "";
            
            for (String t : tokens) {
                String tokenClean = t.trim();
                if (tokenClean.startsWith("risk=")) {
                    riskStr = tokenClean.substring(5);
                } else if (tokenClean.startsWith("amount=")) {
                    amountStr = tokenClean.substring(7);
                }
            }
            
            if (!riskStr.isEmpty() && !amountStr.isEmpty()) {
                int risk = Integer.parseInt(riskStr);
                double amount = Double.parseDouble(amountStr);
                
                if (risk >= 70 || amount >= 5000) {
                    validIds.add(user);
                    totalAmount += amount;
                }
            }
        }
        
        String idsJoined = String.join(",", validIds);
        String result = String.format(Locale.US, "count=%d;ids=%s;amount=%.2f", validIds.size(), idsJoined, totalAmount);
        
        writer.write(result);
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
// b. Nhận từ server một chuỗi log giao dịch, các dòng nối bằng ||. Mỗi dòng có dạng user action=... risk=... amount=.... Ví dụ: U300 action=PAY risk=85 amount=7200.50.
// c. Một dòng rủi ro cao nếu risk >= 70 hoặc amount >= 5000. Giữ thứ tự các user rủi ro cao theo dữ liệu ban đầu và tính tổng amount của các dòng này.
// d. Gửi kết quả theo định dạng count=<n>;ids=<id1,id2>;amount=<tổng>, tổng làm tròn 02 chữ số thập phân. Ví dụ: count=2;ids=U300,U302;amount=10500.75.
// e. Đóng kết nối hoặc kết thúc client sau khi nộp kết quả.