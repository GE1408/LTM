package restsumproject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONObject;

public class HTTP_Path_Parameter {

    public static void main(String[] args) throws Exception {
        // --- 1. THÔNG TIN CẤU HÌNH ---
        String studentCode = "B22DCVT090"; // Mã sinh viên của bạn
        String qCode = "qQdgPaej"; // Mã câu hỏi từ image_2b41fc.jpg
        String examIP = "36.50.135.242"; //
        String port = "2230"; // Thử port 2230 hoặc 8080 nếu bị lỗi 404
        String baseUrl = "http://" + examIP + ":" + port + "/api/rest/path";

        // --- 2. BƯỚC 1: LẤY DANH SÁCH SẢN PHẨM (GET) ---
        String getListUrl = baseUrl + "?studentCode=" + studentCode + "&qCode=" + qCode;
        URL url1 = URI.create(getListUrl).toURL();
        HttpURLConnection conn1 = (HttpURLConnection) url1.openConnection();
        conn1.setRequestMethod("GET");
        conn1.setRequestProperty("Accept", "application/json");

        // Đọc dữ liệu Bước 1
        BufferedReader in1 = new BufferedReader(new InputStreamReader(conn1.getInputStream()));
        StringBuilder res1 = new StringBuilder();
        String line;
        while ((line = in1.readLine()) != null) {
            res1.append(line);
        }
        in1.close();
        System.out.println("Response Buoc 1: " + res1.toString());

        // Parse JSON để lấy requestId và id sản phẩm đầu tiên
        JSONObject json1 = new JSONObject(res1.toString());
        String requestId = json1.getString("requestId");
        JSONArray dataArray = json1.getJSONArray("data");
        
        // Chọn id bất kỳ từ danh sách (ví dụ lấy phần tử đầu tiên)
        int productId = dataArray.getJSONObject(0).getInt("id");

        // --- 3. BƯỚC 2: TRUY VẤN SẢN PHẨM THEO ID (GET) ---
        // Cấu trúc URL yêu cầu: /api/rest/path/{productId}?studentCode=...&qCode=...&requestId=...&currency=USD
        String getDetailUrl = baseUrl + "/" + productId
                + "?studentCode=" + studentCode
                + "&qCode=" + qCode
                + "&requestId=" + requestId
                + "&currency=USD";

        System.out.println("Dang goi Buoc 2: " + getDetailUrl);

        URL url2 = URI.create(getDetailUrl).toURL();
        HttpURLConnection conn2 = (HttpURLConnection) url2.openConnection();
        conn2.setRequestMethod("GET");

        // Đọc kết quả cuối cùng
        BufferedReader in2 = new BufferedReader(new InputStreamReader(conn2.getInputStream()));
        StringBuilder res2 = new StringBuilder();
        while ((line = in2.readLine()) != null) {
            res2.append(line);
        }
        in2.close();

        System.out.println("KET QUA CUOI CUNG: " + res2.toString());
    }
}

//Bài tập này yêu cầu bạn sử dụng path parameter và query parameter để truy vấn một tài nguyên cụ thể trong danh sách sản phẩm.
//Giao thức
//Bước 1 — Lấy danh sách sản phẩm (GET):
//GET /api/rest/path?studentCode=<mã_sv>&qCode=<qAlias>
//Phản hồi:
//{
  //"requestId": "ghi01234",
  //"data": [
    //{"id": 1, "name": "Laptop", "priceVND": 15000000},
    //{"id": 2, "name": "Smartphone", "priceVND": 8500000},
    //{"id": 3, "name": "Tablet", "priceVND": 6200000}
  //]
//}
//Bước 2 — Truy vấn sản phẩm theo ID (GET):
//GET /api/rest/path/{productId}?studentCode=<mã_sv>&qCode=<qAlias>&requestId=ghi01234&currency=USD
//Ví dụ: truy vấn sản phẩm có id=2:
//GET /api/rest/path/2?studentCode=B22DCCN001&qCode=<qAlias>&requestId=ghi01234&currency=USD
//Phản hồi khi đúng:
//{"status": "AC", "message": "..."}
//Yêu cầu
//Chọn bất kỳ id hợp lệ từ danh sách Phase 1 rồi đưa vào path.
//Truyền requestId từ Phase 1 qua query parameter.
//Truyền currency=USD qua query parameter.
//Endpoint Phase 2 chỉ chấp nhận phương thức GET.