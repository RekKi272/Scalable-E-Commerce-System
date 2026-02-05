package com.hmkeyewear.order_service.util;

import com.hmkeyewear.order_service.constant.OrderStatus;

public class OrderStatusValidator {

    private OrderStatusValidator() {
        // util class, không cho new
    }

    public static void validateStatusTransition(
            OrderStatus currentStatus,
            OrderStatus newStatus) {

        // FAILED: không cho update thủ công, cũng không cho chuyển tiếp
        if (currentStatus == OrderStatus.FAILED) {
            throw new RuntimeException("Đơn hàng đã FAILED, không thể cập nhật trạng thái");
        }

        if (newStatus == OrderStatus.FAILED) {
            throw new RuntimeException("Không cho phép cập nhật trạng thái FAILED thủ công");
        }

        switch (currentStatus) {

            case PENDING:
                if (newStatus != OrderStatus.PAID
                        && newStatus != OrderStatus.DELIVERING
                        && newStatus != OrderStatus.CANCEL) {
                    throw new RuntimeException("Không thể chuyển từ PENDING sang " + newStatus);
                }
                break;

            case PAID:
                if (newStatus != OrderStatus.DELIVERING
                        && newStatus != OrderStatus.CANCEL) {
                    throw new RuntimeException("Không thể chuyển từ PAID sang " + newStatus);
                }
                break;

            case DELIVERING:
                if (newStatus != OrderStatus.COMPLETED
                        && newStatus != OrderStatus.CANCEL) {
                    throw new RuntimeException("Không thể chuyển từ DELIVERING sang " + newStatus);
                }
                break;

            case COMPLETED:
                throw new RuntimeException("Đơn hàng đã COMPLETED, không thể cập nhật trạng thái");

            case CANCEL:
                throw new RuntimeException("Đơn hàng đã CANCEL, không thể cập nhật trạng thái");

            default:
                throw new RuntimeException("Trạng thái không hợp lệ");
        }
    }
}
