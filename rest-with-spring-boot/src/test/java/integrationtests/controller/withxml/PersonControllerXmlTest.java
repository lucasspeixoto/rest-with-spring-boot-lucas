package integrationtests.controller.withxml;

import com.api.restwithspringboot.Startup;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import config.TestConfigs;
import integrationtests.testcontainers.AbstractIntegrationTest;
import integrationtests.vo.AccountCredentialsVO;
import integrationtests.vo.PersonVO;
import integrationtests.vo.TokenVO;
import integrationtests.vo.pagedmodels.PagedModelPerson;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = Startup.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestMethodOrder(OrderAnnotation.class)
public class PersonControllerXmlTest extends AbstractIntegrationTest {

    private static RequestSpecification specification;
    private static XmlMapper objectMapper;
    private static PersonVO person;

    @BeforeAll
    public static void setup() {
        objectMapper = new XmlMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        person = new PersonVO();
    }

    @Test
    @Order(0)
    public void authorization() throws IOException {
        AccountCredentialsVO user = new AccountCredentialsVO("lucas", "admin123");

        var accessToken = given()
                .basePath("/auth/signin")
                .port(TestConfigs.SERVER_PORT)
                .contentType(TestConfigs.CONTENT_TYPE_XML)
                .accept(TestConfigs.CONTENT_TYPE_XML)
                .body(user)
                .when()
                .post()
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(TokenVO.class)
                .getAccessToken();

        specification = new RequestSpecBuilder()
                .addHeader(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + accessToken)
                .setBasePath("/api/person/v1")
                .setPort(TestConfigs.SERVER_PORT)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();

    }

    @Test
    @Order(1)
    public void testCreate() throws IOException {
        mockPerson();

        var content = given().spec(specification)
                .contentType(TestConfigs.CONTENT_TYPE_XML)
                .accept(TestConfigs.CONTENT_TYPE_XML)
                .body(person)
                .when()
                .post()
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        PersonVO createdPerson = objectMapper.readValue(content, PersonVO.class);
        person = createdPerson;

        assertNotNull(createdPerson);

        assertNotNull(createdPerson.getId());
        assertNotNull(createdPerson.getFirstName());
        assertNotNull(createdPerson.getLastName());
        assertNotNull(createdPerson.getGender());
        assertNotNull(createdPerson.getAddress());

        assertTrue(createdPerson.getId() > 0);

        assertTrue(createdPerson.getEnabled());

        assertEquals("Lucas", createdPerson.getFirstName());
        assertEquals("Peixoto", createdPerson.getLastName());
        assertEquals("Male", createdPerson.getGender());
        assertEquals("Bauru City, São Paulo", createdPerson.getAddress());

    }

    @Test
    @Order(2)
    public void testUpdate() throws IOException {
        person.setFirstName("Baby dont Hurt me");

        var content = given().spec(specification)
                .contentType(TestConfigs.CONTENT_TYPE_XML)
                .accept(TestConfigs.CONTENT_TYPE_XML)
                .body(person)
                .when()
                .post()
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        PersonVO createdPerson = objectMapper.readValue(content, PersonVO.class);
        person = createdPerson;

        assertNotNull(createdPerson);

        assertNotNull(createdPerson.getId());
        assertNotNull(createdPerson.getFirstName());
        assertNotNull(createdPerson.getLastName());
        assertNotNull(createdPerson.getGender());
        assertNotNull(createdPerson.getAddress());

        assertEquals(person.getId(), createdPerson.getId());

        assertTrue(createdPerson.getEnabled());

        assertEquals("Baby dont Hurt me", createdPerson.getFirstName());
        assertEquals("Peixoto", createdPerson.getLastName());
        assertEquals("Male", createdPerson.getGender());
        assertEquals("Bauru City, São Paulo", createdPerson.getAddress());

    }

