import java.io.*;
import java.net.*;
import java.util.*;

public class bitmask {

    static final String STUDENT_CODE = "B22DCVT090";
    static final String QCODE = "UuRULB0f";
    static final int INF = 100000000;

    static class Edge {
        String to;
        int weight;
        Edge(String to, int weight) {
            this.to = to;
            this.weight = weight;
        }
    }

    static class Node implements Comparable<Node> {
        String name;
        int dist;
        Node(String name, int dist) {
            this.name = name;
            this.dist = dist;
        }
        @Override
        public int compareTo(Node o) {
            return Integer.compare(this.dist, o.dist);
        }
    }

    public static void main(String[] args) throws Exception {
        String examIP = "36.50.135.242";
        String baseUrl = "http://" + examIP + ":2230/api/rest/path";

        // ---------------------------------------------------------
        // 1. GỬI GET REQUEST LẤY ĐỀ
        // ---------------------------------------------------------
        String getUrlStr = baseUrl + "?studentCode=" + STUDENT_CODE + "&qCode=" + QCODE;
        URL getUrl = new URL(getUrlStr);
        HttpURLConnection getConn = (HttpURLConnection) getUrl.openConnection();
        getConn.setRequestMethod("GET");

        int getStatus = getConn.getResponseCode();
        System.out.println("GET STATUS = " + getStatus);

        InputStream getIs = (getStatus >= 200 && getStatus < 300) ? getConn.getInputStream() : getConn.getErrorStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(getIs));
        StringBuilder response = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        String jsonStr = response.toString();
        System.out.println("GET RESULT:\n" + jsonStr + "\n");

        if (getStatus != 200) return;

        // ---------------------------------------------------------
        // 2. PHÂN TÍCH DỮ LIỆU & THUẬT TOÁN
        // ---------------------------------------------------------
        String requestId = jsonStr.split("\"requestId\":\"")[1].split("\"")[0];
        String startNode = jsonStr.split("\"start\":\"")[1].split("\"")[0];
        String endNode = jsonStr.split("\"end\":\"")[1].split("\"")[0];

        int mandStart = jsonStr.indexOf("\"mandatory\":[") + 13;
        int mandEnd = jsonStr.indexOf("]", mandStart);
        String mandSub = jsonStr.substring(mandStart, mandEnd);
        String[] mandTokens = mandSub.split(",");
        List<String> mandatory = new ArrayList<>();
        for (String t : mandTokens) {
            String clean = t.replace("\"", "").trim();
            if (!clean.isEmpty()) mandatory.add(clean);
        }

        int edgesStart = jsonStr.indexOf("\"edges\":[") + 9;
        int edgesEnd = jsonStr.lastIndexOf("]");
        String edgesStr = jsonStr.substring(edgesStart, edgesEnd);

        Map<String, Map<String, Integer>> minEdges = new HashMap<>();
        int idx = 0;
        while ((idx = edgesStr.indexOf("{", idx)) != -1) {
            int endIdx = edgesStr.indexOf("}", idx);
            if (endIdx == -1) break;

            String edgeObj = edgesStr.substring(idx, endIdx + 1);
            String from = edgeObj.split("\"from\":\"")[1].split("\"")[0];
            String to = edgeObj.split("\"to\":\"")[1].split("\"")[0];
            
            String wPart = edgeObj.split("\"weight\":")[1];
            int wE = 0;
            while (wE < wPart.length() && Character.isDigit(wPart.charAt(wE))) {
                wE++;
            }
            int weight = Integer.parseInt(wPart.substring(0, wE).trim());

            if (!minEdges.containsKey(from)) minEdges.put(from, new HashMap<>());
            if (!minEdges.containsKey(to)) minEdges.put(to, new HashMap<>());

            // CHỈ NẠP CẠNH 1 CHIỀU (ĐỒ THỊ CÓ HƯỚNG)
            if (!minEdges.get(from).containsKey(to) || weight < minEdges.get(from).get(to)) {
                minEdges.get(from).put(to, weight);
            }
            
            idx = endIdx + 1;
        }

        Map<String, List<Edge>> adj = new HashMap<>();
        for (String node : minEdges.keySet()) {
            adj.put(node, new ArrayList<>());
            for (Map.Entry<String, Integer> entry : minEdges.get(node).entrySet()) {
                adj.get(node).add(new Edge(entry.getKey(), entry.getValue()));
            }
        }

        List<String> keyNodes = new ArrayList<>();
        keyNodes.add(startNode);
        for (String m : mandatory) keyNodes.add(m);
        keyNodes.add(endNode);

        int K = keyNodes.size();
        int[][] distMatrix = new int[K][K];

        for (int i = 0; i < K; i++) {
            String src = keyNodes.get(i);
            Map<String, Integer> distances = new HashMap<>();
            for (String n : adj.keySet()) distances.put(n, INF);

            PriorityQueue<Node> pq = new PriorityQueue<>();
            distances.put(src, 0);
            pq.add(new Node(src, 0));

            while (!pq.isEmpty()) {
                Node curr = pq.poll();
                String u = curr.name;
                if (curr.dist > distances.get(u)) continue;
                if (adj.get(u) != null) {
                    for (Edge edge : adj.get(u)) {
                        String v = edge.to;
                        if (distances.get(u) + edge.weight < distances.get(v)) {
                            distances.put(v, distances.get(u) + edge.weight);
                            pq.add(new Node(v, distances.get(v)));
                        }
                    }
                }
            }

            for (int j = 0; j < K; j++) {
                distMatrix[i][j] = distances.getOrDefault(keyNodes.get(j), INF);
            }
        }

