package pt.tecnico.bicloin.hub;

import pt.tecnico.bicloin.hub.grpc.Hub.*;

import static io.grpc.Status.INVALID_ARGUMENT;
import io.grpc.StatusRuntimeException;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class InfoStationIT extends BaseIT {

    @Test
    public void InfoStationTest() {

		InfoStationRequest infoStationRequest = InfoStationRequest.newBuilder().setStationId("ocea").build();
		InfoStationResponse infoStationResponse = frontend.infoStation(infoStationRequest);
        
		assertEquals("Oceanário", infoStationResponse.getName());
        assertEquals((float)38.7633, infoStationResponse.getLatitude());
		assertEquals((float)-9.0950, infoStationResponse.getLongitude());
		assertEquals(20, infoStationResponse.getCapacity());
		assertEquals(2, infoStationResponse.getReward());
		assertEquals(15, infoStationResponse.getAvailableBikes());
        assertEquals(0, infoStationResponse.getStatDeposit());
		assertEquals(0, infoStationResponse.getStatWithdrawals());
	}

	@Test
	public void emptyStationTest() {
		InfoStationRequest infoStationRequest = InfoStationRequest.newBuilder().setStationId("").build();

		assertEquals(
		INVALID_ARGUMENT.getCode(),
		assertThrows(
		StatusRuntimeException.class, () -> frontend.infoStation(infoStationRequest))
		.getStatus()
		.getCode());

		assertEquals("Unknown Station Id!", 
		assertThrows(StatusRuntimeException.class, () -> frontend.infoStation(infoStationRequest))
		.getStatus()
		.getDescription());
	}

	@Test
	public void unknownStationTest() {
		InfoStationRequest infoStationRequest = InfoStationRequest.newBuilder().setStationId("joaquim").build();
		
		assertEquals(
		INVALID_ARGUMENT.getCode(),
		assertThrows(
		StatusRuntimeException.class, () -> frontend.infoStation(infoStationRequest))
		.getStatus()
		.getCode());

		assertEquals("Unknown Station Id!", 
		assertThrows(StatusRuntimeException.class, () -> frontend.infoStation(infoStationRequest))
		.getStatus()
		.getDescription());
	}
       
}
