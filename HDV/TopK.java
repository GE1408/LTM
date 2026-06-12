import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TopK {
    public static void main(String[] args) throws Exception {
        String studentCode = "B22DCVT090";
        String qCode = "qr5hGp7C";
        String baseUrl = "http://36.50.135.242:2230/api/rest/data";

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

        int codesStart = jsonStr.indexOf("[") + 1;
        int codesEnd = jsonStr.indexOf("]");
        String codesStr = jsonStr.substring(codesStart, codesEnd);
        String[] tokens = codesStr.split(",");
        List<String> codes = new ArrayList<>();
        for (String token : tokens) {
            String cleanToken = token.replace("\"", "").trim();
            if (!cleanToken.isEmpty()) {
                codes.add(cleanToken);
            }
        }

        String kTag = "\"k\":";
        int kStart = jsonStr.indexOf(kTag) + kTag.length();
        int kEnd = kStart;
        while (kEnd < jsonStr.length() && (Character.isDigit(jsonStr.charAt(kEnd)) || jsonStr.charAt(kEnd) == ' ')) {
            kEnd++;
        }
        int k = Integer.parseInt(jsonStr.substring(kStart, kEnd).trim());

        Map<String, Integer> frequencyMap = new HashMap<>();
        for (String code : codes) {
            frequencyMap.put(code, frequencyMap.getOrDefault(code, 0) + 1);
        }

        List<Map.Entry<String, Integer>> entryList = new ArrayList<>(frequencyMap.entrySet());
        Collections.sort(entryList, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                int freqCompare = o2.getValue().compareTo(o1.getValue());
                if (freqCompare != 0) {
                    return freqCompare;
                }
                return o1.getKey().compareTo(o2.getKey());
            }
        });

        StringBuilder answerBuilder = new StringBuilder();
        int limit = Math.min(k, entryList.size());
        for (int i = 0; i < limit; i++) {
            Map.Entry<String, Integer> entry = entryList.get(i);
            answerBuilder.append(entry.getKey()).append("=").append(entry.getValue());
            if (i < limit - 1) {
                answerBuilder.append("|");
            }
        }
        String answer = answerBuilder.toString();

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
// Một dịch vụ REST được triển khai trên server tại URL
// http://<Exam_IP>:2230/api/rest/data để xử lý các bài toán
// với dữ liệu dạng mảng, chuỗi mã và số liệu.
// Yêu cầu: Viết chương trình tại máy trạm (REST client)
// để giao tiếp với dịch vụ và thực hiện các công việc sau:
// a. Gửi GET /api/rest/data?studentCode=<mã_sinh_viên>
//    &qCode=<qAlias> để nhận JSON gồm requestId và data.
// b. Server trả về `data` là object gồm `codes`
//    (mảng mã giao dịch) và `k`.
// c. Đếm tần suất từng mã, chọn `k` mã có tần suất
//    cao nhất; nếu hòa thì chọn mã nhỏ hơn theo
//    thứ tự từ điển.
// d. Gửi POST /api/rest/data/submit với body JSON gồm
//    studentCode, qCode, requestId và answer.
// e. Ví dụ: nếu `codes=["TX-10","TX-20","TX-10"]`
//    và `k=1` thì `answer` là `TX-10=2`.