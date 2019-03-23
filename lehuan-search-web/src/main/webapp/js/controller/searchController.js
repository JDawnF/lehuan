app.controller('searchController', function ($scope, $location, searchService) {
    //定义搜索对象的结构
    $scope.searchMap = {
        'keywords': '', 'category': '', 'brand': '', 'spec': {}, 'price': '',
        'pageNo': 1, 'pageSize': 40, 'sort': '', 'sortField': ''
    };//搜索对象

    //搜索
    $scope.search = function () {
        $scope.searchMap.pageNo = parseInt($scope.searchMap.pageNo);
        searchService.search($scope.searchMap).success(
            function (response) {
                //搜索返回的结果，进行绑定，返回一个对象
                $scope.resultMap = response;
                buildPageLabel();   //构建分页栏
            }
        );
    }

    //添加搜索项，key是多个选项
    $scope.addSearchItem = function (key, value) {
        //如果用户点击的是分类或者是品牌
        if (key == 'category' || key == 'brand' || key == 'price') {
            $scope.searchMap[key] = value;  // 根据点击的选项，存入对应的列表
        } else {
            //用户点击的是规格，无法知道key是什么，所以就要用作参数传入
            $scope.searchMap.spec[key] = value;
        }
        //后端查询
        $scope.search();
    }

    //撤销搜索项
    $scope.removeSearchItem = function (key) {
        //如果用户点击的是分类或者是品牌
        if (key == 'category' || key == 'brand' || key == 'price') {
            $scope.searchMap[key] = "";
        } else {
            //用户点击的是规格，移除属性
            delete $scope.searchMap.spec[key];
        }
        //后端查询
        $scope.search();
    }
    //构建分页栏
    ItemSearchServiceImplbuildPageLabel = function () {
        //构建分页栏，即分页码1，2，3，4，5...
        $scope.pageLabel = [];
        // 8 9 10 11 12,8位开始页码，12位截止页码
        var firstPage = 1;//开始页码
        var lastPage = $scope.resultMap.totalPages;//截止页码
        $scope.firstDot = true;//前面有点
        $scope.lastDot = true;//后边有点
        if ($scope.resultMap.totalPages > 5) {  //如果总页数大于5页,显示部分页码
            if ($scope.searchMap.pageNo <= 3) {//如果当前页小于等于3
                lastPage = 5; //前5页
                $scope.firstDot = false;//前面没点
            } else if ($scope.searchMap.pageNo >= lastPage - 2) {//如果当前页大于等于最大页码-2
                firstPage = $scope.resultMap.totalPages - 4;		 //后5页
                $scope.lastDot = false;//后边没点
            } else { //显示当前页为中心的5页
                firstPage = $scope.searchMap.pageNo - 2;
                lastPage = $scope.searchMap.pageNo + 2;
            }
        } else {
            $scope.firstDot = false;//前面无点
            $scope.lastDot = false;//后边无点

        }
        //循环产生页码标签,从开始页码到截止页码，如果总页数小于5则全显示
        for (var i = firstPage; i <= lastPage; i++) {
            $scope.pageLabel.push(i);
        }
    }

    //分页查询,即跳到选中的页码
    $scope.queryByPage = function (pageNo) {
        //页码验证
        if (pageNo < 1 || pageNo > $scope.resultMap.totalPages) {
            return;
        }
        $scope.searchMap.pageNo = pageNo;
        $scope.search();
    }
    //判断当前页为第一页
    $scope.isTopPage = function () {
        if ($scope.searchMap.pageNo == 1) {
            return true;
        } else {
            return false;
        }
    }

    //判断当前页是否未最后一页
    $scope.isEndPage = function () {
        if ($scope.searchMap.pageNo == $scope.resultMap.totalPages) {
            return true;
        } else {
            return false;
        }
    }

    $scope.sortSearch = function (sortField, sort) {
        $scope.searchMap.sortField = sortField;
        $scope.searchMap.sort = sort;
        $scope.search();
    }

    //判断关键字是不是品牌
    $scope.keywordsIsBrand = function () {
        //通过判断searchMap中的关键字是否包含resultMap品牌列表，注意品牌列表格式是:{id:1,text:’’}，所以要.text
        for (var i = 0; i < $scope.resultMap.brandList.length; i++) {
            if ($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text) >= 0) {
                return true;
            }
        }
        return false;
    }
    //加载查询字符串，获取location传递的参数
    $scope.loadkeywords = function () {
        $scope.searchMap.keywords = $location.search()['keywords'];
        $scope.search();
    }
});