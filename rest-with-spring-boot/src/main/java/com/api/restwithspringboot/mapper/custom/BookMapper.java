package com.api.restwithspringboot.mapper.custom;

import com.api.restwithspringboot.data.vo.v1.BookVO;
import com.api.restwithspringboot.models.Book;
import org.springframework.stereotype.Service;

@Service
public class BookMapper {

    public Book convertVoToEntity(BookVO vo) {
        Book book = new Book();
        book.setId(vo.getKey());
        book.setAuthor(vo.getAuthor());
        book.setLaunchDate(vo.getLaunchDate());
        book.setPrice(vo.getPrice());
        book.setTitle(vo.getTitle());
        return book;
    }
}
