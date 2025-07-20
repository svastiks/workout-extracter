package com.svastik.workoutextract;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "videos", uniqueConstraints = {
    @UniqueConstraint(columnNames = "youtube_video_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Video {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "youtube_video_id", unique = true, nullable = false)
    private String youtubeVideoId;

    private String title;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @ManyToOne(optional = false)
    @JoinColumn(name = "creator_id", nullable = false)
    private Creator creator;

    @Column(name = "workout_data", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String workoutData;
} 