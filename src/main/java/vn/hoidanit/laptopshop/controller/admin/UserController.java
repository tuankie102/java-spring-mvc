package vn.hoidanit.laptopshop.controller.admin;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import vn.hoidanit.laptopshop.domain.Product;
import vn.hoidanit.laptopshop.domain.User;
import vn.hoidanit.laptopshop.service.UploadFileService;
import vn.hoidanit.laptopshop.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UserController {

    private final UserService userService;
    private final UploadFileService uploadFileService;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService, PasswordEncoder passwordEncoder,
            UploadFileService uploadFileService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.uploadFileService = uploadFileService;
    }

    // MVC: view gửi rq tới controller xong ctrl yêu cầu lấy data từ model xong
    // model trả về ctrl xong ctrl phản hổi view
    @RequestMapping("/")
    public String getHomePage(Model model) {
        model.addAttribute("tuankiet", "from with controller model");
        List<User> arrUsers;
        // arrUsers = userService.getAllUsers();
        arrUsers = this.userService.getAllUsersByEmail("testid3@gmail.com");
        System.out.println(arrUsers);
        return "hello";
    }

    @RequestMapping("/admin/user")
    public String getUserPage(Model model, @RequestParam("page") Optional<String> pageOptional) {
        int page = 1;
        try {
            if (pageOptional.isPresent()) {
                page = Integer.parseInt(pageOptional.get());
            } else {
                // page =1
            }
        } catch (Exception e) {
            // page =1
            // TODO: handle exception
        }
        Pageable pageable = PageRequest.of(page - 1, 2);
        Page<User> usersPage = this.userService.getAllUsers(pageable);
        List<User> users = usersPage.getContent();
        int totalPages = usersPage.getTotalPages();
        model.addAttribute("users1", users);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        return "admin/user/show";
    }

    @RequestMapping("/admin/user/{id}")
    public String getUserDetailPage(Model model, @PathVariable long id) {
        User user = this.userService.getUserById(id);
        model.addAttribute("user", user);
        return "admin/user/detail";
    }

    @RequestMapping("/admin/user/create")
    public String getCreatePage(Model model) {
        model.addAttribute("newUser", new User());
        return "admin/user/create";
    }

    @PostMapping(value = "/admin/user/create")
    public String createUserPage(Model model, @ModelAttribute("newUser") @Valid User tuankiet,
            BindingResult newUserBindingResult,
            @RequestParam("avatarFile") MultipartFile file) {

        List<FieldError> errors = newUserBindingResult.getFieldErrors();
        for (FieldError error : errors) {
            System.out.println(error.getField() + " - " + error.getDefaultMessage());
        }
        // validate
        if (newUserBindingResult.hasErrors()) {
            return "admin/user/create";
        }

        String avatar = this.uploadFileService.handleSaveUploadFile(file, "avatar");
        String hashPassword = this.passwordEncoder.encode(tuankiet.getPassword());
        tuankiet.setAvatar(avatar);
        tuankiet.setPassword(hashPassword);
        tuankiet.setRole(this.userService.getRoleByName(tuankiet.getRole().getName()));

        this.userService.handleSaveUser(tuankiet);
        return "redirect:/admin/user";
    }

    @RequestMapping("/admin/user/update/{id}")
    public String getUpdatePage(Model model, @PathVariable long id) {
        User currentUser = this.userService.getUserById(id);
        model.addAttribute("newUser", currentUser);
        model.addAttribute("emailUser", currentUser.getEmail());
        model.addAttribute("avatarFile", currentUser.getAvatar());
        return "admin/user/update";
    }

    @PostMapping("/admin/user/update")
    public String postUpdateUser(Model model, @ModelAttribute("newUser") @Valid User tuankiet,
            BindingResult newUserBindingResult,
            @RequestParam("avatarFile") MultipartFile file) {
        User currentUser = this.userService.getUserById(tuankiet.getId());
        model.addAttribute("emailUser", currentUser.getEmail());
        model.addAttribute("avatarFile", currentUser.getAvatar());
        if (newUserBindingResult.hasErrors()) {
            return "admin/user/update";
        }
        if (currentUser != null) {
            if (file != null) {
                String avatar = this.uploadFileService.handleSaveUploadFile(file, "avatar");
                currentUser.setAvatar(avatar);
            }
            currentUser.setFullName(tuankiet.getFullName());
            currentUser.setAddress(tuankiet.getAddress());
            currentUser.setPhone(tuankiet.getPhone());
            currentUser.setRole(this.userService.getRoleByName(tuankiet.getRole().getName()));
            this.userService.handleSaveUser(currentUser);
        }
        return "redirect:/admin/user";
    }

    @GetMapping("/admin/user/delete/{id}")
    public String getDeletePage(Model model, @PathVariable long id) {
        model.addAttribute("id", id);
        model.addAttribute("newUser", new User());
        return "admin/user/delete";
    }

    @PostMapping("/admin/user/delete")
    public String postDeleteUser(Model model, @ModelAttribute("newUser") User tuankiet) {
        System.out.println(tuankiet.getId());
        this.userService.deleteAUser(tuankiet.getId());
        return "redirect:/admin/user";
    }

}

// @RestController
// public class UserController {
// UserService userService;

// public UserController(UserService userService) {
// this.userService = userService;
// }

// @RequestMapping("/")
// public String getHomePage() {
// return this.userService.handleHello();
// }
// }
