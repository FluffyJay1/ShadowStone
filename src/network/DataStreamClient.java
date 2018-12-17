package network;

import java.io.*;
import java.net.*;

public class DataStreamClient extends DataStream {

	public DataStreamClient(String ip, int port) throws UnknownHostException, IOException {
		super(ip, port);
	}

}
