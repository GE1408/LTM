package restsumproject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONObject;

public class RestSumProject {

    public static void main(String[] args) throws Exception {
        String studentCode = "B22DCVT090";
        String qCode = "uYAcNRUA";
        String examIP = "36.50.135.242";
        String getUrl = "http://" + examIP + ":2230/api/rest/data" + "?studentCode=" + studentCode + "&qCode=" + qCode;
        System.out.println("GET URL:");
        System.out.println(getUrl);
        URL url = new URL(getUrl);
        HttpURLConnection getConn =
                (HttpURLConnection) url.openConnection();
        getConn.setRequestMethod("GET");
        BufferedReader in = new BufferedReader(new InputStreamReader(getConn.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        System.out.println("GET RESPONSE:");
        System.out.println(response.toString());
        JSONObject json = new JSONObject(response.toString());
        String requestId = json.getString("requestId");
        JSONArray data = json.getJSONArray("data");
        int sum = 0;
        for (int i = 0; i < data.length(); i++) {
            sum += data.getInt(i);
        }
        System.out.println("SUM = " + sum);
        JSONObject postData = new JSONObject();
        postData.put("studentCode", studentCode);
        postData.put("qCode", qCode);
        postData.put("requestId", requestId);
        postData.put("answer", sum);
        String postUrl = "http://" + examIP + ":2230/api/rest/data/submit";
        URL submitUrl = new URL(postUrl);
        HttpURLConnection postConn = (HttpURLConnection) submitUrl.openConnection();
        postConn.setRequestMethod("POST");
        postConn.setRequestProperty(
                "Content-Type",
                "application/json"
        );
        postConn.setDoOutput(true);
        OutputStream os = postConn.getOutputStream();
        os.write(postData.toString().getBytes());
        os.flush();
        os.close();
        BufferedReader postIn = new BufferedReader(new InputStreamReader(postConn.getInputStream()));
        StringBuilder postResponse =
                new StringBuilder();
        while ((inputLine = postIn.readLine()) != null) {
            postResponse.append(inputLine);
        }
        postIn.close();
        System.out.println("POST RESPONSE:");
        System.out.println(postResponse.toString());
    }
}
//Một dịch vụ REST được triển khai trên server tại URL http://<Exam_IP>:2230/api/rest/data để xử lý các bài toán với dữ liệu nguyên thủy.
//Yêu cầu: Viết chương trình Java (REST client) để giao tiếp với DataService và thực hiện các công việc sau:
//Gửi HTTP GET request tới /api/rest/data?studentCode=<mã_sv>&qCode=<qCode trong đề bài> để nhận về một đối tượng JSON từ server.
//Response JSON có dạng:
//{
  //"requestId": "a1b2c3d4",
  //"data": [7602, 9136, 1090, 3431, 7830, 6179]
//}
//Thực hiện tính tổng của tất cả các phần tử trong danh sách số nguyên nhận được.
//Gửi HTTP POST request tới /api/rest/data/submit với body JSON:
//{
  //"studentCode": "B21DCCN001",
  //"qCode": "<qCode trong đề bài>",
  //"requestId": "a1b2c3d4",
  //"answer": 35268
//}
//Trong body JSON trên, trường "requestId": "a1b2c3d4" là requestId nhận được ở bước 1.
//Ví dụ: Nếu danh sách số nguyên nhận được là [1, 2, 3, 4, 5], chương trình client tính tổng là 15 và gửi "answer": 15.
//Kết thúc chương trình client. Server trả về {"status":"AC"} hoặc {"status":"WA"}.