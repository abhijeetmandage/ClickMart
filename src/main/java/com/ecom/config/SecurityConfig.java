package com.ecom.config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
public class SecurityConfig
{

    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    
    @Autowired
    private AuthenticationSuccessHandler authenticationSuccessHandler;
    
    @Autowired
    @Lazy
    private AuthFailureHandlerImpl authFailureHandlerImpl;

    @Bean
    public PasswordEncoder passwordEncoder()
    {
    		return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() 
    {
        DaoAuthenticationProvider auth =new DaoAuthenticationProvider();
        auth.setUserDetailsService(userDetailsService);
        auth.setPasswordEncoder(passwordEncoder());
        return auth;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception 
    {
	    	http.csrf(csrf->csrf.disable()).cors(cors->cors.disable())
			.authorizeHttpRequests(req->req.requestMatchers("/user/**").hasRole("USER")
			.requestMatchers("/admin/**").hasRole("ADMIN")		
			.requestMatchers	("/**").permitAll())
			.formLogin(form->form.loginPage("/signin")
					.loginProcessingUrl("/login")
					.failureHandler(authFailureHandlerImpl)
					.successHandler(authenticationSuccessHandler))
			.logout(logout->logout.permitAll());
			return http.build();
    }
}