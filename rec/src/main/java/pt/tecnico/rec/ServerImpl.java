package pt.tecnico.rec;

import io.grpc.stub.StreamObserver;
import pt.tecnico.rec.grpc.*;
import pt.tecnico.rec.grpc.Rec.*;

import io.grpc.Status;
import io.grpc.Context;
import java.util.ArrayList;


public class ServerImpl extends RecordServiceGrpc.RecordServiceImplBase {

	Record _record = new Record();
    
    @Override
    public void ping(PingRequest request, StreamObserver<PingResponse> responseObserver) {

		if (Context.current().isCancelled()) {
			responseObserver.onError(Status.CANCELLED.withDescription("Cancelled by client").asRuntimeException());
			return;
		}

		String output = "UP";

		PingResponse response = PingResponse.newBuilder().
		setOutput(output).build();
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

	@Override
	public void read(ReadRequest request, StreamObserver<ReadResponse> responseObserver) {

		if (Context.current().isCancelled()) {
			responseObserver.onError(Status.CANCELLED.withDescription("Cancelled by client").asRuntimeException());
			return;
		}

		if (request.getId() == null || request.getId().isEmpty()) {
			responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Invalid ID").asRuntimeException());
			return;
		}
		
		try {
			ArrayList<Integer> value = _record.readRecord(request.getId());
			System.out.println("Read with id " + request.getId() + ", value " + value.get(0) + ", tag " + value.get(1) + ", cid " + value.get(2));

			ReadResponse response = ReadResponse.newBuilder().
									setValue(value.get(0)).setTag(value.get(1)).setCid(value.get(2)).build();
			responseObserver.onNext(response);
			responseObserver.onCompleted();

		} catch (NullPointerException e) {
			System.out.println("Didn't find value for id " + request.getId());

			ReadResponse response = ReadResponse.newBuilder().
									setValue(-1).setTag(0).setCid(0).build();
			responseObserver.onNext(response);
			responseObserver.onCompleted();		
		}
	}

	@Override
	public void write(WriteRequest request, StreamObserver<WriteResponse> responseObserver) {

		if (Context.current().isCancelled()) {
			responseObserver.onError(Status.CANCELLED.withDescription("Cancelled by client").asRuntimeException());
			return;
		}

		if (request.getId() == null || request.getId().isEmpty()) {
			responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Invalid ID").asRuntimeException());
			return;
		}

		_record.writeRecord(request.getId(), request.getValue(), request.getTag(), request.getCid());
		System.out.println("Wrote record with id " + request.getId() + ", value " + request.getValue() + ", tag " + request.getTag() + ", cid " + request.getCid());
		WriteResponse response = WriteResponse.newBuilder().build();
		responseObserver.onNext(response);
		responseObserver.onCompleted();
			
	}
}