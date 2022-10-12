package pt.tecnico.rec;

import io.grpc.stub.StreamObserver;
import pt.tecnico.rec.grpc.*;
import pt.tecnico.rec.grpc.Rec.*;
import pt.ulisboa.tecnico.sdis.zk.*;

import java.util.Collection;

import io.grpc.StatusRuntimeException;

public class RecordTester {
	
	public static void main(String[] args) {
		System.out.println(RecordTester.class.getSimpleName());
		
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
		AsyncRecFrontend asyncFrontend;
	
		try {

			asyncFrontend = new AsyncRecFrontend();

			Collection<ZKRecord> nodes = zkNaming.listRecords("/grpc/bicloin/rec");
			asyncFrontend = new AsyncRecFrontend();
			for (ZKRecord node: nodes) {
				asyncFrontend.addServer(node.getURI(), node.getPath());
			}
			asyncFrontend.weight();
			System.out.println(asyncFrontend.ping());

			asyncFrontend.shutdownChannel();
			//asyncFrontend.shutdownChannel();
		} catch (StatusRuntimeException e) {
			System.out.println("Caught exception with description: " +
			e.getStatus().getDescription());
		} catch (ZKNamingException e) {
			System.out.println("Could not communicate with ZKnaming server!");
		}
			

	}
	
}
