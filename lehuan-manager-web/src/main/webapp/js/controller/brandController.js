//品牌控制层,$controller继承要使用传递过来的服务
app.controller('brandController', function ($scope, $controller, brandService) {
    //继承,其实是一种伪继承，通过传递$scope来实现继承
    $controller('baseController',{$scope:$scope});

    //查询品牌列表,get请求地址，即项目中的地址,findAll为函数名，要请求后端的接口
    $scope.findAll = function () {
        brandService.findAll().success(
            // function中的参数名称随意
            function (response) {
                $scope.list = response;
            }
        );
    }

    //刷新列表(局部刷新)，即一次查询
    $scope.reloadList = function () {
        $scope.search($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
        // $scope.findPage($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
    }

    //分页方法，要请求后端分页接口
    $scope.findPage = function (page, rows) {
        brandService.findPage(page, rows).success(
            function (response) {
                $scope.list = response.rows;//显示当前页数据
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }

    //     新增和保存
    $scope.save = function () {
        var object = null;     //方法名
        // 如果等于null，则为修改，不等于null则为新增
        if ($scope.entity.id != null) {
            object = brandService.update($scope.entity);
        } else {
            object = brandService.add($scope.entity);
        }
        //    post请求，所以要添加另一个参数变量$scope.entity表明是post里面是什么数据
        object.success(
            function (response) {
                if (response.success) {
                    $scope.reloadList();    //成功即刷新
                } else {
                    alert(response.message);    //提示错误
                }
            }
        );
    }
    //查询品牌实体
    $scope.findOne = function (id) {
        brandService.findOne(id).success(
            function (response) {
                //双向绑定
                $scope.entity = response;
            }
        );
    }

    //删除
    $scope.dele = function () {
        if (confirm('确定要删除吗？')) {
            brandService.dele($scope.selectIds).success(
                function (response) {
                    if (response.success) {
                        $scope.reloadList();//刷新
                    } else {
                        alert(response.message);
                    }
                }
            );
        }
    }
    //初始化searchEntity，否则前端会报错400，在列表中也需要传参page，size，
    // 可以直接调用reloadList相当于调用了search传参
    $scope.searchEntity = {};
    //条件查询
    $scope.search = function (page, rows) {
        brandService.search(page,rows,$scope.searchEntity).success(
            function (response) {
                $scope.list = response.rows;//显示当前页数据
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }
});
