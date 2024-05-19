package MainSystem;

import Order.Order;
import User.User;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Hashtable;
import java.util.List;

public record SystemDataHolder(Hashtable<String, User> users, double profit, List<Order> orders) {
    @JsonCreator
    public SystemDataHolder(
            @JsonProperty("users") Hashtable<String, User> users,
            @JsonProperty("profit") double profit,
            @JsonProperty("orders") List<Order> orders
    ) {
        this.users = users;
        this.profit = profit;
        this.orders = orders;
    }
}
