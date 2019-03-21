package com.lehuan.portal.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.lehuan.content.service.ContentService;
import com.lehuan.pojo.TbContent;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @program: lehuan-parent
 * @description: 广告控制层
 * @author: baichen
 * @create: 2018-11-04 20:04
 **/
@RestController
@RequestMapping("/content")
public class ContentController {
    @Reference
    private ContentService contentService;

    /**
     * 根据广告分类ID查询广告列表
     * @param categoryId
     * @return
     */
    @RequestMapping("/findByCategoryId")
    public List<TbContent> findByCategoryId(Long categoryId) {
        return contentService.findByCategoryId(categoryId);
    }
}

