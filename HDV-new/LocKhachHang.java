import java.io.*;
import java.net.*;
import java.util.*;

public class LocKhachHang {
    public static void main(String[] args) throws Exception {
        String studentCode = "B22DCVT090";
        String qCode = "ZZCIPbND";
        String baseUrl = "http://36.50.135.242:2230/api/rest/path";

        String getUrlStr = baseUrl + "?studentCode=" + studentCode + "&qCode=" + qCode;
        URL getUrl = new URL(getUrlStr);
        HttpURLConnection getConn = (HttpURLConnection) getUrl.openConnection();
        getConn.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(getConn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        String jsonStr = response.toString();

        String requestId = "";
        if (jsonStr.contains("\"requestId\":\"")) {
            String reqIdTag = "\"requestId\":\"";
            int reqIdStart = jsonStr.indexOf(reqIdTag) + reqIdTag.length();
            int reqIdEnd = jsonStr.indexOf("\"", reqIdStart);
            requestId = jsonStr.substring(reqIdStart, reqIdEnd);
        }

        String bestCustomerId = "";
        double maxOverdueAmount = -1.0;
        int bestPage = 0;
        String bestStatus = "OVERDUE";

        int idx = 0;
        while ((idx = jsonStr.indexOf("{", idx)) != -1) {
            int endIdx = jsonStr.indexOf("}", idx);
            if (endIdx == -1) break;

            String objStr = jsonStr.substring(idx, endIdx + 1);
            if (objStr.contains("\"status\":\"OVERDUE\"")) {
                String idTag = "\"customerId\":\"";
                int idS = objStr.indexOf(idTag) + idTag.length();
                int idE = objStr.indexOf("\"", idS);
                String customerId = objStr.substring(idS, idE);

                String amountTag = "\"overdueAmount\":";
                int aS = objStr.indexOf(amountTag) + amountTag.length();
                int aE = aS;
                while (aE < objStr.length() && (Character.isDigit(objStr.charAt(aE)) || objStr.charAt(aE) == '.')) {
                    aE++;
                }
                double overdueAmount = Double.parseDouble(objStr.substring(aS, aE).trim());

                String pageTag = "\"page\":";
                int pS = objStr.indexOf(pageTag) + pageTag.length();
                int pE = pS;
                while (pE < objStr.length() && Character.isDigit(objStr.charAt(pE))) {
                    pE++;
                }
                int page = Integer.parseInt(objStr.substring(pS, pE).trim());

                if (overdueAmount > maxOverdueAmount) {
                    maxOverdueAmount = overdueAmount;
                    bestCustomerId = customerId;
                    bestPage = page;
                }
            }
            idx = endIdx + 1;
        }

        String submitUrlStr = baseUrl + "/" + bestCustomerId 
                + "?studentCode=" + studentCode 
                + "&qCode=" + qCode 
                + "&requestId=" + requestId 
                + "&status=" + bestStatus 
                + "&page=" + bestPage;

        URL submitUrl = new URL(submitUrlStr);
        HttpURLConnection submitConn = (HttpURLConnection) submitUrl.openConnection();
        submitConn.setRequestMethod("GET");

        int responseCode = submitConn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader submitIn = new BufferedReader(new InputStreamReader(submitConn.getInputStream()));
            String submitLine;
            StringBuilder submitResponse = new StringBuilder();
            while ((submitLine = submitIn.readLine()) != null) {
                submitResponse.append(submitLine);
            }
            submitIn.close();
            System.out.println(submitResponse.toString());
        }
    }
}
// Một dịch vụ REST PathService được triển khai trên server tại URL http://<Exam_IP>:2230/api/rest/path để kiểm tra cách sử dụng path parameter và query parameter.
// Yêu cầu: Viết chương trình tại máy trạm (REST client) để giao tiếp với PathService và thực hiện các công việc sau.
// a. Gửi GET /api/rest/path?studentCode=<mã_sinh_viên>&qCode=<qAlias>. qCode là alias runtime được giao.
// b. Server trả về requestId và data là danh sách khách hàng, mỗi phần tử có customerId, status, overdueAmount, page.
// c. Chỉ xét khách hàng có status bằng OVERDUE, chọn khách hàng có overdueAmount lớn nhất.
// d. Gửi GET /api/rest/path/{customerId}?studentCode=<mã_sinh_viên>&qCode=<qAlias>&requestId=<requestId>&status=OVERDUE&page=<page>.
// e. customerId, status và page phải khớp khách hàng đã chọn.