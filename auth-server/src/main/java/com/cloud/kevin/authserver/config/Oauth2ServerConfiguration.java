package com.cloud.kevin.authserver.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用于OAuth2.0 协议的授权服务器配置
 * 客户端模式：           http://localhost:8080/oauth/token?grant_type=client_credentials&scope=select&client_id=client_1&client_secret=123456
 * 密码模式：             http://localhost:8080/oauth/token?grant_type=password&scope=select&username=admin&password=123456&client_id=client_1&client_secret=123456
 * 授权码模式：           http://localhost:8080/oauth/authorize?client_id=client_1&response_type=code&redirect_uri=http://www.baidu.com
 * 获取访问码：           http://localhost:8080/oauth/token?grant_type=authorization_code&code=DVzCLM&redirect_uri=http://www.baidu.com&client_id=client_1&client_secret=123456
 * 通过访问码访问受限资源： http://localhost:8081/resource/order/1?access_token=eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOlsicmVzb3VyY2VTZXJ2ZXIxIl0sImNsaWVudElkIjoiY2xpZW50XzEiLCJzY29wZSI6WyJzZWxlY3QiXSwiZXhwIjoxNTQxNTAwMTczLCJhdXRob3JpdGllcyI6WyJjbGllbnQiXSwianRpIjoiMzliNThjODEtYjUyOC00MmFmLThiM2UtNWQzM2EzYjJmMzhiIiwiY2xpZW50X2lkIjoiY2xpZW50XzEifQ.fOfFlTlI-Jj2MzWLe3yogjavLCAm8UwF1S5jJEkDfwNdYa_-CTETan6edUgf-vh29_cEr1sGnr8C4hKJBtjybLHLpfvki4A5SzweCtEKGJemU2IwZcKGeEqVAe-pNKXN9HmgqXCsjJBGp14VsP_-NGwfS30J-Q8ktbA-ViwjU8Y
 * 简易模式：            http://localhost:8080/oauth/authorize?client_id=client_1&response_type=token&redirect_uri=http://www.baidu.com
 * 刷新访问码：          http://localhost:8080/oauth/token?grant_type=refresh_token&client_id=client_1&client_secret=123456&refresh_token=eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOlsicmVzb3VyY2VTZXJ2ZXIxIl0sInVzZXJfbmFtZSI6ImFkbWluIiwic2NvcGUiOlsic2VsZWN0Il0sImF0aSI6ImNlNDk4YTM4LWI0YTQtNGM4ZS1iYTRkLTVmYjEzZTYxM2ZkNSIsImV4cCI6MTU0MTQ5NDM4NywidGVzdDEiOiJoZWxsbyB3b3JsZCEiLCJhdXRob3JpdGllcyI6W3siYXV0aG9yaXR5IjoiUk9MRV9BRE1JTiJ9LHsiYXV0aG9yaXR5IjoiUk9MRV9VU0VSIn0seyJhdXRob3JpdHkiOiJVU0VSOkRFTCJ9XSwianRpIjoiYTUxZGViOWEtYmM3OC00MjlmLTg1ZTktM2I2ZDZjZGNhZmExIiwiY2xpZW50X2lkIjoiY2xpZW50XzEiLCJ1c2VybmFtZSI6ImFkbWluIn0.Qdt2z-njAhyhljKbWLXBjd13ayhAQ88E3eMkqVi0kssqDv-io7cksc-thBFIirAPZ3e-69z9DM78GmiVm16Z1zT-0h1f2EesVvH1iXQYO4xTcyc5rLxYerrATeQH5at_XLtqfUD-cR6Ue0zaXLXNfnqkOHL9lNNHabsLCwlONvE
 *
 * @author Kevin
 * @date 11/8/2017
 */
@Configuration
@EnableAuthorizationServer
public class Oauth2ServerConfiguration extends AuthorizationServerConfigurerAdapter {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private TokenEnhancer tokenEnhancer;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Bean
    public JwtAccessTokenConverter jwtAccessTokenConverter() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();

        // authserver.jks : 证书库文件的访问路径
        // storepwd : 证书库的访问密码
        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(
                new ClassPathResource("authserver.jks"), "storepwd".toCharArray());

        // authServer : 证书的名称（在证书库中的唯一标识）
        // keypwd : 证书的访问密码
        converter.setKeyPair(keyStoreKeyFactory.getKeyPair("authServer", "keypwd".toCharArray()));

