package restsumproject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI; // Sử dụng URI để tránh cảnh báo dòng đỏ về URL cũ
import java.net.URL;
import org.json.JSONObject;

public class HTTP_PUT {

    public static void main(String[] args) throws Exception {
        // --- 1. THÔNG TIN CẤU HÌNH ---
        String studentCode = "B22DCVT090"; // Mã sinh viên của bạn
        String qCode = "A5bfb2nZ"; // Mã câu hỏi
        String examIP = "36.50.135.242";
        String port = "2230";
        String baseUrl = "http://" + examIP + ":" + port + "/api/rest/method";

        // --- 2. BƯỚC 1: GỬI GET ĐỂ LẤY REQUESTID ---
        String getUrlStr = baseUrl + "?studentCode=" + studentCode + "&qCode=" + qCode;
        
        // Sử dụng URI.create().toURL() để không hiện cảnh báo đỏ deprecated trong NetBeans
        URL getUrl = URI.create(getUrlStr).toURL();
        HttpURLConnection getConn = (HttpURLConnection) getUrl.openConnection();
        getConn.setRequestMethod("GET");
        getConn.setRequestProperty("Accept", "application/json");

        // Đọc dữ liệu phản hồi từ lệnh GET
        BufferedReader in = new BufferedReader(new InputStreamReader(getConn.getInputStream()));
        StringBuilder res1 = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            res1.append(line);
        }
        in.close();
        System.out.println("GET Response: " + res1.toString());

        // Parse JSON để lấy requestId
        JSONObject jsonRes = new JSONObject(res1.toString());
        String requestId = jsonRes.getString("requestId");

        // --- 3. BƯỚC 2: GỬI CẬP NHẬT (PUT) ---
        // Đường dẫn yêu cầu: /api/rest/method/{requestId}
        String putUrlStr = baseUrl + "/" + requestId;
        URL putUrl = URI.create(putUrlStr).toURL();
        
        HttpURLConnection putConn = (HttpURLConnection) putUrl.openConnection();
        putConn.setRequestMethod("PUT");
        putConn.setRequestProperty("Content-Type", "application/json");
        putConn.setDoOutput(true);

        // Tạo Body JSON theo đúng mẫu yêu cầu của đề bài
        JSONObject answer = new JSONObject();
        answer.put("status", "done");

        JSONObject putBody = new JSONObject();
        putBody.put("studentCode", studentCode);
        putBody.put("qCode", qCode);
        putBody.put("answer", answer);

        // Gửi dữ liệu PUT lên Server
        OutputStream os = putConn.getOutputStream();
        os.write(putBody.toString().getBytes("UTF-8"));
        os.flush();
        os.close();

        // --- 4. KIỂM TRA KẾT QUẢ ---
        int responseCode = putConn.getResponseCode();
        System.out.println("PUT Response Code: " + responseCode);

        // Đọc kết quả cuối cùng để xác nhận trạng thái "AC"
        if (responseCode == HttpURLConnection.HTTP_OK || responseCode == 201) {
            BufferedReader putIn = new BufferedReader(new InputStreamReader(putConn.getInputStream()));
            StringBuilder res2 = new StringBuilder();
            while ((line = putIn.readLine()) != null) {
                res2.append(line);
            }
            putIn.close();
            System.out.println("KẾT QUẢ CUỐI CÙNG: " + res2.toString());
        } else {
            System.out.println("Lỗi: Server phản hồi mã " + responseCode);
        }
    }
}

//Bài tập này yêu cầu bạn sử dụng phương thức HTTP PUT để cập nhật dữ liệu một tài nguyên. Bạn sẽ nhận dữ liệu ban đầu qua GET và gửi cập nhật qua PUT.
//Giao thức
//Bước 1 — Lấy dữ liệu (GET):
//GET /api/rest/method?studentCode=<mã_sv>&qCode=<qAlias>
//Phản hồi:
//{
  //"requestId": "abc12345",
  //"data": {
    //"id": 512,
    //"title": "Update task #7",
    //"status": "pending"
  ///}
//}
//Bước 2 — Gửi cập nhật (PUT):

//PUT /api/rest/method/{requestId}
//Body JSON:
//{
  //"studentCode": "<mã_sv>",
  //"qCode": "<qAlias>",
  //"answer": {
    //"status": "done"
  //}
//}
//Phản hồi khi đúng:
//{"status": "AC", "message": "..."}
//Yêu cầu
//Endpoint Phase 2 chỉ chấp nhận phương thức PUT — gửi GET/POST sẽ nhận lỗi 405.
//Trường answer.status phải có giá trị "done".