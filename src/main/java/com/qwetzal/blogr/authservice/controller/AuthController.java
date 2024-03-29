package com.qwetzal.blogr.authservice.controller;

import com.qwetzal.blogr.authservice.UserSecurity;
import com.qwetzal.blogr.authservice.config.JwtUtils;
import com.qwetzal.blogr.authservice.dao.JpaUserDetailsService;
import com.qwetzal.blogr.authservice.request.AuthenticationRequest;
import com.qwetzal.blogr.authservice.requests.UserRequest;
import com.qwetzal.blogr.authservice.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;

    private final JpaUserDetailsService jpaUserDetailsService;

    private final AuthService authService;

    private final JwtUtils jwtUtils;

    @PostMapping("/authenticate")
    public ResponseEntity<String> authenticate(@RequestBody AuthenticationRequest request, HttpServletResponse response){
        try {
            authenticationManager
                    .authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    request.getEmail(),
                                    request.getPassword(),
                                    new ArrayList<>()
                            )
                    );

            final UserDetails user = jpaUserDetailsService.loadUserByUsername(request.getEmail());
            if(user != null){
                String jwt = jwtUtils.generateToken(user);
                Cookie cookie = new Cookie("jwt", jwt);
                cookie.setMaxAge(7 * 24 * 60 * 60);
                cookie.setHttpOnly(true);
                cookie.setPath("/");
                response.addCookie(cookie);
                return ResponseEntity.ok(jwt);
            }
            return ResponseEntity.status(400).body("Error authenticating");
        }catch(Exception e){
            System.out.println(e);
            return ResponseEntity.status(400).body("" + e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<UserSecurity> register(@RequestBody UserRequest user) throws Exception{
        return ResponseEntity.ok(authService.AddUser(user).map(UserSecurity::new)
                .orElseThrow(() -> new Exception("Unknown")));
    }
}
