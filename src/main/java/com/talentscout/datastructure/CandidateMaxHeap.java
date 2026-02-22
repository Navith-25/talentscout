package com.talentscout.datastructure;

import com.talentscout.model.Candidate;
import com.talentscout.repository.CandidateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class CandidateMaxHeap {

    private final List<Candidate> heap;
    private final CandidateRepository candidateRepository;

    @Autowired
    public CandidateMaxHeap(CandidateRepository candidateRepository) {
        this.heap = new ArrayList<>();
        this.candidateRepository = candidateRepository;
    }

    private int parent(int i) { return (i - 1) / 2; }
    private int leftChild(int i) { return (2 * i) + 1; }
    private int rightChild(int i) { return (2 * i) + 2; }

    public void insert(Candidate candidate) {
        heap.add(candidate);
        int current = heap.size() - 1;

        while (current > 0 && heap.get(current).getSuitabilityScore() > heap.get(parent(current)).getSuitabilityScore()) {
            swap(current, parent(current));
            current = parent(current);
        }
    }

    public Candidate extractMax() {
        if (heap.isEmpty()) return null;
        if (heap.size() == 1) return heap.remove(0);

        Candidate bestCandidate = heap.get(0);
        heap.set(0, heap.remove(heap.size() - 1));
        heapifyDown(0);

        return bestCandidate;
    }

    public Candidate peekMax() {
        if (heap.isEmpty()) return null;
        return heap.get(0);
    }

    private void heapifyDown(int i) {
        int maxIndex = i;
        int left = leftChild(i);
        int right = rightChild(i);

        if (left < heap.size() && heap.get(left).getSuitabilityScore() > heap.get(maxIndex).getSuitabilityScore()) {
            maxIndex = left;
        }
        if (right < heap.size() && heap.get(right).getSuitabilityScore() > heap.get(maxIndex).getSuitabilityScore()) {
            maxIndex = right;
        }
        if (i != maxIndex) {
            swap(i, maxIndex);
            heapifyDown(maxIndex);
        }
    }

    private void swap(int i, int j) {
        Candidate temp = heap.get(i);
        heap.set(i, heap.get(j));
        heap.set(j, temp);
    }

    public void applyAntiStarvationBoost() {
        boolean isUpdated = false;
        LocalDate today = LocalDate.now();
        List<Candidate> updatedCandidates = new ArrayList<>();

        for (Candidate c : heap) {
            if (c.getApplicationDate() != null) {
                long daysWaiting = java.time.temporal.ChronoUnit.DAYS.between(c.getApplicationDate(), today);

                if (daysWaiting >= 7) {
                    c.setSuitabilityScore(c.getSuitabilityScore() + 5.0);
                    c.setApplicationDate(today);
                    isUpdated = true;
                    updatedCandidates.add(c);
                }
            }
        }

        if (isUpdated) {
            buildHeap();
            candidateRepository.saveAll(updatedCandidates);
        }
    }

    private void buildHeap() {
        for (int i = (heap.size() / 2) - 1; i >= 0; i--) {
            heapifyDown(i);
        }
    }
}