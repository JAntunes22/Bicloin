package pt.tecnico.bicloin.hub;

import pt.tecnico.bicloin.hub.grpc.Hub.*;

import static io.grpc.Status.INVALID_ARGUMENT;
import io.grpc.StatusRuntimeException;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

public class LocateStationIT extends BaseIT{

    @Test
    public void locateOneStationTest() {

		LocateStationRequest locateStationRequest = LocateStationRequest.newBuilder().setLatitude((float)38.7633).setLongitude((float)-9.0950).setK(1).build();
		LocateStationResponse locateStationResponse = frontend.locateStation(locateStationRequest);
        
        List<String> station_list = locateStationResponse.getStationIdList();
		assertEquals("ocea", station_list.get(0));
	}

	@Test
	public void locateThreeStationsTest() {

		LocateStationRequest locateStationRequest = LocateStationRequest.newBuilder().setLatitude((float)38.7376).setLongitude((float)-9.3031).setK(3).build();
		LocateStationResponse locateStationResponse = frontend.locateStation(locateStationRequest);
        
        List<String> station_list = locateStationResponse.getStationIdList();
		assertEquals("istt", station_list.get(0));
		assertEquals("stao", station_list.get(1));
		assertEquals("jero", station_list.get(2));
		
	}

	@Test
	public void invalidCoordinatesTest() {
		LocateStationRequest locateStationRequest = LocateStationRequest.newBuilder().setLatitude((float)198.7633).setLongitude((float)-199.0950).setK(1).build();
		assertEquals(
		INVALID_ARGUMENT.getCode(),
		assertThrows(
		StatusRuntimeException.class, () -> frontend.locateStation(locateStationRequest))
		.getStatus()
		.getCode());

		assertEquals("The coordinates are invalid!", 
		assertThrows(StatusRuntimeException.class, () -> frontend.locateStation(locateStationRequest))
		.getStatus()
		.getDescription());
	}

	@Test
	public void invalidKNumberTest() {
		LocateStationRequest locateStationRequest = LocateStationRequest.newBuilder().setLatitude((float)38.7633).setLongitude((float)-9.0950).setK(0).build();
		assertEquals(
		INVALID_ARGUMENT.getCode(),
		assertThrows(
		StatusRuntimeException.class, () -> frontend.locateStation(locateStationRequest))
		.getStatus()
		.getCode());

		assertEquals("Invalid number of near stations!", 
		assertThrows(StatusRuntimeException.class, () -> frontend.locateStation(locateStationRequest))
		.getStatus()
		.getDescription());
	}
}
