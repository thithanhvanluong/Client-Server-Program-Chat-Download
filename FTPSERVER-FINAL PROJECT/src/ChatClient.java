


import java.awt.BorderLayout;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileSystemView;

/**
 * This class overrides some of the methods defined in the abstract
 * superclass in order to give more functionality to the client.
 */
public class ChatClient extends AbstractClient//ClientConsole uses ChatClient to connect with the server
{
  //Instance variables **********************************************
  
  /**
   * The interface type variable.  It allows the implementation of 
   * the display method in the client.
   */
  ChatIF clientUI; 
  GUIConsole guiConsole;
  public File fileObjectToSend = null; //file object that is chosen to be sent
  public String fileSend; //name of file to send
  private FileReader reader;//read the file
  private byte[] fileContent;  
  String folderDownload = "C:\\Comp258FinalProject\\downloads\\"; //specify the downloads folder which was manually created

  
  

  
  //Constructors ****************************************************
  
  /**
   * Constructs an instance of the chat client.
   *
   * @param host The server to connect to.
   * @param port The port number to connect on.
   * @param clientUI The interface type variable.
   */
  
  public ChatClient(String host, int port, ChatIF clientUI) throws IOException 
  {
    super(host, port); //Call the superclass constructor
    this.clientUI = clientUI;
    openConnection();//method in AbstractClient class
    
  }

  
  //Instance methods ************************************************
    
  /**
   * This method handles all data that comes in from the server.
   *
   * @param msg The message from the server.
   */
  public void handleMessageFromServer(Object msg) 
  {
      if(msg instanceof Envelope)
      {
          Envelope env = (Envelope)msg;
          handleCommandFromServer(env);
      }
      else
      {
         clientUI.display(msg.toString()); 
      }
    
  }
  
