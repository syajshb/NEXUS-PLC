package com.project.service;

import com.project.entity.ControlCommand;
import com.project.mapper.ControlCommandMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ControlCommandService {

    @Autowired
    private ControlCommandMapper controlCommandMapper;

    public List<ControlCommand> findAll() {
        return controlCommandMapper.selectAll();
    }

    public List<ControlCommand> findByEquipment(String equipmentCode) {
        return controlCommandMapper.selectByEquipment(equipmentCode);
    }

    public ControlCommand findById(Long id) {
        return controlCommandMapper.selectById(id);
    }

    public List<ControlCommand> findHistory(String equipmentCode) {
        return controlCommandMapper.selectHistory(equipmentCode);
    }

    public ControlCommand add(ControlCommand command) {
        controlCommandMapper.insert(command);
        return command;
    }

    public int updateStatus(Long id, Integer status, String responseMessage) {
        return controlCommandMapper.updateStatus(id, status, responseMessage);
    }
}