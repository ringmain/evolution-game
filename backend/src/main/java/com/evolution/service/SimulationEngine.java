package com.evolution.service;

import com.evolution.model.Hominid;
import com.evolution.model.Species;
import com.evolution.model.Tribe;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class SimulationEngine {

    /**
     * Executes a single simulation tick representing 1 month of game time.
     */
    public void executeTick(Tribe tribe) {
        if (tribe == null || tribe.getMembers() == null || tribe.getMembers().isEmpty()) {
            return;
        }

        List<Hominid> members = tribe.getMembers();
        List<Hominid> newBirths = new ArrayList<>();
        
        // 1. Hierarchical Food Distribution: Alphas eat first
        members.sort(Comparator.comparing(Hominid::isAlpha).reversed());

        double totalFoodAvailable = calculateMonthlyFoodProduction(tribe);

        Iterator<Hominid> iterator = members.iterator();
        while (iterator.hasNext()) {
            Hominid hominid = iterator.next();

            // 2. Aging
            hominid.setAgeInMonths(hominid.getAgeInMonths() + 1);

            // 3. Mother Blockage Counter
            if (hominid.isBlockedMother()) {
                hominid.setMonthsBlockedRemaining(hominid.getMonthsBlockedRemaining() - 1);
                if (hominid.getMonthsBlockedRemaining() <= 0) {
                    hominid.setBlockedMother(false);
                    hominid.setMonthsBlockedRemaining(0);
                }
            }

            // 4. Pregnancy Lifecycle
            if (hominid.isPregnant()) {
                hominid.setMonthsPregnancyRemaining(hominid.getMonthsPregnancyRemaining() - 1);
                
                // Birth event
                if (hominid.getMonthsPregnancyRemaining() <= 0) {
                    hominid.setPregnant(false);
                    hominid.setMonthsPregnancyRemaining(0);
                    hominid.setBlockedMother(true);
                    hominid.setMonthsBlockedRemaining(36); // 3 years of care

                    String newbornGender = ThreadLocalRandom.current().nextBoolean() ? "MALE" : "FEMALE";
                    Hominid newborn = Hominid.builder()
                            .id(UUID.randomUUID())
                            .species(Species.CHIMPANZEE)
                            .gender(newbornGender)
                            .ageInMonths(0)
                            .health(100.0)
                            .satiety(100.0)
                            .visibleScleraPercentage(0.0)
                            .isAlpha(false)
                            .isBlockedMother(false)
                            .monthsBlockedRemaining(0)
                            .isPregnant(false)
                            .monthsPregnancyRemaining(0)
                            .build();
                            
                    newBirths.add(newborn);
                }
            } else if ("FEMALE".equals(hominid.getGender()) && 
                       hominid.getAgeInMonths() >= 144 && 
                       hominid.getAgeInMonths() <= 420 && 
                       !hominid.isBlockedMother() && 
                       hominid.getSatiety() >= 90.0) {
                
                // 2% chance to become pregnant
                if (ThreadLocalRandom.current().nextDouble() < 0.02) {
                    hominid.setPregnant(true);
                    hominid.setMonthsPregnancyRemaining(8); // Chimpanzee pregnancy is ~8 months
                }
            }

            // 5. Food Consumption & Recovery/Starvation Logic
            double foodRequired = (hominid.getLifeStage() == Hominid.LifeStage.INFANT) ? 0.5 : 1.0;

            if (totalFoodAvailable >= foodRequired) {
                totalFoodAvailable -= foodRequired;

                // Satiety & Health Recovery
                if (hominid.getSatiety() >= 100.0) {
                    hominid.setSatiety(100.0);
                    hominid.setHealth(Math.min(hominid.getHealth() + 5.0, 100.0));
                } else {
                    hominid.setSatiety(Math.min(hominid.getSatiety() + 20.0, 100.0));
                }
            } else {
                double foodDeficit = foodRequired - totalFoodAvailable;
                totalFoodAvailable = 0.0; // Food pool is exhausted

                double satietyDrop = (foodDeficit / foodRequired) * 100.0;
                hominid.setSatiety(Math.max(hominid.getSatiety() - satietyDrop, 0.0));
            }

            // Health Drop from Hladovění (Starvation)
            if (hominid.getSatiety() <= 0.0) {
                hominid.setHealth(hominid.getHealth() - 15.0);
            }

            // 6. Organic Old Age Death & Health Death Handling
            boolean diedOfOldAge = false;
            int age = hominid.getAgeInMonths();
            
            if (age >= 480) { // 40+ years
                if (ThreadLocalRandom.current().nextDouble() < 0.20) {
                    diedOfOldAge = true;
                }
            } else if (age > 420) { // 35 - 39.9 years
                if (ThreadLocalRandom.current().nextDouble() < 0.05) {
                    diedOfOldAge = true;
                }
            }

            if (hominid.getHealth() <= 0.0 || diedOfOldAge) {
                iterator.remove();
            }
        }
        
        // 7. Safely add new births to the tribe to avoid ConcurrentModificationException
        members.addAll(newBirths);
    }

    /**
     * Calculates the total food production for the current tick based on available workers,
     * tribe priorities, and seasonal effects.
     */
    private double calculateMonthlyFoodProduction(Tribe tribe) {
        long availableWorkers = tribe.getMembers().stream()
                .filter(h -> h.getLifeStage() == Hominid.LifeStage.ADULT && !h.isBlockedMother())
                .count();

        if (availableWorkers == 0) {
            return 0.0;
        }

        Map<String, Double> priorities = tribe.getPriorities();
        double sberWeight = priorities != null ? priorities.getOrDefault("SBER", 0.0) : 0.0;
        double lovWeight = priorities != null ? priorities.getOrDefault("LOV", 0.0) : 0.0;

        double workersGathering = availableWorkers * sberWeight;
        double workersHunting = availableWorkers * lovWeight;

        double gatheringProduction = workersGathering * 1.5;
        if ("DRY".equalsIgnoreCase(tribe.getCurrentSeason())) {
            gatheringProduction *= 0.5; // 50% penalty during DRY season
        }

        double huntingProduction = workersHunting * 3.0;

        return gatheringProduction + huntingProduction;
    }
}