package vn.hoidanit.laptopshop.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import vn.hoidanit.laptopshop.domain.Product;
import vn.hoidanit.laptopshop.repository.ProductRepository;

@Service
public class ProductService {
    private final ProductRepository ProductRepository;

    public ProductService(ProductRepository ProductRepository) {
        this.ProductRepository = ProductRepository;
    }

    public Product handleSaveProduct(Product Product) {
        Product testProduct = this.ProductRepository.save(Product);
        return testProduct;
    }

    public List<Product> getAllProducts() {
        List<Product> arrProducts;
        arrProducts = this.ProductRepository.findAll();
        return arrProducts;
    }

    public Optional<Product> getProductById(long id) {
        return this.ProductRepository.findById(id);
    }

    public void deleteAProduct(long id) {
        this.ProductRepository.deleteById(id);
    }

}
