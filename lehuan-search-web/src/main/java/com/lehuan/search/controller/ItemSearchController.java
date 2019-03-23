package com.lehuan.search.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.lehuan.search.service.ItemSearchService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @program: lehuan-parent
 * @description: 商品搜索
 * @author: baichen
 **/
@RestController
@RequestMapping("/itemSearch")
public class ItemSearchController {
    //@Reference(timeout = 5000)
    @Reference
    private ItemSearchService itemSearchService;

    @RequestMapping("/search")
    public Map search(@RequestBody Map searchMap) {
        return itemSearchService.search(searchMap);
    }
}
