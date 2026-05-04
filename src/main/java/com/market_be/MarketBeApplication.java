package com.market_be;

import com.market_be.content.Role;
import com.market_be.entity.AppUser;
import com.market_be.entity.Posts;
import com.market_be.repository.AppUserRepository;
import com.market_be.repository.PostsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;


//
@SpringBootApplication
@Slf4j
@RequiredArgsConstructor
public class MarketBeApplication implements CommandLineRunner {

    private final PostsRepository postsRepository;
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;


    public static void main(String[] args) {
        SpringApplication.run(MarketBeApplication.class, args);
    }


    public void run(String... args) throws Exception {
        if (!appUserRepository.existsByLoginId("admin")) {
            appUserRepository.save(AppUser.builder()
                    .loginId("admin")
                    .password(passwordEncoder.encode("1234"))
                    .nickname("관리자")
                    .userName("관리자")
                    .phoneNum("01000000000")
                    .birth("19000101")
                    .email("admin@market.com")
                    .addr("관리국")
                    .role(Role.ADMIN)
                    .build());
            log.info("Admin account (admin/1234) created successfully.");
        }
    }

}
