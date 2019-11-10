package org.tzw.mvc.annotation;

import java.lang.annotation.*;

/**
 * @Author: zhiwutu
 * @Date: 2019/11/8 15:57
 * @Description:  请求控制器注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target(ElementType.TYPE)
public @interface Controller {

    public String name() default "";
}
