package pt.tecnico.bicloin.hub;

//import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
//import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import io.grpc.StatusRuntimeException;
import pt.tecnico.bicloin.hub.exceptions.*;
import pt.tecnico.rec.AsyncRecFrontend;
import pt.tecnico.rec.grpc.Rec.*;

public class Hub {

  	ConcurrentHashMap<String, User> _users = new ConcurrentHashMap<String, User>();
	ConcurrentHashMap<String, Station> _stations = new ConcurrentHashMap<String, Station>();
	private AsyncRecFrontend _recFrontend;
	private static final int EURtoBTCconversion = 10;
	private String _path;
	private int _cid;

	public Hub(AsyncRecFrontend recFrontend, String path, int cid){
		_recFrontend = recFrontend;
		_path = path;
		_cid = cid;
	}

  	public void addUser(String id, String name, String phone_number) {
		if (id.length() >= 3 && id.length() <= 10 && name.length() >= 3 && name.length() <= 30) {
	  		_users.put(id, new User(id, name, phone_number)); 
		} else {
			System.out.println("Add User: Wrong input format");
		}	
	}

	public void addUserRec(String id) throws FailedRecCommunicationException {
		//adds initial mutable values associated to a user to rec
		if (id.length() >= 3 && id.length() <= 10) {
			writeRecord("user/" + id + "/balance", 0);
			writeRecord("user/" + id + "/hasbike", 0);
		} else {
			System.out.println("Add User Rec: Wrong input format");
		}
	}

	public void addStation(String name, String id, float latitude, float longitude, int n_docks, int n_bikes, int award) {
		if (id.length() == 4) {
    		_stations.put(id, new Station(id, name, longitude, latitude, n_docks, n_bikes, award));
		} else {
			System.out.println("Add Station: Wrong input format");
		}
	}

	public void addStationRec(String id, int n_bikes) throws FailedRecCommunicationException {
		//adds initial mutable values associated to a station to rec
		if (id.length() == 4) {
			writeRecord("station/" + id + "/availablebikes", n_bikes);
			writeRecord("station/" + id + "/statwithdrawals", 0);
			writeRecord("station/" + id + "/statdeposit", 0);
		} else {
			System.out.println("Add Station Rec: Wrong input format");
		}
	}

	// Function made for the hub IT tests
	public void resetUser(String id) throws FailedRecCommunicationException {
		// Reset mutable values of a user
		writeRecord("user/" + id + "/balance", 0);
	  	writeRecord("user/" + id + "/hasbike", 0);
	}

	// Function made for the hub IT tests
	public void resetStation(String id) throws FailedRecCommunicationException {
		// Reset mutable values of a station
		writeRecord("station/" + id + "/availablebikes", _stations.get(id).getNBikes());
    	writeRecord("station/" + id + "/statwithdrawals", 0);
		writeRecord("station/" + id + "/statdeposit", 0);
	}

	public int getNDocks(String id) {
		return _stations.get(id).getNDocks();
	}

	public int getAward(String id) {
		return _stations.get(id).getAward();
	}

	public boolean reachableStation(String id, float longitude, float latitude) {
		Station station = _stations.get(id);
		return calculateDistance(longitude, latitude, station.getLongitude(), station.getLatitude()) < 200;
	}

  	public int readRecord(String name) throws FailedRecCommunicationException {
		try {
			return _recFrontend.read(name);	
		} catch (StatusRuntimeException e) {
			String description = e.getStatus().getDescription();
			if (description.equals("io exception")) {
				System.out.println("ERROR reading from rec");
			} else if (description.startsWith("deadline")) {
                System.out.println("ERROR reading time out");
			}
			// Failed to read from rec, exception to be handled by ServiceImpl
			throw new FailedRecCommunicationException();
		}
	}

	public void writeRecord(String name, int value) throws FailedRecCommunicationException {
		try {
			_recFrontend.write(name, value, _cid);
		} catch (StatusRuntimeException e) {
			String description = e.getStatus().getDescription();
			if (description.equals("io exception")) {
				System.out.println("ERROR writing on rec");
			} else if (description.startsWith("deadline")) {
                System.out.println("ERROR writing time out");
			}
			// Failed to write on rec, exception to be handled by ServiceImpl
			throw new FailedRecCommunicationException();
		}
	}

  	public String getStationName(String id){
		return _stations.get(id).getName();
	}

	public float getStationLongitude(String id){
		return _stations.get(id).getLongitude();
	}

	public float getStationLatitude(String id){
		return _stations.get(id).getLatitude();
	}

	public int getStationCapacity(String id){
		return _stations.get(id).getNDocks();
	}

	public int getStationAward(String id){
		return _stations.get(id).getAward();
	}

  	public User getUser(String id) throws UnknownUserIdException {
		User user = _users.get(id);

		if (user == null) 
			throw new UnknownUserIdException();

		return user;
  	}

  	public void verifyUser(String id) throws UnknownUserIdException {
		if (_users.get(id) == null) {
			throw new UnknownUserIdException();
		}
 	}

	public void verifyStation(String id) throws UnknownStationIdException {
		if (_stations.get(id) == null) {
			throw new UnknownStationIdException();
		}
	}

	public int getUserBalance(String id) throws UnknownUserIdException, FailedRecCommunicationException {

		verifyUser(id);

		int balance = readRecord("user/" + id + "/balance");

		return balance;
	}

