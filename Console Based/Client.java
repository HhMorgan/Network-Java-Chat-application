import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Scanner;

public class Client {
	final static int ServerPort1 = 6000;
	final static int ServerPort2 = 6669;
	static boolean loop = true;
	static int[] counter;
	static int serverPort;
	Scanner scn;
	Socket socket;
	DataInputStream dataInputStream;
	DataOutputStream dataOutputStream;
	public static boolean isValid(String input, int length) {
		if (input.length() < length) {
			return false;
		}
		if (input.length() <= 6) {
			return false;
		}
		String commandPart = input.substring(0, 5);
		if (!(commandPart.equals("Join(")))
			return false;
		if (input.charAt(input.length() - 1) != ')')
			return false;

		return true;
	}

	public static String validJoin(String usernameCommand) {
		Scanner scn = new Scanner(System.in);
		String commandPart = usernameCommand.substring(0, 4);
		while (((!(commandPart.equals("Join("))) && usernameCommand.charAt(usernameCommand.length() - 1) != ')')
				&& usernameCommand.length() <= 6) {
			usernameCommand = scn.nextLine();
			while (!isValid(usernameCommand, 4)) {
				usernameCommand = scn.nextLine();
			}
			commandPart = usernameCommand.substring(0, 3);
		}
		return usernameCommand.substring(5, usernameCommand.length() - 1);
	}
	public void start() throws UnknownHostException, IOException{
		scn = new Scanner(System.in);

		System.out.println("please enter the 'Join(username)' command with the username that you want to use");
		String usernameCommand = scn.nextLine();

		while (!isValid(usernameCommand, 4)) {
			System.out.println("You have entered a wrong command, please enter the 'Join(username)' command");
			usernameCommand = scn.nextLine();
		}

		String username = validJoin(usernameCommand);
		FileInputStream fileIn = new FileInputStream("counter.txt");
		ObjectInputStream in = new ObjectInputStream(fileIn);
		try {
			counter = (int[]) in.readObject();
			//System.out.println(Arrays.toString(counter));
		} catch (ClassNotFoundException e1) {

			e1.printStackTrace();
		}
		if (counter[0] <= counter[1]) {
			socket = new Socket("localhost", ServerPort1);
			try {
				FileOutputStream fileOut = new FileOutputStream(new File("counter.txt"));
				ObjectOutputStream out = new ObjectOutputStream(fileOut);
				counter[0]++;
				serverPort = 0;
				out.writeObject(counter);
				out.close();
				fileOut.close();
			} catch (IOException i) {
				i.printStackTrace();
			}
		} else {
			socket = new Socket("localhost", ServerPort2);
			try {
				FileOutputStream fileOut = new FileOutputStream(new File("counter.txt"));
				ObjectOutputStream out = new ObjectOutputStream(fileOut);
				counter[1]++;
				serverPort = 1;
				out.writeObject(counter);
				out.close();
				fileOut.close();
			} catch (IOException i) {
				i.printStackTrace();
			}
		}
		dataInputStream = new DataInputStream(socket.getInputStream());
		dataOutputStream = new DataOutputStream(socket.getOutputStream());
		dataOutputStream.writeUTF(username);
	}
	
	
	public void sndMsg(){
		Thread messageSending = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {

					String message = scn.nextLine();

					try {

						if (message.equals("Help()")) {
							System.out.println(
									"The available commands are as follows :\nGetMemberList() : Gets the online users\nChat(Source,Desination,TTL,Message) : Sends a Message from the Source to the Destination with the counter TTL to prevent infinite looping, TTL can be left empty by leaving an empty space\nBYE or QUIT: Logs you off");
							continue;
						} else {
							if (message.equals("BYE") || message.equals("QUIT")) {
								FileInputStream fileIn = new FileInputStream("counter.txt");
								ObjectInputStream in = new ObjectInputStream(fileIn);
								try {
									counter = (int[]) in.readObject();
									// System.out.println(Arrays.toString(counter));
									counter[serverPort]--;
									FileOutputStream fileOut = new FileOutputStream(new File("counter.txt"));
									ObjectOutputStream out = new ObjectOutputStream(fileOut);
									out.writeObject(counter);
									out.close();
									fileOut.close();
								} catch (IOException i) {
									i.printStackTrace();
								} catch (ClassNotFoundException e) {
									e.printStackTrace();
								}
								dataOutputStream.writeUTF(message);
								socket.close();
								loop = false;
								System.out.println("You have logged off");
								break;
							} else {
								if (message.equals("GetMemberList()")) {
									dataOutputStream.writeUTF("GetMemberList()");
								} else {
									if (message.length() > 5 && message.substring(0, 5).equals("Join(")) {
										dataOutputStream.writeUTF(message.substring(5, message.length() - 1));
									} else {

										if (message.length() > 5 && message.substring(0, 5).equals("Chat(")
												&& message.charAt(message.length() - 1) == ')') {
											dataOutputStream.writeUTF(message.substring(5, message.length() - 1));
										} else {
											System.out.println("Invalid command");
										}
									}
								}
							}
						}

					} catch (IOException e) {
						System.out.println("Invalid input, restart the console");
					}
				}
			}
		});
		messageSending.start();
	}
	public void rcvMsg(){
		Thread messageReading = new Thread(new Runnable() {
			@Override
			public void run() {

				while (true) {
					if (loop) {
						try {
							if (socket != null && socket.isConnected()) {
								String msg = dataInputStream.readUTF();
								System.out.println(msg);
							} else {
								if (socket.isClosed())
									break;
							}
						} catch (IOException e) {

						}
					} else {
						try {
							dataInputStream.close();
							dataOutputStream.close();
						} catch (IOException e) {
						}

						break;
					}
				}
			}
		});
		messageReading.start();
	}
	public static void main(String args[]) throws UnknownHostException, IOException {
		Client client=new Client();
		client.start();
		client.sndMsg();
		client.rcvMsg();
	}
}