package com.hmkeyewear.blog_service.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.stream.IntStream;

@Service
public class StressService {

    @Async
    public void runStress() {

        long duration = 10 * 60 * 1000;
        long endTime = System.currentTimeMillis() + duration;

        int threads = Runtime.getRuntime().availableProcessors() * 4;

        IntStream.range(0, threads).parallel().forEach(t -> {

            double result = 0;

            while (System.currentTimeMillis() < endTime) {
                result += Math.sqrt(Math.random()) * Math.tan(System.nanoTime());
            }

            System.out.println("Thread done: " + result);
        });
    }
}
