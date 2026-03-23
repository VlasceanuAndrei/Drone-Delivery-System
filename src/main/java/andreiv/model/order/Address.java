package andreiv.model.order;

public class Address {
    private final String country;
    private final String city;
    private final String street;
    private final String number;

    public Address(String country, String city, String street, String number) {
        this.country = country;
        this.city = city;
        this.street = street;
        this.number = number;
    }

    public Address updateCity(String newCity) {
        return new Address(this.country, newCity, this.street, this.number);
    }

    public Address updateStreet(String newStreet, String newNumber) {
        return new Address(this.country, this.city, newStreet, newNumber);
    }

    public String getAddress() {
        return country + ", " + city + ", " + street + ", " + number;
    }
}
