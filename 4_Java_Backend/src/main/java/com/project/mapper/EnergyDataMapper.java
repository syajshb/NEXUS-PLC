package com.project.mapper;

import com.project.entity.EnergyData;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface EnergyDataMapper {

    String COLUMNS = "id, EquipmentCode AS equipment_code, Electricity AS electricity, " +
            "Water AS water, Gas AS gas, CreateTime AS create_time";

    @Select("SELECT " + COLUMNS + " FROM energy_data WHERE EquipmentCode = #{equipmentCode} ORDER BY create_time DESC LIMIT #{limit}")
    List<EnergyData> selectByCode(String equipmentCode, int limit);

    @Select("SELECT COALESCE(SUM(Electricity), 0), COALESCE(SUM(Water), 0), COALESCE(SUM(Gas), 0) " +
            "FROM energy_data WHERE EquipmentCode = #{equipmentCode} AND CreateTime BETWEEN #{startTime} AND #{endTime}")
    @Results({
            @Result(column = "COALESCE(SUM(Electricity), 0)", property = "electricity"),
            @Result(column = "COALESCE(SUM(Water), 0)", property = "water"),
            @Result(column = "COALESCE(SUM(Gas), 0)", property = "gas")
    })
    EnergyData sumByRange(String equipmentCode, String startTime, String endTime);

    @Insert("INSERT INTO energy_data (EquipmentCode, Electricity, Water, Gas, CreateTime) " +
            "VALUES (#{equipmentCode}, #{electricity}, #{water}, #{gas}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(EnergyData energyData);
}