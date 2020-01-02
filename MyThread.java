//Designed by Xudong Ma 2/09/2018
//A thread class

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class MyThread extends Thread {

	private Socket client;
	private BufferedReader input;
	private BufferedWriter out;
	
	public MyThread(Socket client) {
		this.client = client;
		try {
			input = new BufferedReader(new InputStreamReader(client.getInputStream()));
			out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
		} catch (IOException e) {
			System.out.println("Cannot get the client's input or output stream.");
		}

	}

	public void run() {
		try {
			JSONParser parser = new JSONParser();
			JSONObject word = null;
			String inputString = null;
			

			while ( (inputString = input.readLine())!= null) {
				word = (JSONObject) parser.parse(inputString);
				String command = (String) word.get("command");
				switch (command) {
				// search word
				case "search":
					searchWord(word);
					break;
				// remove word
				case "remove":
					removeWord(word);
					break;
				// add word
				case "add":
					addWord(word);
				} 
				
				//After every communication, check if the word collection is changed
				//If changed, store the change to disk, then reset the change State
				if(DictionaryServer.getIfChanged()) {
					
					DictionaryServer.updateDatabase();
					DictionaryServer.resetIfChanged();
				}
			}
			
			//After the client exits, close the socket and stream, then exit this thread
			input.close();
			out.close();
			System.out.println("Client offline");
			client.close();

		} catch (NullPointerException e) {
			System.out.println("Sorry, the client is offline");

		} catch(ParseException e) {
			try {
				JSONObject message = new JSONObject();
				message.put("message", "Sorry, your request is unmeaningful");
				out.write(message.toJSONString() + "\n");
				out.flush();
				System.out.println("Sorry, the client gives unmeaningful request");
			} catch(IOException e1) {
				System.out.println("Sorry, the client offline");
			}
		
		} catch (IOException e) {
			System.out.println("Sorry, something wrong happened inside the io stream!!!");
		}

	}


	private void searchWord(JSONObject word) throws IOException {
		
		String pureWord = (String) word.get("word");
		System.out.println("Receive search demand: " + pureWord);
		JSONObject result = DictionaryServer.search(pureWord);
		if (result == null) {
			System.out.println("Search fail, word not in dictionary:" + pureWord);
			
			JSONObject message = new JSONObject();
			message.put("message", "Sorry, \"" + pureWord + "\" is not in the dictionary");
			out.write(message.toJSONString() + "\n");
			out.flush();
		} else {
			System.out.println("Succeed to search word: " + pureWord );
			out.write(result.toJSONString() + "\n");
			out.flush();
		}
	}

	private void removeWord(JSONObject word) throws IOException {
		
		System.out.println("Receive remove word demand:" + word.get("word"));
		String pureWord = (String) word.get("word");
		boolean result = DictionaryServer.removeWord(pureWord);
		if (result == false) {
			System.out.println("Remove fail, word not in the dictionary: " + pureWord);
			JSONObject message = new JSONObject();
			message.put("message", "Sorry, \"" + pureWord + "\" is not in the dictionary");
			out.write(message.toJSONString() + "\n");
			out.flush();
		} else {
			System.out.println("Remove succeed: " + pureWord);
			JSONObject message = new JSONObject();
			message.put("message", "\"" + pureWord + "\" has been deleted");
			out.write(message.toJSONString() + "\n");
			out.flush();
		}
	}

	private void addWord(JSONObject word) throws IOException, NullPointerException {
		
			word.remove("command");
			System.out.println("Receive add word demand:" + word.get("word") + ": " + word.get("meanings"));
			

			boolean result = DictionaryServer.addWord(word);
			String pureWord = (String) word.get("word");
			if (!result) {	
				System.out.println("Fail to add word:" + pureWord);
				JSONObject message = new JSONObject();
				message.put("message", "\"" + pureWord + "\" has existed in the dictionary");
				out.write(message.toJSONString() + "\n");
				out.flush();
			} else {
				System.out.println("Succeed to add word:" + pureWord);
				JSONObject message = new JSONObject();
				message.put("message", "\"" + pureWord + "\" has been added to the dictionary");
				out.write(message.toJSONString() + "\n");
				out.flush();
			}	

	}

}
