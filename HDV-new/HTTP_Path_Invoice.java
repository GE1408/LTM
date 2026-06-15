package restsumproject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONObject;

public class HTTP_Path_Invoice {

    public static void main(String[] args) throws Exception {
        // --- 1. THÔNG TIN CẤU HÌNH ---
        String studentCode = "B22DCVT090"; // Mã sinh viên của Hồ Anh Dũng
        String qCode = "ymlgLco3"; // Mã câu hỏi từ image_2ad89e.jpg
        String examIP = "36.50.135.242";
        
        // Thử cổng 2230 hoặc 8080 tùy theo cấu hình Server lúc thi
        String port = "2230"; 
        String baseUrl = "http://" + examIP + ":" + port + "/api/rest/path";

        // --- 2. BƯỚC 1: LẤY DANH SÁCH INVOICE (GET) ---
        String getListUrl = baseUrl + "?studentCode=" + studentCode + "&qCode=" + qCode;
        URL url1 = URI.create(getListUrl).toURL();
        HttpURLConnection conn1 = (HttpURLConnection) url1.openConnection();
        conn1.setRequestMethod("GET");
        conn1.setRequestProperty("Accept", "application/json");

        // Đọc dữ liệu từ Bước 1
        BufferedReader in1 = new BufferedReader(new InputStreamReader(conn1.getInputStream()));
        StringBuilder res1 = new StringBuilder();
        String line;
        while ((line = in1.readLine()) != null) res1.append(line);
        in1.close();
        System.out.println("Response Buoc 1: " + res1.toString());

        // Parse JSON để lấy requestId và ID của hóa đơn (Invoice)
        JSONObject json1 = new JSONObject(res1.toString());
        String requestId = json1.getString("requestId");
        JSONArray dataArray = json1.getJSONArray("data");
        
        // Chọn ID của hóa đơn đầu tiên trong danh sách
        int invoiceId = dataArray.getJSONObject(0).getInt("id");

        // --- 3. BƯỚC 2: TRUY VẤN THEO PATH + QUERY (GET) ---
        // Cấu trúc URL yêu cầu: /api/rest/path/{invoiceId}?studentCode=...&qCode=...&requestId=...&currency=USD
        String getDetailUrl = baseUrl + "/" + invoiceId
                + "?studentCode=" + studentCode
                + "&qCode=" + qCode
                + "&requestId=" + requestId
                + "&currency=USD";

        System.out.println("Dang goi Buoc 2: " + getDetailUrl);

        URL url2 = URI.create(getDetailUrl).toURL();
        HttpURLConnection conn2 = (HttpURLConnection) url2.openConnection();
        conn2.setRequestMethod("GET");

        // Đọc kết quả cuối cùng để xác nhận AC
        BufferedReader in2 = new BufferedReader(new InputStreamReader(conn2.getInputStream()));
        StringBuilder res2 = new StringBuilder();
        while ((line = in2.readLine()) != null) res2.append(line);
        in2.close();

        System.out.println("KET QUA CUOI CUNG: " + res2.toString());
    }
}
//Bạn cần chọn invoice hợp lệ từ phase 1 rồi gọi phase 2 với path param và query param đúng chuẩn.
//Giao thức
//Bước 1 — Lấy danh sách invoice (GET):
//GET /api/rest/path?studentCode=<mã_sv>&qCode=<qAlias trong đề>
//Bước 2 — Truy vấn theo path + query (GET):
//GET /api/rest/path/{invoiceId}?studentCode=<mã_sv>&qCode=<qAlias>&requestId=<requestId phase1>&currency=USD
//Ví dụ:
//GET /api/rest/path/2?studentCode=B22DCCN001&qCode=z3Np8Rk1&requestId=p4Ks7n2Q&currency=USD
//Yêu cầu
//invoiceId phải nằm trong danh sách phase 1.
//currency phải là USD.
//Endpoint phase 2 chỉ chấp nhận GET.