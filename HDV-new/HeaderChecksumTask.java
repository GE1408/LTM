package restsumproject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

public class HeaderChecksumTask {

    public static void main(String[] args) throws Exception {
        // THÔNG SỐ CHUẨN TỪ ĐỀ BÀI
        String serverIp = "36.50.135.242"; // Lấy từ đề bài
        int serverPort = 8080;             // <<< ĐỔI PORT NẾU CẦN (kiểm tra trên đề bài)
        String studentCode = "B22DCVT090"; // Mã SV đầy đủ của Dũng
        String qCode = "sUc0oprf";        // Mã câu hỏi (lấy từ đề bài)
        String requestId = "x2Q8p4Lm";

        HttpClient client = HttpClient.newHttpClient();

        // --- PHASE 1: GET ĐỂ LẤY X-CHECKSUM ---
        String baseUrl = "http://" + serverIp + ":" + serverPort;
        String getUrl = baseUrl + "/api/rest/header?studentCode=" + studentCode + "&qCode=" + qCode;
        HttpRequest getRequest = HttpRequest.newBuilder().uri(URI.create(getUrl)).GET().build();
        
        HttpResponse<String> getResponse = client.send(getRequest, BodyHandlers.ofString());

        // ==== DEBUG: In ra toàn bộ response để kiểm tra ====
        System.out.println("=== GET Response ===");
        System.out.println("URL: " + getUrl);
        System.out.println("Status Code: " + getResponse.statusCode());
        System.out.println("Tất cả Headers:");
        getResponse.headers().map().forEach((k, v) -> System.out.println("  " + k + ": " + v));
        System.out.println("Body: " + getResponse.body());
        System.out.println("====================");

        // Lấy X-Checksum từ header (thử cả chữ thường)
        String xChecksum = getResponse.headers()
                .firstValue("X-Checksum")
                .or(() -> getResponse.headers().firstValue("x-checksum"))
                .orElseThrow(() -> new RuntimeException("Lỗi: Server " + serverIp + " không trả về Checksum. " 
                        + "Hãy kiểm tra xem bạn có đang dùng Wi-Fi trường không!"));

        System.out.println("Lấy thành công Checksum: " + xChecksum);

        // --- PHASE 2: POST ĐỂ SUBMIT ---
        String postUrl = baseUrl + "/api/rest/header/submit";
        String jsonBody = """
                {
                    "studentCode": "%s",
                    "qCode": "%s",
                    "requestId": "%s"
                }
                """.formatted(studentCode, qCode, requestId);

        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(postUrl))
                .header("Content-Type", "application/json")
                .header("X-Checksum", xChecksum) // Quan trọng nhất là dòng này
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> postResponse = client.send(postRequest, BodyHandlers.ofString());

        System.out.println("Status Code: " + postResponse.statusCode());
        System.out.println("Kết quả cuối cùng: " + postResponse.body());
    }
}