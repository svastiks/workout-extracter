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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/v1/workouts")
public class WorkoutExtractionController {
    private static final Logger logger = LoggerFactory.getLogger(WorkoutExtractionController.class);
    private final VideoExtractionService videoExtractionService;
    private final VideoRepository videoRepository;
    private final ExtractionJobRepository extractionJobRepository;

    public WorkoutExtractionController(
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
        logger.info("[Extract] Received extraction request for URL: {}", request.get("url"));
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
        logger.info("[Extract] Extraction job created with ID: {}", job.getId());
        // 4. Call the asynchronous processWorkoutExtraction(jobId)
        videoExtractionService.processWorkoutExtraction(job.getId());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of("jobId", job.getId()));
    }

    @GetMapping("/extract/status/{jobId}")
    public ResponseEntity<?> getExtractionStatus(@PathVariable UUID jobId) {
        logger.info("[Extract] Checking status for job ID: {}", jobId);
        return extractionJobRepository.findById(jobId)
                .<ResponseEntity<?>>map(job -> {
                    logger.info("[Extract] Job status: {}", job.getStatus());
                    return ResponseEntity.ok(job);
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Job not found")));
    }



    @GetMapping("/{youtubeVideoId}")
    public ResponseEntity<?> getWorkoutByYoutubeVideoId(@PathVariable String youtubeVideoId) {
        return videoRepository.findByYoutubeVideoId(youtubeVideoId)
                .<ResponseEntity<?>>map(video -> ResponseEntity.ok(video))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Video not found")));
    }

    private String parseYoutubeVideoId(String url) {
        logger.info("[Extract] Attempting to parse YouTube video ID from URL: {}", url);
        // Simple regex for YouTube video ID extraction
        String pattern = "(?:v=|youtu.be/|embed/|v/|shorts/)([a-zA-Z0-9_-]{11})";
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile(pattern).matcher(url);
        if (matcher.find()) {
            String videoId = matcher.group(1);
            logger.info("[Extract] Extracted YouTube video ID: {}", videoId);
            return videoId;
        }
        logger.warn("[Extract] Could not extract YouTube video ID from URL: {}", url);
        return null;
    }
} 