package org.tzw.mvc.param;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Author: zhiwutu
 * @Date: 2019-11-12 22:18
 * @Description:
 * @since JDK 1.8
 */
public class IndexParam {

    private String name;

    private String password;

    private int age;

    private Date date;

    @Override
    public String toString() {
        return "Request Param are : " + name +";"+password+";"+age+";"+formateDate(date,"yyyy-MM-dd HH:mm:ss");
    }

    private String formateDate(Date date, String pattern) {
        if(null == date) {
            return "";
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        return simpleDateFormat.format(date);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
