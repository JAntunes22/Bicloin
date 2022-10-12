package pt.tecnico.rec;

import io.grpc.stub.StreamObserver;
import io.grpc.StatusRuntimeException;

public class RecObserver<R> implements StreamObserver<R> {
    private ResponseCollector<R> responseCollector;
    private String _path;
    private String _server;
    private int _recNumber;
    private float _weight;

    public RecObserver(ResponseCollector<R> responseList, String path, String server, int recNumber, float weight) {
        responseCollector = responseList;
        _path = path;
        _server = server;
        _recNumber = recNumber;
        _weight = weight;
    }

    @Override
    public void onNext(R r) {

        // if the path is not null, it is doing a ping, which has slightly different behaviour
        if(_path != null) 
            responseCollector.addResponse(r, _weight, _path);
        else             
            responseCollector.addResponse(r, _weight);
    }

    @Override
    public void onError(Throwable throwable) {

        // ignore the error where the response from server is ignored, because the client already has enough responses for the quorum
        if (!((StatusRuntimeException) throwable).getStatus().getDescription().equals("io.grpc.Context was cancelled without error")) {
            responseCollector.addError(throwable);
            System.out.println("Received error: " + throwable + " when contacting replica " + _recNumber + " at " + _server);
        }
    }

    @Override
    public void onCompleted() {
    }
}