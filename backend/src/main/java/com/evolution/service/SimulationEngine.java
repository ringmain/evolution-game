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

        // 1. Advance the global timeline
        tribe.setTotalTicks(tribe.getTotalTicks() + 1);

        // 2. Season Logic
        int monthOfYear = (tribe.getTotalTicks() - 1) % 12;
        String newSeason = (monthOfYear < 6) ? "MONSOON" : "DRY";
        
        if (!newSeason.equals(tribe.getCurrentSeason())) {
            if ("DRY".equals(newSeason)) {
                tribe.getLogMessages().add("Měsíc " + tribe.getTotalTicks() + ": Začalo období sucha (DRY). Produkce sběru je snížena o 50%!");
            } else if ("MONSOON".equals(newSeason)) {
                tribe.getLogMessages().add("Měsíc " + tribe.getTotalTicks() + ": Začalo období dešťů (MONSOON). Les překypuje ovocem.");
            }
            tribe.setCurrentSeason(newSeason);
        }

        List<Hominid> members = tribe.getMembers();
        List<Hominid> newBirths = new ArrayList<>();
        
        // 3. Hierarchical Food Distribution: Alphas eat first
        members.sort(Comparator.comparing(Hominid::isAlpha).reversed());

        double totalFoodAvailable = calculateMonthlyFoodProduction(tribe);

        Iterator<Hominid> iterator = members.iterator();
        while (iterator.hasNext()) {
            Hominid hominid = iterator.next();

            // 4. Aging
            hominid.setAgeInMonths(hominid.getAgeInMonths() + 1);

            // 5. Mother Blockage Counter
            if (hominid.isBlockedMother()) {
                hominid.setMonthsBlockedRemaining(hominid.getMonthsBlockedRemaining() - 1);
                if (hominid.getMonthsBlockedRemaining() <= 0) {
                    hominid.setBlockedMother(false);
                    hominid.setMonthsBlockedRemaining(0);
                }
            }

            // 6. Pregnancy Lifecycle
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

                    // Log the birth
                    String czGender = newbornGender.equals("MALE") ? "Sameček" : "Samička";
                    tribe.getLogMessages().add("Měsíc " + tribe.getTotalTicks() + ": Narodilo se nové mládě (" + czGender + ")!");
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

            // 7. Food Consumption & Recovery/Starvation Logic
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

            // 8. Organic Old Age Death & Health Death Handling
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
                // Log the death before removal
                String alphaPrefix = hominid.isAlpha() ? "ALFA " : "";
                String shortId = hominid.getId().toString().substring(0, 8);
                tribe.getLogMessages().add("Měsíc " + tribe.getTotalTicks() + ": Šimpanz " + shortId + " (" + alphaPrefix + hominid.getGender() + ") zemřel věkem/hladem.");
                
                iterator.remove();
            }
        }
        
        // 9. Safely add new births to the tribe
        members.addAll(newBirths);

        // 10. Alpha Succession Logic
        if (!members.isEmpty()) {
            boolean hasAlpha = members.stream().anyMatch(Hominid::isAlpha);
            
            if (!hasAlpha) {
                // Attempt to find the oldest adult male
                Hominid newAlpha = members.stream()
                        .filter(h -> h.getLifeStage() == Hominid.LifeStage.ADULT && "MALE".equals(h.getGender()))
                        .max(Comparator.comparingInt(Hominid::getAgeInMonths))
                        .orElseGet(() -> 
                            // Fallback: oldest adult female
                            members.stream()
                                .filter(h -> h.getLifeStage() == Hominid.LifeStage.ADULT && "FEMALE".equals(h.getGender()))
                                .max(Comparator.comparingInt(Hominid::getAgeInMonths))
                                .orElse(null)
                        );
                
                // Absolute fallback in case only infants/seniors are alive
                if (newAlpha == null) {
                    newAlpha = members.stream().max(Comparator.comparingInt(Hominid::getAgeInMonths)).orElse(null);
                }

                if (newAlpha != null) {
                    newAlpha.setAlpha(true);
                    int ageInYears = newAlpha.getAgeInMonths() / 12;
                    tribe.getLogMessages().add("Měsíc " + tribe.getTotalTicks() + ": Novým vůdcem tlupy se stává " + newAlpha.getGender() + " (" + ageInYears + " let).");
                }
            }
        }

        // 11. Truncate log messages to prevent infinite memory growth (max 50 messages)
        while (tribe.getLogMessages().size() > 50) {
            tribe.getLogMessages().remove(0);
        }
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