package com.api.restwithspringboot.repositories;

import com.api.restwithspringboot.models.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {
}
