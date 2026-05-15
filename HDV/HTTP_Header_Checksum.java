package restsumproject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import org.json.JSONObject;

public class HTTP_Header_Checksum {

    public static void main(String[] args) throws Exception {
        // --- 1. THÔNG TIN CẤU HÌNH ---
        String studentCode = "B22DCVT090"; // Mã sinh viên của bạn
        String qCode = "rLEwW47Q"; // Mã câu hỏi từ image_2b4db6.jpg
        String examIP = "36.50.135.242"; //
        String port = "2230";
        String baseUrl = "http://" + examIP + ":" + port + "/api/rest/header";

        // --- 2. PHASE 1: GỬI GET VÀ ĐỌC HEADER X-CHECKSUM ---
        String getUrlStr = baseUrl + "?studentCode=" + studentCode + "&qCode=" + qCode;
        URL getUrl = URI.create(getUrlStr).toURL();
        HttpURLConnection getConn = (HttpURLConnection) getUrl.openConnection();
        getConn.setRequestMethod("GET");

        // Đọc giá trị X-Checksum từ Header phản hồi
        String xChecksum = getConn.getHeaderField("X-Checksum");
        System.out.println("X-Checksum nhan duoc: " + xChecksum);

        // Đọc Body để lấy requestId
        BufferedReader in = new BufferedReader(new InputStreamReader(getConn.getInputStream()));
        StringBuilder res1 = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            res1.append(line);
        }
        in.close();
        
        JSONObject jsonRes = new JSONObject(res1.toString());
        String requestId = jsonRes.getString("requestId");

        // --- 3. PHASE 2: GỬI POST KÈM HEADER X-CHECKSUM ---
        String postUrlStr = baseUrl + "/submit";
        URL postUrl = URI.create(postUrlStr).toURL();
        HttpURLConnection postConn = (HttpURLConnection) postUrl.openConnection();
        postConn.setRequestMethod("POST");
        postConn.setRequestProperty("Content-Type", "application/json");
        
        // Gửi lại giá trị X-Checksum vào Request Header theo yêu cầu
        postConn.setRequestProperty("X-Checksum", xChecksum);
        postConn.setDoOutput(true);

        // Tạo Body JSON
        JSONObject postBody = new JSONObject();
        postBody.put("studentCode", studentCode);
        postBody.put("qCode", qCode);
        postBody.put("requestId", requestId);

        // Gửi dữ liệu
        OutputStream os = postConn.getOutputStream();
        os.write(postBody.toString().getBytes("UTF-8"));
        os.flush();
        os.close();

        // --- 4. KIỂM TRA KẾT QUẢ ---
        int responseCode = postConn.getResponseCode();
        System.out.println("POST Response Code: " + responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK || responseCode == 201) {
            BufferedReader postIn = new BufferedReader(new InputStreamReader(postConn.getInputStream()));
            StringBuilder res2 = new StringBuilder();
            while ((line = postIn.readLine()) != null) {
                res2.append(line);
            }
            postIn.close();
            System.out.println("KET QUA CUOI CUNG: " + res2.toString());
        } else {
            System.out.println("Loi: Server phan hoi ma " + responseCode);
        }
    }
}

//Bài tập này yêu cầu bạn đọc một custom HTTP response header (X-Checksum) từ phản hồi Phase 1 và gửi lại giá trị đó trong request header ở Phase 2.
//Giao thức
//Bước 1 — Lấy dữ liệu (GET):
//GET /api/rest/header?studentCode=<mã_sv>&qCode=<qAlias>
//Phản hồi (body):
//{
  //"requestId": "def56789",
  //"data": [3421, 7890, 1234, 5678, 9012, 3456]
//}
//Phản hồi (header):
//X-Checksum: a3f2c1...  (SHA-256 của danh sách số)
//Bước 2 — Gửi đáp án (POST):
//POST /api/rest/header/submit
//Body JSON:
//{
  //"studentCode": "<mã_sv>",
  //"qCode": "<qAlias>",
  //"requestId": "def56789"
//}
//Request header bắt buộc:
//X-Checksum: a3f2c1...  (giá trị đọc từ Phase 1)
//Phản hồi khi đúng:
//{"status": "AC", "message": "..."}
//Yêu cầu
//Đọc giá trị header X-Checksum từ phản hồi Phase 1.
//Gửi lại đúng giá trị đó trong request header X-Checksum ở Phase 2.
//Không cần tính SHA-256 thủ công — chỉ cần truyền lại giá trị đã nhận.