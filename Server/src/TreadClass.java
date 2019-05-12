import java.net.Socket;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;


public class TreadClass extends Thread
{
    protected Socket socket;
    private String clientAddress;
    private String directoryName;
    private String ivString;

    public TreadClass(){ }

    public TreadClass(Socket socket, String clientAddress)
    {
        this.socket = socket;
        this.clientAddress = clientAddress;
    }

    public void run()
    {
        directoryName = "C:/Documents/"+clientAddress;
        new File(directoryName).mkdirs(); //a directory with a unique name based on the clients IP will be created

        Encryption encr;
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        BufferedInputStream bis = null;
        InputStream is = null;
        OutputStream os = null;
        String str;

        try
        {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            str = in.readLine();
            encr = new Encryption(clientAddress,16,"AES");

            if(str.equals("file"))
            {
                out.write("Everything is ok!\n");
                out.flush();
                do
                {
                    str = in.readLine();

                    if(str.equals("send"))
                    {
                        //receive a file from the client and read it
                        byte[] mybytearray = new byte[1024];
                        is = socket.getInputStream();
                        fos = new FileOutputStream(directoryName+"/clients_file.txt");
                        bos = new BufferedOutputStream(fos);
                        int bytesRead = is.read(mybytearray, 0 , mybytearray.length);
                        bos.write(mybytearray,0,bytesRead);
                        System.out.println("File received");
                        bos.flush();
                        fos.flush();

                        //encrypts the file and then saves it in a unique per user directory
                        encr.encryptFile(new File(directoryName+"/clients_file.txt"));

                        //writes the iv in a file and then saves it in a unique per user directory
                        ivString = encr.getInitvector();
                        try (PrintWriter printWriter = new PrintWriter(directoryName+"/iv.txt"))
                        {
                            printWriter.println(ivString);
                        }

                    }
                    else if(str.equals("receive"))
                    {
                        //the server sends the iv to the client
                        File myFileIV = new File(directoryName+"/iv.txt");
                        byte[] mybytearrayIV = new byte[(int) myFileIV.length()];
                        bis = new BufferedInputStream(new FileInputStream(myFileIV));
                        bis.read(mybytearrayIV, 0, mybytearrayIV.length);
                        os = socket.getOutputStream();
                        os.write(mybytearrayIV, 0, mybytearrayIV.length);
                        System.out.println("Iv sent to client");
                        os.flush();

                        str=in.readLine();

                        if(str.equals("received iv ok"))
                        {
                            //the server sends the iv to the client
                            File myFileMac = new File(directoryName+"/mac.txt");
                            byte[] mybytearrayMac= new byte[(int) myFileMac.length()];
                            bis = new BufferedInputStream(new FileInputStream(myFileMac));
                            bis.read(mybytearrayMac, 0, mybytearrayMac.length);
                            os = socket.getOutputStream();
                            os.write(mybytearrayMac, 0, mybytearrayMac.length);
                            System.out.println("Mac sent to client");
                            os.flush();
                        }

                        str=in.readLine();

                        if(str.equals("received mac ok"))
                        {
                            //the server sends the encrypted file back to the client
                            File myFile = new File(directoryName+"/clients_file.txt");
                            byte[] mybytearray = new byte[(int) myFile.length()];
                            bis = new BufferedInputStream(new FileInputStream(myFile));
                            bis.read(mybytearray, 0, mybytearray.length);
                            os = socket.getOutputStream();
                            os.write(mybytearray, 0, mybytearray.length);
                            System.out.println("File sent to client");
                            os.flush();
                        }
                    }
                }while(!str.equals("end"));

                fos.close();
                bos.close();
                bis.close();
                os.close();
                is.close();
                socket.close();
            }
        }
        catch(IOException ex) {
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(TreadClass.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchPaddingException ex) {
            Logger.getLogger(TreadClass.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeyException ex) {
            System.out.println(ex);
        } catch (IllegalBlockSizeException ex) {
            Logger.getLogger(TreadClass.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadPaddingException ex) {
            Logger.getLogger(TreadClass.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}