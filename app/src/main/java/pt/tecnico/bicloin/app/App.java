package pt.tecnico.bicloin.app;

import java.util.HashMap;
import java.util.List;

import io.grpc.StatusRuntimeException;
import pt.tecnico.bicloin.hub.HubFrontend;
import pt.tecnico.bicloin.hub.grpc.Hub.*;
import pt.tecnico.bicloin.hub.grpc.Hub.SysStatusResponse.Status;

public class App {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    private HubFrontend _hubFrontend;
    private String _user;
    private String _phone;
    private HashMap<String, Coordinates> _tags = new HashMap<String, Coordinates>();
    private Coordinates _currentLocation;

    public App(HubFrontend hubFrontend, String user, String phone, float latitude, float longitude) {
        _hubFrontend = hubFrontend;
        _user = user;
        _phone = phone;
        _tags = new HashMap<String, Coordinates>();
        _currentLocation = new Coordinates(latitude, longitude);
    }

    public void balance() {
        try {
            BalanceRequest request = BalanceRequest.newBuilder().setName(_user).build();
            BalanceResponse response = _hubFrontend.balance(request);

            System.out.println(_user + " " + response.getBalance() + " BIC");

        } catch (StatusRuntimeException e) {
            String description = e.getStatus().getDescription();
            if (description.equals("io exception") || description.startsWith("deadline")) {
                System.out.println("ERROR Failed communication with server!");
            } else if (description.equals("Failed communication with rec!")) {
                System.out.println("ERROR Internal server error");
            } else {
                System.out.println("ERROR " + description);
            }
        } 
    }

    

    public void topUp(int ammount) {

        try{
            TopUpRequest request = TopUpRequest.newBuilder().setUserName(_user).setPhoneNumber(_phone).setAmount(ammount).build();
            TopUpResponse response = _hubFrontend.topUp(request);

            System.out.println(_user + " " + response.getBalance() + " BIC");

        } catch (StatusRuntimeException e){
            String description = e.getStatus().getDescription();
            if (description.equals("io exception") || description.startsWith("deadline")) {
                System.out.println("ERROR Failed communication with server!");
            } else if (description.equals("Failed communication with rec!")) {
                System.out.println("ERROR Internal server error");
            } else {
                System.out.println("ERROR " + description);
            }
        }
    }

    public void tag(float latitude, float longitude, String tag_name) {
        _tags.put(tag_name, new Coordinates(latitude, longitude));

        System.out.println("OK");
    }

    public void move(String tag_name) {
        if (_tags.get(tag_name) == null) {
            System.out.println("ERROR Invalid tag!");
            return;
        }
        _currentLocation = _tags.get(tag_name);

        System.out.println(_user + " em " + _currentLocation.getGMapsLink());
    }

    public void move(float latitude, float longitude) {
        _currentLocation = new Coordinates(latitude, longitude);

        System.out.println(_user + " em " + _currentLocation.getGMapsLink());
    }

    public void at(){
        System.out.println(_user + " em " + _currentLocation.getGMapsLink());
    }

    public void scan(int n_closest){

        LocateStationRequest request = LocateStationRequest.newBuilder()
                                        .setLatitude(_currentLocation.getLatitude())
                                        .setLongitude(_currentLocation.getLongitude())
                                        .setK(n_closest)
                                        .build();

        try {
            LocateStationResponse response = _hubFrontend.locateStation(request);

            List<String> station_list = response.getStationIdList();

            for (String id: station_list){
                InfoStationRequest info_request = InfoStationRequest.newBuilder().setStationId(id).build();

                InfoStationResponse info_response = _hubFrontend.infoStation(info_request);

                Coordinates station_coordinates = new Coordinates(info_response.getLatitude(), info_response.getLongitude());
                int distance = _currentLocation.distance(station_coordinates);

                System.out.println(id + ", lat "
                                    + info_response.getLatitude() + ", long "
                                    + info_response.getLongitude() + ", "
                                    + info_response.getCapacity() + " docas, " +
                                    + info_response.getReward() + " BIC prémio, "
                                    + info_response.getAvailableBikes() + " bicicletas, a "
                                    + distance + " metros");
            }

        } catch (StatusRuntimeException e){
            String description = e.getStatus().getDescription();
            if (description.equals("io exception") || description.startsWith("deadline")) {
                System.out.println("ERROR Failed communication with server!");
            } else if (description.equals("Failed communication with rec!")) {
                System.out.println("ERROR Internal server error");
            } else {
                System.out.println("ERROR " + description);
            }
        }
    }

    public void info(String id){
        try {
            InfoStationRequest request = InfoStationRequest.newBuilder().setStationId(id).build();
            InfoStationResponse response = _hubFrontend.infoStation(request);

            Coordinates station_coordinates = new Coordinates(response.getLatitude(), response.getLongitude());

            System.out.println(response.getName() + ", lat "
                                + response.getLatitude() + ", long "
                                + response.getLongitude() + ", "
                                + response.getCapacity() + " docas, " +
                                + response.getReward() + " BIC prémio, "
                                + response.getAvailableBikes() + " bicicletas, "
                                + response.getStatWithdrawals() + " levantamentos, "
                                + response.getStatDeposit() + " devoluções, "
                                + station_coordinates.getGMapsLink());
        
        } catch (StatusRuntimeException e){
            String description = e.getStatus().getDescription();
            if (description.equals("io exception") || description.startsWith("deadline")) {
                System.out.println("ERROR Failed communication with server!");
            } else if (description.equals("Failed communication with rec!")) {
                System.out.println("ERROR Internal server error");
            } else {
                System.out.println("ERROR " + description);
            }
        }
    }

    
    public void bikeUp(String id){

        try {

            BikeUpRequest request = BikeUpRequest.newBuilder().setStationId(id)
                                    .setName(_user)
                                    .setLatitude(_currentLocation.getLatitude())
                                    .setLongitude(_currentLocation.getLongitude())
                                    .build();

            _hubFrontend.bikeUp(request);

            System.out.println("OK");

        } catch (StatusRuntimeException e){
            String description = e.getStatus().getDescription();
            if (description.equals("io exception") || description.startsWith("deadline")) {
                System.out.println("ERROR Failed communication with server!");
            } else if (description.equals("Failed communication with rec!")) {
                System.out.println("ERROR Internal server error");
            } else {
                System.out.println("ERROR " + description);
            }
        }
    }

