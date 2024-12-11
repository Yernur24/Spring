package book.store.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "t_cart")
@Getter
@Setter
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    private User user;

    @ManyToMany
    private List<BookModel> books;

    @Column(name = "total_price")
    private Double totalPrice;

    public void calculateTotalPrice() {
        this.totalPrice = books.stream().mapToDouble(BookModel::getPrice).sum();
    }
}
