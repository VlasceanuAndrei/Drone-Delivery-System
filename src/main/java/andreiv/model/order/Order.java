package andreiv.model.order;

import java.util.Set;
import java.util.UUID;
import andreiv.model.PackageRequirement;

public class Order {
    private final UUID id;
    private final Contact sender;
    private final Contact receiver;
    private final Package pkg;

    public Order(Contact sender, Contact receiver, Package pkg) {
        this.id = UUID.randomUUID();
        this.sender = sender;
        this.receiver = receiver;
        this.pkg = pkg;
    }

    public String getReceiverCity() {
        return receiver.getAddress().getCity();
    }

    public Set<PackageRequirement> getPackageRequirements() {
        return pkg.getRequirements();
    }
}
