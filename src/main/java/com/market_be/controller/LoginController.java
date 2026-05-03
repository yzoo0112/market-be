package com.market_be.controller;

import com.market_be.content.Role;
import com.market_be.dto.AccountCredentials;
import com.market_be.dto.AppUserDto;
import com.market_be.dto.SignupRequest;
import com.market_be.entity.AppUser;
import com.market_be.entity.Visit;
import com.market_be.repository.AppUserRepository;
import com.market_be.repository.VisitRepository;
import com.market_be.service.JwtService;
import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class LoginController {

    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final VisitRepository visitRepository;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
        if (appUserRepository.existsByLoginId(request.getLoginId())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 존재하는 ID입니다");
        }

        AppUser user = AppUser.builder()
                .loginId(request.getLoginId())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .userName(request.getUserName())
                .phoneNum(request.getPhoneNum())
                .birth(request.getBirth())
                .email(request.getEmail())
                .addr(request.getAddr())
                .role(Role.USER) // USER에서 ROLE_USER으로 수정함
                // .lastVisitDate(new Date())
                .build();

        appUserRepository.save(user);
        return ResponseEntity.ok("회원가입 성공");
    }

    @PostMapping("/login")
    @Transactional
    public ResponseEntity<?> login(@RequestBody AccountCredentials credentials) {

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                credentials.getLoginId(), credentials.getPassword());
        Authentication authentication = authenticationManager.authenticate(token);

        AppUser appUser = appUserRepository.findByLoginId(credentials.getLoginId())
                .orElseThrow(EntityExistsException::new);
        // 1. 로그인하는 유저의 마지막 접속 일자 조회
        LocalDate lastVisitDate = appUser.getLastVisitDate();
        // 2. 마지막 접속 일자가 오늘이 아닌 경우
        if (lastVisitDate == null || !lastVisitDate.equals(LocalDate.now())) {
            // 3. 마지막 접속 일자 오늘로 변경
            appUser.setLastVisitDate(LocalDate.now());
            // 4. visit 테이블 조회
            Visit visit = visitRepository.findByVisitDate(LocalDate.now());
            // 5. 오늘자 방문 레코드 자체가 없는 경우 카운트 1을 갖는 레코드 생성
            if (visit == null) {
                Visit newVisit = Visit.builder()
                        .visitDate(LocalDate.now())
                        .visits(1)
                        .build();
                visitRepository.save(newVisit);
            } else { // 6. 오늘자 방문 레코드가 이미 존재하는 경우 기존 카운트 + 1
                visit.setVisits(visit.getVisits() + 1);
            }
        }

        List<String> roles = List.of("ROLE_" + appUser.getRole().name()); // roles 변수 선언 및 초기화

        String jwtToken = jwtService.generateToken(credentials.getLoginId(), roles);

        Map<String, Object> result = new HashMap<>();
        result.put("userId", appUser.getId());
        result.put("nickname", appUser.getNickname());
        result.put("role", appUser.getRole());
        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                .body(result);

    }

    @GetMapping("/signup/echeck")
    public ResponseEntity<?> checkDuplicateEmail(@RequestParam String email) {
        boolean exists = appUserRepository.existsByEmail(email);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/signup/ncheck")
    public ResponseEntity<?> checkDuplicateNickname(@RequestParam String nickname) {
        boolean exists = appUserRepository.existsByNickname(nickname);
        return ResponseEntity.ok(exists);
    }
 
    @GetMapping("/signup/pcheck")
    public ResponseEntity<?> checkDuplicatePhone(@RequestParam String phoneNum) {
        boolean exists = appUserRepository.existsByPhoneNum(phoneNum);
        return ResponseEntity.ok(exists);
    }
 
}

