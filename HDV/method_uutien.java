import java.io.*;
import java.net.*;
import java.util.*;

public class method_uutien {

    static final String STUDENT_CODE = "B22DCVT090";
    static final String QCODE = "0P3u4itZ";

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

    static class Module implements Comparable<Module> {
        String id;
        String version;

        Module(String id, String version) {
            this.id = id;
            this.version = version;
        }

        @Override
        public int compareTo(Module other) {
            int cmp = compareVersions(other.version, this.version);
            if (cmp == 0) {
                return this.id.compareTo(other.id);
            }
            return cmp;
        }

        private int compareVersions(String v1, String v2) {
            String[] p1 = v1.split("\\D+");
            String[] p2 = v2.split("\\D+");
            
            List<Integer> l1 = new ArrayList<>();
            for(String s: p1) if(!s.isEmpty()) l1.add(Integer.parseInt(s));
            
            List<Integer> l2 = new ArrayList<>();
            for(String s: p2) if(!s.isEmpty()) l2.add(Integer.parseInt(s));

            int len = Math.max(l1.size(), l2.size());
            for(int i = 0; i < len; i++) {
                int n1 = i < l1.size() ? l1.get(i) : 0;
                int n2 = i < l2.size() ? l2.get(i) : 0;
                if(n1 != n2) return Integer.compare(n1, n2);
            }
            return 0;
        }
    }

    public static void main(String[] args) throws Exception {
        String examIP = "36.50.135.242";
        String baseUrl = "http://" + examIP + ":2230/api/rest/method";

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

        int modsStart = jsonStr.indexOf("\"modules\":");
        modsStart = jsonStr.indexOf("[", modsStart);
        int modsEnd = jsonStr.indexOf("]", modsStart);
        String modsStr = jsonStr.substring(modsStart, modsEnd);

        int depsStart = jsonStr.indexOf("\"deps\":");
        if (depsStart != -1) {
            depsStart = jsonStr.indexOf("[", depsStart);
        }
        int depsEnd = depsStart != -1 ? jsonStr.indexOf("]", depsStart) : -1;
        String depsStr = depsStart != -1 ? jsonStr.substring(depsStart, depsEnd) : "";

        Map<String, Module> moduleMap = new HashMap<>();
        Map<String, List<String>> adj = new HashMap<>();
        Map<String, Integer> inDegree = new HashMap<>();

        int idx = 0;
        while ((idx = modsStr.indexOf("{", idx)) != -1) {
            int endIdx = modsStr.indexOf("}", idx);
            if (endIdx == -1) break;
            
            String modObj = modsStr.substring(idx, endIdx + 1);
            
            String id = getJsonValue(modObj, "id");
            if (id == null) id = getJsonValue(modObj, "name");
            if (id == null) id = getJsonValue(modObj, "module");
            
            String version = getJsonValue(modObj, "version");
            if (version == null) version = "0";

            if (id != null) {
                moduleMap.put(id, new Module(id, version));
                adj.put(id, new ArrayList<>());
                inDegree.put(id, 0);
            }
            idx = endIdx + 1;
        }

        idx = 0;
        while (!depsStr.isEmpty() && (idx = depsStr.indexOf("{", idx)) != -1) {
            int endIdx = depsStr.indexOf("}", idx);
            if (endIdx == -1) break;
            String depObj = depsStr.substring(idx, endIdx + 1);

            String from = getJsonValue(depObj, "from");
            if (from == null) from = getJsonValue(depObj, "before");

            String to = getJsonValue(depObj, "to");
            if (to == null) to = getJsonValue(depObj, "after");

            if (from != null && to != null && adj.containsKey(from) && adj.containsKey(to)) {
                adj.get(from).add(to);
                inDegree.put(to, inDegree.get(to) + 1);
            }
            idx = endIdx + 1;
        }

        PriorityQueue<Module> pq = new PriorityQueue<>();
        for (String m : inDegree.keySet()) {
            if (inDegree.get(m) == 0) {
                pq.add(moduleMap.get(m));
            }
        }

        List<String> topoOrder = new ArrayList<>();
        while (!pq.isEmpty()) {
            Module u = pq.poll();
            topoOrder.add(u.id);

            for (String v : adj.get(u.id)) {
                inDegree.put(v, inDegree.get(v) - 1);
                if (inDegree.get(v) == 0) {
                    pq.add(moduleMap.get(v));
                }
            }
        }

        String answer = String.join(",", topoOrder);

        String putUrlStr = baseUrl + "/" + requestId;
        URL putUrl = new URL(putUrlStr);
        HttpURLConnection putConn = (HttpURLConnection) putUrl.openConnection();
        putConn.setRequestMethod("PUT");
        putConn.setRequestProperty("Content-Type", "application/json");
        putConn.setDoOutput(true);

        String jsonPut = "{"
                + "\"studentCode\":\"" + STUDENT_CODE + "\","
                + "\"qCode\":\"" + QCODE + "\","
                + "\"answer\":\"" + answer + "\""
                + "}";

        OutputStream os = putConn.getOutputStream();
        os.write(jsonPut.getBytes("UTF-8"));
        os.flush();
        os.close();

        int putStatus = putConn.getResponseCode();
        
        InputStream putIs = (putStatus >= 200 && putStatus < 300) ? putConn.getInputStream() : putConn.getErrorStream();
        if (putIs != null) {
            BufferedReader putIn = new BufferedReader(new InputStreamReader(putIs));
            String putLine;
            StringBuilder putResponse = new StringBuilder();
            while ((putLine = putIn.readLine()) != null) {
                putResponse.append(putLine);
            }
            putIn.close();
            System.out.println(putResponse.toString());
        }
    }
}
// Một dịch vụ REST được triển khai trên server tại URL http://<Exam_IP>:2230/api/rest/method để xử lý các bài toán mô phỏng phương thức xử lý, trạng thái và quan hệ phụ thuộc. Yêu cầu: Viết chương trình tại máy trạm (REST client) để giao tiếp với dịch vụ và thực hiện các công việc sau.
// a. Gửi GET /api/rest/method?studentCode=<mã_sinh_viên>&qCode=<qAlias> để nhận JSON gồm requestId và data.
// b. Server trả về `data` là object gồm `modules` và `deps`.
// c. Sắp xếp topo, nhưng khi có nhiều module sẵn sàng thì ưu tiên module có version cao hơn.
// d. Gửi PUT /api/rest/method/{requestId} với body JSON gồm studentCode, qCode và answer.
// e. Ví dụ: nếu thứ tự cuối là `M2,M4,M1` thì `answer` là `M2,M4,M1`.
// (Topo là cách xếp thứ tự công việc có phụ thuộc. Nếu có A -> B thì A phải đứng trước B. Với Kahn, ta luôn chọn các task chưa có phụ thuộc đầu vào (in-degree = 0), đưa vào kết quả, rồi loại ảnh hưởng của chúng để tìm task tiếp theo)
// Lưu ý: requestId lấy từ phase 1 và truyền trên path /api/rest/method/{requestId}.