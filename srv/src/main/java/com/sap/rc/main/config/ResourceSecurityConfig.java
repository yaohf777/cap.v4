package com.sap.rc.main.config;

import static org.springframework.http.HttpMethod.*;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter;

import com.sap.rc.main.multitenancy.TenantFilter;
import com.sap.xs2.security.commons.SAPOfflineTokenServicesCloud;

@Configuration
@EnableWebSecurity
@EnableResourceServer
@PropertySources({@PropertySource("classpath:conf/conf.common.properties"),
		@PropertySource("classpath:conf/conf.properties")})
public class ResourceSecurityConfig extends ResourceServerConfigurerAdapter {

	private static final String DISPLAY_SCOPE_LOCAL = "Display";
	private static final String UPDATE_SCOPE_LOCAL = "Update";
	public static final String REGEX_TENANT_INDEX = "(!t\\d+)?.";
	private static final String XSAPPNAME = "readinesscheck";
	public static final String DISPLAY_SCOPE = XSAPPNAME + "." + DISPLAY_SCOPE_LOCAL;
	public static final String UPDATE_SCOPE = XSAPPNAME + "." + UPDATE_SCOPE_LOCAL;

	@Value("#{'${resource.scope.public}'.split(',')}")
	private List<String> publicResources;

	@Value("#{'${resource.scope.display}'.split(',')}")
	private List<String> scopeDisplayResources;

	@Value("#{'${resource.scope.update}'.split(',')}")
	private List<String> scopeUpdateResources;

	// configure Spring Security, demand authentication and specific scopes
	@Override
	public void configure(HttpSecurity http) throws Exception {

		// http://docs.spring.io/spring-security/oauth/apidocs/org/springframework/security/oauth2/provider/expression/OAuth2SecurityExpressionMethods.html
		String hasScopeUpdate = "#oauth2.hasScopeMatching('" + XSAPPNAME + REGEX_TENANT_INDEX + UPDATE_SCOPE_LOCAL
				+ "')";
		String hasScopeDisplay = "#oauth2.hasScopeMatching('" + XSAPPNAME + REGEX_TENANT_INDEX + DISPLAY_SCOPE_LOCAL
				+ "')";

		// session is created by approuter
		http.addFilterAfter(new TenantFilter(), SecurityContextHolderAwareRequestFilter.class).sessionManagement()
				.sessionCreationPolicy(SessionCreationPolicy.NEVER);

		// enable OAuth2 checks
		// make public resource accessible by "anybody"
		// http.authorizeRequests().antMatchers(GET, "/**").permitAll();
		http.authorizeRequests().antMatchers(GET, "/index.html", "/").permitAll();
		http.authorizeRequests().antMatchers(GET, "/health", "/").permitAll(); // used as health check on CF: must be
		for (String publicResource : publicResources) {
			if (publicResource == null || publicResource.isEmpty()) {
				continue;
			}
			http.authorizeRequests().antMatchers(GET, publicResource).permitAll();
		}

		// demand display scope for specified resources
		for (String scopeDisplayResource : scopeDisplayResources) {
			if (scopeDisplayResource == null || scopeDisplayResource.isEmpty()) {
				continue;
			}
			http.authorizeRequests().antMatchers(GET, scopeDisplayResource).access(hasScopeDisplay);
		}

		// demand update scope for specified resources
		for (String scopeUpdateResource : scopeUpdateResources) {
			if (scopeUpdateResource == null || scopeUpdateResource.isEmpty()) {
				continue;
			}
			http.authorizeRequests().antMatchers(POST, scopeUpdateResource).access(hasScopeUpdate);
			http.authorizeRequests().antMatchers(PUT, scopeUpdateResource).access(hasScopeUpdate);
			http.authorizeRequests().antMatchers(DELETE, scopeUpdateResource).access(hasScopeUpdate);
		}

		// for testing
		http.authorizeRequests().antMatchers(POST, "/api/v1/ads/**").access(hasScopeUpdate);
		http.authorizeRequests().antMatchers(GET, "/api/v1/ads/**").access(hasScopeDisplay);

		// deny anything not configured above
		// http.authorizeRequests().anyRequest().denyAll();

		/*        // @formatter:off
        http.addFilterAfter(new TenantFilter(), SecurityContextHolderAwareRequestFilter.class)
            .sessionManagement()
                // session is created by approuter
                .sessionCreationPolicy(SessionCreationPolicy.NEVER)
                .and()
            // demand specific scopes depending on intended request
            .authorizeRequests()
                // enable OAuth2 checks
                .antMatchers(GET, "/index.html", "/").permitAll() //index.html: must be accessible by "anybody"
                .antMatchers(GET, "/health", "/").permitAll() //used as health check on CF: must be accessible by "anybody"
                .antMatchers(POST, "/api/**").access(hasScopeUpdate)
                .antMatchers(PUT, "/api/**").access(hasScopeUpdate)
                .antMatchers(DELETE, "/api/**").access(hasScopeUpdate)
                .antMatchers(GET, "/api/**").access(hasScopeDisplay)
                .anyRequest().denyAll(); // deny anything not configured above
          // @formatter:on
       */
	}

	// configure offline verification which checks if any provided JWT was properly signed
	@Bean
	protected SAPOfflineTokenServicesCloud offlineTokenServices() {
		return new SAPOfflineTokenServicesCloud();
	}
}