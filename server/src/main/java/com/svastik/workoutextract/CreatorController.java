package com.svastik.workoutextract;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/creators")
public class CreatorController {
    private final CreatorRepository creatorRepository;
    private final VideoRepository videoRepository;

    public CreatorController(CreatorRepository creatorRepository, VideoRepository videoRepository) {
        this.creatorRepository = creatorRepository;
        this.videoRepository = videoRepository;
    }

    @GetMapping
    public List<CreatorDTO> getAllCreators() {
        return creatorRepository.findAll().stream()
            .map(CreatorDTO::new)
            .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCreatorById(@PathVariable Long id) {
        Optional<Creator> creator = creatorRepository.findById(id);
        return creator
            .<ResponseEntity<?>>map(c -> ResponseEntity.ok(new CreatorDTO(c)))
            .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Creator not found"));
    }

    @GetMapping("/{id}/videos")
    public ResponseEntity<List<Video>> getVideosByCreatorId(@PathVariable Long id) {
        List<Video> videos = videoRepository.findAllByCreatorId(id);
        return ResponseEntity.ok(videos);
    }
} 