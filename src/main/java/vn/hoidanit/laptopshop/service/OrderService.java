package vn.hoidanit.laptopshop.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import vn.hoidanit.laptopshop.domain.Order;
import vn.hoidanit.laptopshop.domain.OrderDetail;
import vn.hoidanit.laptopshop.domain.User;
import vn.hoidanit.laptopshop.repository.OrderDetailRepository;
import vn.hoidanit.laptopshop.repository.OrderRepository;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;

    public OrderService(OrderRepository orderRepository, OrderDetailRepository orderDetailRepository) {
        this.orderRepository = orderRepository;
        this.orderDetailRepository = orderDetailRepository;
    }

    public Order handleSaveOrder(Order order) {
        return this.orderRepository.save(order);
    }

    public List<Order> getAllOrders() {
        List<Order> arrOrders;
        arrOrders = this.orderRepository.findAll();
        return arrOrders;
    }

    public Optional<Order> getOrderById(long id) {
        return this.orderRepository.findById(id);
    }

    public void deleteAOrder(long id) {
        Order order = this.orderRepository.findById(id).get();
        if (order != null) {
            List<OrderDetail> orderDetails = order.getOrderDetails();
            if (orderDetails != null) {
                for (OrderDetail orderDetail : orderDetails) {
                    this.orderDetailRepository.deleteById(orderDetail.getId());
                }
            }
        }
        this.orderRepository.deleteById(id);
    }

    public List<Order> fetchOrdersByUser(User user) {
        return this.orderRepository.findByUser(user);
    }
}
