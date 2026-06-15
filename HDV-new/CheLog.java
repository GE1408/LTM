import java.io.*;
import java.net.*;
import java.util.*;

public class CheLog {
    public static void main(String[] args) throws Exception {
        String studentCode = "B22DCVT090";
        String qCode = "l2Uqq3PL";
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

        String dataTag = "\"data\":\"";
        int dataStart = jsonStr.indexOf(dataTag) + dataTag.length();
        int dataEnd = jsonStr.lastIndexOf("\"");
        if (jsonStr.contains(",\"requestId\"") && jsonStr.indexOf(",\"requestId\"") > dataStart) {
            dataEnd = jsonStr.indexOf(",\"requestId\"");
            if (jsonStr.charAt(dataEnd - 1) == '"') {
                dataEnd--;
            }
        }
        String data = jsonStr.substring(dataStart, dataEnd);
        data = data.replace("\\\"", "\"").replace("\\\\", "\\");

        String[] lines = data.split("\\|\\|");
        StringBuilder answerBuilder = new StringBuilder();

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            
            line = line.replaceAll("[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}", "[EMAIL]");
            line = line.replaceAll("\\b0\\d{9}\\b", "[PHONE]");
            line = line.replaceAll("token=[a-zA-Z0-9]+", "token=[TOKEN]");

            answerBuilder.append(line);
            if (i < lines.length - 1) {
                answerBuilder.append("||");
            }
        }

        if (data.endsWith("||") && !answerBuilder.toString().endsWith("||")) {
            answerBuilder.append("||");
        }

        String answer = answerBuilder.toString();

        URL postUrl = new URL(baseUrl + "/submit");
        HttpURLConnection postConn = (HttpURLConnection) postUrl.openConnection();
        postConn.setRequestMethod("POST");
        postConn.setRequestProperty("Content-Type", "application/json");
        postConn.setDoOutput(true);

        String escapedAnswer = answer.replace("\\", "\\\\").replace("\"", "\\\"");

        String jsonPost = "{"
                + "\"studentCode\":\"" + studentCode + "\","
                + "\"qCode\":\"" + qCode + "\","
                + "\"requestId\":\"" + requestId + "\","
                + "\"answer\":\"" + escapedAnswer + "\""
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
// Một dịch vụ REST CharacterService được triển khai trên server tại URL http://<Exam_IP>:2230/api/rest/character để xử lý các bài toán về chuỗi và ký tự qua HTTP/JSON.
// Yêu cầu: Viết chương trình tại máy trạm (REST client) để giao tiếp với CharacterService và thực hiện các công việc sau.
// a. Gửi GET /api/rest/character?studentCode=<mã_sinh_viên>&qCode=<qAlias>. qCode là alias runtime được giao.
// b. Server trả về requestId và data là nhiều dòng log nối bằng ||. Mỗi dòng có thể chứa email, số điện thoại Việt Nam 10 chữ số bắt đầu bằng 0, và token dạng token=<giá_trị>.
// c. Thay email bằng [EMAIL], số điện thoại bằng [PHONE], token bằng token=[TOKEN]. Giữ nguyên thứ tự các dòng.
// d. Gửi POST /api/rest/character/submit với body JSON chứa studentCode, qCode, requestId và answer là chuỗi log sau khi che dữ liệu, các dòng vẫn nối bằng ||.
// e. Ví dụ một phần kết quả: INFO user=[EMAIL] phone=[PHONE] token=[TOKEN].