package com.anime.mapper;

import com.anime.entity.Product;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface ProductMapper extends BaseMapper<Product> {

    @Update("UPDATE product SET stock = stock - #{qty}, sales = sales + #{qty} WHERE id = #{productId} AND stock >= #{qty} AND deleted = 0")
    int deductStock(@Param("productId") Long productId, @Param("qty") Integer qty);
}

