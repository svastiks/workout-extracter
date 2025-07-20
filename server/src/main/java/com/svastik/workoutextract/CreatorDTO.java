package com.svastik.workoutextract;

public class CreatorDTO {
    private Long id;
    private String name;
    private String youtubeChannelId;
    private String profileImageUrl;

    public CreatorDTO(Creator creator) {
        this.id = creator.getId();
        this.name = creator.getName();
        this.youtubeChannelId = creator.getYoutubeChannelId();
        this.profileImageUrl = creator.getProfileImageUrl();
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getYoutubeChannelId() { return youtubeChannelId; }
    public String getProfileImageUrl() { return profileImageUrl; }
} 