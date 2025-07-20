package com.svastik.workoutextract;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface VideoRepository extends JpaRepository<Video, Long> {
    Optional<Video> findByYoutubeVideoId(String youtubeVideoId);
    List<Video> findAllByCreatorId(Long creatorId);
} 