package org.sid.cinema.security;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	@Autowired
	public DataSource dataSource;

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		PasswordEncoder passwordEncoder=passwordEncoder();
		
		System.out.println(passwordEncoder.encode("1234"));
		
		auth.jdbcAuthentication()
			.dataSource(dataSource)
			.usersByUsernameQuery("select username as principal, password as credentials, active from users where username=?")
			.authoritiesByUsernameQuery("select username as principal, role as role from users_roles where username=?")
			.passwordEncoder(passwordEncoder)
			.rolePrefix("ROLE_");
		/*
		 * auth.inMemoryAuthentication().withUser("user1").password(passwordEncoder.
		 * encode("1234")).roles("USER");
		 * auth.inMemoryAuthentication().withUser("user2").password(passwordEncoder.
		 * encode("1234")).roles("USER");
		 * auth.inMemoryAuthentication().withUser("admin").password(passwordEncoder.
		 * encode("1234")).roles("USER","ADMIN");
		 */
		
	}
	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers("/payerTickets/**");
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		
		//http.authorizeRequests().antMatchers("/payerTickets").permitAll();
		http.formLogin().loginPage("/login");
		
		http.authorizeRequests().antMatchers("/imageFilm**/**","/films**/**","/salles**/**","/places**/**","/tickets**/**","/seances**/**","/cinemas**/**","/villes**/**","/projections**/**").permitAll();
		
		http.authorizeRequests().antMatchers("/add**/**","/delete**/**").hasAnyRole("ADMIN");
		http.authorizeRequests().antMatchers("/film**/**","/salle**/**","/place**/**","/ticket**/**","/seance**/**","/cinema**/**","/ville**/**","/projection**/**").hasAnyRole("USER");
		http.authorizeRequests().antMatchers("/connect").permitAll();
		
		//http.authorizeRequests().anyRequest().authenticated();
		http.csrf();
		
	}
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
