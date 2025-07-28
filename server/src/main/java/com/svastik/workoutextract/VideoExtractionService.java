package com.svastik.workoutextract;

import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.UUID;
import java.util.Optional;
import java.io.IOException;
import java.util.Map;

@Service
public class VideoExtractionService {
    private final VideoRepository videoRepository;
    private final CreatorRepository creatorRepository;
    private final ExtractionJobRepository extractionJobRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final Logger logger = LoggerFactory.getLogger(VideoExtractionService.class);

    public VideoExtractionService(
            VideoRepository videoRepository,
            CreatorRepository creatorRepository,
            ExtractionJobRepository extractionJobRepository,
            RestTemplate restTemplate,
            ObjectMapper objectMapper) {
        this.videoRepository = videoRepository;
        this.creatorRepository = creatorRepository;
        this.extractionJobRepository = extractionJobRepository;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    // Service methods to be implemented

    @Async
    public void processWorkoutExtraction(UUID jobId) {
        try {
            logger.info("[Extract] processWorkoutExtraction called for jobId: {}", jobId);

            Optional<ExtractionJob> jobOpt = extractionJobRepository.findById(jobId);
            if (jobOpt.isEmpty()) {
                logger.error("ExtractionJob not found for id: {}", jobId);
                return;
            }
            logger.info("[Extract] ExtractionJob found for id: {}", jobId);

            ExtractionJob job = jobOpt.get();
            job.setStatus("FETCHING");
            job.setProgress(10);
            extractionJobRepository.save(job);
            logger.info("[Extract] Job status set to FETCHING and progress to 10");

            Optional<Video> existingVideo = videoRepository.findByYoutubeVideoId(job.getYoutubeVideoId());
            if (existingVideo.isPresent()) {
                job.setStatus("COMPLETE");
                job.setProgress(100);
                job.setResultVideoId(existingVideo.get().getId());
                extractionJobRepository.save(job);
                logger.info("Existing video found for youtubeVideoId {}. Marking job {} as COMPLETE.", job.getYoutubeVideoId(), jobId);
                return;
            }
            logger.info("[Extract] No existing video found, proceeding with extraction");

            // yt-dlp step
            logger.info("[Extract] Running yt-dlp command...");
            String youtubeVideoId = job.getYoutubeVideoId();
            String url = "https://www.youtube.com/watch?v=" + youtubeVideoId;
            
            // First command: Get metadata JSON
            String metadataCommand = String.format(
                "yt-dlp --dump-json --skip-download \"%s\"",
                url
            );
            logger.info("[Extract] yt-dlp metadata command: {}", metadataCommand);
            String ytDlpOutput = executeShellCommand(metadataCommand);
            logger.info("[Extract] yt-dlp metadata command executed. Output length: {}", ytDlpOutput.length());
            
            // Second command: Get transcript and comments files
            String filesCommand = String.format(
                "yt-dlp --write-auto-sub --sub-lang en --write-comments --skip-download --output \"%s.%%(ext)s\" \"%s\"",
                youtubeVideoId, url
            );
            logger.info("[Extract] yt-dlp files command: {}", filesCommand);
            String filesOutput = executeShellCommand(filesCommand);
            logger.info("[Extract] yt-dlp files command executed. Output length: {}", filesOutput.length());

            // Parse yt-dlp output
            logger.info("[Extract] Parsing yt-dlp output...");
            Map<String, Object> videoJson = objectMapper.readValue(ytDlpOutput, Map.class);
            String title = (String) videoJson.get("title");
            String channelId = (String) videoJson.get("channel_id");
            String uploader = (String) videoJson.get("uploader");
            String thumbnail = (String) videoJson.get("thumbnail");
            logger.info("[Extract] yt-dlp output parsed - Title: {}, Channel: {}, Uploader: {}", title, channelId, uploader);

            // Read comments and transcript
            logger.info("[Extract] Reading comments and transcript...");
            
            // List all files in current directory to see what yt-dlp created
            java.io.File[] allFiles = new java.io.File(".").listFiles();
            if (allFiles != null) {
                logger.info("[Extract] Files in current directory:");
                for (java.io.File file : allFiles) {
                    if (file.getName().contains(youtubeVideoId)) {
                        logger.info("[Extract] Found file: {} (size: {} bytes)", file.getName(), file.length());
                    }
                }
            }
            
            String commentsFile = youtubeVideoId + ".comments.json";
            java.io.File comments = new java.io.File(commentsFile);
            java.util.List<Map<String, Object>> commentsList = null;
            if (comments.exists()) {
                commentsList = objectMapper.readValue(comments, java.util.List.class);
                logger.info("[Extract] Comments file found with {} comments", commentsList.size());
            } else {
                logger.warn("[Extract] Comments file not found: {}", commentsFile);
            }

            String transcriptFile = youtubeVideoId + ".en.vtt";
            java.io.File transcript = new java.io.File(transcriptFile);
            String rawTranscriptString = null;
            if (transcript.exists()) {
                rawTranscriptString = java.nio.file.Files.readString(transcript.toPath());
                logger.info("[Extract] Transcript file found with {} characters", rawTranscriptString.length());
            } else {
                // Try .en.json as fallback
                transcriptFile = youtubeVideoId + ".en.json";
                transcript = new java.io.File(transcriptFile);
                if (transcript.exists()) {
                    rawTranscriptString = java.nio.file.Files.readString(transcript.toPath());
                    logger.info("[Extract] Transcript file found (JSON) with {} characters", rawTranscriptString.length());
                } else {
                    logger.warn("[Extract] No transcript file found: {} or {}", youtubeVideoId + ".en.vtt", transcriptFile);
                }
            }
            logger.info("[Extract] Comments and transcript read");

            // Clean transcript and find golden comments
            logger.info("[Extract] Cleaning transcript and finding golden comments...");
            String cleanedTranscript = cleanTranscript(rawTranscriptString);
            java.util.List<String> goldenComments = findGoldenComments(commentsList);
            logger.info("[Extract] Cleaned transcript length: {}, Golden comments found: {}", 
                cleanedTranscript != null ? cleanedTranscript.length() : 0, goldenComments.size());
            
            // Log the actual content for debugging
            if (cleanedTranscript != null && !cleanedTranscript.trim().isEmpty()) {
                logger.info("[Extract] First 500 chars of cleaned transcript: {}", 
                    cleanedTranscript.substring(0, Math.min(500, cleanedTranscript.length())));
            } else {
                logger.warn("[Extract] Cleaned transcript is null or empty");
            }
            
            if (!goldenComments.isEmpty()) {
                logger.info("[Extract] First golden comment: {}", goldenComments.get(0));
            } else {
                logger.warn("[Extract] No golden comments found");
            }

            // 1. Update the job status to ANALYZING_WORKOUT and progress to 75.
            job.setStatus("ANALYZING_WORKOUT");
            job.setProgress(75);
            extractionJobRepository.save(job);

            String goldenCommentsText = String.join("\n", goldenComments);
            StringBuilder promptBuilder = new StringBuilder();
            promptBuilder.append("You are an expert fitness data extractor. Your task is to analyze the provided video transcript and user comments to create a complete, structured workout plan in JSON format.\n\n")
                .append("**Instructions:**\n")
                .append("1.  **Prioritize User Comments:** The user comments are highly likely to contain the precise sets and reps. Use them as the primary source for the 'exercises' array.\n")
                .append("2.  **Use Transcript for Context:** Use the video transcript to fill in missing details, such as exercise names, detailed notes, workout type, and target muscles.\n")
                .append("3.  **Handle Missing Data:** If a specific value for 'reps', 'sets', or 'rest' cannot be found in any source, the value in the JSON MUST be `null`. Do not guess or make up values.\n")
                .append("4.  **Be Conservative:** Only include exercises where you can confidently identify the exercise name. If you're unsure about an exercise, omit it.\n")
                .append("5.  **Output ONLY the JSON object.** Do not include any other text or explanations.\n\n")
                .append("**JSON Schema to follow:**\n")
                .append("{\n")
                .append("  \"equipment\": [\"List of strings\"],\n")
                .append("  \"exercises\": [\n")
                .append("    {\n")
                .append("      \"name\": \"String\",\n")
                .append("      \"reps\": \"String or null\",\n")
                .append("      \"rest\": \"String or null\",\n")
                .append("      \"sets\": \"String or null\",\n")
                .append("      \"emoji\": \"String (a single relevant emoji)\",\n")
                .append("      \"notes\": \"String (detailed notes on form and execution)\",\n")
                .append("      \"difficulty\": \"String (Easy, Medium, or Hard)\"\n")
                .append("    }\n")
                .append("  ],\n")
                .append("  \"workoutType\": \"String\",\n")
                .append("  \"targetMuscles\": [\"List of strings\"]\n")
                .append("}\n\n")
                .append("---\n")
                .append("**DATA STARTS HERE**\n\n")
                .append("**[User Comments - High Priority for Sets/Reps]:**\n")
                .append(goldenCommentsText).append("\n\n")
                .append("**[Video Transcript - For Context and Notes]:**\n")
                .append(cleanedTranscript);
            String prompt = promptBuilder.toString();
            logger.info("[Extract] LLM prompt constructed. Length: {}", prompt.length());
            logger.info("[Extract] Prompt preview (first 1000 chars): {}", 
                prompt.substring(0, Math.min(1000, prompt.length())));

            // Call LLM API
            logger.info("[Extract] Calling LLM API...");
            String apiKey = System.getenv("GOOGLE_API_KEY");
            if (apiKey == null || apiKey.trim().isEmpty()) {
                throw new RuntimeException("GOOGLE_API_KEY environment variable is not set");
            }
            String llmApiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey;
            java.util.Map<String, Object> requestBody = java.util.Map.of(
                "contents", java.util.List.of(
                    java.util.Map.of(
                        "parts", java.util.List.of(
                            java.util.Map.of("text", prompt)
                        )
                    )
                )
            );
            logger.info("[Extract] LLM API URL: {}", llmApiUrl);
            logger.info("[Extract] LLM API request body size: {}", objectMapper.writeValueAsString(requestBody).length());
            logger.info("[Extract] LLM API key present: {}", apiKey != null && !apiKey.isEmpty());
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("Content-Type", "application/json");
            org.springframework.http.HttpEntity<java.util.Map<String, Object>> entity = new org.springframework.http.HttpEntity<>(requestBody, headers);
            try {
                String llmResponse = restTemplate.postForObject(llmApiUrl, entity, String.class);
                logger.info("[Extract] LLM API response received. Length: {}", llmResponse != null ? llmResponse.length() : 0);
                logger.info("[Extract] LLM API response: {}", llmResponse);
                
                // Parse the LLM response to extract the actual JSON
                String extractedJson = extractJsonFromLlmResponse(llmResponse);
                logger.info("[Extract] Extracted JSON from LLM response. Length: {}", extractedJson != null ? extractedJson.length() : 0);
                logger.info("[Extract] Extracted JSON: {}", extractedJson);
                
                // Validate the extracted JSON
                try {
                    Map<String, Object> parsedJson = objectMapper.readValue(extractedJson, Map.class);
                    logger.info("[Extract] JSON parsed successfully");
                    logger.info("[Extract] Parsed JSON structure: {}", parsedJson.keySet());
                    
                    // Process exercises and add fallback values with transparency flags
                    if (parsedJson.containsKey("exercises")) {
                        java.util.List<Map<String, Object>> exercises = (java.util.List<Map<String, Object>>) parsedJson.get("exercises");
                        logger.info("[Extract] Exercises array size: {}", exercises.size());
                        
                        // Process each exercise to add fallback values and transparency flags
                        for (Map<String, Object> exercise : exercises) {
                            processExerciseWithFallbacks(exercise);
                        }
                        
                        if (exercises.isEmpty()) {
                            logger.warn("[Extract] WARNING: Exercises array is empty!");
                        } else {
                            // Stage 2: Send back to LLM to fill missing values with reasonable estimates
                            logger.info("[Extract] Stage 2: Sending to LLM for missing value estimation");
                            String estimatedJson = estimateMissingValues(extractedJson, prompt);
                            if (estimatedJson != null && !estimatedJson.equals(extractedJson)) {
                                // Parse the estimated JSON and use it as our final result
                                Map<String, Object> estimatedData = objectMapper.readValue(estimatedJson, Map.class);
                                parsedJson = estimatedData; // Replace with estimated data
                                logger.info("[Extract] Stage 2 completed with estimated values");
                                
                                // Re-process the estimated JSON to ensure transparency flags are correct
                                java.util.List<Map<String, Object>> estimatedExercises = (java.util.List<Map<String, Object>>) estimatedData.get("exercises");
                                for (Map<String, Object> exercise : estimatedExercises) {
                                    // Re-apply transparency flags based on whether values were originally null
                                    // We need to check if the value was originally null and is now filled
                                    reapplyTransparencyFlags(exercise);
                                }
                                logger.info("[Extract] Re-processed estimated JSON with transparency flags");
                            } else {
                                logger.info("[Extract] Stage 2: No changes made by estimation");
                            }
                        }
                    }
                    
                    if (parsedJson.containsKey("equipment")) {
                        java.util.List<?> equipment = (java.util.List<?>) parsedJson.get("equipment");
                        logger.info("[Extract] Equipment array size: {}", equipment.size());
                    }
                    
                    if (parsedJson.containsKey("targetMuscles")) {
                        java.util.List<?> targetMuscles = (java.util.List<?>) parsedJson.get("targetMuscles");
                        logger.info("[Extract] Target muscles array size: {}", targetMuscles.size());
                    }
                    
                    // Convert final processed JSON to string for persistence
                    extractedJson = objectMapper.writeValueAsString(parsedJson);
                    logger.info("[Extract] Final processed JSON: {}", extractedJson);
                    
                } catch (Exception e) {
                    logger.error("[Extract] Failed to parse extracted JSON: {}", e.getMessage());
                }
                
                // Persist results
                logger.info("[Extract] Persisting extraction results...");
                // 1. Find or create the Creator entity using channelId and uploader name from yt-dlp data
                Creator creator = creatorRepository.findAll().stream()
                    .filter(c -> channelId.equals(c.getYoutubeChannelId()))
                    .findFirst()
                    .orElseGet(() -> {
                        Creator newCreator = Creator.builder()
                            .youtubeChannelId(channelId)
                            .name(uploader)
                            .profileImageUrl(thumbnail)
                            .build();
                        return creatorRepository.save(newCreator);
                    });

                // 2. Create a new Video entity
                Video video = Video.builder()
                    .title(title)
                    .youtubeVideoId(youtubeVideoId)
                    .thumbnailUrl(thumbnail)
                    .creator(creator)
                    .workoutData(extractedJson != null ? extractedJson : llmResponse)
                    .build();

                // 4. Save the new Video entity to the database
                video = videoRepository.save(video);
                logger.info("[Extract] Video saved with ID: {}", video.getId());

                // 5. Update the ExtractionJob status to COMPLETE, progress to 100, and set result_video_id
                job.setStatus("COMPLETE");
                job.setProgress(100);
                job.setResultVideoId(video.getId());
                extractionJobRepository.save(job);
                logger.info("[Extract] Extraction results persisted successfully");
            } catch (Exception e) {
                logger.error("[Extract] LLM API call failed", e);
                throw e;
            }

        } catch (Exception e) {
            logger.error("[Extract] Exception in processWorkoutExtraction", e);
            extractionJobRepository.findById(jobId).ifPresent(job -> {
                job.setStatus("FAILED");
                job.setErrorMessage(e.getMessage());
                extractionJobRepository.save(job);
            });
        }
    }

    @Async
    public void beginExtractionProcess(UUID jobId) {
        try {
            // 1. Find the ExtractionJob by its jobId. Update its status to FETCHING_DATA and progress to 10.
            Optional<ExtractionJob> jobOpt = extractionJobRepository.findById(jobId);
            if (jobOpt.isEmpty()) {
                logger.error("ExtractionJob not found for id: {}", jobId);
                return;
            }
            ExtractionJob job = jobOpt.get();
            job.setStatus("FETCHING_DATA");
            job.setProgress(10);
            extractionJobRepository.save(job);

            String youtubeVideoId = job.getYoutubeVideoId();
            String url = "https://www.youtube.com/watch?v=" + youtubeVideoId;

            // 2. Construct and execute yt-dlp shell command
            String command = String.format(
                "yt-dlp --dump-json --write-auto-sub --sub-lang en --write-comments --skip-download \"%s\"",
                url
            );
            String ytDlpOutput = executeShellCommand(command);
            logger.info("[Extract] yt-dlp command executed");

            // 3. Parse the JSON output from the command's stdout
            Map<String, Object> videoJson = objectMapper.readValue(ytDlpOutput, Map.class);
            String title = (String) videoJson.get("title");
            String channelId = (String) videoJson.get("channel_id");
            String uploader = (String) videoJson.get("uploader");
            String thumbnail = (String) videoJson.get("thumbnail");

            String commentsFile = youtubeVideoId + ".comments.json";
            java.io.File comments = new java.io.File(commentsFile);
            java.util.List<Map<String, Object>> commentsList = null;
            if (comments.exists()) {
                commentsList = objectMapper.readValue(comments, java.util.List.class);
            }

            String transcriptFile = youtubeVideoId + ".en.vtt";
            java.io.File transcript = new java.io.File(transcriptFile);
            String rawTranscriptString = null;
            if (transcript.exists()) {
                rawTranscriptString = java.nio.file.Files.readString(transcript.toPath());
            } else {
                // Try .en.json as fallback
                transcriptFile = youtubeVideoId + ".en.json";
                transcript = new java.io.File(transcriptFile);
                if (transcript.exists()) {
                    rawTranscriptString = java.nio.file.Files.readString(transcript.toPath());
                }
            }

            // 6. Delete temporary files
            if (comments.exists()) comments.delete();
            if (transcript.exists()) transcript.delete();
            logger.info("[Extract] Comments and transcript processed");

            // Clean up any other temporary files created by yt-dlp
            java.io.File[] tempFiles = new java.io.File(".").listFiles((dir, name) -> name.startsWith(youtubeVideoId));
            if (tempFiles != null) {
                for (java.io.File file : tempFiles) {
                    logger.info("[Extract] Cleaning up temporary file: {}", file.getName());
                    file.delete();
                }
            }

            // --- New logic: Clean transcript and find golden comments ---
            String cleanedTranscript = cleanTranscript(rawTranscriptString);
            java.util.List<String> goldenComments = findGoldenComments(commentsList);

            // 1. Update the job status to ANALYZING_WORKOUT and progress to 75.
            job.setStatus("ANALYZING_WORKOUT");
            job.setProgress(75);
            extractionJobRepository.save(job);

            String goldenCommentsText = String.join("\n", goldenComments);
            StringBuilder promptBuilder = new StringBuilder();
            promptBuilder.append("You are an expert fitness data extractor. Your task is to analyze the provided video transcript and user comments to create a complete, structured workout plan in JSON format.\n\n")
                .append("**Instructions:**\n")
                .append("1.  **Prioritize User Comments:** The user comments are highly likely to contain the precise sets and reps. Use them as the primary source for the 'exercises' array.\n")
                .append("2.  **Use Transcript for Context:** Use the video transcript to fill in missing details, such as exercise names, detailed notes, workout type, and target muscles.\n")
                .append("3.  **Handle Missing Data:** If a specific value for 'reps', 'sets', or 'rest' cannot be found in any source, the value in the JSON MUST be `null`. Do not guess or make up values.\n")
                .append("4.  **Be Conservative:** Only include exercises where you can confidently identify the exercise name. If you're unsure about an exercise, omit it.\n")
                .append("5.  **Output ONLY the JSON object.** Do not include any other text or explanations.\n\n")
                .append("**JSON Schema to follow:**\n")
                .append("{\n")
                .append("  \"equipment\": [\"List of strings\"],\n")
                .append("  \"exercises\": [\n")
                .append("    {\n")
                .append("      \"name\": \"String\",\n")
                .append("      \"reps\": \"String or null\",\n")
                .append("      \"rest\": \"String or null\",\n")
                .append("      \"sets\": \"String or null\",\n")
                .append("      \"emoji\": \"String (a single relevant emoji)\",\n")
                .append("      \"notes\": \"String (detailed notes on form and execution)\",\n")
                .append("      \"difficulty\": \"String (Easy, Medium, or Hard)\"\n")
                .append("    }\n")
                .append("  ],\n")
                .append("  \"workoutType\": \"String\",\n")
                .append("  \"targetMuscles\": [\"List of strings\"]\n")
                .append("}\n\n")
                .append("---\n")
                .append("**DATA STARTS HERE**\n\n")
                .append("**[User Comments - High Priority for Sets/Reps]:**\n")
                .append(goldenCommentsText).append("\n\n")
                .append("**[Video Transcript - For Context and Notes]:**\n")
                .append(cleanedTranscript);
            String prompt = promptBuilder.toString();

            // 3. Use RestTemplate to make the POST request to the Google LLM API endpoint
            String llmApiUrl = "https://your-google-llm-endpoint.com/v1/generate"; // Replace with actual endpoint
            java.util.Map<String, String> requestBody = java.util.Map.of("prompt", prompt);
            String apiKey = System.getenv("GOOGLE_API_KEY");
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("Authorization", "Bearer " + apiKey);
            headers.set("Content-Type", "application/json");
            org.springframework.http.HttpEntity<java.util.Map<String, String>> entity = new org.springframework.http.HttpEntity<>(requestBody, headers);
            String llmResponse = restTemplate.postForObject(llmApiUrl, entity, String.class);
            logger.info("[Extract] LLM API called");

            // 4. The llmResponse should be the JSON string from the LLM
            // --- Final Persistence Logic ---
            // 1. Find or create the Creator entity using channelId and uploader name from yt-dlp data
            Creator creator = creatorRepository.findAll().stream()
                .filter(c -> channelId.equals(c.getYoutubeChannelId()))
                .findFirst()
                .orElseGet(() -> {
                    Creator newCreator = Creator.builder()
                        .youtubeChannelId(channelId)
                        .name(uploader)
                        .profileImageUrl(thumbnail)
                        .build();
                    return creatorRepository.save(newCreator);
                });

            // 2. Create a new Video entity
            Video video = Video.builder()
                .title(title)
                .youtubeVideoId(youtubeVideoId)
                .thumbnailUrl(thumbnail)
                .creator(creator)
                .workoutData(llmResponse)
                .build();

            // 4. Save the new Video entity to the database
            video = videoRepository.save(video);

            // 5. Update the ExtractionJob status to COMPLETE, progress to 100, and set result_video_id
            job.setStatus("COMPLETE");
            job.setProgress(100);
            job.setResultVideoId(video.getId());
            extractionJobRepository.save(job);
            logger.info("[Extract] Extraction results persisted for job ID: {}", jobId);

        } catch (Exception e) {
            logger.error("Error processing ExtractionJob {}: {}", jobId, e.getMessage(), e);
            extractionJobRepository.findById(jobId).ifPresent(job -> {
                job.setStatus("FAILED");
                job.setErrorMessage(e.getMessage());
                extractionJobRepository.save(job);
            });
        }
    }

    private String executeShellCommand(String command) {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", "-c", command);
        StringBuilder output = new StringBuilder();
        try {
            Process process = processBuilder.start();
            try (java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append(System.lineSeparator());
                }
            }
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                logger.error("Shell command exited with code {}: {}", exitCode, command);
            }
        } catch (IOException | InterruptedException e) {
            logger.error("Error executing shell command '{}': {}", command, e.getMessage(), e);
            Thread.currentThread().interrupt();
        }
        return output.toString();
    }

    private String cleanTranscript(String rawTranscript) {
        if (rawTranscript == null) return null;
        // Remove timestamps (e.g., 00:01:23.456 --> 00:01:25.789)
        String cleaned = rawTranscript.replaceAll("(?m)^\\d{2}:\\d{2}:\\d{2}\\.\\d{3} --> \\d{2}:\\d{2}:\\d{2}\\.\\d{3}.*$", "");
        // Remove speaker tags (e.g., [Speaker 1], (Speaker), etc.)
        cleaned = cleaned.replaceAll("\\[.*?\\]|\\(.*?\\)", "");
        // Remove common filler words
        cleaned = cleaned.replaceAll("\\b(like|um|uh|you know)\\b", "");
        // Remove long pauses (e.g., [pause], ...)
        cleaned = cleaned.replaceAll("\\[pause\\]|\\.\\.\\.", "");
        // Remove extra whitespace and empty lines
        cleaned = cleaned.replaceAll("(?m)^\\s*$", "");
        cleaned = cleaned.replaceAll("\\s+", " ").trim();
        return cleaned;
    }

    private java.util.List<String> findGoldenComments(java.util.List<java.util.Map<String, Object>> commentsList) {
        if (commentsList == null) return java.util.Collections.emptyList();
        java.util.List<String> golden = new java.util.ArrayList<>();
        // 1. Prioritize pinned comments
        commentsList.stream()
            .filter(c -> Boolean.TRUE.equals(c.get("pinned")))
            .map(c -> (String) c.get("text"))
            .filter(this::isWorkoutComment)
            .limit(3)
            .forEach(golden::add);
        // 2. If less than 3, add high-like comments with workout keywords
        if (golden.size() < 3) {
            commentsList.stream()
                .filter(c -> !Boolean.TRUE.equals(c.get("pinned")))
                .sorted((a, b) -> Integer.compare((int) b.getOrDefault("like_count", 0), (int) a.getOrDefault("like_count", 0)))
                .map(c -> (String) c.get("text"))
                .filter(this::isWorkoutComment)
                .filter(text -> !golden.contains(text))
                .limit(3 - golden.size())
                .forEach(golden::add);
        }
        return golden;
    }

    private boolean isWorkoutComment(String text) {
        if (text == null) return false;
        String lower = text.toLowerCase();
        // Keywords and regex for sets x reps
        return lower.contains("sets") || lower.contains("reps") || lower.contains("routine") ||
               lower.matches(".*\\d+x\\d+.*");
    }

    private String extractJsonFromLlmResponse(String llmResponse) {
        if (llmResponse == null || llmResponse.trim().isEmpty()) {
            logger.warn("[Extract] LLM response is null or empty");
            return null;
        }
        
        try {
            // Parse the LLM response structure
            Map<String, Object> responseMap = objectMapper.readValue(llmResponse, Map.class);
            
            // Navigate through the response structure
            if (responseMap.containsKey("candidates")) {
                java.util.List<?> candidates = (java.util.List<?>) responseMap.get("candidates");
                if (!candidates.isEmpty()) {
                    Map<String, Object> firstCandidate = (Map<String, Object>) candidates.get(0);
                    if (firstCandidate.containsKey("content")) {
                        Map<String, Object> content = (Map<String, Object>) firstCandidate.get("content");
                        if (content.containsKey("parts")) {
                            java.util.List<?> parts = (java.util.List<?>) content.get("parts");
                            if (!parts.isEmpty()) {
                                Map<String, Object> firstPart = (Map<String, Object>) parts.get(0);
                                if (firstPart.containsKey("text")) {
                                    String text = (String) firstPart.get("text");
                                    logger.info("[Extract] Extracted text from LLM response: {}", text);
                                    
                                    // Try to extract JSON from the text
                                    return extractJsonFromText(text);
                                }
                            }
                        }
                    }
                }
            }
            
            logger.warn("[Extract] Could not parse LLM response structure");
            return null;
            
        } catch (Exception e) {
            logger.error("[Extract] Failed to parse LLM response: {}", e.getMessage());
            return null;
        }
    }
    
    private String extractJsonFromText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        
        // Look for JSON in markdown code blocks
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("```(?:json)?\\s*(\\{.*?\\})\\s*```", java.util.regex.Pattern.DOTALL);
        java.util.regex.Matcher matcher = pattern.matcher(text);
        
        if (matcher.find()) {
            String json = matcher.group(1);
            logger.info("[Extract] Found JSON in markdown code block");
            return json;
        }
        
        // Look for JSON without markdown
        java.util.regex.Pattern jsonPattern = java.util.regex.Pattern.compile("\\{.*\\}", java.util.regex.Pattern.DOTALL);
        java.util.regex.Matcher jsonMatcher = jsonPattern.matcher(text);
        
        if (jsonMatcher.find()) {
            String json = jsonMatcher.group(0);
            logger.info("[Extract] Found JSON without markdown");
            return json;
        }
        
        logger.warn("[Extract] No JSON found in text");
        return null;
    }

    private void processExerciseWithFallbacks(Map<String, Object> exercise) {
        if (exercise == null) return;

        java.util.function.Predicate<Object> isNullValue = (value) -> {
            if (value == null) return true;
            String strValue = value.toString().trim();
            return strValue.isEmpty() || "null".equalsIgnoreCase(strValue) || "undefined".equalsIgnoreCase(strValue);
        };

        // Fallback for reps
        if (isNullValue.test(exercise.get("reps"))) {
            exercise.put("reps", null); // Mark as missing with transparency
            exercise.put("reps_transparency", "missing");
            logger.info("[Extract] Exercise '{}': reps marked as missing", exercise.get("name"));
        } else {
            exercise.put("reps_transparency", "present");
            logger.info("[Extract] Exercise '{}': reps found in video", exercise.get("name"));
        }

        // Fallback for sets
        if (isNullValue.test(exercise.get("sets"))) {
            exercise.put("sets", null); // Mark as missing with transparency
            exercise.put("sets_transparency", "missing");
            logger.info("[Extract] Exercise '{}': sets marked as missing", exercise.get("name"));
        } else {
            exercise.put("sets_transparency", "present");
            logger.info("[Extract] Exercise '{}': sets found in video", exercise.get("name"));
        }

        // Fallback for rest
        if (isNullValue.test(exercise.get("rest"))) {
            exercise.put("rest", null); // Mark as missing with transparency
            exercise.put("rest_transparency", "missing");
            logger.info("[Extract] Exercise '{}': rest marked as missing", exercise.get("name"));
        } else {
            exercise.put("rest_transparency", "present");
            logger.info("[Extract] Exercise '{}': rest found in video", exercise.get("name"));
        }
    }

    private String estimateMissingValues(String currentJson, String originalPrompt) {
        try {
            // Parse the current JSON to identify missing values
            Map<String, Object> currentData = objectMapper.readValue(currentJson, Map.class);
            java.util.List<Map<String, Object>> exercises = (java.util.List<Map<String, Object>>) currentData.get("exercises");
            
            if (exercises == null || exercises.isEmpty()) {
                return currentJson;
            }

            // Build a list of exercises with missing values
            java.util.List<String> missingExercises = new java.util.ArrayList<>();
            for (int i = 0; i < exercises.size(); i++) {
                Map<String, Object> exercise = exercises.get(i);
                java.util.List<String> missing = new java.util.ArrayList<>();
                
                // Check for missing values using the same logic as processExerciseWithFallbacks
                java.util.function.Predicate<Object> isNullValue = (value) -> {
                    if (value == null) return true;
                    String strValue = value.toString().trim();
                    return strValue.isEmpty() || "null".equalsIgnoreCase(strValue) || "undefined".equalsIgnoreCase(strValue);
                };
                
                Object repsValue = exercise.get("reps");
                Object setsValue = exercise.get("sets");
                Object restValue = exercise.get("rest");
                
                logger.info("[Extract] Exercise '{}': reps={}, sets={}, rest={}", 
                    exercise.get("name"), repsValue, setsValue, restValue);
                
                if (isNullValue.test(repsValue)) missing.add("reps");
                if (isNullValue.test(setsValue)) missing.add("sets");
                if (isNullValue.test(restValue)) missing.add("rest");
                
                if (!missing.isEmpty()) {
                    String exerciseName = (String) exercise.get("name");
                    missingExercises.add(String.format("Exercise %d (%s): missing %s", 
                        i + 1, exerciseName, String.join(", ", missing)));
                    logger.info("[Extract] Found missing values for exercise '{}': {}", exerciseName, missing);
                }
            }

            if (missingExercises.isEmpty()) {
                logger.info("[Extract] No missing values to estimate");
                return currentJson;
            }

            // Create the estimation prompt
            StringBuilder estimationPrompt = new StringBuilder();
            estimationPrompt.append("You are a fitness expert. I have extracted workout data from a video, but some values are missing. ")
                .append("Please provide reasonable estimates for the missing values based on your knowledge of fitness and exercise science.\n\n")
                .append("**Instructions:**\n")
                .append("1. Only fill in the missing values (reps, sets, rest) that are currently 'null'\n")
                .append("2. Use your knowledge of exercise science to provide realistic estimates\n")
                .append("3. For reps: Use realistic ranges like '8-12', '10-15', or 'till failure' for high-intensity exercises\n")
                .append("4. For sets: Use realistic ranges like '3-4', '4-5', or '3' for most exercises\n")
                .append("5. For rest: Use realistic times like '60s', '90s', '2-3 min' based on exercise intensity\n")
                .append("6. Consider the exercise type, difficulty, and typical workout patterns\n")
                .append("7. Keep the transparency flags as 'missing' to indicate these are estimates\n")
                .append("8. Return ONLY the JSON object with the estimated values filled in\n\n")
                .append("**Missing values to estimate:**\n")
                .append(String.join("\n", missingExercises)).append("\n\n")
                .append("**Current workout data:**\n")
                .append(currentJson);

            logger.info("[Extract] Estimation prompt length: {}", estimationPrompt.length());

            // Call LLM for estimation
            String apiKey = System.getenv("GOOGLE_API_KEY");
            if (apiKey == null || apiKey.trim().isEmpty()) {
                throw new RuntimeException("GOOGLE_API_KEY environment variable is not set");
            }
            String llmApiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey;
            java.util.Map<String, Object> requestBody = java.util.Map.of(
                "contents", java.util.List.of(
                    java.util.Map.of(
                        "parts", java.util.List.of(
                            java.util.Map.of("text", estimationPrompt.toString())
                        )
                    )
                )
            );

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("Content-Type", "application/json");
            org.springframework.http.HttpEntity<java.util.Map<String, Object>> entity = new org.springframework.http.HttpEntity<>(requestBody, headers);
            
            String llmResponse = restTemplate.postForObject(llmApiUrl, entity, String.class);
            logger.info("[Extract] LLM estimation response received. Length: {}", llmResponse != null ? llmResponse.length() : 0);
            
            // Extract JSON from response
            String estimatedJson = extractJsonFromLlmResponse(llmResponse);
            if (estimatedJson != null) {
                logger.info("[Extract] Successfully extracted estimated JSON");
                return estimatedJson;
            } else {
                logger.warn("[Extract] Failed to extract estimated JSON, returning original");
                return currentJson;
            }

        } catch (Exception e) {
            logger.error("[Extract] Error in estimateMissingValues: {}", e.getMessage());
            return currentJson;
        }
    }

    private void reapplyTransparencyFlags(Map<String, Object> exercise) {
        if (exercise == null) return;

        java.util.function.Predicate<Object> isNullValue = (value) -> {
            if (value == null) return true;
            String strValue = value.toString().trim();
            return strValue.isEmpty() || "null".equalsIgnoreCase(strValue) || "undefined".equalsIgnoreCase(strValue);
        };

        // For estimated values, we need to determine which values were originally null
        // and are now filled in by the LLM. These should be marked as "missing"
        
        // Check if reps was originally null but now has a value
        if (!isNullValue.test(exercise.get("reps")) && !exercise.containsKey("reps_transparency")) {
            // Reps has a value but no transparency flag - this means it was estimated
            exercise.put("reps_transparency", "missing");
            logger.info("[Extract] Exercise '{}': reps was estimated, setting transparency to 'missing'", exercise.get("name"));
        } else if (!isNullValue.test(exercise.get("reps")) && exercise.containsKey("reps_transparency")) {
            // Reps has a value and already has a transparency flag - keep it
            logger.info("[Extract] Exercise '{}': reps transparency preserved as '{}'", exercise.get("name"), exercise.get("reps_transparency"));
        }

        // Check if sets was originally null but now has a value
        if (!isNullValue.test(exercise.get("sets")) && !exercise.containsKey("sets_transparency")) {
            // Sets has a value but no transparency flag - this means it was estimated
            exercise.put("sets_transparency", "missing");
            logger.info("[Extract] Exercise '{}': sets was estimated, setting transparency to 'missing'", exercise.get("name"));
        } else if (!isNullValue.test(exercise.get("sets")) && exercise.containsKey("sets_transparency")) {
            // Sets has a value and already has a transparency flag - keep it
            logger.info("[Extract] Exercise '{}': sets transparency preserved as '{}'", exercise.get("name"), exercise.get("sets_transparency"));
        }

        // Check if rest was originally null but now has a value
        if (!isNullValue.test(exercise.get("rest")) && !exercise.containsKey("rest_transparency")) {
            // Rest has a value but no transparency flag - this means it was estimated
            exercise.put("rest_transparency", "missing");
            logger.info("[Extract] Exercise '{}': rest was estimated, setting transparency to 'missing'", exercise.get("name"));
        } else if (!isNullValue.test(exercise.get("rest")) && exercise.containsKey("rest_transparency")) {
            // Rest has a value and already has a transparency flag - keep it
            logger.info("[Extract] Exercise '{}': rest transparency preserved as '{}'", exercise.get("name"), exercise.get("rest_transparency"));
        }

        // If any value is still null, mark it as missing
        if (isNullValue.test(exercise.get("reps"))) {
            exercise.put("reps_transparency", "missing");
        }
        if (isNullValue.test(exercise.get("sets"))) {
            exercise.put("sets_transparency", "missing");
        }
        if (isNullValue.test(exercise.get("rest"))) {
            exercise.put("rest_transparency", "missing");
        }
    }
} 