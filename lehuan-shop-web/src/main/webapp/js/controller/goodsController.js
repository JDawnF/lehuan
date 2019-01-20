//控制层
app.controller('goodsController', function ($scope, $controller, $location, goodsService, uploadService, itemCatService, typeTemplateService) {

        $controller('baseController', {$scope: $scope});//继承

        //读取列表数据绑定到表单中
        $scope.findAll = function () {
            goodsService.findAll().success(
                function (response) {
                    $scope.list = response;
                }
            );
        }

        //分页
        $scope.findPage = function (page, rows) {
            goodsService.findPage(page, rows).success(
                function (response) {
                    $scope.list = response.rows;
                    $scope.paginationConf.totalItems = response.total;//更新总记录数
                }
            );
        }

        //查询实体
        $scope.findOne = function () {
            //search方法可以获取页面上的所有参数值
            var id = $location.search()['id'];
            if (id == null) {
                return;
            }
            goodsService.findOne(id).success(
                function (response) {
                    $scope.entity = response;
                    //向富文本编辑器添加商品介绍
                    editor.html($scope.entity.goodsDesc.introduction);
                    //显示图片列表,先转格式
                    $scope.entity.goodsDesc.itemImages = JSON.parse($scope.entity.goodsDesc.itemImages);
                    //显示扩展属性
                    $scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.entity.goodsDesc.customAttributeItems);
                    //规格
                    $scope.entity.goodsDesc.specificationItems = JSON.parse($scope.entity.goodsDesc.specificationItems);
                    //SKU列表规格列转换,字符串转为json对象
                    for (var i = 0; i < $scope.entity.itemList.length; i++) {
                        $scope.entity.itemList[i].spec = JSON.parse($scope.entity.itemList[i].spec);
                    }
                }
            );
        }

        // //增加商品
        // $scope.add = function () {
        //     //提取kindeditor编辑器的内容:editor.html()
        //     $scope.entity.goodsDesc.introduction = editor.html();
        //     goodsService.add($scope.entity).success(
        //         function (response) {
        //             if (response.success) {
        //                 alert("新增成功")
        //                 //新增之后清空，因为不是列表不用重新刷新
        //                 $scope.entity = {};
        //                 //清空富文本编辑器
        //                 editor.html("")
        //             } else {
        //                 alert(response.message);
        //             }
        //         }
        //     );
        // }

        //保存
        $scope.save = function () {
            //提取文本编辑器的值
            $scope.entity.goodsDesc.introduction=editor.html();
            var serviceObject;//服务层对象
            if ($scope.entity.goods.id != null) {//如果有ID
                alert($scope.entity.id)
                serviceObject = goodsService.update($scope.entity); //修改
            } else {
                serviceObject = goodsService.add($scope.entity);//增加
            }
            serviceObject.success(
                function (response) {
                    if (response.success) {
                        location.href="goods.html";//跳转到商品列表页
                    } else {
                        alert(response.message);
                    }
                }
            );
        }


        //批量删除
        $scope.dele = function () {
            //获取选中的复选框
            goodsService.dele($scope.selectIds).success(
                function (response) {
                    if (response.success) {
                        $scope.reloadList();//刷新列表
                        $scope.selectIds = [];
                    }
                }
            );
        }

        $scope.searchEntity = {};//定义搜索对象

        //搜索
        $scope.search = function (page, rows) {
            goodsService.search(page, rows, $scope.searchEntity).success(
                function (response) {
                    $scope.list = response.rows;
                    $scope.paginationConf.totalItems = response.total;//更新总记录数
                }
            );
        }
//    上传图片
        $scope.uploadFile = function () {
            uploadService.uploadFile().success(
                function (response) {
                    //如果上传成功，取出url
                    if (response.success) {
                        $scope.image_entity.url = response.message;//设置文件地址
                    } else {
                        alert(response.message);
                    }
                });
        }
        //定义页面实体结构，与数据库中的goodsDesc字段一样,相当于初始化
        $scope.entity = {goodsDesc: {itemImages: [], specificationItems: []}};
//    将当前上传的图片实体存入图片列表
        $scope.add_image_entity = function () {
            $scope.entity.goodsDesc.itemImages.push($scope.image_entity);
        }
//    列表中移除照片
        $scope.remove_image_entity = function (index) {
            $scope.entity.goodsDesc.itemImages.splice(index, 1);
        }
//    查询一级商品分类下拉列表
        $scope.selectItemCat1List = function () {
            itemCatService.findByParentId(0).success(
                function (response) {
                    //将结果存入到itemCat1List中，它相当于数据库中分类表中的一条记录
                    $scope.itemCat1List = response;
                }
            );
        }
        //查询二级商品分类下拉列表
        //$watch方法用于监控某个变量的值,是一个方法，当被监控的值发生变化，就自动执行相应的函数。
        $scope.$watch('entity.goods.category1Id', function (newValue, oldValue) {
            //根据选择的值，查询二级分类
            itemCatService.findByParentId(newValue).success(
                function (response) {
                    $scope.itemCat2List = response;
                }
            );
        });
        //查询三级商品分类下拉列表
        //$watch方法用于监控某个变量的值,是一个方法，当被监控的值发生变化，就自动执行相应的函数。
        $scope.$watch('entity.goods.category2Id', function (newValue, oldValue) {
            //根据选择的值，查询二级分类
            itemCatService.findByParentId(newValue).success(
                function (response) {
                    $scope.itemCat3List = response;
                }
            );
        });
//    读取模板ID
        $scope.$watch('entity.goods.category2Id', function (newValue, oldValue) {
            //根据选择的值，查询二级分类
            itemCatService.findOne(newValue).success(
                function (response) {
                    //更新模板ID
                    $scope.entity.goods.typeTemplateId = response.typeId;
                }
            );
        });
        //模板ID选择后  更新品牌列表,监控模板id=newValue
        $scope.$watch('entity.goods.typeTemplateId', function (newValue, oldValue) {
            typeTemplateService.findOne(newValue).success(
                function (response) {
                    $scope.typeTemplate = response;//获取类型模板
                    $scope.typeTemplate.brandIds = JSON.parse($scope.typeTemplate.brandIds);//品牌列表
                    //扩展属性，首先，在模板表tb_type_template中会有一个扩展属性custom_attribute_items的字段，
                    // 然后我们填充对应的扩展属性和值之后就会存放到tb_goods_desc表里的字段custom_attribute_items
                    //如果没有ID，则加载模板中的扩展数据
                    if ($location.search()['id'] == null) {
                        $scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.typeTemplate.customAttributeItems);
                    }
                }
            );
            //查询规格列表
            typeTemplateService.findSpecList(newValue).success(
                function (response) {
                    $scope.specList = response;
                })
        });
        //更新参数属性，如果有就添加，没有就创造一个
        $scope.updateSpecAttribute = function ($event, name, value) {
            var object = $scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems, 'attributeName', name);
            if (object != null) {
                if ($event.target.checked) {
                    object.attributeValue.push(value);
                } else {
                    //取消勾选,移除选项
                    object.attributeValue.splice(object.attributeValue.indexOf(value), 1);
                    if (object.attributeValue.length == 0) {
                        $scope.entity.goodsDesc.specificationItems.splice(
                            $scope.entity.goodsDesc.specificationItems.indexOf(object), 1);
                    }
                }
            } else {
                $scope.entity.goodsDesc.specificationItems.push(
                    {"attributeName": name, "attributeValue": [value]});
            }
        }
        //创建SKU列表
        $scope.createItemList = function () {
            //初始化一个不带规格名称的集合，只有一条记录
            $scope.entity.itemList = [{spec: {}, price: 0, num: 99999, status: '0', isDefault: '0'}];
            var items = $scope.entity.goodsDesc.specificationItems;
            //    循环用户选择的规格，根据规格名称和已选择的规格选项对原集合进行扩充，
            // 添加规格名称和值，新增的记录数与选择的规格选项个数相同
            for (var i = 0; i < items.length; i++) {
                //[{"attributeName":"网络","attributeValue":["移动3G"]},{"attributeName":"机身内存","attributeValue":["64G"]}]
                $scope.entity.itemList = addColumn($scope.entity.itemList, items[i].attributeName, items[i].attributeValue);
            }
        }
        //添加列值,相当于控制器的一个私有方法
        addColumn = function (list, columnName, columnValues) {
            //新的集合
            var newList = [];
            for (var i = 0; i < list.length; i++) {
                var oldRow = list[i];
                for (var j = 0; j < columnValues.length; j++) {
                    //深克隆
                    var newRow = JSON.parse(JSON.stringify(oldRow));
                    newRow.spec[columnName] = columnValues[j];
                    newList.push(newRow);
                }
            }
            return newList;
        }
        //商品状态
        $scope.status = ['未审核', '已审核', '审核未通过', '已关闭'];
        $scope.itemCatList = [];  //商品分类列表,是这样的格式：['','']
        //查询商品分类列表
        $scope.findItemCatList = function () {
            itemCatService.findAll().success(
                function (response) {
                    for (var i = 0; i < response.length; i++) {
                        $scope.itemCatList[response[i].id] = response[i].name;
                    }
                }
            );
        }
        //根据规格名称和选项名称返回是否被勾选,返回true或false
        $scope.checkAttributeValue = function (specName, optionName) {
            var items = $scope.entity.goodsDesc.specificationItems;
            //searchObjectByKey在list集合中根据某key的值查询对象,嵌套数组
            //[{“attributeName”:”规格名称”,”attributeValue”:[“规格选项1”,“规格选项2”.... ]  } , ....  ]
            //object返回最外层数组
            var object = $scope.searchObjectByKey(items, 'attributeName', specName);
            if (object != null) {
                //如果可以查到规格选项
                if (object.attributeValue.indexOf(optionName) >= 0) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
    }
);
