import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class Client {
	final static int ServerPort1 = 6000;
	final static int ServerPort2 = 6669;
	static boolean loop = true;
	static int[] counter;
	static int serverPort;
	Scanner scn;
	Socket socket;
	Thread messageSending;
	Thread messageReading;
	DataInputStream dataInputStream;
	DataOutputStream dataOutputStream;
	private static JFrame frame;
	private static JTextField textField;
	private static JTextArea textArea;

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

	public String validJoin(String usernameCommand) {
		// Scanner scn = new Scanner(System.in);
		String commandPart = usernameCommand.substring(0, 4);
		while (((!(commandPart.equals("Join("))) && usernameCommand.charAt(usernameCommand.length() - 1) != ')')
				&& usernameCommand.length() <= 6) {
			usernameCommand = this.scn.nextLine();
			while (!isValid(usernameCommand, 4)) {
				usernameCommand = this.scn.nextLine();
			}
			commandPart = usernameCommand.substring(0, 3);
		}
		return usernameCommand.substring(5, usernameCommand.length() - 1);
	}

	public void start(String scn) throws UnknownHostException, IOException {
		String usernameCommand = scn;

		while (!isValid(usernameCommand, 4)) {
			textArea.append("You have entered a wrong command, please enter the 'Join(username)' command\n");
			usernameCommand = scn;
		}

		String username = validJoin(usernameCommand);
		FileInputStream fileIn = new FileInputStream("counter.txt");
		ObjectInputStream in = new ObjectInputStream(fileIn);
		try {
			counter = (int[]) in.readObject();
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
		textArea.append("Your selected username for this session is : "+username+"\n");
		this.scn = new Scanner(textField.getText());
		String msg = dataInputStream.readUTF();
		textArea.append(msg);
	}

	public void sndMsg() {
		Scanner scn = null;
		FileInputStream fileIn2;
		ObjectInputStream in2 = null;
		try {
			fileIn2 = new FileInputStream("msg.txt");
			in2 = new ObjectInputStream(fileIn2);
		} catch (Exception e) {

		}
		try {
			scn = new Scanner((String) in2.readObject());
		} catch (ClassNotFoundException e1) {

			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(scn.hasNext());
		String message = "";
		if (scn.hasNext()) {
			message = scn.nextLine();
		}
		System.out.println(message);

		try {

			if (message.equals("Help()")) {
				textArea.append(
						"The available commands are as follows :\nGetMemberList() : Gets the online users\nChat(Source,Desination,TTL,Message) : Sends a Message from the Source to the Destination with the counter TTL to prevent infinite looping, TTL can be left empty by leaving an empty space\nBYE or QUIT: Logs you off\n");
			} else {
				if (message.equals("BYE") || message.equals("QUIT")) {
					FileInputStream fileIn = new FileInputStream("counter.txt");
					ObjectInputStream in = new ObjectInputStream(fileIn);
					try {
						counter = (int[]) in.readObject();
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
					textArea.append("You have logged off\n");
					frame.dispose();
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
								textArea.append("Invalid command from client\n");
							}
						}
					}
				}
			}

		} catch (IOException e) {
			textArea.append("Invalid input, restart the console\n");
		}

	}

	public void rcvMsg() {
		messageReading = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					if (loop) {
						try {
							if (socket != null && socket.isConnected()) {
								String msg = dataInputStream.readUTF();
								System.out.println(msg);
								textArea.append(msg + "\n");
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
	}

	public static void main(String args[]) throws UnknownHostException, IOException {
		Client client = new Client();
		frame = new JFrame();
		frame.setBounds(100, 100, 753, 789);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		textArea = new JTextArea();
		textArea.setBounds(0, 0, 731, 659);
		textArea.setEditable(false);
		textArea.setVisible(true);
		frame.getContentPane().add(textArea);

		textField = new JTextField();
		textField.setBounds(0, 661, 610, 72);
		frame.getContentPane().add(textField);
		textField.setColumns(10);
		JButton btnNewButton = new JButton("Send");
		textArea.append("please enter the 'Join(username)' command with the username that you want to use\n");
		btnNewButton.addActionListener(new ActionListener() {
			int flag = 0;

			public void actionPerformed(ActionEvent e) {
				String input = textField.getText();
				try {
					FileOutputStream fileOut = new FileOutputStream(new File("msg.txt"));
					ObjectOutputStream out = new ObjectOutputStream(fileOut);
					out.writeObject(input);
					out.close();
					fileOut.close();
				} catch (IOException i) {
					i.printStackTrace();
				}
				if (flag == 0)
					textArea.append("please enter correct username\n");
				try {
					if (flag == 2) {
						// System.out.println(3);
						client.sndMsg();
					}
					if (flag == 0) {
						input = textField.getText();
						if (isValid(input, 4)) {
							client.start(input);
							flag++;
							try {

								FileOutputStream fileOut = new FileOutputStream(new File("msg.txt"));
								ObjectOutputStream out = new ObjectOutputStream(fileOut);

								out.writeObject(input);

								out.close();
								fileOut.close();
							} catch (IOException i) {
							}
						}
						if (flag == 1) {
							client.rcvMsg();
							client.messageReading.start();
							flag++;
							System.out.println(flag);
						}

					}
					textField.setText("");
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}

		});
		btnNewButton.setBounds(606, 661, 125, 72);
		frame.getContentPane().add(btnNewButton);
		frame.setVisible(true);

	}
}