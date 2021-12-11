package com.bjpowernode.util;

import java.sql.*;
import java.util.ResourceBundle;

public class Dutil {
    private static ResourceBundle resourceBundle = ResourceBundle.getBundle("resource.db");
    private static String url = resourceBundle.getString("url");
    private static String username = resourceBundle.getString("user");
    private static String password = resourceBundle.getString("password");
    private static Connection conn = null;



//------------------------此处为Servlet监听器所需要的方法--------------------------------------

//--------------------------------------------------------------
    //注册驱动：
    static {
        try {
            Class.forName(resourceBundle.getString("driver"));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    //定义私有的构造方法，防止new对象！！
    private Dutil() {
    }
    //连接到数据库：
    public static Connection connection() throws SQLException {
        //Connection conn = null;
        conn = DriverManager.getConnection(url,username,password);
        return conn;
    }

/*    //创建STATEMENT：
    public static Statement createStmt() throws SQLException{
        return connection().createStatement();
    }

    //创建PREPARED_STATEMENT：
    public static PreparedStatement createPS(String sql)throws SQLException{
        return connection().prepareStatement(sql);
    }*/

    //释放资源：
    public static void releaseResource(ResultSet rs, Statement stmt, Connection conn){
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }
}