	public synchronized int topUp(String id, int amount, String phone) throws FailedRecCommunicationException, InvalidTopUpAmmountException, UnknownUserIdException, IncorrectPhoneNumberException {

		// verify that the user exists
		verifyUser(id);

		if (amount < 1 || amount > 20) {
			throw new InvalidTopUpAmmountException();
		}

		int balance = getUserBalance(id);

		if (!phone.equals(getUser(id).getPhoneNumber())) {
			throw new IncorrectPhoneNumberException();
		}

		balance += amount * EURtoBTCconversion;

		writeRecord("user/" + id + "/balance", balance);

		return balance;
	}

	public synchronized void bikeUp(String u_id, String s_id, float longi, float lati) throws FailedRecCommunicationException, HasBikeException, InsufficientBalanceException, NoBikeAvailableException, UnknownUserIdException, UnknownStationIdException, UnreachableStationException, InvalidCoordinatesException {
		
		// verify the user, station and coordinates are valid
		verifyStation(s_id);
		verifyUser(u_id);
		verifyCoordinates(lati, longi);

		int hasBike = readRecord("user/" + u_id + "/hasbike");
		int balance = readRecord("user/" + u_id + "/balance");
		int available = readRecord("station/" + s_id + "/availablebikes");

		if (hasBike == 1) {
			throw new HasBikeException();
		}
		if (balance < 10) {
			throw new InsufficientBalanceException();
		}
		if (!reachableStation(s_id, longi, lati)) {
			throw new UnreachableStationException();
		}
		if (available == 0) {
			throw new NoBikeAvailableException();
		}

		//all conditions checked
		int withdrawal = readRecord("station/" + s_id + "/statwithdrawals");
		hasBike     = 1;
		balance    -= 10;
		available  -= 1;
		withdrawal += 1;
		writeRecord("user/" + u_id + "/hasbike", hasBike);
		writeRecord("user/" + u_id + "/balance", balance);
		writeRecord("station/" + s_id + "/availablebikes", available);
		writeRecord("station/" + s_id + "/statwithdrawals", withdrawal);

	}

	public synchronized void bikeDown(String u_id, String s_id, float longi, float lati) throws FailedRecCommunicationException, FullStationException, UnreachableStationException, HasNoBikeException, UnknownUserIdException, UnknownStationIdException, InvalidCoordinatesException {

		// verify the user, station and coordinates are valid
		verifyStation(s_id);
		verifyUser(u_id);
		verifyCoordinates(lati, longi);
		
			
		int hasBike = readRecord("user/" + u_id + "/hasbike");
		int available = readRecord("station/" + s_id + "/availablebikes");

		if (hasBike == 0) {
			throw new HasNoBikeException();
		}
		if (!reachableStation(s_id, longi, lati)) {
			throw new UnreachableStationException();
		}
		if (available == getNDocks(s_id)) {
			throw new FullStationException();
		}
		//all conditions checked
		int balance = readRecord("user/" + u_id + "/balance");
		int deposit = readRecord("station/" + s_id + "/statdeposit");
		hasBike    = 0;
		balance   += getAward(s_id);
		available += 1;
		deposit   += 1;
		writeRecord("user/" + u_id + "/hasbike", hasBike);
		writeRecord("user/" + u_id + "/balance", balance);
		writeRecord("station/" + s_id + "/availablebikes", available);
		writeRecord("station/" + s_id + "/statdeposit", deposit);
			
	}


  	public List<String> locateStation(float latitude, float longitude, int k) throws InvalidKNumberException, InvalidCoordinatesException {
		HashMap<String, Integer> distances = new HashMap<String, Integer>();

		if(k <= 0) {
			throw new InvalidKNumberException();
		}
		
		// make sure n isn't bigger than the number of stations that exist
		int n = (k > _stations.size()) ? _stations.size() : k;

		// verify that the coordinates are valid
		verifyCoordinates(latitude, longitude);

		for (Station s: _stations.values()) {
			distances.put(s.getId(), calculateDistance(longitude, latitude, s.getLongitude(), s.getLatitude()));
		}

		// sort the hashmap by the distance of each station to the user
		final LinkedHashMap<String, Integer> sortedDistance = distances.entrySet().stream()
				.sorted(Entry.comparingByValue())
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		
		ArrayList<String> idList = new ArrayList<String>(sortedDistance.keySet());
		// return the n-closest sations to the user
		return idList.subList(0, n);
	}


	private void verifyCoordinates(float latitude, float longitude) throws InvalidCoordinatesException {
		if(latitude < -90.0 || latitude > 90.0 || longitude < -180.0 || longitude > 180.0) {
			throw new InvalidCoordinatesException();
		}
	}


	private int calculateDistance(float longitude1, float latitude1, float longitude2, float latitude2) {
		double latDistance = Math.toRadians(latitude1 - latitude2);
		double lngDistance = Math.toRadians(longitude1 - longitude2);

		double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
			+ Math.cos(Math.toRadians(latitude1)) * Math.cos(Math.toRadians(latitude2))
			* Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);

		double d = 2 * Math.asin(Math.sqrt(a)) * 6371000;

		return (int) (Math.round(d));
	}

	public HashMap<String, Boolean> sysStatus() {

		// ping all recs
		HashMap<String, Boolean> status = _recFrontend.ping();
		// add hub status
		status.put(_path, true);
		return status;
	}
	
	public void shutDownFrontend(){
		_recFrontend.shutdownChannel();
	}

}
