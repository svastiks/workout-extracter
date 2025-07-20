package com.svastik.workoutextract;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workouts")
public class WorkoutController {
    private final VideoExtractionService videoExtractionService;
    private final VideoRepository videoRepository;
    private final ExtractionJobRepository extractionJobRepository;

    public WorkoutController(
            VideoExtractionService videoExtractionService,
            VideoRepository videoRepository,
            ExtractionJobRepository extractionJobRepository) {
        this.videoExtractionService = videoExtractionService;
        this.videoRepository = videoRepository;
        this.extractionJobRepository = extractionJobRepository;
    }

    // Controller methods to be implemented

    @PostMapping("/extract")
    public ResponseEntity<?> extractWorkout(@RequestBody Map<String, String> request) {
        String url = request.get("url");
        if (url == null || url.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing or empty url field"));
        }
        // 1. Parse the YouTube video ID from the URL.
        String videoId = parseYoutubeVideoId(url);
        if (videoId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid YouTube URL"));
        }
        // 2. Check if a video with this ID already exists
        Optional<Video> existing = videoRepository.findByYoutubeVideoId(videoId);
        if (existing.isPresent()) {
            return ResponseEntity.ok(existing.get());
        }
        // 3. Create and save a new ExtractionJob with 'PENDING' status
        ExtractionJob job = new ExtractionJob();
        job.setYoutubeVideoId(videoId);
        job.setStatus("PENDING");
        job.setProgress(0);
        job = extractionJobRepository.save(job);
        // 4. Call the asynchronous processWorkoutExtraction(jobId)
        videoExtractionService.processWorkoutExtraction(job.getId());
        // 5. Return a response containing the jobId
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of("jobId", job.getId()));
    }

    @GetMapping("/extract/status/{jobId}")
    public ResponseEntity<?> getExtractionStatus(@PathVariable UUID jobId) {
        return extractionJobRepository.findById(jobId)
                .<ResponseEntity<?>>map(job -> ResponseEntity.ok(job))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Job not found")));
    }

    @GetMapping("/{videoId}")
    public ResponseEntity<?> getWorkoutById(@PathVariable Long videoId) {
        return videoRepository.findById(videoId)
                .<ResponseEntity<?>>map(video -> ResponseEntity.ok(video))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Video not found")));
    }

    @GetMapping("/youtube/{youtubeVideoId}")
    public ResponseEntity<?> getWorkoutByYoutubeVideoId(@PathVariable String youtubeVideoId) {
        return videoRepository.findByYoutubeVideoId(youtubeVideoId)
                .<ResponseEntity<?>>map(video -> ResponseEntity.ok(video))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Video not found")));
    }

    // Helper to parse YouTube video ID from URL
    private String parseYoutubeVideoId(String url) {
        // Simple regex for YouTube video ID extraction
        String pattern = "(?:v=|youtu.be/|embed/|v/|shorts/)([a-zA-Z0-9_-]{11})";
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile(pattern).matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
} 