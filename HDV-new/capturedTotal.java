/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package restsumproject;

import java.io.*;
import java.net.*;
import java.util.*;

public class capturedTotal {
    public static void main(String[] args) throws Exception {
        String studentCode = "B22DCVT090";
        String qCode = "qi8gzSyA";
        String baseUrl = "http://36.50.135.242:2230/api/rest/data";

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

        double capturedTotal = 0;
        double refundedTotal = 0;
        int failedCount = 0;

        int idx = 0;
        while ((idx = jsonStr.indexOf("{", idx)) != -1) {
            int endIdx = jsonStr.indexOf("}", idx);
            if (endIdx == -1) break;
            
            String objStr = jsonStr.substring(idx, endIdx + 1);
            if (objStr.contains("\"amount\"") && objStr.contains("\"status\"")) {
                String statusTag = "\"status\":\"";
                int sS = objStr.indexOf(statusTag) + statusTag.length();
                int sE = objStr.indexOf("\"", sS);
                String status = objStr.substring(sS, sE);

                String amountTag = "\"amount\":";
                int aS = objStr.indexOf(amountTag) + amountTag.length();
                int aE = aS;
                while (aE < objStr.length() && (Character.isDigit(objStr.charAt(aE)) || objStr.charAt(aE) == '.' || objStr.charAt(aE) == '-')) {
                    aE++;
                }
                double amount = Double.parseDouble(objStr.substring(aS, aE).trim());

                if (status.equals("CAPTURED")) {
                    capturedTotal += amount;
                } else if (status.equals("REFUNDED")) {
                    refundedTotal += amount;
                } else if (status.equals("FAILED")) {
                    failedCount++;
                }
            }
            idx = endIdx + 1;
        }

        double netTotal = capturedTotal - refundedTotal;

        String capStr = String.format(Locale.US, "%.2f", capturedTotal);
        String refStr = String.format(Locale.US, "%.2f", refundedTotal);
        String netStr = String.format(Locale.US, "%.2f", netTotal);

        URL postUrl = new URL(baseUrl + "/submit");
        HttpURLConnection postConn = (HttpURLConnection) postUrl.openConnection();
        postConn.setRequestMethod("POST");
        postConn.setRequestProperty("Content-Type", "application/json");
        postConn.setDoOutput(true);

        String jsonPost = "{"
                + "\"studentCode\":\"" + studentCode + "\","
                + "\"qCode\":\"" + qCode + "\","
                + "\"requestId\":\"" + requestId + "\","
                + "\"answer\":{"
                + "\"capturedTotal\":" + capStr + ","
                + "\"refundedTotal\":" + refStr + ","
                + "\"netTotal\":" + netStr + ","
                + "\"failedCount\":" + failedCount
                + "}"
                + "}";

        OutputStream os = postConn.getOutputStream();
        os.write(jsonPost.getBytes("UTF-8"));
        os.flush();
        os.close();

        int responseCode = postConn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader postIn = new BufferedReader(new InputStreamReader(postConn.getInputStream()));
            String postLine;
            StringBuilder postResponse = new StringBuilder();
            while ((postLine = postIn.readLine()) != null) {
                postResponse.append(postLine);
            }
            postIn.close();
            System.out.println(postResponse.toString());
        }
    }
}
// Một dịch vụ REST DataService được triển khai trên server tại URL http://<Exam_IP>:2230/api/rest/data để xử lý các bài toán với dữ liệu nguyên thủy qua HTTP/JSON.
// Yêu cầu: Viết chương trình tại máy trạm (REST client) để giao tiếp với DataService và thực hiện các công việc sau.
// a. Gửi GET /api/rest/data?studentCode=<mã_sinh_viên>&qCode=<qAlias>. Tham số qCode là alias runtime được giao trong hệ thống.
// b. Server trả về requestId và data là mảng giao dịch, mỗi giao dịch có transactionId, amount, currency, status.
// c. Tính capturedTotal là tổng tiền giao dịch CAPTURED, refundedTotal là tổng tiền giao dịch REFUNDED, netTotal = capturedTotal - refundedTotal, và failedCount là số giao dịch FAILED. Các giá trị tiền làm tròn 2 chữ số thập phân.
// d. Gửi POST /api/rest/data/submit với body JSON chứa studentCode, qCode, requestId và answer là object gồm capturedTotal, refundedTotal, netTotal, failedCount.
// e. Ví dụ answer hợp lệ: {"capturedTotal":120.50,"refundedTotal":20.00,"netTotal":100.50,"failedCount":1}.