    public void bikeDown(String id){

        try {

            BikeDownRequest request = BikeDownRequest.newBuilder()
                                    .setStationId(id)
                                    .setName(_user)
                                    .setLatitude(_currentLocation.getLatitude())
                                    .setLongitude(_currentLocation.getLongitude())
                                    .build();

            _hubFrontend.bikeDown(request);

            System.out.println("OK");
            
        } catch (StatusRuntimeException e){
            String description = e.getStatus().getDescription();
            if (description.equals("io exception") || description.startsWith("deadline")) {
                System.out.println("ERROR Failed communication with server!");
            } else if (description.equals("Failed communication with rec!")) {
                System.out.println("ERROR Internal server error");
            } else {
                System.out.println("ERROR " + description);
            }
        }
    }

    public void ping(){
        try {
            PingResponse response =_hubFrontend.ping(PingRequest.newBuilder().build());
            System.out.println(response.getOutput());
            
        } catch (StatusRuntimeException e){
            String description = e.getStatus().getDescription();
            if (description.equals("io exception") || description.startsWith("deadline")) {
                System.out.println("ERROR Failed communication with server!");
            }
        }
    }

    public void sysStatus() {
        try {
            
            SysStatusResponse response =_hubFrontend.sysStatus(SysStatusRequest.newBuilder().build());

            for(Status s: response.getOutputList()) {
                System.out.println("path:" + s.getPath() + ", up:" + s.getUp());
            }

        } catch (StatusRuntimeException e){
            String description = e.getStatus().getDescription();
            if (description.equals("io exception") || description.startsWith("deadline")) {
                System.out.println("ERROR Failed communication with server!");
            }
        }
    }

    public void help() {
        System.out.println(ANSI_GREEN + "*-------------------------------------- Command list --------------------------------------*" + ANSI_RESET);
        System.out.println(ANSI_YELLOW + "[balance]" + ANSI_RESET + " - shows current balance");
        System.out.println(ANSI_YELLOW + "[top-up <integer>]" + ANSI_RESET + " - puts <integer> amount of money in account");
        System.out.println(ANSI_YELLOW + "[tag <latitude> <longitude> <name>]" + ANSI_RESET + " - saves a place with a <name> at given coordinates");
        System.out.println(ANSI_YELLOW + "[move <place>] | [move <latitude> <longitude>]" + ANSI_RESET + " - moves user to <place> or given coordinates");
        System.out.println(ANSI_YELLOW + "[at]" + ANSI_RESET + " - shows google maps link to current location");
        System.out.println(ANSI_YELLOW + "[scan <integer>]" + ANSI_RESET + " - shows <integer> closest stations");
        System.out.println(ANSI_YELLOW + "[info <station>]" + ANSI_RESET + " - shows info about selected station");
        System.out.println(ANSI_YELLOW + "[bike-up <station>]" + ANSI_RESET + " - rents a bike at given station if user is in station range (200m)");
        System.out.println(ANSI_YELLOW + "[bike-down <station>]" + ANSI_RESET + " - returns bike to given station if user is in station range (200m)");
        System.out.println(ANSI_YELLOW + "[ping]" + ANSI_RESET + " - pings hub to know if it is up and running");
        System.out.println(ANSI_YELLOW + "[sys_status]" + ANSI_RESET + " - pings hub and all recs to know if they're up and running");
        System.out.println(ANSI_YELLOW + "[zzz <integer>]" + ANSI_RESET + " - puts app to sleep for <integer> miliseconds");
        System.out.println(ANSI_GREEN + "*------------------------------------------------------------------------------------------*" + ANSI_RESET);
    }


    class Coordinates {
        private float _latitude;
        private float _longitude;

        public Coordinates(float latitude, float longitude) {
            _latitude = latitude;
            _longitude = longitude;
        }

		public float getLongitude() {
            return _longitude;
        }

        public float getLatitude() {
            return _latitude;
        }

        public int distance(Coordinates c){
			
            //haversine formula
			double latDistance = Math.toRadians(_latitude - c.getLatitude());
			double lngDistance = Math.toRadians(_longitude - c.getLongitude());

			double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
				+ Math.cos(Math.toRadians(_latitude)) * Math.cos(Math.toRadians(c.getLatitude()))
				* Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);

			double d = 2 * Math.asin(Math.sqrt(a)) * 6371000;

			return (int) (Math.round(d));		
		}

        public String getGMapsLink(){
            return "https://www.google.com/maps/place/" + this.getLatitude() + "," +  this.getLongitude();
        }
    }


    public void shutDownFrontend() {
        _hubFrontend.shutdownChannel();
    }


}
