package com.evolution.service;

import com.evolution.model.Hominid;
import com.evolution.model.Species;
import com.evolution.model.Tribe;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class TribeService {

    private final SimulationEngine simulationEngine;
    private Tribe tribe;

    /**
     * Initializes the game state upon application startup if it doesn't already exist.
     */
    @PostConstruct
    public void initGame() {
        if (this.tribe != null) {
            return;
        }

        List<Hominid> initialMembers = new ArrayList<>();
        ThreadLocalRandom random = ThreadLocalRandom.current();

        // Generate 3 MALEs
        for (int i = 0; i < 3; i++) {
            boolean isAlpha = (i == 0); // Exactly ONE male is alpha
            initialMembers.add(createHominid("MALE", isAlpha, random));
        }

        // Generate 3 FEMALEs
        for (int i = 0; i < 3; i++) {
            initialMembers.add(createHominid("FEMALE", false, random));
        }

        // Set default priorities
        Map<String, Double> defaultPriorities = new HashMap<>();
        defaultPriorities.put("SBER", 0.5);
        defaultPriorities.put("LOV", 0.5);
        defaultPriorities.put("STRAZ", 0.0);
        defaultPriorities.put("VYZKUM", 0.0);

        this.tribe = Tribe.builder()
                .members(initialMembers)
                .priorities(defaultPriorities)
                .currentSeason("MONSOON")
                .build();
    }

    /**
     * Helper method to generate an individual hominid with initial default stats.
     */
    private Hominid createHominid(String gender, boolean isAlpha, ThreadLocalRandom random) {
        // Random age between 180 and 240 months (15-20 years)
        int ageInMonths = random.nextInt(180, 241);

        return Hominid.builder()
                .id(UUID.randomUUID())
                .species(Species.CHIMPANZEE)
                .gender(gender)
                .ageInMonths(ageInMonths)
                .health(100.0)
                .satiety(100.0)
                .visibleScleraPercentage(0.0)
                .isAlpha(isAlpha)
                .isBlockedMother(false)
                .monthsBlockedRemaining(0)
                .build();
    }

    /**
     * Returns the current state of the tribe.
     */
    public Tribe getTribe() {
        return this.tribe;
    }

    /**
     * Triggers a single simulation tick (1 month) and updates the tribe's state.
     */
    public void nextTick() {
        if (this.tribe != null) {
            simulationEngine.executeTick(this.tribe);
        }
    }

    /**
     * Updates the tribe's work priorities.
     */
    public void updatePriorities(Map<String, Double> newPriorities) {
        if (this.tribe != null && newPriorities != null) {
            this.tribe.setPriorities(newPriorities);
        }
    }
}