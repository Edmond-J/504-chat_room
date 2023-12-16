package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import cipher.*;
import database.DBLogin;
import dataStructure.Friend;
import dataStructure.User;

public class ServerThread extends Thread implements Runnable {
	Socket socket;
	DBLogin db;
	BufferedReader in;
	PrintWriter out;
	User thisUser;

	public ServerThread(Socket socket, DBLogin db) {
		this.socket = socket;
		this.db = db;
		try {
			InputStreamReader inputStream = new InputStreamReader(socket.getInputStream());
			in = new BufferedReader(inputStream);
			out = new PrintWriter(socket.getOutputStream(), true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		String req;
		try {
			while ((req = in.readLine()) != null) {
				Gson gson = new Gson();
				if (req.contains("public_key")) {
//				JsonObject jsonObject = gson.fromJson(initialReq, JsonObject.class);
					String publicKeyString = new String(
							Files.readAllBytes(Paths.get(Server.configPath+"rsa\\publicKey")));
					JsonObject messageObject = new JsonObject();
					messageObject.addProperty("res_type", "key_delivery");
					messageObject.addProperty("key", publicKeyString);
					out.println(gson.toJson(messageObject));
					req = in.readLine();
				}
				if (req.contains("validate")) {
					JsonObject rawJson = gson.fromJson(req, JsonObject.class);
					String encryBody = rawJson.get("body").getAsString();
					String decryBody = RSA.decrypt(encryBody,
							RSA.getPrivateFromFile(Server.configPath+"rsa\\privateKey"));
					JsonObject jsonObject = gson.fromJson(decryBody, JsonObject.class);
					String username = jsonObject.get("user").getAsString();
					String password = jsonObject.get("password").getAsString();
					if (isUserOnline(username)) {
						JsonObject messageObject = new JsonObject();
						messageObject.addProperty("res_type", "301:user_already_online");
						out.println(gson.toJson(messageObject));
					} else {
						if (db.validate(username, password)) {
							String algorithm = jsonObject.get("algorithm").getAsString();
							String key = jsonObject.get("key").getAsString();
							String token = UUID.randomUUID().toString();
							boolean isEncrypted = db.checkEncryption(username);
							String messagePw = db.getMessagePw(username);
//						String aesKey = "";
							thisUser = new User(username, isEncrypted, messagePw, token, algorithm, key, socket);
							// 加入AES密钥
							Server.onlineUsers.add(thisUser);
							JsonObject messageObject = new JsonObject();
							messageObject.addProperty("token", token);
							messageObject.addProperty("user", username);
							messageObject.addProperty("isEncrypted", isEncrypted);
							ArrayList<User> allUsers = db.getUserList();
							ArrayList<Friend> allFriends = new ArrayList<>();
//						HashMap<String, Boolean> friendStatus = new HashMap<>();
							for (User user : allUsers) {
								if (Server.onlineUsers.contains(user))
									user.setOnline(true);
//							friendStatus.put(user.getUsername(), user.isOnline());
								allFriends.add(new Friend(user.getUsername(), user.isOnline()));
							}
							String jsonList = gson.toJson(allFriends, new TypeToken<ArrayList<Friend>>() {
							}.getType());
							messageObject.addProperty("friends", jsonList);
							String bodyTxEn = CipherBox.encrypt(gson.toJson(messageObject), algorithm, key);
							JsonObject jsonToSend = new JsonObject();
							jsonToSend.addProperty("res_type", "200:login_succeed");
							jsonToSend.addProperty("body", bodyTxEn);
							out.println(gson.toJson(jsonToSend));
							onlineBroadcast(username);
							System.out.println(username+" is on line");
						} else {
							JsonObject messageObject = new JsonObject();
							messageObject.addProperty("res_type", "300:invalid_combination");
							out.println(gson.toJson(messageObject));
							System.out.println(username+":wrong passward");
						}
					}
				}
				if (req.contains("message")) {
					JsonObject rawJson = gson.fromJson(req, JsonObject.class);
					String sourceUser = rawJson.get("source").getAsString();
					String encryBody = rawJson.get("body").getAsString();
					String decryBody = CipherBox.decrypt(encryBody, thisUser.getAlgorithm(), thisUser.getKey());
					System.out.println("107"+thisUser.getKey());
					JsonObject jsonObject = gson.fromJson(decryBody, JsonObject.class);
					String token = jsonObject.get("token").getAsString();
					String dest = jsonObject.get("dest").getAsString();
					String message = jsonObject.get("message_body").getAsString();
					if (!token.equals(thisUser.getToken())) {
						// 返回相应消息
						return;
					}
					User tagetUser = thisUser;
					for (User user : Server.onlineUsers) {
						if (user.getUsername().equals(dest)) {
							tagetUser = user;
						}
					}
					Socket destSocket = tagetUser.getSocket();
					JsonObject messageObject = new JsonObject();
//							messageObject.addProperty("req_type", "message");
					DateTimeFormatter dtf = DateTimeFormatter.ofPattern("hh:mm");
					String currentTime = dtf.format(LocalTime.now());
					messageObject.addProperty("time", currentTime);
					messageObject.addProperty("source", sourceUser);// 加入user为了防止恶意攻击,需要用一个合理的user身份证明，使用用户输入值不可靠
					messageObject.addProperty("dest", tagetUser.getUsername());
					messageObject.addProperty("message_body", message);
					String bodyTxEn = CipherBox.encrypt(gson.toJson(messageObject), tagetUser.getAlgorithm(),
							tagetUser.getKey());
					JsonObject jsonToSend = new JsonObject();
					jsonToSend.addProperty("res_type", "paging");
					jsonToSend.addProperty("body", bodyTxEn);
					PrintWriter out = new PrintWriter(destSocket.getOutputStream(), true);
					out.println(gson.toJson(jsonToSend));
					db.acceptMessage(currentTime, sourceUser, dest, message);
				}
				if (req.contains("log_out")) {
					offlineBroadcast(thisUser.getUsername());
					System.out.println(thisUser.getUsername()+" is offline");
					Server.onlineUsers.remove(thisUser);
					in.close();
					out.close();
					socket.close();
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean isUserOnline(String username) {
		for (User user : Server.onlineUsers) {
			if (user.getUsername().equals(username)) {
				return true;
			}
		}
		return false;
	}

	private void onlineBroadcast(String username) {
		for (User user : Server.onlineUsers) {
			try {
				PrintWriter out = new PrintWriter(user.getSocket().getOutputStream(), true);
				JsonObject messageObject = new JsonObject();
				messageObject.addProperty("res_type", "online_broadcast");
				messageObject.addProperty("new_user", username);
				Gson gson = new Gson();
				System.out.println(user.getSocket().getLocalSocketAddress());
				System.out.println(gson.toJson(messageObject));
				out.print(gson.toJson(messageObject));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void offlineBroadcast(String username) {
		
	}
}