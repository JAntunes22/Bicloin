package pt.tecnico.bicloin.hub;

public class Station {
    private String _id;
    private String _name;
    private float _longitude;
    private float _latitude;
    private int _n_docks;
    private int _n_bikes;
    private int _award;

    public Station(String id, String name, float longitude, float latitude, int n_docks, int n_bikes, int award) {
        _id      = id;
        _name    = name;
        _longitude = longitude;
        _latitude    = latitude;
        _n_docks = n_docks;
        _n_bikes = n_bikes;
        _award   = award;
    }

    public String getId() {
        return _id;
    }

    public String getName() {
        return _name;
    }
    public float getLongitude() {
        return _longitude;
    }

    public float getLatitude() {
        return _latitude;
    }

    public int getNDocks() {
        return _n_docks;
    }

    public int getNBikes() {
        return _n_bikes;
    }

    public int getAward() {
        return _award;
    }
}