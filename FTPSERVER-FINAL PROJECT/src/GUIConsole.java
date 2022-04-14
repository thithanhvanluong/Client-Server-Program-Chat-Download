
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.filechooser.FileSystemView;


public class GUIConsole extends JFrame implements ChatIF{
    final public static int DEFAULT_PORT = 5555;
    
   
    
    ChatClient client;
  
    private JButton logInB = new JButton("Login");
    private JButton logOffB = new JButton("Logoff");
    private JButton browseB = new JButton("Browse");
    private JButton sendB = new JButton("Send");
    private JButton saveB = new JButton("Save");  
    private JButton downB = new JButton("Download");
    private JButton listB = new JButton("File List");
    private JButton downloadB = new JButton("Download");
    private JButton quitB = new JButton("Quit");
    
    //textfield is next to label to know which textfield is for
    public JTextField portTxF = new JTextField("5555");
    public JTextField hostTxF = new JTextField("127.0.0.1");
    public JTextField messageTxF = new JTextField("");
    public JTextField userTxF = new JTextField("");
    
    //combo box for file list
    private JComboBox listCB = new JComboBox();
    
    //labels
    private JLabel portLB = new JLabel("Port: ", JLabel.RIGHT);
    private JLabel hostLB = new JLabel("Host: ", JLabel.RIGHT);
    private JLabel messageLB = new JLabel("Message: ", JLabel.RIGHT);
    private JLabel userLB = new JLabel("userId: ", JLabel.RIGHT);
    
    //where the chat messages will display
    //Lớp JTextArea tạo một ô gõ văn bản
    private JTextArea messageList = new JTextArea();
    
    public GUIConsole()
    {
        
    }
    
    //COnstructor
    public GUIConsole(String host, int port, String userId)
    {
        super("FPT Server - the Final Project of Van and Andy");
        setSize(450,550);
        
        setLayout(new BorderLayout(5,5));
        JPanel bottom = new JPanel();
        add("Center", messageList);
        add("South", bottom);
        
        //creates a grid layout with the specified number of rows and columns with horizontal and vertical gap.
        //5 hang + 2 cot
        bottom.setLayout( new GridLayout(9,6,5,5));
        
        //label and textbox            
        bottom.add(hostLB);//host LABEL     
        bottom.add(hostTxF);//host TEXT
        bottom.add(portLB);//port LABEL
        bottom.add(portTxF);//port TEXT
        bottom.add(userLB);
        bottom.add(userTxF);
        bottom.add(messageLB);//message LABEL
        bottom.add(messageTxF);//message TEXT
        bottom.add(logInB);//login button
        bottom.add(logOffB);//logoff button
        bottom.add(browseB);//browse button
        bottom.add(sendB);//send button        
        bottom.add(saveB);//save button
        bottom.add(downloadB);//download button
        bottom.add(listB);//list files button
        bottom.add(listCB);//list files combo box        
        bottom.add(quitB);//quit button
           
        portTxF.setText(port +"");
        hostTxF.setText(host);
        userTxF.setText(userId);
     
        setVisible(true); 
        sendB.addActionListener(new buttonHandler(this));
        logInB.addActionListener(new loginHandler(this));
        logOffB.addActionListener(new logoffHandler(this));
        quitB.addActionListener(new quitHandler(this));
        browseB.addActionListener(new browseHandler(this));
        saveB.addActionListener(new saveHandler(this));
        downB.addActionListener(new saveHandler(this));        
        listB.addActionListener(new listHandler(this));
        downloadB.addActionListener(new downloadHandler(this));
            
        try {
            client = new ChatClient(host, port, this);
        } catch (IOException exception) {
            System.out.println("Error: Can't setup connection!!!!"
                    + " Terminating client.");
            System.exit(1);
        }
    }
    
