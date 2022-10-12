package pt.tecnico.bicloin.hub;

import io.grpc.Context;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import pt.tecnico.bicloin.hub.exceptions.*;
import pt.tecnico.bicloin.hub.grpc.HubServiceGrpc;
import pt.tecnico.bicloin.hub.grpc.Hub.*;
import pt.tecnico.bicloin.hub.grpc.Hub.LocateStationResponse.Builder;

import java.util.HashMap;
import java.util.List;

import static io.grpc.Status.*;

public class HubServiceImpl extends HubServiceGrpc.HubServiceImplBase {

	private Hub _hub;

	public HubServiceImpl(Hub hub) {
		_hub = hub;
	}

	@Override
	public void resetUser(ResetUserRequest request, StreamObserver<ResetUserResponse> responseObserver) {

		// first, check if client hasn't cancelled the request
		if (Context.current().isCancelled()) {
			responseObserver.onError(Status.CANCELLED.withDescription("Cancelled by client").asRuntimeException());
			return;
		}
		
		try {
			_hub.resetUser(request.getId());

			ResetUserResponse response = ResetUserResponse.newBuilder().build();
			responseObserver.onNext(response);
			responseObserver.onCompleted();

		} catch (FailedRecCommunicationException e) {
			responseObserver.onError(UNAVAILABLE.withDescription(e.getMessage()).asRuntimeException());
		}
	}

	@Override
	public void resetStation(ResetStationRequest request, StreamObserver<ResetStationResponse> responseObserver) {

  		// first, check if client hasn't cancelled the request
		if (Context.current().isCancelled()) {
			responseObserver.onError(Status.CANCELLED.withDescription("Cancelled by client").asRuntimeException());
			return;
		}

		try {
			_hub.resetStation(request.getId());

			ResetStationResponse response = ResetStationResponse.newBuilder().build();
			responseObserver.onNext(response);
			responseObserver.onCompleted();

		}  catch (FailedRecCommunicationException e) {
			responseObserver.onError(UNAVAILABLE
			.withDescription(e.getMessage()).asRuntimeException());
		}
	}

	@Override
	public void balance(BalanceRequest request, StreamObserver<BalanceResponse> responseObserver) {

  		// first, check if client hasn't cancelled the request
		if (Context.current().isCancelled()) {
			responseObserver.onError(Status.CANCELLED.withDescription("Cancelled by client").asRuntimeException());
			return;
		}
		
		try {
			int balance = _hub.getUserBalance(request.getName());
			BalanceResponse response = BalanceResponse.newBuilder().
								setBalance(balance).build();
			responseObserver.onNext(response);
			responseObserver.onCompleted();
			
		} catch (FailedRecCommunicationException e) {
			responseObserver.onError(UNAVAILABLE
			.withDescription(e.getMessage()).asRuntimeException());
		} catch (UnknownUserIdException e) {
			responseObserver.onError(INVALID_ARGUMENT
			.withDescription(e.getMessage()).asRuntimeException());
		}
	}

	@Override
	public void topUp(TopUpRequest request, StreamObserver<TopUpResponse> responseObserver) {

  		// first, check if client hasn't cancelled the request
		if (Context.current().isCancelled()) {
			responseObserver.onError(Status.CANCELLED.withDescription("Cancelled by client").asRuntimeException());
			return;
		}

		try {
			int balance = _hub.topUp(request.getUserName(), request.getAmount(), request.getPhoneNumber());
			TopUpResponse response = TopUpResponse.newBuilder().
			setBalance(balance).build();
			responseObserver.onNext(response);
			responseObserver.onCompleted();

		// if something goes wrong, send an exception to the client with the proper description
		} catch (FailedRecCommunicationException e) {
			responseObserver.onError(UNAVAILABLE
			.withDescription(e.getMessage()).asRuntimeException());
		} catch (UnknownUserIdException e) {
			responseObserver.onError(INVALID_ARGUMENT
			.withDescription(e.getMessage()).asRuntimeException());
		} catch (InvalidTopUpAmmountException e) {
			responseObserver.onError(INVALID_ARGUMENT
			.withDescription(e.getMessage()).asRuntimeException());
		} catch (IncorrectPhoneNumberException e) {
			responseObserver.onError(INVALID_ARGUMENT
			.withDescription(e.getMessage()).asRuntimeException());
		}
	}

