import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ByteStringInputStream extends DataInputStream {

	public ByteStringInputStream(InputStream in) {
		super(in);
	}
	
	public String readOneLine() throws IOException {
		StringBuilder builder = new StringBuilder();
		int b = 0;
		
		while (true) {
			b = this.in.read();
			if (b == -1 || b == '\n') {
				break;
			} else if (b == '\r') {
				int temp = this.in.read();
				if (temp != '\n') {
					builder.append(Character.toString((char) b));
					builder.append(Character.toString((char) temp));
				} else {
					break;
				}
			} else {
				builder.append(Character.toString((char) b));
			}
		}
		
		return builder.toString();
	}
}