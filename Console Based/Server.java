import java.io.*;
import java.util.*;
import java.net.*;

public class Server extends Thread{
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
		try{
		while(true){
		socket = serverSocket.accept();

		System.out.println("New request received at : " + socket);

		DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
		DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

		System.out.println("Creating a new client...");

		String username = dataInputStream.readUTF();
		while (Server.nameVectorContains(username)) {
			dataOutputStream.writeUTF("This username is taken, please choose a different username");
			username = dataInputStream.readUTF();
		}
		dataOutputStream.writeUTF("You are now connected to server\nType Help() to get the commands list");
		ClientServerThreads match = new ClientServerThreads(socket, username, dataInputStream, dataOutputStream);

		Thread t = new Thread(match);

		System.out.println("Adding this client to the online client list");

		clientsArray.add(match);
		clientServerArray.add(clientsArray.size()%2);
		//System.out.println("size : "+clientServerArray.size());
		
		vectorPrint(clientsArray);
		t.start();

		System.out.println("Client number : " + clientsArray.size());
		}
		}catch(Exception e){
			//e.printStackTrace();
		}
	}
	public Server(int socketNumber, Initiator initiator ) throws IOException {
		this.serverSocket = new ServerSocket(socketNumber);
		this.initiator=initiator;
	}
}

class ClientServerThreads implements Runnable {

	Scanner scn = new Scanner(System.in);
	public String name;
	final DataInputStream dataInputStream;
	final DataOutputStream dataOutputStream;
	Socket socket;

	public static String vectorPrintString(Vector<ClientServerThreads> ar) {
		String ret = ("Current connected cilents : ");
		for (ClientServerThreads o : ar) {
			ret += (o.name + ", ");
		}
		return ret;
	}
	
	public static boolean countCommas(String msg){
		int count=0;
		boolean flag=false;
		for(int i=0;i<msg.length();i++){
			if(flag && ((int)msg.charAt(i))==44){
				return false;
			}
			if(((int)msg.charAt(i))==44 && !flag){
				flag=true;
				count++;
				continue;
			}
			if(flag && ((int)msg.charAt(i))!=44){
				flag=false;
			}
		}
		
		return count==3;
	}

	public static void vectorPrint(Vector<ClientServerThreads> clientArray) {
		System.out.print("Current connected cilents : ");
		for (ClientServerThreads o : clientArray) {
			System.out.print(o.name + ", ");
		}
		System.out.println();
	}

	public ClientServerThreads(Socket socket, String name, DataInputStream dataInputStream, DataOutputStream dataOutputStream) {
		this.dataInputStream = dataInputStream;
		this.dataOutputStream = dataOutputStream;
		this.name = name;
		this.socket = socket;
	}

	@Override
	public void run() {
		System.out.println("The socket has been opened");
		String received;
		while (true) {
			if (this.socket != null && this.socket.isConnected()) {
				try {
					received = dataInputStream.readUTF();
					if (received.equals("BYE") || received.equals("QUIT")) {

						try {

							//this.isloggedin = false;
							int k=0;
							for (ClientServerThreads cst : Server.clientsArray) {
								if (cst.name.equals(this.name)){
									Server.clientServerArray.remove(k);
									break;
								}
								k++;
							}
							Server.clientsArray.remove(this);
							
							System.out.println("A user has logged off");
							
							vectorPrint(Server.clientsArray);
							this.dataInputStream.close();
							this.dataOutputStream.close();

						} catch (IOException e) {

						}
						break;
					}
					if (received.equals("GetMemberList()")) {
						dataOutputStream.writeUTF(vectorPrintString(Server.clientsArray));
						continue;
					}


					if (Server.clientsArray.contains(this) && countCommas(received)) {
						StringTokenizer st = new StringTokenizer(received, ",");
						String src = st.nextToken();
						String recipient = st.nextToken();
						int ttl = 2;
						try {
							ttl = Integer.parseInt(st.nextToken());
						} catch (Exception e) {

						}
						ttl--;
						int temp=-1;
						String MsgToSend = st.nextToken();
						int k=0;
						for (ClientServerThreads cst : Server.clientsArray) {
							if (cst.name.equals(src)){
								temp=Server.clientServerArray.get(k);
							}
							k++;
						}
						k=0;
						for (ClientServerThreads cst : Server.clientsArray) {
							if(ttl<=0){
								this.dataOutputStream.writeUTF("Message couldn't get sent");
								break;
							}
							if (this.socket.isConnected())
								if (cst.name.equals(recipient)) {
									
									if(Server.clientServerArray.get(k)!=temp)
										ttl--;
									if(ttl<=0){
										this.dataOutputStream.writeUTF("Message couldn't get sent");
										break;
									}
									cst.dataOutputStream.writeUTF(this.name + " : " + MsgToSend);
									break;
								}
							k++;
						}
					}else{
						this.dataOutputStream.writeUTF("invalid command");
					}
				} catch (IOException e) {

					e.printStackTrace();
				}

			}

		}
	}
}
