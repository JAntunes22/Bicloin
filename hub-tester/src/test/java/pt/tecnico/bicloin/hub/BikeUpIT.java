package pt.tecnico.bicloin.hub;

import pt.tecnico.bicloin.hub.grpc.Hub.*;

import static io.grpc.Status.INVALID_ARGUMENT;
import static io.grpc.Status.ALREADY_EXISTS;
import static io.grpc.Status.UNAVAILABLE;
import static io.grpc.Status.FAILED_PRECONDITION;
import io.grpc.StatusRuntimeException;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BikeUpIT extends BaseIT {

    @Test
    public void BikeUpTest() {

		TopUpRequest topUpRequest = TopUpRequest.newBuilder().setUserName("alice").setAmount(10).setPhoneNumber("+35191102030").build();
		frontend.topUp(topUpRequest);

		BikeUpRequest bikeUpRequest = BikeUpRequest.newBuilder().setName("alice").setStationId("ocea").setLatitude((float)38.7633).setLongitude((float)-9.0950).build();
		frontend.bikeUp(bikeUpRequest);

		InfoStationRequest infoStationRequest = InfoStationRequest.newBuilder().setStationId("ocea").build();
		InfoStationResponse infoStationResponse = frontend.infoStation(infoStationRequest);
        
		assertEquals("Oceanário", infoStationResponse.getName());
        assertEquals((float)38.7633, infoStationResponse.getLatitude());
		assertEquals((float)-9.0950, infoStationResponse.getLongitude());
		assertEquals(20, infoStationResponse.getCapacity());
		assertEquals(2, infoStationResponse.getReward());
		assertEquals(14, infoStationResponse.getAvailableBikes());
        assertEquals(0, infoStationResponse.getStatDeposit());
		assertEquals(1, infoStationResponse.getStatWithdrawals());
    }
    
	@Test
	public void emptyUserIdBikeUpTest() {
		TopUpRequest topUpRequest = TopUpRequest.newBuilder().setUserName("alice").setAmount(10).setPhoneNumber("+35191102030").build();
		frontend.topUp(topUpRequest);

		BikeUpRequest bikeUpRequest = BikeUpRequest.newBuilder().setName("").setStationId("ocea").setLatitude((float)38.7633).setLongitude((float)-9.0950).build();

		assertEquals(
		INVALID_ARGUMENT.getCode(),
		assertThrows(
		StatusRuntimeException.class, () -> frontend.bikeUp(bikeUpRequest))
		.getStatus()
		.getCode());

		assertEquals("Unknown User Id!", 
		assertThrows(StatusRuntimeException.class, () -> frontend.bikeUp(bikeUpRequest))
		.getStatus()
		.getDescription());
	}

	@Test
	public void emptyStationIdBikeUpTest() {
		TopUpRequest topUpRequest = TopUpRequest.newBuilder().setUserName("alice").setAmount(10).setPhoneNumber("+35191102030").build();
		frontend.topUp(topUpRequest);

		BikeUpRequest bikeUpRequest = BikeUpRequest.newBuilder().setName("alice").setStationId("").setLatitude((float)38.7633).setLongitude((float)-9.0950).build();

		assertEquals(
		INVALID_ARGUMENT.getCode(),
		assertThrows(
		StatusRuntimeException.class, () -> frontend.bikeUp(bikeUpRequest))
		.getStatus()
		.getCode());

		assertEquals("Unknown Station Id!", 
		assertThrows(StatusRuntimeException.class, () -> frontend.bikeUp(bikeUpRequest))
		.getStatus()
		.getDescription());
	}

	@Test
	public void userHasBikeTest() {
		TopUpRequest topUpRequest = TopUpRequest.newBuilder().setUserName("alice").setAmount(20).setPhoneNumber("+35191102030").build();
		frontend.topUp(topUpRequest);

		BikeUpRequest bikeUpRequest = BikeUpRequest.newBuilder().setName("alice").setStationId("ocea").setLatitude((float)38.7633).setLongitude((float)-9.0950).build();
		frontend.bikeUp(bikeUpRequest);

		BikeUpRequest bikeUpRequest2 = BikeUpRequest.newBuilder().setName("alice").setStationId("ocea").setLatitude((float)38.7633).setLongitude((float)-9.0950).build();

		assertEquals(
		ALREADY_EXISTS.getCode(),
		assertThrows(
		StatusRuntimeException.class, () -> frontend.bikeUp(bikeUpRequest2))
		.getStatus()
		.getCode());

		assertEquals("User already has a bike!", 
		assertThrows(StatusRuntimeException.class, () -> frontend.bikeUp(bikeUpRequest2))
		.getStatus()
		.getDescription());
	}

	@Test
	public void insufficientBalanceTest() {
		
		BikeUpRequest bikeUpRequest = BikeUpRequest.newBuilder().setName("alice").setStationId("ocea").setLatitude((float)38.7633).setLongitude((float)-9.0950).build();

		assertEquals(
		FAILED_PRECONDITION.getCode(),
		assertThrows(
		StatusRuntimeException.class, () -> frontend.bikeUp(bikeUpRequest))
		.getStatus()
		.getCode());

		assertEquals("Insufficient balance to bike up, necessary to top up!", 
		assertThrows(StatusRuntimeException.class, () -> frontend.bikeUp(bikeUpRequest))
		.getStatus()
		.getDescription());
	}

	@Test
	public void unreachableStationTest() {
		TopUpRequest topUpRequest = TopUpRequest.newBuilder().setUserName("alice").setAmount(10).setPhoneNumber("+35191102030").build();
		frontend.topUp(topUpRequest);
		
		BikeUpRequest bikeUpRequest = BikeUpRequest.newBuilder().setName("alice").setStationId("ocea").setLatitude((float)30.7633).setLongitude((float)-9.0950).build();

		assertEquals(
		UNAVAILABLE.getCode(),
		assertThrows(
		StatusRuntimeException.class, () -> frontend.bikeUp(bikeUpRequest))
		.getStatus()
		.getCode());

		assertEquals("This station is unreachable (more than 200 meters)!", 
		assertThrows(StatusRuntimeException.class, () -> frontend.bikeUp(bikeUpRequest))
		.getStatus()
		.getDescription());
	}

	@Test
	public void noBikeAvailableTest() {
		TopUpRequest topUpRequest = TopUpRequest.newBuilder().setUserName("alice").setAmount(10).setPhoneNumber("+35191102030").build();
		frontend.topUp(topUpRequest);
		
		BikeUpRequest bikeUpRequest = BikeUpRequest.newBuilder().setName("alice").setStationId("cate").setLatitude((float)38.7097).setLongitude((float) -9.1336).build();

		assertEquals(
		UNAVAILABLE.getCode(),
		assertThrows(
		StatusRuntimeException.class, () -> frontend.bikeUp(bikeUpRequest))
		.getStatus()
		.getCode());

		assertEquals("No bikes availbale in this station!", 
		assertThrows(StatusRuntimeException.class, () -> frontend.bikeUp(bikeUpRequest))
		.getStatus()
		.getDescription());
	}

	@Test
	public void invalidCoordinatesTest() {
		TopUpRequest topUpRequest = TopUpRequest.newBuilder().setUserName("alice").setAmount(10).setPhoneNumber("+35191102030").build();
		frontend.topUp(topUpRequest);
		
		BikeUpRequest bikeUpRequest = BikeUpRequest.newBuilder().setName("alice").setStationId("ocea").setLatitude((float)91.7097).setLongitude((float) -181.1336).build();

		assertEquals(
		INVALID_ARGUMENT.getCode(),
		assertThrows(
		StatusRuntimeException.class, () -> frontend.bikeUp(bikeUpRequest))
		.getStatus()
		.getCode());

		assertEquals("The coordinates are invalid!", 
		assertThrows(StatusRuntimeException.class, () -> frontend.bikeUp(bikeUpRequest))
		.getStatus()
		.getDescription());
	}

	@AfterEach
	public void cleanup() {
		ResetUserRequest resetAlice = ResetUserRequest.newBuilder().setId("alice").build();
		frontend.resetUser(resetAlice);
		ResetStationRequest resetOcea = ResetStationRequest.newBuilder().setId("ocea").build();
		frontend.resetStation(resetOcea);
		ResetStationRequest resetCate = ResetStationRequest.newBuilder().setId("cate").build();
		frontend.resetStation(resetCate);
	}
    
}
