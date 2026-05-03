package andreiv.model.order;

public class OrderBuilder {
    private Address senderAddress;
    private Contact senderContact;
    private Address receiverAddress;
    private Contact receiverContact;
    private Package pkg;

    public void createSenderAddress(final String country, final String city,
                                    final String street, final String number) {
        senderAddress = new Address(country, city, street, number);
    }

    public void createSenderContact(final String name, final String emailAddress, final String phoneNumber,
                                    final String vatNumber, final boolean isCompany) {
        senderContact = new Contact(name, senderAddress, emailAddress, phoneNumber, vatNumber, isCompany);
    }

    public void createReceiverAddress(final String country, final String city,
                                    final String street, final String number) {
        receiverAddress = new Address(country, city, street, number);
    }

    public void createReceiverContact(final String name, final String emailAddress, final String phoneNumber,
                                    final String vatNumber, final boolean isCompany) {
        receiverContact = new Contact(name, receiverAddress, emailAddress, phoneNumber, vatNumber, isCompany);
    }

    public void createPackage(final double weight, final double width, final double length,
                              final double height, final String[] requirements) {
        pkg = new Package(weight, width, length, height, requirements);
    }

    public Order build() {
        if (senderContact == null || receiverContact == null) {
            throw new IllegalStateException("Failed to create order. Contact information not provided.");
        }

        if (pkg == null) {
            throw new IllegalStateException("Failed to create order. Package couldn't be instantiated.");
        }

        return new Order(senderContact, receiverContact, pkg);
    }
}
