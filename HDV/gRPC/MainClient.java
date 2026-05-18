package vn.edu.ptit.grpc;

import GRPC.*; // Import các class sinh ra từ file proto
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.Arrays;

public class MainClient {

    public static void main(String[] args) throws Exception {
        // 1. Cấu hình thông số kết nối
        String host = "36.50.135.242";
        int port = 2240;
        String studentCode = "B22DCVT090"; // Kiểm tra lại nếu server báo lỗi SV
        String questionAlias = "kOy1TUsj"; // HÃY CẬP NHẬT MÃ MỚI NHẤT TRÊN WEB

        // Tạo kết nối tới Server
        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();

        // Tạo Stub để gọi các hàm rpc
        JudgeServiceGrpc.JudgeServiceBlockingStub stub = JudgeServiceGrpc.newBlockingStub(channel);

        // --- BƯỚC 1: GỬI REQUEST LẤY DỮ LIỆU ---
        JudgeRequest request = JudgeRequest.newBuilder()
                .setStudentCode(studentCode)
                .setQuestionAlias(questionAlias)
                .build();

        System.out.println("Đang gửi yêu cầu tới server...");
        JudgeResponse response = stub.request(request);
        
        String requestId = response.getRequestId();
        String data = response.getData();

        System.out.println("RequestId nhận được: " + requestId);
        System.out.println("Dữ liệu chuỗi số: " + data);

        // --- BƯỚC 2: XỬ LÝ TÍNH TỔNG ---
        // Tách chuỗi bằng dấu phẩy, chuyển sang số nguyên và tính tổng
        int sum = Arrays.stream(data.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .mapToInt(Integer::parseInt)
                .sum();

        String answer = String.valueOf(sum);
        System.out.println("Tổng tính được: " + answer);

        // --- BƯỚC 3: SUBMIT KẾT QUẢ ---
        SubmitRequest submitReq = SubmitRequest.newBuilder()
                .setStudentCode(studentCode)
                .setQuestionAlias(questionAlias)
                .setRequestId(requestId) // Quan trọng: Phải nộp lại đúng ID từ Bước 1
                .setAnswer(answer)
                .build();

        System.out.println("Đang nộp bài...");
        SubmitResponse submitRes = stub.submit(submitReq);
        
        // In kết quả phản hồi cuối cùng
        System.out.println("-----------------------------------");
        System.out.println("TRẠNG THÁI: " + submitRes.getStatus()); // AC = Thành công
        System.out.println("THÔNG BÁO: " + submitRes.getMessage());
        System.out.println("-----------------------------------");

        // 2. Ngắt kết nối
        channel.shutdown();
    }
}

// =========================================================================================
// ĐỀ BÀI: gRPC JudgeService
// 1. Địa chỉ Server: <Exam_IP>:2240 (Sử dụng IP hiển thị trên DB Lab, ví dụ: 36.50.135.242)
// 2. Yêu cầu: Viết gRPC client thực hiện các công việc sau:
//    - Bước 1: Tạo kết nối gRPC tới server (plaintext, không TLS).
//    - Bước 2: Gọi phương thức Request(student_code, question_alias).
//    - Bước 3: Nhận JudgeResponse:
//        + request_id: chuỗi định danh duy nhất (VD: "a1b2c3d4").
//        + data: chuỗi số nguyên phân tách bằng dấu phẩy (VD: "12,45,88,3,210").
//    - Bước 4: Parse chuỗi data thành danh sách số nguyên và tính TỔNG.
//    - Bước 5: Gọi phương thức Submit với:
//        + student_code, question_alias.
//        + request_id (Lấy chính xác từ Bước 3).
//        + answer: kết quả tổng đã tính ở Bước 4 dưới dạng chuỗi (VD: "141").
//    - Bước 6: Nhận SubmitResponse chứa status ("AC" / "WA" / "RTE") và message.
//    - Bước 7: Đóng kênh gRPC và kết thúc chương trình.
//
// Ví dụ: data = "1,2,3,4,5" -> tổng = 15 -> answer = "15"
//
// =========================================================================================
// PROTO CONTRACT:
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
// =========================================================================================