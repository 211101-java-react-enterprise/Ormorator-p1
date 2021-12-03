package com.revature.orm;

import com.revature.orm.annotations.Column;
import com.revature.orm.annotations.Table;
import com.revature.orm.util.ConnectionFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

public class SQLMapper {

    public boolean create(Object obj) throws SQLException {
        try (Connection conn = ConnectionFactory.getInstance().getConnection()) {

            // Get columns & values from the object
            Class<?> objClass = obj.getClass();
            Method[] methods = getMethodsForSQL(objClass); // helper
            int fieldQty = methods.length;
            String[] columns = new String[fieldQty];
            for (int i = 0; i < fieldQty; i++) {
                columns[i] = methods[i].getAnnotation(Column.class).name();
            }

            // Build SQL subStrings
            String table = objClass.getAnnotation(Table.class).name();
            String columnString = String.join(",", columns);
            String questionMarks = buildQuestionMarksString(fieldQty); // helper

            // Build SQL string
            String sql = "insert into " + table + " (" + columnString + ") " +
                    "values " + "(" + questionMarks + ")";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            for (int i = 0; i < fieldQty; i++) {
                pstmt.setObject(i + 1, methods[i].invoke(obj));
             }
            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted != 0) {
                return true;
            }
        } catch (SQLException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    public ResultSet read(String table, String[] identifier) {
        try (Connection conn = ConnectionFactory.getInstance().getConnection()) {
            String sql = "select * from " + table + " where " + identifier[0] +
                    " = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, identifier[1]);
            return pstmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ResultSet read(String table, String[] id1,
                          String[] id2) {
        try (Connection conn = ConnectionFactory.getInstance().getConnection()) {
            String sql = "select * from " + table + " where " + id1[0] +
                    " = ? and " + id2[0] + " = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id1[1]);
            pstmt.setString(2, id2[1]);
            return pstmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean update(String table, String[][] changes, String[] id) {
        try (Connection conn = ConnectionFactory.getInstance().getConnection()) {
            String changesString = buildChangesString(changes); // helper
            String sql = "update " + table + " set " + changesString +
                    " where " + id[0] + " = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            for (int i = 0; i < changes.length; i++) {
                pstmt.setObject(i + 1, changes[i][1]);
            }
            pstmt.setObject(changes.length + 1, id[1]);
            System.out.println("\n"+ pstmt);
            if (pstmt.executeUpdate() > 0) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(String table, String[] id) {
        try(Connection conn = ConnectionFactory.getInstance().getConnection()) {
            String deleteSQL = "delete from " + table + " where " + id[0] +
                    " = ?";
            PreparedStatement pstmt = conn.prepareStatement(deleteSQL);
            pstmt.setObject(1, id[1]);
            int rowDeleted = pstmt.executeUpdate();
            if (rowDeleted != 0){
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Method[] getMethodsForSQL(Class<?> objClass) {
        return Arrays.stream(objClass.getDeclaredMethods())
            .filter(method -> method.isAnnotationPresent(Column.class))
            .toArray(Method[]::new);
    }

    private String buildQuestionMarksString(int fieldQty) {
        String[] qMarksArray = new String[fieldQty];
        Arrays.fill(qMarksArray, "?");
        return String.join(",", qMarksArray);
    }

    private String buildChangesString(String[][] changes) {
        String[] changeStrings = new String[changes.length];
        for (int i = 0; i < changes.length; i++) {
            changeStrings[i] = (changes[i][0] + " = ?");
        }
        return String.join(",", changeStrings);
    }

}
