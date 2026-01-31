package com.hmkeyewear.notificationservice.util;

public class RenderForm {

    public static String wrapBody(String content) {
        return """
                <!DOCTYPE html>
                <html lang="vi">
                <head>
                  <meta charset="UTF-8" />
                  <title>Xác minh OTP</title>
                </head>

                <body style="margin:0; padding:0; background-color:#f3f4f6; font-family:Arial, Helvetica, sans-serif;">
                  <table width="100%%" cellpadding="0" cellspacing="0" style="padding:30px 0;">
                    <tr>
                      <td align="center">
                        <table width="520" cellpadding="0" cellspacing="0"
                               style="background:#ffffff; border-radius:12px; overflow:hidden;
                                      box-shadow:0 8px 24px rgba(0,0,0,0.08);">

                          %s   <!-- HEADER -->
                          %s   <!-- BODY -->
                          %s   <!-- FOOTER -->

                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(renderHeader(), content, renderFooter());
    }

    public static String renderHeader() {
        return """
                <tr>
                  <td align="center" style="background:#000000; padding:24px;">
                    <img
                      src="https://fe-hmk-eyewear.vercel.app/img/logo.55f13726.webp"
                      alt="HMK Eyewear"
                      style="max-width:140px; display:block;"
                    />
                  </td>
                </tr>
                """;
    }

    public static String renderFooter() {
        return """
                <tr>
                  <td align="center" style="background:#000000; padding:16px;">
                    <p style="margin:0; font-size:12px; color:#777;">
                      © 2026 HMK Eyewear. All rights reserved.
                    </p>
                  </td>
                </tr>
                """;
    }
}