    @Test
    @Order(3)
    public void testDisablePersonById() throws IOException {

        var content = given().spec(specification)
                .contentType(TestConfigs.CONTENT_TYPE_XML)
                .accept(TestConfigs.CONTENT_TYPE_XML)
                .header(TestConfigs.HEADER_PARAM_ORIGIN, TestConfigs.ORIGIN_ERUDIO)
                .pathParam("id", person.getId())
                .when()
                .patch("{id}")
                .then()
                .statusCode(200)
                .extract()
                .body().asString();

        PersonVO persistedPerson = objectMapper.readValue(content, PersonVO.class);
        person = persistedPerson;

        assertNotNull(persistedPerson);

        assertNotNull(persistedPerson.getId());
        assertNotNull(persistedPerson.getFirstName());
        assertNotNull(persistedPerson.getLastName());
        assertNotNull(persistedPerson.getGender());
        assertNotNull(persistedPerson.getAddress());
        assertFalse(persistedPerson.getEnabled());

        assertTrue(persistedPerson.getId() > 0);

        assertEquals("Baby dont Hurt me", persistedPerson.getFirstName());
        assertEquals("Peixoto", persistedPerson.getLastName());
        assertEquals("Male", persistedPerson.getGender());
        assertEquals("Bauru City, São Paulo", persistedPerson.getAddress());

    }

    @Test
    @Order(4)
    public void testFindById() throws IOException {
        mockPerson();

        var content = given().spec(specification)
                .contentType(TestConfigs.CONTENT_TYPE_XML)
                .accept(TestConfigs.CONTENT_TYPE_XML)
                .header(TestConfigs.HEADER_PARAM_ORIGIN, TestConfigs.ORIGIN_ERUDIO)
                .pathParam("id", person.getId())
                .when()
                .get("{id}")
                .then()
                .statusCode(200)
                .extract()
                .body().asString();

        PersonVO persistedPerson = objectMapper.readValue(content, PersonVO.class);
        person = persistedPerson;

        assertNotNull(persistedPerson);

        assertNotNull(persistedPerson.getId());
        assertNotNull(persistedPerson.getFirstName());
        assertNotNull(persistedPerson.getLastName());
        assertNotNull(persistedPerson.getGender());
        assertNotNull(persistedPerson.getAddress());

        assertTrue(persistedPerson.getId() > 0);
        assertFalse(persistedPerson.getEnabled());

        assertEquals("Baby dont Hurt me", persistedPerson.getFirstName());
        assertEquals("Peixoto", persistedPerson.getLastName());
        assertEquals("Male", persistedPerson.getGender());
        assertEquals("Bauru City, São Paulo", persistedPerson.getAddress());

    }

    @Test
    @Order(5)
    public void testDelete() throws IOException {

        given().spec(specification)
                .contentType(TestConfigs.CONTENT_TYPE_XML)
                .accept(TestConfigs.CONTENT_TYPE_XML)
                .pathParam("id", person.getId())
                .when()
                .delete("{id}")
                .then();
    }

    @Test
    @Order(6)
    public void testFindAll() throws IOException {

        var content = given().spec(specification)
                .contentType(TestConfigs.CONTENT_TYPE_XML)
                .accept(TestConfigs.CONTENT_TYPE_XML)
                .when()
                .get()
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();
        //.as(new TypeRef<List<PersonVO>>() {});

        PagedModelPerson wrapper = objectMapper.readValue(content, PagedModelPerson.class);

        var people = wrapper.getContent();

        PersonVO foundPersonOne = people.get(0);

        assertNotNull(foundPersonOne.getFirstName());
        assertNotNull(foundPersonOne.getLastName());
        assertNotNull(foundPersonOne.getGender());
        assertNotNull(foundPersonOne.getAddress());

        assertEquals(701, foundPersonOne.getId());

        assertEquals("Aaron", foundPersonOne.getFirstName());
        assertEquals("Oddy", foundPersonOne.getLastName());
        assertEquals("Male", foundPersonOne.getGender());
        assertEquals("01 Colorado Court", foundPersonOne.getAddress());
        assertFalse(foundPersonOne.getEnabled());

        PersonVO foundPersonFour = people.get(2);

        assertNotNull(foundPersonFour.getFirstName());
        assertNotNull(foundPersonFour.getLastName());
        assertNotNull(foundPersonFour.getGender());
        assertNotNull(foundPersonFour.getAddress());

        assertEquals(380, foundPersonFour.getId());

        assertEquals("Abe", foundPersonFour.getFirstName());
        assertEquals("Ciabatteri", foundPersonFour.getLastName());
        assertEquals("Male", foundPersonFour.getGender());
        assertEquals("181 Clove Street", foundPersonFour.getAddress());
        assertFalse(foundPersonFour.getEnabled());

    }

