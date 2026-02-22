package com.talentscout.service;

import com.talentscout.model.Candidate;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CVParserService {

    private final List<String> KNOWN_SKILLS = List.of("Java", "Spring Boot", "React", "MySQL", "AWS", "Python", "JavaScript", "SQL", "HTML", "CSS");

    public Candidate parseCV(MultipartFile file) throws Exception {
        try (InputStream inputStream = file.getInputStream();
             PDDocument document = PDDocument.load(inputStream)) {

            PDFTextStripper pdfStripper = new PDFTextStripper();
            String cvText = pdfStripper.getText(document);

            Candidate candidate = new Candidate();

            candidate.setId("C" + (int)(Math.random() * 10000));

            String cleanName = file.getOriginalFilename().replace(".pdf", "").replace("_", " ");
            candidate.setName(cleanName);

            candidate.setSkills(extractSkills(cvText));

            candidate.setExperienceYears(extractExperience(cvText));

            candidate.setEducationLevel(extractEducation(cvText));

            return candidate;
        }
    }

    private List<String> extractSkills(String text) {
        List<String> foundSkills = new ArrayList<>();
        String upperText = text.toUpperCase();
        for (String skill : KNOWN_SKILLS) {
            if (upperText.contains(skill.toUpperCase())) {
                foundSkills.add(skill);
            }
        }
        return foundSkills;
    }

    private Double extractExperience(String text) {
        Pattern pattern = Pattern.compile("(\\d+)(\\.\\d+)?\\s+(years|yrs)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return Double.parseDouble(matcher.group(1));
        }
        return 0.5;
    }

    private String extractEducation(String text) {
        String upperText = text.toUpperCase();
        if (upperText.contains("BSC") || upperText.contains("BACHELOR")) return "BSc";
        if (upperText.contains("MSC") || upperText.contains("MASTER")) return "MSc";
        if (upperText.contains("HND") || upperText.contains("HIGHER NATIONAL DIPLOMA")) return "HND";
        return "Other";
    }
}