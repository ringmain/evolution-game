package com.evolution.model;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Hominid {
    
    private UUID id;
    private Species species;
    private String gender; // "MALE" or "FEMALE"
    private int ageInMonths;
    
    @Builder.Default
    private double health = 100.0; // 0.0 - 100.0
    
    @Builder.Default
    private double satiety = 100.0; // 0.0 - 100.0
    
    private double visibleScleraPercentage; // 0.0 - 100.0
    private boolean isAlpha;
    private boolean isBlockedMother;
    private int monthsBlockedRemaining;

    public enum LifeStage {
        INFANT, ADULT, SENIOR
    }

    /**
     * Determines the current biological life stage of the hominid.
     */
    public LifeStage getLifeStage() {
        if (ageInMonths <= 119) {
            return LifeStage.INFANT;
        } else if (ageInMonths <= 479) {
            return LifeStage.ADULT;
        } else {
            return LifeStage.SENIOR;
        }
    }
}