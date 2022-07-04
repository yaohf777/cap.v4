package com.sap.rc.main.config;

import java.util.Arrays;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.sap.cloud.sdk.service.prov.v4.rt.core.web.ODataServlet;

/**
 * Activates Web MVC and its @Controller classes via
 * RequestMappingHandlerMapping. Defines Spring beans for the application
 * context and triggers via @ComponentScan the search and the registration of
 * Beans. Beans are detected within @Configuration, @Component and @Controller
 * annotated classes.
 */
@Configuration
@EnableWebMvc
@ComponentScan(basePackages = "com.sap.rc")
@PropertySources({ @PropertySource("classpath:conf/conf.common.properties"),
		@PropertySource("classpath:conf/conf.properties") })
public class WebAppContextConfig implements WebMvcConfigurer {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Value("${cacheControl.max-age:3600}")
	private int cacheControlMaxage;
	
    @Value("${application.basepackage:com.sap.rc}")
    private String basePackage;

	@Bean
	@Profile("cloud")
	public static PropertySourcesPlaceholderConfigurer placeholderConfigurer() {
		// make environment variables available for Spring's @Value annotation
		return new PropertySourcesPlaceholderConfigurer();
	}

	@Bean
	public MethodValidationPostProcessor methodValidationPostProcessor() {
		return new MethodValidationPostProcessor();
	}

	@Bean 	// register logging servlet filter which logs HTTP request processing details
	public CommonsRequestLoggingFilter logFilter() {

		// servletContext.addFilter("RequestLoggingFilter",
		// RequestLoggingFilter.class).addMappingForUrlPatterns(null, false, "/*");

		CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
		filter.setIncludeQueryString(true);
		filter.setIncludePayload(true);
		filter.setMaxPayloadLength(10000);
		filter.setIncludeHeaders(false);
		filter.setAfterMessagePrefix("REQUEST DATA : ");
		return filter;
	}

    /**
     * Enable default view ("index.html") mapped under "/".
     */
    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }

	/**
	 * Set up the cached resource handling for OpenUI5 runtime served from the
	 * webjar in {@code /WEB-INF/lib} directory and local JavaScript files in
	 * {@code /resources} directory.
	 */
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {

		logger.trace("cacheControl.max-age: {}", cacheControlMaxage);
		registry.addResourceHandler("/resources/**").addResourceLocations("classpath:/resources/", "/resources/**")
				.setCachePeriod(cacheControlMaxage);
	}
	
	@Bean
	// Register oDataServlet and URL mapping here instead doing it in web.xml
	// Refer details in RCService.java
	public ServletRegistrationBean oDataServletRegistration() {
		
		ServletRegistrationBean srb = new ServletRegistrationBean();
		srb.setServlet(new ODataServlet());
		srb.setUrlMappings(Arrays.asList("/odata/*"));
		return srb;
	}

	@Bean
	// Specify package to scan for oData servide implementation class
	// Refer details in RCService.java
	public ServletContextInitializer contextInitializer() {
		
		return new ServletContextInitializer() {
			@Override
			public void onStartup(ServletContext servletContext) throws ServletException {
				servletContext.setInitParameter("package", basePackage);
			}
		};
	}

}
