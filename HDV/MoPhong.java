import java.io.*;
import java.net.*;
import java.util.*;

public class MoPhong {
    public static void main(String[] args) throws Exception {
        String studentCode = "B22DCVT090";
        String qCode = "YxwhkHjj";
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

        String stateTag = "\"initialState\":\"";
        int stateStart = jsonStr.indexOf(stateTag) + stateTag.length();
        int stateEnd = jsonStr.indexOf("\"", stateStart);
        String currentState = jsonStr.substring(stateStart, stateEnd);

        int eventsStart = jsonStr.indexOf("\"events\":[") + 10;
        int eventsEnd = jsonStr.indexOf("]", eventsStart);
        String eventsStr = jsonStr.substring(eventsStart, eventsEnd);
        String[] eventTokens = eventsStr.split(",");
        List<String> events = new ArrayList<>();
        for (String t : eventTokens) {
            String clean = t.replace("\"", "").trim();
            if (!clean.isEmpty()) {
                events.add(clean);
            }
        }

        int transStart = jsonStr.indexOf("\"transitions\":[") + 15;
        int transEnd = jsonStr.indexOf("]", transStart);
        String transStr = jsonStr.substring(transStart, transEnd);
        
        Map<String, Map<String, String>> transitionMap = new HashMap<>();
        int idx = 0;
        while ((idx = transStr.indexOf("{", idx)) != -1) {
            int endIdx = transStr.indexOf("}", idx);
            String objStr = transStr.substring(idx, endIdx + 1);
            
            String fromTag = "\"from\":\"";
            int fS = objStr.indexOf(fromTag) + fromTag.length();
            int fE = objStr.indexOf("\"", fS);
            String fromState = objStr.substring(fS, fE);

            String eventTag = "\"event\":\"";
            int eS = objStr.indexOf(eventTag) + eventTag.length();
            int eE = objStr.indexOf("\"", eS);
            String eventName = objStr.substring(eS, eE);

            String toTag = "\"to\":\"";
            int tS = objStr.indexOf(toTag) + toTag.length();
            int tE = objStr.indexOf("\"", tS);
            String toState = objStr.substring(tS, tE);

            if (!transitionMap.containsKey(fromState)) {
                transitionMap.put(fromState, new HashMap<>());
            }
            transitionMap.get(fromState).put(eventName, toState);

            idx = endIdx + 1;
        }

        for (String event : events) {
            if (transitionMap.containsKey(currentState)) {
                Map<String, String> nextMoves = transitionMap.get(currentState);
                if (nextMoves.containsKey(event)) {
                    currentState = nextMoves.get(event);
                }
            }
        }

        String answer = currentState;

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
// Một dịch vụ REST được triển khai trên server tại URL
// http://<Exam_IP>:2230/api/rest/method để xử lý
// các bài toán mô phỏng phương thức, trạng thái
// và quan hệ phụ thuộc.
// Yêu cầu: Viết chương trình tại máy trạm (REST client)
// để giao tiếp với dịch vụ và thực hiện các công việc sau:
// a. Gửi GET /api/rest/method?studentCode=<mã_sinh_viên>
//    &qCode=<qAlias> để nhận JSON gồm requestId và data.
// b. Server trả về `data` là object gồm `initialState`,
//    `events` và `transitions`.
// c. Mô phỏng máy trạng thái theo danh sách sự kiện
//    để lấy trạng thái cuối cùng.
// d. Gửi PUT /api/rest/method/{requestId} với body JSON gồm
//    studentCode, qCode và answer.
// Lưu ý: requestId lấy từ phase 1 và truyền trên path.
// e. Ví dụ: nếu trạng thái cuối là `ACTIVE`
//    thì `answer` là `ACTIVE`.