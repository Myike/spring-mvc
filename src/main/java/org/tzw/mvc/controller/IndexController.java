package org.tzw.mvc.controller;

import org.tzw.mvc.annotation.*;
import org.tzw.mvc.param.IndexParam;
import org.tzw.mvc.service.IndexService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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


    @RequestMapping(path = "/index")
    public String index(HttpServletRequest request, HttpServletResponse response, @RequestParam  IndexParam param) {
        return "Hello Index; Request Param: " + (null != param ? param.toString() : "null");
    }

    @RequestMapping(path = "/index2")
    public String index2(HttpServletRequest request, HttpServletResponse response, @RequestBody IndexParam param) {
        return "Hello Index2; Request2 Param: " + (null != param ? param.toString() : "null");
    }

    @RequestMapping(path = "/login")
    public String login(HttpServletRequest request, HttpServletResponse response) {
        String result = "Loing successful! Welcome to my SpringMvc!" + indexService.login();
        return result;
    }

}
