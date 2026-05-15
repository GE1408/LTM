/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tcpgzipclient;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

public class TCPQueryStringClient {

    static void readFully(SocketChannel channel, ByteBuffer buffer) throws Exception {
        while (buffer.hasRemaining()) {
            channel.read(buffer);
        }
    }

    static void writeFully(SocketChannel channel, ByteBuffer buffer) throws Exception {
        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }
    }

    static String readFrame(SocketChannel channel) throws Exception {

        ByteBuffer lenBuffer = ByteBuffer.allocate(4);
        readFully(channel, lenBuffer);

        lenBuffer.flip();
        int len = lenBuffer.getInt();

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
        String qCode = "EtZJAj51";

        SocketChannel channel = SocketChannel.open();
        channel.connect(new InetSocketAddress(server, port));

        // gửi MSSV;qCode
        writeFrame(channel, studentCode + ";" + qCode);

        // nhận 2 frame
        String part1 = readFrame(channel);
        String part2 = readFrame(channel);

        String query = part1 + part2;

        // parse query string
        String[] pairs = query.split("&");

        TreeMap<String, String> map = new TreeMap<>();

        for (String pair : pairs) {

            String[] kv = pair.split("=", 2);

            String key = URLDecoder.decode(kv[0], "UTF-8");

            String value = "";

            if (kv.length > 1) {
                value = URLDecoder.decode(kv[1], "UTF-8");
            }

            map.put(key, value);
        }

        // tạo kết quả
        StringBuilder result = new StringBuilder();

        for (Map.Entry<String, String> entry : map.entrySet()) {

            if (result.length() > 0) {
                result.append(";");
            }

            result.append(entry.getKey())
                  .append("=")
                  .append(entry.getValue());
        }

        // gửi kết quả
        writeFrame(channel, result.toString());

        channel.close();
    }
}