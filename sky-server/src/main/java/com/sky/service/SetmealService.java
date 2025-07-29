package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.SetmealVO;

import java.util.List;

public interface SetmealService {
    PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    void saveWithSetmealDish(SetmealDTO setmealDTO);

    SetmealVO getByIdWithSetmealDish(Long id);

    void startOrStop(Integer status, Long id);

    void deleteByIdsWithSetmealDish(List<Long> ids);

    void updateWithSetmealDish(SetmealDTO setmealDTO);
}
