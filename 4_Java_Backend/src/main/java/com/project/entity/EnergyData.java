package com.project.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class EnergyData {
    private Long id;
    private String equipmentCode;
    private BigDecimal electricity;
    private BigDecimal water;
    private BigDecimal gas;
    private LocalDateTime createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEquipmentCode() { return equipmentCode; }
    public void setEquipmentCode(String equipmentCode) { this.equipmentCode = equipmentCode; }
    public BigDecimal getElectricity() { return electricity; }
    public void setElectricity(BigDecimal electricity) { this.electricity = electricity; }
    public BigDecimal getWater() { return water; }
    public void setWater(BigDecimal water) { this.water = water; }
    public BigDecimal getGas() { return gas; }
    public void setGas(BigDecimal gas) { this.gas = gas; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}