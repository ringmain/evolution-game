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

    @PostConstruct
    public void initGame() {
        if (this.tribe != null) {
            return;
        }

        List<Hominid> initialMembers = new ArrayList<>();
        ThreadLocalRandom random = ThreadLocalRandom.current();

        // Generate 3 MALEs
        for (int i = 0; i < 3; i++) {
            boolean isAlpha = (i == 0);
            initialMembers.add(createHominid("MALE", isAlpha, random));
        }

        // Generate 3 FEMALEs
        for (int i = 0; i < 3; i++) {
            initialMembers.add(createHominid("FEMALE", false, random));
        }

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

    private Hominid createHominid(String gender, boolean isAlpha, ThreadLocalRandom random) {
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
                .isPregnant(false)
                .monthsPregnancyRemaining(0)
                .build();
    }

    public Tribe getTribe() {
        return this.tribe;
    }

    public void nextTick() {
        if (this.tribe != null) {
            simulationEngine.executeTick(this.tribe);
        }
    }

    public void nextTicks(int count) {
        if (this.tribe != null) {
            for (int i = 0; i < count; i++) {
                simulationEngine.executeTick(this.tribe);
            }
        }
    }

    public void updatePriorities(Map<String, Double> newPriorities) {
        if (this.tribe != null && newPriorities != null) {
            this.tribe.setPriorities(newPriorities);
        }
    }

    /**
     * Resets the simulation to the initial starting state.
     */
    public void resetGame() {
        this.tribe = null;
        initGame(); 
    }
}