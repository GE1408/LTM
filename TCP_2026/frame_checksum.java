import java.net.*;
import java.nio.*;
import java.nio.channels.*;

public class frame_checksum {
    public static void main(String[] args) throws Exception {
        SocketChannel ch = SocketChannel.open(new InetSocketAddress("36.50.135.242", 2211));
        
        // a. Gửi frame yêu cầu
        sendFrame(ch, "B22DCVT090;1Nicao3s");
        
        // b. Nhận 3 frame liên tiếp và gộp lại
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            ByteBuffer lenBuf = ByteBuffer.allocate(4);
            while (lenBuf.hasRemaining()) ch.read(lenBuf);
            lenBuf.flip();
            
            ByteBuffer payBuf = ByteBuffer.allocate(lenBuf.getInt());
            while (payBuf.hasRemaining()) ch.read(payBuf);
            payBuf.flip();
            sb.append(new String(payBuf.array(), "UTF-8"));
        }
        
        // c. Tính toán chuỗi kết quả
        String s = sb.toString();
        long charSum = 0;
        for (char c : s.toCharArray()) charSum += c;
        
        // d. Gửi frame kết quả
        sendFrame(ch, "len=" + s.length() + ";checksum=" + (charSum % 100000));
        ch.close();
    }

    // Hàm phụ gộp dùng chung để gửi dữ liệu dạng Frame gọn nhất
    private static void sendFrame(SocketChannel ch, String s) throws Exception {
        byte[] b = s.getBytes("UTF-8");
        ByteBuffer buf = ByteBuffer.allocate(4 + b.length);
        buf.putInt(b.length).put(b).flip();
        while (buf.hasRemaining()) ch.write(buf);
    }
}
// Một chương trình server cho phép kết nối qua giao thức TCP tại cổng 2211, sử dụng SocketChannel và ByteBuffer để trao đổi dữ liệu theo giao thức frame.
// Yêu cầu
// a. Gửi frame đầu tiên gồm 4 byte độ dài big-endian và payload UTF-8 là chuỗi theo định dạng "studentCode;qCode". Ví dụ payload: "B15DCCN999;9F8E7D6C".
// b. Sau khi gửi frame chứa studentCode;qCode, nhận đúng 03 frame liên tiếp. Mỗi frame gồm 4 byte độ dài big-endian và payload UTF-8.
// c. Nối payload của 03 frame theo đúng thứ tự, tính độ dài chuỗi và tổng mã ký tự theo modulo 100000.
// d. Gửi một frame kết quả theo định dạng len=<độ_dài>;checksum=<checksum>.
// e. Đóng kết nối hoặc kết thúc client sau khi nộp kết quả.
// Ví dụ: nếu dữ liệu nhận được là frame1=abc;frame2=def;frame3=ghi thì dữ liệu nộp lại là len=9;checksum=909.