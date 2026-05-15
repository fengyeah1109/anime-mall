package com.anime.vo;

import lombok.Data;

@Data
public class CategoryVO {
    private Long id;
    private Long parentId;
    private String name;
    private Integer level;
    private Integer sortOrder;
    private String icon;
    private Integer status;
}

