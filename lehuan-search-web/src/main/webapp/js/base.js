// 定义模块:
var app = angular.module("lehuan", []);
/*$sce服务写成过滤器,一个过滤器只做一件事*/
app.filter('trustHtml', ['$sce', function ($sce) {
    return function (data) {//传入参数是被过滤的内容(信任HTML的转换)
        return $sce.trustAsHtml(data);//返回过滤后的内容
    }
}]);