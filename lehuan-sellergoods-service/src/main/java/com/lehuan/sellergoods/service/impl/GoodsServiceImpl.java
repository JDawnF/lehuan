package com.lehuan.sellergoods.service.impl;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.lehuan.group.Goods;
import com.lehuan.mapper.*;
import com.lehuan.pojo.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.lehuan.pojo.TbGoodsExample.Criteria;
import com.lehuan.sellergoods.service.GoodsService;

import entity.PageResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
@Transactional
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private TbGoodsMapper goodsMapper;
    @Autowired
    private TbGoodsDescMapper goodsDescMapper;
    @Autowired
    private TbItemMapper itemMapper;
    @Autowired
    private TbBrandMapper brandMapper;
    @Autowired
    private TbSellerMapper sellerMapper;
    @Autowired
    private TbItemCatMapper itemCatMapper;

    /**
     * 查询全部
     */
    @Override
    public List<TbGoods> findAll() {
        return goodsMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbGoods> page = (Page<TbGoods>) goodsMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加商品，需要根据数据库中商品表中有什么字段就存入什么样的值
     */
    @Override
    public void add(Goods goods) {
        goods.getGoods().setAuditStatus("0");   //状态：未审核
//        插入商品基本信息
        goodsMapper.insert(goods.getGoods());
//  将商品基本表的ID给商品扩展表，在mapper文件中有一个返回主键的语句
        goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());
        //插入商品扩展表数据
        goodsDescMapper.insert(goods.getGoodsDesc());
        //插入SKU商品数据
        saveItemList(goods);
    }

    private void setItemValus(Goods goods, TbItem item) {
        item.setGoodsId(goods.getGoods().getId());//商品SPU编号
        item.setSellerId(goods.getGoods().getSellerId());//商家编号
        item.setCategoryid(goods.getGoods().getCategory3Id());//商品分类编号（3级）
        item.setCreateTime(new Date());//创建日期
        item.setUpdateTime(new Date());//修改日期

        //品牌名称
        TbBrand brand = brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
        item.setBrand(brand.getName());
        //分类名称
        TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(goods.getGoods().getCategory3Id());
        item.setCategory(itemCat.getName());

        //商家名称
        TbSeller seller = sellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId());
        item.setSeller(seller.getNickName());

        //图片地址（取spu的第一个图片）,主要看商品规格表中item_images字段，图片还有一个颜色的属性，而url是其中的key，所以就取这个url的值即可
        List<Map> imageList = JSON.parseArray(goods.getGoodsDesc().getItemImages(), Map.class);
        if (imageList.size() > 0) {
            item.setImage((String) imageList.get(0).get("url"));
        }

    }

    private void saveItemList(Goods goods) {
        //用户是否选择显示规格
        if ("1".equals(goods.getGoods().getIsEnableSpec())) {
            for (TbItem item : goods.getItemList()) {
//            构建标题  SPU名称+规格选项值,规格选项值在item表中有
                String title = goods.getGoods().getGoodsName();
                Map<String, Object> specMap = JSON.parseObject(item.getSpec());
                for (String key : specMap.keySet()) {
                    title += " " + specMap.get(key);
                }
                item.setTitle(title);
                setItemValus(goods, item);
                itemMapper.insert(item);
            }
        } else {
//            没有启动规格
            TbItem item = new TbItem();
            item.setTitle(goods.getGoods().getGoodsName());//商品KPU+规格描述串作为SKU名称
            item.setPrice(goods.getGoods().getPrice());//价格
            item.setStatus("1");//状态
            item.setIsDefault("1");//是否默认
            item.setNum(99999);//库存数量
            item.setSpec("{}"); //没有规格存一个空括号
            setItemValus(goods, item);
            itemMapper.insert(item);
        }
    }

    /**
     * 修改
     */
    @Override
    public void update(Goods goods) {
        //设置未申请状态:如果是经过修改的商品，需要重新设置状态
        goods.getGoods().setAuditStatus("0");
        //更新基本表数据
        goodsMapper.updateByPrimaryKey(goods.getGoods());
        //更新扩展表数据
        goodsDescMapper.updateByPrimaryKey(goods.getGoodsDesc());
        //删除原有的SKU列表，及规格属性数据
        TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andGoodsIdEqualTo(goods.getGoods().getId());
        itemMapper.deleteByExample(example);
        //插入新的SKU列表数据
        saveItemList(goods);
    }

    /**
     * 根据ID获取实体,要显示规格和商品具体相关属性
     *
     * @param id
     * @return
     */
    @Override
    public Goods findOne(Long id) {
        Goods goods = new Goods();
        //商品基本表
        TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
        goods.setGoods(tbGoods);
        //商品扩展表
        TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(id);
        goods.setGoodsDesc(goodsDesc);
        // 读取SKU列表
        TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        //查询条件：商品ID
        criteria.andGoodsIdEqualTo(id);
        List<TbItem> itemList = itemMapper.selectByExample(example);
        goods.setItemList(itemList);
        return goods;
        //return goodsMapper.selectByPrimaryKey(id);
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
            tbGoods.setIsDelete("1");   //表示逻辑删除,修改商品状态
            goodsMapper.updateByPrimaryKey(tbGoods);
//            物理删除,而我们要实现逻辑删除
//            goodsMapper.deleteByPrimaryKey(id);
        }
    }


    @Override
    public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbGoodsExample example = new TbGoodsExample();
        Criteria criteria = example.createCriteria();
        //非删除状态,增加条件IsDelete字段为空
        criteria.andIsDeleteIsNull();
        if (goods != null) {
            if (goods.getSellerId() != null && goods.getSellerId().length() > 0) {
                //criteria.andSellerIdLike("%" + goods.getSellerId() + "%");
                criteria.andSellerIdEqualTo(goods.getSellerId());
            }
            if (goods.getGoodsName() != null && goods.getGoodsName().length() > 0) {
                criteria.andGoodsNameLike("%" + goods.getGoodsName() + "%");
            }
            if (goods.getAuditStatus() != null && goods.getAuditStatus().length() > 0) {
                criteria.andAuditStatusLike("%" + goods.getAuditStatus() + "%");
            }
            if (goods.getIsMarketable() != null && goods.getIsMarketable().length() > 0) {
                criteria.andIsMarketableLike("%" + goods.getIsMarketable() + "%");
            }
            if (goods.getCaption() != null && goods.getCaption().length() > 0) {
                criteria.andCaptionLike("%" + goods.getCaption() + "%");
            }
            if (goods.getSmallPic() != null && goods.getSmallPic().length() > 0) {
                criteria.andSmallPicLike("%" + goods.getSmallPic() + "%");
            }
            if (goods.getIsEnableSpec() != null && goods.getIsEnableSpec().length() > 0) {
                criteria.andIsEnableSpecLike("%" + goods.getIsEnableSpec() + "%");
            }
            if (goods.getIsDelete() != null && goods.getIsDelete().length() > 0) {
                criteria.andIsDeleteLike("%" + goods.getIsDelete() + "%");
            }

        }

        Page<TbGoods> page = (Page<TbGoods>) goodsMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

    //更新商品状态
    @Override
    public void updateStatus(Long[] ids, String status) {
        for (Long id : ids) {
            TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
            tbGoods.setAuditStatus(status);
            goodsMapper.updateByPrimaryKey(tbGoods);
        }
    }

    @Override
    public List<TbItem> findItemListByGoodsIdAndStatus(Long[] goodsIds, String status) {
        TbItemExample example=new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andGoodsIdIn(Arrays.asList(goodsIds));   //商品ID集合
        criteria.andStatusEqualTo(status);   //状态
        return itemMapper.selectByExample(example);
    }

}
