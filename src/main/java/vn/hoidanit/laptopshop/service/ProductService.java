package vn.hoidanit.laptopshop.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import vn.hoidanit.laptopshop.domain.Cart;
import vn.hoidanit.laptopshop.domain.CartDetail;
import vn.hoidanit.laptopshop.domain.Order;
import vn.hoidanit.laptopshop.domain.OrderDetail;
import vn.hoidanit.laptopshop.domain.Product;
import vn.hoidanit.laptopshop.domain.User;
import vn.hoidanit.laptopshop.domain.dto.ProductCriteriaDTO;
import vn.hoidanit.laptopshop.repository.CartDetailRepository;
import vn.hoidanit.laptopshop.repository.CartRepository;
import vn.hoidanit.laptopshop.repository.OrderDetailRepository;
import vn.hoidanit.laptopshop.repository.OrderRepository;
import vn.hoidanit.laptopshop.repository.ProductRepository;
import vn.hoidanit.laptopshop.service.specification.ProductSpecs;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final UserService userService;
    private final CartRepository cartRepository;
    private final CartDetailRepository cartDetailRepository;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;

    public ProductService(ProductRepository productRepository, CartRepository cartRepository,
            CartDetailRepository cartDetailRepository, UserService userService, OrderRepository orderRepository,
            OrderDetailRepository orderDetailRepository) {
        this.productRepository = productRepository;
        this.cartRepository = cartRepository;
        this.cartDetailRepository = cartDetailRepository;
        this.userService = userService;
        this.orderRepository = orderRepository;
        this.orderDetailRepository = orderDetailRepository;
    }

    public Product handleSaveProduct(Product Product) {
        Product testProduct = this.productRepository.save(Product);
        return testProduct;
    }

    public Page<Product> getAllProducts(Pageable pageable, String name) {
        return this.productRepository.findAll(ProductSpecs.nameLike(name), pageable);
    }

    // public Page<Product> getAllProducts(Pageable pageable, double min) {
    // return this.productRepository.findAll(ProductSpecs.minPrice(min ), pageable);
    // }

    // public Page<Product> getAllProducts(Pageable pageable, double max) {
    // return this.productRepository.findAll(ProductSpecs.maxPrice(max ), pageable);
    // }

    // public Page<Product> getAllProducts(Pageable pageable, String factory) {
    // return this.productRepository.findAll(ProductSpecs.matchFactory(factory ),
    // pageable);
    // }

    // public Page<Product> getAllProducts(Pageable pageable, List<String>
    // factories) {
    // return
    // this.productRepository.findAll(ProductSpecs.matchListFactories(factories ),
    // pageable);
    // }

    // public Page<Product> getAllProducts(Pageable pageable, String price) {
    // if (price.equals("10-toi-15-trieu")) {
    // double min = 10000000;
    // double max = 15000000;
    // return this.productRepository.findAll(ProductSpecs.matchPrice(min, max),
    // pageable);
    // } else if (price.equals("16-toi-30-trieu")) {
    // double min = 16000000;
    // double max = 30000000;
    // return this.productRepository.findAll(ProductSpecs.matchPrice(min, max),
    // pageable);
    // } else
    // return this.productRepository.findAll(pageable);
    // }

    public Specification<Product> buildPriceSpecification(List<String> price) {
        Specification<Product> combinedSpec = Specification.where(null);
        for (String s : price) {
            double min = 0;
            double max = 0;
            switch (s) {
                case "duoi-10-trieu":
                    min = 0;
                    max = 10000000;
                    break;
                case "10-15-trieu":
                    min = 10000000;
                    max = 15000000;
                    break;
                case "15-20-trieu":
                    min = 15000000;
                    max = 20000000;
                    break;
                case "tren-20-trieu":
                    min = 20000000;
                    max = 200000000;
                    break;
            }
            if (min != 0 && max != 0) {
                Specification<Product> rangeSpec = ProductSpecs.matchMultiplePrice(min, max);
                combinedSpec = combinedSpec.or(rangeSpec);
            }
        }

        return combinedSpec;
    }

    public Page<Product> getAllProducts(Pageable pageable) {

        return this.productRepository.findAll(pageable);
    }

    public Page<Product> fetchProducts(Pageable pageable, ProductCriteriaDTO productCriteriaDTO) {
        // get page no filter when first load
        if (productCriteriaDTO.getFactory() == null && productCriteriaDTO.getTarget() == null
                && productCriteriaDTO.getPrice() == null) {
            return productRepository.findAll(pageable);
        }
        // this one is combined include many specifications (condition where)
        Specification<Product> combinedSpec = Specification.where(null);

        if (productCriteriaDTO.getFactory() != null && productCriteriaDTO.getFactory().isPresent()) {
            Specification<Product> currentSpecs = ProductSpecs.matchListFactory(productCriteriaDTO.getFactory().get());
            combinedSpec = combinedSpec.and((currentSpecs));
        }

        if (productCriteriaDTO.getTarget() != null && productCriteriaDTO.getTarget().isPresent()) {
            Specification<Product> currentSpecs = ProductSpecs.matchListTarget(productCriteriaDTO.getTarget().get());
            combinedSpec = combinedSpec.and(currentSpecs);
        }

        if (productCriteriaDTO.getPrice() != null && productCriteriaDTO.getPrice().isPresent()) {
            Specification<Product> currentSpecs = this.buildPriceSpecification(productCriteriaDTO.getPrice().get());
            combinedSpec = combinedSpec.and(currentSpecs);
        }

        return productRepository.findAll(combinedSpec, pageable);
    }

    public Optional<Product> getProductById(long id) {
        return this.productRepository.findById(id);
    }

    public void deleteAProduct(long id) {
        this.productRepository.deleteById(id);
    }

    public void handleAddProductToCart(String email, long productId, HttpSession session, long quantity) {
        User user = this.userService.getUserByEmail(email);
        if (user != null) {
            Cart cart = this.cartRepository.findCartByUser(user);
            if (cart == null) {
                Cart newCart = new Cart();
                newCart.setSum(0);
                newCart.setUser(user);
                cart = this.cartRepository.save(newCart);
            }
            Optional<Product> productOptional = this.productRepository.findById(productId);
            if (productOptional.isPresent()) {

                CartDetail oldCartDetail = this.cartDetailRepository.findByProductAndCart(productOptional.get(), cart);
                if (oldCartDetail == null) {
                    Product realProduct = productOptional.get();
                    CartDetail cartDetail = new CartDetail();
                    cartDetail.setProduct(realProduct);
                    cartDetail.setPrice(realProduct.getPrice());
                    cartDetail.setQuantity(quantity);
                    cartDetail.setCart(cart);
                    this.cartDetailRepository.save(cartDetail);
                    // update cart sum
                    int s = cart.getSum() + 1;
                    cart.setSum(s);
                    this.cartRepository.save(cart);
                    session.setAttribute("sum", s);
                } else {
                    oldCartDetail.setQuantity(oldCartDetail.getQuantity() + quantity);
                    this.cartDetailRepository.save(oldCartDetail);
                }
            }

        }
    }

    public Cart fetchByUser(User user) {
        return this.cartRepository.findCartByUser(user);
    }

    public void handleRemoveCartDetail(long cartDetailId, HttpSession session) {
        Optional<CartDetail> cartDetailOptional = this.cartDetailRepository.findById(cartDetailId);
        if (cartDetailOptional.isPresent()) {
            CartDetail cartDetail = cartDetailOptional.get();
            Cart cart = cartDetail.getCart();
            this.cartDetailRepository.deleteById(cartDetailId);
            int sum = cart.getSum();
            if (sum > 1) {
                sum = sum - 1;
                cart.setSum(sum);
                this.cartRepository.save(cart);
            } else {
                sum = sum - 1;
                this.productRepository.deleteById(cart.getId());
            }
            session.setAttribute("sum", sum);
        }
    }

    public void handleUpdateCartBeforeCheckout(List<CartDetail> cartDetails) {
        for (CartDetail cartDetail : cartDetails) {
            Optional<CartDetail> cdOptional = this.cartDetailRepository.findById(cartDetail.getId());
            CartDetail currentCartDetail = cdOptional.get();
            currentCartDetail.setQuantity(cartDetail.getQuantity());
            this.cartDetailRepository.save(currentCartDetail);
        }
    }

    public void handlePlaceOrder(User user, HttpServletRequest request, String receiverName, String receiverAddress,
            String receiverPhone, HttpSession session) {

        Cart cart = this.cartRepository.findCartByUser(user);
        double totalPrice = 0;
        cart.getCartDetails();
        if (cart != null) {
            List<CartDetail> cartDetails = cart.getCartDetails();
            if (cartDetails != null) {
                // step 0: caculate totalprice
                for (CartDetail cd : cartDetails) {
                    totalPrice += cd.getPrice();
                }
                // step 1 : create an order
                Order order = new Order();
                order.setUser(user);
                order.setReceiverAddress(receiverAddress);
                order.setReceiverName(receiverName);
                order.setReceiverPhone(receiverPhone);
                order.setStatus("PENDING");
                order.setTotalPrice(totalPrice);
                this.orderRepository.save(order);
                // step 2: create order detail for each cart detail
                for (CartDetail cd : cartDetails) {
                    OrderDetail orderDetail = new OrderDetail();
                    orderDetail.setOrder(order);
                    orderDetail.setPrice(cd.getPrice());
                    orderDetail.setProduct(cd.getProduct());
                    orderDetail.setQuantity(cd.getQuantity());
                    this.orderDetailRepository.save(orderDetail);
                }
                // step 3: delete cart detail and cart
                for (CartDetail cd : cartDetails) {
                    this.cartDetailRepository.deleteById(cd.getId());
                }
                this.cartRepository.deleteById(cart.getId());
                session.setAttribute("sum", 0);
            }

        }

    }
}
