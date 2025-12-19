package com.leseviteurs.json_rpc.service;


import com.leseviteurs.json_rpc.model.UserAccount;
import com.leseviteurs.json_rpc.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UserLoader implements CommandLineRunner {
    private final UserRepository userRepo;
    public UserLoader(UserRepository userRepo) { this.userRepo = userRepo; }

    @Override
    public void run(String... args) throws Exception {
        if (userRepo.findByUsername("admin").isEmpty()) {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            UserAccount admin = new UserAccount("admin", encoder.encode("adminpass"), "ADMIN");
            userRepo.save(admin);
            System.out.println("Admin user created: admin / adminpass");
        }
    }
}
