package pt.tecnico.bicloin.app;

import java.util.Scanner;

//import io.grpc.ManagedChannel;
//import io.grpc.ManagedChannelBuilder;
import pt.tecnico.bicloin.hub.HubFrontend;
import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;
import pt.ulisboa.tecnico.sdis.zk.ZKRecord;
import org.apache.commons.lang3.time.StopWatch;

import java.lang.NumberFormatException;

public class AppMain {

	private static StopWatch timer = new StopWatch();
	
	public static void main(String[] args) {
		System.out.println(AppMain.class.getSimpleName());
		
		// receive and print arguments
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

		if (args.length < 5) {
			System.err.println("Argument(s) missing!");
			return;
		}

		final String zooHost = args[0];
		final String zooPort = args[1];
		final String user = args[2];
		final String phone = args[3];
		final float latitude = Float.parseFloat(args[4]);
		final float longitude = Float.parseFloat(args[5]);

		ZKRecord node = null;
		try {
			System.out.println("Contacting ZooKeeper at " + zooHost + ":" + zooPort + "...");

			ZKNaming zkNaming = new ZKNaming(zooHost, zooPort);

			node = zkNaming.lookup("/grpc/bicloin/hub/1");

		} catch (ZKNamingException e) {
			System.out.println("Failed to communicate with zookeeper!");
		}

		HubFrontend hub_frontend = new HubFrontend(node.getURI());

		App app = new App(hub_frontend, user, phone, latitude, longitude);

		// If it receives SIGINT signal, shutdown hub frontend
		Runtime.getRuntime().addShutdownHook(new Unbind(app));

		System.out.printf("App running fine%n");

		String procedure = " ";

		timer.start();
		
		try (Scanner scanner = new Scanner(System.in)) {
			while(true) {

				System.out.print("> ");
				System.out.flush();

				if (!scanner.hasNextLine()) {
					break;
				}

				procedure = scanner.nextLine();
				
				//ignore comments
				if (!procedure.isEmpty() && procedure.charAt(0) == '#'){
					continue;
				}
				args = procedure.split(" ");

				try {
					switch (args[0]) {
						case "balance":
							if (args.length != 1) {
								System.out.println("Wrong input format - argument number");
							} else { 
								app.balance();
							}
							break;

						case "top-up":
							if (args.length != 2) {
								System.out.println("Wrong input format - argument number");
							} else {
								app.topUp(Integer.parseInt(args[1]));
							}
							break;

						case "tag":
							if (args.length != 4) {
								System.out.println("Wrong input format - argument number");
							} else if (Float.parseFloat(args[1]) < -90 || Float.parseFloat(args[1]) > 90
										|| Float.parseFloat(args[2]) < -180 || Float.parseFloat(args[2]) > 180){
								System.out.println("Wrong input format - invalid coordinates");
							} else {
								app.tag(Float.parseFloat(args[1]), Float.parseFloat(args[2]), args[3]);
							}
							break;

						case "move":
							if (args.length != 2 && args.length != 3) {
								System.out.println("Wrong input format - argument number");
							} else if (args.length == 2){
								app.move(args[1]);
							} else if (Float.parseFloat(args[1]) < -90 || Float.parseFloat(args[1]) > 90
										|| Float.parseFloat(args[2]) < -180 || Float.parseFloat(args[2]) > 180){
								System.out.println("Wrong input format - invalid coordinates");
							} else {
								app.move(Float.parseFloat(args[1]), Float.parseFloat(args[2]));
							}
							break;

						case "at":
							if (args.length != 1) {
								System.out.println("Wrong input format - argument number");
							} else {
								app.at();
							}
							break;

						case "scan":
							if (args.length != 2) {
								System.out.println("Wrong input format - argument number");
							} else {
								app.scan(Integer.parseInt(args[1]));
							}
							break;

						case "info":
							if (args.length != 2) {
								System.out.println("Wrong input format - argument number");
							} else {
								app.info(args[1]);
							}
							break;

						case "bike-up":
							if (args.length != 2) {
								System.out.println("Wrong input format - argument number");
							} else {
								app.bikeUp(args[1]);
							}
							break;

						case "bike-down":
							if (args.length != 2) {
								System.out.println("Wrong input format - argument number");
							} else {
								app.bikeDown(args[1]);
							}
							break;

						case "ping":
							app.ping();
							break;
						
						case "sys_status":
							app.sysStatus();
							break;
						
						case "zzz":
							if (args.length != 2) {
								System.out.println("Wrong input format - argument number");
							} else {
								try {
									Thread.sleep(Integer.parseInt(args[1]));
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
							break;
						
						case "help":
						if (args.length != 1) {
							System.out.println("Wrong input format - argument number");
						} else {
							app.help();
						}
						break;
							
						default:
							System.out.println("Command not found");
							break;
					}
				} catch (NumberFormatException e) {
					System.out.println("Wrong input format in one of the arguments");
				}
			}
			app.shutDownFrontend();
			timer.suspend();
			System.out.println("Time: " + timer.toString());
			timer.stop();
		}
	}

	static class Unbind extends Thread {
		private App _app;

		public Unbind (App app) {
			_app = app;
		}

		@Override
        public void run() {
			_app.shutDownFrontend();
		}
	}
	
}
