package com.project.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PlcData {
    private Long id;
    private LocalDateTime timestamp;
    private Integer status;
    private Integer counter;
    private BigDecimal temperature;
    private BigDecimal humidity;
    private BigDecimal pressure;
    private BigDecimal powerConsumption;
    private BigDecimal runHours;
    private Integer productCount;
    private BigDecimal qualityRate;
    private BigDecimal speed;
    private BigDecimal efficiency;
    private BigDecimal electricity;
    private BigDecimal water;
    private BigDecimal gas;
    private Integer alarmActive;
    private Integer alarmLevel;
    private Integer alarmCounter;
    private String source;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Integer getCounter() { return counter; }
    public void setCounter(Integer counter) { this.counter = counter; }
    public BigDecimal getTemperature() { return temperature; }
    public void setTemperature(BigDecimal temperature) { this.temperature = temperature; }
    public BigDecimal getHumidity() { return humidity; }
    public void setHumidity(BigDecimal humidity) { this.humidity = humidity; }
    public BigDecimal getPressure() { return pressure; }
    public void setPressure(BigDecimal pressure) { this.pressure = pressure; }
    public BigDecimal getPowerConsumption() { return powerConsumption; }
    public void setPowerConsumption(BigDecimal powerConsumption) { this.powerConsumption = powerConsumption; }
    public BigDecimal getRunHours() { return runHours; }
    public void setRunHours(BigDecimal runHours) { this.runHours = runHours; }
    public Integer getProductCount() { return productCount; }
    public void setProductCount(Integer productCount) { this.productCount = productCount; }
    public BigDecimal getQualityRate() { return qualityRate; }
    public void setQualityRate(BigDecimal qualityRate) { this.qualityRate = qualityRate; }
    public BigDecimal getSpeed() { return speed; }
    public void setSpeed(BigDecimal speed) { this.speed = speed; }
    public BigDecimal getEfficiency() { return efficiency; }
    public void setEfficiency(BigDecimal efficiency) { this.efficiency = efficiency; }
    public BigDecimal getElectricity() { return electricity; }
    public void setElectricity(BigDecimal electricity) { this.electricity = electricity; }
    public BigDecimal getWater() { return water; }
    public void setWater(BigDecimal water) { this.water = water; }
    public BigDecimal getGas() { return gas; }
    public void setGas(BigDecimal gas) { this.gas = gas; }
    public Integer getAlarmActive() { return alarmActive; }
    public void setAlarmActive(Integer alarmActive) { this.alarmActive = alarmActive; }
    public Integer getAlarmLevel() { return alarmLevel; }
    public void setAlarmLevel(Integer alarmLevel) { this.alarmLevel = alarmLevel; }
    public Integer getAlarmCounter() { return alarmCounter; }
    public void setAlarmCounter(Integer alarmCounter) { this.alarmCounter = alarmCounter; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
}
