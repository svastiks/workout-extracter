package com.svastik.workoutextract;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class RepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private CreatorRepository creatorRepository;

    @Autowired
    private ExtractionJobRepository extractionJobRepository;

    private Creator testCreator;
    private Video testVideo;
    private ExtractionJob testJob;

    @BeforeEach
    void setUp() {
        testCreator = new Creator();
        testCreator.setName("Test Creator");
        testCreator.setYoutubeChannelId("test_channel_123");
        testCreator.setProfileImageUrl("https://example.com/profile.jpg");
        testCreator = entityManager.persistAndFlush(testCreator);

        testVideo = new Video();
        testVideo.setYoutubeVideoId("test123");
        testVideo.setTitle("Test Video");
        testVideo.setThumbnailUrl("https://example.com/thumbnail.jpg");
        testVideo.setCreator(testCreator);
        testVideo.setWorkoutData("{\"exercises\": []}");
        testVideo = entityManager.persistAndFlush(testVideo);

        testJob = new ExtractionJob();
        testJob.setYoutubeVideoId("test123");
        testJob.setStatus("PENDING");
        testJob.setProgress(0);
        testJob = entityManager.persistAndFlush(testJob);
    }

    @Test
    void testVideoRepository_FindByYoutubeVideoId() {
        Optional<Video> found = videoRepository.findByYoutubeVideoId("test123");
        
        assertTrue(found.isPresent());
        assertEquals("test123", found.get().getYoutubeVideoId());
        assertEquals("Test Video", found.get().getTitle());
        assertEquals(testCreator.getId(), found.get().getCreator().getId());
    }

    @Test
    void testVideoRepository_FindByYoutubeVideoId_NotFound() {
        Optional<Video> found = videoRepository.findByYoutubeVideoId("nonexistent");
        
        assertFalse(found.isPresent());
    }

    @Test
    void testVideoRepository_FindAllByCreatorId() {
        Video secondVideo = new Video();
        secondVideo.setYoutubeVideoId("test456");
        secondVideo.setTitle("Second Test Video");
        secondVideo.setCreator(testCreator);
        entityManager.persistAndFlush(secondVideo);

        // Finding all videos by creator ID
        List<Video> videos = videoRepository.findAllByCreatorId(testCreator.getId());
        
        assertEquals(2, videos.size());
        assertTrue(videos.stream().anyMatch(v -> v.getYoutubeVideoId().equals("test123")));
        assertTrue(videos.stream().anyMatch(v -> v.getYoutubeVideoId().equals("test456")));
    }

    @Test
    void testVideoRepository_FindAllByCreatorId_Empty() {
        List<Video> videos = videoRepository.findAllByCreatorId(999L);
        
        assertTrue(videos.isEmpty());
    }

    @Test
    void testVideoRepository_Save() {
        Video newVideo = new Video();
        newVideo.setYoutubeVideoId("new123");
        newVideo.setTitle("New Video");
        newVideo.setCreator(testCreator);
        
        Video saved = videoRepository.save(newVideo);
        
        assertNotNull(saved.getId());
        assertEquals("new123", saved.getYoutubeVideoId());
        assertEquals("New Video", saved.getTitle());
    }

    @Test
    void testVideoRepository_FindById() {
        Optional<Video> found = videoRepository.findById(testVideo.getId());
        
        assertTrue(found.isPresent());
        assertEquals(testVideo.getId(), found.get().getId());
        assertEquals("test123", found.get().getYoutubeVideoId());
    }

    @Test
    void testVideoRepository_FindAll() {
        Video secondVideo = new Video();
        secondVideo.setYoutubeVideoId("test456");
        secondVideo.setTitle("Second Video");
        secondVideo.setCreator(testCreator);
        entityManager.persistAndFlush(secondVideo);

        List<Video> videos = videoRepository.findAll();
        
        assertTrue(videos.size() >= 2);
        assertTrue(videos.stream().anyMatch(v -> v.getYoutubeVideoId().equals("test123")));
        assertTrue(videos.stream().anyMatch(v -> v.getYoutubeVideoId().equals("test456")));
    }

    @Test
    void testCreatorRepository_FindById() {
        Optional<Creator> found = creatorRepository.findById(testCreator.getId());
        
        assertTrue(found.isPresent());
        assertEquals(testCreator.getId(), found.get().getId());
        assertEquals("Test Creator", found.get().getName());
        assertEquals("test_channel_123", found.get().getYoutubeChannelId());
    }

    @Test
    void testCreatorRepository_FindAll() {
        Creator secondCreator = new Creator();
        secondCreator.setName("Second Creator");
        secondCreator.setYoutubeChannelId("test_channel_456");
        entityManager.persistAndFlush(secondCreator);

        List<Creator> creators = creatorRepository.findAll();
        
        assertTrue(creators.size() >= 2);
        assertTrue(creators.stream().anyMatch(c -> c.getName().equals("Test Creator")));
        assertTrue(creators.stream().anyMatch(c -> c.getName().equals("Second Creator")));
    }

    @Test
    void testCreatorRepository_Save() {
        Creator newCreator = new Creator();
        newCreator.setName("New Creator");
        newCreator.setYoutubeChannelId("new_channel_123");
        
        Creator saved = creatorRepository.save(newCreator);
        
        assertNotNull(saved.getId());
        assertEquals("New Creator", saved.getName());
        assertEquals("new_channel_123", saved.getYoutubeChannelId());
    }

    @Test
    void testExtractionJobRepository_FindById() {
        Optional<ExtractionJob> found = extractionJobRepository.findById(testJob.getId());
        
        assertTrue(found.isPresent());
        assertEquals(testJob.getId(), found.get().getId());
        assertEquals("test123", found.get().getYoutubeVideoId());
        assertEquals("PENDING", found.get().getStatus());
    }

    @Test
    void testExtractionJobRepository_FindAll() {
        ExtractionJob secondJob = new ExtractionJob();
        secondJob.setYoutubeVideoId("test456");
        secondJob.setStatus("COMPLETE");
        secondJob.setProgress(100);
        entityManager.persistAndFlush(secondJob);

        List<ExtractionJob> jobs = extractionJobRepository.findAll();
        
        assertTrue(jobs.size() >= 2);
        assertTrue(jobs.stream().anyMatch(j -> j.getYoutubeVideoId().equals("test123")));
        assertTrue(jobs.stream().anyMatch(j -> j.getYoutubeVideoId().equals("test456")));
    }

    @Test
    void testExtractionJobRepository_Save() {
        ExtractionJob newJob = new ExtractionJob();
        newJob.setYoutubeVideoId("new123");
        newJob.setStatus("PENDING");
        newJob.setProgress(0);
        
        ExtractionJob saved = extractionJobRepository.save(newJob);
        
        assertNotNull(saved.getId());
        assertEquals("new123", saved.getYoutubeVideoId());
        assertEquals("PENDING", saved.getStatus());
    }

    @Test
    void testExtractionJobRepository_Update() {
        testJob.setStatus("COMPLETE");
        testJob.setProgress(100);
        testJob.setResultVideoId(testVideo.getId());
        
        ExtractionJob updated = extractionJobRepository.save(testJob);
        
        assertEquals("COMPLETE", updated.getStatus());
        assertEquals(100, updated.getProgress());
        assertEquals(testVideo.getId(), updated.getResultVideoId());
    }

    @Test
    void testVideoRepository_UniqueConstraint() {
        Video duplicateVideo = new Video();
        duplicateVideo.setYoutubeVideoId("test123"); // Same as existing
        duplicateVideo.setTitle("Duplicate Video");
        duplicateVideo.setCreator(testCreator);

        assertThrows(Exception.class, () -> {
            entityManager.persistAndFlush(duplicateVideo);
        });
    }

    @Test
    void testCreatorRepository_UniqueConstraint() {
        Creator duplicateCreator = new Creator();
        duplicateCreator.setName("Duplicate Creator");
        duplicateCreator.setYoutubeChannelId("test_channel_123"); // Same as existing
        
        assertThrows(Exception.class, () -> {
            entityManager.persistAndFlush(duplicateCreator);
        });
    }
} 