package com.lehuan.search.service;

import java.util.List;
import java.util.Map;

public interface ItemSearchService {
    /**
     * 搜索
     * @param searchMap
     * @return
     */
    public Map search(Map searchMap);

    /**
     * 批量导入数据到solr索引库
     * @param list  需要导入的数据列表
     */
    public void importList(List list);

    /**
     * 删除数据,SPU的id
     * @param goodsIdList   商品id集合
     */
    public void deleteByGoodsIds(List goodsIdList);


}

