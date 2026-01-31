package com.hmkeyewear.order_service.controller;

import com.hmkeyewear.order_service.service.OrderStatisticService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/order/statistic")
public class StatisticController {

    private final OrderStatisticService service;

    public StatisticController(OrderStatisticService service) {
        this.service = service;
    }

    @GetMapping("/week")
    public ResponseEntity<?> statisticWeek(
            @RequestHeader("X-User-Role") String role,
            @RequestParam("date") LocalDate date)
            throws ExecutionException, InterruptedException {

        if (!"ROLE_ADMIN".equals(role)) {
            return ResponseEntity.status(403).body("Bạn không có thẩm quyền");
        }

        LocalDate monday = date.with(java.time.DayOfWeek.MONDAY);
        LocalDate sunday = date.with(java.time.DayOfWeek.SUNDAY);

        return ResponseEntity.ok(service.statistic(monday, sunday));
    }

    @GetMapping("/month")
    public ResponseEntity<?> statisticMonth(
            @RequestHeader("X-User-Role") String role,
            @RequestParam(name = "month") int month,
            @RequestParam(name = "year") int year)
            throws ExecutionException, InterruptedException {

        if (!"ROLE_ADMIN".equals(role)) {
            return ResponseEntity.status(403).body("Bạn không có thẩm quyền");
        }

        LocalDate from = LocalDate.of(year, month, 1);
        LocalDate to = from.with(java.time.temporal.TemporalAdjusters.lastDayOfMonth());

        return ResponseEntity.ok(service.statistic(from, to));
    }

    @GetMapping("/year")
    public ResponseEntity<?> statisticYear(
            @RequestHeader("X-User-Role") String role,
            @RequestParam(name = "year") int year)
            throws ExecutionException, InterruptedException {

        if (!"ROLE_ADMIN".equals(role)) {
            return ResponseEntity.status(403).body("Bạn không có thẩm quyền");
        }

        LocalDate from = LocalDate.of(year, 1, 1);
        LocalDate to = LocalDate.of(year, 12, 31);

        return ResponseEntity.ok(service.statisticYear(year));
    }
}
