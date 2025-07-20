package com.svastik.workoutextract;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "creators", uniqueConstraints = {
    @UniqueConstraint(columnNames = "youtube_channel_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Creator {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "youtube_channel_id", unique = true, nullable = false)
    private String youtubeChannelId;

    @Column(name = "profile_image_url")
    private String profileImageUrl;
} 