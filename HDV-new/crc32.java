import java.io.*;
import java.net.*;
import java.util.*;
import java.util.zip.*;

public class crc32 {
    public static void main(String[] args) throws Exception {
        String studentCode = "B22DCVT090";
        String qCode = "nlSR3mJE";
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

        String payload = "";
        if (jsonStr.contains("\"payload\":\"")) {
            String payloadTag = "\"payload\":\"";
            int payloadStart = jsonStr.indexOf(payloadTag) + payloadTag.length();
            int payloadEnd = jsonStr.indexOf("\"", payloadStart);
            payload = jsonStr.substring(payloadStart, payloadEnd);
        }

        CRC32 crc = new CRC32();
        crc.update(payload.getBytes("UTF-8"));
        String answer = Long.toHexString(crc.getValue()).toLowerCase();

        URL postUrl = new URL(baseUrl + "/submit");
        HttpURLConnection postConn = (HttpURLConnection) postUrl.openConnection();
        postConn.setRequestMethod("POST");
        postConn.setRequestProperty("Content-Type", "application/json");
        postConn.setDoOutput(true);

        String jsonPost = "{"
                + "\"studentCode\":\"" + studentCode + "\","
                + "\"qCode\":\"" + qCode + "\","
                + "\"requestId\":\"" + requestId + "\","
                + "\"answer\":\"" + answer + "\""
                + "}";

        OutputStream os = postConn.getOutputStream();
        os.write(jsonPost.getBytes("UTF-8"));
        os.flush();
        os.close();

        int responseCode = postConn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
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
// Một dịch vụ REST được triển khai trên server tại URL http://<Exam_IP>:2230/api/rest/header để xử lý các bài toán mã kiểm tra, chữ ký và băm dữ liệu. Yêu cầu: Viết chương trình tại máy trạm (REST client) để giao tiếp với dịch vụ và thực hiện các công việc sau.
// a. Gửi GET /api/rest/header?studentCode=<mã_sinh_viên>&qCode=<qAlias> để nhận JSON gồm requestId và data.
// b. Server trả về data là object gồm payload và một giá trị checksum để đối chiếu.
// c. Tính CRC32 của payload và nộp lại kết quả ở dạng hex chữ thường.
// d. Gửi POST /api/rest/header/submit với body JSON gồm studentCode, qCode, requestId và answer.
// e. Ví dụ: nếu CRC32 của payload là 1a2b3c4d thì answer là 1a2b3c4d.