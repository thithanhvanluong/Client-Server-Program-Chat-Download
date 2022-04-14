
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class ServerConsole implements ChatIF {

    final public static int DEFAULT_PORT = 5555;
    EchoServer echoServer;

    public static void main(String[] args) {
        int port = 0; //Port to listen on

        try {
            port = Integer.parseInt(args[0]);
        } catch (ArrayIndexOutOfBoundsException oob) {
            port = DEFAULT_PORT; //Set port to 5555
        }


        ServerConsole serverConsole = new ServerConsole(port);//Constructs an instance of the ClientConsole UI.
        serverConsole.accept();  //This method waits for input from the console.


    }

    public ServerConsole(int port) {
        echoServer = new EchoServer(port);
    }

    public void accept() {
        try {
            BufferedReader fromConsole
                    = new BufferedReader(new InputStreamReader(System.in));
            String message;

            while (true) //keep reading from the console
            {
                message = fromConsole.readLine();
                echoServer.handleCommandFromServerConsole(message);// this method is from ChatClient class
            }
        } catch (Exception ex) {
            System.out.println("Unexpected error while reading from console!");
        }
    }

    @Override
    public void display(String message) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
