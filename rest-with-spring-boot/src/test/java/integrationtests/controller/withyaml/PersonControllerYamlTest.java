package integrationtests.controller.withyaml;

import com.api.restwithspringboot.Startup;
import config.TestConfigs;
import integrationtests.controller.withyaml.mapper.YMLMapper;
import integrationtests.testcontainers.AbstractIntegrationTest;
import integrationtests.vo.AccountCredentialsVO;
import integrationtests.vo.PersonVO;
import integrationtests.vo.TokenVO;
import integrationtests.vo.pagedmodels.PagedModelPerson;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.EncoderConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
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
public class PersonControllerYamlTest extends AbstractIntegrationTest {

    private static RequestSpecification specification;
    private static YMLMapper objectMapper;
    private static PersonVO person;

    @BeforeAll
    public static void setup() {
        objectMapper = new YMLMapper();
        person = new PersonVO();
    }

    @Test
    @Order(0)
    public void authorization() throws IOException {
        AccountCredentialsVO user = new AccountCredentialsVO("lucas", "admin123");

        var accessToken = given()
                .config(
                        RestAssuredConfig.config().encoderConfig(
                                EncoderConfig.encoderConfig()
                                        .encodeContentTypeAs(
                                                TestConfigs.CONTENT_TYPE_YML,
                                                ContentType.TEXT)
                        )
                )
                .basePath("/auth/signin")
                .port(TestConfigs.SERVER_PORT)
                .contentType(TestConfigs.CONTENT_TYPE_YML)
                .accept(TestConfigs.CONTENT_TYPE_YML)
                .body(user, objectMapper)
                .when()
                .post()
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(TokenVO.class, objectMapper)
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

        var createdPerson = given().spec(specification)
                .config(
                        RestAssuredConfig.config().encoderConfig(
                                EncoderConfig.encoderConfig()
                                        .encodeContentTypeAs(
                                                TestConfigs.CONTENT_TYPE_YML,
                                                ContentType.TEXT)
                        )
                )
                .contentType(TestConfigs.CONTENT_TYPE_YML)
                .accept(TestConfigs.CONTENT_TYPE_YML)
                .body(person, objectMapper)
                .when()
                .post()
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(PersonVO.class, objectMapper);

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

        var createdPerson = given().spec(specification)
                .config(
                        RestAssuredConfig.config().encoderConfig(
                                EncoderConfig.encoderConfig()
                                        .encodeContentTypeAs(
                                                TestConfigs.CONTENT_TYPE_YML,
                                                ContentType.TEXT)
                        )
                )
                .contentType(TestConfigs.CONTENT_TYPE_YML)
                .accept(TestConfigs.CONTENT_TYPE_YML)
                .body(person, objectMapper)
                .when()
                .post()
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(PersonVO.class, objectMapper);

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

        var persistedPerson = given().spec(specification)
                .config(
                        RestAssuredConfig.config().encoderConfig(
                                EncoderConfig.encoderConfig()
                                        .encodeContentTypeAs(
                                                TestConfigs.CONTENT_TYPE_YML,
                                                ContentType.TEXT)
                        )
                )
                .contentType(TestConfigs.CONTENT_TYPE_YML)
                .accept(TestConfigs.CONTENT_TYPE_YML)
                .header(TestConfigs.HEADER_PARAM_ORIGIN, TestConfigs.ORIGIN_ERUDIO)
                .pathParam("id", person.getId())
                .when()
                .patch("{id}")
                .then()
                .statusCode(200)
                .extract()
                .body().as(PersonVO.class, objectMapper);

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

        var persistedPerson = given().spec(specification)
                .config(
                        RestAssuredConfig.config().encoderConfig(
                                EncoderConfig.encoderConfig()
                                        .encodeContentTypeAs(
                                                TestConfigs.CONTENT_TYPE_YML,
                                                ContentType.TEXT)
                        )
                )
                .contentType(TestConfigs.CONTENT_TYPE_YML)
                .accept(TestConfigs.CONTENT_TYPE_YML)
                .header(TestConfigs.HEADER_PARAM_ORIGIN, TestConfigs.ORIGIN_ERUDIO)
                .pathParam("id", person.getId())
                .when()
                .get("{id}")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(PersonVO.class, objectMapper);

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
                .config(
                        RestAssuredConfig.config().encoderConfig(
                                EncoderConfig.encoderConfig()
                                        .encodeContentTypeAs(
                                                TestConfigs.CONTENT_TYPE_YML,
                                                ContentType.TEXT)
                        )
                )
                .contentType(TestConfigs.CONTENT_TYPE_YML)
                .accept(TestConfigs.CONTENT_TYPE_YML)
                .pathParam("id", person.getId())
                .when()
                .delete("{id}")
                .then();
    }