        int M = mandatory.size();
        int numStates = 1 << M;
        int[][] dp = new int[numStates][M];
        List<String>[][] pathDP = new ArrayList[numStates][M];

        for (int i = 0; i < numStates; i++) Arrays.fill(dp[i], INF);

        for (int i = 0; i < M; i++) {
            if (distMatrix[0][i + 1] < INF) {
                dp[1 << i][i] = distMatrix[0][i + 1];
                pathDP[1 << i][i] = new ArrayList<>();
                pathDP[1 << i][i].add(startNode);
                pathDP[1 << i][i].add(mandatory.get(i));
            }
        }

        for (int mask = 1; mask < numStates; mask++) {
            for (int i = 0; i < M; i++) {
                if ((mask & (1 << i)) == 0 || dp[mask][i] == INF) continue;

                for (int j = 0; j < M; j++) {
                    if ((mask & (1 << j)) != 0) continue;
                    if (distMatrix[i + 1][j + 1] == INF) continue;

                    int nextMask = mask | (1 << j);
                    int nextDist = dp[mask][i] + distMatrix[i + 1][j + 1];

                    if (nextDist < dp[nextMask][j]) {
                        dp[nextMask][j] = nextDist;
                        pathDP[nextMask][j] = new ArrayList<>(pathDP[mask][i]);
                        pathDP[nextMask][j].add(mandatory.get(j));
                    }
                }
            }
        }

        int finalCost = INF;
        List<String> finalPathNodes = new ArrayList<>();
        int fullMask = numStates - 1;
        int endIdx = K - 1;

        if (M == 0) {
            finalCost = distMatrix[0][endIdx];
            finalPathNodes.add(startNode);
            finalPathNodes.add(endNode);
        } else {
            for (int i = 0; i < M; i++) {
                if (dp[fullMask][i] != INF && distMatrix[i + 1][endIdx] < INF) {
                    int total = dp[fullMask][i] + distMatrix[i + 1][endIdx];
                    if (total < finalCost) {
                        finalCost = total;
                        finalPathNodes = new ArrayList<>(pathDP[fullMask][i]);
                        finalPathNodes.add(endNode);
                    }
                }
            }
        }

        String answer = finalCost + "|" + String.join("->", finalPathNodes);
        System.out.println("SUBMITTING ANSWER = " + answer + "\n");

        // ---------------------------------------------------------
        // 3. GỬI NỘP BÀI VÀ IN RA STATUS ĐỂ BẮT BỆNH
        // ---------------------------------------------------------
        String encodedAnswer = URLEncoder.encode(answer, "UTF-8");
        String submitUrlStr = baseUrl + "/submit?studentCode=" + STUDENT_CODE 
                              + "&qCode=" + QCODE 
                              + "&requestId=" + requestId 
                              + "&answer=" + encodedAnswer;
        
        URL submitUrl = new URL(submitUrlStr);
        HttpURLConnection submitConn = (HttpURLConnection) submitUrl.openConnection();
        submitConn.setRequestMethod("GET"); // Server yêu cầu Method Not Allowed nếu dùng POST

        int submitStatus = submitConn.getResponseCode();
        System.out.println("SUBMIT STATUS = " + submitStatus);

        InputStream submitIs = (submitStatus >= 200 && submitStatus < 300) ? submitConn.getInputStream() : submitConn.getErrorStream();
        if (submitIs != null) {
            BufferedReader submitIn = new BufferedReader(new InputStreamReader(submitIs));
            String submitLine;
            StringBuilder submitResponse = new StringBuilder();
            while ((submitLine = submitIn.readLine()) != null) {
                submitResponse.append(submitLine);
            }
            submitIn.close();
            System.out.println("SUBMIT RESULT:\n" + submitResponse.toString());
        }
    }
}
// Một dịch vụ REST được triển khai trên server tại URL http://<Exam_IP>:2230/api/rest/path để xử lý các bài toán lựa chọn bản ghi và tìm đường đi. Yêu cầu: Viết chương trình tại máy trạm (REST client) để giao tiếp với dịch vụ và thực hiện các công việc sau.
// a. Gửi GET /api/rest/path?studentCode=<mã_sinh_viên>&qCode=<qAlias> để nhận JSON gồm requestId và data.
// b. Server trả về `data` là object gồm `nodes`, `edges`, `start`, `end` và `mandatory`.
// c. Tìm lộ trình ngắn nhất đi từ `start` đến `end` và ghé đủ mọi điểm trong `mandatory` bằng bitmask DP.
// d. Gửi POST /api/rest/path/submit với body JSON gồm studentCode, qCode, requestId và answer.
// e. Ví dụ: nếu lộ trình tốt nhất là `P1->P3->P4->P6` với chi phí `32` thì `answer` là `32|P1->P3->P4->P6`.