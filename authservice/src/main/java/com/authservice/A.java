package com.authservice;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class A {
	public static void main(String[] args) {
    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    System.out.println(passwordEncoder.encode("mike@12334aliaz"));

	}

}
