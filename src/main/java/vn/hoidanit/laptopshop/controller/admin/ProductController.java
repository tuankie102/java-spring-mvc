package vn.hoidanit.laptopshop.controller.admin;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import vn.hoidanit.laptopshop.domain.Product;
import vn.hoidanit.laptopshop.domain.User;
import vn.hoidanit.laptopshop.service.ProductService;
import vn.hoidanit.laptopshop.service.UploadFileService;

@Controller
public class ProductController {
    private final ProductService productService;
    private final UploadFileService uploadFileService;

    public ProductController(ProductService productService, UploadFileService uploadFileService) {
        this.productService = productService;
        this.uploadFileService = uploadFileService;
    }

    @GetMapping("/admin/product")
    public String getProductPage(Model model, @RequestParam("page") int page) {
        Pageable pageable = PageRequest.of(page - 1, 2);
        Page<Product> productsPage = this.productService.getAllProducts(pageable);
        List<Product> products = productsPage.getContent();
        int totalPages = productsPage.getTotalPages();
        model.addAttribute("products", products);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        return "admin/product/show";
    }

    @GetMapping("/admin/product/create")
    public String getCreatePage(Model model) {
        model.addAttribute("newProduct", new Product());
        return "admin/product/create";
    }

    @PostMapping(value = "/admin/product/create")
    public String createProductPage(Model model, @ModelAttribute("newProduct") @Valid Product tuankiet,
            BindingResult newProductBindingResult,
            @RequestParam("productFile") MultipartFile file) {

        // validate
        if (newProductBindingResult.hasErrors()) {
            return "admin/product/create";
        }

        String productImg = this.uploadFileService.handleSaveUploadFile(file, "product");
        tuankiet.setImage(productImg);

        this.productService.handleSaveProduct(tuankiet);
        return "redirect:/admin/product";
    }

    @RequestMapping("/admin/product/update/{id}")
    public String getUpdatePage(Model model, @PathVariable long id) {
        Product currentProduct = this.productService.getProductById(id).get();
        model.addAttribute("newProduct", currentProduct);
        model.addAttribute("productImgOlder", currentProduct.getImage());
        return "admin/product/update";
    }

    @PostMapping("/admin/product/update")
    public String postUpdateProduct(Model model, @ModelAttribute("newProduct") @Valid Product tuankiet,
            BindingResult newProductBindingResult,
            @RequestParam("productFile") MultipartFile file) {
        Product currentProduct = this.productService.getProductById(tuankiet.getId()).get();
        model.addAttribute("productImgOlder", currentProduct.getImage());
        List<FieldError> errors = newProductBindingResult.getFieldErrors();
        for (FieldError error : errors) {
            System.out.println(error.getField() + " - " + error.getDefaultMessage());
        }
        // validate
        if (newProductBindingResult.hasErrors()) {
            return "admin/product/update";
        }
        if (file != null) {
            String productImgFile = this.uploadFileService.handleSaveUploadFile(file, "product");
            currentProduct.setImage(productImgFile);
        }
        if (currentProduct != null) {
            currentProduct.setName(tuankiet.getName());
            currentProduct.setPrice(tuankiet.getPrice());
            currentProduct.setDetailDesc(tuankiet.getDetailDesc());
            currentProduct.setShortDesc(tuankiet.getShortDesc());
            currentProduct.setQuantity(tuankiet.getQuantity());
            currentProduct.setFactory(tuankiet.getFactory());
            currentProduct.setTarget(tuankiet.getTarget());

            this.productService.handleSaveProduct(currentProduct);
        }
        return "redirect:/admin/product";
    }

    @GetMapping("/admin/product/delete/{id}")
    public String getDeletePage(Model model, @PathVariable long id) {
        model.addAttribute("id", id);
        model.addAttribute("newProduct", new Product());
        return "admin/product/delete";
    }

    @PostMapping("/admin/product/delete")
    public String postDeleteUser(Model model, @ModelAttribute("newProduct") User tuankiet) {
        System.out.println(tuankiet.getId());
        this.productService.deleteAProduct(tuankiet.getId());
        return "redirect:/admin/product";
    }

    @RequestMapping("/admin/product/{id}")
    public String getUserDetailPage(Model model, @PathVariable long id) {
        Product product = this.productService.getProductById(id).get();
        model.addAttribute("product", product);
        return "admin/product/detail";
    }

}
