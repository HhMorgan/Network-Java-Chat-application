import java.io.*;
import java.util.*;
import java.net.*;

public class Server extends Thread {
	Socket socket;
	ServerSocket serverSocket;
	Initiator initiator;
	public static Vector<ClientServerThreads> clientsArray = new Vector<>();
	public static Vector<Integer> clientServerArray = new Vector<>();

	public static void vectorPrint(Vector<ClientServerThreads> clientsArray) {
		System.out.print("Current connected cilents : ");
		for (ClientServerThreads o : clientsArray) {
			System.out.print(o.name + ", ");
		}
		System.out.println();
	}

	public static boolean nameVectorContains(String name) {
		for (ClientServerThreads o : Server.clientsArray) {
			if (o.name.equals(name)) {

				return true;
			}
		}
		return false;
	}

	@Override
	public void run() {
		try {
			while (true) {
				socket = serverSocket.accept();

				System.out.println("New request received at : " + socket);

				DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
				DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

				System.out.println("Creating a new client...");

				String username = dataInputStream.readUTF();
				while (Server.nameVectorContains(username)) {
					dataOutputStream.writeUTF("This username is taken, please choose a different username\n");
					username = dataInputStream.readUTF();
				}
				dataOutputStream.writeUTF("You are now connected to server\nType Help() to get the commands list\n");
				ClientServerThreads match = new ClientServerThreads(socket, username, dataInputStream,
						dataOutputStream);
				Thread t = new Thread(match);

				System.out.println("Adding this client to the online client list");

				clientsArray.add(match);
				clientServerArray.add(clientsArray.size() % 2);
				vectorPrint(clientsArray);
				t.start();

				System.out.println("Client number : " + clientsArray.size());
			}
		} catch (Exception e) {
		}
	}

	public Server(int socketNumber, Initiator initiator) throws IOException {
		this.serverSocket = new ServerSocket(socketNumber);
		this.initiator = initiator;
	}
}
