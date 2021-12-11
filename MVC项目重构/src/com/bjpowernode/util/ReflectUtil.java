package com.bjpowernode.util;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReflectUtil {
    //要求请求参数名称和实体类中的参数名称完全一致！
    public static Object init(Class classFile, HttpServletRequest request){
        Field[] fields = null;
        Object instance = null;
        String fieldName, value, typeName;
        fields = classFile.getDeclaredFields();
        //通过classfile，创建实体类对象！
        try {
            instance = classFile.newInstance();
            for (Field field:
                 fields) {
                fieldName = field.getName();
                value = request.getParameter(fieldName);
                field.setAccessible(true);
                typeName = field.getType().getSimpleName();
                if (null != value && !"".equals(value)){
                    if ("Integer".equals(typeName)){
                        field.set(instance,Integer.valueOf(value));
                    }else if ("String".equals(typeName)){
                        field.set(instance,value);
                    }else if ("Double".equals(typeName)){
                        field.set(instance,Double.valueOf(value));
                    }
                }
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return instance;
    }

    //通过反射机制，生成SQL语句
    //新增语句：
    public static String createInsert(Object instance){ //这里的instance之后就是emp类
        StringBuffer sql = new StringBuffer("insert into ");
        StringBuffer columns = new StringBuffer("(");
        StringBuffer values = new StringBuffer(" values(");
        Class classFile = null;
        String tableName = null;
        Field fields[] = null;
        //通过getClass()方法，可以让对象类返回一个Class，从而可以访问这个对象类的属性、方法等等。
        //比如要访问类的变量值，那么要new类，同理，要获得属性、方法等，那么就要通过getClass（）；
        classFile = instance.getClass();
        tableName = classFile.getSimpleName();
        fields = classFile.getDeclaredFields();
        for (Field field:
                fields) {
            String fieldName = field.getName();
            //需要判断，如果是空串或者null，那么不插入：
            field.setAccessible(true);
            Object value = null;
            try {
                value = field.get(instance);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            if (value != null && !"".equals(value)){
                //如果这个字段不是第一个字段，那么就在这个字段前面加一个小括号。
                if (!columns.toString().equals("(")){
                    columns.append(",");
                    values.append(",");
                }
                columns.append(fieldName);
                values.append("'");
                values.append(value);
                values.append("'");
            }
        }
        columns.append(")");
        values.append(")");

        //整理sql语句：
        sql.append(tableName);
        sql.append(columns);
        sql.append(values);
        return sql.toString();
    }

    //查询：
    public static List findAll(Class classFile, ResultSet rs){
        List empList = new ArrayList();
        ResultSetMetaData rsmd = null;
        Field[] fieldArray = null;
        Object instance = null;
        try {
            rsmd = rs.getMetaData();

            while (rs.next()){
                //得到rs中每一个元素的字段数：
                int count = rsmd.getColumnCount();
                //实例化实体类
                instance = classFile.newInstance();
                //对这些字段进行遍历：
                for (int i = 1; i <= count; i++) {
                    //拿到字段的名字：
                    String columnName = rsmd.getColumnName(i);
                    //通过字段名字，拿到字段的value；
                    String value = rs.getString(columnName);
                    if (value != null && !"".equals(value)){
                        //通过字段名字，拿到字段的type；
                        /*String columnType = rsmd.getColumnTypeName(i);*/
                        //通过反射，拿到实体类的属性数组（属性名=字段名）
                        fieldArray = classFile.getDeclaredFields();

                        //这里可以做判断，如果属性名=字段名，那么就赋值！
                        for (int j = 0; j < fieldArray.length; j++){
                            //如果属性名相等：
                            if (fieldArray[j].getName().equalsIgnoreCase(columnName)){
                                fieldArray[j].setAccessible(true);
                                String fieldType = fieldArray[j].getType().getSimpleName();
                                if (fieldType.equals("Integer")){
                                    fieldArray[j].set(instance,Integer.valueOf(value));
                                } else if (fieldType.equals("Double")){
                                    fieldArray[j].set(instance, Double.valueOf(value));
                                } else if (fieldType.equals("String")){
                                    fieldArray[j].set(instance,value);
                                }
                            }
                        }
                    }
                }
                empList.add(instance);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return empList;
    }

    //删除：
    public static String createDelete(Object instance, String fieldName){
        StringBuffer sql = new StringBuffer("delete from ");
        StringBuffer where = new StringBuffer(" where ");
        Class classFile = instance.getClass();
        String tableName = null;
        Field[] fieldArray = null;
        tableName = classFile.getSimpleName();
        fieldArray = classFile.getDeclaredFields();
        for (Field field : fieldArray){
            field.setAccessible(true);
            if (field.getName().equalsIgnoreCase(fieldName)){
                Object value = null;
                try {
                    value = field.get(instance);
                    where.append(field.getName());
                    where.append("=");
                    where.append("'");
                    where.append(value);
                    where.append("'");
                    break;
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        sql.append(tableName);
        sql.append(where);
        return sql.toString();
    }
}