    @Test
    @Order(7)
    public void testFindAllWithoutToken() throws IOException {

        RequestSpecification specificationWithoutToken = new RequestSpecBuilder()
                .setBasePath("/api/person/v1")
                .setPort(TestConfigs.SERVER_PORT)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();

        given().spec(specificationWithoutToken)
                .contentType(TestConfigs.CONTENT_TYPE_XML)
                .accept(TestConfigs.CONTENT_TYPE_XML)
                .when()
                .get()
                .then()
                .statusCode(403);

    }

    @Test
    @Order(8)
    public void testFindByName() throws IOException {

        var content = given().spec(specification)
                .contentType(TestConfigs.CONTENT_TYPE_XML)
                .pathParam("firstName", "liana")
                .queryParams("page", 0, "size", 6, "direction", "asc")
                .accept(TestConfigs.CONTENT_TYPE_XML)
                .when()
                .get("findPersonByName/{firstName}")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();
        //.as(new TypeRef<List<PersonVO>>() {});

        PagedModelPerson wrapper = objectMapper.readValue(content, PagedModelPerson.class);

        var people = wrapper.getContent();

        PersonVO foundPersonOne = people.get(0);

        assertNotNull(foundPersonOne.getFirstName());
        assertNotNull(foundPersonOne.getLastName());
        assertNotNull(foundPersonOne.getGender());
        assertNotNull(foundPersonOne.getAddress());

        assertEquals(1, foundPersonOne.getId());

        assertEquals("Liana", foundPersonOne.getFirstName());
        assertEquals("Fernandes", foundPersonOne.getLastName());
        assertEquals("Female", foundPersonOne.getGender());
        assertEquals("Bauru", foundPersonOne.getAddress());
        assertTrue(foundPersonOne.getEnabled());

    }
    /*
    @Test
    @Order(9)
    public void testHATEOAS() throws IOException {

        var content = given().spec(specification)
                .contentType(TestConfigs.CONTENT_TYPE_XML)
                .queryParams("page", 3, "size", 10, "direction", "asc")
                .accept(TestConfigs.CONTENT_TYPE_XML)
                .when()
                .get()
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        assertTrue(content.contains("<links><rel>self</rel><href>http://localhost:8888/api/person/v1/677</href></links>"));
        assertTrue(content.contains("<links><rel>self</rel><href>http://localhost:8888/api/person/v1/846</href></links>"));
        assertTrue(content.contains("<links><rel>self</rel><href>http://localhost:8888/api/person/v1/714</href></links>"));

        assertTrue(content.contains("<links><rel>first</rel><href>http://localhost:8888/api/person/v1?direction=asc&amp;page=0&amp;size=10&amp;sort=firstName,asc</href></links>"));
        assertTrue(content.contains("<links><rel>prev</rel><href>http://localhost:8888/api/person/v1?direction=asc&amp;page=2&amp;size=10&amp;sort=firstName,asc</href></links>"));
        assertTrue(content.contains("<links><rel>self</rel><href>http://localhost:8888/api/person/v1?page=3&amp;size=10&amp;direction=asc</href></links>"));
        assertTrue(content.contains("<links><rel>next</rel><href>http://localhost:8888/api/person/v1?direction=asc&amp;page=4&amp;size=10&amp;sort=firstName,asc</href></links>"));
        assertTrue(content.contains("<links><rel>last</rel><href>http://localhost:8888/api/person/v1?direction=asc&amp;page=100&amp;size=10&amp;sort=firstName,asc</href></links>"));
        assertTrue(content.contains("<page><size>10</size><totalElements>1008</totalElements><totalPages>101</totalPages><number>3</number></page>"));
    }
    */
    private void mockPerson() {
        person.setFirstName("Lucas");
        person.setLastName("Peixoto");
        person.setGender("Male");
        person.setAddress("Bauru City, São Paulo");
        person.setEnabled(true);
    }

}
