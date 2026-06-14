import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;

import org.json.JSONArray;
import org.json.JSONObject;

public class Dijkstra {

    static class Edge {
        String to;
        int weight;

        Edge(String to, int weight) {
            this.to = to;
            this.weight = weight;
        }
    }

    static class State {
        String node;
        int dist;

        State(String node, int dist) {
            this.node = node;
            this.dist = dist;
        }
    }

    public static void main(String[] args) throws Exception {

        String studentCode = "B22DCVT090";
        String qCode = "dMgqJXov";
        String host = "36.50.135.242";

        HttpClient client = HttpClient.newHttpClient();

        String getUrl =
                "http://" + host
                        + ":2230/api/rest/path"
                        + "?studentCode=" + studentCode
                        + "&qCode=" + qCode;

        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(getUrl))
                .GET()
                .build();

        HttpResponse<String> getResponse =
                client.send(getRequest, HttpResponse.BodyHandlers.ofString());

        System.out.println("GET STATUS = " + getResponse.statusCode());
        System.out.println(getResponse.body());

        JSONObject root = new JSONObject(getResponse.body());

        String requestId = root.getString("requestId");
        JSONObject data = root.getJSONObject("data");

        String start = data.getString("start");
        String end = data.getString("end");

        JSONArray edges = data.getJSONArray("edges");

        Map<String, List<Edge>> graph = new HashMap<>();

        for (int i = 0; i < edges.length(); i++) {

            JSONObject e = edges.getJSONObject(i);

            String from = e.getString("from");
            String to = e.getString("to");
            int weight = e.getInt("weight");

            graph.computeIfAbsent(from, k -> new ArrayList<>())
                    .add(new Edge(to, weight));

            graph.computeIfAbsent(to, k -> new ArrayList<>());
        }

        Map<String, Integer> dist = new HashMap<>();
        Map<String, String> prev = new HashMap<>();

        PriorityQueue<State> pq =
                new PriorityQueue<>(Comparator.comparingInt(s -> s.dist));

        dist.put(start, 0);
        pq.add(new State(start, 0));

        while (!pq.isEmpty()) {

            State cur = pq.poll();

            if (cur.dist >
                    dist.getOrDefault(cur.node, Integer.MAX_VALUE))
                continue;

            for (Edge edge :
                    graph.getOrDefault(cur.node, Collections.emptyList())) {

                int newDist = cur.dist + edge.weight;

                if (newDist <
                        dist.getOrDefault(edge.to, Integer.MAX_VALUE)) {

                    dist.put(edge.to, newDist);
                    prev.put(edge.to, cur.node);

                    pq.add(new State(edge.to, newDist));
                }
            }
        }

        List<String> path = new ArrayList<>();

        String current = end;

        while (current != null) {
            path.add(current);
            current = prev.get(current);
        }

        Collections.reverse(path);

        String pathString = String.join("->", path);
        String answer = dist.get(end) + "|" + pathString;

        System.out.println("ANSWER = " + answer);

        JSONObject submitBody = new JSONObject();
        submitBody.put("studentCode", studentCode);
        submitBody.put("qCode", qCode);
        submitBody.put("requestId", requestId);
        submitBody.put("answer", answer);

        System.out.println("SUBMIT BODY:");
        System.out.println(submitBody.toString());

        // ====== CÁCH 1: POST ======

        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(
                        "http://" + host + ":2230/api/rest/path/submit"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        submitBody.toString()))
                .build();

        HttpResponse<String> postResponse =
                client.send(postRequest,
                        HttpResponse.BodyHandlers.ofString());

        System.out.println("POST STATUS = "
                + postResponse.statusCode());
        System.out.println(postResponse.body());

        // ====== NẾU POST 405 THÌ THỬ GET ======

        String submitUrl =
                "http://" + host
                        + ":2230/api/rest/path/submit"
                        + "?studentCode="
                        + URLEncoder.encode(studentCode,
                                StandardCharsets.UTF_8)
                        + "&qCode="
                        + URLEncoder.encode(qCode,
                                StandardCharsets.UTF_8)
                        + "&requestId="
                        + URLEncoder.encode(requestId,
                                StandardCharsets.UTF_8)
                        + "&answer="
                        + URLEncoder.encode(answer,
                                StandardCharsets.UTF_8);

        HttpRequest getSubmitRequest =
                HttpRequest.newBuilder()
                        .uri(URI.create(submitUrl))
                        .GET()
                        .build();

        HttpResponse<String> getSubmitResponse =
                client.send(getSubmitRequest,
                        HttpResponse.BodyHandlers.ofString());

        System.out.println("GET SUBMIT STATUS = "
                + getSubmitResponse.statusCode());
        System.out.println(getSubmitResponse.body());
    }
}
// Một dịch vụ REST được triển khai trên server tại URL http://<Exam_IP>:2230/api/rest/path để xử lý các bài toán lựa chọn bản ghi và tìm đường đi. Yêu cầu: Viết chương trình tại máy trạm (REST client) để giao tiếp với dịch vụ và thực hiện các công việc sau.
// a. Gửi GET /api/rest/path?studentCode=<mã_sinh_viên>&qCode=<qAlias> để nhận JSON gồm requestId và data.
// b. Server trả về data là object gồm nodes, edges, start, end.
// c. Tính đường đi ngắn nhất giữa start và end bằng Dijkstra.
// d. Gửi POST /api/rest/path/submit với body JSON gồm studentCode, qCode, requestId và answer.
// e. Ví dụ: nếu chi phí là 21 và đường đi là P1->P2->P5 thì answer là 21|P1->P2->P5.