    @Test
    @Order(6)
    public void testFindAll() throws IOException {

        var wrapper = given().spec(specification)
                .config(
                        RestAssuredConfig.config().encoderConfig(
                                EncoderConfig.encoderConfig()
                                        .encodeContentTypeAs(
                                                TestConfigs.CONTENT_TYPE_YML,
                                                ContentType.TEXT)
                        )
                )
                .contentType(TestConfigs.CONTENT_TYPE_YML)
                .accept(TestConfigs.CONTENT_TYPE_YML)
                .when()
                .get()
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(PagedModelPerson.class, objectMapper);

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
                .config(
                        RestAssuredConfig.config().encoderConfig(
                                EncoderConfig.encoderConfig()
                                        .encodeContentTypeAs(
                                                TestConfigs.CONTENT_TYPE_YML,
                                                ContentType.TEXT)
                        )
                )
                .contentType(TestConfigs.CONTENT_TYPE_YML)
                .accept(TestConfigs.CONTENT_TYPE_YML)
                .when()
                .get()
                .then()
                .statusCode(403);

    }

    @Test
    @Order(8)
    public void testFindByName() throws IOException {

        var wrapper = given().spec(specification)
                .config(
                        RestAssuredConfig.config().encoderConfig(
                                EncoderConfig.encoderConfig()
                                        .encodeContentTypeAs(
                                                TestConfigs.CONTENT_TYPE_YML,
                                                ContentType.TEXT)
                        )
                )
                .contentType(TestConfigs.CONTENT_TYPE_YML)
                .pathParam("firstName", "liana")
                .queryParams("page", 0, "size", 6, "direction", "asc")
                .accept(TestConfigs.CONTENT_TYPE_YML)
                .when()
                .get("findPersonByName/{firstName}")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(PagedModelPerson.class, objectMapper);

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

        var unthreatedContent = given().spec(specification)
                .config(
                        RestAssuredConfig.config().encoderConfig(
                                EncoderConfig.encoderConfig()
                                        .encodeContentTypeAs(
                                                TestConfigs.CONTENT_TYPE_YML,
                                                ContentType.TEXT)
                        )
                )
                .contentType(TestConfigs.CONTENT_TYPE_YML)
                .accept(TestConfigs.CONTENT_TYPE_YML)
                .queryParams("page", 3, "size", 10, "direction", "asc")
                .when()
                .get()
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        var content = unthreatedContent.replace("\n", "").replace("\r", "");

        assertTrue(content.contains("rel: \"self\"    href: \"http://localhost:8888/api/person/v1/677\""));
        assertTrue(content.contains("rel: \"self\"    href: \"http://localhost:8888/api/person/v1/846\""));
        assertTrue(content.contains("rel: \"self\"    href: \"http://localhost:8888/api/person/v1/714\""));

        assertTrue(content.contains("rel: \"first\"  href: \"http://localhost:8888/api/person/v1?direction=asc&page=0&size=10&sort=firstName,asc\""));
        assertTrue(content.contains("rel: \"prev\"  href: \"http://localhost:8888/api/person/v1?direction=asc&page=2&size=10&sort=firstName,asc\""));
        assertTrue(content.contains("rel: \"self\"  href: \"http://localhost:8888/api/person/v1?page=3&size=10&direction=asc\""));
        assertTrue(content.contains("rel: \"next\"  href: \"http://localhost:8888/api/person/v1?direction=asc&page=4&size=10&sort=firstName,asc\""));
        assertTrue(content.contains("rel: \"last\"  href: \"http://localhost:8888/api/person/v1?direction=asc&page=100&size=10&sort=firstName,asc\""));

        assertTrue(content.contains("page:  size: 10  totalElements: 1008  totalPages: 101  number: 3"));

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
