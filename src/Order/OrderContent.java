package Order;

import Item.Cart;
import Item.CartItem;
import Item.Item;
import Shop.Shop;

import java.util.ArrayList;
import java.util.List;

// Cart filtered by shop
public class OrderContent {
    private final Shop shop;
    private final double totalPrice;
    private final List<OrderItem> items;

    private OrderContent(Shop shop, Cart cart) {
        super();
        this.shop = shop;
        totalPrice = cart.getTotalPrice();
        items = new ArrayList<>();
        for (CartItem cartItem : cart.getItems()) {
            Item thisItem = cartItem.getItemStock().getItem();
            int itemIndex = existsInList(items, thisItem);
            if (itemIndex != -1) {
                // if already exists in the list, increase the quantity
                items.set(
                        itemIndex,
                        new OrderItem(thisItem, items.get(itemIndex).quantity() + cartItem.getQuantity())
                );
                continue;
            }
            items.add(new OrderItem(thisItem, cartItem.getQuantity()));
        }
    }

    public static OrderContent filterFromCustomerCart(Shop shop, Cart customerCart) {
        Cart filteredCart = new Cart(customerCart.getItems().stream()
                .filter(cartItem ->
                        cartItem.getItemStock().getShop().equals(shop)
                ).toList());
        return new OrderContent(shop, filteredCart);
    }

    public Shop getShop() {
        return shop;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    private static int existsInList(List<OrderItem> list, Item item) {
        for (int i = 0; i < list.size(); i++){
            if (list.get(i).item().equals(item)) {
                return i;
            }
        }
        return -1;
    }
}
