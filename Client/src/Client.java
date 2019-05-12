import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;


public class Client
{
    private static final String serverAddress = "192.168.1.82";
    private static final int port = 5555;

    public static void main(String[] args)
    {
        System.out.println("Connecting to server..Please Wait");
        Socket socket = null;

        try {
            socket = new Socket(serverAddress, port); //initializes the socket at the right server address and port
            System.out.println("Connection OK");
        } catch (UnknownHostException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        new FileSharing(socket).run(); //Creates a new FileSharing object with the server socket that was created and calls the run function
    }

}
