/**
 * TFTP
 *
 * Description: This program implements Trivial File Transfer Protocol (TFTP).
 *              
 * @version 1.0   09/26/2014 6.30 pm
 * $Id:
 *
 * @author  Manas Mandhani
 *
 * Revisions:
 *
 */
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * 
 * Class TFTP_CLIENT implements Trivial File Transfer Protocol (TFTP)
 * 
 * @version 1.0
 * 
 * @author Manas Mandhani
 */
public class TFTP_CLIENT {
	String mode = "octet";
	InetAddress address; 
	DatagramSocket socket;
	DatagramSocket socket1;
	
	TFTP_CLIENT(){
		address = null;
	}

	/**
	 * Method "connect" for connecting with the server.
	 */
	public InetAddress connect(String inetAddress) {
		try {
			address = InetAddress.getByName(inetAddress);
		} catch (UnknownHostException e) {
			System.out.println("tftp: nodename nor servname provided, or not known");
		}
		return address;
	}

	/**
	 * Method "get" for retrieving a file from the server.
	 */
	public void get(String value, InetAddress address) throws IOException {
		int flag = 0;
		int count = 0;
		socket = new DatagramSocket(20000);
		File file = new File(value);
		FileOutputStream out = new FileOutputStream(file);

		byte[] buffer = new byte[516];
		for (int i = 0; i < 2; i++) {
			buffer[count] = (byte) i;
			count++;
		}
		for (int i = 0; i < value.length(); i++) {
			buffer[count] = (byte) value.charAt(i);
			count++;
		}
		buffer[count] = 0;
		count++;
		for (int i = 0; i < mode.length(); i++) {
			buffer[count] = (byte) mode.charAt(i);
			count++;
		}
		buffer[count] = 0;
		DatagramPacket packet = new DatagramPacket(buffer, count, address, 69);
		socket.send(packet);
		socket.close();

		byte[] buff = new byte[516];
		byte[] ack;
		DatagramPacket packet1;
		socket1 = new DatagramSocket(20000);
		
		double time1 = System.currentTimeMillis();
		double time2 = 0;
		while (true) {
			packet1 = new DatagramPacket(buff, buff.length);
			socket1.setSoTimeout(4000);
			socket1.receive(packet1);
			buff = packet1.getData();
			if (buff[0] == 0 && buff[1] == 5) {
				if (buff[2] == 0 && buff[3] == 1) {
					System.out.println("File not found.");
					file.delete();
					flag++;
					break;
				}
				if (buff[2] == 0 && buff[3] == 2) {
					System.out.println("Access violation.");
					flag++;
					break;
				}
				if (buff[2] == 0 && buff[3] == 3) {
					System.out.println("Disk full or allocation exceeded.");
					flag++;
					break;
				}
				if (buff[2] == 0 && buff[3] == 4) {
					System.out.println("Illegal TFTP operation.");
					flag++;
					break;
				}
				if (buff[2] == 0 && buff[3] == 5) {
					System.out.println("Unknown transfer ID.");
					flag++;
					break;
				}
				if (buff[2] == 0 && buff[3] == 6) {
					System.out.println("File already exists.");
					flag++;
					break;
				}
				if (buff[2] == 0 && buff[3] == 7) {
					System.out.println("No such user.");
					flag++;
					break;
				}
			}

			if (packet1.getLength() < 516) {
				break;
			}
			ack = new byte[20];
			int server_port = packet1.getPort();
			ack[0] = 0;
			ack[1] = 4;
			ack[2] = buff[2];
			ack[3] = buff[3];
			out.write(buff, 4, packet1.getLength() - 4);
			DatagramPacket ack_packet = new DatagramPacket(ack, ack.length,address, server_port);
			socket1.send(ack_packet);
			buff = new byte[516];
		}
		if (flag == 0) {
			time2 = System.currentTimeMillis();
			buff = packet1.getData();
			out.write(buff, 4, packet1.getLength() - 4);
			System.out.println("Received " + file.length() + " bytes in " + (time2 - time1)/1000 + " seconds" );
			socket1.close();
			out.close();
		} else {
			flag = 0;
			socket1.close();
			out.close();
		}
	}

	/**
	 * Method "main" controls the execution of the program.
	 */
	public static void main(String[] args) throws IOException {
		InetAddress addr = null;
		TFTP_CLIENT client = new TFTP_CLIENT();
		Scanner sc = new Scanner(System.in);
		System.out.print("tftp> ");

		while (sc.hasNext()) {
			String command = sc.next();
			if (command.equals("quit")) {
				System.exit(0);
			}

			else if (command.equals("?")) {
				System.out.println("Commands may be abbreviated.  Commands are:");
				System.out.println("get     	receive file");
				System.out.println("quit    	exit tftp");
				System.out.println("status  	show current status");
				System.out.println("?       	print help information");
			}

			else if (command.equals("status")) {
				if (client.address != null) {
					System.out.println("Connected to " + client.address.getHostName());
					System.out.println("Mode: " + client.mode);
				}else{
						System.out.println("Not connected");
						System.out.println("Mode: " + client.mode );
					}
				}
			
			else if (command.equals("connect")) {
					String value = sc.next();
					addr = client.connect(value);
				}
				
			else if (command.equals("mode")) {
					String value = sc.next();
					client.mode = value;
				}

			else if (command.equals("get")) {
					try {
						String value = sc.next();
						client.get(value, addr);
					} catch (SocketTimeoutException e) {
						System.out.println(e.getMessage());
						client.socket1.close();
					}
				}
			else{
				System.out.println("Invalid command");
			}
				System.out.print("tftp> ");
			}
		}
	}
