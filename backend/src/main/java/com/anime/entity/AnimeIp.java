package com.anime.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("anime_ip")
public class AnimeIp {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String coverImage;
    private String description;
    private Integer popularity;
    private Integer status;
}

