package com.ecom.Shopping.Cart.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
public class SecurityConfig {

    @Autowired
    private AuthenticationSuccessHandler  authenticationSuccessHandler;

    @Autowired
    @Lazy
    private AuthFailureHandlerImpl  authFailureHandler;

    @Autowired
    private UserDetailsService userDetailsService;
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }


      @Bean
    public DaoAuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
          authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
         http.csrf(csrf -> csrf.disable()
                )
                .cors(cors-> cors.disable())
                .authenticationProvider(authenticationProvider())
                .authorizeHttpRequests
                        (req->req
                                        .requestMatchers("/signin", "/login","/register","/products","/", "/search","/saveUser",
                                                "/css/**",
                                                "/js/**",
                                                "/img/**",
                                                "/forgot-password","/product/**").permitAll()
                                        .requestMatchers("/user/addToCart").permitAll()

                                .requestMatchers("/user/**").hasRole("USER")
                .requestMatchers("/admin/**").hasRole("ADMIN")
                                        .anyRequest().authenticated()


                        )
                .formLogin(form->form
                        .loginPage("/signin")
                        .loginProcessingUrl("/login")
                        .failureHandler(authFailureHandler)
                        .successHandler(authenticationSuccessHandler)
                        .permitAll()   )

                                .logout(logout->logout
                                            .permitAll()
                                );



        return http.build();
    }


}
