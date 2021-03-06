import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import javax.imageio.ImageIO;

public class ClientHandler extends Thread
{

	private static ArrayList<String> connectedList = new ArrayList<String>();	

	
	private Socket socket;
	private int clientNumber = 0;

	public ClientHandler(Socket socket, int clientNumber) throws IOException 
	{
		this.clientNumber = clientNumber;
		this.socket = socket;
//		System.out.println("Nouvelle connexion avec le client #" + ++clientNumber + " a " + socket);

	}
	
	/* Cette fonction existe dans la classe mère Thread et elle s'execute une fonction que le thread est crééé  */	
	public void run() 
	{
		try
		{
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			out.writeUTF("Message du serveur: Connexion reussie!");
			
			while(readOptions());
			
		}
		catch (IOException e)
		{
			System.out.println("Probleme avec le client " + clientNumber + ": " + e);
		}
		finally
		{
			try
			{
				socket.close();
			}
			catch (IOException e)
			{
				System.out.println("Impossible de fermer le socket");
			}
			System.out.println("La connexion avec le client " + clientNumber + " est ferme");
		}
	}
	
	/* Cette fonction sert à recevoir du serveur l'option choisi par l'utilsateur
		 Cette fonction décide l'état du compte de l'utilisateur. (S'il existe ou pas, s'il est déjà connecté etc..)
	 */
	private boolean readOptions() throws IOException {
		
		DataInputStream in = new DataInputStream(socket.getInputStream());	
		DataOutputStream out = new DataOutputStream(socket.getOutputStream());	
		String optionFromClient = in.readUTF();

		if(optionFromClient.equals("login"))
		{
			String account = in.readUTF();
				
			if(connectedList.contains(account))
			{
				out.writeUTF("Vous etes déjà connecté");
				System.out.println("vous etes déjà connecté");
				
			}
			else if(findInFile(out, "members.txt", account))
			{
				out.writeUTF("Connexion réussie!");
				connect(account);
			}
			else
			{
				out.writeUTF("Le mot de passe saisie est incorrecte!");	
				System.out.println("le mot de passe est incorrecte");
			}
			
		}
		else if(optionFromClient.equals("sobelFilter"))
		{
			String imageState = in.readUTF();
			if(imageState.equals("fichier trouve"))				
				readImageFromClient(in);
		}
		else if(optionFromClient.equals("logout"))
		{
			return false;
		}
		
		return true;
	}
	
	/* Cette fonction cherche dans notre base de données(fichier.txt) le compte qui vient de se connecter à notre serveur 
		 Si le compte existe, l'utilisateur se connecte au serveur
		 Sinon, le compte de l'utilisateur sera ajouté dans le fichier puis il se connecte au serveur.
		 Si le mot de passe est incorrecte, le client va recevoir un avertissement.
	*/
	private boolean findInFile(DataOutputStream out, String fileName, String account) throws IOException
	{

		boolean usernameExist = false;
    	File file = new File(fileName);

		if(file.exists())
		{			
			Scanner scanner = new Scanner(file);
			while (scanner.hasNextLine() != false)
			{
				String currentUser = scanner.nextLine();
				if(currentUser.split(":")[0].equals(account.split(":")[0]))
				{		
					usernameExist = true;
					if(currentUser.split(":")[1].equals(account.split(":")[1]))
						return true;
					else
						return false;
				}
			}
			scanner.close();
			
			if(usernameExist == false) 
			{
				writeInFile(fileName, account);
				return true;
			}
		}
		else
		{
			writeInFile(fileName, account);
			return true;
		}
    	
    	
    	return false;
	}

	/* Cette fonction lit l'image envoyé du client puis elle applique le filtre de sobel sur la photo et à la fin elle la renvoie au client*/
	private void readImageFromClient(DataInputStream in) throws IOException
	{
		File file = new File(clientNumber + "newImage.png");
		FileOutputStream fos = new FileOutputStream(file);
		int arrlen = in.readInt();
		byte[] b = new byte[arrlen];
		in.readFully(b);
		fos.write(b, 0 , b.length);
		fos.close();
		
		File imFile = new File(clientNumber + "newImage.png");
		BufferedImage sobelImage = ImageIO.read(imFile);
		Sobel.process(sobelImage);
			
		sendImageToClient("sobel.png");
	}
	
	/* Cette fonction envoie une image au client */
	private void sendImageToClient(String imageName) throws IOException
	{
    	File file = new File(imageName);

    	DataOutputStream out = new DataOutputStream(socket.getOutputStream());
    	
    	byte[] b = new byte[(int) file.length()];
    	FileInputStream fis = new FileInputStream(file);
    	fis.read(b);
    	fis.close();
    	out.writeInt((int) file.length());
    	out.flush();
    	out.write(b, 0, b.length);
    	out.flush();
    	System.out.println("Fichier bien envoyé");
		
	}
	
	/* Cette fonction écrit dans un fichier.txt */
	private static void writeInFile(String fileName, String str) throws IOException
	{  
		BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true));
	    writer.append(str);
	    writer.append("\n");
	    writer.close();
	}
	
	/* Cette fonction ajoute le client qui vient de se connecter */
	private void connect(String account) throws IOException 
	{
		connectedList.add(account);

		System.out.println("Nouvelle connexion avec le client #" + clientNumber + " à " + socket);
		System.out.println("Bonjour " + account.split(":")[0] + "!");
	}
	
}
