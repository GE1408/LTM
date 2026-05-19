/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tcp;

import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class HoaDonNIO {
    public static void main(String[] args) throws Exception {
        SocketChannel ch = SocketChannel.open(new InetSocketAddress("36.50.135.242", 2211));
        
        // a. Gửi frame yêu cầu
        sendFrame(ch, "B22DCVT090;Y9z7Uh1K");
        
        // b. Nhận 3 frame liên tiếp và gộp lại thành danh sách bản ghi
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
        
        // c. Duyệt qua các bản ghi phân tách bởi dấu '|' để tính toán dữ liệu
        double totalAll = 0;
        int largeCount = 0;
        
        for (String record : sb.toString().split("\\|")) {
            if (record.trim().isEmpty()) continue;
            
            // Định dạng bản ghi: invoiceId:quantity:unitPrice
            String[] tokens = record.split(":");
            if (tokens.length >= 3) {
                int quantity = Integer.parseInt(tokens[1].trim());
                double unitPrice = Double.parseDouble(tokens[2].trim());
                
                double cost = quantity * unitPrice;
                totalAll += cost;
                
                if (cost >= 500) largeCount++;
            }
        }
        
        // d. Gửi một frame kết quả theo định dạng "total=<tổng>;large=<n>"
        String result = String.format(Locale.US, "total=%.2f;large=%d", totalAll, largeCount);
        sendFrame(ch, result);
        
        ch.close();
    }

    // Hàm phụ dùng chung để đóng gói và truyền tải dữ liệu dạng Frame qua SocketChannel
    private static void sendFrame(SocketChannel ch, String s) throws Exception {
        byte[] b = s.getBytes("UTF-8");
        ByteBuffer buf = ByteBuffer.allocate(4 + b.length);
        buf.putInt(b.length).put(b).flip();
        while (buf.hasRemaining()) ch.write(buf);
    }
}
// Một chương trình server cho phép kết nối qua giao thức TCP tại cổng 2211, sử dụng SocketChannel và ByteBuffer để trao đổi dữ liệu theo frame.
// Yêu cầu
// a. Gửi frame đầu tiên gồm 04 byte độ dài big-endian và payload UTF-8 là chuỗi studentCode;qCode. Ví dụ payload: B21DCCN001;9F8E7D6C.
// b. Nhận đúng 03 frame liên tiếp, nối payload theo thứ tự để được danh sách bản ghi invoiceId:quantity:unitPrice phân tách bằng |. Ví dụ: I400:2:120.50|I401:5:90.00.
// c. Tính tổng tiền tất cả bản ghi và đếm số dòng có quantity * unitPrice >= 500.
// d. Gửi một frame kết quả theo định dạng total=<tổng>;large=<n>, tổng làm tròn 02 chữ số thập phân. Ví dụ: total=691.00;large=1.
// e. Đóng kết nối hoặc kết thúc client sau khi nộp kết quả.