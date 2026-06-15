import java.io.*;
import java.net.*;
import java.util.*;

public class SLA2 {
    public static void main(String[] args) throws Exception {
        String studentCode = "B22DCVT090";
        String qCode = "6l1nOGX6";
        String baseUrl = "http://36.50.135.242:2230/api/rest/object";

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

        String wTag = "\"weightKg\":";
        int wStart = jsonStr.indexOf(wTag) + wTag.length();
        int wEnd = wStart;
        while (wEnd < jsonStr.length() && (Character.isDigit(jsonStr.charAt(wEnd)) || jsonStr.charAt(wEnd) == '.')) {
            wEnd++;
        }
        double weightKg = Double.parseDouble(jsonStr.substring(wStart, wEnd).trim());

        int quotesStart = jsonStr.indexOf("\"quotes\":[") + 10;
        int quotesEnd = jsonStr.lastIndexOf("]");
        String quotesStr = jsonStr.substring(quotesStart, quotesEnd);

        String bestCarrier = "";
        double minTotalFee = Double.MAX_VALUE;
        double bestReliability = -1.0;
        int bestEtaDays = 0;

        int idx = 0;
        while ((idx = quotesStr.indexOf("{", idx)) != -1) {
            int endIdx = quotesStr.indexOf("}", idx);
            if (endIdx == -1) break;

            String quoteObj = quotesStr.substring(idx, endIdx + 1);

            String etaTag = "\"etaDays\":";
            int etaS = quoteObj.indexOf(etaTag) + etaTag.length();
            int etaE = etaS;
            while (etaE < quoteObj.length() && Character.isDigit(quoteObj.charAt(etaE))) {
                etaE++;
            }
            int etaDays = Integer.parseInt(quoteObj.substring(etaS, etaE).trim());

            String carrierTag = "\"carrier\":\"";
            int cS = quoteObj.indexOf(carrierTag) + carrierTag.length();
            int cE = quoteObj.indexOf("\"", cS);
            String carrier = quoteObj.substring(cS, cE);

            String baseFeeTag = "\"baseFee\":";
            int bS = quoteObj.indexOf(baseFeeTag) + baseFeeTag.length();
            int bE = bS;
            while (bE < quoteObj.length() && (Character.isDigit(quoteObj.charAt(bE)) || quoteObj.charAt(bE) == '.')) {
                bE++;
            }
            double baseFee = Double.parseDouble(quoteObj.substring(bS, bE).trim());

            String perKgFeeTag = "\"perKgFee\":";
            int pS = quoteObj.indexOf(perKgFeeTag) + perKgFeeTag.length();
            int pE = pS;
            while (pE < quoteObj.length() && (Character.isDigit(quoteObj.charAt(pE)) || quoteObj.charAt(pE) == '.')) {
                pE++;
            }
            double perKgFee = Double.parseDouble(quoteObj.substring(pS, pE).trim());

            String relTag = "\"reliability\":";
            int rS = quoteObj.indexOf(relTag) + relTag.length();
            int rE = rS;
            while (rE < quoteObj.length() && (Character.isDigit(quoteObj.charAt(rE)) || quoteObj.charAt(rE) == '.')) {
                rE++;
            }
            double reliability = Double.parseDouble(quoteObj.substring(rS, rE).trim());

            double totalFee = baseFee + weightKg * perKgFee;
            totalFee = Double.parseDouble(String.format(Locale.US, "%.2f", totalFee));

            if (totalFee < minTotalFee) {
                minTotalFee = totalFee;
                bestReliability = reliability;
                bestCarrier = carrier;
                bestEtaDays = etaDays;
            } else if (Math.abs(totalFee - minTotalFee) < 0.005) {
                if (reliability > bestReliability) {
                    bestReliability = reliability;
                    bestCarrier = carrier;
                    bestEtaDays = etaDays;
                }
            }
            
            idx = endIdx + 1;
        }

        String totalFeeStr = String.format(Locale.US, "%.2f", minTotalFee);
        String answer = bestCarrier + "|" + totalFeeStr + "|" + bestEtaDays;

        URL postUrl = new URL(baseUrl + "/submit");
        HttpURLConnection postConn = (HttpURLConnection) postUrl.openConnection();
        postConn.setRequestMethod("POST");
        postConn.setRequestProperty("Content-Type", "application/json");
        postConn.setDoOutput(true);

        String jsonPost = "{"
                + "\"studentCode\":\"" + studentCode + "\","
                + "\"qCode\":\"" + qCode + "\","
                + "\"requestId\":\"" + requestId + "\","
                + "\"answer\":\"" + answer + "\""
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
// Một dịch vụ REST được triển khai trên server tại URL http://<Exam_IP>:2230/api/rest/object để xử lý các bài toán với đối tượng. Yêu cầu: Viết chương trình tại máy trạm (REST client) để giao tiếp với dịch vụ và thực hiện các công việc sau.
// a. Gửi GET /api/rest/object?studentCode=<mã_sinh_viên>&qCode=<qAlias> để nhận JSON gồm requestId và data.
// b. Server trả về data là object gồm orderId, weightKg, maxEtaDays và quotes.
// c. Chỉ xét quote có etaDays <= maxEtaDays, tính phí và chọn quote rẻ nhất; nếu hòa thì chọn reliability cao hơn.
// d. Gửi POST /api/rest/object/submit với body JSON gồm studentCode, qCode, requestId và answer.
// e. Ví dụ: nếu quote tốt nhất là C2 với phí 12.50 và etaDays=3 thì answer là C2|12.50|3.