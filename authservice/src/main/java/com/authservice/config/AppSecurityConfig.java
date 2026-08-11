package com.authservice.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.authservice.service.CustomUserDetailService;
import com.authservice.service.JwtFilter;


@Configuration
@EnableWebSecurity
public class AppSecurityConfig {
	
	@Autowired
	private CustomUserDetailService customUserDetailService;
	
	@Autowired
	private JwtFilter jwtFilter;
	
	private String[] openUrl = {
			"/api/v1/auth/register", 
			"/api/v1/auth/login",
			"/api/v1/auth/get-user",
    		"/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/swagger-resources/**",
            "/webjars/**"
	};
	
	 @Bean
		public AuthenticationManager authManager(AuthenticationConfiguration config) throws Exception {
			return config.getAuthenticationManager();
		}

	@Bean
	public PasswordEncoder getEncodedPassword() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	public AuthenticationProvider authProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
		authProvider.setUserDetailsService(customUserDetailService);
		authProvider.setPasswordEncoder(getEncodedPassword());
		return authProvider;
	}
	
	@Bean
	public SecurityFilterChain securityConfig(HttpSecurity http) throws Exception{
		http
		.csrf(csrf -> csrf.disable())//Disable CSRF
		.cors(cors -> cors.disable())
		.authorizeHttpRequests(
				 req->{
					 req.requestMatchers(openUrl).permitAll()
					 .requestMatchers("/api/v1/admin/welcome").hasAnyRole("ADMIN","USER")
					 .anyRequest().authenticated();
					 }).authenticationProvider(authProvider())
        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
		
		return http.build();
		
	}
}
