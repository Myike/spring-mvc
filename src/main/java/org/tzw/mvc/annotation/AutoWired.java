package org.tzw.mvc.annotation;

import java.lang.annotation.*;

/**
 * @Author: zhiwutu
 * @Date: 2019-11-09 14:39
 * @Description:
 * @since JDK 1.8
 */
@Target(ElementType.FIELD)
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoWired {

    public String name();

}
