package database;

import server.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;

import dataStructure.User;

public class DBLogin {
	static String jdbc_url = "jdbc:mysql://localhost:3306/edmond_chat_room";
	static String username_db = "root";
	static String password_db = "";

	static public boolean validate(String username, String password) {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			System.out.println("query from database...");
			Connection connection = DriverManager.getConnection(jdbc_url, username_db, password_db);
			Statement statement = connection.createStatement();
			String sql = "SELECT * FROM `users` WHERE username = '"+username+"' AND password='"+password+"'";
			ResultSet resultset = statement.executeQuery(sql);
			Boolean valid = resultset.next();
//			System.out.println(resultset.getString("user_name"));;
			System.out.println("database query returned");
			resultset.close();
			statement.close();
			connection.close();
			return valid;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	static public boolean updatePw(String username, String password) {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			System.out.println("update password...");
			Connection connection = DriverManager.getConnection(jdbc_url, username_db, password_db);
			Statement statement = connection.createStatement();
//			String sql1 = "UPDATE users SET `password`='"+password+"' WHERE user_name='"+username+"'";
			String sql1 = "UPDATE `users` SET `password`='"+password+"' WHERE user_name='"+username+"'";
			int rowsAffected = statement.executeUpdate(sql1);
//			statement.close();
//			connection.close();
			if (rowsAffected > 0) {
				String sql2 = "INSERT INTO `history_pass`(`user_name`, `password`, `changed_date`) VALUES ('"+username
						+"','"+password+"','"+new Date().toString()+"')";
				statement.executeUpdate(sql2);
				return true;
			}
//			System.out.println(resultset.getString("user_name"));;
			else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	static public ArrayList<User> getUserList() {
		ArrayList<User> allUsers = new ArrayList<>();
		try {
			Connection connection = DriverManager.getConnection(jdbc_url, username_db, password_db);
			Statement statement = connection.createStatement();
			String sql = "SELECT * FROM `users` WHERE 1";
			ResultSet rs = statement.executeQuery(sql);
			while (rs.next()) {
				User userObj = new User(rs.getString("username"));
				allUsers.add(userObj);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return allUsers;
	}

	static public boolean checkEncryption(String username) {
		try {
			Connection connection = DriverManager.getConnection(jdbc_url, username_db, password_db);
//			Statement statement = connection.createStatement();
			String sql = "SELECT  `encrypted` FROM `users` WHERE username=?";
			PreparedStatement statement = connection.prepareStatement(sql);
			statement.setString(1, username);
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				boolean result = rs.getBoolean("encrypted");
				return result;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	static public String getMessagePw(String username) {
		try {
			Connection connection = DriverManager.getConnection(jdbc_url, username_db, password_db);
//			Statement statement = connection.createStatement();
			String sql = "SELECT  `message_pw` FROM `users` WHERE username=?";
			PreparedStatement statement = connection.prepareStatement(sql);
			statement.setString(1, username);
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				String result = rs.getString("message_pw");
				return result;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "";
	}

	public static void main(String[] args) {
//		new DBLogin();
//		System.out.println(validate("jam", "dd"));
		System.out.println(checkEncryption("jam"));
	}
}
