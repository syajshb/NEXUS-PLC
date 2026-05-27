package com.project.controller;

import com.project.entity.ControlCommand;
import com.project.service.ControlCommandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/control/command")
@CrossOrigin(origins = "*")
public class ControlCommandController {

    @Autowired
    private ControlCommandService controlCommandService;

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(required = false) String equipmentCode) {
        List<ControlCommand> list;
        if (equipmentCode != null && !equipmentCode.isEmpty()) {
            list = controlCommandService.findByEquipment(equipmentCode);
        } else {
            list = controlCommandService.findAll();
        }
        Map<String, Object> body = new HashMap<>();
        body.put("data", list);
        body.put("total", list.size());
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{commandId}")
    public ResponseEntity<Map<String, Object>> detail(@PathVariable Long commandId) {
        ControlCommand cmd = controlCommandService.findById(commandId);
        Map<String, Object> body = new HashMap<>();
        if (cmd == null) {
            body.put("success", false);
            body.put("message", "指令不存在");
            return ResponseEntity.ok(body);
        }
        body.put("success", true);
        body.put("data", cmd);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/equipment/{equipmentCode}")
    public ResponseEntity<?> history(@PathVariable String equipmentCode) {
        List<ControlCommand> list = controlCommandService.findHistory(equipmentCode);
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("data", list);
        body.put("total", list.size());
        return ResponseEntity.ok(body);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestParam String equipmentCode,
                                                       @RequestParam String commandType,
                                                       @RequestParam(required = false) String commandValue) {
        ControlCommand cmd = new ControlCommand();
        cmd.setEquipmentCode(equipmentCode);
        cmd.setCommandType(commandType);
        cmd.setCommandValue(commandValue);
        ControlCommand saved = controlCommandService.add(cmd);
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("data", saved);
        body.put("message", "指令创建成功");
        return ResponseEntity.ok(body);
    }

    @PutMapping("/{commandId}")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable Long commandId,
            @RequestBody ControlCommand command) {
        int rows = controlCommandService.updateStatus(commandId, command.getStatus(),
                command.getResponseMessage());
        Map<String, Object> body = new HashMap<>();
        body.put("success", rows > 0);
        body.put("message", rows > 0 ? "状态更新成功" : "指令不存在");
        return ResponseEntity.ok(body);
    }
}