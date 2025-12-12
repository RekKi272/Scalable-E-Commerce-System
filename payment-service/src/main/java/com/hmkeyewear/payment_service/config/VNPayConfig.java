package com.hmkeyewear.payment_service.config;


import com.hmkeyewear.payment_service.util.VNPayUtil;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

@Getter
@Configuration
public class VNPayConfig {
    @Value("${payment.vnpay.url}")
    private String vnp_PayUrl;
    @Value("${payment.vnpay.return-url}")
    private String vnp_ReturnUrl;
    @Value("${payment.vnpay.tmn-code}")
    private String vnp_TmnCode;
    @Value("${payment.vnpay.secret-key}")
    private String vnp_SecretKey;
    @Value("${payment.vnpay.version}")
    private String vnp_Version;
    @Value("${payment.vnpay.command}")
    private String vnp_Command;
    @Value("${payment.vnpay.order-type}")
    private String vnp_OrderType;

    public Map<String, String> getVNPayConfig() {
        Map<String, String> vnpParamsMap = new HashMap<>();
        vnpParamsMap.put("vnp_Version", this.vnp_Version);
        vnpParamsMap.put("vnp_Command", this.vnp_Command);
        vnpParamsMap.put("vnp_TmnCode", this.vnp_TmnCode);
        vnpParamsMap.put("vnp_CurrCode", "VND");
        vnpParamsMap.put("vnp_OrderType", this.vnp_OrderType);
        vnpParamsMap.put("vnp_Locale", "vn");
        vnpParamsMap.put("vnp_ReturnUrl", this.vnp_ReturnUrl);
        TimeZone tz = TimeZone.getTimeZone("Asia/Ho_Chi_Minh");
        Calendar cal = Calendar.getInstance(tz);

        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        format.setTimeZone(tz);

        // Create date
        String createDate = format.format(cal.getTime());
        vnpParamsMap.put("vnp_CreateDate", createDate);

        // Expire +15 minutes
        cal.add(Calendar.MINUTE, 15);
        String expireDate = format.format(cal.getTime());
        vnpParamsMap.put("vnp_ExpireDate", expireDate);

        return vnpParamsMap;
    }
}
