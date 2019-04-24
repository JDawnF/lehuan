package com.lehuan.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.lehuan.pojo.TbBrand;
import com.lehuan.sellergoods.service.BrandService;
import entity.PageResult;
import entity.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @program: lehuan-parent
 * @description: 商品品牌控制层
 * @author: baichen
 **/
@RestController
@RequestMapping("/brand")
public class BrandController {
    @Reference  // Dubbo服务调用注解
    private BrandService brandService;

    @RequestMapping("/findAll")
    public List<TbBrand> findAll() {
        return brandService.findAll();
    }

    //    分页返回品牌列表
    @RequestMapping("/findPage")
    public PageResult findPage(int page, int size) {
        PageResult brandServicePage = brandService.findPage(page, size);
        return brandServicePage;
    }

    //    分页按条件查询返回品牌列表,RequestBody将 HTTP 请求正文插入方法中，
//1) 该注解用于读取Request请求的body部分数据，使用系统默认配置的HttpMessageConverter进行解析，然后把相应的数据绑定到要返回的对象上；
//2) 再把HttpMessageConverter返回的对象数据绑定到 controller中方法的参数上。
    @RequestMapping("/search")
    public PageResult search(@RequestBody TbBrand brand, int page,int size){
        return brandService.findPage(brand,page,size);
    }

    //    增加商品品牌,前端通过post传数据过来,所以要用RequestBody注解
    @RequestMapping("/add")
    public Result add(@RequestBody TbBrand brand) {
        try {
            brandService.add(brand);
            return new Result(true, "添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "添加失败");
        }
    }

    @RequestMapping("/findOne")
    public TbBrand findOne(Long id) {
        return brandService.findOne(id);
    }

    @RequestMapping("/update")
    public Result update(@RequestBody TbBrand brand) {
        try {
            brandService.update(brand);
            return new Result(true, "修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "修改失败");
        }
    }

    @RequestMapping("/delete")
    public Result delete(Long[] ids) {
        try {
            brandService.delete(ids);
            return new Result(true, "删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "删除失败");
        }
    }
//    下拉列表
    @RequestMapping("/selectOptionList")
    public List<Map> selectOptionList(){
        return brandService.selectOptionList();
    }

}
