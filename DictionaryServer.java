//Designed by Xudong Ma 2/09/2018
//The server main code

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class DictionaryServer {
	private static int port;
	private static String path;
	private static JSONArray words;
	private static ArrayList<String> searchWords = new ArrayList<String>(); // used for search
	private static boolean ifChanged = false; //to store if there are words remove or add.

	public static void main(String[] args) {
		try {
			if (args.length != 2) {
				throw new WrongNumberOfArgumentsException();
			} else {
				port = Integer.parseInt(args[0]);
				path = args[1];
				startTheServer();
				ServerSocket server = new ServerSocket(port);

				System.out.println("Server start, waiting for client....");
				Socket client = null;
				while (true) {
					client = server.accept();
					System.out.println("client coming");
					
					//per client per thread
					Thread t = new Thread(new MyThread(client));
					t.start();
				}
			}
		} catch (WrongNumberOfArgumentsException e1) {
			JOptionPane.showMessageDialog(null, "Sorry, please input two arguments: port filePath");
			System.exit(1);
		} catch (NumberFormatException e1) {
			JOptionPane.showMessageDialog(null, "Sorry, please input a int number as the port number");
			System.exit(1);
		} catch (FileNotFoundException e2) {
			JOptionPane.showMessageDialog(null, "Sorry, cannot find the file following the path you provided");
			System.exit(1);
		} catch (ParseException e3) {
			JOptionPane.showMessageDialog(null, "Sorry, the file you provided doesn't fit the format needed");
			System.exit(1);
		} catch (IOException e4) {
			JOptionPane.showMessageDialog(null, "Sorry, the port is unavailable");
			System.exit(1);
		}
	}
	
	public static boolean getIfChanged() {
		return ifChanged;
	}
	
	//After write the change of the word collection to the disk, reset the value of ifChanged to false
	public static void resetIfChanged() {
		ifChanged = false;
	}

	//provide synchronized operation to remove word
	public static synchronized boolean removeWord(String word) {
		
		if (!searchWords.contains(word)) {
			return false;
		} else {
			int wordIndex = searchWords.indexOf(word);
			words.remove(wordIndex);
			searchWords.remove(wordIndex);
			ifChanged = true;
			return true;
		}

	}
	
	//provide synchronized operation to add word
	public static synchronized boolean addWord(JSONObject word) {

		String pureWord = (String) word.get("word");
		if (searchWords.contains(pureWord)) {
			return false;
		} else {
			words.add(word);
			searchWords.add(pureWord);
			ifChanged = true;
			return true;
		}
	}

	public static JSONObject search(String word) {
		word = word.toLowerCase();
		//long a = System.currentTimeMillis();
		int index = searchWords.indexOf(word);
		if (index == -1) {
			return null;
		} else {
			JSONObject result = (JSONObject) words.get(index);
			//long b = System.currentTimeMillis();
			//System.out.println("total cost = " + (b - a));
			return result;
		}
	}

	//provide synchronized operation to undate the database to the disk
	public static synchronized void updateDatabase(){
		try {
			FileWriter dictionary = new FileWriter(path);
			dictionary.write(words.toJSONString());
			dictionary.flush();
			dictionary.close();	
		} catch(IOException e) {
			System.out.println("Sorry, cannot store the changes to the disk.");
		}
	}

	// a method to load the words in the dictionary so that turn on the server
	private static void startTheServer() throws IOException, ParseException{
		
			FileReader dictionary = new FileReader(path);
			JSONParser parser = new JSONParser();
			JSONArray words = (JSONArray) parser.parse(dictionary);
			DictionaryServer.words = words;
			for (int i = 0; i < words.size(); i++) {
				JSONObject word = (JSONObject) words.get(i);
				searchWords.add((String) word.get("word"));
			}
		
	}
}
