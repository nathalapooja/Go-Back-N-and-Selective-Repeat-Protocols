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

public class Sender {
	

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
		
		
		    String fname= "", p1 = "", lastS1 = "" ; //Taking arguments from the client to connect to the server.
        
        if (args.length <3 || args.length > 3) 
            {
            System.out.println("Insufficient or no arguments found. Please pass arguments in proper order\t 1:Input File Name \t 2:port \t 3: Number of Packets");
            } 
        else            //If the arguments are correct and in proper order then store them in proper variables. 
            {
            fname = args[0];
            p1 = args[1];//Integer.parseInt(args[1]);
            lastS1= args[2];//Integer.parseInt(args[2]);
            }
        int port= Integer.parseInt(p1);
        int lastSeq= Integer.parseInt(lastS1);
		
		
		
		
		
		int chk=0,packet_count=0;
		file(fname);
		
		// Sequence number of the last packet sent (rcvbase)
		int lastSent = 0;
		
		// Sequence number of the last acked packet
		int waitingForAck = 0;

		// Data to be sent (you can, and should, use your own Data-> byte[] function here)
		byte[] fileBytes = new byte[MSS];
		System.out.println("Data size: " + fileBytes.length + " bytes");

		// Last packet sequence number
		//int lastSeq = (int) Math.ceil( (double) fileBytes.length / MSS);
		//int lastSeq = 50;
		int prob_count = 0;
		boolean chk_flag = false;

		System.out.println("Number of packets to send: " + lastSeq);

		DatagramSocket toReceiver = new DatagramSocket();

		// Receiver address
		InetAddress receiverAddress = InetAddress.getByName("localhost");
		
		// List of all the packets sent
		ArrayList<RDTPacket> sent = new ArrayList<RDTPacket>();

		while(true){

			// Sending loop
			while(lastSent - waitingForAck < WINDOW_SIZE && lastSent < lastSeq){

				// Array to store part of the bytes to send
				//byte[] filePacketBytes = new byte[MSS];
				String msg="CCN PROJECT 2 SPRING-2017...........................................................................................................................................................................................................................................................................................................................................................................................................................................................................................";
				byte[] filePacketBytes = new byte[MSS];
				filePacketBytes = msg.getBytes();
				//new Random().nextBytes(filePacketBytes);
				chk=ChecksumMethod.generateChecksum(msg);
				System.out.println("The checksum generated is = "+ chk);
	
				// Copy segment of data bytes to array
				//filePacketBytes = Arrays.copyOfRange(fileBytes, lastSent*MSS, lastSent*MSS + MSS);
				
				/*if(prob_count==(int)(0.1*lastSeq+2)){
					chk=chk+2;
					prob_count=0;
					System.out.println("***************CHECKSUM CHANGED**********************");
				}*/
				if(Math.random() > PROBABILITY){
					//toReceiver.send(packet);
				}else{
					chk=chk+2;
					chk_flag=true;
					System.out.println("[X] Checksum changed for packet with sequence number " + lastSent);
				}
				// Create RDTPacket object
				RDTPacket rdtPacketObject = new RDTPacket(chk,lastSent, filePacketBytes, (lastSent == lastSeq-1) ? true : false);
				
				// Serialize the RDTPacket object
				byte[] sendData = Serializer.toBytes(rdtPacketObject);
				
				// Create the packet
				DatagramPacket packet = new DatagramPacket(sendData, sendData.length, receiverAddress, port );

				System.out.println("Sending packet with sequence number " + lastSent +  " and size " + sendData.length + " bytes");

				// Add packet to the sent list
				if(chk_flag==true){
					// Create RDTPacket object
					RDTPacket rdtPacketObject_ce = new RDTPacket(chk-2,lastSent, filePacketBytes, (lastSent == lastSeq-1) ? true : false);
					
					// Serialize the RDTPacket object
					byte[] sendData_ce = Serializer.toBytes(rdtPacketObject);
					
					// Create the packet
					DatagramPacket packet_ce = new DatagramPacket(sendData, sendData.length, receiverAddress, port );
					sent.add(rdtPacketObject_ce);
					chk_flag=false;	
				}else sent.add(rdtPacketObject);
				
				// Send with some probability of loss
				/*if(prob_count==0.1*lastSeq){
					System.out.println("[X] Lost packet with sequence number " + lastSent);
				}else{
					toReceiver.send(packet);
				}*/
				prob_count++;
				if(Math.random() > PROBABILITY){
					toReceiver.send(packet);
				}else{
					System.out.println("[X] Lost packet with sequence number " + lastSent);
				}

				// Increase the last sent
				lastSent++;
//				if (lastSent==15)
//					lastSent=0;
//				packet_count++;

			} // End of sending while
			
			// Byte array for the ACK sent by the receiver
			byte[] ackBytes = new byte[48];
			
			// Creating packet for the ACK
			DatagramPacket ack = new DatagramPacket(ackBytes, ackBytes.length);
			
			try{
				// If an ACK was not received in the time specified (continues on the catch clausule)
				toReceiver.setSoTimeout(TIMER);
				
				// Receive the packet
				toReceiver.receive(ack);
				
				// Unserialize the RDTAck object
				RDTAck ackObject = (RDTAck) Serializer.toObject(ack.getData());
				
				System.out.println("Received ACK for " + ackObject.getPacket());
				
				// If this ack is for the last packet, stop the sender (Note: gbn has a cumulative acking)
				if(ackObject.getPacket() == lastSeq){
					break;
				}
				
				waitingForAck = Math.max(waitingForAck, ackObject.getPacket());
				
			}catch(SocketTimeoutException e){
				// then send all the sent but non-acked packets
				
				for(int i = waitingForAck; i < lastSent; i++){
					
					// Serialize the RDTPacket object
					byte[] sendData = Serializer.toBytes(sent.get(i));

					// Create the packet
					DatagramPacket packet = new DatagramPacket(sendData, sendData.length, receiverAddress, port );
					
					// Send with some probability
					if(Math.random() > PROBABILITY){
						toReceiver.send(packet);
					}else{
						System.out.println("[X] Lost packet with sequence number " + sent.get(i).getSeq());
					}

					System.out.println("REsending packet with sequence number " + sent.get(i).getSeq() +  " and size " + sendData.length + " bytes");
				}
			}
			
		
		}
		
		System.out.println("Finished transmission");

	}

}