  public void handleCommandFromServer(Envelope env)
  {
      if(env.getId().equals("who"))
      {
          ArrayList<String> userList = (ArrayList<String>)env.getContents();
          String room = env.getArgs();
          clientUI.display("Users in "+ room+": ");
          for(String s: userList)
          {
              clientUI.display(s);
          }
      }
      
      if(env.getId().equals("fileList")){
             ArrayList<String> fileList = (ArrayList<String>) env.getContents();
             
            if(clientUI instanceof GUIConsole){
                ((GUIConsole) clientUI).displayFileList(fileList);
            }
        }
      
      if(env.getId().equals("downloadedFile")){
            String filename = env.getArgs();
            
            byte[] bytes = (byte[]) env.getContents();

            clientUI.display("The file: " + filename + " has been downloaded" + "\n");
            
            InputStream inputStream = new ByteArrayInputStream(bytes);
            File file = new File(folderDownload + filename);
            try {
                Files.copy(inputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                System.out.println("Failed to download the file.");
            }            
        }
  }

  /**
   * This method handles all data coming from the UI            
   *
   * @param message The message from the UI.    
   */
    public void handleMessageFromClientUI(String message) 
    {

        if (message.charAt(0) == '#') 
        {

            handleClientCommand(message);

        }
        else 
        {
            try {
                sendToServer(message);//chat message comes to the server--> the server calls method to SEND TO ALL CLIENTS
                //--> echo back to all clients
            } catch (IOException e) {
                clientUI.display("Could not send message to server.  Terminating client.......");
                quit();
            }
        }
    }
  
  /**
   * This method terminates the client.
   */
    public void quit() 
    {
        try 
        {
            closeConnection();
        } 
        catch (IOException e)
        {
            
        }
        System.exit(0);
    }
  
    public void connectionClosed() {

        System.out.println("Connection closed");

    }
	
  		
    public void handleClientCommand(String message) 
    {

        if (message.equals("#quit")) {
            clientUI.display("Shutting Down Client");
            quit(); //call method above

        }

        if (message.equals("#logoff")) 
        {
            clientUI.display("Disconnecting from server");
            try 
            {
                closeConnection(); //close connection but NOT shut down client
            } 
            catch (IOException e) 
            {
            };

        }

        if (message.indexOf("#setHost") >= 0) {

            if (isConnected()) { //cannot setHost & setPort khi dang connect. Muon set thi phai LOGOFF truoc
                clientUI.display("Cannot change host while connected");
            } else {
                setHost(message.substring(8, message.length()).trim());
            }

        }

        if (message.indexOf("#setPort") >= 0) { // >=0 vi setPort co the o dau chuoi
            //#setPort 5556
            if (isConnected()) { //cannot setHost & setPort khi dang connect
                clientUI.display("Cannot change port while connected");
            } else {
                setPort(Integer.parseInt(message.substring(8, message.length()).trim()));
                //message.substring(8, message.length()).trim() : cat chuoi setPort de lay gia tri 5556
                //Phương thức trim() được sử dụng để xóa khoảng trẳng ở đầu và cuối chuỗi
            }

        }

        if (message.indexOf("#login") == 0) {
            //#login Nick
            if (isConnected()) {
                clientUI.display("already connected");
            } else {

                try {
                    String userName = message.substring(6, message.length()).trim();
                    openConnection();
                    clientUI.display((">>>>> Logging in as: "+ userName));
                    Envelope env = new Envelope("login", "", userName);
                    this.sendToServer(env);
                } catch (IOException e) {
                    clientUI.display("failed to connect to server.");
                }
            }
        }
        if(message.indexOf("#chooseFile") == 0)
        {
            //point to the folder
            JFileChooser fileChooserObject = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());

            //allow users to choose FILES/DIRECTORIES
            fileChooserObject.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

            //show notification on the console after the user choose his files/directories
            int returnVal = fileChooserObject.showOpenDialog(null);

            if (returnVal == 0) {
                fileObjectToSend = fileChooserObject.getSelectedFile();
            }
            fileSend = fileObjectToSend.getName() + " ";//get the name of the chosen file

            //create a frame
            JFrame frame = new JFrame("Text Contents");
            frame.setVisible(true);//set the frame visible
            frame.setSize(300, 200);

            //JTextArea is a component allowing displaying multiple line and the user can edit the text.
            JTextArea content = new JTextArea();
            //JScrollPane create a scroll when row of text larger than row of
            // JTextArea

            //new JScrollPane(fileContents): bring content to 
            frame.getContentPane().add(new JScrollPane(content), BorderLayout.CENTER);
            try {
                reader = new FileReader(fileObjectToSend);
                BufferedReader br = new BufferedReader(reader);//read file
                content.read(br, fileObjectToSend);

                br.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

    } 

        
        if(message.indexOf("#ftpUpload") == 0)
        {
            
            try {
                //CONVERT file to bytes[]
                //fileObjectToSend.toPath(): get the Path of the chosen file
                //fileContent: array of bytes ( after converting files to bytes)
                fileContent = Files.readAllBytes(fileObjectToSend.toPath());

            } catch (IOException e) {
                e.printStackTrace();
            }
                    
            Envelope env = new Envelope("upload", fileSend, fileContent);
            try {
                this.sendToServer(env);
            } catch (IOException ex) {
                Logger.getLogger(ChatClient.class.getName()).log(Level.SEVERE, null, ex);
            }
         
        }
        
        if(message.contains("#ftpList")){
            try{
                Envelope env = new Envelope("fileList", "", "");
                this.sendToServer(env);
            }catch(IOException ioe){
                clientUI.display("Failed to acquire the file list.");
            }
        }
        
        if(message.contains("#ftpGet")){
            try{
                String filename = message.substring(7, message.length()).trim();
                
                Envelope env = new Envelope("download", "", filename);
                this.sendToServer(env);
            }catch(IOException ioe){
                clientUI.display("Failed to download the file.");
            }
        }
        
        
        

        if (message.indexOf("#join") == 0) {
            //#join roomName
            //have a string that we cut up
            //we create envelope
            //send envelope

            try {
                String roomName = message.substring(5, message.length()).trim();
                //create an envelope
                Envelope env = new Envelope("join", "", roomName);
                this.sendToServer(env);
            } catch (IOException e) {
                clientUI.display("Failed to join a room");
            }
        }

        if (message.indexOf("#pm") == 0) {
            try {
                //#pm Bob hello Bob
                String targetAndMessage = message.substring(3, message.length()).trim();
                //Bob hello Bob
                String target = targetAndMessage.substring(0, targetAndMessage.indexOf(" ")).trim();//lay duoc ten ng dung
                //lay message
                String pm = targetAndMessage.substring(targetAndMessage.indexOf(" "), targetAndMessage.length()).trim();
                Envelope env = new Envelope("pm", target, pm);
                
                this.sendToServer(env);

            } catch (IOException e) {
                clientUI.display("Target can't be found");
            }

        }

        if (message.indexOf("#yell") == 0) {
            try {
                String yellMessage = message.substring(5, message.length()).trim();
                Envelope env = new Envelope("yell", "", yellMessage);
                this.sendToServer(env);

            } catch (IOException e) {
                clientUI.display("Failed to yell");
            }
        }

        if (message.equals("#who")) {
            try {
                Envelope env = new Envelope("who", "", "");
                this.sendToServer(env);
            } catch (IOException io) {
                clientUI.display("Failed to acquire user list");
            }
        }

    }

        
    protected void connectionException(Exception exception) { //if the user shut down the EchoServer,this method run
    // it is the override method from AbstractCLient class
    //override the hook method clietnException from AbstractServer,
    //when the proxy object knows problem connection from Client(not Server)
        System.out.println("Server has shutdown");
    }
  
}
//End of ChatClient class
