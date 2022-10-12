package pt.tecnico.bicloin.hub;

import pt.tecnico.bicloin.hub.grpc.Hub.*;

import static io.grpc.Status.INVALID_ARGUMENT;
import io.grpc.StatusRuntimeException;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TopUpIT extends BaseIT {

    @Test
    public void TopUpTest() {

		TopUpRequest topUpRequest = TopUpRequest.newBuilder().setUserName("bruno").setAmount(10).setPhoneNumber("+35193334444").build();
		TopUpResponse topUpResponse = frontend.topUp(topUpRequest);
        
		assertEquals(100, topUpResponse.getBalance());
	}

	@Test
	public void emptyTopUpTest() {
		TopUpRequest topUpRequest = TopUpRequest.newBuilder().setUserName("").setAmount(10).setPhoneNumber("+35193334444").build();
		
		assertEquals(
		INVALID_ARGUMENT.getCode(),
		assertThrows(
		StatusRuntimeException.class, () -> frontend.topUp(topUpRequest))
		.getStatus()
		.getCode());

		assertEquals("Unknown User Id!", 
		assertThrows(StatusRuntimeException.class, () -> frontend.topUp(topUpRequest))
		.getStatus()
		.getDescription());
	}

	@Test
	public void invalidIdTopUpTest() {
		TopUpRequest topUpRequest = TopUpRequest.newBuilder().setUserName("joaquim").setAmount(10).setPhoneNumber("+35193334444").build();
		assertEquals(
		INVALID_ARGUMENT.getCode(),
		assertThrows(
		StatusRuntimeException.class, () -> frontend.topUp(topUpRequest))
		.getStatus()
		.getCode());

		assertEquals("Unknown User Id!", 
		assertThrows(StatusRuntimeException.class, () -> frontend.topUp(topUpRequest))
		.getStatus()
		.getDescription());
	}

	@Test
	public void invalidTopUpAmmountTest() {
		TopUpRequest topUpRequest = TopUpRequest.newBuilder().setUserName("bruno").setAmount(21).setPhoneNumber("+35193334444").build();
		assertEquals(
		INVALID_ARGUMENT.getCode(),
		assertThrows(
		StatusRuntimeException.class, () -> frontend.topUp(topUpRequest))
		.getStatus()
		.getCode());

		assertEquals("Invalid amount, must be between 1 and 20, inclusive!", 
		assertThrows(StatusRuntimeException.class, () -> frontend.topUp(topUpRequest))
		.getStatus()
		.getDescription());
	}

	@Test
	public void incorrectPhoneNumberTest() {
		TopUpRequest topUpRequest = TopUpRequest.newBuilder().setUserName("bruno").setAmount(15).setPhoneNumber("+35193334440").build();
		assertEquals(
		INVALID_ARGUMENT.getCode(),
		assertThrows(
		StatusRuntimeException.class, () -> frontend.topUp(topUpRequest))
		.getStatus()
		.getCode());

		assertEquals("Incorrect Phone number!", 
		assertThrows(StatusRuntimeException.class, () -> frontend.topUp(topUpRequest))
		.getStatus()
		.getDescription());
	}

	@AfterEach
	public void cleanup() {
		ResetUserRequest resetAlice = ResetUserRequest.newBuilder().setId("bruno").build();
		frontend.resetUser(resetAlice);
	}
       
}
