package com.anime.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("`character`")
public class CharacterEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long animeIpId;
    private String name;
    private String imageUrl;
    private String description;
    private Integer popularity;
    private Integer status;
}