	@Override
	public void bikeUp(BikeUpRequest request, StreamObserver<BikeUpResponse> responseObserver) {

  		// first, check if client hasn't cancelled the request
		if (Context.current().isCancelled()) {
			responseObserver.onError(Status.CANCELLED.withDescription("Cancelled by client").asRuntimeException());
			return;
		}

		try {
			_hub.bikeUp(request.getName(), request.getStationId(), request.getLongitude(), request.getLatitude());
			BikeUpResponse response = BikeUpResponse.newBuilder().build();
			responseObserver.onNext(response);
			responseObserver.onCompleted();
		
		// if something goes wrong, send an exception to the client with the proper description
		} 	catch (FailedRecCommunicationException e) {
			responseObserver.onError(UNAVAILABLE
			.withDescription(e.getMessage()).asRuntimeException());
		}	catch (UnknownUserIdException e) {
			responseObserver.onError(INVALID_ARGUMENT
			.withDescription(e.getMessage()).asRuntimeException());
		} 	catch (UnknownStationIdException e) {
			responseObserver.onError(INVALID_ARGUMENT
			.withDescription(e.getMessage()).asRuntimeException());
		}  	catch (HasBikeException e) {
			responseObserver.onError(ALREADY_EXISTS
			.withDescription(e.getMessage()).asRuntimeException());
		}	catch (InsufficientBalanceException e) {
			responseObserver.onError(FAILED_PRECONDITION
			.withDescription(e.getMessage()).asRuntimeException());
		}	catch (UnreachableStationException e) {
			responseObserver.onError(UNAVAILABLE
			.withDescription(e.getMessage()).asRuntimeException());
		}	catch (NoBikeAvailableException e) {
			responseObserver.onError(UNAVAILABLE
			.withDescription(e.getMessage()).asRuntimeException());
		} catch (InvalidCoordinatesException e) {
			responseObserver.onError(INVALID_ARGUMENT
			.withDescription(e.getMessage()).asRuntimeException());
		}

	}

	@Override
	public void bikeDown(BikeDownRequest request, StreamObserver<BikeDownResponse> responseObserver) {

  		// first, check if client hasn't cancelled the request
		if (Context.current().isCancelled()) {
			responseObserver.onError(Status.CANCELLED.withDescription("Cancelled by client").asRuntimeException());
			return;
		}
		
		try {
			_hub.bikeDown(request.getName(), request.getStationId(), request.getLongitude(), request.getLatitude());
			BikeDownResponse response = BikeDownResponse.newBuilder().build();
			responseObserver.onNext(response);
			responseObserver.onCompleted();

		// if something goes wrong, send an exception to the client with the proper description
		} 	catch (FailedRecCommunicationException e) {
			responseObserver.onError(UNAVAILABLE
			.withDescription(e.getMessage()).asRuntimeException());
		}	catch (UnknownUserIdException e) {
			responseObserver.onError(INVALID_ARGUMENT
			.withDescription(e.getMessage()).asRuntimeException());
		}  catch (HasNoBikeException e) {
			responseObserver.onError(UNAVAILABLE
			.withDescription(e.getMessage()).asRuntimeException());
		}	catch (UnreachableStationException e) {
			responseObserver.onError(UNAVAILABLE
			.withDescription(e.getMessage()).asRuntimeException());
		}	catch (FullStationException e) {
			responseObserver.onError(UNAVAILABLE
			.withDescription(e.getMessage()).asRuntimeException());
		} catch (UnknownStationIdException e) {
			responseObserver.onError(INVALID_ARGUMENT
			.withDescription(e.getMessage()).asRuntimeException());
		} catch (InvalidCoordinatesException e) {
			responseObserver.onError(INVALID_ARGUMENT
			.withDescription(e.getMessage()).asRuntimeException());
		}
		
	}

