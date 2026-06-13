package com.evolution.model;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tribe {
    
    private List<Hominid> members;
    
    // Priorities map, e.g., keys: "SBER", "LOV", "STRAZ", "VYZKUM"
    private Map<String, Double> priorities;
    
    // "MONSOON" or "DRY"
    private String currentSeason; 
}