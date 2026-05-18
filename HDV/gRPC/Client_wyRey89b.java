package vn.edu.ptit.grpc;

import GRPC.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class Client_wyRey89b {
    public static void main(String[] args) throws Exception {
        String host = "36.50.135.242";
        int port = 2240;
        String studentCode = "B22DCVT090";
        String questionAlias = "wyREy89b";

        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();

        JudgeServiceGrpc.JudgeServiceBlockingStub stub = JudgeServiceGrpc.newBlockingStub(channel);

        // --- BƯỚC 1: REQUEST LẤY JSON ---
        JudgeRequest request = JudgeRequest.newBuilder()
                .setStudentCode(studentCode)
                .setQuestionAlias(questionAlias)
                .build();

        JudgeResponse response = stub.request(request);
        String requestId = response.getRequestId();
        String jsonData = response.getData();

        System.out.println("JSON nhận được: " + jsonData);

        // --- BƯỚC 2: PARSE JSON VÀ TÍNH TOÁN ---
        // Sử dụng regex hoặc cắt chuỗi thủ công để không cần thêm thư viện JSON ngoài
        double price = parseValue(jsonData, "price");
        double taxRate = parseValue(jsonData, "taxRate");
        double discount = parseValue(jsonData, "discount");

        double finalPrice = price * (1 + taxRate / 100) - discount;
        
        // Làm tròn 2 chữ số thập phân
        String answer = String.format("%.2f", finalPrice);
        System.out.println("Giá cuối cùng: " + answer);

        // --- BƯỚC 3: SUBMIT ---
        SubmitRequest submitReq = SubmitRequest.newBuilder()
                .setStudentCode(studentCode)
                .setQuestionAlias(questionAlias)
                .setRequestId(requestId)
                .setAnswer(answer)
                .build();

        SubmitResponse submitRes = stub.submit(submitReq);
        
        System.out.println("STATUS: " + submitRes.getStatus());
        System.out.println("MESSAGE: " + submitRes.getMessage());

        channel.shutdown();
    }

    // Hàm hỗ trợ lấy giá trị double từ chuỗi JSON đơn giản
    private static double parseValue(String json, String key) {
        String pattern = "\"" + key + "\":";
        int start = json.indexOf(pattern) + pattern.length();
        int end = json.indexOf(",", start);
        if (end == -1) end = json.indexOf("}", start);
        return Double.parseDouble(json.substring(start, end).trim());
    }
}
    // =========================================================================================
    // NỘI DUNG
    // Một dịch vụ gRPC Judgeservice được triển khai trên server tại <Exam_IP>:2240.
    // Yêu cầu: Viết chương trình Java (gRPC client) để giao tiếp với JudgeService và thực hiện các công việc sau:
    // - Gọi phương thức Request với student_code là mã sinh viên và question_alias là <question_alias trong đề bài>.
    // - Nhận về JudgeResponse chứa request_id là chuỗi định danh và data là chuỗi JSON mô tả sản phẩm, ví dụ:
    //   {"name":"ProductABC","price":150.0,"taxRate":10.0,"discount":15.0}
    //   Trong đó discount là giá trị tuyệt đối (số tiền chiết khấu, không phải %).
    // - Parse chuỗi JSON và tính giá cuối cùng theo công thức:
    //   finalPrice = price * (1 + taxRate / 100) - discount
    // - Gọi phương thức Submit với request_id là giá trị nhận được ở bước 1 và answer là giá trị finalPrice làm tròn 2 chữ số thập phân, dạng chuỗi, ví dụ "150.00".
    // - Trong lời gọi Submit, request_id phải là giá trị đã nhận được ở bước 1.
    // Sai số cho phép: <= 0.01.
    // Ví dụ: price=150.0, taxRate=10.0, discount=15.0 -> finalPrice = 150 * 1.1 - 15 = 150.0 -> answer = "150.00"
    // Đóng kênh gRPC và kết thúc chương trình.
    // 
    // IDL (Proto Contract)
    // syntax = "proto3";
    // package GRPC;
    // option java_package = "GRPC";
    // option java_multiple_files = true;
    // 
    // service JudgeService {
    //   rpc Request (JudgeRequest) returns (JudgeResponse);
    //   rpc Submit (SubmitRequest) returns (SubmitResponse);
    // }
    // 
    // message JudgeRequest {
    //   string student_code = 1;
    //   string question_alias = 2;
    // }
    // 
    // message JudgeResponse {
    //   string request_id = 1;
    //   string data = 2;
    // }
    // 
    // message SubmitRequest {
    //   string student_code = 1;
    //   string question_alias = 2;
    //   string request_id = 3;
    //   string answer = 4;
    // }
    // 
    // message SubmitResponse {
    //   string status = 1;
    //   string message = 2;
    // }
    // Field numbers phải giữ nguyên để đúng wire format protobuf. package GRPC và service name JudgeService phải đúng theo đặc tả.
    // =========================================================================================
