package reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reggie.common.R;
import reggie.dto.DishDto;
import reggie.dto.OrdersDto;
import reggie.entity.Category;
import reggie.entity.Dish;
import reggie.entity.OrderDetail;
import reggie.entity.Orders;
import reggie.service.OrderDetailService;
import reggie.service.OrderService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单
 */
@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderDetailService orderDetailService;

    /**
     * 用户下单
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        log.info("订单数据：{}",orders);
        orderService.submit(orders);
        return R.success("下单成功");
    }


    /**
     * 分页查询
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        log.info("page:{},pageSize", page, pageSize);
        //构造分页对象
        Page<Orders> pageInfo = new Page(page, pageSize);
        Page<OrdersDto> ordersDtoPage = new Page<>();

        //构造条件构造器
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(Orders::getOrderTime).like(StringUtils.isNotEmpty(name), Orders::getId, name);
        orderService.page(pageInfo, queryWrapper);
        //对象拷贝
        BeanUtils.copyProperties(pageInfo, ordersDtoPage, "records");
        List<Orders> records = pageInfo.getRecords();
        List<OrdersDto> list = records.stream().map(item -> {
            OrdersDto ordersDto = new OrdersDto();
            BeanUtils.copyProperties(item, ordersDto);
            OrderDetail orderDetail = orderDetailService.getById(item.getUserId());
            if (orderDetail != null) {
                ordersDto.setUserName(orderDetail.getName());
            }

            return ordersDto;
        }).collect(Collectors.toList());
        ordersDtoPage.setRecords(list);

        return R.success(ordersDtoPage);
    }

}