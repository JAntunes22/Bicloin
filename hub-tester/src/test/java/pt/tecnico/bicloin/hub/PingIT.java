package pt.tecnico.bicloin.hub;

import pt.tecnico.bicloin.hub.grpc.Hub.*;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PingIT extends BaseIT{

    @Test
	public void pingOKTest() {
		PingRequest request = PingRequest.newBuilder().build();
		PingResponse response = frontend.ping(request);
		assertEquals("UP", response.getOutput());
	}

}