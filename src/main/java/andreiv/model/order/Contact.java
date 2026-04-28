package andreiv.model.order;

import andreiv.exception.InvalidEmailAddressException;
import andreiv.exception.InvalidPhoneNumberException;
import andreiv.exception.InvalidVatNumberException;

public class Contact {
    private final String name;
    private Address address;
    private String emailAddress;
    private String phoneNumber;
    private final boolean isCompany;
    private final String vatNumber;

    public Contact(String name, Address address, String emailAddress, String phoneNumber,
                   String vatNumber, boolean isCompany) {
        this.name = name;
        this.address = address;
        this.emailAddress = validateEmailAddress(emailAddress);
        this.phoneNumber = validatePhoneNumber(phoneNumber);
        this.isCompany = isCompany;
        this.vatNumber = validateVatNumber(vatNumber, isCompany);
    }

    private String validateEmailAddress(String emailAddress) {
        if (emailAddress != null &&
                emailAddress.matches("^[\\w.-]+@([\\w-]+\\.)+[\\w-]{2,5}")) {
            return emailAddress;
        }
        throw new InvalidEmailAddressException(emailAddress);
    }

    private String validatePhoneNumber(String phoneNumber) {
        if (phoneNumber != null &&
                phoneNumber.matches("^[+][0-9]{7,15}")) {
            return phoneNumber;
        }
        throw new InvalidPhoneNumberException(phoneNumber);
    }

    private String validateVatNumber(String vatNumber, boolean isCompany) {
        if (isCompany && vatNumber != null &&
                vatNumber.matches("^[0-9A-Z]{4,15}")) {
            return vatNumber;
        } else if (!isCompany) {
            return "N/A";
        }
        throw new InvalidVatNumberException(vatNumber);
    }

    public String getName() {
        return name;
    }

    public Address getAddress() {
        return address;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getVatNumber() {
        return vatNumber;
    }

    public boolean isCompany() {
        return isCompany;
    }

}
