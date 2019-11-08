package mvc.servlet;

import mvc.util.XmlUtil;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @Author: zhiwutu
 * @Date: 2019/11/7 17:41
 * @Description:  映射分发器
 */
public class DispatcherServlet extends HttpServlet {

    /**
     * 扫描基本包
     */
    private String basePackage = "";

    /**
     * 包名
     */
    private Set<String> packageNames = new HashSet<>();

    /**
     * 路径-方法映射map
     */
    private Map<String, String> urlMethodMap = new HashMap<>();

    /**
     * 实例集合
     */
    private Map<String, Object> instanceMap = new HashMap<>();


    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("进入service");
        super.service(req, resp);
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        getBasePackageNameFromConfigXml();
        scanBasePackage(basePackage);
    }



    /**
     * 扫描基本包路径下所有的文件
     * @param basePackage
     */
    private void scanBasePackage(String basePackage) {
        URL url = this.getClass().getClassLoader().getResource(basePackage.replaceAll("\\.", "/"));
        File baseFile = new File(url.getPath());
        System.out.println("扫描到文件是："+baseFile.getName());
        File[] files = baseFile.listFiles();
        for(File file : files) {
            if(file.isDirectory()) {
                scanBasePackage(basePackage+"."+file.getName());
            } else if(file.isFile()) {
                packageNames.add(basePackage+"."+file.getName().split("\\.")[0]);
            }
        }
    }

    /**
     * 获得项目代码所在基本的包路径
     */
    private void getBasePackageNameFromConfigXml() {
        String nodeValue = XmlUtil.getNodeValue("scan-package-path", "dispatcherServlet.xml");
        this.basePackage = nodeValue;
    }
}
