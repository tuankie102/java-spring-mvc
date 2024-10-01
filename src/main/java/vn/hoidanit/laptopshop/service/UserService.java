package vn.hoidanit.laptopshop.service;

import java.util.List;

import org.springframework.stereotype.Service;

import vn.hoidanit.laptopshop.domain.User;
import vn.hoidanit.laptopshop.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String handleHello() {
        return "Hello from Service";
    }

    public User handleSaveUser(User user) {
        User testUser = this.userRepository.save(user);
        System.out.println(testUser);
        return testUser;
    }

    public List<User> getAllUsers() {
        List<User> arrUsers;
        arrUsers = userRepository.findAll();
        return arrUsers;
    }

    public List<User> getAllUsersByEmail(String email) {
        List<User> arrUsers;
        arrUsers = userRepository.findAllByEmail(email);
        return arrUsers;
    }
}
