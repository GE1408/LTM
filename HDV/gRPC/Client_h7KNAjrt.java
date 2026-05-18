package vn.edu.ptit.grpc;

import GRPC.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Client_h7KNAjrt {

    public static void main(String[] args) throws Exception {
        // 1. Thông số từ đề bài mới
        String host = "36.50.135.242";
        int port = 2240;
        String studentCode = "B22DCVT090";
        String questionAlias = "hzKNAjrt";

        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();

        JudgeServiceGrpc.JudgeServiceBlockingStub stub = JudgeServiceGrpc.newBlockingStub(channel);

        // --- BƯỚC 1: REQUEST LẤY CHUỖI TỪ ---
        JudgeRequest request = JudgeRequest.newBuilder()
                .setStudentCode(studentCode)
                .setQuestionAlias(questionAlias)
                .build();

        JudgeResponse response = stub.request(request);
        String requestId = response.getRequestId();
        String data = response.getData();

        System.out.println("Data nhận được: " + data);

        // --- BƯỚC 2: XỬ LÝ SẮP XẾP (CASE-INSENSITIVE) ---
        // Tách chuỗi -> Sắp xếp không phân biệt hoa thường -> Gộp lại bằng dấu phẩy
        String answer = Arrays.stream(data.split(","))
                .map(String::trim)
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.joining(","));

        System.out.println("Chuỗi sau khi sắp xếp: " + answer);

        // --- BƯỚC 3: SUBMIT KẾT QUẢ ---
        SubmitRequest submitReq = SubmitRequest.newBuilder()
                .setStudentCode(studentCode)
                .setQuestionAlias(questionAlias)
                .setRequestId(requestId)
                .setAnswer(answer)
                .build();

        SubmitResponse submitRes = stub.submit(submitReq);
        
        System.out.println("-----------------------------------");
        System.out.println("STATUS: " + submitRes.getStatus());
        System.out.println("MESSAGE: " + submitRes.getMessage());
        System.out.println("-----------------------------------");

        channel.shutdown();
    }
}
// =========================================================================================
// NỘI DUNG ĐỀ BÀI:
// Một dịch vụ gRPC JudgeService được triển khai trên server tại <Exam_IP>:2240.
// Yêu cầu: Viết chương trình Java (gRPC client) để giao tiếp với JudgeService và thực hiện các công việc sau:
// 1. Gọi phương thức Request với student_code là mã sinh viên và question_alias là <question_alias trong đề bài>.
// 2. Nhận về JudgeResponse chứa request_id là chuỗi định danh và data là các từ phân tách bằng dấu phẩy, 
//    ví dụ "banana,apple,cherry,date".
// 3. Parse chuỗi data thành danh sách từ, sắp xếp theo thứ tự từ điển (không phân biệt hoa thường - case-insensitive).
// 4. Gọi phương thức Submit với request_id là giá trị nhận được ở bước 1 và answer là danh sách từ đã sắp xếp, 
//    phân tách bằng dấu phẩy, ví dụ "apple,banana,cherry,date".
// 5. Trong lời gọi Submit, request_id phải là giá trị đã nhận được ở bước 1.
// 
// Ví dụ: data = "banana,apple,cherry" -> sort case-insensitive -> answer = "apple,banana,cherry"
// 6. Đóng kênh gRPC và kết thúc chương trình.
// 
// =========================================================================================
// IDL (PROTO CONTRACT):
// syntax = "proto3";
// package GRPC;
// option java_package = "GRPC";
// option java_multiple_files = true;
// 
// service JudgeService {
//   rpc Request (JudgeRequest) returns (JudgeResponse);
//   rpc Submit  (SubmitRequest) returns (SubmitResponse);
// }
// 
// message JudgeRequest {
//   string student_code    = 1;
//   string question_alias  = 2;
// }
// 
// message JudgeResponse {
//   string request_id = 1;
//   string data       = 2;
// }
// 
// message SubmitRequest {
//   string student_code    = 1;
//   string question_alias  = 2;
//   string request_id      = 3;
//   string answer          = 4;
// }
// 
// message SubmitResponse {
//   string status  = 1;
//   string message = 2;
// }
// 
// GHI CHÚ:
// Field numbers phải giữ nguyên để đúng wire format protobuf. 
// package GRPC và service name JudgeService phải đúng theo đặc tả.
// =========================================================================================
// Bước 1: Khởi tạo Project
// - Chọn File > New Project > Java with Maven > Java Application.
// - Đặt tên Project và Group Id (VD: vn.edu.ptit).
//
// Bước 2: Cấu hình file pom.xml
// - Thêm 3 dependency chính: grpc-netty-shaded, grpc-protobuf, grpc-stub.
// - Thêm build extension: os-maven-plugin (bắt buộc để nhận diện Windows/Mac).
// - Thêm build plugin: protobuf-maven-plugin (để biên dịch file .proto).
//
// Bước 3: Đặt file .proto
// - Tạo thư mục đúng đường dẫn: src/main/proto (ngang hàng với folder java).
// - Dán file judge.proto vào thư mục proto vừa tạo.
//
// Bước 4: Sinh Code tự động (Generate Sources)
// - Chuột phải vào Project > chọn Clean and Build.
// - Phải đợi Output báo BUILD SUCCESS (để Maven tự sinh ra các Class Java).
// - Nếu code vẫn báo lỗi đỏ, chuột phải Project > chọn Reload Project.