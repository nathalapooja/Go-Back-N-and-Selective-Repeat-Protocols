import java.net.DatagramPacket;
import java.net.*;
import java.util.*;
import java.io.*;

public class Receiver {
	
	
	// Maximum Segment Size - Quantity of data from the application layer in the segment
	public static  int MSS = 500;

	// Probability of loss during packet sending
	public static  double PROBABILITY = 0.1;

	// Window size - Number of packets sent without acking
	public static  int W_SIZE = 15;//edit
	
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
            Scanner scan1 = new Scanner(scan2.nextLine());
        while (scan1.hasNext()) {
            String st = scan1.next();//edit
			file[x]=st;
			x=x+1;
            //System.out.println(s);
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
	
	
	
	public static boolean useLoop(int[] arr, int targetValue) {
		for(int s: arr){
			if(s==targetValue)
				return true;
		}
		return false;
	}

	
	
	// Probability of ACK loss
	//public static final double PROBABILITY = 0.1;

	public static void main(String[] args) throws Exception{
		
		
		
		    String fname= "", port1= "" ; //Taking arguments from the client to connect to the server.
        
        if (args.length <2 || args.length > 2) 
            {
            System.out.println("Insufficient or no arguments found. Please pass arguments in proper order\t 1:Input File Name \t 2:port \t 3: Number of Packets");
            } 
        else            //If the arguments are correct and in proper order then store them in proper variables. 
            {
            fname = args[0];
            port1 = args[1];//Integer.parseInt(args[1]);
            }
        int port= Integer.parseInt(port1);
        
		
		
		
		file(fname);
		
		DatagramSocket fromSender = new DatagramSocket(port);
		 
		byte[] receivedData = new byte[Sender.file("asd.txt") + 93];
		int[] waitingFor = new int[W_SIZE];
		for (int x=0;x<W_SIZE;x++)
			waitingFor[x]=x;
		
		int sendACK=0,lastPack=-1;// = new int[W_SIZE];
		
		int[] setACK = new int[W_SIZE];
		
		int shift=0;
		String rec_msg;//edit
		int rec_chk=0;
		boolean last=false;
		ArrayList<RDTPacket> received = new ArrayList<RDTPacket>();
		InetAddress Address = InetAddress.getByName("localhost");
		//int port=0;
		boolean end = false;
		
		while(!end){
			
			
			DatagramPacket receivedPacket = new DatagramPacket(receivedData, receivedData.length);
			fromSender.receive(receivedPacket);
			
			
			RDTPacket packet = (RDTPacket) Serializer.toObject(receivedPacket.getData());
			rec_msg="";
			for(byte b: packet.getData()){
				
				rec_msg=rec_msg+(char) b;//edit
			}
			rec_chk=ChecksumMethod.generateChecksum(rec_msg);//edit
						
			System.out.println("Packet with sequence number " + packet.getSeq() + " received (last: " + packet.isLast() + " )");
		
			if(packet.chk != rec_chk){
				System.out.println("Packet discarded (Checksum error)");
				
			}else if(useLoop(waitingFor,packet.getSeq()) && packet.isLast()){
				
				
				received.add(packet);
				last=true;
				lastPack=packet.getSeq();
				System.out.println("Last packet received");
				
				for(int z=0;z<waitingFor.length;z++)
					if(packet.getSeq()==waitingFor[z]){
						setACK[z]=packet.getSeq();break;
					}
				
			}else if(  useLoop(waitingFor,packet.getSeq())   ){
				//waitingFor++;
				received.add(packet);
				sendACK=packet.getSeq();
				System.out.println("===================================GENERATED ACK  "+sendACK);
				System.out.println("Packet stored in buffer");
				for(int z=0;z<waitingFor.length;z++)
					if(packet.getSeq()==waitingFor[z]){
						setACK[z]=1;break;
					}
				
			}else{
				System.out.println("Packet discarded (not in order)");
			}
			
			
			RDTAck ackObject = new RDTAck(sendACK,last);
			
			
			byte[] ackBytes = Serializer.toBytes(ackObject);
			
			
			DatagramPacket ackPacket = new DatagramPacket(ackBytes, ackBytes.length, receivedPacket.getAddress(), receivedPacket.getPort());
			Address=receivedPacket.getAddress();
			port=receivedPacket.getPort();
			
			
			if(Math.random() > PROBABILITY){
				fromSender.send(ackPacket);
				for(int z=0;z<waitingFor.length;z++)
					if(ackObject.getPacket()==waitingFor[z]){
						setACK[z]=1;break;
					}
				System.out.println("Sending ACK to seq " + sendACK + " with " + ackBytes.length  + " bytes");
			}else{
				System.out.println("[X] Lost ack with sequence number " + ackObject.getPacket());
				for(int z=0;z<waitingFor.length;z++)
					if(ackObject.getPacket()==waitingFor[z]){
						setACK[z]=0;break;
					}
				//setACK[ackObject.getPacket()%W_SIZE]=0;
			}
			
			
			
			for(int y=0;y<W_SIZE;y++){
				if(setACK[y]==1){
					
					shift++;
				}else
					break;
			}
			if (shift!=0){
				for(int y=0;y<W_SIZE-shift;y++){
					setACK[y]=setACK[y+shift];
					waitingFor[y]=waitingFor[y+shift];
				}
				for(int y=W_SIZE-shift;y<W_SIZE;y++){
					setACK[y]=0;
					if(y==0){
						waitingFor[y]=waitingFor[waitingFor.length-1]+1;
						y=y+1;
						setACK[y]=0;
					}
					waitingFor[y]=waitingFor[y-1]+1;
				}
				shift=0;
			}
			
			System.out.println("waitingFor==");
			for(int y=0;y<W_SIZE;y++)
				System.out.println(waitingFor[y]);
			
			System.out.println("setACK==");
			for(int y=0;y<W_SIZE;y++)
				System.out.println(setACK[y]);
			end=true;
			for(int y=0;y<setACK.length;y++){
				if(y==0 && setACK[y]==lastPack){
					break;
				}else if(setACK[y]==lastPack){
					for(int z=0;z<y;z++)
						if(setACK[z]==0){
							end=false;
							break;
						}
				}else end=false;
			}
			
			if(end==true){
			
			ackObject = new RDTAck(lastPack+1,true);
			
			
			ackBytes = Serializer.toBytes(ackObject);
			
			
			ackPacket = new DatagramPacket(ackBytes, ackBytes.length, Address, port);
			
			fromSender.send(ackPacket);
			}
		}
		
		
		
	}
	
	
}
