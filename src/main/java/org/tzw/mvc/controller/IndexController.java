package org.tzw.mvc.controller;

import org.tzw.mvc.annotation.AutoWired;
import org.tzw.mvc.annotation.Controller;
import org.tzw.mvc.annotation.RequestMapping;
import org.tzw.mvc.service.IndexService;

/**
 * @Author: zhiwutu
 * @Date: 2019-11-09 21:34
 * @Description:
 * @since JDK 1.8
 */
@Controller
@RequestMapping(path = "/index")
public class IndexController {

    @AutoWired(name = "indexService")
    private IndexService indexService;

    @RequestMapping(path = "/login")
    public String login() {
        return "Loing successful! Welcome to my SpringMvc!" + indexService.login();
    }

}
