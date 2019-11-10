package org.tzw.mvc.annotation;

import java.lang.annotation.*;

/**
 * @Author: zhiwutu
 * @Date: 2019/11/8 15:57
 * @Description:
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target(ElementType.TYPE)
public @interface Service {

    public String name() default "";

}
