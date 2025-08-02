package com.svastik.workoutextract;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@Service
public class PDFExportService {

    private List<String> wrapText(String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();
        
        for (String word : words) {
            if (currentLine.length() + word.length() + 1 <= maxWidth) {
                if (currentLine.length() > 0) {
                    currentLine.append(" ");
                }
                currentLine.append(word);
            } else {
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                } else {
                    // Word is too long, add it anyway
                    lines.add(word);
                }
            }
        }
        
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }
        
        return lines;
    }

    public byte[] generateWorkoutPDF(Video video) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            try {
                int yPosition = 750;
                int leftMargin = 50;
                int rightMargin = 550;
                int pageWidth = 595; // A4 width in points
                int maxLineWidth = pageWidth - leftMargin - 50; // Leave 50pt right margin

                // Title
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
                contentStream.newLineAtOffset(leftMargin, yPosition);
                String title = "Workout: " + video.getTitle();
                List<String> wrappedTitle = wrapText(title, 50);
                contentStream.showText(wrappedTitle.get(0));
                contentStream.endText();
                yPosition -= 30;

                // Creator
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(leftMargin, yPosition);
                contentStream.showText("Created by: " + video.getCreator().getName());
                contentStream.endText();
                yPosition -= 40;

                // Workout Details
                if (video.getWorkoutData() != null && !video.getWorkoutData().isEmpty()) {
                    ObjectMapper mapper = new ObjectMapper();
                    Map<String, Object> workoutData = mapper.readValue(video.getWorkoutData(), new TypeReference<Map<String, Object>>() {});
                    
                    // Workout Type
                    if (workoutData.get("workoutType") != null) {
                        contentStream.beginText();
                        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
                        contentStream.newLineAtOffset(leftMargin, yPosition);
                        contentStream.showText("Workout Type: " + workoutData.get("workoutType"));
                        contentStream.endText();
                        yPosition -= 25;
                    }

                    // Target Muscles
                    if (workoutData.get("targetMuscles") != null) {
                        List<String> targetMuscles = (List<String>) workoutData.get("targetMuscles");
                        if (!targetMuscles.isEmpty()) {
                            String musclesText = "Target Muscles: " + String.join(", ", targetMuscles);
                            List<String> wrappedMuscles = wrapText(musclesText, 60);
                            
                            for (String line : wrappedMuscles) {
                                contentStream.beginText();
                                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
                                contentStream.newLineAtOffset(leftMargin, yPosition);
                                contentStream.showText(line);
                                contentStream.endText();
                                yPosition -= 20;
                            }
                            yPosition -= 5;
                        }
                    }

                    // Equipment
                    if (workoutData.get("equipment") != null) {
                        List<String> equipment = (List<String>) workoutData.get("equipment");
                        if (!equipment.isEmpty()) {
                            String equipmentText = "Equipment: " + String.join(", ", equipment);
                            List<String> wrappedEquipment = wrapText(equipmentText, 60);
                            
                            for (String line : wrappedEquipment) {
                                contentStream.beginText();
                                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
                                contentStream.newLineAtOffset(leftMargin, yPosition);
                                contentStream.showText(line);
                                contentStream.endText();
                                yPosition -= 20;
                            }
                            yPosition -= 20;
                        }
                    }

                    // Exercises
                    if (workoutData.get("exercises") != null) {
                        contentStream.beginText();
                        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
                        contentStream.newLineAtOffset(leftMargin, yPosition);
                        contentStream.showText("Exercises:");
                        contentStream.endText();
                        yPosition -= 30;

                        List<Map<String, Object>> exercises = (List<Map<String, Object>>) workoutData.get("exercises");
                        for (int i = 0; i < exercises.size(); i++) {
                            Map<String, Object> exercise = exercises.get(i);
                            
                            // Check if we need a new page
                            if (yPosition < 100) {
                                contentStream.close();
                                page = new PDPage();
                                document.addPage(page);
                                contentStream = new PDPageContentStream(document, page);
                                yPosition = 750;
                            }

                            // Exercise name
                            contentStream.beginText();
                            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                            contentStream.newLineAtOffset(leftMargin, yPosition);
                            String exerciseName = (i + 1) + ". " + exercise.get("name");
                            if (exerciseName.length() > 70) {
                                exerciseName = exerciseName.substring(0, 67) + "...";
                            }
                            contentStream.showText(exerciseName);
                            contentStream.endText();
                            yPosition -= 20;

                            // Exercise details
                            contentStream.beginText();
                            contentStream.setFont(PDType1Font.HELVETICA, 10);
                            contentStream.newLineAtOffset(leftMargin + 20, yPosition);
                            String details = String.format("Sets: %s | Reps: %s | Rest: %s",
                                exercise.get("sets") != null ? exercise.get("sets") : "N/A",
                                exercise.get("reps") != null ? exercise.get("reps") : "N/A",
                                exercise.get("rest") != null ? exercise.get("rest") : "N/A");
                            contentStream.showText(details);
                            contentStream.endText();
                            yPosition -= 20;

                            // Exercise notes
                            if (exercise.get("notes") != null && !exercise.get("notes").toString().isEmpty()) {
                                String notes = "Notes: " + exercise.get("notes");
                                List<String> wrappedNotes = wrapText(notes, 60);
                                
                                for (String line : wrappedNotes) {
                                    // Check if we need a new page
                                    if (yPosition < 100) {
                                        contentStream.close();
                                        page = new PDPage();
                                        document.addPage(page);
                                        contentStream = new PDPageContentStream(document, page);
                                        yPosition = 750;
                                    }
                                    
                                    contentStream.beginText();
                                    contentStream.setFont(PDType1Font.HELVETICA, 9);
                                    contentStream.newLineAtOffset(leftMargin + 20, yPosition);
                                    contentStream.showText(line);
                                    contentStream.endText();
                                    yPosition -= 15;
                                }
                            }

                            yPosition -= 10; // Extra spacing between exercises
                        }
                    }
                }

                // Footer
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 8);
                contentStream.newLineAtOffset(leftMargin, 50);
                contentStream.showText("Generated by WorkoutExtract - " + java.time.LocalDate.now());
                contentStream.endText();
            } finally {
                contentStream.close();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        }
    }
} 