import java.io.*;
import java.net.*;
import java.util.*;

public class idempotency {
    public static void main(String[] args) throws Exception {
        String studentCode = "B22DCVT090";
        String qCode = "Klt4n3p6";
        String baseUrl = "http://36.50.135.242:2230/api/rest/method";

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

        String capTag = "\"capacity\":";
        int capStart = jsonStr.indexOf(capTag) + capTag.length();
        int capEnd = capStart;
        while (capEnd < jsonStr.length() && Character.isDigit(jsonStr.charAt(capEnd))) {
            capEnd++;
        }
        int capacity = Integer.parseInt(jsonStr.substring(capStart, capEnd).trim());

        int reqsStart = jsonStr.indexOf("\"requests\":[") + 12;
        int reqsEnd = jsonStr.lastIndexOf("]");
        String reqsStr = jsonStr.substring(reqsStart, reqsEnd);

        List<String> cacheKeys = new ArrayList<>();
        List<String> cacheIds = new ArrayList<>();

        int idx = 0;
        while ((idx = reqsStr.indexOf("{", idx)) != -1) {
            int endIdx = reqsStr.indexOf("}", idx);
            if (endIdx == -1) break;

            String objStr = reqsStr.substring(idx, endIdx + 1);

            String idTag = "\"id\":\"";
            int idS = objStr.indexOf(idTag) + idTag.length();
            int idE = objStr.indexOf("\"", idS);
            String id = objStr.substring(idS, idE);

            String keyTag = "\"key\":\"";
            int keyS = objStr.indexOf(keyTag) + keyTag.length();
            int keyE = objStr.indexOf("\"", keyS);
            String key = objStr.substring(keyS, keyE);

            if (cacheKeys.contains(key)) {
                idx = endIdx + 1;
                continue;
            }

            if (cacheKeys.size() >= capacity) {
                cacheKeys.remove(0);
                cacheIds.remove(0);
            }

            cacheKeys.add(key);
            cacheIds.add(id);

            idx = endIdx + 1;
        }

        StringBuilder answerBuilder = new StringBuilder();
        for (int i = 0; i < cacheIds.size(); i++) {
            answerBuilder.append(cacheIds.get(i));
            if (i < cacheIds.size() - 1) {
                answerBuilder.append(",");
            }
        }
        String answer = answerBuilder.toString();

        URL postUrl = new URL(baseUrl + "/" + requestId);
        HttpURLConnection postConn = (HttpURLConnection) postUrl.openConnection();
        postConn.setRequestMethod("PUT");
        postConn.setRequestProperty("Content-Type", "application/json");
        postConn.setDoOutput(true);

        String jsonPost = "{"
                + "\"studentCode\":\"" + studentCode + "\","
                + "\"qCode\":\"" + qCode + "\","
                + "\"answer\":\"" + answer + "\""
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
// Một dịch vụ REST được triển khai trên server tại URL http://<Exam_IP>:2230/api/rest/method để xử lý các bài toán mô phỏng phương thức xử lý, trạng thái và quan hệ phụ thuộc. Yêu cầu: Viết chương trình tại máy trạm (REST client) để giao tiếp với dịch vụ và thực hiện các công việc sau.
// a. Gửi GET /api/rest/method?studentCode=<mã_sinh_viên>&qCode=<qAlias> để nhận JSON gồm requestId và data.
// b. Server trả về data là object gồm capacity và requests.
// c. Duyệt các request theo thứ tự, chỉ nhận request đầu tiên của mỗi key và loại bỏ duplicate theo LRU cache.
// d. Gửi PUT /api/rest/method/{requestId} với body JSON gồm studentCode, qCode và answer.
// Lưu ý: requestId lấy từ phase 1 và truyền trên path.
// e. Ví dụ: nếu chỉ nhận được REQ-101,REQ-205` thì answer là REQ-101,REQ-205.