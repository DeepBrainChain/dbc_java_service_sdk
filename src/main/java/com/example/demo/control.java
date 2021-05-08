package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class control {

    @ResponseBody
    @RequestMapping("/hello")
    public String hello(){
        return "Hello";
    }
}
