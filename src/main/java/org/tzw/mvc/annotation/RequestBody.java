package org.tzw.mvc.annotation;

import java.lang.annotation.*;

/**
 * @Author: zhiwutu
 * @Date: 2019/11/13 15:08
 * @Description:
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestBody {
}
