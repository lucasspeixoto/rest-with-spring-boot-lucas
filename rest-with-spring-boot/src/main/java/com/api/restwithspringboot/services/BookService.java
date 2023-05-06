package com.api.restwithspringboot.services;

import com.api.restwithspringboot.controllers.BookController;
import com.api.restwithspringboot.data.vo.v1.BookVO;
import com.api.restwithspringboot.exceptions.RequiredObjectIsNullException;
import com.api.restwithspringboot.exceptions.ResourceNotFoundException;
import com.api.restwithspringboot.mapper.DozerMapper;
import com.api.restwithspringboot.mapper.custom.BookMapper;
import com.api.restwithspringboot.models.Book;
import com.api.restwithspringboot.repositories.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Logger;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Service
public class BookService {

    private final Logger logger = Logger.getLogger(BookService.class.getName());

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookMapper bookMapper;

    public List<BookVO> findAll() {
        logger.info("Finding All books!");

        var booksViewObject = DozerMapper.parseListObjects(this.bookRepository.findAll(), BookVO.class);

        booksViewObject.forEach(
                person -> person
                        .add(linkTo(methodOn(BookController.class).findById(person.getKey())).withSelfRel()));

        return booksViewObject;

    }

    public BookVO findById(Long id) {

        logger.info("Finding a book by Id");

        var entity = this.bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No records found for this ID!"));
        BookVO bookViewObject = DozerMapper.parseObject(entity, BookVO.class);

        bookViewObject
                .add(linkTo(methodOn(BookController.class)
                        .findById(id)
                ).withSelfRel());

        return bookViewObject;
    }

    public BookVO create(BookVO book) {
        if (book == null) throw new RequiredObjectIsNullException();

        logger.info("Creating a Book");

        var entity = DozerMapper.parseObject(book, Book.class);

        var bookViewObject = DozerMapper.parseObject(this.bookRepository.save(entity), BookVO.class);

        bookViewObject
                .add(linkTo(methodOn(BookController.class)
                        .findById(bookViewObject.getKey())
                ).withSelfRel());

        return bookViewObject;
    }

    public BookVO update(BookVO book) {
        if (book == null) throw new RequiredObjectIsNullException();

        logger.info("Creating a Book");

        var entity = this.bookRepository.findById(book.getKey())
                .orElseThrow(() -> new ResourceNotFoundException("No records founds for this ID!"));

        entity.setAuthor(book.getAuthor());
        entity.setLaunchDate(book.getLaunchDate());
        entity.setPrice(book.getPrice());
        entity.setTitle(book.getTitle());

        var bookViewObject = DozerMapper.parseObject(this.bookRepository.save(entity), BookVO.class);

        bookViewObject
                .add(linkTo(methodOn(BookController.class)
                        .findById(bookViewObject.getKey())
                ).withSelfRel());

        return bookViewObject;

    }

    public void delete(Long id) {
        logger.info("Deleting a Book");

        var entity = this.bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No records found for this ID!"));
        this.bookRepository.delete(entity);
    }
}
