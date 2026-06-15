import java.io.*;
import java.net.*;
import java.util.*;

public class SLA {
    public static void main(String[] args) throws Exception {
        String studentCode = "B22DCVT090";
        String qCode = "5zygtnqq";
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

        String maxEtaTag = "\"maxEtaDays\":";
        int maxEtaStart = jsonStr.indexOf(maxEtaTag) + maxEtaTag.length();
        int maxEtaEnd = maxEtaStart;
        while (maxEtaEnd < jsonStr.length() && Character.isDigit(jsonStr.charAt(maxEtaEnd))) {
            maxEtaEnd++;
        }
        int maxEtaDays = Integer.parseInt(jsonStr.substring(maxEtaStart, maxEtaEnd).trim());

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

            if (etaDays <= maxEtaDays) {
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
                totalFee = Math.round(totalFee * 100.0) / 100.0;

                if (totalFee < minTotalFee) {
                    minTotalFee = totalFee;
                    bestReliability = reliability;
                    bestCarrier = carrier;
                    bestEtaDays = etaDays;
                } else if (Math.abs(totalFee - minTotalFee) < 0.0001) {
                    if (reliability > bestReliability) {
                        bestReliability = reliability;
                        bestCarrier = carrier;
                        bestEtaDays = etaDays;
                    }
                }
            }
            idx = endIdx + 1;
        }

        String totalFeeStr = String.format(Locale.US, "%.2f", minTotalFee);

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
                + "\"carrier\":\"" + bestCarrier + "\","
                + "\"totalFee\":" + totalFeeStr + ","
                + "\"etaDays\":" + bestEtaDays
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
// Một dịch vụ REST ObjectService được triển khai trên server tại URL http://<Exam_IP>:2230/api/rest/object để xử lý các bài toán với đối tượng JSON.
// Yêu cầu: Viết chương trình tại máy trạm (REST client) để giao tiếp với ObjectService và thực hiện các công việc sau.
// a. Gửi GET /api/rest/object?studentCode=<mã_sinh_viên>&qCode=<qAlias>. qCode là alias runtime được giao.
// b. Server trả về requestId và data gồm orderId, weightKg, maxEtaDays, quotes. Mỗi quote có carrier, baseFee, perKgFee, etaDays, reliability.
// c. Chỉ xét quote có etaDays <= maxEtaDays. Tính totalFee = baseFee + weightKg * perKgFee và làm tròn 2 chữ số thập phân.
// d. Chọn quote có totalFee nhỏ nhất; nếu bằng nhau, chọn quote có reliability cao hơn.
// e. Gửi POST /api/rest/object/submit với body JSON chứa studentCode, qCode, requestId và answer gồm carrier, totalFee, etaDays.