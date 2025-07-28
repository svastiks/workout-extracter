package com.svastik.workoutextract;

import org.springframework.data.jpa.repository.JpaRepository;
 
public interface UserFavoriteWorkoutRepository extends JpaRepository<UserFavoriteWorkout, UserFavoriteWorkoutId> {
} 