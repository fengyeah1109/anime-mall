package com.anime.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("review")
public class Review {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderItemId;
    private Long userId;
    private Long productId;
    private Integer rating;
    private String content;
    private String images;
    private LocalDateTime createTime;
    
    // 非数据库字段，用于前端显示
    private transient String productName;
    private transient String productImage;
    private transient BigDecimal price;
}

