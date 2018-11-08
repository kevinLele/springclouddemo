package com.cloud.kevin.resourceserver.config;

import org.apache.commons.io.IOUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.authentication.BearerTokenExtractor;
import org.springframework.security.oauth2.provider.authentication.TokenExtractor;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 用于Oauth2.0协议的资源服务器
 *
 * @author Kevin
 */
@Configuration
@EnableResourceServer
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {

    /**
     * 自定义tokenExtractor，允许通过cookie携带access_token来进行验证
     *
     * @return
     */
    @Bean
    public TokenExtractor cookieTokenExtractor() {
        return new BearerTokenExtractor() {
            @Override
            protected String extractHeaderToken(HttpServletRequest request) {
                String token = super.extractHeaderToken(request);

                if (StringUtils.isEmpty(token)) {
                    // 尝试从cookie中取
                    for (Cookie cookie : request.getCookies()) {
                        if ("access_token".toLowerCase().equals(cookie.getName().toLowerCase())) {
                            return cookie.getValue();
                        }
                    }
                }

                return token;
            }
        };
    }

    @Bean
    public JwtAccessTokenConverter jwtAccessTokenConverter() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();

        try {
            Resource resource = new ClassPathResource("AuthServer_public.key");
            String publicKey = IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);
            converter.setVerifierKey(publicKey);

            return converter;
        } catch (final IOException e) {
            throw new RuntimeException("用于进行验证的公钥读取发生错误！", e);
        }
    }

    @Bean
    public TokenStore jwtTokenStore() {
        return new JwtTokenStore(jwtAccessTokenConverter());
    }

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) {
        resources.resourceId("resourceServer1")
                .stateless(true)
                .tokenStore(jwtTokenStore())
                .tokenExtractor(cookieTokenExtractor());
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        // @formatter:off
        http
            .requestMatchers()
                .antMatchers("/resource/**")
                .and()
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
            .authorizeRequests()
                //.antMatchers("/product/**").access("#oauth2.hasScope('select') and hasRole('ROLE_USER')")
                .antMatchers("/resource/**").authenticated();
        // @formatter:on
    }
}
