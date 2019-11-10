package org.tzw.mvc.service;

import org.tzw.mvc.annotation.AutoWired;
import org.tzw.mvc.annotation.Service;
import org.tzw.mvc.repository.IndexRepository;

/**
 * @Author: zhiwutu
 * @Date: 2019-11-09 21:34
 * @Description:
 * @since JDK 1.8
 */
@Service
public class IndexService {

    @AutoWired(name = "indexRepository")
    private IndexRepository indexRepository;

    public String login() {
        return "service-" + indexRepository.findUser();
    }

}
