import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.*;
import java.io.*;


public class Receiver {
	
	// Maximum Segment Size - Quantity of data from the application layer in the segment
	public static  int MSS = 500;

	// Probability of loss during packet sending
	public static  double PROBABILITY = 0.1;

	// Window size - Number of packets sent without acking
	public static  int WINDOW_SIZE = 15;
	
	// Time (ms) before REsending all the non-acked packets
	public static  int TIMER = 1000;
	
	public static int file(String filename){
	String[] file=new String[5];
	int x=0;
    Scanner sc2 = null;
    try {
        sc2 = new Scanner(new File(filename));
    } catch (FileNotFoundException e) {
        e.printStackTrace();  
    }
    while (sc2.hasNextLine()) {
            Scanner s2 = new Scanner(sc2.nextLine());
        while (s2.hasNext()) {
            String s = s2.next();
			file[x]=s;
			x=x+1;
            System.out.println(s);
        }
    }
	System.out.println(file);	
	// Maximum Segment Size - Quantity of data from the application layer in the segment
	MSS = Integer.parseInt(file[4]);

	// Probability of loss during packet sending
	PROBABILITY = 0.1;

	// Window size - Number of packets sent without acking
	WINDOW_SIZE = Integer.parseInt(file[2]);
	
	// Time (ms) before REsending all the non-acked packets
	TIMER = Integer.parseInt(file[3]);
	return Integer.parseInt(file[4]);
	}
	
	public static void main(String[] args) throws Exception{
		
		
		
		    String fname= "", p1 = "" ; //Taking arguments from the client to connect to the server.
        
        if (args.length <2 || args.length > 2) 
            {
            System.out.println("Insufficient or no arguments found. Please pass arguments in proper order\t 1:Input File Name \t 2:port \t 3: Number of Packets");
            } 
        else            //If the arguments are correct and in proper order then store them in proper variables. 
            {
            fname = args[0];
            p1 = args[1];//Integer.parseInt(args[1]);
            }
        int port= Integer.parseInt(p1);
        
		
		
		file(fname);
		DatagramSocket fromSender = new DatagramSocket(port);
		
		// 83 is the base size (in bytes) of a serialized RDTPacket object 
		byte[] receivedData = new byte[MSS + 93];
		
		int waitingFor = 0;
		String rec_msg;
		int rec_chk=0;
		boolean last=false;
		ArrayList<RDTPacket> received = new ArrayList<RDTPacket>();
		
		boolean end = false;
		
		while(!end){
			
			System.out.println("Waiting for packet"+waitingFor);
			
			// Receive packet
			DatagramPacket receivedPacket = new DatagramPacket(receivedData, receivedData.length);
			fromSender.receive(receivedPacket);
			
			// Unserialize to a RDTPacket object
			RDTPacket packet = (RDTPacket) Serializer.toObject(receivedPacket.getData());
			rec_msg="";
			for(byte b: packet.getData()){
				
				rec_msg=rec_msg+(char) b;
			}
			rec_chk=ChecksumMethod.generateChecksum(rec_msg);
						
			System.out.println("Packet with sequence number " + packet.getSeq() + " received (last: " + packet.isLast() + " )");
		
			if(packet.chk != rec_chk){
				System.out.println("Packet discarded (Checksum error)");
				//waitingFor++;
			}else if(packet.getSeq() == waitingFor && packet.isLast()){
				
				waitingFor++;
				received.add(packet);
				last=true;
				System.out.println("Last packet received");
				
				end = true;
				
			}else if(packet.getSeq() == waitingFor){
				waitingFor++;
				received.add(packet);
				System.out.println("Packed stored in buffer");
			}else{
				System.out.println("Packet discarded (not in order)");
			}
			//if (waitingFor==15)
				//waitingFor=0;
			// Create an RDTAck object
			RDTAck ackObject = new RDTAck(waitingFor,last);
			
			// Serialize
			byte[] ackBytes = Serializer.toBytes(ackObject);
			
			
			DatagramPacket ackPacket = new DatagramPacket(ackBytes, ackBytes.length, receivedPacket.getAddress(), receivedPacket.getPort());
			
			// Send with some probability of loss
			if(Math.random() > PROBABILITY){
				fromSender.send(ackPacket);
			}else{
				System.out.println("[X] Lost ack with sequence number " + ackObject.getPacket());
			}
			
			System.out.println("Sending ACK to seq " + waitingFor + " with " + ackBytes.length  + " bytes");
			

		}
		
		// Print the data received
		/*System.out.println(" ------------ DATA ---------------- ");
		
		for(RDTPacket p : received){
			for(byte b: p.getData()){
				
				System.out.print((char) b);
			}
		}*/
		
	}
	
	
}
