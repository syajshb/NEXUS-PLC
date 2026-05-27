package com.project.mapper;

import com.project.entity.ControlCommand;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ControlCommandMapper {

    String COLUMNS = "id, EquipmentCode AS equipment_code, CommandType AS command_type, " +
            "CommandValue AS command_value, Status AS status, ResponseMessage AS response_message, " +
            "CreateTime AS create_time, UpdateTime AS update_time";

    @Select("SELECT " + COLUMNS + " FROM control_command WHERE EquipmentCode = #{equipmentCode} ORDER BY create_time DESC")
    List<ControlCommand> selectByEquipment(String equipmentCode);

    @Select("SELECT " + COLUMNS + " FROM control_command ORDER BY create_time DESC")
    List<ControlCommand> selectAll();

    @Select("SELECT " + COLUMNS + " FROM control_command WHERE id = #{id}")
    ControlCommand selectById(Long id);

    @Select("SELECT " + COLUMNS + " FROM control_command WHERE EquipmentCode = #{equipmentCode} ORDER BY create_time DESC")
    List<ControlCommand> selectHistory(String equipmentCode);

    @Insert("INSERT INTO control_command (EquipmentCode, CommandType, CommandValue, Status, CreateTime) " +
            "VALUES (#{equipmentCode}, #{commandType}, #{commandValue}, 0, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ControlCommand command);

    @Update("UPDATE control_command SET Status = #{status}, ResponseMessage = #{responseMessage}, UpdateTime = NOW() WHERE id = #{id}")
    int updateStatus(Long id, Integer status, String responseMessage);
}