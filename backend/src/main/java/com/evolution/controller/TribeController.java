package com.evolution.controller;

import com.evolution.model.Tribe;
import com.evolution.service.TribeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/tribe")
@CrossOrigin
@RequiredArgsConstructor
public class TribeController {

    private final TribeService tribeService;

    /**
     * Retrieves the current state of the tribe.
     *
     * @return The active Tribe entity.
     */
    @GetMapping("")
    public Tribe getTribe() {
        return tribeService.getTribe();
    }

    /**
     * Advances the simulation by one tick (1 month).
     *
     * @return The updated Tribe entity after the tick is executed.
     */
    @PostMapping("/tick")
    public Tribe triggerTick() {
        tribeService.nextTick();
        return tribeService.getTribe();
    }

    /**
     * Updates the tribe's task priorities.
     *
     * @param priorities A map of task names to their respective weights (e.g., "SBER": 0.5).
     * @return The updated Tribe entity.
     */
    @PostMapping("/priorities")
    public Tribe updatePriorities(@RequestBody Map<String, Double> priorities) {
        tribeService.updatePriorities(priorities);
        return tribeService.getTribe();
    }
}