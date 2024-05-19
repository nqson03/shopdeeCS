package Order;

public enum OrderState {
    CREATED("Created"),
    SHOP_ACCEPTED("Shop accepted"),
    SHIPPING("Shipping"),
    AT_WAREHOUSE("At warehouse"),
    DELIVERED("Delivered"),
    CUSTOMER_CONFIRMED("Customer confirmed");

    private final String state;
    OrderState(String stateName) {
        state = stateName;
    }

    @Override
    public String toString() {
        return state;
    }
}
