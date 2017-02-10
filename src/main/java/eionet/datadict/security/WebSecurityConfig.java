package eionet.datadict.security;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {



@Autowired
  private EntryPointUnauthorizedHandler unauthorizedHandler;


 @Bean
  @Override
  public AuthenticationManager authenticationManagerBean() throws Exception {
    return super.authenticationManagerBean();
  }

  @Bean
  public AuthenticationReqFilter authenticationRequestFilterBean( ) throws Exception {
    AuthenticationReqFilter authenticationReqFilter = new AuthenticationReqFilter();
    authenticationReqFilter.setAuthenticationManager(authenticationManagerBean());
    return authenticationReqFilter;
  }



  @Override
  protected void configure(HttpSecurity httpSecurity) throws Exception {
    httpSecurity
      .csrf()
        .disable()
      .exceptionHandling()
        .authenticationEntryPoint(this.unauthorizedHandler)
        .and()
      .sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.NEVER)
        .and()
                  .authorizeRequests()
        .antMatchers("/*").permitAll();

    // Custom JWT based authentication
    httpSecurity
      .addFilter((authenticationRequestFilterBean()));
  }

}