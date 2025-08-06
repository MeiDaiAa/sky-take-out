package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.ShoppingCart;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {

    /**
     * 插入数据
     * @param shoppingCart 购物车对象数据
     */
    @Insert("insert into shopping_cart (name, user_id, dish_id, setmeal_id, dish_flavor, number, amount, image, create_time) " +
            "values (#{name}, #{userId}, #{dishId}, #{setmealId}, #{dishFlavor}, #{number}, #{amount}, #{image}, #{createTime})")
    void insert(ShoppingCart shoppingCart);


    /**
     * 根据菜品id或套餐id查询
     * @param shoppingCart 购物车对象数据
     * @return List<ShoppingCart>
     */
    List<ShoppingCart> getByDishIdOrSetmealId(ShoppingCart shoppingCart);

    /**
     * 更新数据
     * @param shoppingCart 购物车对象数据
     */
    void update(ShoppingCart shoppingCart);

    /**
     * 根据用户id查询
     * @param userId 用户id
     * @return List<ShoppingCart>
     */
    @Select("select * from shopping_cart where user_id = #{userId}")
    List<ShoppingCart> getByUserId(Long userId);

    /**
     * 清空当前用户的数据
     * @param userId 用户id
     */
    @Delete("delete from shopping_cart where user_id = #{userId}")
    void deleteByUserId(Long userId);

    /**
     * 批量插入
     * @param shoppingCartList 购物车列表
     */
    void insertBatch(List<ShoppingCart> shoppingCartList);
}
