package com.ziggy.king.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private CurrentUserDetailsService userDetailsService;

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(authenticationProvider());
	}

	@Bean
	public DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
		authProvider.setUserDetailsService(userDetailsService);
		authProvider.setPasswordEncoder(new BCryptPasswordEncoder());
		return authProvider;
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		// @formatter:off
		http.httpBasic().and().authorizeRequests()
    .antMatchers(
    		"/index.html",
    		"/home.html",
    		"/login.html",
    		"/app.html",
    		"/",
    		"/home",
    		"/login",
    		"/logout")
    .permitAll().anyRequest().authenticated()
    .and().csrf().disable().logout();
		// @formatter:on
	}

}
