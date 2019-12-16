package miaoshaproject.service;

import miaoshaproject.error.BusinessException;
import miaoshaproject.service.model.OrderModel;

public interface OrderService {
    //使用 1 通过前端url传秒杀id，然后在下单接口校验id
    //2 直接在下单接口判断是否存在秒杀活动，若存在则秒杀下单
    OrderModel createOrder(Integer userId,Integer itemId,Integer promoId, Integer amount) throws BusinessException;
}
