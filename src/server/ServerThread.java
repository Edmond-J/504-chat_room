package server;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;

public class ServerThread extends Thread implements Runnable {
	Socket socket;

	public ServerThread(Socket s) {
		socket = s;
	}

	@Override
	public void run() {
		try {
			DataInputStream in;
			in = new DataInputStream(socket.getInputStream());
			System.out.println(in.readUTF());
			PrintWriter outp=new PrintWriter(socket.getOutputStream());
			outp.println("服务器->客户端："+ LocalDateTime.now()+socket.getLocalSocketAddress());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}