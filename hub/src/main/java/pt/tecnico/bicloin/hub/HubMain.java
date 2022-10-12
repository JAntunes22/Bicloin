package pt.tecnico.bicloin.hub;

import java.io.*;
import java.io.IOException;
import java.util.Collection;
import java.util.Scanner;  

import pt.tecnico.bicloin.hub.exceptions.*;
import pt.tecnico.rec.AsyncRecFrontend;
import pt.tecnico.rec.grpc.Rec.*;
import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;
import pt.ulisboa.tecnico.sdis.zk.ZKRecord;
import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;

public class HubMain {

	public static void main(String[] args) throws IOException, InterruptedException {
		System.out.println(HubMain.class.getSimpleName());

		// receive and print arguments
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}
	
		final String zooHost = args[0];
		final String zooPort = args[1];
		final String rec_path = "/grpc/bicloin/rec/";
		final String hub_host = args[2];
		final int hub_port = Integer.parseInt(args[3]);
		final String hub_path = "/grpc/bicloin/hub/"  + args[4];
		final int cid = Integer.parseInt(args[4]);

		ZKNaming zkNaming = null;
		AsyncRecFrontend recFrontend = null;

		try {
			System.out.println("Contacting ZooKeeper at " + zooHost + ":" + zooPort + "...");

			zkNaming = new ZKNaming(zooHost, zooPort);
			System.out.println("Binding " + hub_path + " to " + hub_host + ":" + hub_port + "...");
			zkNaming.rebind(hub_path, hub_host, Integer.toString(hub_port));
			System.out.println("Success");

			// Get list of all Zoo Records of rec
			Collection<ZKRecord> nodes = zkNaming.listRecords("/grpc/bicloin/rec");
			recFrontend = new AsyncRecFrontend();
			for (ZKRecord node: nodes) {
				recFrontend.addServer(node.getURI(), node.getPath());
			}
			// calculate weight each rec server
			recFrontend.weight();

		} catch (ZKNamingException e) {
			System.out.println("Failed to communicate with zookeeper!");
		}

		System.out.println(recFrontend.ping());
		Hub _hub = new Hub(recFrontend, hub_path, cid);

		// if users.csv and initRec in argumments, register those users
		if (args.length > 5) {
			Scanner scanner = new Scanner(new File(args[5]));  

			while (scanner.hasNext()) {  
				String user = scanner.nextLine();
				String[] user_args = user.split(",");

				_hub.addUser(user_args[0], user_args[1], user_args[2]); 

				if (args.length > 7 && args[7].equals("initRec")) {
					try {
						_hub.addUserRec(user_args[0]);
					} catch (FailedRecCommunicationException e) {
						System.out.println("While creating hub wasn't able to access rec");
					}
				}
			}   
			scanner.close();
		}

		// if stations.csv and initRec in argumments, register those stations
		if (args.length > 6) {
			Scanner scanner = new Scanner(new File(args[6]));  

			while (scanner.hasNext()) {  
				String station = scanner.nextLine();
				String[] station_args = station.split(",");

				_hub.addStation(station_args[0], station_args[1], Float.parseFloat(station_args[2]), Float.parseFloat(station_args[3]), Integer.parseInt(station_args[4]), Integer.parseInt(station_args[5]), Integer.parseInt(station_args[6]));

				if (args.length > 7 && args[7].equals("initRec")) {
					try {
						_hub.addStationRec(station_args[1], Integer.parseInt(station_args[5]));
					} catch (FailedRecCommunicationException e) {
						//Hub will run but rec won't be full with all the initial records
						System.out.println("While creating hub wasn't able to access rec");
					}
				}
			}   
			scanner.close(); 
		}

		// receive SIGINT signal and handle it.
		// Unbinds from ZooKeeper and closes frontend channel
		Runtime.getRuntime().addShutdownHook(new Unbind(_hub, zkNaming, hub_path));

		final BindableService impl = (BindableService) new HubServiceImpl(_hub);
		
		// Create a new server to listen on port
		Server server = ServerBuilder.forPort(hub_port).addService(impl).build();

		// Start the server
		server.start();

		// Server threads are running in the background.
		System.out.println("Server started");

		// Do not exit the main thread. Wait until server is terminated.
		server.awaitTermination();
		
	}

	static class Unbind extends Thread {
		private Hub _hub;
		private ZKNaming _zoo;
		private String _path;

		public Unbind (Hub hub, ZKNaming zKNaming, String path) {
			_hub = hub;
			_zoo = zKNaming;
			_path = path;
		}

		@Override
        public void run() {
			_hub.shutDownFrontend();
			try {
				_zoo.unbind(_path, null, null);
			} catch (ZKNamingException e) {
				System.out.println("Couldn't Unbind from zkNaming server!");
			}
		}
	}
}