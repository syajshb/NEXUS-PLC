package com.project.controller;

import com.project.entity.Equipment;
import com.project.service.EquipmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/equipment")
@CrossOrigin(origins = "*")
public class EquipmentController {

    @Autowired
    private EquipmentService equipmentService;

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(required = false) String equipmentType) {
        List<Equipment> list;
        if (equipmentType != null && !equipmentType.isEmpty()) {
            list = equipmentService.findByType(equipmentType);
        } else {
            list = equipmentService.findAll();
        }
        Map<String, Object> body = new HashMap<>();
        body.put("data", list);
        body.put("total", list.size());
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{equipmentCode}")
    public ResponseEntity<Equipment> detail(@PathVariable String equipmentCode) {
        Equipment equipment = equipmentService.findByCode(equipmentCode);
        if (equipment == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(equipment);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> add(@RequestBody Equipment equipment) {
        Equipment saved = equipmentService.add(equipment);
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("data", saved);
        body.put("message", "设备添加成功");
        return ResponseEntity.ok(body);
    }

    @PutMapping
    public ResponseEntity<Map<String, Object>> update(@RequestBody Equipment equipment) {
        equipmentService.update(equipment);
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("message", "设备更新成功");
        return ResponseEntity.ok(body);
    }

    @DeleteMapping("/{equipmentCode}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable String equipmentCode) {
        int rows = equipmentService.deleteByCode(equipmentCode);
        Map<String, Object> body = new HashMap<>();
        body.put("success", rows > 0);
        body.put("message", rows > 0 ? "设备已删除" : "设备不存在");
        return ResponseEntity.ok(body);
    }
}