package server;

public class User {
	String username;
	String avatarPath;
	boolean isOnline;
	String token;
	String status;
	String key;

	public User(String name) {
	}

	public User(String username, String token, String key) {
		this.username = username;
		this.isOnline = true;
		this.token = token;
		this.status = "online";
		this.key = key;
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

	public void setStatus(String status) {
		this.status = status;
	}
	
}
