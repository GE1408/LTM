import java.io.*;
import java.net.*;
import java.util.*;

public class deadline {
    static class Job {
        String id;
        int start;
        int end;

        Job(String id, int start, int end) {
            this.id = id;
            this.start = start;
            this.end = end;
        }
    }

    public static void main(String[] args) throws Exception {
        String studentCode = "B22DCVT090";
        String qCode = "MsZVOOFu";
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

        int jobsStart = jsonStr.indexOf("\"jobs\":[") + 8;
        int jobsEnd = jsonStr.lastIndexOf("]");
        String jobsStr = jsonStr.substring(jobsStart, jobsEnd);

        List<Job> jobs = new ArrayList<>();
        int idx = 0;
        while ((idx = jobsStr.indexOf("{", idx)) != -1) {
            int endIdx = jobsStr.indexOf("}", idx);
            if (endIdx == -1) break;

            String jobObj = jobsStr.substring(idx, endIdx + 1);

            String idTag = "\"id\":\"";
            int nS = jobObj.indexOf(idTag) + idTag.length();
            int nE = jobObj.indexOf("\"", nS);
            String id = jobObj.substring(nS, nE);

            String startTag = "\"start\":";
            int sS = jobObj.indexOf(startTag) + startTag.length();
            int sE = sS;
            while (sE < jobObj.length() && Character.isDigit(jobObj.charAt(sE))) {
                sE++;
            }
            int start = Integer.parseInt(jobObj.substring(sS, sE).trim());

            String endTag = "\"end\":";
            int eS = jobObj.indexOf(endTag) + endTag.length();
            int eE = eS;
            while (eE < jobObj.length() && Character.isDigit(jobObj.charAt(eE))) {
                eE++;
            }
            int end = Integer.parseInt(jobObj.substring(eS, eE).trim());

            jobs.add(new Job(id, start, end));
            idx = endIdx + 1;
        }

        Collections.sort(jobs, new Comparator<Job>() {
            @Override
            public int compare(Job o1, Job o2) {
                if (o1.end != o2.end) {
                    return Integer.compare(o1.end, o2.end);
                }
                if (o1.start != o2.start) {
                    return Integer.compare(o1.start, o2.start);
                }
                return o1.id.compareTo(o2.id);
            }
        });

        StringBuilder answerBuilder = new StringBuilder();
        int lastEndTime = -1;
        boolean first = true;

        for (Job job : jobs) {
            if (job.start >= lastEndTime) {
                if (!first) {
                    answerBuilder.append(",");
                }
                answerBuilder.append(job.id);
                lastEndTime = job.end;
                first = false;
            }
        }
        String answer = answerBuilder.toString();

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
// b. Server trả về data là object gồm danh sách jobs.
// c. Chọn nhiều job không chồng lấp nhất bằng greedy theo thời điểm kết thúc.
// d. Gửi POST /api/rest/object/submit với body JSON gồm studentCode, qCode, requestId và answer.
// e. Ví dụ: nếu chọn được J1, J3, J5 thì answer là J1,J3,J5.