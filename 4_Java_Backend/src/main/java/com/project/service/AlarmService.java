package com.project.service;

import com.project.entity.Alarm;
import com.project.mapper.AlarmMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlarmService {

    @Autowired
    private AlarmMapper alarmMapper;

    public List<Alarm> findAll() {
        return alarmMapper.selectAll();
    }

    public List<Alarm> findByStatus(Integer status) {
        return alarmMapper.selectByStatus(status);
    }

    public Alarm findById(Long id) {
        return alarmMapper.selectById(id);
    }

    public int countUnhandled() {
        return alarmMapper.countUnhandled();
    }

    public Alarm add(Alarm alarm) {
        alarmMapper.insert(alarm);
        return alarm;
    }

    public int handle(Long id, String handleUser, String handleNote) {
        return alarmMapper.handle(id, handleUser, handleNote);
    }
}