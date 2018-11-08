package com.cloud.kevin.resourceserver.controller.resource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Kevin
 * @date 11/13/2017
 */
@RestController
@RequestMapping("/resource/order")
@Slf4j
public class OrderEndpoints {

    @GetMapping("/{id}")
    @PreAuthorize("#oauth2.hasScope('select')")
    public String getById(@PathVariable String id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return "order id : " + authentication.getPrincipal();
    }
}
