package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import cipher.AES;
import cipher.MD5;
import dataStructure.User;

public class DBLogin {
	String jdbc_url = "jdbc:mysql://192.168.1.13:3306/edmond_chat_room";
	String username_db = "edmond";
	String password_db = "edmond_1216";

	public DBLogin(String address, String username_db, String password_db) {
//		this.jdbc_url = "jdbc:mysql://"+address+":3306/edmond_chat_room\"";
//		this.username_db = username_db;
//		this.password_db = password_db;
	}

	public boolean validate(String username, String password) {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			System.out.println("query from database...");
			Connection connection = DriverManager.getConnection(jdbc_url, username_db, password_db);
//			Statement statement = connection.createStatement();
			String sql = "SELECT * FROM `users` WHERE username = ? AND password= ?";
			PreparedStatement statement = connection.prepareStatement(sql);
			statement.setString(1, username);
			statement.setString(2, MD5.toMd5(password));
			ResultSet resultset = statement.executeQuery();
			Boolean valid = resultset.next();
//			System.out.println(resultset.getString("user_name"));;
			System.out.println("database query returned "+ valid);
			resultset.close();
			statement.close();
			connection.close();
			return valid;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean updatePw(String username, String password) {
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

	public ArrayList<User> getUserList() {
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

	public boolean checkEncryption(String username) {
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

	public String getMessagePw(String username) {
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

	public void storeMessage(String time, String source, String dest, String message, boolean encrypted,
			boolean inbound) {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			Connection connection = DriverManager.getConnection(jdbc_url, username_db, password_db);
			String sql = "INSERT INTO `message`(`date`, `time`, `source`, `dest`, `message`, `encrypted`, `inbound`) VALUES (?,?,?,?,?,?,?)";
			PreparedStatement statement = connection.prepareStatement(sql);
			Date currentDate = new Date();
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
			statement.setString(1, dateFormat.format(currentDate));
			statement.setString(2, time);
			statement.setString(3, source);
			statement.setString(4, dest);
			statement.setString(5, message);
			statement.setBoolean(6, encrypted);
			statement.setBoolean(7, inbound);
			statement.execute();
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void acceptMessage(String time, String source, String dest, String message) {
		if (checkEncryption(source)) {// 选择加密的情况
			String key = getMessagePw(source);
			String messageEn = AES.fakeEncryption(message);
			storeMessage(time, source, dest, messageEn, true, true);
		} else {
			storeMessage(time, source, dest, message, false, true);
		}
		if (checkEncryption(dest)) {
			String key = getMessagePw(dest);
			String messageEn = AES.fakeEncryption(message);
			storeMessage(time, source, dest, messageEn, true, false);
		} else {
			storeMessage(time, source, dest, message, false, false);
		}
	}

	public static void main(String[] args) {
//		new DBLogin();
//		System.out.println(validate("jam", "dd"));
//		System.out.println(checkEncryption("jam"));
//		storeMessage("9:52", "jam", "pet", "school day is good", false, true);
	}
}
