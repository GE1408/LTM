import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class doanh_thu {
    public static void main(String[] args) throws Exception {
        Socket socket = new Socket("36.50.135.242", 2207);
        DataInputStream dis = new DataInputStream(socket.getInputStream());
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        
        dos.writeUTF("B22DCVT090;9x1YStTO");
        dos.flush();
        
        Thread.sleep(200);
        if (dis.available() <= 0) {
            dis.close();
            dos.close();
            socket.close();
            return;
        }
        
        int n = dis.readInt();
        double totalAll = 0;
        Map<String, Double> categoryMap = new TreeMap<>();
        
        for (int i = 0; i < n; i++) {
            String category = dis.readUTF();
            double amount = dis.readDouble();
            int quantity = dis.readInt();
            
            double totalRecord = amount * quantity;
            totalAll += totalRecord;
            
            categoryMap.put(category, categoryMap.getOrDefault(category, 0.0) + totalRecord);
        }
        
        String maxCategory = "";
        double maxTotal = -1;
        
        for (Map.Entry<String, Double> entry : categoryMap.entrySet()) {
            if (entry.getValue() > maxTotal) {
                maxTotal = entry.getValue();
                maxCategory = entry.getKey();
            }
        }
        
        String result = String.format(Locale.US, "category=%s;total=%.2f;records=%d", maxCategory, totalAll, n);
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
// b. Nhận từ server một số nguyên n, sau đó nhận n bản ghi; mỗi bản ghi gồm category bằng readUTF, amount bằng readDouble, và quantity bằng readInt. Ví dụ bản ghi: BOOK,120.50,3.
// c. Tính doanh thu từng bản ghi là amount * quantity, cộng tổng toàn bộ doanh thu và xác định category có tổng doanh thu lớn nhất; nếu bằng nhau chọn category theo thứ tự từ điển nhỏ hơn.
// d. Gửi kết quả bằng writeUTF theo định dạng category=<category>;total=<tổng>;records=<n>, làm tròn tổng 02 chữ số thập phân. Ví dụ: category=BOOK;total=361.50;records=1.
// e. Đóng kết nối hoặc kết thúc client sau khi nộp kết quả.