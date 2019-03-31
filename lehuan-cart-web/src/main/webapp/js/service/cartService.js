//购物车服务层
app.service('cartService', function ($http) {
    //购物车列表
    this.findCartList = function () {
        return $http.get('cart/findCartList.do');
    }
    //添加商品到购物车
    this.addGoodsToCartList = function (itemId, num) {
        return $http.get('cart/addGoodsToCartList.do?itemId=' + itemId + '&num=' + num);
    }
    //求合计商品数量,遍历每个商家下面的商品订单list对应的具体商品订单
    this.sum = function (cartList) {
        var totalValue = {totalNum: 0, totalMoney: 0.00};   //合计实体，是一个Json格式的变量
        for (var i = 0; i < cartList.length; i++) {
            //购物车对象，购物车列表是一个大list，大list里面是每个商家对应的商品订单list
            var cart = cartList[i];
            // 遍历商品订单list
            for (var j = 0; j < cart.orderItemList.length; j++) {
                var orderItem = cart.orderItemList[j];  //购物车中的商品订单明细
                totalValue.totalNum += orderItem.num;
                totalValue.totalMoney += orderItem.totalFee;
            }
        }
        return totalValue;
    }

    //获取当前登录账号的收货地址列表
    this.findAddressList = function () {
        return $http.get('address/findListByLoginUser.do');
    }
    //保存订单
    this.submitOrder = function (order) {
        return $http.post('order/add.do', order);
    }
});
