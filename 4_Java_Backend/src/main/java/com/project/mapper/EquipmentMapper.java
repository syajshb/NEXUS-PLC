package com.project.mapper;

import com.project.entity.Equipment;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface EquipmentMapper {

    String COLUMNS = "id, EquipmentCode AS equipment_code, EquipmentName AS equipment_name, " +
            "EquipmentType AS equipment_type, Location AS location, IpAddress AS ip_address, " +
            "Description AS description, CreateTime AS create_time, UpdateTime AS update_time";

    @Select("SELECT " + COLUMNS + " FROM equipment ORDER BY id")
    List<Equipment> selectAll();

    @Select("SELECT " + COLUMNS + " FROM equipment WHERE equipment_type = #{equipmentType} ORDER BY id")
    List<Equipment> selectByType(String equipmentType);

    @Select("SELECT " + COLUMNS + " FROM equipment WHERE EquipmentCode = #{equipmentCode}")
    Equipment selectByCode(String equipmentCode);

    @Insert("INSERT INTO equipment (EquipmentCode, EquipmentName, EquipmentType, Location, IpAddress, Description, CreateTime) " +
            "VALUES (#{equipmentCode}, #{equipmentName}, #{equipmentType}, #{location}, #{ipAddress}, #{description}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Equipment equipment);

    @Update("UPDATE equipment SET EquipmentName = #{equipmentName}, EquipmentType = #{equipmentType}, " +
            "Location = #{location}, IpAddress = #{ipAddress}, Description = #{description}, UpdateTime = NOW() " +
            "WHERE EquipmentCode = #{equipmentCode}")
    int updateByCode(Equipment equipment);

    @Delete("DELETE FROM equipment WHERE EquipmentCode = #{equipmentCode}")
    int deleteByCode(String equipmentCode);
}