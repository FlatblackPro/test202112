package com.bjpowernode.controller123;

import com.bjpowernode.service.BaseService;
import com.bjpowernode.util.JDBCUtil;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

public class DispatcherServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ServletContext application = request.getServletContext();
        String uri = null;
        String serviceName = null;
        String classPath = null;
        String serviceMethod = null;
        Class classFile = null;
        BaseService service = null;
        Method method = null;
        String result = null;
        //1. 调用请求对象，读取uri：
        uri = request.getRequestURI();
        //2. 通过字符串截串，得到classPath，也就是xml文件中注册的key
        serviceName = uri.substring(uri.lastIndexOf("/") + 1);
        System.out.println(serviceName);
        //3. 通过Key，得到value，然后就得到了类文件的地址：
        classPath = application.getInitParameter(serviceName);
        //4. 创建service类的实例对象(通过service类的全路径！)，
        //   然后实例化这个对象
        try {
            service = getService(classPath,application);
            System.out.println(service);
        //5. 初始化service对象
            initService(service,request);
        //6. 根据请求对象，得到调用service对象的方法
            serviceMethod = request.getParameter("method");
            result = invokeService(serviceMethod,service);
        //7.响应：
            request.getRequestDispatcher(result).forward(request,response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //service对象创建，确保只有一个相同的对象被创建：
    private BaseService getService(String classPath, ServletContext application){
        Class classFile = null;
        //1. 检测被访问的service类是否已存在：
        BaseService service = (BaseService) application.getAttribute(classPath);
        if (service != null){
            return service;
        }
        //2.创建service对象：
        try {
            classFile = Class.forName(classPath);
            service = (BaseService) classFile.newInstance();
            application.setAttribute(classPath,service);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return service;
    }

    //service对象的初始化——为service对象中的属性进行赋值操作
    //也就是为service调用的DAO类赋值。
    private void initService(BaseService service,HttpServletRequest request){
        Class classFile = null;
        Field[] fieldArray = null;
        //1.获得当前service对象隶属的类文件,也就是说，要确认这个service是deptService还是empService
        classFile = service.getClass();
        //2.获得当前service类文件的属性信息：
        fieldArray = classFile.getDeclaredFields();
        //3.对service的属性进行初始化——new Dao:
        for (Field field : fieldArray){
            field.setAccessible(true);
            String daoTypeName = field.getType().getName();//com.bjpowernode.Dao.deptDao
            Object daoInstance = request.getServletContext().getAttribute(daoTypeName);
            if (daoInstance != null){
                try {
                    field.set(service,daoInstance);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else {
                try {
                    classFile = Class.forName(daoTypeName);
                    daoInstance = classFile.newInstance();
                    field.set(service,daoInstance);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                request.getServletContext().setAttribute(daoTypeName,daoInstance);
            }
            service.request = request;
        }
    }

    //service对象相关调用
    private String invokeService(String methodName, BaseService service){
        Method method = null;
        String result = null;
        //通过事务管理service，来执行
        Connection conn = JDBCUtil.createConn();
        try {
            conn.setAutoCommit(false);
            method = service.getClass().getDeclaredMethod(methodName,null);
            //调用method进行业务处理
            try {
                result = (String) method.invoke(service,null);
                conn.commit();
            } catch (Exception e) {
                e.printStackTrace();
                //回滚
                conn.rollback();
            }finally {
                JDBCUtil.closeResultSet();
                JDBCUtil.closePreparedStatement();
                JDBCUtil.closeConnection();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return result;
    }
}
