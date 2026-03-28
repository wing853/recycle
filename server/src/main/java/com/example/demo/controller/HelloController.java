package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping(value = "/", produces = "text/plain;charset=UTF-8")
    public String hello() {
        return "서버가 정상적으로 작동 중입니다!";
    }
}
