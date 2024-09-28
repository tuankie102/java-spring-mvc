package vn.hoidanit.laptopshop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vn.hoidanit.laptopshop.service.UserService;

@Controller
public class UserController {

    UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // MVC: view gửi rq tới controller xong ctrl yêu cầu lấy data từ model xong
    // model trả về ctrl xong ctrl phản hổi view
    @RequestMapping("/")
    public String getHomePage(Model model) {
        String test = this.userService.handleHello();
        model.addAttribute("eric", test);
        model.addAttribute("tuankiet", "from with controller model");
        return "hello";
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
