/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tcp;

import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class PhanLoaiDiemNIO {
    public static void main(String[] args) throws Exception {
        SocketChannel ch = SocketChannel.open(new InetSocketAddress("36.50.135.242", 2211));
        
        // a. Gửi frame yêu cầu
        sendFrame(ch, "B22DCVT090;NPe4XXFh");
        
        // b. Nhận 2 frame dữ liệu liên tiếp và gộp lại
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 2; i++) {
            ByteBuffer lenBuf = ByteBuffer.allocate(4);
            while (lenBuf.hasRemaining()) ch.read(lenBuf);
            lenBuf.flip();
            
            ByteBuffer payBuf = ByteBuffer.allocate(lenBuf.getInt());
            while (payBuf.hasRemaining()) ch.read(payBuf);
            payBuf.flip();
            sb.append(new String(payBuf.array(), "UTF-8"));
        }
        
        // c. Phân loại id vào PASS, REVIEW, FAIL (Giữ nguyên thứ tự xuất hiện)
        List<String> passList = new ArrayList<>();
        List<String> reviewList = new ArrayList<>();
        List<String> failList = new ArrayList<>();
        
        for (String record : sb.toString().split("\\|")) {
            if (record.trim().isEmpty()) continue;
            
            // Định dạng bản ghi: id,type,score
            String[] tokens = record.split(",");
            if (tokens.length >= 3) {
                String id = tokens[0].trim();
                int score = Integer.parseInt(tokens[2].trim());
                
                if (score >= 80) {
                    passList.add(id);
                } else if (score >= 50) { // Từ 50 đến 79
                    reviewList.add(id);
                } else { // Nhỏ hơn 50
                    failList.add(id);
                }
            }
        }
        
        // d. Tạo chuỗi kết quả dạng PASS=id,id;REVIEW=id;FAIL=id
        String result = "PASS=" + String.join(",", passList) +
                       ";REVIEW=" + String.join(",", reviewList) +
                       ";FAIL=" + String.join(",", failList);
        
        // Gửi frame kết quả
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
// b. Nhận đúng 02 frame liên tiếp, nối payload theo thứ tự để được danh sách bản ghi id,type,score phân tách bằng |. Ví dụ: R500,AUTH,90|R501,PAY,45.
// c. Phân loại id vào PASS nếu score >= 80, REVIEW nếu score từ 50 đến 79, và FAIL nếu score nhỏ hơn 50; giữ thứ tự xuất hiện.
// d. Gửi một frame kết quả theo định dạng PASS=id,id;REVIEW=id;FAIL=id. Ví dụ: PASS=R500;REVIEW=;FAIL=R501.
// e. Đóng kết nối hoặc kết thúc client sau khi nộp kết quả.