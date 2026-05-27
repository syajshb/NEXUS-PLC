package com.project.mapper;

import com.project.entity.Alarm;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AlarmMapper {

    String COLUMNS = "id, EquipmentCode AS equipment_code, AlarmCode AS alarm_code, " +
            "AlarmLevel AS alarm_level, AlarmMessage AS alarm_message, Status AS status, " +
            "HandleUser AS handle_user, HandleNote AS handle_note, HandleTime AS handle_time, " +
            "CreateTime AS create_time";

    @Select("SELECT " + COLUMNS + " FROM alarm ORDER BY create_time DESC")
    List<Alarm> selectAll();

    @Select("SELECT " + COLUMNS + " FROM alarm WHERE status = #{status} ORDER BY create_time DESC")
    List<Alarm> selectByStatus(Integer status);

    @Select("SELECT " + COLUMNS + " FROM alarm WHERE id = #{id}")
    Alarm selectById(Long id);

    @Select("SELECT COUNT(*) FROM alarm WHERE status = 0")
    int countUnhandled();

    @Insert("INSERT INTO alarm (EquipmentCode, AlarmCode, AlarmLevel, AlarmMessage, Status, CreateTime) " +
            "VALUES (#{equipmentCode}, #{alarmCode}, #{alarmLevel}, #{alarmMessage}, 0, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Alarm alarm);

    @Update("UPDATE alarm SET Status = 1, HandleUser = #{handleUser}, HandleNote = #{handleNote}, HandleTime = NOW() WHERE id = #{id}")
    int handle(Long id, String handleUser, String handleNote);
}