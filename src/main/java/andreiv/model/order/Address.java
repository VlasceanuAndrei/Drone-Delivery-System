package andreiv.model.order;

import java.util.UUID;

public class Address {
    private final UUID id;
    private final String country;
    private final String city;
    private final String street;
    private final String number;

    public Address(String country, String city, String street, String number) {
        this(UUID.randomUUID(), country, city, street, number);
    }

    public Address(UUID id, String country, String city, String street, String number) {
        this.id = id == null ? UUID.randomUUID() : id;
        this.country = country;
        this.city = city;
        this.street = street;
        this.number = number;
    }

    public Address updateCity(String newCity) {
        return new Address(this.id, this.country, newCity, this.street, this.number);
    }

    public Address updateStreet(String newStreet, String newNumber) {
        return new Address(this.id, this.country, this.city, newStreet, newNumber);
    }

    public UUID getId() {
        return id;
    }

    public String getCountry() {
        return country;
    }

    public String getStreet() {
        return street;
    }

    public String getCity() {
        return city;
    }

    public String getNumber() {
        return number;
    }
}
