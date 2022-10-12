package pt.tecnico.bicloin.hub;

//import io.grpc.stub.StreamObserver;
//import pt.tecnico.bicloin.hub.*;
//import pt.tecnico.bicloin.hub.grpc.*;
import pt.tecnico.bicloin.hub.grpc.HubServiceGrpc;
import pt.tecnico.bicloin.hub.grpc.Hub.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

//import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class HubFrontend {

    private HubServiceGrpc.HubServiceBlockingStub stub;
    private final ManagedChannel channel;

    public HubFrontend(String target) {

		channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();

	    stub = HubServiceGrpc.newBlockingStub(channel);

    }    

    public PingResponse ping(PingRequest request) {
        return stub.withDeadlineAfter((long) 3000, TimeUnit.MILLISECONDS).ping(request);
    }

    public ResetUserResponse resetUser(ResetUserRequest request) {
        return stub.withDeadlineAfter((long) 3000, TimeUnit.MILLISECONDS).resetUser(request);
    }

    public ResetStationResponse resetStation(ResetStationRequest request) {
        return stub.withDeadlineAfter((long) 3000, TimeUnit.MILLISECONDS).resetStation(request);
    }

    public BalanceResponse balance(BalanceRequest request) {
        return stub.withDeadlineAfter((long) 3000, TimeUnit.MILLISECONDS).balance(request);
    }

    public TopUpResponse topUp(TopUpRequest request) {
        return stub.withDeadlineAfter((long) 3000, TimeUnit.MILLISECONDS).topUp(request);
    }

    public InfoStationResponse infoStation(InfoStationRequest request) {
        return stub.withDeadlineAfter((long) 3000, TimeUnit.MILLISECONDS).infoStation(request);
    }

    public LocateStationResponse locateStation(LocateStationRequest request) {
        return stub.withDeadlineAfter((long) 3000, TimeUnit.MILLISECONDS).locateStation(request);
    }

    public BikeUpResponse bikeUp(BikeUpRequest request) {
        return stub.withDeadlineAfter((long) 3000, TimeUnit.MILLISECONDS).bikeUp(request);
    }

    public BikeDownResponse bikeDown(BikeDownRequest request) {
        return stub.withDeadlineAfter((long) 3000, TimeUnit.MILLISECONDS).bikeDown(request);
    }

    public SysStatusResponse sysStatus(SysStatusRequest request) {
        return stub.withDeadlineAfter((long) 3000, TimeUnit.MILLISECONDS).sysStatus(request);
    }

    public void shutdownChannel() {
        channel.shutdownNow();
    }
}