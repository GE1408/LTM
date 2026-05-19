import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class PhanLoaiBanGhi {
    public static void main(String[] args) throws Exception {
        SocketChannel ch = SocketChannel.open(new InetSocketAddress("36.50.135.242", 2211));
        
        // a. Gửi frame yêu cầu
        sendFrame(ch, "B22DCVT090;sXjTKnzL");
        
        // b. Nhận 2 frame dữ liệu và gộp lại
        StringBuilder raw = new StringBuilder();
        for (int i = 0; i < 2; i++) {
            ByteBuffer lenBuf = ByteBuffer.allocate(4);
            while (lenBuf.hasRemaining()) ch.read(lenBuf);
            lenBuf.flip();
            
            ByteBuffer payBuf = ByteBuffer.allocate(lenBuf.getInt());
            while (payBuf.hasRemaining()) ch.read(payBuf);
            payBuf.flip();
            raw.append(new String(payBuf.array(), "UTF-8"));
        }
        
        // c. Phân loại ID nhanh bằng List
        List<String> ok = new ArrayList<>(), fail = new ArrayList<>(), retry = new ArrayList<>();
        for (String r : raw.toString().split("\\|")) {
            if (r.trim().isEmpty()) continue;
            String[] t = r.split(",");
            if (t[2].trim().equalsIgnoreCase("OK")) ok.add(t[0].trim());
            else if (t[2].trim().equalsIgnoreCase("FAIL")) fail.add(t[0].trim());
            else if (t[2].trim().equalsIgnoreCase("RETRY")) retry.add(t[0].trim());
        }
        
        // d. Gửi frame kết quả
        sendFrame(ch, "OK=" + String.join(",", ok) + ";FAIL=" + String.join(",", fail) + ";RETRY=" + String.join(",", retry));
        ch.close();
    }

    // Hàm phụ gộp dùng chung để gửi gói tin dạng Frame gọn nhất
    private static void sendFrame(SocketChannel ch, String s) throws Exception {
        byte[] b = s.getBytes("UTF-8");
        ByteBuffer buf = ByteBuffer.allocate(4 + b.length);
        buf.putInt(b.length).put(b).flip();
        while (buf.hasRemaining()) ch.write(buf);
    }
}