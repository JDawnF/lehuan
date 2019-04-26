//文件上传服务层
app.service("uploadService", function ($http) {
    this.uploadFile = function () {
        // k-v的数据表单
        var formData = new FormData();
        formData.append("file", file.files[0]);
        return $http({
            method: 'POST',
            url: "../upload.do",
            data: formData,
// 如果不指定为undefined默认为json格式
            headers: {'Content-Type': undefined},
//要对表单进行二进制的序列化
            transformRequest: angular.identity
        });
    }
});
