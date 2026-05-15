package restsumproject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import org.json.JSONObject;

public class HTTP_PUT_Audit {

    public static void main(String[] args) throws Exception {
        // --- 1. THÔNG TIN CẤU HÌNH ---
        String studentCode = "B22DCVT090"; // Mã sinh viên của bạn
        String qCode = "gsO4TEVK"; // Mã câu hỏi từ image_2b3a1f.jpg
        String examIP = "36.50.135.242"; //
        String port = "2230"; 
        String baseUrl = "http://" + examIP + ":" + port + "/api/rest/method";

        // --- 2. BƯỚC 1: LẤY REQUESTID (GET) ---
        String getUrlStr = baseUrl + "?studentCode=" + studentCode + "&qCode=" + qCode;
        URL getUrl = URI.create(getUrlStr).toURL();
        HttpURLConnection getConn = (HttpURLConnection) getUrl.openConnection();
        getConn.setRequestMethod("GET");
        getConn.setRequestProperty("Accept", "application/json");

        BufferedReader in = new BufferedReader(new InputStreamReader(getConn.getInputStream()));
        StringBuilder res1 = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) res1.append(line);
        in.close();

        JSONObject jsonRes = new JSONObject(res1.toString());
        String requestId = jsonRes.getString("requestId");
        System.out.println("RequestId: " + requestId);

        // --- 3. BƯỚC 2: CẬP NHẬT TRẠNG THÁI (PUT) ---
        // URL: /api/rest/method/{requestId}
        String putUrlStr = baseUrl + "/" + requestId;
        URL putUrl = URI.create(putUrlStr).toURL();
        HttpURLConnection putConn = (HttpURLConnection) putUrl.openConnection();
        putConn.setRequestMethod("PUT");
        putConn.setRequestProperty("Content-Type", "application/json");
        putConn.setDoOutput(true);

        // Tạo đối tượng answer với đúng các Audit Fields yêu cầu
        JSONObject answer = new JSONObject();
        answer.put("status", "ACTIVE"); // Phải là ACTIVE
        answer.put("activatedBy", studentCode); // Trùng với studentCode
        answer.put("auditNote", "manual-review-ok"); // Theo mẫu trong ảnh

        // Tạo Body JSON tổng thể
        JSONObject putBody = new JSONObject();
        putBody.put("studentCode", studentCode);
        putBody.put("qCode", qCode);
        putBody.put("answer", answer);

        // Gửi dữ liệu
        OutputStream os = putConn.getOutputStream();
        os.write(putBody.toString().getBytes("UTF-8"));
        os.flush();
        os.close();

        // --- 4. KIỂM TRA KẾT QUẢ ---
        int responseCode = putConn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK || responseCode == 201) {
            BufferedReader putIn = new BufferedReader(new InputStreamReader(putConn.getInputStream()));
            StringBuilder res2 = new StringBuilder();
            while ((line = putIn.readLine()) != null) res2.append(line);
            putIn.close();
            System.out.println("KẾT QUẢ: " + res2.toString());
        } else {
            System.out.println("Lỗi PUT: " + responseCode);
        }
    }
}

//Bạn cần xử lý bài toán cập nhật trạng thái tài khoản theo mô hình 2 phase và gửi đủ thông tin audit.
//Giao thức
//Bước 1 — Lấy dữ liệu (GET):
//GET /api/rest/method?studentCode=<mã_sv>&qCode=<qAlias trong đề>
//Bước 2 — Cập nhật (PUT):
//PUT /api/rest/method/{requestId}
//Body JSON:
//{
  //"studentCode": "B22DCCN001",
  //"qCode": "a7Lm2Pq9",
  //"answer": {
    //"status": "ACTIVE",
    //"activatedBy": "B22DCCN001",
    //"auditNote": "manual-review-ok"
  //}
//}
//Yêu cầu
//Phase 2 chỉ chấp nhận HTTP PUT.
//answer.status phải là ACTIVE.
//answer.activatedBy phải trùng với studentCode.