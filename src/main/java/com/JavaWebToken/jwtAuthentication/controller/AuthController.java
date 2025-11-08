package com.JavaWebToken.jwtAuthentication.controller;



import com.JavaWebToken.jwtAuthentication.config.JwtTokenUtil;
import com.JavaWebToken.jwtAuthentication.entity.User;
import com.JavaWebToken.jwtAuthentication.repository.UserRepository;
import com.JavaWebToken.jwtAuthentication.service.CustomUserDetailsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    //dependency injection via constructor
    //they are declared here to be used in the constructor
    private final UserRepository userRepository;//used to talk to database
    private final PasswordEncoder passwordEncoder;// to hash passwords
    private final AuthenticationManager authenticationManager;// to verify credentials
    private final JwtTokenUtil jwtTokenUtil;// to create java web tokens
    private final CustomUserDetailsService userDetailsService;// to load user details

    //constructor
    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          AuthenticationManager authenticationManager,
                          JwtTokenUtil jwtTokenUtil,
                          CustomUserDetailsService userDetailsService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody RegistrationRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            return ResponseEntity.badRequest().body("Username already exists");
        }

        // in here we are saving the values in the User entity
        User newUser = new User();//initialise the User
        newUser.setUsername(request.username());//sets username
        newUser.setPassword(passwordEncoder.encode(request.password()));//encodes password and sets it to the entity
        newUser.setRole("USER");// sets the user

        userRepository.save(newUser);// userrepository saves the newuser , inserts the values to the user table in the db
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(//authentication manager is a springboot security gateway for authentication
                    new UsernamePasswordAuthenticationToken(// it takes the json request parameters from the request , retrieves the uer and its password and if they match user ia authenticated
                            request.username(),
                            request.password()
                    )
            );

            final UserDetails userDetails = userDetailsService.loadUserByUsername(request.username());
            final String token = jwtTokenUtil.generateToken(userDetails);

            return ResponseEntity.ok(new LoginResponse(token));
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid username or password");
        }
    }

    // this hold the data from the api
    public record RegistrationRequest(String username, String password) {}
    public record LoginRequest(String username, String password) {}
    public record LoginResponse(String token) {}
}
