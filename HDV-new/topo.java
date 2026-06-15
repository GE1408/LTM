import java.io.*;
import java.net.*;
import java.util.*;

public class topo {
    public static void main(String[] args) throws Exception {
        String studentCode = "B22DCVT090";
        String qCode = "ELU2206L";
        String baseUrl = "http://36.50.135.242:2230/api/rest/method";

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

        int tasksStart = jsonStr.indexOf("\"tasks\":[") + 9;
        int tasksEnd = jsonStr.indexOf("]", tasksStart);
        String tasksStr = jsonStr.substring(tasksStart, tasksEnd);
        String[] taskTokens = tasksStr.split(",");
        List<String> tasks = new ArrayList<>();
        for (String t : taskTokens) {
            String clean = t.replace("\"", "").trim();
            if (!clean.isEmpty()) {
                tasks.add(clean);
            }
        }

        int depsStart = jsonStr.indexOf("\"deps\":[") + 8;
        int depsEnd = jsonStr.lastIndexOf("]");
        String depsStr = jsonStr.substring(depsStart, depsEnd);

        Map<String, List<String>> adj = new HashMap<>();
        Map<String, Integer> inDegree = new HashMap<>();
        for (String task : tasks) {
            adj.put(task, new ArrayList<>());
            inDegree.put(task, 0);
        }

        int idx = 0;
        while ((idx = depsStr.indexOf("{", idx)) != -1) {
            int endIdx = depsStr.indexOf("}", idx);
            if (endIdx == -1) break;

            String depObj = depsStr.substring(idx, endIdx + 1);

            String fromTag = "\"from\":\"";
            int fS = depObj.indexOf(fromTag) + fromTag.length();
            int fE = depObj.indexOf("\"", fS);
            String from = depObj.substring(fS, fE);

            String toTag = "\"to\":\"";
            int tS = depObj.indexOf(toTag) + toTag.length();
            int tE = depObj.indexOf("\"", tS);
            String to = depObj.substring(tS, tE);

            if (adj.containsKey(from) && adj.containsKey(to)) {
                adj.get(from).add(to);
                inDegree.put(to, inDegree.get(to) + 1);
            }

            idx = endIdx + 1;
        }

        PriorityQueue<String> pq = new PriorityQueue<>();
        for (String task : tasks) {
            if (inDegree.get(task) == 0) {
                pq.add(task);
            }
        }

        List<String> topoOrder = new ArrayList<>();
        while (!pq.isEmpty()) {
            String u = pq.poll();
            topoOrder.add(u);

            for (String v : adj.get(u)) {
                inDegree.put(v, inDegree.get(v) - 1);
                if (inDegree.get(v) == 0) {
                    pq.add(v);
                }
            }
        }

        StringBuilder answerBuilder = new StringBuilder();
        for (int i = 0; i < topoOrder.size(); i++) {
            answerBuilder.append(topoOrder.get(i));
            if (i < topoOrder.size() - 1) {
                answerBuilder.append(",");
            }
        }
        String answer = answerBuilder.toString();

        URL postUrl = new URL(baseUrl + "/" + requestId);
        HttpURLConnection postConn = (HttpURLConnection) postUrl.openConnection();
        postConn.setRequestMethod("PUT");
        postConn.setRequestProperty("Content-Type", "application/json");
        postConn.setDoOutput(true);

        String jsonPost = "{"
                + "\"studentCode\":\"" + studentCode + "\","
                + "\"qCode\":\"" + qCode + "\","
                + "\"answer\":\"" + answer + "\""
                + "}";

        OutputStream os = postConn.getOutputStream();
        os.write(jsonPost.getBytes("UTF-8"));
        os.flush();
        os.close();

        int responseCode = postConn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
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
// Một dịch vụ REST được triển khai trên server tại URL http://<Exam_IP>:2230/api/rest/method để xử lý các bài toán mô phỏng phương thức xử lý, trạng thái và quan hệ phụ thuộc. Yêu cầu: Viết chương trình tại máy trạm (REST client) để giao tiếp với dịch vụ và thực hiện các công việc sau.
// a. Gửi GET /api/rest/method?studentCode=<mã_sinh_viên>&qCode=<qAlias> để nhận JSON gồm requestId và data.
// b. Server trả về data là object gồm tasks và deps.
// c. Sắp xếp topo các task bằng Kahn để tôn trọng mọi quan hệ phụ thuộc.
// d. Gửi PUT /api/rest/method/{requestId} với body JSON gồm studentCode, qCode và answer.
// e. Ví dụ: nếu thứ tự hợp lệ là T1,T2,T4,T3 thì answer là T1,T2,T4,T3.