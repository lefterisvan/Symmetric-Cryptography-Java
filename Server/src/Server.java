
import java.net.*;

public class Server
{

    static final int PORT= 5555;
    public static void main(String[] args)
    {

        ServerSocket serverSocket = null;
        Socket socket = null;
        InetAddress inetAddress = null;

        try
        {
            serverSocket = new ServerSocket(PORT);

        }catch (Exception ex)
        {
            System.out.println("Error");
        }
        while(true)
        {
            try
            {
                socket  = serverSocket.accept();

                SocketAddress socketAddress = socket.getRemoteSocketAddress();
                if (socketAddress instanceof InetSocketAddress)
                {
                    inetAddress = ((InetSocketAddress)socketAddress).getAddress();
                }
                else
                {
                    System.err.println("Not an internet protocol socket.");
                }

                System.out.println("Connected to client: "+inetAddress);
            }
            catch(Exception Ex)
            {
                System.out.println("Error");
            }
            new TreadClass(socket,inetAddress.toString()).start();
        }
    }

}
