import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Locale;

public class TrungViTruot {
    public static void main(String[] args) throws Exception {
        String studentCode = "B22DCVT090";
        String qCode = "kMQ9HUus";
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

        String winSizeTag = "\"windowSize\":";
        int winSizeStart = jsonStr.indexOf(winSizeTag) + winSizeTag.length();
        int winSizeEnd = winSizeStart;
        while (winSizeEnd < jsonStr.length() && (Character.isDigit(jsonStr.charAt(winSizeEnd)) || jsonStr.charAt(winSizeEnd) == ' ')) {
            winSizeEnd++;
        }
        int windowSize = Integer.parseInt(jsonStr.substring(winSizeStart, winSizeEnd).trim());

        StringBuilder answerBuilder = new StringBuilder();
        for (int i = 0; i <= values.length - windowSize; i++) {
            int[] subArray = new int[windowSize];
            for (int j = 0; j < windowSize; j++) {
                subArray[j] = values[i + j];
            }
            Arrays.sort(subArray);
            
            double median;
            if (windowSize % 2 == 1) {
                median = subArray[windowSize / 2];
            } else {
                median = (subArray[(windowSize / 2) - 1] + subArray[windowSize / 2]) / 2.0;
            }
            
            answerBuilder.append(String.format(Locale.US, "%.2f", median));
            if (i < values.length - windowSize) {
                answerBuilder.append(",");
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
// b. Server trả về `data` là object gồm `values`
//    (mảng số nguyên) và `windowSize`.
// c. Tính trung vị của từng cửa sổ trượt kích thước
//    `windowSize` theo thứ tự xuất hiện.
// d. Gửi POST /api/rest/data/submit với body JSON gồm
//    studentCode, qCode, requestId và answer.
// e. Ví dụ: nếu `values=[1,2,3,4]` và `windowSize=2`
//    thì `answer` là `1.50,2.50,3.50`.