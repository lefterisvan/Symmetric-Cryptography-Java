import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class FileSharing
{
    private Socket socket;
    private Scanner scanner;
    private FileInputStream fileInputStream;
    private FileOutputStream fileOutputStream;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private BufferedInputStream bufferedInputStream;
    private BufferedOutputStream bufferedOutputStream;
    private InputStream inputStream;
    private OutputStream outputStream;
    private String file_to_send;
    private String ivString;

    public FileSharing() { } //default constructor

    public FileSharing(Socket socket)
    {
        this.socket = socket;
        this.scanner = new Scanner(System.in);
    }

    public void run()
    {
        Decryption decrypted;
        String send;
        String receive;

        try
        {
            decrypted = new Decryption(socket.getLocalAddress().toString(),16,"AES");

            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bufferedWriter.write("file\n"); //sends the message to the server
            bufferedWriter.flush(); //flushes the buffer

            System.out.println("Waiting for server's response...");
            receive = bufferedReader.readLine(); //reads the server's response and prints it
            System.out.println("Server said: "+receive);

            do
            {
                //continue with file sharing or end the program?
                System.out.println();
                System.out.println("---------------- MENU ----------------");
                System.out.println("If you wish to send a file, type <send>");
                System.out.println("If you wish to receive a file from the server, type <receive>");
                System.out.println("If you wish to terminate the communication, type <end>");
                send = scanner.nextLine();

                bufferedWriter.write(send+"\n"); //this message lets the server know that a file transfer will occur
                bufferedWriter.flush(); //flushes the buffer

                if(send.equals("send"))
                {
                    System.out.println("Enter the path of the file you want to send: "); 
                    send = scanner.nextLine(); //reads the file path
                    file_to_send = send;

                    File myFile = new File (file_to_send); //creates a new file object based on the given path
                    byte [] mybytearray  = new byte [(int)myFile.length()]; //creates a byte array with size equal to the file's bytes

                    //initialization of the stream and buffer
                    fileInputStream = new FileInputStream(myFile);
                    bufferedInputStream = new BufferedInputStream(fileInputStream);
                    bufferedInputStream.read(mybytearray,0,mybytearray.length); //copies the file into mybytearray
                    outputStream = socket.getOutputStream();
                    System.out.println("Sending " + file_to_send + "(" + mybytearray.length + " bytes)");
                    outputStream.write(mybytearray,0,mybytearray.length); //sends the file to the server
                    outputStream.flush(); //flushes the stream
                    System.out.println("File sent to server");
                }
                else if(send.equals("receive"))
                {
                    byte [] mybytearrayIV  = new byte [1024]; //creates a byte array
                    inputStream = socket.getInputStream();//creates an InputStream
                    bufferedOutputStream = new BufferedOutputStream(new FileOutputStream("iv.txt")); //creates a BufferedOutputStream and wraps the FileOutputStream for the iv file
                    int bytesReadIV = inputStream.read(mybytearrayIV, 0, mybytearrayIV.length);
                    bufferedOutputStream.write(mybytearrayIV, 0, bytesReadIV); //writes tha data to mybytearrayIV
                    System.out.println("Iv received from server");
                    bufferedOutputStream.flush();//flushes the buffer
                    bufferedWriter.write("received iv ok\n");//lets the server know that the iv file was received
                    bufferedWriter.flush();

                    byte [] mybytearrayMac = new byte [1024]; //creates a byte array
                    inputStream = socket.getInputStream();//creates an InputStream
                    bufferedOutputStream = new BufferedOutputStream(new FileOutputStream("mac.txt")); //creates a BufferedOutputStream and wraps the FileOutputStream for the mac file
                    int bytesReadMac = inputStream.read(mybytearrayMac, 0, mybytearrayMac.length);
                    bufferedOutputStream.write(mybytearrayMac, 0, bytesReadMac); //writes tha data to mybytearrayMac
                    System.out.println("Mac received from server");
                    bufferedOutputStream.flush();//flushes the buffer
                    bufferedWriter.write("received mac ok\n");//lets the server know that the mac file was received
                    bufferedWriter.flush();

                    byte [] mybytearray  = new byte [1024]; //creates a byte array
                    inputStream = socket.getInputStream();//creates an InputStream
                    fileOutputStream = new FileOutputStream("file_from_server.txt");//creates a FileOutputStream that will receive the incoming file
                    bufferedOutputStream = new BufferedOutputStream(fileOutputStream); //creates a BufferedOutputStream and wraps the FileOutputStream for the ciphertext file
                    int bytesRead = inputStream.read(mybytearray, 0, mybytearray.length);
                    bufferedOutputStream.write(mybytearray, 0, bytesRead); //writes tha data to mybytearray
                    System.out.println("File received from server");
                    bufferedOutputStream.flush();//flushes the buffer
                    decrypted.decryptFile(new File("file_from_server.txt")); //asks for the file to be decrypted and then saves it
                }
                else if (send.equals("end"))
                {
                    bufferedWriter.write(send + "\n");//lets the server know that the communication will be terminated
                    bufferedWriter.flush();
                }
            }while(!send.equals("end"));

            if(fileOutputStream != null) { fileOutputStream.close(); }
            if(fileInputStream != null){fileInputStream.close();}
            if(bufferedInputStream != null){bufferedInputStream.close();}
            if(bufferedOutputStream != null){bufferedOutputStream.close();}
            if(outputStream != null){outputStream.close();}
            if(inputStream != null){inputStream.close();}
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        System.out.println("Goodbye!!");
    }

    public String getIv() //function that receives the iv file, opens it, reads its contents and returns the read string
    {
        try
        {
            FileInputStream fstream = new FileInputStream("iv.txt");
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String txt;
            while ((txt = br.readLine()) != null)
            {
                ivString = txt;
            }

            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return  ivString;
    }

}

