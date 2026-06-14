import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class IfMatch {

    static final String STUDENT_CODE = "B22DCVT090";
    static final String QCODE = "vt3rP0je";

    static String getJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\"";
        int start = json.indexOf(searchKey);
        if (start == -1) return null;
        start = json.indexOf(":", start);
        if (start == -1) return null;
        start++; 
        
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) {
            start++;
        }
        
        if (json.charAt(start) == '"') {
            start++;
            int end = start;
            while (end < json.length()) {
                if (json.charAt(end) == '"' && json.charAt(end - 1) != '\\') break;
                end++;
            }
            return json.substring(start, end).replace("\\\"", "\"");
        } else {
            int end = start;
            while (end < json.length() && json.charAt(end) != ',' && json.charAt(end) != '}' && !Character.isWhitespace(json.charAt(end))) {
                end++;
            }
            return json.substring(start, end).trim();
        }
    }

    public static void main(String[] args) throws Exception {
        String examIP = "36.50.135.242";
        String baseUrl = "http://" + examIP + ":2230/api/rest/method";

        HttpClient client = HttpClient.newHttpClient();

        String getUrlStr = baseUrl + "?studentCode=" + STUDENT_CODE + "&qCode=" + QCODE;
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(getUrlStr))
                .GET()
                .build();

        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        String jsonStr = getResponse.body();
        
        if (getResponse.statusCode() != 200) {
            System.out.println(jsonStr);
            return;
        }

        String requestId = getJsonValue(jsonStr, "requestId");
        String etag = getJsonValue(jsonStr, "etag");

        String patchUrlStr = baseUrl + "/" + requestId;
        
        String jsonPatch = "{"
                + "\"studentCode\":\"" + STUDENT_CODE + "\","
                + "\"qCode\":\"" + QCODE + "\","
                + "\"answer\":{\"status\":\"RESOLVED\"}"
                + "}";

        HttpRequest.Builder patchBuilder = HttpRequest.newBuilder()
                .uri(URI.create(patchUrlStr))
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(jsonPatch));

        if (etag != null) {
            patchBuilder.header("If-Match", etag);
        }

        HttpRequest patchRequest = patchBuilder.build();
        HttpResponse<String> patchResponse = client.send(patchRequest, HttpResponse.BodyHandlers.ofString());

        System.out.println(patchResponse.body());
    }
}
// Một dịch vụ REST MethodService được triển khai trên server tại URL http://<Exam_IP>:2230/api/rest/method để kiểm tra cách sử dụng HTTP method trong quy trình submit.
// Yêu cầu: Viết chương trình tại máy trạm (REST client) để giao tiếp với MethodService và thực hiện các công việc sau.
// a. Gửi GET /api/rest/method?studentCode=<mã_sinh_viên>&qCode=<qAlias> để nhận ticket cần xử lý. qCode là alias runtime được giao.
// b. Server trả về requestId và data gồm ticketId, status, targetStatus, version, etag.
// c. Gửi phase 2 bằng phương thức PATCH tới /api/rest/method/{requestId} và kèm header If-Match đúng bằng etag nhận được.
// d. Body JSON chứa studentCode, qCode và answer; trong đó answer.status bằng RESOLVED.
// e. Thiếu header If-Match, sai etag, hoặc dùng sai phương thức sẽ không đạt.