	@Override
	public void infoStation(InfoStationRequest request, StreamObserver<InfoStationResponse> responseObserver){

  		// first, check if client hasn't cancelled the request
		if (Context.current().isCancelled()) {
			responseObserver.onError(Status.CANCELLED.withDescription("Cancelled by client").asRuntimeException());
			return;
		}

		String stationID = request.getStationId();

		try {
			_hub.verifyStation(stationID);

			InfoStationResponse response = InfoStationResponse.newBuilder()
										.setAvailableBikes(_hub.readRecord("station/" + stationID + "/availablebikes"))
										.setStatDeposit(_hub.readRecord("station/" + stationID + "/statdeposit"))
										.setStatWithdrawals(_hub.readRecord("station/" + stationID + "/statwithdrawals"))
										.setName(_hub.getStationName(stationID))
										.setCapacity(_hub.getStationCapacity(stationID))
										.setReward(_hub.getStationAward(stationID))
										.setLatitude(_hub.getStationLatitude(stationID))
										.setLongitude(_hub.getStationLongitude(stationID))
										.build();


			responseObserver.onNext(response);
			responseObserver.onCompleted();

		// if something goes wrong, send an exception to the client with the proper description	
		} catch (FailedRecCommunicationException e) {
			responseObserver.onError(UNAVAILABLE
			.withDescription(e.getMessage()).asRuntimeException());
		} catch (UnknownStationIdException e) {
			responseObserver.onError(INVALID_ARGUMENT
			.withDescription(e.getMessage()).asRuntimeException());
		}

	}

	@Override
	public void locateStation(LocateStationRequest request, StreamObserver<LocateStationResponse> responseObserver) {

  		// first, check if client hasn't cancelled the request
		if (Context.current().isCancelled()) {
			responseObserver.onError(Status.CANCELLED.withDescription("Cancelled by client").asRuntimeException());
			return;
		}

		try {
			List<String> stations = _hub.locateStation(request.getLatitude(), request.getLongitude(), request.getK());

			Builder responseBuilder = LocateStationResponse.newBuilder();
			for(String s: stations) {
				responseBuilder.addStationId(s);
			}

			LocateStationResponse response = responseBuilder.build();

			responseObserver.onNext(response);
			responseObserver.onCompleted();

		// if something goes wrong, send an exception to the client with the proper description
		} catch (InvalidCoordinatesException e) {
			responseObserver.onError(INVALID_ARGUMENT
			.withDescription(e.getMessage()).asRuntimeException());
		} catch (InvalidKNumberException e) {
			responseObserver.onError(INVALID_ARGUMENT
			.withDescription(e.getMessage()).asRuntimeException());
		}
	}

    @Override
    public void sysStatus(SysStatusRequest request, StreamObserver<SysStatusResponse> responseObserver) {

		// first, check if client hasn't cancelled the request
		if (Context.current().isCancelled()) {
			responseObserver.onError(Status.CANCELLED.withDescription("Cancelled by client").asRuntimeException());
			return;
		}
		
		HashMap<String, Boolean> status = _hub.sysStatus();
		SysStatusResponse.Builder responseBuilder = SysStatusResponse.newBuilder();

		// Send to client a list of SysStatusResponse(path, status)
		for (HashMap.Entry<String, Boolean> entry : status.entrySet()) {
			String path = entry.getKey();
			Boolean value = entry.getValue();

			responseBuilder.addOutput(SysStatusResponse.Status.newBuilder().
			setPath(path).setUp(value).build());
		}

		SysStatusResponse response = responseBuilder.build();

		responseObserver.onNext(response);
		responseObserver.onCompleted();
		
    }

    @Override
    public void ping(PingRequest request, StreamObserver<PingResponse> responseObserver) {

  		// first, check if client hasn't cancelled the request
		if (Context.current().isCancelled()) {
			responseObserver.onError(Status.CANCELLED.withDescription("Cancelled by client").asRuntimeException());
			return;
		}

		String output = "UP";

		PingResponse response = PingResponse.newBuilder().setOutput(output).build();
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}
}
