import java.io.IOException;
import java.net.ServerSocket;
import java.util.Scanner;

public class Server {
	
	private static ServerSocket listener;
	private static Scanner userInput = new Scanner(System.in);
	private static String ip = null;
	private static String port = null;
	
	public static void main(String[] args) throws IOException
	{
		
		int clientNumber = 0;
		
		while(!validateIp());
		while(!validatePort());

		listener = new ServerSocket(Integer.parseInt(port));
		listener.setReuseAddress(true);
		
//		InetAddress serverIP = InetAddress.getByName(serverAddress);
//		listener.bind(new InetSocketAddress(serverIP, serverPort));
		
		System.out.format("Le serveur fonctionne sur %s: %s %n", ip, port);
		
		try 
		{			
			while(true) 
			{			

				new ClientHandler(listener.accept(), clientNumber++).start();
			}
		}
		catch (Exception e)
		{
			System.out.println(e);
		}
		finally
		{
		    System.out.println("Closing sockets.");
			listener.close();
		}
	}

	private static boolean validatePort() {
		System.out.println("Saisir le numero du port:");
		port = userInput.nextLine();
		
		try 
		{
			Integer.parseInt(port);
		}
		catch(Exception e)
		{
			System.out.println(port + " doit etre un numero valide entre 5000 et 5050");
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
