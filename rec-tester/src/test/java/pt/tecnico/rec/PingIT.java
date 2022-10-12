package pt.tecnico.rec;

import pt.tecnico.rec.grpc.Rec.*;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PingIT extends BaseIT {

    @Test
	public void pingOKTest() {
		PingRequest request = PingRequest.newBuilder().build();
		PingResponse response = frontend.ping(request);
		assertEquals("UP", response.getOutput());
	}

}