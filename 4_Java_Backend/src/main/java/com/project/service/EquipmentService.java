package com.project.service;

import com.project.entity.Equipment;
import com.project.mapper.EquipmentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EquipmentService {

    @Autowired
    private EquipmentMapper equipmentMapper;

    public List<Equipment> findAll() {
        return equipmentMapper.selectAll();
    }

    public List<Equipment> findByType(String equipmentType) {
        return equipmentMapper.selectByType(equipmentType);
    }

    public Equipment findByCode(String equipmentCode) {
        return equipmentMapper.selectByCode(equipmentCode);
    }

    public Equipment add(Equipment equipment) {
        equipmentMapper.insert(equipment);
        return equipment;
    }

    public Equipment update(Equipment equipment) {
        equipmentMapper.updateByCode(equipment);
        return equipment;
    }

    public int deleteByCode(String equipmentCode) {
        return equipmentMapper.deleteByCode(equipmentCode);
    }
}