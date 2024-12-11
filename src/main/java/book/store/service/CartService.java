package book.store.service;

import book.store.model.Cart;
import book.store.model.BookModel;
import book.store.model.Order;
import book.store.model.User;
import book.store.repository.CartRepository;
import book.store.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final UserService userService;

    public Cart getUserCart(User user) {
        return cartRepository.findByUser(user).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setUser(user);
            newCart.setBooks(new ArrayList<>());
            return cartRepository.save(newCart);
        });
    }


    public void addToCart(User user, BookModel book) {
        Cart cart = getUserCart(user);
        if (cart.getBooks().contains(book)) {
            return;
        }
        cart.getBooks().add(book);
        cart.calculateTotalPrice();
        cartRepository.save(cart);
    }

    public Cart removeBookFromCart(User user, BookModel book) {
        Cart cart = getUserCart(user);
        if (cart != null) {
            cart.getBooks().remove(book);
            cart.calculateTotalPrice();
            return cartRepository.save(cart);
        }
        return null;
    }

    public void clearCart(User user) {
        Cart cart = getUserCart(user);
        if (cart != null) {
            cart.getBooks().clear();
            cart.calculateTotalPrice();
            cartRepository.save(cart);
        }
    }

    public Order createOrder(User user, Cart cart) {
        Order order = new Order();
        order.setUserName(user.getFullname());
        order.setTotalPrice(cart.getTotalPrice());
        order.setBookNames(cart.getBooks().stream().map(BookModel::getName).collect(Collectors.toList()));
        order.setStatus("Ожидание подтверждения");
        return orderRepository.save(order);
    }
}
