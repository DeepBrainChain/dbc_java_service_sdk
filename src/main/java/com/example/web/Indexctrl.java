package com.example.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class Indexctrl {

    @Autowired
    private MetadataService metadataService;

    @GetMapping
    public String show(Model model) {
        model.addAttribute("metadata", metadataService.get());
        return "index";
    }
}
