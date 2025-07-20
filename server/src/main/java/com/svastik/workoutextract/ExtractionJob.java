package com.svastik.workoutextract;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "extraction_jobs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExtractionJob {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "youtube_video_id", nullable = false)
    private String youtubeVideoId;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private int progress;

    @Column(name = "result_video_id")
    private Long resultVideoId;

    @Column(name = "error_message")
    private String errorMessage;

    public UUID getId() {
        return id;
    }
} 