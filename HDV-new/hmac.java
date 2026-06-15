import java.io.*;
import java.net.*;
import java.util.*;
import javax.crypto.*;
import javax.crypto.spec.*;

public class hmac {
    public static void main(String[] args) throws Exception {
        String studentCode = "B22DCVT090";
        String qCode = "XZEg37FS";
        String baseUrl = "http://36.50.135.242:2230/api/rest/header";

        String getUrlStr = baseUrl + "?studentCode=" + studentCode + "&qCode=" + qCode;
        URL getUrl = new URL(getUrlStr);
        HttpURLConnection getConn = (HttpURLConnection) getUrl.openConnection();
        getConn.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(getConn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        String jsonStr = response.toString();

        String requestId = "";
        if (jsonStr.contains("\"requestId\":\"")) {
            String reqIdTag = "\"requestId\":\"";
            int reqIdStart = jsonStr.indexOf(reqIdTag) + reqIdTag.length();
            int reqIdEnd = jsonStr.indexOf("\"", reqIdStart);
            requestId = jsonStr.substring(reqIdStart, reqIdEnd);
        }

        String nonce = "";
        if (jsonStr.contains("\"nonce\":\"")) {
            String nonceTag = "\"nonce\":\"";
            int nonceStart = jsonStr.indexOf(nonceTag) + nonceTag.length();
            int nonceEnd = jsonStr.indexOf("\"", nonceStart);
            nonce = jsonStr.substring(nonceStart, nonceEnd);
        }

        String signingKey = "";
        if (jsonStr.contains("\"signingKey\":\"")) {
            String signingKeyTag = "\"signingKey\":\"";
            int signingKeyStart = jsonStr.indexOf(signingKeyTag) + signingKeyTag.length();
            int signingKeyEnd = jsonStr.indexOf("\"", signingKeyStart);
            signingKey = jsonStr.substring(signingKeyStart, signingKeyEnd);
        }

        int eventsStart = jsonStr.indexOf("\"events\":[") + 10;
        int eventsEnd = jsonStr.indexOf("]", eventsStart);
        String eventsStr = jsonStr.substring(eventsStart, eventsEnd);
        String[] eventTokens = eventsStr.split(",");
        List<String> events = new ArrayList<>();
        for (String t : eventTokens) {
            String clean = t.replace("\"", "").trim();
            if (!clean.isEmpty()) {
                events.add(clean);
            }
        }

        StringBuilder payloadBuilder = new StringBuilder();
        payloadBuilder.append(nonce).append(":");
        for (int i = 0; i < events.size(); i++) {
            payloadBuilder.append(events.get(i));
            if (i < events.size() - 1) {
                payloadBuilder.append("|");
            }
        }
        payloadBuilder.append(":").append(studentCode.toUpperCase());
        String payload = payloadBuilder.toString();

        Mac hmacSha256 = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(signingKey.getBytes("UTF-8"), "HmacSHA256");
        hmacSha256.init(secretKey);
        byte[] rawHmac = hmacSha256.doFinal(payload.getBytes("UTF-8"));

        StringBuilder hexString = new StringBuilder();
        for (byte b : rawHmac) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        String signature = hexString.toString().toLowerCase();

        URL postUrl = new URL(baseUrl + "/submit");
        HttpURLConnection postConn = (HttpURLConnection) postUrl.openConnection();
        postConn.setRequestMethod("POST");
        postConn.setRequestProperty("Content-Type", "application/json");
        postConn.setRequestProperty("X-Signature", signature);
        postConn.setDoOutput(true);

        String jsonPost = "{"
                + "\"studentCode\":\"" + studentCode + "\","
                + "\"qCode\":\"" + qCode + "\","
                + "\"requestId\":\"" + requestId + "\""
                + "}";

        OutputStream os = postConn.getOutputStream();
        os.write(jsonPost.getBytes("UTF-8"));
        os.flush();
        os.close();

        int responseCode = postConn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
            BufferedReader postIn = new BufferedReader(new InputStreamReader(postConn.getInputStream()));
            String postLine;
            StringBuilder postResponse = new StringBuilder();
            while ((postLine = postIn.readLine()) != null) {
                postResponse.append(postLine);
            }
            postIn.close();
            System.out.println(postResponse.toString());
        }
    }
}
// Một dịch vụ REST HeaderService được triển khai trên server tại URL http://<Exam_IP>:2230/api/rest/header để kiểm tra cách đọc và gửi HTTP header.
// Yêu cầu: Viết chương trình tại máy trạm (REST client) để giao tiếp với HeaderService và thực hiện các công việc sau.
// a. Gửi GET /api/rest/header?studentCode=<mã_sinh_viên>&qCode=<qAlias>. qCode là alias runtime được giao.
// b. Server trả về requestId và data gồm nonce, events, signingKey.
// c. Tạo payload theo đúng dạng nonce:event1|event2|...:STUDENT_CODE_UPPER, sau đó tính HMAC-SHA256 bằng signingKey.
// d. Gửi POST /api/rest/header/submit với body JSON chứa studentCode, qCode, requestId và header X-Signature là chữ ký hex vừa tính.
// e. Không đưa chữ ký vào body nếu đề yêu cầu header; server chấm giá trị từ X-Signature.