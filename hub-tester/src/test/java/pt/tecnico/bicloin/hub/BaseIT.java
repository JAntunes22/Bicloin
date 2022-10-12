package pt.tecnico.bicloin.hub;

import java.io.IOException;
import java.util.Properties;

import org.junit.jupiter.api.*;

import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;
import pt.ulisboa.tecnico.sdis.zk.ZKRecord;


public class BaseIT {

	private static final String TEST_PROP_FILE = "/test.properties";
	protected static Properties testProps;
	static HubFrontend frontend;
	
	@BeforeAll
	public static void oneTimeSetup () throws IOException {
		testProps = new Properties();
		
		try {
			testProps.load(BaseIT.class.getResourceAsStream(TEST_PROP_FILE));
			System.out.println("Test properties:");
			System.out.println(testProps);
		}catch (IOException e) {
			final String msg = String.format("Could not load properties file {}", TEST_PROP_FILE);
			System.out.println(msg);
			throw e;
		}

		try {
			final String zooHost = testProps.getProperty("zoo.host");
			final String zooPort = testProps.getProperty("zoo.port");
			final String path = testProps.getProperty("hub.path");

			ZKNaming zkNaming = new ZKNaming(zooHost,zooPort);
			ZKRecord node = zkNaming.lookup(path);

			frontend = new HubFrontend(node.getURI());

		} catch (ZKNamingException e) {
			e.printStackTrace();
		}

	}

	@AfterAll
	public static void shutDownChannel(){
		frontend.shutdownChannel();
	}

}
