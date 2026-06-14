import java.io.*;
import java.net.*;
import java.util.*;

public class rolling_hash {

    static final String STUDENT_CODE = "B22DCVT090";
    static final String QCODE = "EqVt4qn4";

    public static void main(String[] args) throws Exception {
        String examIP = "36.50.135.242";
        String baseUrl = "http://" + examIP + ":2230/api/rest/header";

        // Gửi GET Request lấy đề
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

        // Bóc tách thủ công
        String requestId = jsonStr.split("\"requestId\":\"")[1].split("\"")[0];
        String text = jsonStr.split("\"text\":\"")[1].split("\"")[0];
        
        String wPart = jsonStr.split("\"windowSize\":")[1].trim(); // Thêm trim() để an toàn
        int wE = 0;
        while (wE < wPart.length() && Character.isDigit(wPart.charAt(wE))) {
            wE++;
        }
        int windowSize = Integer.parseInt(wPart.substring(0, wE));

        // Thuật toán Rolling Hash (Rabin-Karp)
        long q = 1000000007; 
        long d = 256;        
        long h = 0;
        long hrm = 1;
        
        // CHỈNH SỬA: Giá trị mặc định phải là "NONE"
        String answer = "NONE"; 
        
        if (windowSize > 0 && text.length() >= windowSize) {
            for (int i = 0; i < windowSize - 1; i++) {
                hrm = (hrm * d) % q;
            }
            for (int i = 0; i < windowSize; i++) {
                h = (h * d + text.charAt(i)) % q;
            }

            Map<Long, List<String>> seen = new HashMap<>();
            seen.put(h, new ArrayList<>());
            seen.get(h).add(text.substring(0, windowSize));

            for (int i = windowSize; i < text.length(); i++) {
                h = (h + q - (hrm * text.charAt(i - windowSize)) % q) % q;
                h = (h * d + text.charAt(i)) % q;
                
                String currentSub = text.substring(i - windowSize + 1, i + 1);
                if (seen.containsKey(h)) {
                    boolean found = false;
                    for (String s : seen.get(h)) {
                        if (s.equals(currentSub)) {
                            answer = currentSub;
                            found = true;
                            break;
                        }
                    }
                    if (found) break; 
                } else {
                    seen.put(h, new ArrayList<>());
                }
                seen.get(h).add(currentSub);
            }
        }

        // Gửi POST Request nộp bài
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
            // CHỈ IN DUY NHẤT PHẢN HỒI JSON CỦA SERVER
            System.out.println(postResponse.toString());
        }
    }
}
// Một dịch vụ REST được triển khai trên server tại URL http://<Exam_IP>:2230/api/rest/header để xử lý các bài toán mã kiểm tra, chữ ký và băm dữ liệu. Yêu cầu: Viết chương trình tại máy trạm (REST client) để giao tiếp với dịch vụ và thực hiện các công việc sau.
// a. Gửi GET /api/rest/header?studentCode=<mã_sinh_viên>&qCode=<qAlias> để nhận JSON gồm requestId và data.
// b. Server trả về `data` là object gồm `text` và `windowSize`.
// c. Dò chuỗi con độ dài `windowSize` đầu tiên xuất hiện lặp lại bằng rolling hash.
// d. Gửi POST /api/rest/header/submit với body JSON gồm studentCode, qCode, requestId và answer.
// e. Ví dụ: nếu chuỗi lặp đầu tiên là `abca` thì `answer` là `abca`.