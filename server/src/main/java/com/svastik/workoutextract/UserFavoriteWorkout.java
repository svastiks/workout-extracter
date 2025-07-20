package com.svastik.workoutextract;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_favorite_workouts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserFavoriteWorkout {
    @EmbeddedId
    private UserFavoriteWorkoutId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("videoId")
    @JoinColumn(name = "video_id", insertable = false, updatable = false)
    private Video video;
} 