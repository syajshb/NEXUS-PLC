package com.project.controller;

import com.project.entity.EnergyData;
import com.project.service.EnergyDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/monitor/energy")
@CrossOrigin(origins = "*")
public class EnergyDataController {

    @Autowired
    private EnergyDataService energyDataService;

    @GetMapping
    public ResponseEntity<?> list(
            @RequestParam String equipmentCode,
            @RequestParam(defaultValue = "100") int limit) {
        List<EnergyData> list = energyDataService.findByCode(equipmentCode, limit);
        Map<String, Object> body = new HashMap<>();
        body.put("data", list);
        body.put("total", list.size());
        return ResponseEntity.ok(body);
    }

    @GetMapping("/total")
    public ResponseEntity<?> total(
            @RequestParam String equipmentCode,
            @RequestParam String startTime,
            @RequestParam String endTime) {
        EnergyData total = energyDataService.sumByRange(equipmentCode, startTime, endTime);
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("data", total);
        body.put("equipmentCode", equipmentCode);
        return ResponseEntity.ok(body);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> add(@RequestBody EnergyData energyData) {
        EnergyData saved = energyDataService.add(energyData);
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("data", saved);
        body.put("message", "能耗数据已上报");
        return ResponseEntity.ok(body);
    }
}