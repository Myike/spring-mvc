package org.tzw.mvc.util;

import org.dom4j.*;
import org.dom4j.io.SAXReader;

import java.io.InputStream;

/**
 * @Author: zhiwutu
 * @Date: 2019/11/8 17:12
 * @Description:  读取xml配置文件工具类
 */
public class XmlUtil {

    public static String getNodeValue(String nodeName, String xmlPath) {
        try {
            ClassLoader classLoader = XmlUtil.class.getClassLoader();
            InputStream resourceAsStream = classLoader.getResourceAsStream(xmlPath);
            SAXReader reader = new SAXReader();
            Document document = reader.read(resourceAsStream);
            Element rootElement = document.getRootElement();
            System.out.println(rootElement.getName());
            if(null != rootElement) {
                return  rootElement.attributeValue("base-package");
            }
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static boolean isEmpty(String string) {
        if(null == string)return false;
        if(null == string ||  "".equals(string) || string.length() < 1) return false;
        return true;
    }

    public static String lowerFirstCase(String str){
        char[] chars = str.toCharArray();
        chars[0] +=32;
        return String.valueOf(chars);

    }
}
