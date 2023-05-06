package com.api.restwithspringboot.repositories;

import com.api.restwithspringboot.models.Person;
import integrationtests.testcontainers.AbstractIntegrationTest;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(SpringExtension.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PersonRepositoryTest extends AbstractIntegrationTest {

    private static Person person;
    @Autowired
    public PersonRepository personRepository;

    @BeforeAll
    public static void setup() {
        person = new Person();
    }

    @Test
    @Order(1)
    public void testFindByName() throws IOException {

        Pageable pageable = PageRequest.of(0, 6, Sort.by(Direction.ASC, "firstName"));
        person = personRepository.findPersonByName("liana", pageable).getContent().get(0);

        assertNotNull(person.getFirstName());
        assertNotNull(person.getLastName());
        assertNotNull(person.getGender());
        assertNotNull(person.getAddress());

        assertEquals(1, person.getId());

        assertEquals("Liana", person.getFirstName());
        assertEquals("Fernandes", person.getLastName());
        assertEquals("Female", person.getGender());
        assertEquals("Bauru", person.getAddress());
        assertTrue(person.getEnabled());

    }

    @Test
    @Order(2)
    public void testDisablePerson() throws IOException {

        personRepository.disablePerson(person.getId());

        Pageable pageable = PageRequest.of(0, 6, Sort.by(Direction.ASC, "firstName"));
        person = personRepository.findPersonByName("liana", pageable).getContent().get(0);

        assertFalse(person.getEnabled());

    }
}
