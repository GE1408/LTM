import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class min_max_sum {
    public static void main(String[] args) throws Exception {
        Socket socket = new Socket("36.50.135.242", 2207);
        DataInputStream dis = new DataInputStream(socket.getInputStream());
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        
        dos.writeUTF("B22DCVT090;qCvupUb8");
        dos.flush();
        
        int n = dis.readInt();
        int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE, sum = 0;
        
        for (int i = 0; i < n; i++) {
            int num = dis.readInt();
            if (num < min) min = num;
            if (num > max) max = num;
            sum += num;
        }
        
        dos.writeUTF(min + ";" + max + ";" + sum);
        dos.flush();
        
        dis.close();
        dos.close();
        socket.close();
    }
}
// Một chương trình server cho phép kết nối qua giao thức TCP tại cổng 2207, sử dụng DataInputStream và DataOutputStream để trao đổi dữ liệu.
// Yêu cầu
// a. Gửi một chuỗi chứa mã sinh viên và mã câu hỏi bằng phương thức writeUTF theo định dạng "studentCode;qCode". Ví dụ: "B15DCCN999;1D25ED92".
// b. Nhận từ server một số nguyên n, sau đó nhận tiếp n số nguyên.
// c. Tìm giá trị nhỏ nhất, giá trị lớn nhất và tổng của toàn bộ dãy số.
// d. Gửi kết quả bằng writeUTF theo định dạng min;max;sum. Ví dụ: 3;20;87.
// e. Đóng kết nối hoặc kết thúc client sau khi nộp kết quả.
// Ví dụ: nếu dữ liệu nhận được là 496,342,197,73,-52,124,130,228,238,-147,-329,411 thì dữ liệu nộp lại là -329;496;1711.