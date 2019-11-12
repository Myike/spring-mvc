package org.tzw.mvc.servlet;


import org.tzw.mvc.annotation.*;
import org.tzw.mvc.util.XmlUtil;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.util.*;

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
    private Map<String, Method> urlMethodMap = new HashMap<>();

    /**
     * 实例集合
     */
    private Map<String, Object> instanceMap = new HashMap<>();

    /**
     * 方法-包名集合
     */
    private Map<Method, String> methodPackageMap = new HashMap<>();

    /**
     * 包名-注解集合
     */
    private Map<String, String> nameMap = new HashMap<>();

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("进入service");
        super.service(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp){
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=utf-8");
        String requestURI = req.getRequestURI().replaceAll(req.getContextPath(), "");
        Method method = urlMethodMap.get(requestURI);
        if(null != method) {
            String packageName = methodPackageMap.get(method);
            Map<String, String[]> parameterMap = req.getParameterMap();
            String controllerName = nameMap.get(packageName);
            Object controller = instanceMap.get(XmlUtil.lowerFirstCase(controllerName));
            try {
                method.setAccessible(true);
                Parameter[] parameters = method.getParameters();
                List<Object> methodParams = new ArrayList<>();
                for(Parameter parameter : parameters) {
                    String name = parameter.getName();
                    System.out.println("参数名："+name);
                    Class<?> parameterType = parameter.getType();
                    if(parameterType == HttpServletRequest.class) {
                        methodParams.add(req);
                    } else if(parameterType == HttpServletResponse.class) {
                        methodParams.add(resp);
                    }else {
                        //其他类型参数
                        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
                        if(parameterMap.containsKey(name)) {
                            String paramValue = parameterMap.get(name)[0].toString();

                            if(parameterType == int.class || parameterType == Integer.class) {
                                methodParams.add(Integer.parseInt(paramValue));
                            } else if(parameterType == double.class || parameterType == Double.class) {
                                methodParams.add(Double.parseDouble(paramValue));
                            } else {
                                methodParams.add(paramValue);
                            }
                        } else {
                            methodParams.add(null);
                        }
                    }
                }
//                methodParams.add("tzw");
//                methodParams.add(2);
                Object invoke = method.invoke(controller, methodParams.toArray());
                System.out.println("返回结果：" + invoke);
                resp.getWriter().write(invoke.toString());
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    resp.getWriter().write("抱歉发生异常了！异常堆栈信息： " + e.getMessage());
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
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
            //依赖赋值
            springIOC(instanceMap);
            //路径映射
            handleUrlMethodMap();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

    /**
     * 请求路径映射
     * @throws ClassNotFoundException
     */
    private void handleUrlMethodMap() throws ClassNotFoundException {
        if(packageNames.isEmpty()) {
            return;
        }
        for(String packageName : packageNames) {
            Class<?> fileClass = Class.forName(packageName);
            Method[] methods = fileClass.getMethods();
            if(fileClass.isAnnotationPresent(RequestMapping.class)) {
                RequestMapping requestMapping = fileClass.getAnnotation(RequestMapping.class);
                //检测方法路径
                for(Method method : methods) {
                    if(method.isAnnotationPresent(RequestMapping.class)) {
                        RequestMapping methodRequestMapping = method.getAnnotation(RequestMapping.class);
                        urlMethodMap.put(requestMapping.path()+methodRequestMapping.path(), method);
                        methodPackageMap.put(method, packageName);
                    }
                }
            }
        }
    }


    /**
     * 字段初始化
     * @param instanceMap
     * @throws IllegalAccessException
     */
    private void springIOC(Map<String, Object> instanceMap) throws IllegalAccessException {
        for(String nameKey : instanceMap.keySet()) {
            Object instance = instanceMap.get(nameKey);
            Field[] fields = instance.getClass().getDeclaredFields();
            for(Field field : fields) {
                if(field.isAnnotationPresent(AutoWired.class)) {
                    String name = field.getAnnotation(AutoWired.class).name();
                    field.setAccessible(true);      //关闭反射检查，提高反射速度
                    field.set(instance, instanceMap.get(name));
                }
            }
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
                    String name = !XmlUtil.isEmpty(controller.name()) ? fileClass.getSimpleName() : controller.name();
                    System.out.println("Controller名称："+name);
                    instanceMap.put(XmlUtil.lowerFirstCase(name), fileClass.newInstance());
                    nameMap.put(packageName, name);
                } else if(fileClass.isAnnotationPresent(Service.class)) {
                    Service service = fileClass.getAnnotation(Service.class);
                    String name = !XmlUtil.isEmpty(service.name()) ? fileClass.getSimpleName() : service.name();
                    System.out.println("Service名称："+name);
                    instanceMap.put(XmlUtil.lowerFirstCase(name), fileClass.newInstance());
                } else if(fileClass.isAnnotationPresent(Repository.class)) {
                    Repository repository = fileClass.getAnnotation(Repository.class);
                    String name = !XmlUtil.isEmpty(repository.name()) ? fileClass.getSimpleName() : repository.name();
                    System.out.println("Repository名称："+name);
                    instanceMap.put(XmlUtil.lowerFirstCase(name), fileClass.newInstance());
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
        File[] files = baseFile.listFiles();
        for(File file : files) {
            if(file.isDirectory()) {
                scanBasePackage(basePackage+"."+file.getName());
            } else if(file.isFile()) {
                packageNames.add(basePackage+"."+file.getName().split("\\.")[0]);
                System.out.println("扫描到文件是："+file.getName());
            }
        }
    }

    /**
     * 获得项目代码所在基本的包路径
     */
    private void getBasePackageNameFromConfigXml() {
        String nodeValue = XmlUtil.getNodeValue("component-scan", "spring-mvc.xml");
        this.basePackage = nodeValue;
    }
}
