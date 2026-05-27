package com.project.service;

import com.project.entity.PlcData;
import com.project.mapper.PlcDataMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlcDataService {

    @Autowired
    private PlcDataMapper plcDataMapper;

    public List<PlcData> findAll() {
        return plcDataMapper.selectAll();
    }

    public List<PlcData> findPage(int page, int size) {
        int offset = Math.max(page, 0) * Math.max(size, 1);
        return plcDataMapper.selectPage(offset, Math.max(size, 1));
    }

    public long count() {
        return plcDataMapper.count();
    }

    public PlcData findById(Long id) {
        return plcDataMapper.selectById(id);
    }

    public PlcData findLatest() {
        return plcDataMapper.selectLatest();
    }
}
