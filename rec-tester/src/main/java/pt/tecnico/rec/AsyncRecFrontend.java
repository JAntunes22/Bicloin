package pt.tecnico.rec;

import io.grpc.stub.StreamObserver;
import pt.tecnico.rec.*;
import pt.tecnico.rec.grpc.*;
import pt.tecnico.rec.grpc.Rec.*;
//import pt.tecnico.rec.grpc.RecordServiceGrpc.RecordServiceBlockingStub;
import pt.tecnico.rec.grpc.RecordServiceGrpc.RecordServiceStub;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.Status;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import org.apache.commons.lang.time.StopWatch;

public class AsyncRecFrontend {

    private ArrayList<ManagedChannel> channels = new ArrayList<ManagedChannel>();
    private ArrayList<RecordServiceStub> stubs = new ArrayList<RecordServiceStub>();
    private ArrayList<String> paths = new ArrayList<String>();
    private ArrayList<String> servers = new ArrayList<String>();
    private ArrayList<Float> weights = new ArrayList<Float>();
    private float totalWeight;

    private int nReads = 0;
    private int nWrites = 0;

    //stop watch to be used in weight calculation
    private static StopWatch timer = new StopWatch();

    public AsyncRecFrontend() {
        
    }

    public void addServer(String target, String path) {
        ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        channels.add(channel);
		stubs.add(RecordServiceGrpc.newStub(channel));
        paths.add(path);
        servers.add(target);
    }

