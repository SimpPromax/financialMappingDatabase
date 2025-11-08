package com.JavaWebToken.jwtAuthentication.service;

import com.JavaWebToken.jwtAuthentication.entity.User;
import com.JavaWebToken.jwtAuthentication.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {


    //dependency injection using constructor
    //dependencies to be used by the customuserdetailsservice
    private final UserRepository userRepository;

    //the constructor
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override// this is used to override a method in the parent class
    // so in this case the userdetailsservice has this userdetails class, and i am overriding it to have this custom user logic
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)//uses my userrepository to check if username exists
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        return user; // Our User entity already implements UserDetails
        // it must return an object in which the spring security can use to check username, password ,roles
    }
}