package com.talentscout.service;

import com.talentscout.model.Candidate;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ScoringEngineService {

    private final List<String> requiredSkills = List.of("Java", "Spring Boot", "React", "MySQL", "AWS");

    public void calculateAndSetScore(Candidate candidate) {
        double skillScore = calculateSkillRelevance(candidate.getSkills());
        double experienceScore = calculateExperienceWeight(candidate.getExperienceYears());
        double educationScore = calculateEducationScore(candidate.getEducationLevel());
        double projectScore = calculateProjectScore(candidate.getProjectCount());

        double finalScore = (skillScore * 0.35) + (educationScore * 0.25) + (experienceScore * 0.25) + (projectScore * 0.15);

        candidate.setSuitabilityScore(Math.round(finalScore * 100.0) / 100.0);
    }

    private double calculateSkillRelevance(List<String> candidateSkills) {
        if (candidateSkills == null || candidateSkills.isEmpty()) return 0.0;
        long matchCount = candidateSkills.stream()
                .filter(skill -> requiredSkills.stream().anyMatch(req -> req.equalsIgnoreCase(skill)))
                .count();
        return ((double) matchCount / requiredSkills.size()) * 100.0;
    }

    private double calculateExperienceWeight(double years) {
        if (years >= 5.0) return 100.0;
        if (years >= 3.0) return 80.0;
        if (years >= 1.0) return 50.0;
        return 20.0;
    }

    private double calculateEducationScore(String education) {
        if (education == null) return 0.0;
        return switch (education.toUpperCase()) {
            case "PHD", "MSC" -> 100.0;
            case "BSC", "BENG" -> 85.0;
            case "HND", "DIPLOMA" -> 50.0;
            default -> 20.0;
        };
    }

    private double calculateProjectScore(int projectCount) {
        if (projectCount >= 5) return 100.0;
        if (projectCount >= 3) return 80.0;
        if (projectCount >= 1) return 50.0;
        return 10.0;
    }
}