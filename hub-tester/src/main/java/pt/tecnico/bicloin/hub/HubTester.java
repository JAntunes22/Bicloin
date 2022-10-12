package pt.tecnico.bicloin.hub;

import pt.tecnico.bicloin.hub.grpc.Hub.*;
import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;
import pt.ulisboa.tecnico.sdis.zk.ZKRecord;
import io.grpc.StatusRuntimeException;

public class HubTester {
	
	public static void main(String[] args) {
		System.out.println(HubTester.class.getSimpleName());
		
		// receive and print arguments
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

		final String zooHost = args[0];
		final String zooPort = args[1];
		final String path = args[2];

		ZKNaming zkNaming = new ZKNaming(zooHost,zooPort);

		// lookup
		ZKRecord record;
		HubFrontend frontend;

		try {

			record = zkNaming.lookup(path);
			String target = record.getURI();
			frontend = new HubFrontend(target);

			PingRequest pingRequest = PingRequest.newBuilder().build();
			PingResponse pingResponse = frontend.ping(pingRequest);
			System.out.println(pingResponse);

			SysStatusRequest sysStatusRequest = SysStatusRequest.newBuilder().build();
			SysStatusResponse sysStatusResponse = frontend.sysStatus(sysStatusRequest);
			System.out.println(sysStatusResponse);

			frontend.shutdownChannel();
			
		} catch (ZKNamingException e) {
			System.out.println("Could not communicate with ZKnaming server!");
		} catch (StatusRuntimeException e) {
			System.out.println("Caught exception with description: " +
			e.getStatus().getDescription());
		}

		

	}
	
}
