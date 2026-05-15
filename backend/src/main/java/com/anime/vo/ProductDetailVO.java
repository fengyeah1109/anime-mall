package com.anime.vo;

import com.anime.entity.AnimeIp;
import com.anime.entity.CharacterEntity;
import com.anime.entity.Product;
import com.anime.entity.ProductImage;
import com.anime.entity.Review;
import lombok.Data;

import java.util.List;

@Data
public class ProductDetailVO {
    private Product product;
    private List<ProductImage> images;
    private AnimeIp animeIp;
    private List<CharacterEntity> characters;
    private List<Review> latestReviews;
}

