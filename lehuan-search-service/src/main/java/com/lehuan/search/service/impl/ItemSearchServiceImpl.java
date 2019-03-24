package com.lehuan.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.lehuan.pojo.TbItem;
import com.lehuan.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @program: lehuan-parent
 * @description: 搜索服务实现类
 * @author: baichen
 *  2018-11-12 23:02
 **/
//防止搜索过久，dubbo报错,也可以在controller中的Reference中写，如果两个都写，那么以消费端为准
@Service(timeout = 5000)
public class ItemSearchServiceImpl implements ItemSearchService {
    @Autowired
    private SolrTemplate solrTemplate;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 将各种查询条件和结果封装为一个个方法，然后在统一在search中调用
     * @param searchMap     在searchController.js中封装好的搜索对象，如下：
     *   $scope.searchMap = {
     *         'keywords': '', 'category': '', 'brand': '', 'spec': {}, 'price': '',
     *         'pageNo': 1, 'pageSize': 40, 'sort': '', 'sortField': ''
     *     };//搜索对象
     * @return  返回最终查询显示结果
     */
    @Override
    public Map search(Map searchMap) {
        Map map = new HashMap();
        //获取关键字，然后去掉空格
        String keywords = (String) searchMap.get("keywords");
        searchMap.put("keywords", keywords.replace(" ", "")); //关键字去掉空格
        //1.按关键字查询列表（高亮显示）
        map.putAll(searchList(searchMap));
        //2.根据关键字分组查询商品分类列表
        List<String> categoryList = searchCategoryList(searchMap);
        map.put("categoryList", categoryList);
        //3.查询品牌和规格列表
        String categoryName = (String) searchMap.get("category");
        if (!categoryName.equals("")) {
            map.putAll(searchBrandAndSpecList(categoryName));
        } else {
            if (categoryList.size() > 0) {
                map.putAll(searchBrandAndSpecList(categoryList.get(0)));
            }
        }
        return map;
    }

