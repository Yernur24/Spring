package book.store.controller;

import book.store.model.BookModel;
import book.store.model.Cart;
import book.store.model.User;
import book.store.service.CartService;
import book.store.service.BookService;
import book.store.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final BookService bookService;
    private final UserService userService;

    @PostMapping("/add-to-cart")
    @PreAuthorize("isAuthenticated()")
    public String addToCart(@RequestParam("bookId") Long bookId) {
        User user = userService.getCurrentSessionUser();
        BookModel book = bookService.getBook(bookId);

        if (user != null && book != null) {
            cartService.addToCart(user, book);
            return "redirect:/?success";
        } else {
            return "redirect:/?error";
        }
    }


    @PreAuthorize("isAuthenticated()")
    @PostMapping(value = "/remove-from-cart")
    public String removeFromCart(@RequestParam(name = "bookId") Long bookId) {
        BookModel book = bookService.getBook(bookId);
        if (book != null) {
            cartService.removeBookFromCart(userService.getCurrentSessionUser(), book);
        }
        return "redirect:/cart";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping(value = "/cart")
    public String viewCart(Model model) {
        Cart cart = cartService.getUserCart(userService.getCurrentSessionUser());
        model.addAttribute("cart", cart);
        return "cart";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping(value = "/checkout")
    public String checkout() {
        User user = userService.getCurrentSessionUser();
        Cart cart = cartService.getUserCart(user);

        if (cart != null && !cart.getBooks().isEmpty()) {
            cartService.createOrder(user, cart);
            cartService.clearCart(user);
            return "redirect:/cart";
        }
        return "redirect:/cart?error";
    }

}
