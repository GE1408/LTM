/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package restsumproject;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;

public class finalPrice {

    public static void main(String[] args) throws Exception {

        String studentCode = "B22DCVT090";
        String qCode = "plzWFuzY";
        String examIP = "36.50.135.242";

        String getUrl =
    "http://" + examIP +
    ":2230/api/rest/object/"
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

        JSONObject data =
                json.getJSONObject("data");

        String name =
                data.getString("name");

        double price =
                data.getDouble("price");

        double taxRate =
                data.getDouble("taxRate");

        double discount =
                data.getDouble("discount");

        double finalPrice =
                price * (1 + taxRate / 100)
                * (1 - discount / 100);

        JSONObject answer =
                new JSONObject();

        answer.put("name", name);
        answer.put("price", price);
        answer.put("taxRate", taxRate);
        answer.put("discount", discount);
        answer.put("finalPrice", finalPrice);

        JSONObject postData =
                new JSONObject();

        postData.put("studentCode", studentCode);
        postData.put("qCode", qCode);
        postData.put("requestId", requestId);
        postData.put("answer", answer);

        String postUrl =
                "http://" + examIP +
                ":2230/api/rest/object/submit";

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
//Một dịch vụ REST được triển khai trên server tại URL http://<Exam_IP>:2230/api/rest/object để xử lý các bài toán với đối tượng.
//Yêu cầu: Viết chương trình Java (REST client) để giao tiếp với ObjectService và thực hiện các công việc sau:
//Gửi HTTP GET request tới /api/rest/object?studentCode=<mã_sv>&qCode=<qCode trong đề bài> để nhận về một đối tượng JSON từ server.
//Response JSON có dạng:
//{
  //"requestId": "m1n2o3p4",
  //"data": {
    //"name": "Laptop Pro",
    //"price": 100.0,
    //"taxRate": 10.0,
    //"discount": 5.0
  //}
//}
//Trong đó discount là phần trăm chiết khấu (%).
//Tính toán giá cuối cùng finalPrice theo công thức:
//finalPrice = price × (1 + taxRate / 100) × (1 - discount / 100)
//Gửi HTTP POST request tới /api/rest/object/submit với body JSON:
//{
  //"studentCode": "B21DCCN001",
  //"qCode": "<qCode trong đề bài>",
  //"requestId": "m1n2o3p4",
  //"answer": {
    //"name": "Laptop Pro",
    //"price": 100.0,
    //"taxRate": 10.0,
    //"discount": 5.0,
    //"finalPrice": 104.5
  //}
//}
//Trong body JSON trên, trường "requestId": "m1n2o3p4" là requestId nhận được ở bước 1.
//Ví dụ: price=100.0, taxRate=10.0, discount=5.0 -> finalPrice = 100 × 1.1 × 0.95 = 104.5
//Sai số cho phép: <= 0.01.
//Kết thúc chương trình client. Server trả về {"status":"AC"} hoặc {"status":"WA"}.