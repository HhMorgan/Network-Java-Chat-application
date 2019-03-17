import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.Vector;

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

	public static boolean countCommas(String msg) {
		int count = 0;
		boolean flag = false;
		for (int i = 0; i < msg.length(); i++) {
			if (flag && ((int) msg.charAt(i)) == 44) {
				return false;
			}
			if (((int) msg.charAt(i)) == 44 && !flag) {
				flag = true;
				count++;
				continue;
			}
			if (flag && ((int) msg.charAt(i)) != 44) {
				flag = false;
			}
		}

		return count == 3;
	}

	public static void vectorPrint(Vector<ClientServerThreads> clientArray) {
		System.out.print("Current connected cilents : ");
		for (ClientServerThreads o : clientArray) {
			System.out.print(o.name + ", ");
		}
		System.out.println();
	}

	public ClientServerThreads(Socket socket, String name, DataInputStream dataInputStream,
			DataOutputStream dataOutputStream) {
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
				System.out.println(received);
				if (received.equals("BYE") || received.equals("QUIT")) {

					try {
						int k = 0;
						for (ClientServerThreads cst : Server.clientsArray) {
							if (cst.name.equals(this.name)) {
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

				} else if (received.equals("GetMemberList()")) {
					dataOutputStream.writeUTF(vectorPrintString(Server.clientsArray));
					continue;
				}

				else if (Server.clientsArray.contains(this) && countCommas(received)) {
					StringTokenizer st = new StringTokenizer(received, ",");
					String src = st.nextToken();
					String recipient = st.nextToken();
					int ttl = 2;
					try {
						ttl = Integer.parseInt(st.nextToken());
					} catch (Exception e) {

					}
					ttl--;
					int temp = -1;
					String MsgToSend = st.nextToken();
					int k = 0;
					for (ClientServerThreads cst : Server.clientsArray) {
						if (cst.name.equals(src)) {
							temp = Server.clientServerArray.get(k);
						}
						k++;
					}
					k = 0;
					for (ClientServerThreads cst : Server.clientsArray) {
						if (ttl <= 0) {
							this.dataOutputStream.writeUTF("Message couldn't get sent");
							break;
						}
						if (this.socket.isConnected())
							if (cst.name.equals(recipient)) {

								if (Server.clientServerArray.get(k) != temp)
									ttl--;
								if (ttl <= 0) {
									this.dataOutputStream.writeUTF("Message couldn't get sent");
									break;
								}
								cst.dataOutputStream.writeUTF(this.name + " : " + MsgToSend );
								break;
							}
						k++;
					}
				} else {
					this.dataOutputStream.writeUTF("invalid command from server");
				}
			} catch (IOException e) {

				e.printStackTrace();
			}

		}

		 }
	}
}
