import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class KMP {
    public static void main(String[] args) throws Exception {
        String studentCode = "B22DCVT090";
        String qCode = "Bh5FpFrE";
        String baseUrl = "http://36.50.135.242:2230/api/rest/character";

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

        String textTag = "\"text\":\"";
        int textStart = jsonStr.indexOf(textTag) + textTag.length();
        int textEnd = jsonStr.indexOf("\"", textStart);
        String text = jsonStr.substring(textStart, textEnd);

        String patternTag = "\"pattern\":\"";
        int patternStart = jsonStr.indexOf(patternTag) + patternTag.length();
        int patternEnd = jsonStr.indexOf("\"", patternStart);
        String pattern = jsonStr.substring(patternStart, patternEnd);

        int m = pattern.length();
        int n = text.length();
        int[] lps = new int[m];
        int len = 0;
        int i = 1;
        while (i < m) {
            if (pattern.charAt(i) == pattern.charAt(len)) {
                len++;
                lps[i] = len;
                i++;
            } else {
                if (len != 0) {
                    len = lps[len - 1];
                } else {
                    lps[i] = 0;
                    i++;
                }
            }
        }

        int result = -1;
        int idxText = 0;
        int idxPat = 0;
        while (idxText < n) {
            if (pattern.charAt(idxPat) == text.charAt(idxText)) {
                idxText++;
                idxPat++;
            }
            if (idxPat == m) {
                result = idxText - idxPat;
                break;
            } else if (idxText < n && pattern.charAt(idxPat) != text.charAt(idxText)) {
                if (idxPat != 0) {
                    idxPat = lps[idxPat - 1];
                } else {
                    idxText++;
                }
            }
        }

        String answer = String.valueOf(result);

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
// http://<Exam_IP>:2230/api/rest/character để xử lý
// các bài toán về chuỗi và ký tự.
// Yêu cầu: Viết chương trình tại máy trạm (REST client)
// để giao tiếp với dịch vụ và thực hiện các công việc sau:
// a. Gửi GET /api/rest/character?studentCode=<mã_sinh_viên>
//    &qCode=<qAlias> để nhận JSON gồm requestId và data.
// b. Server trả về `data` là object gồm `text` và `pattern`.
// c. Tìm vị trí xuất hiện đầu tiên của `pattern` trong `text`
//    bằng KMP.
// d. Gửi POST /api/rest/character/submit với body JSON gồm
//    studentCode, qCode, requestId và answer.
// e. Ví dụ: nếu `text="alpha beta gamma"` và
//    `pattern="beta"` thì `answer` là `6`.