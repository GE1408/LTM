/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tcpgzipclient;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class TCPHTTPFrameClient {

    static void writeFully(SocketChannel channel, ByteBuffer buffer) throws Exception {
        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }
    }

    static void readFully(SocketChannel channel, ByteBuffer buffer) throws Exception {
        while (buffer.hasRemaining()) {
            channel.read(buffer);
        }
    }

    static String readFrame(SocketChannel channel) throws Exception {

        // đọc 4 byte độ dài
        ByteBuffer lenBuffer = ByteBuffer.allocate(4);
        readFully(channel, lenBuffer);

        lenBuffer.flip();
        int len = lenBuffer.getInt();

        // đọc payload
        ByteBuffer dataBuffer = ByteBuffer.allocate(len);
        readFully(channel, dataBuffer);

        dataBuffer.flip();

        return StandardCharsets.UTF_8.decode(dataBuffer).toString();
    }

    static void writeFrame(SocketChannel channel, String message) throws Exception {

        byte[] data = message.getBytes(StandardCharsets.UTF_8);

        ByteBuffer buffer = ByteBuffer.allocate(4 + data.length);

        buffer.putInt(data.length);
        buffer.put(data);

        buffer.flip();

        writeFully(channel, buffer);
    }

    public static void main(String[] args) throws Exception {

        String server = "36.50.135.242";
        int port = 2211;

        String studentCode = "B22DCVT090";
        String qCode = "aqHSLWKk";

        SocketChannel channel = SocketChannel.open();
        channel.connect(new InetSocketAddress(server, port));

        // gửi MSSV;qCode
        writeFrame(channel, studentCode + ";" + qCode);

        // nhận 3 frame HTTP
        String part1 = readFrame(channel);
        String part2 = readFrame(channel);
        String part3 = readFrame(channel);

        String httpRequest = part1 + part2 + part3;

        // tách request line
        String[] lines = httpRequest.split("\r\n");

        String requestLine = lines[0];

        String[] requestParts = requestLine.split(" ");

        String method = requestParts[0];
        String path = requestParts[1];

        // lấy Host
        String host = "";

        for (String line : lines) {
            if (line.startsWith("Host:")) {
                host = line.substring(5).trim();
                break;
            }
        }

        String result = method + ";" + path + ";" + host;

        // gửi kết quả
        writeFrame(channel, result);

        channel.close();
    }
}

//Một chương trình server cho phép kết nối qua giao thức TCP tại cổng 2211 (thời gian giao tiếp tối đa cho mỗi yêu cầu là 5s). Yêu cầu là xây dựng một chương trình client tương tác tới server ở trên sử dụng SocketChannel và ByteBuffer để trao đổi thông tin theo giao thức frame: 4 byte độ dài (int32) + payload (UTF-8).
//Lưu ý: server & client đều phải đọc đủ dữ liệu bằng vòng lặp (readFully) do server luôn chia nhỏ dữ liệu khi gửi. Trình tự trao đổi như sau:
//a. Gửi mã sinh viên và mã câu hỏi theo định dạng "studentCode;qCode"
//Ví dụ: "B16DCCN999;fkdRJYuX
//b. Nhận dữ liệu từ server gồm đúng 3 frame liên tiếp. Payload của mỗi frame là một phần của cùng một HTTP request, client phải nối 3 payload theo đúng thứ tự để thu được chuỗi HTTP request hoàn chỉnh (các dòng phân tách bởi "\r\n" và kết thúc bằng "\r\n\r\n").
//c. Từ chuỗi HTTP request hoàn chỉnh, trích xuất và gửi lại lên server theo định dạng "METHOD;PATH;HOST" trong đó PATH luôn bao gồm query-string.
//d. Đóng kết nối và kết thúc chương trình.