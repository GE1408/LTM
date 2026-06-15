import java.io.*;
import java.net.*;
import java.util.*;

public class XChecksum {
    public static void main(String[] args) throws Exception {
        String studentCode = "B22DCVT090";
        String qCode = "sUc0oprf";
        String baseUrl = "http://36.50.135.242:2230/api/rest/header";

        String getUrlStr = baseUrl + "?studentCode=" + studentCode + "&qCode=" + qCode;
        URL getUrl = new URL(getUrlStr);
        HttpURLConnection getConn = (HttpURLConnection) getUrl.openConnection();
        getConn.setRequestMethod("GET");

        String checksumValue = getConn.getHeaderField("X-Checksum");

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

        URL postUrl = new URL(baseUrl + "/submit");
        HttpURLConnection postConn = (HttpURLConnection) postUrl.openConnection();
        postConn.setRequestMethod("POST");
        postConn.setRequestProperty("Content-Type", "application/json");
        if (checksumValue != null) {
            postConn.setRequestProperty("X-Checksum", checksumValue);
        }
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
// b. Server trả về requestId, data là danh sách mã giao dịch và response header X-Checksum.
// c. Đọc giá trị X-Checksum từ response phase 1.
// d. Gửi POST /api/rest/header/submit với body chứa studentCode, qCode, requestId và kèm lại header X-Checksum.
// e. Giá trị header ở phase 2 phải trùng chính xác với header server đã trả về.