package org.tzw.mvc.repository;

import org.tzw.mvc.annotation.Repository;

/**
 * @Author: zhiwutu
 * @Date: 2019-11-09 21:34
 * @Description:
 * @since JDK 1.8
 */
@Repository
public class IndexRepository {


    public String findUser() {
        return "repository";
    }

}
