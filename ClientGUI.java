
//Designed by Xudong Ma at 02/09/2018
import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JTextField;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.awt.BorderLayout;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;
import java.awt.event.ActionEvent;
import javax.swing.JTextArea;
import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JPanel;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.swing.JScrollPane;
import java.awt.CardLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

public class ClientGUI {

	private JFrame frame;
	private JTextField textField;
	BufferedReader input = null;
	BufferedWriter out = null;
	private JButton btnAddWord;
	private JButton btnNewButton_1;

	/**
	 * Create the application.
	 */
	public ClientGUI(Socket client) {
		try {
			initialize(client);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Sorry, the server offline");
		}
	}

	/**
	 * Initialize the contents of the frame.
	 * 
	 * @throws IOException
	 */
	private void initialize(Socket client) throws IOException {

		input = new BufferedReader(new InputStreamReader(client.getInputStream()));
		out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));

		frame = new JFrame();
		frame.getContentPane().setForeground(Color.RED);
		frame.setBounds(100, 100, 500, 400);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.frame.setVisible(true);
		frame.getContentPane().setLayout(null);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 105, 480, 255);
		frame.getContentPane().add(scrollPane);

		JTextArea textArea = new JTextArea();
		scrollPane.setViewportView(textArea);

		JPanel panel_2 = new JPanel();
		panel_2.setBounds(10, 55, 480, 40);
		frame.getContentPane().add(panel_2);
		panel_2.setLayout(new GridLayout(0, 1, 0, 0));

		textField = new JTextField();
		panel_2.add(textField);
		textField.setColumns(10);

		JPanel panel_1 = new JPanel();
		panel_1.setBounds(10, 10, 480, 40);
		frame.getContentPane().add(panel_1);
		panel_1.setLayout(new GridLayout(0, 3, 0, 0));

		btnAddWord = new JButton("Add Word");
		panel_1.add(btnAddWord);
		btnAddWord.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					textArea.setText("");
					String inputString = textField.getText();
					String[] wordWithMeanings = inputString.split("\"");
					if(wordWithMeanings.length == 1) {
						textArea.append("Sorry, please separate the word and different meanings using \"\".\n");
					}
					else if (wordWithMeanings.length < 2) {
						textArea.append("Sorry, you cannot add word without any meaning.\n");
					} else {

						JSONObject word = new JSONObject();
						word.put("command", "add");
						word.put("word", wordWithMeanings[1]);
						JSONArray meanings = new JSONArray();
						int meaningsCount = 1;
						for (int i = 2; i < wordWithMeanings.length; i++) {
							if (!wordWithMeanings[i].matches("\\s*")) {
								JSONObject meaning = new JSONObject();
								meaning.put("meaning", meaningsCount + ". " + wordWithMeanings[i]);
								meanings.add(meaning);
								meaningsCount++;
							}
						}
						if (meanings.size() < 1) {
							textArea.append("Sorry, you cannot add word without any meaning.\n");
						} else {
							word.put("meanings", meanings);

							out.write(word.toJSONString() + "\n");
							out.flush();

							JSONParser parser = new JSONParser();
							JSONObject result;
							result = (JSONObject) parser.parse(input.readLine());
							textArea.append(result.get("message") + ":\n");
						}
					}

				} catch (SocketException e1) {
					JOptionPane.showMessageDialog(null, "Sorry, the server is offline");
					System.exit(0);
				} catch (ParseException e2) {
					textArea.setText("");
					textArea.append("Sorry, server offers something unmeaningful.\n");
				} catch (IOException e3) {
					// TODO Auto-generated catch block
					e3.printStackTrace();
				}

			}
		});

		JButton btnNewButton = new JButton("Search");
		panel_1.add(btnNewButton);
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					textArea.setText("");
					String inputString = textField.getText();
					JSONObject searchWord = new JSONObject();
					searchWord.put("command", "search");
					searchWord.put("word", inputString);

					out.write(searchWord.toJSONString() + "\n");
					out.flush();

					// parser the json objcet getting from server;
					JSONParser parser = new JSONParser();
					JSONObject result = (JSONObject) parser.parse(input.readLine());
					if (result.get("message") == null) {
						textArea.append(result.get("word") + ":\n");
						JSONArray meanings = (JSONArray) result.get("meanings");
						for (int i = 0; i < meanings.size(); i++) {
							JSONObject meaning = (JSONObject) meanings.get(i);
							textArea.append(meaning.get("meaning") + "\n");
						}
					} else {
						textArea.append(result.get("message") + "\n");
					}
				} catch (SocketException e1) {
					JOptionPane.showMessageDialog(null, "Sorry, the server is offline");
					System.exit(0);
				} catch (ParseException e2) {
					textArea.setText("");
					textArea.append("Sorry, server offeres something unmeaningful.\n");
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			}
		});

		btnNewButton_1 = new JButton("Remove Word");
		panel_1.add(btnNewButton_1);

		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					textArea.setText("");
					String inputString = textField.getText();
					JSONObject removeWord = new JSONObject();
					removeWord.put("command", "remove");
					removeWord.put("word", inputString);

					out.write(removeWord.toJSONString() + "\n");
					out.flush();

					// receive the result from the server and display it in the textArea
					JSONParser parser = new JSONParser();
					JSONObject result;
					result = (JSONObject) parser.parse(input.readLine());
					textArea.append(result.get("message") + ":\n");
				} catch (SocketException e1) {
					JOptionPane.showMessageDialog(null, "Sorry, the server is offline");
					System.exit(0);
				} catch (ParseException e2) {
					textArea.setText("");
					textArea.append("Sorry, server offeres something unmeaningful.\n");
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			}
		});

	}
}
