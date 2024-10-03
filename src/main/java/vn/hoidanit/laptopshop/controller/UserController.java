package vn.hoidanit.laptopshop.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import vn.hoidanit.laptopshop.domain.User;
import vn.hoidanit.laptopshop.repository.UserRepository;
import vn.hoidanit.laptopshop.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    public UserController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
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
    public String getUserPage(Model model) {
        List<User> users = this.userService.getAllUsers();
        model.addAttribute("users1", users);
        return "/admin/user/show";
    }

    @RequestMapping("/admin/user/{id}")
    public String getUserDetailPage(Model model, @PathVariable long id) {
        User user = this.userService.getUserById(id);
        model.addAttribute("user", user);
        return "/admin/user/detail";
    }

    @RequestMapping("/admin/user/create")
    public String getCreatePage(Model model) {
        model.addAttribute("newUser", new User());
        return "/admin/user/create";
    }

    @RequestMapping(value = "/admin/user/create", method = RequestMethod.POST)
    public String createUserPage(Model model, @ModelAttribute("newUser") User tuankiet) {
        System.out.println("run here" + tuankiet);
        this.userService.handleSaveUser(tuankiet);
        return "redirect:/admin/user";
    }

    @RequestMapping("/admin/user/update/{id}")
    public String getUpdatePage(Model model, @PathVariable long id) {
        User currentUser = this.userService.getUserById(id);
        model.addAttribute("newUser", currentUser);
        return "/admin/user/update";
    }

    @PostMapping("/admin/user/update")
    public String postUpdateUser(Model model, @ModelAttribute("newUser") User tuankiet) {
        User currentUser = this.userService.getUserById(tuankiet.getId());
        model.addAttribute("newUser", currentUser);
        if (currentUser != null) {
            currentUser.setFullName(tuankiet.getFullName());
            currentUser.setAddress(tuankiet.getAddress());
            currentUser.setPhone(tuankiet.getPhone());
            this.userService.handleSaveUser(currentUser);
        }
        return "redirect:/admin/user";
    }

    @GetMapping("/admin/user/delete/{id}")
    public String getDeletePage(Model model, @PathVariable long id) {
        model.addAttribute("id", id);
        model.addAttribute("newUser", new User());
        return "/admin/user/delete";
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
