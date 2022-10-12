package pt.tecnico.bicloin.hub;

import pt.tecnico.bicloin.hub.grpc.Hub.*;

import static io.grpc.Status.INVALID_ARGUMENT;
import io.grpc.StatusRuntimeException;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BalanceIT extends BaseIT {

    @Test
    public void BalanceTest() {

		BalanceRequest balanceRequest = BalanceRequest.newBuilder().setName("alice").build();
		BalanceResponse balanceResponse = frontend.balance(balanceRequest);
        
		assertEquals(0, balanceResponse.getBalance());
	}

	@Test
	public void emptyIdBalanceTest() {
		BalanceRequest balanceRequest = BalanceRequest.newBuilder().setName("").build();
		assertEquals(
		INVALID_ARGUMENT.getCode(),
		assertThrows(
		StatusRuntimeException.class, () -> frontend.balance(balanceRequest))
		.getStatus()
		.getCode());

		assertEquals("Unknown User Id!", 
		assertThrows(StatusRuntimeException.class, () -> frontend.balance(balanceRequest))
		.getStatus()
		.getDescription());
	}

	@Test
	public void invalidIdBalanceTest() {
		BalanceRequest balanceRequest = BalanceRequest.newBuilder().setName("joaquim").build();
		assertEquals(
		INVALID_ARGUMENT.getCode(),
		assertThrows(
		StatusRuntimeException.class, () -> frontend.balance(balanceRequest))
		.getStatus()
		.getCode());

		assertEquals("Unknown User Id!", 
		assertThrows(StatusRuntimeException.class, () -> frontend.balance(balanceRequest))
		.getStatus()
		.getDescription());
	}

	@AfterEach
	public void cleanup() {
		ResetUserRequest resetAlice = ResetUserRequest.newBuilder().setId("alice").build();
		frontend.resetUser(resetAlice);
	}
    
}
