package pt.tecnico.rec;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import pt.ulisboa.tecnico.sdis.zk.*;

import java.io.IOException;
import java.util.HashMap;

public class RecordMain {

	public static void main(String[] args) throws IOException, InterruptedException {
		System.out.println(RecordMain.class.getSimpleName());
		
		// receive and print arguments
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

		// check arguments
		if (args.length < 1) {
			System.err.println("Argument(s) missing!");
			System.err.printf("Usage: java %s port%n", RecordMain.class.getName());
			return;
		}

		final String zooHost = args[0];
		final String zooPort = args[1];
		final String host = args[2];
		final int port = Integer.valueOf(args[3]);
		final String path = "/grpc/bicloin/rec/" + args[4];

		ZKNaming zkNaming = null;
		final BindableService impl = new ServerImpl();

		// Create a new server to listen on port
		Server server = ServerBuilder.forPort(port).addService(impl).build();

		// Start the server
		server.start();

		// Server threads are running in the background.
		System.out.println("Replica " + Character.getNumericValue(path.charAt(path.length()-1)) + " starting...");

		try {
			System.out.println("Contacting ZooKeeper at " + zooHost + ":" + zooPort + "...");
			zkNaming = new ZKNaming(zooHost, zooPort);
			System.out.println("Binding " + path + " to " + host + ":" + port + "...");

			// publish
			zkNaming.rebind(path, host, Integer.toString(port));
			System.out.println("Success");
			
			Runtime.getRuntime().addShutdownHook(new Unbind(zkNaming, path));
			// Do not exit the main thread. Wait until server is terminated.
			server.awaitTermination();

		} catch (ZKNamingException e) {
			System.out.println("Failed to communicate with Naming Server!");
			return;
		} finally  {
			
			if (zkNaming != null) {
				// remove
				try {
					System.out.println("Unbiding from zookeeper");
					zkNaming.unbind(path,host,String.valueOf(port));
				} catch (ZKNamingException e) {
					e.printStackTrace();
				}
			}		
		}	
	}

	static class Unbind extends Thread {
		private ZKNaming _zoo;
		private String _path;
	
		public Unbind (ZKNaming zKNaming, String path) {
			_zoo = zKNaming;
			_path = path;
		}
	
		@Override
		public void run() {
			try {
				_zoo.unbind(_path, null, null);
			} catch (ZKNamingException e) {
				System.out.println("Couldn't Unbind from zkNaming server!");
			}
		}
	}
}



