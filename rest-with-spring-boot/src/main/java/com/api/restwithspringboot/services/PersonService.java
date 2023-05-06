package com.api.restwithspringboot.services;

import com.api.restwithspringboot.controllers.PersonController;
import com.api.restwithspringboot.data.vo.v1.PersonVO;
import com.api.restwithspringboot.data.vo.v2.PersonVOV2;
import com.api.restwithspringboot.exceptions.RequiredObjectIsNullException;
import com.api.restwithspringboot.exceptions.ResourceNotFoundException;
import com.api.restwithspringboot.mapper.DozerMapper;
import com.api.restwithspringboot.mapper.custom.PersonMapper;
import com.api.restwithspringboot.models.Person;
import com.api.restwithspringboot.repositories.PersonRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * @Service, serve para o spring boot entender
 * que precisar injetar in run time em outras classes
 * da aplicação
 */

@Service
public class PersonService {

    private final Logger logger = Logger.getLogger(PersonService.class.getName());

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private PersonMapper personMapper;

    @Autowired
    private PagedResourcesAssembler<PersonVO> assembler;

    public PagedModel<EntityModel<PersonVO>> findAll(Pageable pageable) throws Exception {

        logger.info("Finding all people!");

        var personPage = this.personRepository.findAll(pageable);

        var personVosPage = personPage.map(
                person -> DozerMapper.parseObject(person, PersonVO.class)
        );

        personVosPage.map(person -> person
                .add(linkTo(methodOn(PersonController.class).findById(person.getKey())).withSelfRel()));

        Link link = linkTo(methodOn(PersonController.class)
                .findAll(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        "asc"))
                .withSelfRel();

        return assembler.toModel(personVosPage, link);

    }

    public PagedModel<EntityModel<PersonVO>> findPersonByName(String firstName, Pageable pageable) throws Exception {

        logger.info("Finding all people by name!");

        var personPage = this.personRepository.findPersonByName(firstName, pageable);

        var personVosPage = personPage.map(
                person -> DozerMapper.parseObject(person, PersonVO.class)
        );

        personVosPage.map(person -> person
                .add(linkTo(methodOn(PersonController.class).findById(person.getKey())).withSelfRel()));

        Link link = linkTo(methodOn(PersonController.class)
                .findAll(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        "asc"))
                .withSelfRel();

        return assembler.toModel(personVosPage, link);

    }

    public PersonVO findById(Long id) {

        logger.info("Finding a person by Id");

        var entity = this.personRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No records found for this ID!"));
        PersonVO personViewObject = DozerMapper.parseObject(entity, PersonVO.class);

        personViewObject
                .add(linkTo(methodOn(PersonController.class)
                        .findById(id)
                ).withSelfRel());

        return personViewObject;
    }

    public PersonVO create(PersonVO person) {
        if (person == null) throw new RequiredObjectIsNullException();

        logger.info("Creating one person!");

        var entity = DozerMapper.parseObject(person, Person.class);

        var personViewObject = DozerMapper.parseObject(this.personRepository.save(entity), PersonVO.class);

        personViewObject
                .add(linkTo(methodOn(PersonController.class)
                        .findById(personViewObject.getKey())
                ).withSelfRel());

        return personViewObject;
        //return DozerMapper.parseObject(this.personRepository.save(entity), PersonVO.class);
    }

    public PersonVOV2 createV2(PersonVOV2 person) {

        logger.info("Creating one person!");

        var entity = this.personMapper.convertVoToEntity(person);

        return this.personMapper.convertEntityToVo(this.personRepository.save(entity));
    }

    public PersonVO update(PersonVO person) {
        if (person == null) throw new RequiredObjectIsNullException();

        logger.info("Updating a Person");

        var entity = this.personRepository.findById(person.getKey())
                .orElseThrow(() -> new ResourceNotFoundException("No records founds for this ID!"));

        entity.setFirstName(person.getFirstName());
        entity.setLastName(person.getLastName());
        entity.setAddress(person.getAddress());
        entity.setGender(person.getGender());

        var personViewObject = DozerMapper.parseObject(this.personRepository.save(entity), PersonVO.class);

        personViewObject
                .add(linkTo(methodOn(PersonController.class)
                        .findById(personViewObject.getKey())
                ).withSelfRel());

        return personViewObject;

    }

    /**
     * O @Transaction é necessário pois esta operação é
     * customizada, sem gerenciamento do Spring Data, o que
     * pode causar eventuais inconsistencias no nosso banco de dados
     */
    @Transactional
    public PersonVO disablePerson(Long id) {

        logger.info("Disabling one person!");

        this.personRepository.disablePerson(id);

        var entity = this.personRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No records found for this ID!"));
        PersonVO personViewObject = DozerMapper.parseObject(entity, PersonVO.class);

        personViewObject
                .add(linkTo(methodOn(PersonController.class)
                        .findById(id)
                ).withSelfRel());

        return personViewObject;
    }

    public void delete(Long id) {
        logger.info("Deleting a Person");

        var entity = this.personRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No records found for this ID!"));
        this.personRepository.delete(entity);
    }
}
