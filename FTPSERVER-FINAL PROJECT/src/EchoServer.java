import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

public class EchoServer extends AbstractServer {
       
    String folderUpload = "C:\\Comp258FinalProject\\uploads\\"; //specify the downloads folder which was manually created
    ChatIF serverUI;
    
    //Constructors ****************************************************
    /**
     * Constructs an instance of the echo server.
     *
     * @param port The port number to connect on.
     */
    public EchoServer(int port) {

        super(port);
        
        this.serverUI = serverUI;       
    }
    
    //Instance methods ************************************************
    /**
     * This method handles any messages received from the client.
     *
     * @param msg The message received from the client.
     * @param client The connection from which the message originated.<-> the
     * connection connected to the client that sent the message. //to know who
     * sends this message
     */
    public void handleMessageFromClient(Object msg, ConnectionToClient client) {
        if (msg instanceof Envelope) {
            Envelope env = (Envelope) msg;
            handleCommandFromClient(env, client);

        } 
        else {
            System.out.println("Message received: " + msg + " from " + client);
            String userId;
            if (client.getInfo("userid") == null) 
            {
                userId = "guest";
                client.setInfo("userid", userId);
                client.setInfo("room", "lobby");
            } 
            else 
            {
                userId = client.getInfo("userid").toString(); //client.getInfo() trả về Object
            }
            this.sendToAllClientsInRoom(userId + " " + msg, client);
        }
    }

    public void handleCommandFromClient(Envelope env, ConnectionToClient client) {
        if (env.getId().equals("login")) { //command is: login
            String userId = env.getContents().toString();
            client.setInfo("userid", userId);
            client.setInfo("room", "lobby");
        }
        if (env.getId().equals("join")) {
            //setinfo "room" roomName
            String roomName = env.getContents().toString(); //getCOntents --> lay duoc roomName tu message
            client.setInfo("room", roomName);
        }
        if (env.getId().equals("pm")) {
            String target = env.getArgs();
            String message = env.getContents().toString();
            sendToAClient(message, target, client);
        }
        if (env.getId().equals("yell")) {
            String message = env.getContents().toString();
            String userId = client.getInfo("userid").toString();
            this.sendToAllClients(userId + "yells: " + message);
        }
        if (env.getId().equals("who")) {
            this.sendRoomListToClient(client);
        }
        if(env.getId().equals("upload"))
        {
            byte[] bytes = (byte[]) env.getContents();

            File outputFile = new File(folderUpload + env.getArgs());

            try (FileOutputStream outputStream = new FileOutputStream(outputFile);) {

                outputStream.write(bytes);  // Write the bytes and you're done.

            } catch (Exception e) {
                e.printStackTrace();
        }     
        }
        
        if(env.getId().equals("fileList")){
            sendFileListToClient(client);
        }
        
        if(env.getId().equals("download")){
            String filename = env.getContents().toString();
            sendFileToClient(client, filename);
        }  
    }
    
    public void sendFileToClient(ConnectionToClient client, String filename){
        Envelope env = new Envelope();
        env.setId("downloadedFile"); 
        env.setArgs(filename);
        
        File folder = new File(folderUpload);       
        File[] files = folder.listFiles();
        
        for(File file: files){
            if(file.getName().equals(filename)){
                byte[] bytes = new byte[(int) file.length()];

                try ( FileInputStream fis = new FileInputStream(file)) {
                    fis.read(bytes);
                } catch (IOException ioe) {
                    System.out.println("Cannot do the conversion.");
                }
                
                env.setContents(bytes);
                
                break;
            }           
        }
        
        try{
            client.sendToClient(env);
        } catch (Exception e) {
            System.out.println("Failed to send the file to client.");
        }
    }
    
    public void sendFileListToClient(ConnectionToClient client){
        Envelope env = new Envelope();
        env.setId("fileList");
        
        ArrayList<String> fileNames = new ArrayList<String>();
        
        File folder = new File(folderUpload);
        
        File[] files = folder.listFiles();
        
        for(File file: files){
            if(file.isFile()){
                fileNames.add(file.getName());
            }
        }  
        
        env.setContents(fileNames);
        
        try {
            client.sendToClient(env);
        } catch (Exception e) {
            System.out.println("Failed to send FileList to client.");
        }
        
    }

    public void sendRoomListToClient(ConnectionToClient client) {
        Envelope env = new Envelope();
        env.setId("who");
        ArrayList<String> userList = new ArrayList<String>();
        String room = client.getInfo("room").toString();
        env.setArgs(room);

        Thread[] clientThreadList = getClientConnections();
        for (int i = 0; i < clientThreadList.length; i++) {
            ConnectionToClient target = (ConnectionToClient) clientThreadList[i];
            if (target.getInfo("room").equals(room)) {
                userList.add(target.getInfo("userid").toString());
            }
        }
        env.setContents(userList);
        try {
            client.sendToClient(env);
        } catch (Exception ex) {
            System.out.println("Failed to send UserList to client");
        }
    }

