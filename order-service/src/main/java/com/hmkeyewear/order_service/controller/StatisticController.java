// package com.hmkeyewear.order_service.controller;

// import com.hmkeyewear.order_service.service.OrderStatisticService;

// import org.springframework.format.annotation.DateTimeFormat;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import java.time.LocalDate;
// import java.util.concurrent.ExecutionException;

// @RestController
// @RequestMapping("/order/statistic")
// public class StatisticController {

// private final OrderStatisticService orderStatisticService;

// public StatisticController(OrderStatisticService orderStatisticService) {
// this.orderStatisticService = orderStatisticService;
// }

// @GetMapping("/month")
// public ResponseEntity<?> statisticByMonth(
// @RequestHeader("X-User-Role") String role,
// @RequestHeader("X-User-Id") String userId,
// @RequestParam("year") int year,
// @RequestParam("month") int month)
// throws ExecutionException, InterruptedException {

// if (!"ROLE_ADMIN".equalsIgnoreCase(role)) {
// return ResponseEntity.status(403).body("Bạn không có thẩm quyền");
// }

// return ResponseEntity.ok(
// orderStatisticService.statisticByMonth(year, month));
// }

// @GetMapping("/week")
// public ResponseEntity<?> statisticByWeek(
// @RequestHeader("X-User-Role") String role,
// @RequestHeader("X-User-Id") String userId,
// @RequestParam("localDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
// LocalDate localDate)
// throws ExecutionException, InterruptedException {

// if (!"ROLE_ADMIN".equalsIgnoreCase(role)) {
// return ResponseEntity.status(403).body("Bạn không có thẩm quyền");
// }

// return ResponseEntity.ok(
// orderStatisticService.statisticByWeek(localDate));
// }

// @GetMapping("/year")
// public ResponseEntity<?> statisticByYear(
// @RequestHeader("X-User-Role") String role,
// @RequestHeader("X-User-Id") String userId,
// @RequestParam("year") int year)
// throws ExecutionException, InterruptedException {

// if (!"ROLE_ADMIN".equalsIgnoreCase(role)) {
// return ResponseEntity.status(403).body("Bạn không có thẩm quyền");
// }

// return ResponseEntity.ok(
// orderStatisticService.statisticByYear(year));
// }
// }
