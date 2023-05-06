package integrationtests.vo.wrappers;

import com.fasterxml.jackson.annotation.JsonProperty;
import integrationtests.vo.PersonVO;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class PersonEmbeddedVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @JsonProperty(value = "personVOList")
    private List<PersonVO> persons;

    public PersonEmbeddedVO() {}

    public List<PersonVO> getPersons() {
        return persons;
    }

    public void setPersons(List<PersonVO> persons) {
        this.persons = persons;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PersonEmbeddedVO that)) return false;
        return getPersons().equals(that.getPersons());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPersons());
    }
}
