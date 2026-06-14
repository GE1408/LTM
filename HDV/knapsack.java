import java.io.*;
import java.net.*;
import java.util.*;

public class knapsack {

    static final String STUDENT_CODE = "B22DCVT090";
    static final String QCODE = "5fdXMkag";

    static String getJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\"";
        int start = json.indexOf(searchKey);
        if (start == -1) return null;
        start = json.indexOf(":", start);
        if (start == -1) return null;
        start++; 
        
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) {
            start++;
        }
        
        if (json.charAt(start) == '"') {
            start++;
            int end = json.indexOf("\"", start);
            return json.substring(start, end);
        } else {
            int end = start;
            while (end < json.length() && json.charAt(end) != ',' && json.charAt(end) != '}' && !Character.isWhitespace(json.charAt(end))) {
                end++;
            }
            return json.substring(start, end).trim();
        }
    }

    static class Item {
        String id;
        int weight;
        int value;

        Item(String id, int weight, int value) {
            this.id = id;
            this.weight = weight;
            this.value = value;
        }
    }

    public static void main(String[] args) throws Exception {
        String examIP = "36.50.135.242";
        String baseUrl = "http://" + examIP + ":2230/api/rest/object";

        String getUrlStr = baseUrl + "?studentCode=" + STUDENT_CODE + "&qCode=" + QCODE;
        URL getUrl = new URL(getUrlStr);
        HttpURLConnection getConn = (HttpURLConnection) getUrl.openConnection();
        getConn.setRequestMethod("GET");

        int getStatus = getConn.getResponseCode();
        
        InputStream getIs = (getStatus >= 200 && getStatus < 300) ? getConn.getInputStream() : getConn.getErrorStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(getIs));
        StringBuilder response = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        String jsonStr = response.toString();
        
        if (getStatus != 200) {
            System.out.println(jsonStr);
            return;
        }

        String requestId = getJsonValue(jsonStr, "requestId");
        int capacity = Integer.parseInt(getJsonValue(jsonStr, "capacity"));

        int itemsStart = jsonStr.indexOf("\"items\":");
        itemsStart = jsonStr.indexOf("[", itemsStart);
        int itemsEnd = jsonStr.indexOf("]", itemsStart);
        String itemsStr = jsonStr.substring(itemsStart, itemsEnd);

        List<Item> items = new ArrayList<>();
        int idx = 0;
        while ((idx = itemsStr.indexOf("{", idx)) != -1) {
            int endIdx = itemsStr.indexOf("}", idx);
            if (endIdx == -1) break;
            
            String itemObj = itemsStr.substring(idx, endIdx + 1);
            
            String id = getJsonValue(itemObj, "id");
            if (id == null) id = getJsonValue(itemObj, "name");
            
            String wStr = getJsonValue(itemObj, "weight");
            if (wStr == null) wStr = getJsonValue(itemObj, "w");
            if (wStr == null) wStr = getJsonValue(itemObj, "size");
            int weight = wStr != null ? Integer.parseInt(wStr) : 0;
            
            String vStr = getJsonValue(itemObj, "value");
            if (vStr == null) vStr = getJsonValue(itemObj, "val");
            if (vStr == null) vStr = getJsonValue(itemObj, "v");
            if (vStr == null) vStr = getJsonValue(itemObj, "price");
            int value = vStr != null ? Integer.parseInt(vStr) : 0;

            items.add(new Item(id, weight, value));
            idx = endIdx + 1;
        }

        int n = items.size();
        int[][] dp = new int[n + 1][capacity + 1];

        for (int i = 1; i <= n; i++) {
            Item item = items.get(i - 1);
            for (int w = 0; w <= capacity; w++) {
                if (item.weight <= w) {
                    dp[i][w] = Math.max(dp[i - 1][w], dp[i - 1][w - item.weight] + item.value);
                } else {
                    dp[i][w] = dp[i - 1][w];
                }
            }
        }

        int maxValue = dp[n][capacity];
        
        List<String> chosenIds = new ArrayList<>();
        int currentCapacity = capacity;
        for (int i = n; i > 0; i--) {
            if (dp[i][currentCapacity] != dp[i - 1][currentCapacity]) {
                Item selectedItem = items.get(i - 1);
                chosenIds.add(selectedItem.id);
                currentCapacity -= selectedItem.weight;
            }
        }

        Collections.reverse(chosenIds);
        
        String answer = String.join(",", chosenIds) + "|" + maxValue;

        URL postUrl = new URL(baseUrl + "/submit");
        HttpURLConnection postConn = (HttpURLConnection) postUrl.openConnection();
        postConn.setRequestMethod("POST");
        postConn.setRequestProperty("Content-Type", "application/json");
        postConn.setDoOutput(true);

        String jsonPost = "{"
                + "\"studentCode\":\"" + STUDENT_CODE + "\","
                + "\"qCode\":\"" + QCODE + "\","
                + "\"requestId\":\"" + requestId + "\","
                + "\"answer\":\"" + answer + "\""
                + "}";

        OutputStream os = postConn.getOutputStream();
        os.write(jsonPost.getBytes("UTF-8"));
        os.flush();
        os.close();

        int postStatus = postConn.getResponseCode();
        
        InputStream postIs = (postStatus >= 200 && postStatus < 300) ? postConn.getInputStream() : postConn.getErrorStream();
        if (postIs != null) {
            BufferedReader postIn = new BufferedReader(new InputStreamReader(postIs));
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
// b. Server trả về `data` là object gồm `capacity` và `items`.
// c. Giải bài toán 0/1 knapsack để chọn tập item có tổng giá trị lớn nhất mà không vượt quá `capacity`.
// d. Gửi POST /api/rest/object/submit với body JSON gồm studentCode, qCode, requestId và answer.
// e. Ví dụ: nếu chọn `I2` và `I4` với tổng giá trị `98` thì `answer` là `I2,I4|98`.