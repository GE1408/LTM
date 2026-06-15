import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import java.util.*;

public class Merkle {

    static final String STUDENT_CODE = "B22DCVT090";
    static final String QCODE = "w4WcWSdk";

    public static void main(String[] args) throws Exception {
        String examIP = "36.50.135.242";
        String baseUrl = "http://" + examIP + ":2230/api/rest/header";

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

        int leavesStart = jsonStr.indexOf("\"leaves\":[") + 10;
        int leavesEnd = jsonStr.indexOf("]", leavesStart);
        String leavesSub = jsonStr.substring(leavesStart, leavesEnd);
        
        List<String> leaves = new ArrayList<>();
        int idx = 0;
        while ((idx = leavesSub.indexOf("\"", idx)) != -1) {
            int endIdx = leavesSub.indexOf("\"", idx + 1);
            if (endIdx == -1) break;
            leaves.add(leavesSub.substring(idx + 1, endIdx));
            idx = endIdx + 1;
        }

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        List<byte[]> currentLevel = new ArrayList<>();

        for (String leaf : leaves) {
            currentLevel.add(md.digest(leaf.getBytes("UTF-8")));
        }

        while (currentLevel.size() > 1) {
            List<byte[]> nextLevel = new ArrayList<>();
            for (int i = 0; i < currentLevel.size(); i += 2) {
                byte[] left = currentLevel.get(i);
                byte[] right = (i + 1 < currentLevel.size()) ? currentLevel.get(i + 1) : left;

                byte[] combined = new byte[left.length + right.length];
                System.arraycopy(left, 0, combined, 0, left.length);
                System.arraycopy(right, 0, combined, left.length, right.length);

                nextLevel.add(md.digest(combined));
            }
            currentLevel = nextLevel;
        }

        byte[] rootHash = currentLevel.get(0);
        StringBuilder hexString = new StringBuilder();
        for (byte b : rootHash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        
        String answer = hexString.toString().toLowerCase();

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
// Một dịch vụ REST được triển khai trên server tại URL http://<Exam_IP>:2230/api/rest/header để xử lý các bài toán mã kiểm tra, chữ ký và băm dữ liệu. Yêu cầu: Viết chương trình tại máy trạm (REST client) để giao tiếp với dịch vụ và thực hiện các công việc sau.
// a. Gửi GET /api/rest/header?studentCode=<mã_sinh_viên>&qCode=<qAlias> để nhận JSON gồm requestId và data.
// b. Server trả về `data` là object gồm `leaves`.
// c. Băm từng lá bằng SHA-256 rồi ghép cặp lên dần để lấy Merkle root.
// d. Gửi POST /api/rest/header/submit với body JSON gồm studentCode, qCode, requestId và answer.
// e. Ví dụ: nếu root hex là `abcd1234...` thì `answer` là chuỗi hex đó.
// 
// (Các quy tắc thực tế
// Mỗi leaf là chuỗi UTF-8, băm SHA-256 ra 32 byte.
// Lên tầng trên bằng cách ghép cặp trái-phải theo thứ tự hiện có.
// Nếu tầng có số node lẻ, node cuối được nhân đôi (right = left).
// Parent hash = SHA-256(left_bytes + right_bytes), tức nối byte hash thô, không phải nối chuỗi hex.
// Kết quả answer là chuỗi hex chữ thường.
// )