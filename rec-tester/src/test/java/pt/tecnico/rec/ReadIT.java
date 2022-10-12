package pt.tecnico.rec;

import pt.tecnico.rec.grpc.Rec.*;

import static io.grpc.Status.INVALID_ARGUMENT;
import io.grpc.StatusRuntimeException;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ReadIT extends BaseIT {

    @Test
    public void ReadTest() {
		WriteRequest writeRequest = WriteRequest.newBuilder().setId("adeus").setValue(2).build();
		frontend.write(writeRequest);

        ReadRequest readRequest = ReadRequest.newBuilder().setId("adeus").build();
        ReadResponse readResponse = frontend.read(readRequest);

		assertEquals(2, readResponse.getValue());
	}

	@Test
	public void emptyReadTest() {
		ReadRequest readRequest = ReadRequest.newBuilder().setId("").build();
		assertEquals(
		INVALID_ARGUMENT.getCode(),
		assertThrows(
		StatusRuntimeException.class, () -> frontend.read(readRequest))
		.getStatus()
		.getCode());
	}
    
}
