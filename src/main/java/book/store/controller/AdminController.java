package book.store.controller;

import book.store.model.AuthorModel;
import book.store.model.BookModel;
import book.store.model.Order;
import book.store.model.User;
import book.store.service.AuthorService;
import book.store.service.BookService;
import book.store.service.FileStorageService;
import book.store.service.OrderService;
import book.store.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class AdminController {

    @Value("${books.images.path}")
    private String booksImagesPath;

    private final BookService bookService;
    private final UserService userService;
    private final FileStorageService fileStorageService;
    private final AuthorService authorService;
    private final OrderService orderService;

    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @GetMapping(value = "/addbooks")
    public String addBooksPage(Model model) {
        model.addAttribute("authors", authorService.getAuthors());
        return "addbooks";
    }

    @PostMapping(value = "/add-book")
    public String addBook(@RequestParam(name = "name") String name,
                          @RequestParam(name = "image") MultipartFile img,
                          @RequestParam(name = "publicationYear") int publicationYear,
                          @RequestParam(name = "price") int price,
                          @RequestParam(name = "description") String description,
                          @RequestParam(name = "authorModel_id") AuthorModel authorModel) {

        try {
            BookModel book = new BookModel();
            book.setName(name);
            book.setPublicationYear(publicationYear);
            book.setPrice(price);
            book.setDescription(description);
            book.setAuthorModel(authorModel);

            if (!img.isEmpty()) {
                String fileName = fileStorageService.saveFile(img, booksImagesPath);
                book.setImg(fileName);
            }

            BookModel newBook = bookService.addBook(book);

            if (newBook != null) {
                return "redirect:/";
            } else {
                return "redirect:/books?error";
            }

        } catch (IOException e) {
            System.out.println(e);
            return "redirect:/addbooks?error";
        }
    }

    @PostMapping(value = "/delete-book")
    public String deleteBook(@RequestParam(name = "id") Long id) {
        bookService.delete(id);
        return "redirect:/";
    }

    @GetMapping(value = "/admin-panel")
    public String adminPanel(Model model) {
        List<BookModel> books = bookService.getAllBooks();
        List<User> users = userService.getAllUsers();
        List<Order> orders = orderService.getAllOrders();
        model.addAttribute("orders", orders);
        model.addAttribute("books", books);
        model.addAttribute("users", users);
        return "admin";
    }
    @PostMapping(value = "/admin-panel/change-role")
    public String changeUserRole(@RequestParam Long userId, @RequestParam String role) {
        userService.changeUserRole(userId, role);
        return "redirect:/admin-panel";
    }


    @PostMapping(value = "/assign-genre")
    public String assignGenre(@RequestParam(name = "book_id") Long bookId,
                              @RequestParam(name = "genre_id") Long genreId) {
        bookService.assignGenre(bookId, genreId);
        return "redirect:/details/" + bookId;
    }

    @PostMapping(value = "/unassign-genre")
    public String unassignGenre(@RequestParam(name = "book_id") Long bookId,
                                @RequestParam(name = "genre_id") Long genreId) {
        bookService.unassignGenre(bookId, genreId);
        return "redirect:/details/" + bookId;
    }

    @PostMapping(value = "/user/toggle/ban")
    public String banUser(@RequestParam(name = "user_id") Long userId,
                          @RequestParam(name = "ban") Boolean ban) {
        userService.toggleBan(userId, ban);
        return "redirect:/admin-panel";
    }

    @PostMapping("/add-author")
    public String addAuthor(@RequestParam(name = "fullname") String fullname,
                            @RequestParam(name = "nick_name") String nickName) {

        AuthorModel author = new AuthorModel();
        author.setFullname(fullname);
        author.setNick_name(nickName);

        AuthorModel newAuthor = authorService.addAuthor(author);

        if (newAuthor != null) {
            return "redirect:/";
        } else {
            return "redirect:/authors/add?error";
        }
    }

    @GetMapping("/authors/add")
    public String showAddAuthorPage(Model model) {
        model.addAttribute("author", new AuthorModel());
        return "add_author";
    }

    @GetMapping(value = "/admin/orders")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String viewOrders(Model model) {
        List<Order> orders = orderService.getAllOrders();
        model.addAttribute("orders", orders);
        return "orders";
    }

    @PostMapping("/admin/orders/confirm")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String confirmOrder(@RequestParam("orderId") Long orderId) {
        orderService.updateOrderStatus(orderId, "Подтверждено");
        return "redirect:/admin-panel";
    }

    @PostMapping("/admin/orders/reject")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String rejectOrder(@RequestParam("orderId") Long orderId) {
        orderService.updateOrderStatus(orderId, "Отклонено");
        return "redirect:/admin-panel";
    }
}
