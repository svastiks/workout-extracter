package com.svastik.workoutextract;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class WorkoutextractApplication {
    public static void main(String[] args) {
        SpringApplication.run(WorkoutextractApplication.class, args);
    }
}
