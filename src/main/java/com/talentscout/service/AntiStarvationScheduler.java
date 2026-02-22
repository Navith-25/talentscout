package com.talentscout.service;

import com.talentscout.datastructure.CandidateMaxHeap;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class AntiStarvationScheduler {

    private final CandidateMaxHeap maxHeap;

    public AntiStarvationScheduler(CandidateMaxHeap maxHeap) {
        this.maxHeap = maxHeap;
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void runDecayAlgorithmAutomatically() {
        System.out.println("Running Anti-Starvation Algorithm...");
        maxHeap.applyAntiStarvationBoost();
    }
}