    public void display(String message)
    {
        messageList.insert(message+"\n", 0);
        
    }
    public void send(String message)
    {
        client.handleMessageFromClientUI(message);
        
    }
    public void login(String host, String port, String userId)
    {
        client.handleMessageFromClientUI("#setHost "+host);
        client.handleMessageFromClientUI("#setPort "+port);
        client.handleMessageFromClientUI("#login "+userId);
    }
    public void chooseFile() 
    {
        client.handleMessageFromClientUI("#chooseFile");       
    }
    
    public void saveFile()
    {
        client.handleMessageFromClientUI("#ftpUpload");        
    }
    public void close()
    {
        client.handleMessageFromClientUI("#logoff");       
    }
    
    public void list()
    {
        client.handleMessageFromClientUI("#ftpList");       
    }
    
    public void download(){
        send("#ftpGet" + listCB.getSelectedItem() + " ");
    }
        
    public void displayFileList(ArrayList<String> fileList){
        listCB.removeAllItems();        
        for(String file: fileList){
            listCB.addItem(file);
            }
    }
       
    public void quit()
    {
        client.handleMessageFromClientUI("#quit");
    }
        
    public static void main(String[] args)
    {
         String host = "";
        int port = 0;  //The port number

        try {
            host = args[0];
            port = Integer.parseInt(args[1]);
        } catch (ArrayIndexOutOfBoundsException e) {
            host = "localhost";//host - The host to connect to.
            //port - The port to connect on.

            port = DEFAULT_PORT;
        }
        
        String userId="";
        try
        {
            userId = args[2];
        }
        catch (ArrayIndexOutOfBoundsException e) {
            userId = "guest";
        }
        
        GUIConsole console = new GUIConsole(host, port, userId);
                               
    }
 
}

class buttonHandler implements ActionListener
{
    private GUIConsole console;
    public buttonHandler(GUIConsole console)
    {
        this.console = console;
    }
    public void actionPerformed(ActionEvent event)
    {
        //console.display(console.messageTxF.getText() + "\n");
        console.send(console.messageTxF.getText());
    }
}

class loginHandler implements ActionListener
{
    private GUIConsole console;
    public loginHandler(GUIConsole console)
    {
        this.console = console;
    }
    public void actionPerformed(ActionEvent event)
    {
        //console.display(console.messageTxF.getText() + "\n");
        console.login(console.hostTxF.getText(), console.portTxF.getText(), console.userTxF.getText());
    }
}
class logoffHandler implements ActionListener
{
    private GUIConsole console;
    public logoffHandler(GUIConsole console)
    {
        this.console = console;
    }
    public void actionPerformed(ActionEvent event)
    {
        console.close();
        
    }
}

class browseHandler implements ActionListener
{
    private GUIConsole console;
    public browseHandler(GUIConsole console)
    {
        this.console = console;
    }
    public void actionPerformed(ActionEvent event)
    {
       console.chooseFile();
       
    }
}

class saveHandler implements ActionListener
{
    private GUIConsole console;
    public saveHandler(GUIConsole console)
    {
        this.console = console;
    }
    public void actionPerformed(ActionEvent event)
    {
        //console.display(console.messageTxF.getText() + "\n");
       console.saveFile();
       console.display("The file has been saved successfully");
    }
}

class listHandler implements ActionListener
{
    private GUIConsole console;
    public listHandler(GUIConsole console)
    {
        this.console = console;
    }
    public void actionPerformed(ActionEvent event)
    {
     console.list();
      
    }
}

class downloadHandler implements ActionListener
{
    private GUIConsole console;
    public downloadHandler(GUIConsole console)
    {
        this.console = console;
    }
    public void actionPerformed(ActionEvent event)
    {
     console.download();
      
    }
}


class quitHandler implements ActionListener
{
    private GUIConsole console;
    public quitHandler(GUIConsole console)
    {
        this.console = console;
    }
    public void actionPerformed(ActionEvent event)
    {
        //console.display(console.messageTxF.getText() + "\n");
        console.quit();
    }
}
