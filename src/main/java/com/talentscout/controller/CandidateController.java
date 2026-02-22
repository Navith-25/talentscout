package com.talentscout.controller;

import com.talentscout.datastructure.CandidateMaxHeap;
import com.talentscout.dto.BlindCandidateDTO;
import com.talentscout.model.Candidate;
import com.talentscout.repository.CandidateRepository;
import com.talentscout.service.CVParserService;
import com.talentscout.service.ScoringEngineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/candidates")
public class CandidateController {

    private final CandidateMaxHeap maxHeap;
    private final ScoringEngineService scoringEngine;
    private final CandidateRepository candidateRepository;
    private final CVParserService cvParserService;

    public CandidateController(CandidateMaxHeap maxHeap,
                               ScoringEngineService scoringEngine,
                               CandidateRepository candidateRepository,
                               CVParserService cvParserService) {
        this.maxHeap = maxHeap;
        this.scoringEngine = scoringEngine;
        this.candidateRepository = candidateRepository;
        this.cvParserService = cvParserService;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadCV(@RequestParam("file") MultipartFile file) {
        try {
            Candidate candidate = cvParserService.parseCV(file);

            scoringEngine.calculateAndSetScore(candidate);

            candidateRepository.save(candidate);

            maxHeap.insert(candidate);

            return ResponseEntity.ok("Successfully parsed and added " + candidate.getName() + "! Calculated Score: " + candidate.getSuitabilityScore());

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error parsing CV: " + e.getMessage());
        }
    }

    @PostMapping("/add")
    public ResponseEntity<String> addCandidate(@RequestBody Candidate candidate) {
        scoringEngine.calculateAndSetScore(candidate);

        candidateRepository.save(candidate);

        maxHeap.insert(candidate);

        return ResponseEntity.ok(candidate.getName() + " added permanently! Score: " + candidate.getSuitabilityScore());
    }

    @GetMapping("/best")
    public ResponseEntity<Candidate> getBestCandidate() {
        Candidate best = maxHeap.peekMax();
        if (best == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(best);
    }

    @GetMapping("/best-blind")
    public ResponseEntity<BlindCandidateDTO> getBestCandidateBlind() {
        Candidate best = maxHeap.peekMax();
        if (best == null) {
            return ResponseEntity.noContent().build();
        }

        BlindCandidateDTO blindCandidate = new BlindCandidateDTO();
        blindCandidate.setCandidateAlias("Anonymous Candidate - Confidential");
        blindCandidate.setSkills(best.getSkills());
        blindCandidate.setSuitabilityScore(best.getSuitabilityScore());
        blindCandidate.setExperienceYears(best.getExperienceYears());

        return ResponseEntity.ok(blindCandidate);
    }

    @GetMapping("/next")
    public ResponseEntity<String> removeAndGetNext() {
        Candidate removed = maxHeap.extractMax();
        if (removed == null) {
            return ResponseEntity.noContent().build();
        }

        candidateRepository.deleteById(removed.getId());

        return ResponseEntity.ok("Removed: " + removed.getName());
    }

    @GetMapping("/queue")
    public ResponseEntity<List<BlindCandidateDTO>> getQueueDetails(@RequestParam(required = false, defaultValue = "false") boolean isBlind) {
        List<Candidate> sortedQueue = maxHeap.getSortedQueue();
        List<BlindCandidateDTO> response = new ArrayList<>();

        for (int i = 0; i < sortedQueue.size(); i++) {
            Candidate c = sortedQueue.get(i);
            BlindCandidateDTO dto = new BlindCandidateDTO();
            dto.setSuitabilityScore(c.getSuitabilityScore());
            if (isBlind) {
                dto.setCandidateAlias("Anonymous Candidate #" + (i + 1));
            } else {
                dto.setCandidateAlias(c.getName());
            }
            response.add(dto);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/trigger-starvation")
    public ResponseEntity<String> triggerStarvationAlgorithm() {
        maxHeap.applyAntiStarvationBoost();
        return ResponseEntity.ok("Anti-Starvation algorithm executed successfully!");
    }
}