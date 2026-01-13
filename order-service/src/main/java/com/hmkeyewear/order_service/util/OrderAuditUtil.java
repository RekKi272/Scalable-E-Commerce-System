package com.hmkeyewear.order_service.util;

import com.google.cloud.Timestamp;
import com.hmkeyewear.order_service.model.Order;

public class OrderAuditUtil {

    public static void setCreateAudit(Order order, String userId) {
        order.setCreatedAt(Timestamp.now());
        order.setCreatedBy(userId);
        order.setUpdatedAt(null);
        order.setUpdatedBy(null);
    }

    public static void setUpdateAudit(Order order, String userId) {
        order.setUpdatedAt(Timestamp.now());
        order.setUpdatedBy(userId);
    }
}
