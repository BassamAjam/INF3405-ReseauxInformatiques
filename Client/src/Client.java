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
		while(!validateIp());
		while(!validatePort());
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
		
		option = userInput.nextInt();
	
		if(option == 1)
		{
			out.writeUTF("login");

			System.out.print("\n\nSaisir votre nom d'utilisateur: ");			
			Scanner usernameInput = new Scanner(System.in);
			username = usernameInput.nextLine();
			
			System.out.print("\n\nSaisir votre mot de passe: ");
			Scanner pswInput = new Scanner(System.in);	
			psw = pswInput.nextLine();
		
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
				Scanner photoInput = new Scanner(System.in);
	
				existFile(out, photoInput.nextLine());
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
	
	private static void existFile(DataOutputStream out, String imageName) throws IOException
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
			Scanner newPhotoInput = new Scanner(System.in);
			readImageFromServer(newPhotoInput.nextLine());
    	}
	}
	
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
		System.out.println("l'image a ete bien creee");	
    }
	
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
    	System.out.println("Fichier bien envoye");
	}
    
	private static boolean validatePort() 
	{
		System.out.println("Saisir le numero du port:");
		port = userInput.nextLine();
		
		try 
		{
			Integer.parseInt(port);
		}
		catch(Exception e)
		{
			System.out.println(port + " doit etre un numero valide.");
			return false;
		}
		if(Integer.parseInt(port) < 5000 || Integer.parseInt(port) > 5050)
		{
			return false;
		}
		return true;
	}

	private static boolean validateIp() 
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
