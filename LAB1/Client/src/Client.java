import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client {
	
	private static Socket socket;
	private static Scanner userInput = new Scanner(System.in);
	
	private static String ip = "";
	private static String port = "";
	private static String account = "";
	private static int option = 0;
	
	public static void main(String args[]) throws Exception
	{	
		while(!getAndValidateIp());
		while(!getAndvalidatePort());
		try 
		{	
			socket = new Socket(ip, Integer.parseInt(port));
		}
		catch(Exception e)
		{
			System.out.println("Le serveur est en panne: \n" + e);
		}
		
		DataInputStream in = new DataInputStream(socket.getInputStream());	
		System.out.println("****" + in.readUTF() + "****");
		
		while(sendOption());
	
		socket.close();
		System.out.println("****Socket est ferme - Fin du programme.****");
	}
	
	/* Cette fonction sert à envoyer au serveur l'option choisi par l'utilsateur */
	private static boolean sendOption() throws IOException 
	{
		String username = "";
		String psw = "";
		
		DataOutputStream out = new DataOutputStream(socket.getOutputStream());
		DataInputStream in = new DataInputStream(socket.getInputStream());	
		
		System.out.println("[1] S'identifier.");
		System.out.println("[2] Ajouter le filtre de sobel.");
		System.out.println("[3] Se deconnecter.");
		System.out.print("\n\nChoisir un numero:");
		
		option = Integer.parseInt(userInput.nextLine());
	
		if(option == 1)
		{
			out.writeUTF("login");

			while(true) {	
				System.out.print("\n\nSaisir votre nom d'utilisateur: ");			
				username = userInput.nextLine();
				
				if(username.length() > 0) {
					break;
				}
				System.out.println("Cannot be empty");
			}
			while(true) {
				System.out.print("\n\nSaisir votre mot de passe: ");
				psw = userInput.nextLine();
				
				if(psw.length() > 0) {
					break;
				}
				System.out.println("Cannot be empty");
			}
			
			account = username + ":" + psw;
			out.writeUTF(account);
			String answerFromServer = in.readUTF();
			System.out.println("****" + answerFromServer + "****");
			
		}
		else if(option == 2)
		{
			if(!account.equals(""))
			{
				out.writeUTF("sobelFilter");
	
				System.out.print("\n\nSaisir le nom d'image pour ajouter le filtre de sobel: ");			
	
				checkFileExistsAndSendToServer(out, userInput.nextLine());
			}
			else
				System.out.println("***S'identifier d'abord***");	
			
		}
		else if(option == 3)
		{
			out.writeUTF("logout");
			return false;
		}
		else
			System.out.println("****Veuillez choisir parmi les options suivantes:");	
		
		return true;
	}
	
	/* Cette fonction sert à envoyer l'image au serveur apres avoir vérifier si l'image est valide ou non */
	private static void checkFileExistsAndSendToServer(DataOutputStream out, String imageName) throws IOException
	{
    	File file = new File(imageName);
    	if(!file.exists())
    	{
    		out.writeUTF("fichier introuvable");
    		System.out.println("***Fichier introuvable***");	
    	}
    	else
    	{
    		out.writeUTF("fichier trouve"); // le message est une condition dans le serveur
    		sendImageToServer(out,file);
    		
    		System.out.print("\n\nSaisir un nom pour la nouvelle image sobel: ");			
			readImageFromServer(userInput.nextLine());
    	}
	}
	
	/* Cette fonction sert à lire l'image reçu du serveur */
    private static void readImageFromServer(String newImageName) throws IOException
    {
		DataInputStream in = new DataInputStream(socket.getInputStream());
		File file = new File(newImageName);
		FileOutputStream fos = new FileOutputStream(file);
		int arrlen = in.readInt();
		byte[] b = new byte[arrlen];
		in.readFully(b);
		fos.write(b, 0 , b.length);
		fos.close();
		System.out.println("l'image a ete bien sauvagardée: " + file.getAbsolutePath());	
    }
	
	/* Cette fonction sert à envoyer l'image au serveur */
    private static void sendImageToServer(DataOutputStream out, File file) throws IOException 
    {
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
    
	/* Cette fonction Vérifie si le port saisi par l'utilisateur est valide */	
	private static boolean getAndvalidatePort() 
	{
		System.out.println("Saisir le numéro du port:");
		port = userInput.nextLine();
		
		try 
		{
			Integer.parseInt(port);
		}
		catch(Exception e)
		{
			System.out.println(port + " doit etre un numéro valide.");
			return false;
		}
		if(Integer.parseInt(port) < 5000 || Integer.parseInt(port) > 5050)
		{
			return false;
		}
		return true;
	}

	/* Cette fonction Vérifie si l'adresse ip saisi par l'utilisateur est valide */	
	private static boolean getAndValidateIp() 
	{
		System.out.println("Saisir l'adresse ip:");
		ip = userInput.nextLine();
		String [] nums = ip.split("\\.");

		for(int i=0; i<4; i++)
		{
			try 
			{
				Integer.parseInt(nums[i]);
			}
			catch(Exception e)
			{
				System.out.println(ip + " doit etre un numero valide.");
				return false;
			}
			if( Integer.parseInt(nums[i]) < 0 || Integer.parseInt(nums[i]) > 255)
			{
				return false;
			}
		}
		return true;
	}
}
