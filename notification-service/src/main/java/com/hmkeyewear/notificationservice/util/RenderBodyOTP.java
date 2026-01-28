package com.hmkeyewear.notificationservice.util;

public class RenderBodyOTP {

    public static String render(String otp) {
        return """
                %s   <!-- TITLE -->
                %s   <!-- OTP BOX -->
                %s   <!-- INFO -->
                %s   <!-- WARNING -->
                %s   <!-- SUPPORT -->
                """.formatted(
                renderTitle(),
                renderOtpBox(otp),
                renderInfo(),
                renderWarning(),
                renderSupport());
    }

    private static String renderTitle() {
        return """
                <tr>
                  <td style="padding:28px 28px 10px;">
                    <h2 style="margin:0; font-size:22px; color:#111;">
                      Xác minh OTP
                    </h2>
                    <p style="margin:12px 0 0; font-size:14px; color:#666; line-height:1.8;">
                      Xin chào,<br/><br/>
                      Chúng tôi đã nhận được yêu cầu <strong>đặt lại mật khẩu</strong>
                      cho tài khoản HMK Eyewear được đăng ký bằng địa chỉ email này.
                      Để tiếp tục quá trình xác minh, vui lòng sử dụng mã OTP bên dưới.
                    </p>
                  </td>
                </tr>
                """;
    }

    private static String renderOtpBox(String otp) {
        return """
                <tr>
                  <td align="center" style="padding:20px 28px 28px;">
                    <div style="
                      display:inline-block;
                      padding:16px 32px;
                      border-radius:10px;
                      background-color:#111111;
                      color:#ffffff;
                      font-size:28px;
                      font-weight:bold;
                      letter-spacing:8px;
                    ">
                      %s
                    </div>
                    <p style="margin:14px 0 0; font-size:13px; color:#666;">
                      Mã OTP có hiệu lực trong <strong>5 phút</strong> kể từ thời điểm gửi email này
                    </p>
                  </td>
                </tr>
                """.formatted(otp);
    }

    private static String renderInfo() {
        return """
                <tr>
                  <td style="padding:0 28px 18px;">
                    <p style="margin:0; font-size:13px; color:#555; line-height:1.8;">
                      Vui lòng nhập chính xác mã OTP này vào màn hình xác minh trên hệ thống
                      để hoàn tất việc đặt lại mật khẩu. Trong trường hợp mã OTP hết hạn,
                      bạn có thể yêu cầu gửi lại mã mới trực tiếp trên trang đăng nhập.
                    </p>
                  </td>
                </tr>
                """;
    }

    private static String renderWarning() {
        return """
                <tr>
                  <td style="padding:0 28px 24px;">
                    <p style="margin:0; font-size:13px; color:#555; line-height:1.8;">
                      <strong>Lưu ý bảo mật:</strong><br/>
                      • Không chia sẻ mã OTP cho bất kỳ ai, kể cả nhân viên HMK Eyewear.<br/>
                      • HMK Eyewear sẽ <strong>không bao giờ</strong> yêu cầu bạn cung cấp mã OTP qua điện thoại hoặc tin nhắn.<br/>
                      • Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email — tài khoản của bạn vẫn an toàn.
                    </p>
                  </td>
                </tr>
                """;
    }

    private static String renderSupport() {
        return """
                <tr>
                  <td style="padding:0 28px 28px;">
                    <p style="margin:0; font-size:13px; color:#555; line-height:1.8;">
                      Nếu bạn gặp bất kỳ khó khăn nào trong quá trình xác minh,
                      vui lòng liên hệ với đội ngũ hỗ trợ của chúng tôi để được trợ giúp kịp thời.
                    </p>
                  </td>
                </tr>
                """;
    }
}
