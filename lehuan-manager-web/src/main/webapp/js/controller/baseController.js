app.controller('baseController', function ($scope) {
    //刷新列表(局部刷新)，即一次查询
    $scope.reloadList = function () {
        $scope.search($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
        // $scope.findPage($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
    }
    //分页控件配置   currentPage:当前页   totalItems :总记录数  itemsPerPage:每页记录数
    // perPageOptions :分页选项,即选择每页显示多少条记录  onChange:当页码变更后自动触发的方法
    //要保证paginationConf跟下面的tm-pagination标签中的一样
    $scope.paginationConf = {
        currentPage: 1,
        totalItems: 10,
        itemsPerPage: 10,
        perPageOptions: [10, 20, 30, 40, 50],
        onChange: function () {
            // $scope.search($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
            $scope.reloadList();    //刷新
        }
    };
    $scope.selectIds = [];  //用户勾选的ID集合,构建一个数组
    //用户勾选复选框,即要删除的选项,$event表示源
    $scope.updateSelection = function ($event, id) {
        //被勾选的时候才添加到id数组中，$event.target指向this
        if ($event.target.checked) {
            $scope.selectIds.push(id);  //用push方法向集合添加元素
        } else {
            //js方法
            var index = $scope.selectIds.indexOf(id);//查找值的 位置
            $scope.selectIds.splice(index, 1);//参数1：移除的位置 参数2：移除的个数
        }
    }
    //提取json字符串数据中某个属性，返回拼接字符串 逗号分隔
    $scope.jsonToString = function (jsonString, key) {
        //将json字符串转换为json对象
        var json = JSON.parse(jsonString);
        var value = "";
        for (var i = 0; i < json.length; i++) {
            //去掉第一个的逗号
            if (i > 0) {
                value += ",";
            }
            value += json[i][key];
        }
        return value;
    }

});