package mvc.util;

import org.dom4j.*;

/**
 * @Author: zhiwutu
 * @Date: 2019/11/8 17:12
 * @Description:  读取xml配置文件工具类
 */
public class XmlUtil {

    public static String getNodeValue(String nodeName, String xmlPath) {
        try {
            Document document = DocumentHelper.parseText(xmlPath);
            Element element = (Element) document.selectSingleNode(nodeName);
            if(null != element) {
                return element.getStringValue();
            }
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return "";
    }
}
