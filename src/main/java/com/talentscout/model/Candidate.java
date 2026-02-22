package com.talentscout.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "candidates")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Candidate {

    @Id
    private String id;
    private String name;

    private String email;
    private String phone;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> skills;

    private Double experienceYears;
    private String educationLevel;
    private int projectCount;
    private Double suitabilityScore;

    private LocalDate applicationDate = LocalDate.now();
}