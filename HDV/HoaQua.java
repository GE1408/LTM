package restsumproject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

import org.json.JSONObject;
public class HoaQua {
    public static void main(String[] args) throws Exception {
        String studentCode = "B22DCVT090";
        String qCode = "byIfjo4y";
        String examIP = "36.50.135.242";
        String getUrl =
    "http://" + examIP +
    ":2230/api/rest/character"
    + "?studentCode=" + studentCode
    + "&qCode=" + qCode;
        URL url = new URL(getUrl);
        HttpURLConnection getConn =
                (HttpURLConnection) url.openConnection();
        getConn.setRequestMethod("GET");
        BufferedReader in =
                new BufferedReader(
                        new InputStreamReader(
                                getConn.getInputStream()
                        )
                );
        String inputLine;
        StringBuilder response =
                new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        JSONObject json =
                new JSONObject(response.toString());
        String requestId =
                json.getString("requestId");
        String data =
                json.getString("data");
        String[] words =
                data.split(" ");
        Arrays.sort(words);
        String answer =
                String.join(" ", words);
        JSONObject postData =
                new JSONObject();
        postData.put("studentCode", studentCode);
        postData.put("qCode", qCode);
        postData.put("requestId", requestId);
        postData.put("answer", answer);
        String postUrl =
                "http://" + examIP +
                ":2230/api/rest/character/submit";
        URL submitUrl =
                new URL(postUrl);
        HttpURLConnection postConn =
                (HttpURLConnection) submitUrl.openConnection();
        postConn.setRequestMethod("POST");
        postConn.setRequestProperty(
                "Content-Type",
                "application/json"
        );
        postConn.setDoOutput(true);
        OutputStream os =
                postConn.getOutputStream();

        os.write(postData.toString().getBytes());
        os.flush();
        os.close();
        BufferedReader postIn =
                new BufferedReader(
                        new InputStreamReader(
                                postConn.getInputStream()
                        )
                );
        StringBuilder postResponse =
                new StringBuilder();
        while ((inputLine = postIn.readLine()) != null) {
            postResponse.append(inputLine);
        }
        postIn.close();
        System.out.println(postResponse.toString());
    }
}

//Một dịch vụ REST được triển khai trên server tại URL http://<Exam_IP>:2230/api/rest/character để xử lý các bài toán về chuỗi và ký tự.
//Yêu cầu: Viết chương trình Java (REST client) để giao tiếp với CharacterService và thực hiện các công việc sau:
//Gửi HTTP GET request tới /api/rest/character?studentCode=<mã_sv>&qCode=<qCode trong đề bài> để nhận về một đối tượng JSON từ server.
//Response JSON có dạng:
//{
  //"requestId": "x1y2z3w4",
  //"data": "banana apple cherry date elderberry"
//}
//Tách chuỗi thành các từ dựa trên khoảng trắng, sau đó sắp xếp các từ theo thứ tự từ điển 
//(alphabetical order, phân biệt hoa thường - case-sensitive).
//Gửi HTTP POST request tới /api/rest/character/submit với body JSON:
//{
  //"studentCode": "B21DCCN001",
  //"qCode": "<qCode trong đề bài>",
  //"requestId": "x1y2z3w4",
  //"answer": "apple banana cherry date elderberry"
//}
//Trong body JSON trên, trường "requestId": "x1y2z3w4" là requestId nhận được ở bước 1.
//Các từ nối lại bằng dấu cách đơn.
//Ví dụ 1: "banana apple cherry" -> sắp xếp -> "apple banana cherry"
//Ví dụ 2 (case-sensitive): "Cherry apple Banana" -> sắp xếp theo thứ tự từ điển -> "Banana Cherry apple" (chữ hoa đứng trước chữ thường trong ASCII)
//Kết thúc chương trình client. Server trả về {"status":"AC"} hoặc {"status":"WA"}.