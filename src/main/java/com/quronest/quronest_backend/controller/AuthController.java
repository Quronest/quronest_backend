package com.quronest.quronest_backend.controller;

import com.quronest.quronest_backend.config.Urls;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(Urls.PUBLIC_AUTH_CONTROLLER)
public class AuthController {

    public AuthController() {
    }
}
