package com.project.entity;

import java.time.LocalDateTime;

public class Alarm {
    private Long id;
    private String equipmentCode;
    private String alarmCode;
    private Integer alarmLevel;
    private String alarmMessage;
    private Integer status;
    private String handleUser;
    private String handleNote;
    private LocalDateTime handleTime;
    private LocalDateTime createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEquipmentCode() { return equipmentCode; }
    public void setEquipmentCode(String equipmentCode) { this.equipmentCode = equipmentCode; }
    public String getAlarmCode() { return alarmCode; }
    public void setAlarmCode(String alarmCode) { this.alarmCode = alarmCode; }
    public Integer getAlarmLevel() { return alarmLevel; }
    public void setAlarmLevel(Integer alarmLevel) { this.alarmLevel = alarmLevel; }
    public String getAlarmMessage() { return alarmMessage; }
    public void setAlarmMessage(String alarmMessage) { this.alarmMessage = alarmMessage; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getHandleUser() { return handleUser; }
    public void setHandleUser(String handleUser) { this.handleUser = handleUser; }
    public String getHandleNote() { return handleNote; }
    public void setHandleNote(String handleNote) { this.handleNote = handleNote; }
    public LocalDateTime getHandleTime() { return handleTime; }
    public void setHandleTime(LocalDateTime handleTime) { this.handleTime = handleTime; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}