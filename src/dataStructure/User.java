package dataStructure;

import java.net.Socket;

public class User {
	String username;
	String avatarPath;
	boolean isOnline;
	boolean isEncrypted;
	String messagePw;
	String token;
	String algorithm;
	String key;
	Socket socket;

	public User(String name) {
		username = name;
	}

	public User(String username, boolean isEncrypted, String messagePw, String token, String algorithm, String key,
			Socket socket) {
		this.username = username;
		this.isOnline = true;
		this.isEncrypted = isEncrypted;
		this.messagePw = messagePw;
		this.token = token;
		this.algorithm = algorithm;
		this.key = key;
		this.socket = socket;
	}

	@Override
	public int hashCode() {
		return username.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		User user = (User)o;
		return username.equals(user.username);
	}

	public String getUsername() {
		return username;
	}

	public String getToken() {
		return token;
	}

	public Socket getSocket() {
		return socket;
	}

	public boolean isOnline() {
		return isOnline;
	}

	public String getAlgorithm() {
		return algorithm;
	}

	public String getKey() {
		return key;
	}

	public void setOnline(boolean isOnline) {
		this.isOnline = isOnline;
	}


}
