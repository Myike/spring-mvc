package org.tzw.mvc.servlet;


import org.tzw.mvc.annotation.*;
import org.tzw.mvc.util.XmlUtil;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
    @SuppressWarnings("unchecked")
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
                //参数
                Parameter[] parameters = method.getParameters();
                //参数注解
                Annotation[][] parameterAnnotations = method.getParameterAnnotations();
                List<Object> methodParams = new ArrayList<>();
                for(int i = 0; i< parameters.length;i++) {
                    Parameter parameter = parameters[i];
                    String name = parameter.getName();
                    System.out.println("参数名："+name);
                    Class<?> parameterType = parameter.getType();
                    if(parameterType == HttpServletRequest.class) {
                        methodParams.add(req);
                    } else if(parameterType == HttpServletResponse.class) {
                        methodParams.add(resp);
                    }else {
                        if(parameterAnnotations[i].length > 0&& parameterAnnotations[i][0].annotationType().getName() == RequestParam.class.getName()) {
                            //参数在请求头中
                            Object requestBodyParam = parameterType.newInstance();
                            Field[] declaredFields = parameterType.getDeclaredFields();
                            for(Field field : declaredFields) {
                                String fieldName = field.getName();
                                if(parameterMap.containsKey(fieldName)) {
                                    String fieldNameMethodOfSetName = "set"+fieldName.substring(0, 1).toUpperCase()+fieldName.substring(1).toLowerCase();
                                    Method setMethod = parameterType.getMethod(fieldNameMethodOfSetName, field.getType());
                                    String paramValue = parameterMap.get(fieldName)[0];
                                    this.excuteMethod(requestBodyParam, field.getType(), paramValue, setMethod);
                                }
                            }
                            methodParams.add(requestBodyParam);
                        } else if(parameterAnnotations[i].length > 0&& parameterAnnotations[i][0].annotationType().getName() == RequestBody.class.getName())  {
                           //参数在请求体中
                            Object requestBodyParam = parameterType.newInstance();
                            Field[] declaredFields = parameterType.getDeclaredFields();
                            // 读取请求内容
                            BufferedReader br = new BufferedReader(new InputStreamReader(req.getInputStream()));
                            String line;
                            Map<String, Object> bodyParamMap = new HashMap<>();
                            while((line = br.readLine())!=null){
                                System.out.println(line);
                                if(line.contains("name=")){
                                    String key = line.substring(line.indexOf("=\"")+2).replaceAll("\"", "");
                                    br.readLine();
                                    bodyParamMap.put(key, br.readLine());
                                }
                            }
                            System.out.println(bodyParamMap.toString());
                            for(Field field : declaredFields) {
                                String fieldName = field.getName();
                                if(bodyParamMap.containsKey(fieldName)) {
                                    String fieldNameMethodOfSetName = "set"+fieldName.substring(0, 1).toUpperCase()+fieldName.substring(1).toLowerCase();
                                    Method setMethod = parameterType.getMethod(fieldNameMethodOfSetName, field.getType());
                                    String paramValue = bodyParamMap.get(fieldName).toString();
                                    this.excuteMethod(requestBodyParam, field.getType(), paramValue, setMethod);
                                }
                            }
                            methodParams.add(requestBodyParam);
                        } else {
                            //其他类型参数
                            if(parameterMap.containsKey(name)) {
                                String paramValue = parameterMap.get(name)[0];
                                if (parameterType.isAssignableFrom(int.class)
                                        || parameterType.isAssignableFrom(Integer.class)) {
                                    methodParams.add(Integer.parseInt(paramValue));
                                } else if (parameterType.isAssignableFrom(Double.class)
                                        || parameterType.isAssignableFrom(double.class)) {
                                    methodParams.add(Double.parseDouble(paramValue));
                                } else if (parameterType.isAssignableFrom(Boolean.class)
                                        || parameterType.isAssignableFrom(boolean.class)) {
                                    methodParams.add(Boolean.parseBoolean(paramValue));
                                } else if (parameterType.isAssignableFrom(Date.class)) {
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                    methodParams.add(dateFormat.parse(paramValue));
                                } else if (parameterType.isAssignableFrom(Timestamp.class)) {
                                    SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                    methodParams.add(new Timestamp(dateFormat.parse(paramValue).getTime()));
                                } else {
                                    methodParams.add(paramValue);
                                }
                            } else {
                                methodParams.add(null);
                            }
                        }
                    }
                }
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

    /**
     * 执行目标方法
     * @param instance
     * @param type
     * @param value
     * @param method
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws ParseException
     */
    private void excuteMethod(Object instance, Class type, Object value, Method method) throws InvocationTargetException, IllegalAccessException, ParseException {
        if(null != value) {
            // ---判断读取数据的类型
            if (type.isAssignableFrom(String.class)) {
                method.invoke(instance, value);
            } else if (type.isAssignableFrom(int.class)
                    || type.isAssignableFrom(Integer.class)) {
                method.invoke(instance, Integer.parseInt(value.toString()));
            } else if (type.isAssignableFrom(Double.class)
                    || type.isAssignableFrom(double.class)) {
                method.invoke(instance, Double.parseDouble(value.toString()));
            } else if (type.isAssignableFrom(Boolean.class)
                    || type.isAssignableFrom(boolean.class)) {
                method.invoke(instance, Boolean.parseBoolean(value.toString()));
            } else if (type.isAssignableFrom(Date.class)) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                method.invoke(instance, dateFormat.parse(value.toString()));
            } else if (type.isAssignableFrom(Timestamp.class)) {
                SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                method.invoke(instance, new Timestamp(dateFormat.parse(value.toString()).getTime()));
            }
        }

    }
}
