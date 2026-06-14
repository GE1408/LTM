import java.io.*;
import java.net.*;
import java.util.*;

public class Dijkstra_object {

    static final String STUDENT_CODE = "B22DCVT090";
    static final String QCODE = "6HULVOg5";

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

        String requestId = jsonStr.split("\"requestId\":\"")[1].split("\"")[0];
        String startNode = jsonStr.split("\"start\":\"")[1].split("\"")[0];
        String endNode = jsonStr.split("\"end\":\"")[1].split("\"")[0];

        int edgesStart = jsonStr.indexOf("\"edges\":[");
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

        Map<String, Integer> dist = new HashMap<>();
        Map<String, String> parent = new HashMap<>();
        PriorityQueue<Node> pq = new PriorityQueue<>();

        dist.put(startNode, 0);
        pq.add(new Node(startNode, 0));

        while (!pq.isEmpty()) {
            Node curr = pq.poll();
            String u = curr.name;

            if (curr.dist > dist.getOrDefault(u, Integer.MAX_VALUE)) continue;
            if (u.equals(endNode)) break;

            if (adj.containsKey(u)) {
                for (Edge e : adj.get(u)) {
                    String v = e.to;
                    int newDist = curr.dist + e.weight;
                    if (newDist < dist.getOrDefault(v, Integer.MAX_VALUE)) {
                        dist.put(v, newDist);
                        parent.put(v, u);
                        pq.add(new Node(v, newDist));
                    }
                }
            }
        }

        List<String> path = new ArrayList<>();
        String currNode = endNode;
        while (currNode != null) {
            path.add(currNode);
            currNode = parent.get(currNode);
        }
        Collections.reverse(path);
        
        String pathStr = String.join("->", path);
        String answer = dist.get(endNode) + "|" + pathStr;

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
// b. Server trả về `data` là object gồm `nodes`, `edges`, `start`, `end`.
// c. Dùng Dijkstra để tìm đường đi ngắn nhất từ `start` đến `end`.
// d. Gửi POST /api/rest/object/submit với body JSON gồm studentCode, qCode, requestId và answer.
// e. Ví dụ: nếu đường đi ngắn nhất là `N1->N3->N5` với chi phí `17` thì `answer` là `17|N1->N3->N5`.