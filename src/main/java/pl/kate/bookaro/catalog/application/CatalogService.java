package pl.kate.bookaro.catalog.application;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.kate.bookaro.catalog.application.port.CatalogUseCase;
import pl.kate.bookaro.catalog.db.AuthorJpaRepository;
import pl.kate.bookaro.catalog.db.BookJpaRepository;
import pl.kate.bookaro.catalog.domain.Author;
import pl.kate.bookaro.catalog.domain.Book;
import pl.kate.bookaro.uploads.application.ports.UploadUseCase;
import pl.kate.bookaro.uploads.application.ports.UploadUseCase.SaveUploadCommand;
import pl.kate.bookaro.uploads.domain.Upload;

import javax.transaction.Transactional;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
class CatalogService implements CatalogUseCase {
    private final BookJpaRepository repository;
    private final AuthorJpaRepository authorRepository;
    private final UploadUseCase upload;

    @Override
    public List<Book> findAll(){
        return repository.findAllEager();
    }

    @Override
    public List<Book> findByTitleAndAuthor(String title, String author) {
        return repository.findByTitleAndAuthor(title, author);
    }

    @Override
    public List<Book> findByTitle(String title){
        return repository.findByTitleStartingWithIgnoreCase(title);
    }

    @Override
    public Optional<Book> findOneByTitle(String title) {
        return repository.findDistinctFirstByTitleContainsIgnoreCase(title);
    }

    @Override
    public Optional<Book> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Optional<Book> findOneByAuthor(String author) {
        return repository.findDistinctFirstByAuthorsContainsIgnoreCase(author);
    }

    @Override
    public List<Book> findByAuthor(String name){
        return repository.findByAuthor(name);
    }

    @Override
    @Transactional
    public Book addBook(CreateBookCommand command){
        Book book = toBook(command);
        return repository.save(book);
    }

    private Book toBook(CreateBookCommand command){
        Book book = new Book(command.getTitle(), command.getYear(), command.getPrice(), command.getAvailable());
        Set<Author> authors = fetchAuthorsByIds(command.getAuthors());
        updateBooks(book, authors);
        return book;
    }

    private void updateBooks(Book book, Set<Author> authors) {
        book.removeAuthors();
        authors.forEach(book::addAuthor);
    }

    private Set<Author> fetchAuthorsByIds(Set<Long> authors) {
        return authors
                .stream()
                .map(authorId ->
                        authorRepository.findById(authorId)
                                .orElseThrow(() -> new IllegalArgumentException("Unable to find author with id: " + authorId)))
                .collect(Collectors.toSet());
    }

    @Override
    public void removeById(Long id){
        repository.deleteById(id);
    }

    @Override
    @Transactional
    public UpdateBookResponse updateBook(UpdateBookCommand command) {
        return repository
                .findById(command.getId())
                    .map(book ->{
                        updateFields(command, book);
                       //Book updatedBook =
                        //repository.save(updatedBook);
                        return UpdateBookResponse.SUCCESS;
                    })
                    .orElseGet(() -> new UpdateBookResponse(false, Collections.singletonList("Book not found with id: " + command.getId())));
    }

    private Book updateFields(UpdateBookCommand command, Book book){
            if(command.getTitle() != null){
                book.setTitle(command.getTitle());
            }
            if (command.getAuthors() != null && command.getAuthors().size() > 0){
                updateBooks(book, fetchAuthorsByIds(command.getAuthors()));
            }
            if(command.getYear() != null){
                book.setYear(command.getYear());
            }
            if(command.getPrice() != null){
                book.setPrice(command.getPrice());
            }
            return book;
        }


    @Override
    public void updateBookCover(UpdateBookCoverCommand command) {
        repository.findById(command.getId())
                .ifPresent(book -> {
                    Upload saveUpload = upload.save(new SaveUploadCommand(command.getFilename(), command.getFile(), command.getContentType()));
                    book.setCoverId(saveUpload.getId());
                    repository.save(book);
                });
    }

    @Override
    public void removeBookCover(Long id) {
        repository.findById(id)
                .ifPresent(book -> {
                    if(book.getCoverId() != null) {
                        upload.removeById(book.getCoverId());
                        book.setCoverId(null);
                        repository.save(book);
                    }

                });
    }

}
