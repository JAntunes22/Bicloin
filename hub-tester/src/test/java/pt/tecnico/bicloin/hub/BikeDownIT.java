package pt.tecnico.bicloin.hub;

import pt.tecnico.bicloin.hub.grpc.Hub.*;

import static io.grpc.Status.INVALID_ARGUMENT;
import static io.grpc.Status.UNAVAILABLE;
import io.grpc.StatusRuntimeException;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BikeDownIT extends BaseIT {

    @Test
    public void BikeDownTest() {
		TopUpRequest topUpRequest = TopUpRequest.newBuilder().setUserName("alice").setAmount(10).setPhoneNumber("+35191102030").build();
		frontend.topUp(topUpRequest);

		BikeUpRequest bikeUpRequest = BikeUpRequest.newBuilder().setName("alice").setStationId("ocea").setLatitude((float)38.7633).setLongitude((float)-9.0950).build();
		frontend.bikeUp(bikeUpRequest);

		BikeDownRequest bikeDownRequest = BikeDownRequest.newBuilder().setName("alice").setStationId("ocea").setLatitude((float)38.7633).setLongitude((float)-9.0950).build();
		frontend.bikeDown(bikeDownRequest);

		InfoStationRequest infoStationRequest = InfoStationRequest.newBuilder().setStationId("ocea").build();
		InfoStationResponse infoStationResponse = frontend.infoStation(infoStationRequest);
        
		assertEquals("Oceanário", infoStationResponse.getName());
        assertEquals((float)38.7633, infoStationResponse.getLatitude());
		assertEquals((float)-9.0950, infoStationResponse.getLongitude());
		assertEquals(20, infoStationResponse.getCapacity());
		assertEquals(2, infoStationResponse.getReward());
		assertEquals(15, infoStationResponse.getAvailableBikes());
        assertEquals(1, infoStationResponse.getStatDeposit());
		assertEquals(1, infoStationResponse.getStatWithdrawals());
    }
    
	@Test
	public void emptyUserIdBikeUpTest() {
		TopUpRequest topUpRequest = TopUpRequest.newBuilder().setUserName("alice").setAmount(10).setPhoneNumber("+35191102030").build();
		frontend.topUp(topUpRequest);

		BikeUpRequest bikeUpRequest = BikeUpRequest.newBuilder().setName("alice").setStationId("ocea").setLatitude((float)38.7633).setLongitude((float)-9.0950).build();
		frontend.bikeUp(bikeUpRequest);

		BikeDownRequest bikeDownRequest = BikeDownRequest.newBuilder().setName("").setStationId("ocea").setLatitude((float)38.7633).setLongitude((float)-9.0950).build();

		assertEquals(
		INVALID_ARGUMENT.getCode(),
		assertThrows(
		StatusRuntimeException.class, () -> frontend.bikeDown(bikeDownRequest))
		.getStatus()
		.getCode());

		assertEquals("Unknown User Id!", 
		assertThrows(StatusRuntimeException.class, () -> frontend.bikeDown(bikeDownRequest))
		.getStatus()
		.getDescription());
	}

	@Test
	public void emptyStationIdBikeUpTest() {
		TopUpRequest topUpRequest = TopUpRequest.newBuilder().setUserName("alice").setAmount(10).setPhoneNumber("+35191102030").build();
		frontend.topUp(topUpRequest);

		BikeUpRequest bikeUpRequest = BikeUpRequest.newBuilder().setName("alice").setStationId("ocea").setLatitude((float)38.7633).setLongitude((float)-9.0950).build();
		frontend.bikeUp(bikeUpRequest);

		BikeDownRequest bikeDownRequest = BikeDownRequest.newBuilder().setName("alice").setStationId("").setLatitude((float)38.7633).setLongitude((float)-9.0950).build();

		assertEquals(
		INVALID_ARGUMENT.getCode(),
		assertThrows(
		StatusRuntimeException.class, () -> frontend.bikeDown(bikeDownRequest))
		.getStatus()
		.getCode());

		assertEquals("Unknown Station Id!", 
		assertThrows(StatusRuntimeException.class, () -> frontend.bikeDown(bikeDownRequest))
		.getStatus()
		.getDescription());
	}

	@Test
	public void userHasNoBikeTest() {
		TopUpRequest topUpRequest = TopUpRequest.newBuilder().setUserName("alice").setAmount(10).setPhoneNumber("+35191102030").build();
		frontend.topUp(topUpRequest);

		BikeDownRequest bikeDownRequest = BikeDownRequest.newBuilder().setName("alice").setStationId("ocea").setLatitude((float)38.7633).setLongitude((float)-9.0950).build();

		assertEquals(
		UNAVAILABLE.getCode(),
		assertThrows(
		StatusRuntimeException.class, () -> frontend.bikeDown(bikeDownRequest))
		.getStatus()
		.getCode());

		assertEquals("User doesn't have a bike to bike down!", 
		assertThrows(StatusRuntimeException.class, () -> frontend.bikeDown(bikeDownRequest))
		.getStatus()
		.getDescription());
	}

	@Test
	public void unreachableStationTest() {
		TopUpRequest topUpRequest = TopUpRequest.newBuilder().setUserName("alice").setAmount(10).setPhoneNumber("+35191102030").build();
		frontend.topUp(topUpRequest);

		BikeUpRequest bikeUpRequest = BikeUpRequest.newBuilder().setName("alice").setStationId("ocea").setLatitude((float)38.7633).setLongitude((float)-9.0950).build();
		frontend.bikeUp(bikeUpRequest);

		BikeDownRequest bikeDownRequest = BikeDownRequest.newBuilder().setName("alice").setStationId("ocea").setLatitude((float)30.7633).setLongitude((float)-9.0950).build();

		assertEquals(
		UNAVAILABLE.getCode(),
		assertThrows(
		StatusRuntimeException.class, () -> frontend.bikeDown(bikeDownRequest))
		.getStatus()
		.getCode());

		assertEquals("This station is unreachable (more than 200 meters)!", 
		assertThrows(StatusRuntimeException.class, () -> frontend.bikeDown(bikeDownRequest))
		.getStatus()
		.getDescription());
	}

	@Test
	public void fullStationTest() {
		TopUpRequest topUpRequest = TopUpRequest.newBuilder().setUserName("alice").setAmount(10).setPhoneNumber("+35191102030").build();
		frontend.topUp(topUpRequest);

		BikeUpRequest bikeUpRequest = BikeUpRequest.newBuilder().setName("alice").setStationId("ocea").setLatitude((float)38.7633).setLongitude((float)-9.0950).build();
		frontend.bikeUp(bikeUpRequest);

		BikeDownRequest bikeDownRequest = BikeDownRequest.newBuilder().setName("alice").setStationId("gulb").setLatitude((float)38.7376).setLongitude((float)-9.1545).build();

		assertEquals(
		UNAVAILABLE.getCode(),
		assertThrows(
		StatusRuntimeException.class, () -> frontend.bikeDown(bikeDownRequest))
		.getStatus()
		.getCode());

		assertEquals("This station is full, no docks available!", 
		assertThrows(StatusRuntimeException.class, () -> frontend.bikeDown(bikeDownRequest))
		.getStatus()
		.getDescription());
	}

	@Test
	public void invalidCoordinatesTest() {
		TopUpRequest topUpRequest = TopUpRequest.newBuilder().setUserName("alice").setAmount(10).setPhoneNumber("+35191102030").build();
		frontend.topUp(topUpRequest);

		BikeDownRequest bikeDownRequest = BikeDownRequest.newBuilder().setName("alice").setStationId("ocea").setLatitude((float)90.7376).setLongitude((float)-190.1545).build();

		assertEquals(
		INVALID_ARGUMENT.getCode(),
		assertThrows(
		StatusRuntimeException.class, () -> frontend.bikeDown(bikeDownRequest))
		.getStatus()
		.getCode());

		assertEquals("The coordinates are invalid!", 
		assertThrows(StatusRuntimeException.class, () -> frontend.bikeDown(bikeDownRequest))
		.getStatus()
		.getDescription());
	}

	@AfterEach
	public void cleanup() {
		ResetUserRequest resetAlice = ResetUserRequest.newBuilder().setId("alice").build();
		frontend.resetUser(resetAlice);
		ResetStationRequest resetOcea = ResetStationRequest.newBuilder().setId("gulb").build();
		frontend.resetStation(resetOcea);
		ResetStationRequest resetCate = ResetStationRequest.newBuilder().setId("ocea").build();
		frontend.resetStation(resetCate);
	}
    
}
