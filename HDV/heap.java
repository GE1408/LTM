import java.io.*;
import java.net.*;
import java.util.*;

public class heap {

    static class Record implements Comparable<Record> {

        String id;
        int value;

        Record(String id, int value) {
            this.id = id;
            this.value = value;
        }

        @Override
        public int compareTo(Record o) {
            return Integer.compare(o.value, this.value);
        }
    }

    public static void main(String[] args) throws Exception {

        String studentCode = "B22DCVT090";
        String qCode = "NLk41BW2";
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

        System.out.println(json);

        String requestId = "";

        String reqTag = "\"requestId\":\"";

        if (json.contains(reqTag)) {

            int reqStart =
                    json.indexOf(reqTag)
                    + reqTag.length();

            int reqEnd =
                    json.indexOf("\"", reqStart);

            requestId =
                    json.substring(reqStart, reqEnd);
        }

        String targetType = "";

        String typeTag = "\"type\":\"";

        int lastType =
                json.lastIndexOf(typeTag);

        if (lastType != -1) {

            int typeStart =
                    lastType + typeTag.length();

            int typeEnd =
                    json.indexOf("\"", typeStart);

            targetType =
                    json.substring(typeStart, typeEnd);
        }

        int k = 1;

        String kTag = "\"k\":";

        if (json.contains(kTag)) {

            int kStart =
                    json.indexOf(kTag)
                    + kTag.length();

            int kEnd = kStart;

            while (kEnd < json.length()
                    && Character.isDigit(json.charAt(kEnd))) {
                kEnd++;
            }

            k =
                    Integer.parseInt(
                            json.substring(kStart, kEnd));
        }

        int recordsStart =
                json.indexOf("\"records\":[")
                + "\"records\":[".length();

        int recordsEnd =
                json.indexOf("]", recordsStart);

        String recordsText =
                json.substring(recordsStart, recordsEnd);

        PriorityQueue<Record> heap =
                new PriorityQueue<>();

        int pos = 0;

        while ((pos = recordsText.indexOf("{", pos)) != -1) {

            int endObj =
                    recordsText.indexOf("}", pos);

            String obj =
                    recordsText.substring(pos, endObj + 1);

            String idField = "\"id\":\"";

            int idStart =
                    obj.indexOf(idField)
                    + idField.length();

            int idEnd =
                    obj.indexOf("\"", idStart);

            String id =
                    obj.substring(idStart, idEnd);

            String valueField = "\"value\":";

            int valueStart =
                    obj.indexOf(valueField)
                    + valueField.length();

            int valueEnd = valueStart;

            while (valueEnd < obj.length()
                    && Character.isDigit(obj.charAt(valueEnd))) {
                valueEnd++;
            }

            int value =
                    Integer.parseInt(
                            obj.substring(valueStart, valueEnd));

            String recTypeField = "\"type\":\"";

            int recTypeStart =
                    obj.indexOf(recTypeField)
                    + recTypeField.length();

            int recTypeEnd =
                    obj.indexOf("\"", recTypeStart);

            String recType =
                    obj.substring(recTypeStart, recTypeEnd);

            if (recType.equals(targetType)) {
                heap.add(new Record(id, value));
            }

            pos = endObj + 1;
        }

        Record answerRecord = null;

        for (int i = 0; i < k && !heap.isEmpty(); i++) {
            answerRecord = heap.poll();
        }

        String answer = "";

        if (answerRecord != null) {
            answer =
                    answerRecord.id
                    + "|"
                    + answerRecord.value;
        }

        System.out.println("ANSWER = " + answer);

        String submitUrl =
                baseUrl
                + "/submit"
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

        BufferedReader resultReader =
                new BufferedReader(
                        new InputStreamReader(
                                submitConn.getInputStream()));

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
// b. Server trả về data là object gồm records, k và type.
// c. Lọc theo type, rồi chọn phần tử thứ k theo giá trị lớn nhất bằng heap.
// d. Gửi POST /api/rest/path/submit với body JSON gồm studentCode, qCode, requestId và answer.
// e. Ví dụ: nếu phần tử được chọn là K5 với giá trị 88 thì answer là K5|88.