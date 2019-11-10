package org.tzw.mvc.annotation;

import java.lang.annotation.*;

/**
 * @Author: zhiwutu
 * @Date: 2019/11/8 15:58
 * @Description:
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Repository {

    public String name() default "";

}
