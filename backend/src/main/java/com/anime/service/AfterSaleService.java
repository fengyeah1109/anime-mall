package com.anime.service;

import com.anime.entity.AfterSale;
import com.anime.vo.AfterSaleVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface AfterSaleService extends IService<AfterSale> {

    List<AfterSaleVO> getUserAfterSales(Long userId);

    List<AfterSaleVO> getAllAfterSales();

    AfterSaleVO getDetail(Long id);

    boolean updateStatus(Long id, Integer status, Long adminId, String reply);
}
