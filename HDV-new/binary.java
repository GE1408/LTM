import java.io.*;
import java.net.*;
import java.util.*;

public class binary {

    static class Record {
        String id;
        int threshold;

        Record(String id, int threshold) {
            this.id = id;
            this.threshold = threshold;
        }
    }

    public static void main(String[] args) throws Exception {

        String studentCode = "B22DCVT090";
        String qCode = "nes9Rogw";
        String baseUrl = "http://36.50.135.242:2230/api/rest/path";

        String getUrl =
                baseUrl
                + "?studentCode=" + studentCode
                + "&qCode=" + qCode;

        URL url = new URL(getUrl);

        HttpURLConnection conn =
                (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("GET");

        BufferedReader br =
                new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));

        StringBuilder sb = new StringBuilder();
        String line;

        while ((line = br.readLine()) != null) {
            sb.append(line);
        }

        br.close();

        String json = sb.toString();

        String requestId = "";

        String reqTag = "\"requestId\":\"";

        int reqStart =
                json.indexOf(reqTag) + reqTag.length();

        int reqEnd =
                json.indexOf("\"", reqStart);

        requestId =
                json.substring(reqStart, reqEnd);

        String targetTag = "\"target\":";

        int targetStart =
                json.indexOf(targetTag) + targetTag.length();

        int targetEnd = targetStart;

        while (targetEnd < json.length()
                && json.charAt(targetEnd) != ','
                && json.charAt(targetEnd) != '}') {
            targetEnd++;
        }

        int target =
                Integer.parseInt(
                        json.substring(targetStart, targetEnd)
                                .replace("\"", "")
                                .trim());

        int recordsStart =
                json.indexOf("\"records\":[")
                + "\"records\":[".length();

        int recordsEnd =
                json.indexOf("]", recordsStart);

        String recordsText =
                json.substring(recordsStart, recordsEnd);

        List<Record> records = new ArrayList<>();

        int pos = 0;

        while ((pos = recordsText.indexOf("{", pos)) != -1) {

            int endObj =
                    recordsText.indexOf("}", pos);

            String obj =
                    recordsText.substring(pos, endObj + 1);

            String idTag = "\"id\":\"";

            int idStart =
                    obj.indexOf(idTag) + idTag.length();

            int idEnd =
                    obj.indexOf("\"", idStart);

            String id =
                    obj.substring(idStart, idEnd);

            String thresholdTag =
                    "\"threshold\":";

            int thStart =
                    obj.indexOf(thresholdTag)
                    + thresholdTag.length();

            int thEnd = thStart;

            while (thEnd < obj.length()
                    && obj.charAt(thEnd) != ','
                    && obj.charAt(thEnd) != '}') {
                thEnd++;
            }

            int threshold =
                    Integer.parseInt(
                            obj.substring(thStart, thEnd).trim());

            records.add(
                    new Record(id, threshold));

            pos = endObj + 1;
        }

        int left = 0;
        int right = records.size() - 1;
        int answerIndex = -1;

        while (left <= right) {

            int mid = (left + right) / 2;

            if (records.get(mid).threshold >= target) {
                answerIndex = mid;
                right = mid - 1;
            } else {
                left = mid + 1;
            }
        }

        String answer = "";

        if (answerIndex != -1) {
            answer = records.get(answerIndex).id;
        }

        String submitUrl =
                baseUrl + "/submit"
                + "?studentCode="
                + URLEncoder.encode(studentCode, "UTF-8")
                + "&qCode="
                + URLEncoder.encode(qCode, "UTF-8")
                + "&requestId="
                + URLEncoder.encode(requestId, "UTF-8")
                + "&answer="
                + URLEncoder.encode(answer, "UTF-8");

        URL submitURL =
                new URL(submitUrl);

        HttpURLConnection submitConn =
                (HttpURLConnection) submitURL.openConnection();

        submitConn.setRequestMethod("GET");

        int responseCode =
                submitConn.getResponseCode();

        System.out.println("STATUS = " + responseCode);

        BufferedReader resultReader;

        if (responseCode >= 200 && responseCode < 300) {
            resultReader =
                    new BufferedReader(
                            new InputStreamReader(
                                    submitConn.getInputStream()));
        } else {
            resultReader =
                    new BufferedReader(
                            new InputStreamReader(
                                    submitConn.getErrorStream()));
        }

        StringBuilder result =
                new StringBuilder();

        while ((line = resultReader.readLine()) != null) {
            result.append(line);
        }

        resultReader.close();

        System.out.println(result.toString());
    }
}
// Một dịch vụ REST được triển khai trên server tại URL http://<Exam_IP>:2230/api/rest/path để xử lý các bài toán lựa chọn bản ghi và tìm đường đi. Yêu cầu: Viết chương trình tại máy trạm (REST client) để giao tiếp với dịch vụ và thực hiện các công việc sau.
// a. Gửi GET /api/rest/path?studentCode=<mã_sinh_viên>&qCode=<qAlias> để nhận JSON gồm requestId và data.
// b. Server trả về data là object gồm records và target.
// c. Dùng binary search trên danh sách đã sắp xếp để tìm bản ghi đầu tiên có giá trị không nhỏ hơn target.
// d. Gửi POST /api/rest/path/submit với body JSON gồm studentCode, qCode, requestId và answer.
// e. Ví dụ: nếu chọn bản ghi R3 thì answer là R3.