        return converter;
    }

    @Bean
    public TokenEnhancer tokenEnhancer(JwtAccessTokenConverter jwtAccessTokenConverter) {
        TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
        List<TokenEnhancer> tokenEnhancerList = new ArrayList<>();

        tokenEnhancerList.add((accessToken, authentication) -> {
            final Map<String, Object> additionalInfo = new HashMap<>();
            Authentication userAuthentication = authentication.getUserAuthentication();

            if (null != userAuthentication) {
                User user = (User) userAuthentication.getPrincipal();
                additionalInfo.put("username", user.getUsername());
            } else {
                String clientId = authentication.getOAuth2Request().getClientId();
                additionalInfo.put("clientId", clientId);
            }

            ((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(additionalInfo);

            return accessToken;
        });

        tokenEnhancerList.add(jwtAccessTokenConverter);
        tokenEnhancerChain.setTokenEnhancers(tokenEnhancerList);

        return tokenEnhancerChain;
    }

    /**
     * 声明TokenStore实现
     *
     * @return
     */
    @Bean
    public TokenStore jwtTokenStore() {
        return new JwtTokenStore(jwtAccessTokenConverter());
    }

    @Bean
    public UserDetailsService userDetailsService() throws Exception {
        return username -> {
            List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
            // 角色： 以ROLE_做为前缀
            grantedAuthorities.add(new StringGrantedAuthority("ROLE_ADMIN"));
            grantedAuthorities.add(new StringGrantedAuthority("ROLE_USER"));
            // 权限用冒号进行分隔，表示层级关系
            grantedAuthorities.add(new StringGrantedAuthority("USER:DEL"));

            return new org.springframework.security.core.userdetails.User(
                    // 用户名
                    "admin",
                    // 密码
                    "$2a$10$CtVeDT1V7cFu9gby4bEu.uuPnKxhqI9H7ScRRJM/EzacEKdgxTS7S",
                    // 是否可用
                    true,
                    // 是否过期
                    true,
                    // 证书不过期为true
                    true,
                    // 账户未锁定为true
                    true,
                    // 权限列表
                    grantedAuthorities);
        };
    }

    /**
     * 声明安全约束，哪些允许访问，哪些不允许访问
     *
     * @param security
     * @throws Exception
     */
    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        security.allowFormAuthenticationForClients();
    }

    /**
     * client客户端的信息配置，client信息包括：clientId、secret、scope、authorizedGrantTypes、authorities
     *
     * @param clients
     * @throws Exception
     */
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.jdbc(dataSource);
    }

    /**
     * 声明授权和token的端点以及token的服务的一些配置信息，比如采用什么存储方式、token的有效期等
     *
     * @param endpoints
     * @throws Exception
     */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints
                .tokenStore(jwtTokenStore())
                .userDetailsService(userDetailsService())
                // 用于Oauth2的密码模式，authenticationManager用于对传入的用户信息进行认证
                .authenticationManager(authenticationManager)
                .tokenEnhancer(tokenEnhancer)
                .allowedTokenEndpointRequestMethods(HttpMethod.GET, HttpMethod.POST);
    }

    private static class StringGrantedAuthority implements GrantedAuthority {

        private String authorityCode;

        public StringGrantedAuthority(String authorityCode) {
            Assert.hasText(authorityCode, "A granted authority textual representation is required");
            this.authorityCode = authorityCode;
        }

        @Override
        public String getAuthority() {
            return authorityCode;
        }
    }

    /*@Bean
    public UserDetailsService userDetailsService(IUserService userService) {
        return username -> {
            if (StringUtils.isEmpty(username)) {
                throw new UsernameNotFoundException("UserName is empty!");
            }

            User user = userService.getByName(username);

            // 用户名不存在
            if (user == null) {
                throw new UsernameNotFoundException("UserName " + username + " not found!");
            }

            List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
            // 角色： 以ROLE_做为前缀
            grantedAuthorities.add(new StringGrantedAuthority("ROLE_ADMIN"));
            grantedAuthorities.add(new StringGrantedAuthority("ROLE_USER"));
            // 权限用冒号进行分隔，表示层级关系
            grantedAuthorities.add(new StringGrantedAuthority("USER:DEL"));

            return new org.springframework.security.core.userdetails.User(
                    // 用户名
                    user.getName(),
                    // 密码
                    user.getPassword(),
                    // 是否可用
                    true,
                    // 是否过期
                    true,
                    // 证书不过期为true
                    true,
                    // 账户未锁定为true
                    true,
                    // 权限列表
                    grantedAuthorities);
        };
    }*/
}
