package server;

import database.DBLogin;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import cipher.RSA;

public class ServerThread extends Thread implements Runnable {
	ArrayList<User> onlineUsers = new ArrayList<>();
	Socket socket;

	public ServerThread(Socket s) {
		socket = s;
	}

	@Override
	public void run() {
		try {
			InputStreamReader inputStream = new InputStreamReader(socket.getInputStream());
			BufferedReader in = new BufferedReader(inputStream);
			PrintWriter outp = new PrintWriter(socket.getOutputStream(), true);
			String initialReq = in.readLine();
			if (initialReq.contains("public_key")) {
				Gson gson = new Gson();
//				JsonObject jsonObject = gson.fromJson(initialReq, JsonObject.class);
				String publicKeyString = new String(Files.readAllBytes(Paths.get(Server.configPath+"rsa\\publicKey")));
				JsonObject messageObject = new JsonObject();
				messageObject.addProperty("res_type", "key_delivery");
				messageObject.addProperty("key", publicKeyString);
				outp.println(gson.toJson(messageObject));
				initialReq = in.readLine();
			}
			String validateReq = RSA.decrypt(initialReq, RSA.getPrivateFromFile(Server.configPath+"rsa\\privateKey"));
			Gson gson = new Gson();
			JsonObject jsonObject = gson.fromJson(validateReq, JsonObject.class);
			String reqType = jsonObject.get("req_type").getAsString();
			if (reqType.equals("validate")) {
				String username = jsonObject.get("user").getAsString();
				String password = jsonObject.get("password").getAsString();
				if (DBLogin.validate(username, password)) {
					String token = UUID.randomUUID().toString();
					User sub = new User(username, token, token);
					//加入AES密钥
					onlineUsers.add(sub);
					JsonObject messageObject = new JsonObject();
					messageObject.addProperty("res_type", "200");
					messageObject.addProperty("token", token);
					messageObject.addProperty("user", username);
					// 如何发送好友列表
					ArrayList<User>allUsers=DBLogin.getUserList();
					for (User user : allUsers) {
						if(onlineUsers.contains(user))
							user.setOnline(true);
					}
					String jsonList = gson.toJson(allUsers, new TypeToken<ArrayList<User>>() {}.getType());
					messageObject.addProperty("friends", jsonList);
					outp.println(gson.toJson(messageObject));
				}
			}
//			System.out.println("服务器："+in.readLine());
//			DataInputStream in;
//			in = new DataInputStream(socket.getInputStream());
//			System.out.println(in.readUTF());
//			PrintWriter outp=new PrintWriter(socket.getOutputStream());
//			outp.println("服务器->客户端："+ LocalDateTime.now()+socket.getLocalSocketAddress());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}