package pt.ulisboa.tecnico.socialsoftware.tutor.clarification.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain.Clarification;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;

import java.io.Serializable;

public class ClarificationDto implements Serializable {
    private Integer id;
    private String content;

    public ClarificationDto() {}

    public ClarificationDto(Clarification clarification) {
        this.id = clarification.getId();

        if (clarification.getContent() == null || clarification.getContent().isEmpty() || clarification.getContent().isBlank())
            throw new TutorException(ErrorMessage.CLARIFICATION_IS_EMPTY);
        else
            this.content = clarification.getContent();
    }

    public Integer getId() { return id; }

    public String getContent() { return content; }

    public void setId(Integer id) { this.id = id; }

    public void setContent(String content) { this.content = content; }

    @Override
    public String toString() {
        return "ClarificationDto{" +
                "id=" + id +
                ", content='" + content + '\'' +
                '}';
    }
}
