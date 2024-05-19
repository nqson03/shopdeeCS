package Item;

import java.util.Objects;

public class CartItem {
    private final int id;
    private int quantity;
    private final ItemStock itemStock;

    private static int currentId = 50_000; // id range: 50_000 -> 59_999


    public CartItem(int quantity, ItemStock itemStock) {
        if (quantity <= 0 || quantity > itemStock.getQuantity()) {
            throw new IllegalArgumentException("Invalid quantity when creating a cart item");
        }
        this.quantity = quantity;
        this.itemStock = itemStock;
        id = ++currentId;
        if ((currentId - 59_999) % 100_000 == 0) {
            currentId += 100_000 - 9999;
        }
    }

    public int getId() {
        return id;
    }

    public int getQuantity() {
        return quantity;
    }

    public ItemStock getItemStock() {
        return itemStock;
    }

    public double getTotalPrice() {
        return itemStock.getPrice() * quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = Math.min(quantity, itemStock.getQuantity());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CartItem cartItem)) return false;
        return id == cartItem.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Cart Item" +
                "\nid: " + id +
                "\nquantity: " + quantity +
                "\nitem name: " + itemStock.getItem().getName() +
                "\nitem price: " + itemStock.getPrice() +
                "\nitem from shop: " + itemStock.getShop().getName() +
                '\n';
    }
}
