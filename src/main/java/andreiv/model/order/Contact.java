package andreiv.model.order;

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
        this.vatNumber = validatevatNumber(vatNumber);
    }

    private String validateEmailAddress(String emailAddress) {
        if (emailAddress != null &&
                emailAddress.matches("^[\\w-]+@([\\w-]+\\.)+[\\w-]{2,5}")) {
            return emailAddress;
        }
        throw new IllegalArgumentException("Invalid value provided for the emailAddress.");
    }

    private String validatePhoneNumber(String phoneNumber) {
        if (phoneNumber != null &&
                phoneNumber.matches("^[+][0-9]{7,15}")) {
            return phoneNumber;
        }
        throw new IllegalArgumentException("Invalid value provided for the phoneNumber");
    }

    private String validatevatNumber(String vatNumber) {
        if (this.isCompany && vatNumber != null &&
                vatNumber.matches("^[0-9A-Z]{4,15}")) {
            return vatNumber;
        } else if (!this.isCompany) {
            return "N/A";
        }
        throw new IllegalArgumentException("Invalid value provided for the vatNumber");
    }

}
