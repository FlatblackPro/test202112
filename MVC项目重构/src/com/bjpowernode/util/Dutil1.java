package com.bjpowernode.util;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Dutil1 {
    Connection conn = null;
    ServletContext application = null;
    PreparedStatement ps = null;

    public Connection connection(ServletRequest request){
        application = request.getServletContext();
        Map connMap = (ConcurrentHashMap)application.getAttribute("connection");
        Iterator it = connMap.keySet().iterator();
        while (it.hasNext()){
            conn = (Connection) it.next();
            boolean flag = (boolean) connMap.get(conn);
            if (flag == true){
                connMap.put(conn,false);
                return conn;
            }
        }
        return null;
    }

    public PreparedStatement getPs(String sql, ServletRequest request){
        try {
            ps = connection(request).prepareStatement(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ps;
    }

    public void close(ServletRequest request){
        if (ps != null) {
            try {
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        application = request.getServletContext();
        Map connMap = (ConcurrentHashMap) application.getAttribute("connection");
        connMap.put(conn, true);
    }

    public void close(){
        if (ps != null) {
            try {
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
