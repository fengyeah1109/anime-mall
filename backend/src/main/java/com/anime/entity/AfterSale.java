package com.anime.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("after_sale")
public class AfterSale {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long orderItemId;
    private Integer type;
    private String reason;
    private String description;
    private String evidenceImages;
    private Integer status;
    private LocalDateTime applyTime;
    private LocalDateTime handleTime;
    private String reply;

    @TableLogic
    private Integer deleted;
}
