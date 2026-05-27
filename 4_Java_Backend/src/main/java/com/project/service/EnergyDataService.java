package com.project.service;

import com.project.entity.EnergyData;
import com.project.mapper.EnergyDataMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EnergyDataService {

    @Autowired
    private EnergyDataMapper energyDataMapper;

    public List<EnergyData> findByCode(String equipmentCode, int limit) {
        return energyDataMapper.selectByCode(equipmentCode, limit);
    }

    public EnergyData sumByRange(String equipmentCode, String startTime, String endTime) {
        return energyDataMapper.sumByRange(equipmentCode, startTime, endTime);
    }

    public EnergyData add(EnergyData energyData) {
        energyDataMapper.insert(energyData);
        return energyData;
    }
}