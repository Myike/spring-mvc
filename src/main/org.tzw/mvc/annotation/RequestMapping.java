package mvc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * @Author: zhiwutu
 * @Date: 2019/11/8 15:58
 * @Description:  路径注解
 */
@Target({ElementType.TYPE,ElementType.METHOD})
public @interface RequestMapping {
    public String path();
}
