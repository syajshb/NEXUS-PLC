package com.project.mapper;

import com.project.entity.PlcData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PlcDataMapper {

    String COLUMNS = "id, `timestamp`, " +
            "Status AS status, Counter AS counter, " +
            "Temperature AS temperature, Humidity AS humidity, Pressure AS pressure, " +
            "PowerConsumption AS power_consumption, RunHours AS run_hours, " +
            "ProductCount AS product_count, QualityRate AS quality_rate, " +
            "Speed AS speed, Efficiency AS efficiency, " +
            "Electricity AS electricity, Water AS water, Gas AS gas, " +
            "AlarmActive AS alarm_active, AlarmLevel AS alarm_level, " +
            "AlarmCounter AS alarm_counter, Source AS source";

    @Select("SELECT " + COLUMNS + " FROM plc_data ORDER BY id")
    List<PlcData> selectAll();

    @Select("SELECT " + COLUMNS + " FROM plc_data ORDER BY id LIMIT #{limit} OFFSET #{offset}")
    List<PlcData> selectPage(int offset, int limit);

    @Select("SELECT COUNT(*) FROM plc_data")
    long count();

    @Select("SELECT " + COLUMNS + " FROM plc_data WHERE id = #{id}")
    PlcData selectById(Long id);

    @Select("SELECT " + COLUMNS + " FROM plc_data ORDER BY `timestamp` DESC, id DESC LIMIT 1")
    PlcData selectLatest();
}
