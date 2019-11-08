package mvc.servlet;

import mvc.annotation.Controller;
import mvc.annotation.Repository;
import mvc.annotation.Service;
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
        try {
            //获取配置
            getBasePackageNameFromConfigXml();
            //设置扫描基本文件
            scanBasePackage(basePackage);
            //实例化
            instance(packageNames);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

    /**
     * 对象的初始化
     * @param packageNames
     */
    private void instance(Set<String> packageNames) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        if(!packageNames.isEmpty()) {
            for(String packageName : packageNames) {
                Class<?> fileClass = Class.forName(packageName);
                if(fileClass.isAnnotationPresent(Controller.class)) {
                    Controller controller = fileClass.getAnnotation(Controller.class);
                    String name = XmlUtil.isEmpty(controller.name()) ? fileClass.getSimpleName() : controller.name();
                    instanceMap.put(name, fileClass.newInstance());
                } else if(fileClass.isAnnotationPresent(Service.class)) {
                    Service service = fileClass.getAnnotation(Service.class);
                    String name = XmlUtil.isEmpty(service.name()) ? fileClass.getSimpleName() : service.name();
                    instanceMap.put(name, fileClass.newInstance());
                } else if(fileClass.isAnnotationPresent(Repository.class)) {
                    Repository repository = fileClass.getAnnotation(Repository.class);
                    String name = XmlUtil.isEmpty(repository.name()) ? fileClass.getSimpleName() : repository.name();
                    instanceMap.put(name, fileClass.newInstance());
                }
            }
        }
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
