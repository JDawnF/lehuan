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
     * 导入数据
     * @param list
     */
    public void importList(List list);

    /**
     * 删除数据,SPU的id
     * @param goodsIdList
     */
    public void deleteByGoodsIds(List goodsIdList);


}

