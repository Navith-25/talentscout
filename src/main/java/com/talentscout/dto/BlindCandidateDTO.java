package com.talentscout.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BlindCandidateDTO {
    private String candidateAlias;
    private List<String> skills;
    private Double suitabilityScore;
    private Double experienceYears;
}