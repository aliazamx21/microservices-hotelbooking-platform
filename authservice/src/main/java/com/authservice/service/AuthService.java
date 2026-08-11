package com.authservice.service;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.authservice.dto.APIResponse;
import com.authservice.dto.UserDto;
import com.authservice.entity.User;
import com.authservice.repository.UserRepository;

@Service
public class AuthService {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	public APIResponse<String> register(UserDto userDto){
		
		//APIResponse object
		APIResponse<String> response = new APIResponse<>();
		
		//check whether username exists
		if(userRepository.existsByUsername(userDto.getUsername())) {
			response.setMessage("Registration Failed");
			response.setStatus(500);
			response.setData("user with this username exists");
			return response;
		}
		//check whether Email exists
		if(userRepository.existsByEmail(userDto.getEmail())) {
			response.setMessage("Registration Failed");
			response.setStatus(600);
			response.setData("Registration with this Email id Already Exists");
			return response;
		}
		//encode the passwords before saving that to the database
		String encryptedPassword = passwordEncoder.encode(userDto.getPassword());
		
		User user = new User();
		BeanUtils.copyProperties(userDto, user);
		user.setPassword(encryptedPassword);
		user.setRole("ROLE_ADMIN");
		
		User savedUser = userRepository.save(user);
		
		if(savedUser==null) {
			//custom
		}
		response.setMessage("Registration Completed");
	    response.setStatus(201);
	    response.setData("User has been registerd");
	    return response;
		
		
		//finally save the user and return response as APIResponse
		
	}

}
