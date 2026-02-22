package com.talentscout.config;

import com.talentscout.datastructure.CandidateMaxHeap;
import com.talentscout.model.Candidate;
import com.talentscout.repository.CandidateRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class DataLoader implements CommandLineRunner {

    private final CandidateRepository candidateRepository;
    private final CandidateMaxHeap maxHeap;

    public DataLoader(CandidateRepository candidateRepository, CandidateMaxHeap maxHeap) {
        this.candidateRepository = candidateRepository;
        this.maxHeap = maxHeap;
    }

    @Override
    public void run(String... args) throws Exception {
        List<Candidate> candidates = candidateRepository.findAll();
        for (Candidate candidate : candidates) {
            maxHeap.insert(candidate);
        }

        System.out.println("✅ Successfully loaded " + candidates.size() + " candidates from MySQL to Max-Heap!");
    }
}