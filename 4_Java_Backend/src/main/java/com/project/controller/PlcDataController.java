package com.project.controller;

import com.project.entity.PlcData;
import com.project.service.PlcDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/plc-data", "/api/data"})
@CrossOrigin(origins = "*")
public class PlcDataController {

    @Autowired
    private PlcDataService plcDataService;

    @GetMapping
    public ResponseEntity<?> list(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        if (page != null || size != null) {
            int p = page != null ? page : 0;
            int s = size != null ? size : 20;
            Map<String, Object> body = new HashMap<>();
            body.put("items", plcDataService.findPage(p, s));
            body.put("total", plcDataService.count());
            body.put("page", p);
            body.put("size", s);
            return ResponseEntity.ok(body);
        }
        return ResponseEntity.ok(plcDataService.findAll());
    }

    @GetMapping("/latest")
    public ResponseEntity<PlcData> latest() {
        PlcData data = plcDataService.findLatest();
        if (data == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(data);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlcData> getById(@PathVariable Long id) {
        PlcData data = plcDataService.findById(id);
        if (data == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(data);
    }
}
