package pt.tecnico.bicloin.hub;

public class User {

    private String _id;
    private String _name;
    private String _phone_number;

    public User(String id, String name, String phone_number) {
        _id = id;
        _name = name;
        _phone_number = phone_number;
    }

    public String getId() {
        return _id;
    }

    public String getName() {
        return _name;
    }

    public String getPhoneNumber() {
        return _phone_number;
    }
}