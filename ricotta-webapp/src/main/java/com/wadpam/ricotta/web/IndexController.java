package com.wadpam.ricotta.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by Ola on Nov 12, 2010
 */
@Controller
@RequestMapping("/index.html")
public class IndexController {
    @RequestMapping(method = RequestMethod.GET)
    public String getIndex() {
        return "index";
    }
}
