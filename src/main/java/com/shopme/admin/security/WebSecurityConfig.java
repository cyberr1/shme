package com.shopme.admin.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.session.HttpSessionEventPublisher;



@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	 
    @Autowired
    private CustomLoginFailureHandler loginFailureHandler;
     
    @Autowired
    private CustomLoginSuccessHandler loginSuccessHandler;
    
	@Bean
	public UserDetailsService userDetailsService() {
		return new ShopmeUserDetailsService();
	}
	
	public DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
		authProvider.setUserDetailsService(userDetailsService());
		authProvider.setPasswordEncoder(passwordEncoder());
		return authProvider;
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(authenticationProvider());
		super.configure(auth);
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.sessionManagement().maximumSessions(2)
		.and().sessionFixation().migrateSession()
		
		.and().authorizeRequests()
		.antMatchers("/users/**").hasAuthority("Admin")
		
		.antMatchers("/savenewpass").permitAll()
		.antMatchers("/categories/**").hasAnyAuthority("Admin", "Editor")
		.antMatchers("/brands/**").hasAnyAuthority("Admin","Editor")
		.antMatchers("/products/**").hasAnyAuthority("Admin","Salesperson","Edito","Shipper")
		.antMatchers("/questions/**").hasAnyAuthority("Admin","Assistant")
		.antMatchers("/reviews/**").hasAnyAuthority("Admin","Assistant")
		.antMatchers("/customers/**").hasAnyAuthority("Admin","Salesperson")
		.antMatchers("/shipping/**").hasAnyAuthority("Admin","Salesperson")
		.antMatchers("/orders/**").hasAnyAuthority("Admin","Salesperson", "Shipper")
		.antMatchers("/reports/**").hasAnyAuthority("Admin","Salesperson")
		.antMatchers("/articles/**").hasAnyAuthority("Admin","Editor")
		.antMatchers("/menus/**").hasAnyAuthority("Admin","Editor")
		.antMatchers("/settings/**").hasAnyAuthority("Admin")
		.antMatchers("/forgot_password").permitAll()
		.antMatchers("/forgotpassword").permitAll()
		.antMatchers("/reset").permitAll()
		.anyRequest().authenticated()
		.and().formLogin()
			.loginPage("/login")
			.usernameParameter("email")
		//	.passwordParameter("password")
			.permitAll()
			.failureHandler(loginFailureHandler)
            .successHandler(loginSuccessHandler)
			
			.and().logout().permitAll()           
            .and()
            .rememberMe().key("uniqueAndSecret")
            			 .tokenValiditySeconds(7* 24 *60 *60)
            			 
//            .rememberMeCookieDomain("localhost")//for sending domain with cookie should be ommited on local
            .rememberMeCookieName("remember-me")//for sending cookie name with cookies

            
            .and()
            .csrf().disable()//never should be done in real projects 
            .headers()
            .frameOptions().sameOrigin()
			.httpStrictTransportSecurity().disable()
			.xssProtection().block(true)
			;
	}

//	.contentSecurityPolicy("script-src 'self'")
//    .and()

	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers("/images/**", "/js/**", "/webjars/**");
	}

	@Bean
	public HttpSessionEventPublisher httpSessionEventPublisher() {
	    return new HttpSessionEventPublisher();
	}

	
}
