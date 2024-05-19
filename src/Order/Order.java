package Order;

import Shop.Shop;
import User.Customer;
import User.Shipper;
import Utils.Address;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id"
)
public class Order {
    private final int id;
    private Date orderedDate;
    
    private final Customer customer;
    
    private final Shop shop;
    
    private OrderState orderState;
    private Shipper shipper;
    
    private Address location;

    private final double totalPrice;
    private final List<OrderItem> items;

    private static int currentId = 10000; // from 10_000 to 19_999

    @JsonCreator
    private Order(
            @JsonProperty("id") int id,
            @JsonProperty("orderedDate") Date orderedDate,
            @JsonProperty("customer")  Customer customer,
            @JsonProperty("shop")  Shop shop,
            @JsonProperty("orderState")  OrderState orderState,
            @JsonProperty("shipper") Shipper shipper,
            @JsonProperty("location")  Address location,
            @JsonProperty("totalPrice") double totalPrice,
            @JsonProperty("items") List<OrderItem> items
    ) {
        this.id = id;
        this.orderedDate = orderedDate;
        this.customer = customer;
        this.totalPrice = totalPrice;
        this.items = items;
        this.shop = shop;
        this.orderState = orderState;
        this.shipper = shipper;
        this.location = location;
        if (id > currentId) {
            currentId = id;
        }
    }

    // an order contents must come from only one shop, using OrderContent to ensure this
    public Order( Customer customer, Date orderedDate,  OrderContent content) {
        if ((currentId - 19_999) % 100_000 == 0) {
            currentId += 100_000 - 9999;
        }
        this.id = ++currentId;
        this.customer = customer;
        this.orderedDate = orderedDate;
        this.totalPrice = content.getTotalPrice();
        this.items = content.getItems();
        this.shop = content.getShop();
        this.orderState = OrderState.CREATED;
        this.location = shop.getAddress();
        shipper = null;
    }

    public int getId() {
        return id;
    }

    public  Customer getCustomer() {
        return customer;
    }

    public Address getCustomerAddress() {
        return customer.getAddress();
    }

    public Date getOrderedDate() {
        return orderedDate;
    }

    public void setOrderedDate(Date orderedDate) {
        this.orderedDate = orderedDate;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public  Shop getShop() {
        return shop;
    }

    public  OrderState getOrderState() {
        return orderState;
    }

    public void setOrderState( OrderState orderState) {
        this.orderState = orderState;
    }

    public  Address getLocation() {
        return location;
    }

    public void setLocation( Address location) {
        this.location = location;
    }

    public Shipper getShipper() {
        return shipper;
    }

    public void setShipper(Shipper shipper) {
        this.shipper = shipper;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order order)) return false;
        return id == order.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Order-------------" +
                "\nid=" + id +
                "\norderedDate=" + orderedDate.toString() +
                "\ncustomer=" + customer.getName() +
                "\nshop=" + shop.getName() +
                "\norderState=" + orderState +
                "\ncontent:\n" + itemsToString() +
                '\n';
    }

    private String itemsToString() {
        StringBuilder sb = new StringBuilder();
        for (OrderItem item : items) {
            sb.append(String.format("Name: %s - Amount: %d\n", item.item().getName(), item.quantity()));
        }
        return sb.toString();
    }
}
