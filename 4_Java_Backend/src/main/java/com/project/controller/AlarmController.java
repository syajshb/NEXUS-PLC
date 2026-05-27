package com.project.controller;

import com.project.entity.Alarm;
import com.project.service.AlarmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/monitor/alarm")
@CrossOrigin(origins = "*")
public class AlarmController {

    @Autowired
    private AlarmService alarmService;

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(required = false) Integer status) {
        List<Alarm> list;
        if (status != null) {
            list = alarmService.findByStatus(status);
        } else {
            list = alarmService.findAll();
        }
        Map<String, Object> body = new HashMap<>();
        body.put("data", list);
        body.put("total", list.size());
        return ResponseEntity.ok(body);
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> count() {
        int count = alarmService.countUnhandled();
        Map<String, Object> body = new HashMap<>();
        body.put("count", count);
        body.put("success", true);
        return ResponseEntity.ok(body);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> add(@RequestBody Alarm alarm) {
        Alarm saved = alarmService.add(alarm);
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("data", saved);
        body.put("message", "报警记录已提交");
        return ResponseEntity.ok(body);
    }

    @PutMapping("/{alarmId}/handle")
    public ResponseEntity<Map<String, Object>> handle(
            @PathVariable Long alarmId,
            @RequestParam String handleUser,
            @RequestParam(required = false) String handleNote) {
        int rows = alarmService.handle(alarmId, handleUser, handleNote != null ? handleNote : "");
        Map<String, Object> body = new HashMap<>();
        body.put("success", rows > 0);
        body.put("message", rows > 0 ? "报警已处理" : "处理失败");
        return ResponseEntity.ok(body);
    }
}