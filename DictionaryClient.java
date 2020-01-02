//Designed by XudongMa at 2/09/2018
import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class DictionaryClient {
	private static String serverAddress;
	private static int port;
	private static Socket client;
	private static ClientGUI window;
	
	public static void main(String[] args) {
		try {
			//check if there are right numbers of command line parameters 
			if (args.length != 2) {
				throw new WrongNumberOfArgumentsException();
			} else {
				serverAddress = args[0];
				port = Integer.parseInt(args[1]);
				client = new Socket(serverAddress, port);
				// start GUI
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						try {
							window = new ClientGUI(client);

						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
			}

		} catch (UnknownHostException e) {
			JOptionPane.showMessageDialog(null, "Sorry, can't find the host");
			System.exit(1);
		} catch (WrongNumberOfArgumentsException e) {
			JOptionPane.showMessageDialog(null, "Sorry, please input two arguments: serverIP port");
			System.exit(1);
		} catch (NumberFormatException e1) {
			JOptionPane.showMessageDialog(null, "Sorry, please input a int number as the port number");
			System.exit(1);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Sorry, fail to access the server, please check the network "
					+ "connection or if the server is online.");
			System.exit(1);
		}
	}
}
