import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Anagram {
    public static void main(String[] args) throws Exception {
        String studentCode = "B22DCVT090";
        String qCode = "DEWP6VMX";
        String baseUrl = "http://36.50.135.242:2230/api/rest/character";

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

        int wordsStart = jsonStr.indexOf("[") + 1;
        int wordsEnd = jsonStr.indexOf("]");
        String wordsStr = jsonStr.substring(wordsStart, wordsEnd);
        String[] tokens = wordsStr.split(",");
        List<String> words = new ArrayList<>();
        for (String token : tokens) {
            String cleanToken = token.replace("\"", "").trim();
            if (!cleanToken.isEmpty()) {
                words.add(cleanToken);
            }
        }

        Map<String, List<String>> anagramMap = new HashMap<>();
        for (String word : words) {
            char[] chars = word.toCharArray();
            Arrays.sort(chars);
            String sortedKey = new String(chars);
            if (!anagramMap.containsKey(sortedKey)) {
                anagramMap.put(sortedKey, new ArrayList<>());
            }
            anagramMap.get(sortedKey).add(word);
        }

        List<String> formattedGroups = new ArrayList<>();
        for (List<String> group : anagramMap.values()) {
            Collections.sort(group);
            StringBuilder groupBuilder = new StringBuilder();
            for (int i = 0; i < group.size(); i++) {
                groupBuilder.append(group.get(i));
                if (i < group.size() - 1) {
                    groupBuilder.append(",");
                }
            }
            formattedGroups.add(groupBuilder.toString());
        }

        Collections.sort(formattedGroups);

        StringBuilder answerBuilder = new StringBuilder();
        for (int i = 0; i < formattedGroups.size(); i++) {
            answerBuilder.append(formattedGroups.get(i));
            if (i < formattedGroups.size() - 1) {
                answerBuilder.append("|");
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
// Một dịch vụ REST được triển khai trên server tại URL
// http://<Exam_IP>:2230/api/rest/character để xử lý
// các bài toán về chuỗi và ký tự.
// Yêu cầu: Viết chương trình tại máy trạm (REST client)
// để giao tiếp với dịch vụ và thực hiện các công việc sau:
// a. Gửi GET /api/rest/character?studentCode=<mã_sinh_viên>
//    &qCode=<qAlias> để nhận JSON gồm requestId và data.
// b. Server trả về `data` là object gồm `words`.
// c. Nhóm các từ cùng chữ cái sau khi sắp xếp,
//    rồi sắp xếp từng nhóm theo thứ tự từ điển.
// d. Gửi POST /api/rest/character/submit với body JSON gồm
//    studentCode, qCode, requestId và answer.
// e. Ví dụ: nếu `words=["eat","tea","tan","ate"]`
//    thì `answer` là `ate,eat,tea|tan`.