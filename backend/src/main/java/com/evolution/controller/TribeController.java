package com.evolution.controller;

import com.evolution.model.Tribe;
import com.evolution.service.TribeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @GetMapping("")
    public Tribe getTribe() {
        return tribeService.getTribe();
    }

    @PostMapping("/tick")
    public Tribe triggerTick() {
        tribeService.nextTick();
        return tribeService.getTribe();
    }

    @PostMapping("/tick/{count}")
    public Tribe triggerMultipleTicks(@PathVariable int count) {
        tribeService.nextTicks(count);
        return tribeService.getTribe();
    }

    @PostMapping("/priorities")
    public Tribe updatePriorities(@RequestBody Map<String, Double> priorities) {
        tribeService.updatePriorities(priorities);
        return tribeService.getTribe();
    }

    /**
     * Resets the entire simulation to the starting state.
     *
     * @return The freshly initialized Tribe entity.
     */
    @PostMapping("/reset")
    public Tribe resetTribe() {
        tribeService.resetGame();
        return tribeService.getTribe();
    }
}