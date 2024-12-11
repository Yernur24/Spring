package book.store.repository;

import book.store.model.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Transactional
public interface UserRepository extends JpaRepository<User,Long> {
    User findByEmail (String email);
    List<User> findAll();

    @EntityGraph(attributePaths = "permissions")
    @Query("SELECT u FROM User u")
    List<User> findAllWithPermissions();
}
