import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class Initiator {
	public Initiator() throws IOException {
		Server s1 = new Server(6000, this);
		Server s2 = new Server(6669, this);
		try {
			FileOutputStream fileOut = new FileOutputStream(new File("counter.txt"));
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			
			int [] j = {0,0};

			out.writeObject(j);
			out.close();
			fileOut.close();
		} catch (IOException i) {
			i.printStackTrace();
		}
		s1.start();
		s2.start();
	}

	public static void main(String[] args) throws IOException {

		Initiator initiator = new Initiator();
	}

}
