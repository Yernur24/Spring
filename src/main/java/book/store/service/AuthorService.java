package book.store.service;

import book.store.model.AuthorModel;
import book.store.repository.AuthorModelRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthorService {
        private final AuthorModelRepository authorModelRepository;
        public List<AuthorModel> getAuthors(){
                return authorModelRepository.findAll();
        }
        public AuthorModel addAuthor(AuthorModel author) {
                return authorModelRepository.save(author);
        }

    public AuthorModel getAuthorById(Long id) {
        return authorModelRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Author not found with id: " + id));
    }
}
