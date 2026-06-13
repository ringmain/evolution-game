package com.evolution.service;

import com.evolution.model.Hominid;
import com.evolution.model.Tribe;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
        
        // 1. Hierarchical Food Distribution: Alphas eat first
        // Boolean comparison defaults to false first, true second. Reversed places true (Alphas) first.
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

            // 4. Food Consumption & Recovery/Starvation Logic
            double foodRequired = (hominid.getLifeStage() == Hominid.LifeStage.INFANT) ? 0.5 : 1.0;

            if (totalFoodAvailable >= foodRequired) {
                // Hominid receives full food requirement
                totalFoodAvailable -= foodRequired;

                // Satiety & Health Recovery
                if (hominid.getSatiety() >= 100.0) {
                    hominid.setSatiety(100.0);
                    // Recover health if satiety is already maxed
                    hominid.setHealth(Math.min(hominid.getHealth() + 5.0, 100.0));
                } else {
                    // Recover satiety
                    hominid.setSatiety(Math.min(hominid.getSatiety() + 20.0, 100.0));
                }
            } else {
                // Hominid receives partial or no food
                double foodDeficit = foodRequired - totalFoodAvailable;
                totalFoodAvailable = 0.0; // Food pool is exhausted

                double satietyDrop = (foodDeficit / foodRequired) * 100.0;
                hominid.setSatiety(Math.max(hominid.getSatiety() - satietyDrop, 0.0));
            }

            // Health Drop from Hladovění (Starvation)
            if (hominid.getSatiety() <= 0.0) {
                hominid.setHealth(hominid.getHealth() - 15.0);
            }

            // 5. Organic Old Age Death & Health Death Handling
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
    }

    /**
     * Calculates the total food production for the current tick based on available workers,
     * tribe priorities, and seasonal effects.
     * * @param tribe The tribe entity containing workers, priorities, and season data.
     * @return Total amount of food produced this month.
     */
    private double calculateMonthlyFoodProduction(Tribe tribe) {
        // 1. Calculate available workers (ADULTS who are NOT blocked mothers)
        long availableWorkers = tribe.getMembers().stream()
                .filter(h -> h.getLifeStage() == Hominid.LifeStage.ADULT && !h.isBlockedMother())
                .count();

        if (availableWorkers == 0) {
            return 0.0;
        }

        // 2. Fetch priorities
        Map<String, Double> priorities = tribe.getPriorities();
        double sberWeight = priorities != null ? priorities.getOrDefault("SBER", 0.0) : 0.0;
        double lovWeight = priorities != null ? priorities.getOrDefault("LOV", 0.0) : 0.0;

        // Calculate active workers dedicated to each task
        double workersGathering = availableWorkers * sberWeight;
        double workersHunting = availableWorkers * lovWeight;

        // 3. Base Gathering Production (1.5 per worker)
        double gatheringProduction = workersGathering * 1.5;
        if ("DRY".equalsIgnoreCase(tribe.getCurrentSeason())) {
            gatheringProduction *= 0.5; // 50% penalty during DRY season
        }

        // 4. Base Hunting Production (3.0 per worker, unaffected by season)
        double huntingProduction = workersHunting * 3.0;

        // Total food produced
        return gatheringProduction + huntingProduction;
    }
}