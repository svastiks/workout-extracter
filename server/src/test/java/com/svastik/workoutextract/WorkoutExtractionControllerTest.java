package com.svastik.workoutextract;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkoutExtractionControllerTest {

    @Mock
    private VideoExtractionService videoExtractionService;

    @Mock
    private VideoRepository videoRepository;

    @Mock
    private ExtractionJobRepository extractionJobRepository;

    @InjectMocks
    private WorkoutExtractionController controller;

    private Video testVideo;
    private ExtractionJob testJob;

    @BeforeEach
    void setUp() {
        testVideo = new Video();
        testVideo.setId(1L);
        testVideo.setYoutubeVideoId("dQw4w9WgXcQ");
        testVideo.setTitle("Test Video");
        testVideo.setCreator(new Creator()); // We'll need to set up creator properly

        testJob = new ExtractionJob();
        testJob.setId(UUID.randomUUID());
        testJob.setYoutubeVideoId("dQw4w9WgXcQ");
        testJob.setStatus("PENDING");
        testJob.setProgress(0);
    }

    @Test
    void testExtractWorkout_Success() {
        Map<String, String> request = new HashMap<>();
        request.put("url", "https://www.youtube.com/watch?v=dQw4w9WgXcQ");

        when(videoRepository.findByYoutubeVideoId("dQw4w9WgXcQ")).thenReturn(Optional.empty());
        when(extractionJobRepository.save(any(ExtractionJob.class))).thenReturn(testJob);
        doNothing().when(videoExtractionService).processWorkoutExtraction(any(UUID.class));

        ResponseEntity<?> response = controller.extractWorkout(request);

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Map);
        Map<?, ?> responseBody = (Map<?, ?>) response.getBody();
        assertEquals(testJob.getId(), responseBody.get("jobId"));

        verify(videoRepository).findByYoutubeVideoId("dQw4w9WgXcQ");
        verify(extractionJobRepository).save(any(ExtractionJob.class));
        verify(videoExtractionService).processWorkoutExtraction(testJob.getId());
    }

    @Test
    void testExtractWorkout_ExistingVideo() {
        Map<String, String> request = new HashMap<>();
        request.put("url", "https://www.youtube.com/watch?v=dQw4w9WgXcQ");

        when(videoRepository.findByYoutubeVideoId("dQw4w9WgXcQ")).thenReturn(Optional.of(testVideo));

        ResponseEntity<?> response = controller.extractWorkout(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testVideo, response.getBody());

        verify(videoRepository).findByYoutubeVideoId("dQw4w9WgXcQ");
        verify(extractionJobRepository, never()).save(any());
        verify(videoExtractionService, never()).processWorkoutExtraction(any());
    }

    @Test
    void testExtractWorkout_MissingUrl() {
        Map<String, String> request = new HashMap<>();

        ResponseEntity<?> response = controller.extractWorkout(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Map);
        Map<?, ?> responseBody = (Map<?, ?>) response.getBody();
        assertEquals("Missing or empty url field", responseBody.get("error"));
    }

    @Test
    void testExtractWorkout_EmptyUrl() {
        Map<String, String> request = new HashMap<>();
        request.put("url", "");

        ResponseEntity<?> response = controller.extractWorkout(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Map);
        Map<?, ?> responseBody = (Map<?, ?>) response.getBody();
        assertEquals("Missing or empty url field", responseBody.get("error"));
    }

    @Test
    void testExtractWorkout_InvalidYoutubeUrl() {
        Map<String, String> request = new HashMap<>();
        request.put("url", "https://invalid-url.com/video");

        ResponseEntity<?> response = controller.extractWorkout(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Map);
        Map<?, ?> responseBody = (Map<?, ?>) response.getBody();
        assertEquals("Invalid YouTube URL", responseBody.get("error"));
    }

    @Test
    void testExtractWorkout_VariousYoutubeUrlFormats() {
        // Test different YouTube URL formats
        String[] validUrls = {
            "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
            "https://youtu.be/dQw4w9WgXcQ",
            "https://www.youtube.com/embed/dQw4w9WgXcQ",
            "https://www.youtube.com/v/dQw4w9WgXcQ",
            "https://www.youtube.com/shorts/dQw4w9WgXcQ"
        };

        for (String url : validUrls) {
            Map<String, String> request = new HashMap<>();
            request.put("url", url);

            when(videoRepository.findByYoutubeVideoId("dQw4w9WgXcQ")).thenReturn(Optional.empty());
            when(extractionJobRepository.save(any(ExtractionJob.class))).thenReturn(testJob);
            doNothing().when(videoExtractionService).processWorkoutExtraction(any(UUID.class));

            ResponseEntity<?> response = controller.extractWorkout(request);
            assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        }
    }

    @Test
    void testGetExtractionStatus_Success() {
        UUID jobId = UUID.randomUUID();
        testJob.setId(jobId);
        testJob.setStatus("COMPLETE");
        testJob.setProgress(100);
        testJob.setResultVideoId(testVideo.getId());

        when(extractionJobRepository.findById(jobId)).thenReturn(Optional.of(testJob));
        when(videoRepository.findById(testVideo.getId())).thenReturn(Optional.of(testVideo));

        ResponseEntity<?> response = controller.getExtractionStatus(jobId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Map);
        Map<?, ?> responseBody = (Map<?, ?>) response.getBody();
        assertEquals(jobId, responseBody.get("id"));
        assertEquals("dQw4w9WgXcQ", responseBody.get("youtubeVideoId"));
        assertEquals("COMPLETE", responseBody.get("status"));
        assertEquals(100, responseBody.get("progress"));
        assertEquals(testVideo.getId(), responseBody.get("resultVideoId"));
        assertEquals("dQw4w9WgXcQ", responseBody.get("resultYoutubeVideoId"));
    }

    @Test
    void testGetExtractionStatus_JobNotFound() {
        UUID jobId = UUID.randomUUID();
        when(extractionJobRepository.findById(jobId)).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.getExtractionStatus(jobId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Map);
        Map<?, ?> responseBody = (Map<?, ?>) response.getBody();
        assertEquals("Job not found", responseBody.get("error"));
    }

    @Test
    void testGetExtractionStatus_PendingJob() {
        UUID jobId = UUID.randomUUID();
        testJob.setId(jobId);
        testJob.setStatus("PENDING");
        testJob.setProgress(50);

        when(extractionJobRepository.findById(jobId)).thenReturn(Optional.of(testJob));

        ResponseEntity<?> response = controller.getExtractionStatus(jobId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testJob, response.getBody());
    }

    @Test
    void testGetExtractionStatus_CompleteJobWithoutVideo() {
        UUID jobId = UUID.randomUUID();
        testJob.setId(jobId);
        testJob.setStatus("COMPLETE");
        testJob.setProgress(100);
        testJob.setResultVideoId(999L);

        when(extractionJobRepository.findById(jobId)).thenReturn(Optional.of(testJob));
        when(videoRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.getExtractionStatus(jobId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testJob, response.getBody());
    }

    @Test
    void testGetWorkoutByYoutubeVideoId_Success() {
        String youtubeVideoId = "test123";
        when(videoRepository.findByYoutubeVideoId(youtubeVideoId)).thenReturn(Optional.of(testVideo));

        ResponseEntity<?> response = controller.getWorkoutByYoutubeVideoId(youtubeVideoId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testVideo, response.getBody());
    }

    @Test
    void testGetWorkoutByYoutubeVideoId_NotFound() {
        String youtubeVideoId = "nonexistent";
        when(videoRepository.findByYoutubeVideoId(youtubeVideoId)).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.getWorkoutByYoutubeVideoId(youtubeVideoId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Map);
        Map<?, ?> responseBody = (Map<?, ?>) response.getBody();
        assertEquals("Video not found", responseBody.get("error"));
    }
} 