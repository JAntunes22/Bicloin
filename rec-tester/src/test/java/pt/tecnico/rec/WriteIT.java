package pt.tecnico.rec;

import pt.tecnico.rec.grpc.Rec.*;

import static io.grpc.Status.INVALID_ARGUMENT;
import io.grpc.StatusRuntimeException;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class WriteIT extends BaseIT {

    @Test
	public void WriteTest() {
		WriteRequest writeRequest = WriteRequest.newBuilder().setId("ola").setValue(1).build();
		frontend.write(writeRequest);

        ReadRequest readRequest = ReadRequest.newBuilder().setId("ola").build();
        ReadResponse readResponse = frontend.read(readRequest);

		assertEquals(1, readResponse.getValue());
	}

	@Test
	public void emptyWriteTest() {
		WriteRequest writeRequest = WriteRequest.newBuilder().setId("").build();
		assertEquals(
		INVALID_ARGUMENT.getCode(),
		assertThrows(
		StatusRuntimeException.class, () -> frontend.write(writeRequest))
		.getStatus()
		.getCode());
	}
    
}
