package com.talentscout.service;

import com.talentscout.model.Candidate;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CVParserService {

    private final List<String> KNOWN_SKILLS = Arrays.asList(
            "Java", "Spring Boot", "React", "MySQL", "AWS", "Python", "JavaScript",
            "TypeScript", "SQL", "HTML", "CSS", "Node.js", "MongoDB", "Docker",
            "Kubernetes", "Git", "C++", "C#", "PHP", "Machine Learning", "IoT", "ESP32",
            "Angular", "Vue.js", "Django", "Flask", "REST API", "GraphQL", "PostgreSQL",
            "Firebase", "Redis", "Jenkins", "CI/CD", "Azure", "GCP", "Microservices",
            "Tailwind", "Bootstrap", "Redux", "Express", "JUnit", "Selenium", "Agile"
    );

    private final List<String> JUNK_HEADERS = Arrays.asList(
            "curriculum", "resume", "cv", "contact", "profile", "objective",
            "experience", "summary", "education", "skills", "address", "phone", "email",
            "developer", "engineer", "professional", "personal", "details", "information",
            "page", "mobile", "linkedin", "github"
    );

    public Candidate parseCV(MultipartFile file) throws Exception {
        try (InputStream inputStream = file.getInputStream();
             PDDocument document = PDDocument.load(inputStream)) {

            PDFTextStripper pdfStripper = new PDFTextStripper();
            String cvText = pdfStripper.getText(document);

            Candidate candidate = new Candidate();
            candidate.setId("C" + (int)(Math.random() * 10000));

            candidate.setName(extractRealName(cvText, file.getOriginalFilename()));

            candidate.setEmail(extractEmail(cvText));
            candidate.setPhone(extractPhone(cvText));

            candidate.setSkills(extractSkills(cvText));
            candidate.setExperienceYears(extractExperience(cvText));
            candidate.setEducationLevel(extractEducation(cvText));
            candidate.setProjectCount(extractProjects(cvText));

            return candidate;
        }
    }

    private String extractEmail(String text) {
        Matcher m = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}").matcher(text);
        return m.find() ? m.group() : "Not Detected";
    }

    private String extractPhone(String text) {
        Matcher m = Pattern.compile("(\\+\\d{1,3}[- ]?)?\\d{9,10}").matcher(text);
        return m.find() ? m.group() : "Not Detected";
    }

    private String extractRealName(String text, String fallbackName) {
        String[] lines = text.split("\\r?\\n");

        Pattern nameKeywordPattern = Pattern.compile("(?i)(Full Name|Name|Candidate Name|Applicant Name)\\s*[:\\-]?\\s*([a-zA-Z\\s]{3,40})");
        for (int i = 0; i < Math.min(lines.length, 25); i++) {
            Matcher m = nameKeywordPattern.matcher(lines[i]);
            if (m.find()) {
                String extracted = m.group(2).trim();
                if (extracted.length() > 2) return extracted;
            }
        }

        int emailLine = -1;
        for (int i = 0; i < Math.min(lines.length, 15); i++) {
            if (lines[i].contains("@")) {
                emailLine = i;
                break;
            }
        }
        if (emailLine > 0) {
            for (int j = emailLine - 1; j >= Math.max(0, emailLine - 3); j--) {
                String line = lines[j].trim();
                if (isValidName(line)) return line;
            }
        }

        for (int i = 0; i < Math.min(lines.length, 10); i++) {
            String line = lines[i].trim();
            if (isValidName(line)) return line;
        }

        return fallbackName.replace(".pdf", "").replace("_", " ");
    }

    private boolean isValidName(String line) {
        if (line.length() < 3 || line.length() > 40) return false;
        String lowerLine = line.toLowerCase();

        for (String junk : JUNK_HEADERS) {
            if (lowerLine.contains(junk)) return false;
        }

        if (!line.matches("^[a-zA-Z\\s\\.]+$")) return false;

        String[] words = line.split("\\s+");
        for (String word : words) {
            if (word.length() > 0 && !Character.isUpperCase(word.charAt(0))) return false;
        }

        return true;
    }

    private int extractProjects(String text) {
        int count = 0;
        String lowerText = text.toLowerCase();
        if (lowerText.contains("github.com") || lowerText.contains("gitlab.com") || lowerText.contains("bitbucket.org")) {
            count += 2;
        }
        Pattern pattern = Pattern.compile("\\b(project|developed|built|created|designed|portfolio|implemented)\\b", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        int keywordHits = 0;
        while (matcher.find()) {
            keywordHits++;
        }
        count += (keywordHits / 2);
        return Math.min(count, 8);
    }

    private List<String> extractSkills(String text) {
        List<String> foundSkills = new ArrayList<>();
        for (String skill : KNOWN_SKILLS) {
            Pattern pattern = Pattern.compile("\\b" + Pattern.quote(skill) + "\\b", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                foundSkills.add(skill);
            }
        }
        return foundSkills;
    }

    private Double extractExperience(String text) {
        Pattern totalExpPattern = Pattern.compile("(?i)total\\s+experience\\s*[:\\-]?\\s*(\\d+)(\\.\\d+)?", Pattern.CASE_INSENSITIVE);
        Matcher mTotal = totalExpPattern.matcher(text);
        if (mTotal.find()) {
            return Double.parseDouble(mTotal.group(1) + (mTotal.group(2) != null ? mTotal.group(2) : ""));
        }

        double maxExp = 0.0;
        Pattern pattern1 = Pattern.compile("(\\d+)(\\.\\d+)?\\+?\\s*(years|yrs)", Pattern.CASE_INSENSITIVE);
        Matcher matcher1 = pattern1.matcher(text);
        while (matcher1.find()) {
            double exp = Double.parseDouble(matcher1.group(1) + (matcher1.group(2) != null ? matcher1.group(2) : ""));
            if (exp > maxExp && exp < 40) maxExp = exp;
        }
        return maxExp > 0 ? (Math.round(maxExp * 10.0) / 10.0) : 0.5;
    }

    private String extractEducation(String text) {
        if (Pattern.compile("\\b(PHD|DOCTORATE|DPHIL)\\b", Pattern.CASE_INSENSITIVE).matcher(text).find()) return "PhD";
        if (Pattern.compile("\\b(MSC|MASTER|MASTERS|M\\.TECH|MBA)\\b", Pattern.CASE_INSENSITIVE).matcher(text).find()) return "MSc";
        if (Pattern.compile("\\b(BSC|BACHELOR|BACHELORS|BENG|B\\.SC|B\\.TECH|BBA)\\b", Pattern.CASE_INSENSITIVE).matcher(text).find()) return "BSc";
        if (Pattern.compile("\\b(HND|HIGHER NATIONAL DIPLOMA|DIPLOMA|ADVANCED DIPLOMA)\\b", Pattern.CASE_INSENSITIVE).matcher(text).find()) return "HND";
        return "Other";
    }
}