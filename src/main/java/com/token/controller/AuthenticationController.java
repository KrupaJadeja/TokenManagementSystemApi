package com.token.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.token.exception.AppException;
import com.token.payload.ForgotPasswordRequest;
import com.token.payload.JwtAuthenticationResponse;
import com.token.payload.LoginRequest;
import com.token.payload.ResetPasswordRequest;
import com.token.payload.SignupRequest;
import com.token.security.JwtTokenProvider;
import com.token.service.UserService;

@RestController
@CrossOrigin("*")
public class AuthenticationController {
	
	@Autowired
    AuthenticationManager authenticationManager;

	@Autowired
    JwtTokenProvider tokenProvider;
	
	@Autowired
	UserService userService;
	
	@PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
		
		UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken
				(loginRequest.getUsernameOrEmail(),loginRequest.getPassword());
		
		Authentication authentication = authenticationManager.authenticate(authRequest);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        return ResponseEntity.ok(new JwtAuthenticationResponse(jwt));
    }
	
	@PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
		try {
			if(userService.existsByUsername(signUpRequest.getUserName())) {
	            return new ResponseEntity<String>("Username is already taken!",HttpStatus.BAD_REQUEST);
	        }

	        if(userService.existsByEmail(signUpRequest.getEmail())) {
	            return new ResponseEntity<String>("Email Address already in use!",HttpStatus.BAD_REQUEST);
	        }
	        userService.registerUser(signUpRequest);
		}catch (Exception e) {
			// TODO: handle exception
			//1.. write in log file
			
			//2.. response
			return new ResponseEntity<AppException>(new AppException(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
        return new ResponseEntity<String>("User registered successfully", HttpStatus.OK);
    }
	
	@PostMapping("/forgotpassword")
	public ResponseEntity<?> processForgotPassword(@Valid @RequestBody ForgotPasswordRequest useremail) {
		try {
			userService.processForgotPassword(useremail);
			return new ResponseEntity<String>("Please check link for forgot password "+useremail.getemail(), HttpStatus.OK);
		}catch (Exception e) {
			// TODO: handle exception
			//1.. write in log file
			
			//2.. response
			return new ResponseEntity<AppException>(new AppException(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PostMapping(value = "/reset")
	public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest resetPasswordRequest) {
		try {
			userService.resetPassword(resetPasswordRequest);
			return new ResponseEntity<String>("Password reset successfully", HttpStatus.OK);
		}catch (Exception e) {
			return new ResponseEntity<AppException>(new AppException(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}		
	}

}
