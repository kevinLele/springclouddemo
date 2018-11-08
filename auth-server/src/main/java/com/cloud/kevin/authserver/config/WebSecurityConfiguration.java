package com.cloud.kevin.authserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.session.HttpSessionEventPublisher;

/**
 * Web Scurity 配置类
 *
 * @author Kevin
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // @formatter:off
        http
            /*
             * 表示只有满足requestMatchers中的条件的请求，下面的authorizeRequests,formLogin等等配置才会生效
             * 这样可以针对不同的请求路径配置不同的参数(例如不同的登陆页面)，requestMatcher未配置的情况下默认匹配所有请求
            */
            .requestMatchers()
                .antMatchers("/**")
                .and()
            // 鉴权规则定义
			.authorizeRequests()
                .anyRequest().authenticated()
                .and()
            // 表单登陆
			.formLogin()
                // 更新后的配置，指定了登录页面的位置, permitAll()允许所有用户访问这个页面。
                .loginPage("/login.html").permitAll()
                // 与form表单的action保持一致即可, 表示将拦载所设置的URL请求并进行用户名密码校验
                .loginProcessingUrl("/loginAction")
                // 登陆失败返回的页面
                .failureUrl("/loginFailed.html")
                .failureHandler((httpServletRequest, httpServletResponse, e) -> {
                    System.out.printf("登陆失败！");
                    System.out.println(e);
                })
                // 自定义用户名参数名称, 未设置时默认为username
                .usernameParameter("uname")
                // 自定义密码参数名称, 未设置时默认为password
                .passwordParameter("pwd")
                // 验证成功后默认跳转的页面, 当alwaysUse为true时表示不管之前访问的是什么URL都统一跳转到该默认页面
                .defaultSuccessUrl("/helloadmin", false)
                .and()
            // 注销登陆
            .logout()
                // 触发注销操作的url，默认是/logout。如果开启了CSRF保护(默认开启),那么请求必须是POST方式。
                .logoutUrl("/logout")
                // 添加一个LogoutHandler, 用于自定义注销时需要执行的操作。默认情况下， SecurityContextLogoutHandler 被作为最后一个 LogoutHandler
                //.addLogoutHandler(logoutHandler)
                // 注销操作发生后重定向到的url，默认为/login?logout
                .logoutSuccessUrl("/logoutSuccess.html").permitAll()
                // 让你指定自定义的 LogoutSuccessHandler。如果指定了， logoutSuccessUrl()的设置将会被忽略。
                //.logoutSuccessHandler(logoutSuccessHandler)
                // 指定在注销的时候是否销毁 HttpSession 。默认为True
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                // 允许指定当注销成功时要移除的cookie的名称。这是显示添加CookieClearingLogoutHandler的一种快捷处理方式
                .deleteCookies("JSESSIONID")
                .and()
            // session相关配置
            .sessionManagement()
                // 同一个用户只能有一个session, 也就是说一个帐号只允许登陆一次，不允许多终端登陆
                .maximumSessions(1)
                    // 为true时不允许登陆并返回到登陆失败页面，为false时可以登陆，之前登陆的session将失效并跳转到expiredUrl设置的页面
                    .maxSessionsPreventsLogin(true)
                    // 当用户的session个数大于maximumSessions的限制时当前session被设置为无效，访问时跳转到expiredUrl设置的页面
                    .expiredUrl("/expired.html")
                    .and()
                .and()
            // CSRF保护，默认登陆时会验证_csrf参数的正确性
            .csrf()
                // 取消CSRF Filter的处理
                .disable()
            // 错误处理相关配置
            .exceptionHandling()
                // 当访问被拒绝（即当前用户没有权限访问访URL时）时的跳转页面
                .accessDeniedPage("/accessDenied.html");
        // @formatter:on
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/js/**",
                "/css/**",
                "/images/**",
                "/**/favicon.ico");
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    /**
     * 设置用户密码的加密方式为BCrypt加密
     *
     * @return
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    /**
     * SpringSecurity内置的session监听器
     *
     * @return
     */
    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }
}