    /**
     * 根据关键字搜索商品列表
     *
     * @param searchMap 在searchController.js中封装好的搜索对象
     * @return      返回搜索得到的商品列表
     */
    private Map searchList(Map searchMap) {
        Map map = new HashMap();
        //Query query = new SimpleQuery("*:*");
        //高亮选项初始化
        HighlightQuery query = new SimpleHighlightQuery();
        //设置高亮的域，在item_title这个域上加高亮，可以有多个高亮域，这里只对item_title进行高亮
        HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");
        //设置高亮前缀和后缀，根据前端页面的标签添加的
        highlightOptions.setSimplePrefix("<em style='color:red'>");
        highlightOptions.setSimplePostfix("</em>");
        //设置高亮选项
        query.setHighlightOptions(highlightOptions);
        //1.1 关键字查询
        //添加查询条件,按照关键字搜索,根据之前配置的域查询
        // item_keywords这个字段是solrhome中的schema.xml普通域,它可以有多个值,比如：荣耀 手机等等
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        //ScoredPage<TbItem> page = solrTemplate.queryForPage(query, TbItem.class);

        //1.2 按照商品分类过滤，根据前端页面的点击分类进行过滤查询
        if (!"".equals(searchMap.get("category"))) {
            FilterQuery filterQuery = new SimpleFilterQuery();
//        searchMap.get的key是controllerJs中传过来的key一样
            Criteria filterCriteria = new Criteria("item_category").is(searchMap.get("category"));
            filterQuery.addCriteria(filterCriteria);
            query.addFilterQuery(filterQuery);
        }

        //1.3 按照品牌分类过滤，根据前端页面的点击分类进行过滤查询
        if (!"".equals(searchMap.get("brand"))) {    //如果用户选择了品牌
            FilterQuery filterQuery = new SimpleFilterQuery();
//        searchMap.get的key是controllerJs中传过来的key一样
            Criteria filterCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
            filterQuery.addCriteria(filterCriteria);
            query.addFilterQuery(filterQuery);
        }

        //1.4过滤规格
        if (searchMap.get("spec") != null) {
            Map<String, String> specMap = (Map<String, String>) searchMap.get("spec");
            for (String key : specMap.keySet()) {
//        searchMap.get的key是controllerJs中传过来的key一样
                Criteria filterCriteria = new Criteria("item_spec_" + key).is(specMap.get(key));
                FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
        }

        //1.5按价格过滤
        if (!"".equals(searchMap.get("price"))) {    //如果用户选择了价格
            //String priceStr = (String) searchMap.get("price");
            //分割前端传过来的价格区间
            String[] price = ((String) searchMap.get("price")).split("-");
            //如果最低价格不等于0，前端传过来的值都是"0-500","500-1000"，先分割
            if (!price[0].equals("0")) {
                FilterQuery filterQuery = new SimpleFilterQuery();
//        searchMap.get的key是controllerJs中传过来的key一样
                Criteria filterCriteria = new Criteria("item_price").greaterThanEqual(price[0]);
                filterQuery.addCriteria(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
            //如果最高价格不等于*,"3000-*"前端封装的值
            if (!price[1].equals("*")) {
                Criteria filterCriteria = new Criteria("item_price").lessThanEqual(price[1]);
                FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
        }

        //1.6分页查询,先定义前端传过来的参数，pageNo：页码；pageSize：每页记录数
        Integer pageNo = (Integer) searchMap.get("pageNo");
        //如果前端没有传过来pageNo这个参数，则默认是第一页
        if (pageNo == null) {
            pageNo = 1;
        }
        Integer pageSize = (Integer) searchMap.get("pageSize");
        if (pageSize == null) {
            pageSize = 20;
        }
        query.setOffset((pageNo - 1) * pageSize);   //起始索引
        query.setRows(pageSize);        //每页记录数

        //1.7排序，升序和降序
        String sortValue = (String) searchMap.get("sort");
        String sortField = (String) searchMap.get("sortField");
        if (sortValue != null && !sortField.equals("")) {
            if (sortValue.equals("ASC")) {
                //拼接搜索字段，item+sortField，sortField是前端传给后端的值
                Sort sort = new Sort(Sort.Direction.ASC, "item_" + sortField);
                query.addSort(sort);
            }
            if (sortValue.equals("DESC")) {
                Sort sort = new Sort(Sort.Direction.DESC, "item_" + sortField);
                query.addSort(sort);
            }
        }


        /**
         * 获取高亮结果集
         */
        //高亮页对象
        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
        //循环高亮入口集合(每条记录的高亮入口)，可以得到高亮结果的数据
        for (HighlightEntry<TbItem> h : page.getHighlighted()) {
            //获取原实体类，并没有高亮结果
            TbItem item = h.getEntity();
            //设置高亮的结果，h.getHighlights得到高亮列表
            //每个域可能存储多个列，只有一列的话，可以用get(0)，item相当于page.getContent()
            /**
             * item_keywords是可以有多个值的
             * h.getHighlights().get(0).getSnipplets()获取第一个高亮域的内容
             * h.getHighlights().get(0).getSnipplets().get(0) 一个高亮域中可能存在多值，取决于solr中的配置域的是否配置了multiValued是否为true
             */
            if (h.getHighlights().size() > 0 && h.getHighlights().get(0).getSnipplets().size() > 0) {
                item.setTitle(h.getHighlights().get(0).getSnipplets().get(0));
            }
        }
        map.put("rows", page.getContent());
        //返回总页数和总记录数
        map.put("totalPages", page.getTotalPages());
        map.put("total", page.getTotalElements());
        //返回map是为了更好地扩展
        return map;
    }

    /**
     * 根据关键字查询分类列表,即分组查询商品分类列表，sql中的group by
     * @param searchMap     在searchController.js中封装好的搜索对象
     * @return      返回分类列表
     */
    private List searchCategoryList(Map searchMap) {
        List<String> list = new ArrayList<>();
        Query query = new SimpleQuery("*:*");
        //按照关键字查询，相当于where语句
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
// 设置分组规则，根据哪一个域进行分组,即相当于group by,设置分组选项,可以多个
        GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");
        query.setGroupOptions(groupOptions);
//        分组查询，要基于某个查询条件查询，获取分组页
        GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);
//        获取分组结果对象，可能有多个分组结果
        GroupResult<TbItem> groupResult = page.getGroupResult("item_category");
//        获取分组入口页，得到具体分组数据，实体分组入口
        Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
//        获取分组入口集合,得到具体的分组入口
        List<GroupEntry<TbItem>> entryList = groupEntries.getContent();
        for (GroupEntry<TbItem> entry : entryList) {
            //将分组结果的名称封装到返回值列表中,返回一个字符串
            list.add(entry.getGroupValue());
        }
        return list;
    }

    /**
     * 查询品牌和规格列表
     * @param category 商品分类名称
     * @return  品牌和规格列表
     */
    private Map searchBrandAndSpecList(String category) {
        Map map = new HashMap();
        //1.根据商品分类名称获取模板ID,从Redis中获取
        // key是在lehuan-sellergoods-service的ItemCatServiceImpl.java中存入的
        Long templateId = (Long) redisTemplate.boundHashOps("itemCat").get(category);
        // 如果模板ID不为空
        if (templateId != null) {
            //2.根据模板ID查询品牌列表
            List brandList = (List) redisTemplate.boundHashOps("brandList").get(templateId);
            map.put("brandList", brandList);   //返回值添加品牌列表
            //3.根据模板ID查询规格列表
            List specList = (List) redisTemplate.boundHashOps("specList").get(templateId);
            map.put("specList", specList);
        }
        return map;
    }

    @Override
    public void importList(List list) {
        solrTemplate.saveBeans(list);
        solrTemplate.commit();
    }

    @Override
    public void deleteByGoodsIds(List goodsIdList) {
        Query query = new SimpleQuery();
        Criteria criteria = new Criteria("item_goodsid").in(goodsIdList);
        query.addCriteria(criteria);
        solrTemplate.delete(query);
        solrTemplate.commit();
    }
}
