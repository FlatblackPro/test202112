package com.bjpowernode.util;

import java.sql.*;

public class JDBCUtil {
    private static Connection conn;
    private static PreparedStatement ps;
    private static ResultSet rs;

    static {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    //创建Connection
    public static Connection createConn(){
        try {
            DriverManager.getConnection("jdbc:mysql://localhost:3306/bjpowernode","uroot","123456");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }

    //创建PreparedStatement：
    public static PreparedStatement createPS(){
        try {
            ps = conn.prepareStatement("");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ps;
    }

    //推送DML命令：insert update delete
    public static int executeUpdate(String dml) throws Exception{
        if (ps == null){
            createPS();
        }
        int flag = ps.executeUpdate(dml);
        return flag;
    }

    //推送DQL命令：select
    public static ResultSet executeQuery(String dql) throws Exception{
        if (ps == null){
            createPS();
        }
        rs = ps.executeQuery(dql);
        return rs;
    }

    //销毁resultset：
    public static void closeResultSet(){
        if (rs != null){
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    //销毁PS：
    public static void closePreparedStatement(){
        if (ps != null){
            try {
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                ps = null;
            }
        }
    }

    //销毁连接通道：
    public static void closeConnection(){
        if (conn != null){
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static Connection getConn() {
        return conn;
    }

    public static PreparedStatement getPs() {
        return ps;
    }

    public static ResultSet getRs() {
        return rs;
    }
}
