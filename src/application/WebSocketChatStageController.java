package application;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Base64;
import javax.websocket.*;
import org.apache.commons.io.FileUtils;
import javafx.scene.control.MenuItem;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;


public class WebSocketChatStageController {
	@FXML TextField usernameText;
	@FXML TextField messageText;
	@FXML TextArea chatText;
	@FXML Button userSetBtn;
	@FXML Button attachSendBtn;
	@FXML Button sendMesBtn;
	@FXML ListView<String> attachView;
	@FXML MenuItem saveOption;
	
	private String username;
	private WebSocketClient webSocketClient;
	private ArrayList<Message> attachements;
	
	@FXML 
	private void initialize() {
		webSocketClient = new WebSocketClient();
		username = usernameText.getText();
		attachements = new ArrayList<>();
	}
	
	@FXML void saveOption_Click() {
		int index = attachView.getSelectionModel().getSelectedIndex();
        
        if(index == -1) return;
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName(attachements.get(index).getTextMessage());
	   	File file = fileChooser.showSaveDialog(null);
	   	if(file != null) {
			try {
				FileOutputStream fos = new FileOutputStream(file);
				byte[] binData = org.apache.commons.codec.binary.Base64.decodeBase64(attachements.get(index).getAttachMessage());
				fos.write(binData);
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	   	}
	}
	
	@FXML 
	private void userSetBtn_Click() {
		if(usernameText.getText().isEmpty()) return;
		username = usernameText.getText();
		Message msg = new Message(Message.Type.BINARY);
		Message msg1 = new Message(Message.Type.TEXT);
		
		System.out.println(msg.getType() + " " + msg1.getType());
	}
	
	@FXML 
	private void sendMesBtn_Click() throws IOException, ClassNotFoundException {
		System.out.println(messageText.getText());
		Message msg = new Message(Message.Type.TEXT);
		
		
		msg.setUsername(username);
		msg.setMessage(messageText.getText());
		
		String str = EncodeAndSerialize(msg);
		
		webSocketClient.sendMessage(str);
		messageText.clear();
	}
	
	@FXML
	private void attachSendBtn_Click() throws IOException {
		FileChooser fileChooser = new FileChooser();
		File file = fileChooser.showOpenDialog(null);		
		if(file != null) {
			byte[] encoded = org.apache.commons.codec.binary.Base64.encodeBase64(FileUtils.readFileToByteArray(file));
			Message msg = new Message(Message.Type.BINARY);
			msg.setUsername(username);
			msg.setMessage(file.getName());
			msg.setAttachMessage(new String(encoded));
		
			String str = EncodeAndSerialize(msg);		
			webSocketClient.sendMessage(str);
		}		
	}
		
	
    private String EncodeAndSerialize( Serializable msg_ ) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream( baos );
        oos.writeObject(msg_);
        oos.close();
        return Base64.getEncoder().encodeToString(baos.toByteArray()); 
    }
	
    private Object DeserializeAndDecode(String str_) throws IOException, ClassNotFoundException {
    	byte [] data = Base64.getDecoder().decode(str_);
    	ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
    	Object o  = ois.readObject();
    	ois.close();
    	return o;
    }
    
	private void updateChat(String str_) throws ClassNotFoundException, IOException {
		Message msg_ = (Message)DeserializeAndDecode(str_);
		if(msg_.getType().equals("TEXT")) {
			chatText.setText(chatText.getText() + msg_.getUsername() + ": " + msg_.getTextMessage() + "\n");
		}
		else if(msg_.getType().equals("BINARY")) {
			attachements.add(msg_);
			attachView.getItems().add(msg_.getUsername() + ": " + msg_.getTextMessage());
		}
			
				
	}
	public void closeSession(CloseReason closeReason) {
		try {
			webSocketClient.session.close(closeReason);
		}
		catch (IOException e) { e.printStackTrace(); }
	}
	
	@ClientEndpoint
	public class WebSocketClient {
		private Session session;
		public WebSocketClient() { connectToWebSocket(); }
		
		@OnOpen 
		public void onOpen(Session session) {
			System.out.println("Connection opened");
			this.session = session;
		}
		@OnClose 
		public void onClose(CloseReason closeReason) {
			System.out.println("Connection closed: " + closeReason.getReasonPhrase());
		}
		@OnMessage 
		public void onMessage(String message, Session session) throws ClassNotFoundException, IOException {
			System.out.println("Message received: "+ message);
			updateChat(message);
			
		}				
			
		private void connectToWebSocket() {
			WebSocketContainer webSocketContainer = ContainerProvider.getWebSocketContainer();
			try {
				URI uri = URI.create("ws://localhost:8080/WebSocketEndpoint/websocketendpoint");
				webSocketContainer.connectToServer(this, uri);
			} catch (DeploymentException | IOException e) { e.printStackTrace(); }
		}
		
		public void sendMessage(String message) {
			try {
				System.out.println("Message sent: " + message);
				session.getBasicRemote().sendText(message);
			}
			catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
}
