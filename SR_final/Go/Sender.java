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
	public static  int W_SIZE = 15;
	
	// Time (ms) before REsending all the non-acked packets
	public static  int TIMER = 1000;
	
	
	public static int file(String filename){
	String[] file=new String[5];
	int x=0;
    Scanner scan2 = null;
    try {
        scan2 = new Scanner(new File(filename));
    } catch (FileNotFoundException e) {
        e.printStackTrace();  
    }
    while (scan2.hasNextLine()) {
            Scanner scan1 = new Scanner(scan2.nextLine());//edit
        while (scan1.hasNext()) {
            String st = scan1.next();
			file[x]=st;
			x=x+1;
            System.out.println(st);
        }
    }
	System.out.println(file);	
	// Maximum Segment Size - Quantity of data from the application layer in the segment
	MSS = Integer.parseInt(file[4]);

	// Probability of loss during packet sending
	PROBABILITY = 0.1;

	// Window size - Number of packets sent without acking
	W_SIZE = Integer.parseInt(file[2]);
	
	// Time (ms) before REsending all the non-acked packets
	TIMER = Integer.parseInt(file[3]);
	return Integer.parseInt(file[4]);
	}
	
	public static void main(String[] args) throws Exception{
		
		    String fname= "", port1 = "", lastS1 = "" ; //Taking arguments from the client to connect to the server.
        
        if (args.length <=2 || args.length > 3) 
            {
            System.out.println("Insufficient or no arguments found. Please pass arguments in proper order\t 1:Input File Name \t 2:port \t 3: Number of Packets");
            } 
        else            //If the arguments are correct and in proper order then store them in proper variables. 
            {
            fname = args[0];
            port1 = args[1];
            lastS1= args[2];
            }
        int port= Integer.parseInt(port1);
        int lastSeq= Integer.parseInt(lastS1);
		
		
		int chk=0,packet_count=0;
		file(fname);
		
		// Sequence number of the last packet sent (rcvbase)
		int lSent = 0;//edit
		
		// Sequence number of the last acked packet
		int waAck = 0;//edit

		// Data to be sent (you can, and should, use your own Data-> byte[] function here)
		byte[] fileBytes = new byte[MSS];
		System.out.println("Data size: " + fileBytes.length + " bytes");

		
		int prob_count = 0;
		int sendBase=0;
		int ack_count=0;
		boolean chk_flag=false;
		
		System.out.println("Number of packets to send: " + lastSeq);

		DatagramSocket toReceiver = new DatagramSocket();

		// Receiver address
		InetAddress receiverAddress = InetAddress.getByName("localhost");
		
		// List of all the packets sent
		ArrayList<RDTPacket> sent = new ArrayList<RDTPacket>();

		int[] acks = new int[lastSeq];
		while(true){

			
			while(lSent < sendBase+W_SIZE && lSent < lastSeq){
				// Array to store part of the bytes to send
				//byte[] filePacketBytes = new byte[MSS];
				String msg="SELECTIVE REPEAT SENDER ...........................................................................................................................................................................................................................................................................................................................................................................................................................................................................................";
				byte[] filePacketBytes = new byte[MSS];
				filePacketBytes = msg.getBytes();
				//new Random().nextBytes(filePacketBytes);
				chk=ChecksumMethod.generateChecksum(msg);
				System.out.println("The checksum generated is = "+ chk);
	
				
				if(prob_count==(int)(0.1*lastSeq+2)){
					chk=chk+2;
					prob_count=0;
					chk_flag=true;
					System.out.println("++++++++++++++++++CHECKSUM HAS CHANGED+++++++++++++++++++");
				}
				
				RDTPacket rdtPacketObject = new RDTPacket(chk,lSent, filePacketBytes, (lSent == lastSeq-1) ? true : false);
				
				// Serialize the RDTPacket object
				byte[] sendData = Serializer.toBytes(rdtPacketObject);
				
				// Create the packet
				DatagramPacket packet = new DatagramPacket(sendData, sendData.length, receiverAddress, port );

				// Add packet to the sent list
				if(chk_flag==true){
					// Create RDTPacket object
					RDTPacket rdtPacketObject_ce = new RDTPacket(chk-2,lSent, filePacketBytes, (lSent == lastSeq-1) ? true : false);
					
					// Serialize the RDTPacket object
					byte[] sendData_ce = Serializer.toBytes(rdtPacketObject);
					
					// Create the packet
					DatagramPacket packet_ce = new DatagramPacket(sendData, sendData.length, receiverAddress, port );
					sent.add(rdtPacketObject_ce);
					chk_flag=false;	
				}else sent.add(rdtPacketObject);
				
				// Send with some probability of loss
				prob_count++;
				if(Math.random() > PROBABILITY){
					toReceiver.send(packet);
					System.out.println(" sequence number of the packet sending" + lSent +  " and size " + sendData.length + " bytes");
				}else{
					System.out.println("[X] sequence number of packet lost " + lSent);
				}
				
				
				lSent++;
				// Increase the last sent
				
				

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
				
				if(ackObject.getPacket()>lastSeq-1) break;
					acks[ackObject.getPacket()]=1;
				
				System.out.println(" ACK is received for " + ackObject.getPacket());
				
				// If this ack is for the last packet, stop the sender (Note: gbn has a cumulative acking)
				if(ackObject.getPacket() == lastSeq){
					break;
				}
				
				waAck = Math.max(waAck, ackObject.getPacket());
				
			}catch(SocketTimeoutException e){
				// then send all the sent but non-acked packets
				
				for(int i = sendBase; i < sendBase+W_SIZE; i++){
					if(i>lastSeq-1) break;
					byte[] sendData = Serializer.toBytes(sent.get(i));

					// Create the packet
					DatagramPacket packet = new DatagramPacket(sendData, sendData.length, receiverAddress, port );
					if(acks[i]!=1){
						// Send with some probability
						if(Math.random() > PROBABILITY){
							toReceiver.send(packet);
							System.out.println(" packet is resending with sequence number " + sent.get(i).getSeq() +  " and size " + sendData.length + " bytes");
						}else{
							System.out.println("[X] packet is lost with sequence number " + sent.get(i).getSeq());
						}
					}
					
				
				}
			}
			
			
			for(int i=sendBase;i<sendBase+W_SIZE;i++){
				if(i>lastSeq-1) break;
				if(acks[i]==0)
				{
					sendBase=i;
					break;
				}
				if(i==sendBase+W_SIZE-1)
					sendBase=i+1;
			}
			
			
			ack_count=0;
			
			System.out.println("LAST SENT PACKET NUMBER = "+lSent);
			System.out.println("SEND BASE SF for the window= "+sendBase);
			System.out.println("ACKS received = ");
			for(int i=sendBase;i<sendBase+W_SIZE;i++){				
				if(i>lastSeq-1) break;
				System.out.println(acks[i]);
			}
			System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++");
		}
		
		System.out.println("The transmission is finished");

	}

}
