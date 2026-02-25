package com.hmkeyewear.notificationservice.util;

import com.hmkeyewear.common_dto.dto.InvoiceEmailEvent;
import com.hmkeyewear.common_dto.dto.OrderItemDto;

public class RenderBodyInvoice {

  public static String render(InvoiceEmailEvent e) {
    return """
        %s   %s   %s   %s   %s   """.formatted(
        renderTitle(),
        renderOrderInfo(e),
        renderProductTable(e),
        renderSummary(e),
        renderThankYou(e));
  }

  private static String renderTitle() {
    return """
        <tr>
          <td style="padding:28px 28px 10px;">
            <h1 style="margin:0; font-size:28px; color:#111;">Xác nhận đơn hàng</h1>
          </td>
        </tr>
        """;
  }

  private static String renderOrderInfo(InvoiceEmailEvent e) {
    // --- XỬ LÝ AN TOÀN CHO SHIP INFO ---
    String address = "Mua tại cửa hàng";
    if (e.getShip() != null) {
      String detail = e.getShip().getAddressDetail() != null ? e.getShip().getAddressDetail() : "";
      String ward = e.getShip().getAddressWard() != null ? e.getShip().getAddressWard() : "";
      String province = e.getShip().getAddressProvince() != null ? e.getShip().getAddressProvince() : "";
      address = String.format("%s, %s, %s", detail, ward, province).replaceAll("^, |, $", "");
    }

    return """
        <tr>
          <td style="padding:0 28px 24px;">
            <table width="100%%" style="font-size:14px; color:#333;">
              <tr><td><strong>Mã đơn hàng:</strong></td><td align="right">%s</td></tr>
              <tr><td><strong>Tên người nhận:</strong></td><td align="right">%s</td></tr>
              <tr><td><strong>Số điện thoại:</strong></td><td align="right">%s</td></tr>
              <tr><td valign="top"><strong>Địa chỉ:</strong></td><td align="right" style="line-height:1.6;">%s</td></tr>
              <tr><td><strong>Ghi chú:</strong></td><td align="right">%s</td></tr>
            </table>
          </td>
        </tr>
        """.formatted(
        e.getOrderId(),
        e.getFullName(),
        e.getPhone(),
        address,
        e.getNote() == null ? "-" : e.getNote());
  }

  private static String renderProductTable(InvoiceEmailEvent e) {
    StringBuilder rows = new StringBuilder();

    if (e.getDetails() != null) {
      for (OrderItemDto d : e.getDetails()) {
        rows.append("""
            <tr>
              <td style="padding:12px; border-bottom:1px solid #f0f0f0;">
                <strong>%s</strong><br/>
                <span style="font-size:12px; color:#888;">Mã SP: %s</span>
              </td>
              <td align="center" style="padding:12px; border-bottom:1px solid #f0f0f0;">%d</td>
              <td align="right" style="padding:12px; border-bottom:1px solid #f0f0f0;">%,.0f ₫</td>
              <td align="right" style="padding:12px; border-bottom:1px solid #f0f0f0;">%,.0f ₫</td>
            </tr>
            """.formatted(
            d.getProductName(),
            d.getProductId(),
            d.getQuantity(),
            d.getUnitPrice(),
            d.getTotalPrice()));
      }
    }

    return """
        <tr>
          <td style="padding:0 28px 24px;">
            <table width="100%%" style="border-collapse:collapse; font-size:14px;">
              <thead>
                <tr style="background:#f8f9fa;">
                  <th align="left" style="padding:12px;">Sản phẩm</th>
                  <th align="center" style="padding:12px;">SL</th>
                  <th align="right" style="padding:12px;">Đơn giá</th>
                  <th align="right" style="padding:12px;">Thành tiền</th>
                </tr>
              </thead>
              <tbody>%s</tbody>
            </table>
          </td>
        </tr>
        """.formatted(rows);
  }

  private static String renderSummary(InvoiceEmailEvent e) {
    // --- XỬ LÝ AN TOÀN CHO SHIPPING FEE ---
    double shippingFee = (e.getShip() != null) ? e.getShip().getShippingFee() : 0.0;

    return """
        <tr>
          <td style="padding:0 28px 28px;">
            <table width="100%%" style="font-size:14px;">
              <tr><td>Phương thức thanh toán</td><td align="right">%s</td></tr>
              <tr><td>Giá tạm tính</td><td align="right">%,.0f ₫</td></tr>
              <tr><td>Giảm giá</td><td align="right">%,.0f ₫</td></tr>
              <tr><td>Phí vận chuyển</td><td align="right">%,.0f ₫</td></tr>
              <tr>
                <td style="padding-top:12px;"><strong>Tổng thanh toán</strong></td>
                <td align="right" style="padding-top:12px; font-size:18px; color:#e11d48;">
                  <strong>%,.0f ₫</strong>
                </td>
              </tr>
            </table>
          </td>
        </tr>
        """.formatted(
        e.getPaymentMethod(),
        e.getPriceTemp(),
        e.getPriceDecreased(),
        shippingFee,
        e.getSummary());
  }

  private static String renderThankYou(InvoiceEmailEvent e) {
    return """
        <tr>
          <td align="center" style="padding:24px 28px; background:#fafafa;">
            <p style="margin:0; font-size:14px; color:#555; line-height:1.6;">
              Xin chào <strong>%s</strong>,<br/>
              Cảm ơn bạn đã tin tưởng và đặt hàng tại <strong>HMK Eyewear</strong>.
            </p>
          </td>
        </tr>
        """.formatted(e.getFullName());
  }
}