    // function responsible to calculate weights for all records taking in count the time it takes to ping them
    public void weight() {
        ArrayList<Float> times = new ArrayList<Float>();
        PingRequest request = PingRequest.newBuilder().build();
        float totalTimes = 0;

        // for loop to first connect all the stubs and avoid a possible disparity in the times
        for (int i = 0; i < paths.size(); i++) {
            ResponseCollector<PingResponse> list0 = new ResponseCollector<PingResponse>(1, 1);
            int recNumber0 = Character.getNumericValue(paths.get(i).charAt(paths.get(i).length()-1));
            RecObserver<PingResponse> recObserver0 = new RecObserver<PingResponse>(list0, paths.get(i), servers.get(i), recNumber0, 1);
            stubs.get(i).withDeadlineAfter((long) 1500, TimeUnit.MILLISECONDS).ping(request, recObserver0);

            try {
                synchronized(list0) {
                    while(!list0.verify()) {
                        list0.wait(200);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // for loop that records the times and attribute the weights
        for (int i = 0; i < paths.size(); i++) {
            
            ResponseCollector<PingResponse> list = new ResponseCollector<PingResponse>(1, 1);
            int recNumber = Character.getNumericValue(paths.get(i).charAt(paths.get(i).length()-1));
            RecObserver<PingResponse> recObserver = new RecObserver<PingResponse>(list, paths.get(i), servers.get(i), recNumber, 1);
            
            //timer to count the send ans response time for the ping
            timer.start();

            stubs.get(i).withDeadlineAfter((long) 1500, TimeUnit.MILLISECONDS).ping(request, recObserver);

            try {
                synchronized(list) {
                    while(!list.verify()) {
                        list.wait(200);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            timer.suspend();

            float time = (float)timer.getTime();
            times.add(time);
            totalTimes += time;
            timer.stop();
            timer.reset();
        }

        // weight calculation
        float timeAvg = totalTimes/times.size();
        float upperDeviation = timeAvg + (float)0.2*timeAvg;
        float lowerDeviation = timeAvg - (float)0.2*timeAvg;
        totalWeight = 0;

        // assigns the fixed values of 0.5, 1 and 2 to the recs concerning their response time
        for (int i = 0; i < times.size(); i++) {
            if (times.get(i) > upperDeviation) {
                weights.add((float)0.5);
                totalWeight += 0.5;
            } else if (times.get(i) < lowerDeviation) {
                weights.add((float)2);
                totalWeight += 2;
            } else {
                weights.add((float)1);
                totalWeight += 1;
            }
        }
        System.out.println("Weights: " + weights.toString());
    }
    
    public HashMap<String, Boolean> ping() {
        HashMap<String, Boolean> status = new HashMap<String, Boolean>();
        ResponseCollector<PingResponse> list = new ResponseCollector<PingResponse>(totalWeight, paths.size());
        int i = 0;
        PingRequest request = PingRequest.newBuilder().build();

        for (RecordServiceStub s: stubs) {
            //get Rec number from its path
            int recNumber = Character.getNumericValue(paths.get(i).charAt(paths.get(i).length()- 1));
            RecObserver<PingResponse> recObserver = new RecObserver<PingResponse>(list, paths.get(i), servers.get(i), recNumber, weights.get(i));
            s.withDeadlineAfter((long) 1500, TimeUnit.MILLISECONDS).ping(request, recObserver);
            System.out.println("Contacting replica " + recNumber + " at " + paths.get(i) + ", sending ping");
            i++;
        }
        
        try {
            synchronized(list){
                while(!list.verify()) {
                    list.wait(1000);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (String p: paths) {
            // if received OK response from rec, set to UP
            if (list.getResponsesPath().containsKey(p)) {
                status.put(p, true);
            } else {
                // else, set do DOWN
                status.put(p, false);
            }
        }
        System.out.println("Received all pings");
          
        return status;
    }

    public int read(String id) {
        // optimisation: need less weight from reads
        float quorum = (float)((totalWeight + 0.5)/3.0);
        int total = paths.size();
        int maxTag = -1;
        int maxCid = 0;
        int correctResponse = -1;
        int i = 0;

        CopyOnWriteArrayList<ReadResponse> responses;
        ResponseCollector<ReadResponse> list = new ResponseCollector<ReadResponse>(quorum, total);
        
        // Request a read from all rec servers
        for (RecordServiceStub s: stubs) {
            ReadRequest request = ReadRequest.newBuilder().setId(id).build();
            
            //get Rec number from its path
            int recNumber = Character.getNumericValue(paths.get(i).charAt(paths.get(i).length()- 1));

            RecObserver<ReadResponse> recObserver = new RecObserver<ReadResponse>(list, null, servers.get(i), recNumber, weights.get(i));
            s.withDeadlineAfter((long) 3000, TimeUnit.MILLISECONDS).read(request, recObserver);
            System.out.println("Contacting replica " + recNumber + " at " + paths.get(i) + ", reading of \"" + id + "\"" );
            nReads++;
            i++;
        }

        // Wait until it received enough responses, or there's no more responses to wait for 
        try {
            synchronized(list) {
                while(!list.verify()) {
                    list.wait(1000);
               }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        System.out.println("Read: Received " + list.getCount() + " answers");
        
        // Did not receive enough OK responses, send error...
        if (list.getCount() == 0) {
            CopyOnWriteArrayList<Throwable> errors = list.getErrors();

            // If at least one exception has the description "Invalid ID", it means that ID isn't valid, but the server is still up
            if (errors.stream().anyMatch(e -> ((StatusRuntimeException) e).getStatus().getDescription().equals("Invalid ID"))) {
                System.out.println("Rec didn't find id " + id);
            } else {
                System.out.println("Too many Recs are not responding!");
            }

            throw Status.INVALID_ARGUMENT.withDescription("io exception").asRuntimeException();
        }

        responses = list.getResponses();
        
        for (ReadResponse response: responses) {
            // The correct, up-to-date value is the one with the biggest tag
            if (response.getTag() > maxTag || (response.getTag() == maxTag && response.getCid() > maxCid)) {
                maxTag = response.getTag();
                maxCid = response.getCid();
                correctResponse = response.getValue();
            } 
        }
        System.out.println("Read from " + id + " the value \"" + correctResponse + "\" and tag \"" + maxTag + "\"");
        
        return correctResponse;
    }

    public void write(String id, int value, int cid) {
        // optimisation: need less weight from reads, more from writes
        float quorum = (float)((totalWeight + 0.5)*(2.0/3.0));
        float quorumRead = (float)((totalWeight + 0.5)/3.0);
        int total = paths.size();
        int maxTag = -1;
        int i = 0;
        CopyOnWriteArrayList<ReadResponse> responses;

        ResponseCollector<ReadResponse> readList = new ResponseCollector<ReadResponse>(quorumRead, total);

        // Request a read from all rec servers to get biggest tag      
        for (RecordServiceStub s: stubs) {
            ReadRequest readRequest = ReadRequest.newBuilder().setId(id).build();

            //get Rec number from its path
            int recNumber = Character.getNumericValue(paths.get(i).charAt(paths.get(i).length()- 1));

            RecObserver<ReadResponse> recObserver = new RecObserver<ReadResponse>(readList, null, servers.get(i), recNumber, weights.get(i));
            s.withDeadlineAfter((long) 3000, TimeUnit.MILLISECONDS).read(readRequest, recObserver);
            nReads++;
            System.out.println("Contacting replica " + recNumber + " at " + paths.get(i) + ", reading of \"" + id + "\" to write next" );
            i++;

        }

        // Wait until it received enough responses, or there's no more responses to wait for 
        try {
            synchronized(readList) {
                while(!readList.verify()) {
                    readList.wait(1000);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Read: Received " + readList.getCount() + " answers");

        // Did not receive enough OK responses, send error...
        if (readList.getCount() == 0) {
            CopyOnWriteArrayList<Throwable> errors = readList.getErrors();

            if (!errors.stream().anyMatch(e -> ((StatusRuntimeException) e).getStatus().getDescription().equals("Invalid ID"))) {
                System.out.println("Too many Recs are not responding!");
            }

            throw Status.INVALID_ARGUMENT.withDescription("io exception").asRuntimeException();
        }

        // get the correct, max Tag
        responses = readList.getResponses();
        for (ReadResponse response: responses) {
            if (response.getTag() > maxTag) {
                maxTag = response.getTag();
            } 
        }
        
        System.out.println("Read from " + id + " with tag \"" + maxTag + "\"");

        maxTag++;
        ResponseCollector<WriteResponse> writeList = new ResponseCollector<WriteResponse>(quorum, paths.size());
        WriteRequest writeRequest = WriteRequest.newBuilder().setId(id).setValue(value).setTag(maxTag).setCid(cid).build();

        i = 0;
        for (RecordServiceStub s: stubs) {
            //get Rec number from its path
            int recNumber = Character.getNumericValue(paths.get(i).charAt(paths.get(i).length()- 1));
            
            RecObserver<WriteResponse> recObserver = new RecObserver<WriteResponse>(writeList, null, servers.get(i), recNumber, weights.get(i));
            s.withDeadlineAfter((long) 3000, TimeUnit.MILLISECONDS).write(writeRequest, recObserver);
            System.out.println("Contacting replica " + recNumber + " at " + paths.get(i) + ", writing \"" + value + "\" on \"" + id + "\" with tag \"" + (maxTag) + "\"");
            nWrites++;
            i++;
        }

        try {
            synchronized(writeList) {
                while(!writeList.verify()) {
                    writeList.wait(1000);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Write: Received " + writeList.getCount() + " answers");
        if (writeList.getCount() == 0) {
            System.out.println("All Recs are down!");
            throw Status.INVALID_ARGUMENT.withDescription("io exception").asRuntimeException();
        }
    }

    public void shutdownChannel() {
        for(ManagedChannel c: channels){
            c.shutdownNow();
        }
        System.out.println("Reads: " + nReads);
        System.out.println("Writes: " + nWrites);
        System.out.println("Racio: " + (nReads/(float)nWrites));
    }
}