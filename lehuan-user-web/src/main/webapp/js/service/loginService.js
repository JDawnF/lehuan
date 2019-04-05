//服务层
app.service('loginService',function($http){
    //读取列表数据绑定到表单中，显示用户名
    this.showName=function(){
        return $http.get('../login/name.do');
    }
});
