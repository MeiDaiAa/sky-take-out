package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

@Mapper
public interface ReportMapper {
    /**
     * 统计营业额
     * @param start 开始时间
     * @param last 结束时间
     */
    Double getTurnoverByDates(LocalDateTime start, LocalDateTime last, Integer status);
}
