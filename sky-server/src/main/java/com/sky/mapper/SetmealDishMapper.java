package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.List;

@Mapper
public interface SetmealDishMapper {

    /**
     * 通过菜品id查询套餐信息
     * @param dishIds
     * @return
     */
    List<SetmealDish> getByDishIds(List<Long> dishIds);


    /**
     * 通过套餐id查询菜品选项
     * @param setmealId
     * @return
     */
    @Select("select * from setmeal_dish where setmeal_id = #{setmealId}")
    List<SetmealDish> getBySetmealId(Long setmealId);

    void deleteBySetmealIds(List<Long> ids);

    void insertBatch(List<SetmealDish> setmealDishes);
}
