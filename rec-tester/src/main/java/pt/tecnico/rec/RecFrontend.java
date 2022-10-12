package pt.tecnico.rec;

import io.grpc.stub.StreamObserver;
import pt.tecnico.rec.*;
import pt.tecnico.rec.grpc.*;
import pt.tecnico.rec.grpc.Rec.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.concurrent.TimeUnit;
import java.util.Scanner;

public class RecFrontend {

    private RecordServiceGrpc.RecordServiceBlockingStub stub;
    private final ManagedChannel channel;

    public RecFrontend(String target) {

		channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();

		stub = RecordServiceGrpc.newBlockingStub(channel);
    }    

    public PingResponse ping(PingRequest request) {
        return stub.withDeadlineAfter((long) 3000, TimeUnit.MILLISECONDS).ping(request);
    }

    public ReadResponse read(ReadRequest request) {
        return stub.withDeadlineAfter((long) 3000, TimeUnit.MILLISECONDS).read(request);
    }

    public WriteResponse write(WriteRequest request) {
        return stub.withDeadlineAfter((long) 3000, TimeUnit.MILLISECONDS).write(request);
    }

    public void shutdownChannel() {
        channel.shutdownNow();
    }
}