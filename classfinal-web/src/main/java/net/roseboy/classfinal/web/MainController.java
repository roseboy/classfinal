package net.roseboy.classfinal.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class MainController {

    @GetMapping("/")
    public String index(HttpServletRequest request) {
        request.setAttribute("ttt", "rrrrr");
        return "index";
    }
}
