import java.io.Serializable;
import java.util.Arrays;


public class RDTPacket implements Serializable {

	public int chk;
	public int seq;
	
	public byte[] data;
	
	public boolean last;

	public RDTPacket(int chk,int seq, byte[] data, boolean last) {
		super();
		this.chk = chk;
		this.seq = seq;
		this.data = data;
		this.last = last;
	}

	public int getChk() {
		return chk;
	}
	
	public void setChk(int chk1) {
		this.chk = chk1;
	}
	
	public int getSeq() {
		return seq;
	}

	public void setSeq(int seq) {
		this.seq = seq;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public boolean isLast() {
		return last;
	}

	public void setLast(boolean last) {
		this.last = last;
	}

	@Override
	public String toString() {
		return "UDPPacket [checksum=" + chk + "seq=" + seq + ", data=" + (data)
				+ ", last=" + last + "]";
	}
	
}
