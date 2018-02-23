import java.io.Serializable;


public class RDTAck implements Serializable{
	
	private int packet;
	public boolean last;
	
	public RDTAck(int packet, boolean last) {
		super();
		this.packet = packet;
		this.last = last;
	}

	public int getPacket() {
		return packet;
	}

	public void setPacket(int packet) {
		this.packet = packet;
	}
	
	public boolean isLast() {
		return last;
	}

	public void setLast(boolean last) {
		this.last = last;
	}

}
