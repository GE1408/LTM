import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DayTangSauChuanHoa {
    public static void main(String[] args) throws Exception {
        String studentCode = "B22DCVT090";
        String qCode = "1ewfWd3D";
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

        int valuesStart = jsonStr.indexOf("[") + 1;
        int valuesEnd = jsonStr.indexOf("]");
        String valuesStr = jsonStr.substring(valuesStart, valuesEnd);
        String[] tokens = valuesStr.split(",");
        int[] values = new int[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            values[i] = Integer.parseInt(tokens[i].trim());
        }

        List<Integer> piles = new ArrayList<>();
        for (int x : values) {
            int idx = Collections.binarySearch(piles, x);
            if (idx < 0) {
                idx = -(idx + 1);
            }
            if (idx == piles.size()) {
                piles.add(x);
            } else {
                piles.set(idx, x);
            }
        }
        
        String answer = String.valueOf(piles.size());

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
// http://<Exam_IP>:2230/api/rest/data để xử lý các bài toán với dữ liệu
// dạng mảng, chuỗi mã và số liệu.
// Yêu cầu: Viết chương trình tại máy trạm (REST client) để giao tiếp
// với dịch vụ và thực hiện các công việc sau:
// a. Gửi GET /api/rest/data?studentCode=<mã_sinh_viên>&qCode=<qAlias>
//    để nhận JSON gồm requestId và data.
// b. Server trả về `data` là object gồm `values`.
// c. Chuẩn hóa dữ liệu nếu cần rồi tính độ dài dãy con tăng dài nhất
//    bằng patience sorting.
// d. Gửi POST /api/rest/data/submit với body JSON gồm studentCode,
//    qCode, requestId và answer.
// e. Ví dụ: nếu `values=[3,1,2,5,4]` thì `answer` là `3`.