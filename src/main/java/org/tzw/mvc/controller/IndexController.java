package org.tzw.mvc.controller;

import org.tzw.mvc.annotation.AutoWired;
import org.tzw.mvc.annotation.Controller;
import org.tzw.mvc.annotation.RequestMapping;
import org.tzw.mvc.service.IndexService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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
    public String login(HttpServletRequest request, HttpServletResponse response) {
        String result = "Loing successful! Welcome to my SpringMvc!" + indexService.login();
        return result;
    }

}