    public void sendToAClient(Object msg, String pmTarget, ConnectionToClient client) {
        Thread[] clientThreadList = getClientConnections();
        for (int i = 0; i < clientThreadList.length; i++) {

            ConnectionToClient target = (ConnectionToClient) clientThreadList[i];
            if (target.getInfo("userid").equals(pmTarget)) {
                try {
                    target.sendToClient(client.getInfo("userid") + ": " + msg);
                } catch (Exception ex) {
                    System.out.println("Failed to send private message");
                }
            }
        }
    }

    public void sendToAllClientsInRoom(Object msg, ConnectionToClient client) {
        //getClientConnections(): Returns an array containing the existing client connections
        Thread[] clientThreadList = getClientConnections();
        String room = client.getInfo("room").toString();

        for (int i = 0; i < clientThreadList.length; i++) {
            ConnectionToClient target = (ConnectionToClient) clientThreadList[i];
            if (target.getInfo("room").equals(room)) { //check if clients are in the same room
                try {
                    target.sendToClient(msg);
                } catch (Exception ex) {
                    System.out.println("Failed to send to client");
                }
            }
        }
    }

   
    public void handleCommandFromServerConsole(String message) throws IOException {
        if (message.indexOf("#setPort") >= 0) {
            setPort(Integer.parseInt(message.substring(8, message.length()).trim()));

        }
        if (message.equals("#start")) {
            try {
                listen();
                System.out.println(">>>>>>> Server listening for connections on port " + getPort());
            } catch (Exception ex) {
                System.out.println("ERROR");
            }

        }
        if (message.indexOf("#ison") >= 0) 
        {
            //getClientConnections(): Returns an array containing the existing client connections
            Thread[] clientThreadList = getClientConnections();
            
            //trim the space in the message to get userName
            String userName = message.substring(5, message.length()).trim();

            for (int i = 0; i < clientThreadList.length; i++) {
                ConnectionToClient targetCheck = (ConnectionToClient) clientThreadList[i];
                if (targetCheck.getInfo("userid").equals(userName)) {
                    String room = targetCheck.getInfo("room").toString();
                    
                    System.out.println(">>>>>"+ userName + " is on in the room " + room);
                   
                }
            }

        }
        if (message.equals("#quit")) {

            System.exit(0);

        }
        if (message.equals("#userstatus")) {
            //getClientConnections(): Returns an array containing the existing client connections
            Thread[] clientThreadList = getClientConnections();
            for (int i = 0; i < clientThreadList.length; i++) {
                ConnectionToClient targetCheck = (ConnectionToClient) clientThreadList[i];
                
                String nameUser = targetCheck.getInfo("userid").toString();//get name info
                String room = targetCheck.getInfo("room").toString();//get room info
                
                System.out.println(">>>>>>>"+ nameUser + ": room "+ room);
            }
        }
        if(message.indexOf("#joinroom") >= 0)
        {
            //getClientConnections(): Returns an array containing the existing client connections
            Thread[] clientThreadList = getClientConnections();
            
            //get the whole 2 rooms after "#joinroom"
            String totalRoom = message.substring(9, message.length()).trim(); 
            
            //room1 name
            String room1 = totalRoom.substring(0, totalRoom.indexOf(" ")).trim();
            int index = totalRoom.indexOf(" ");
            
            //room2 name
            String room2  = totalRoom.substring(index, totalRoom.length()).trim();
            
            for (int i = 0; i < clientThreadList.length; i++) 
            {
                ConnectionToClient targetCheck = (ConnectionToClient) clientThreadList[i];
                //get info of the room for each client
                String room = targetCheck.getInfo("room").toString();
                
                if(room.equals(room1)) //everyone from room1
                {
                    targetCheck.setInfo("room", room2);//change into room2
                    System.out.println(">>>>>>>CHANGE ROOM SUCCESSFULLY");
                }
                
            }
            
        }
       
        if (message.equals("#stop")) {
            System.out.println("Server has stopped listening for connections.");
            close();
        }       
    }
    
    //Hook method called each time a new client connection is accepted.
    protected void clientConnected(ConnectionToClient client) {
        System.out.println("<Client Connected:" + client + ">");
    }

    synchronized protected void clientException( //override the hook method clietnException from AbstractServer,
            //when the proxy object knows problem connection from Server(not Client)
            ConnectionToClient client, Throwable exception) {
        System.out.println("Client Shut down");
    }
}
//End of EchoServer class
