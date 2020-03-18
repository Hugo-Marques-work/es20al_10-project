package pt.ulisboa.tecnico.socialsoftware.tutor.clarification.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain.ClarificationAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

import java.io.Serializable;

public class ClarificationAnswerDto implements Serializable {
    private Integer id;
    private String content;
    private Integer userKey;

    public ClarificationAnswerDto() {}

    public ClarificationAnswerDto(ClarificationAnswer clarificationAnswer) {
        this.id = clarificationAnswer.getId();
        this.userKey = clarificationAnswer.getUser().getKey();

        if (clarificationAnswer.getContent() == null
                || clarificationAnswer.getContent().isEmpty() || clarificationAnswer.getContent().isBlank())
            throw new TutorException(ErrorMessage.CLARIFICATION_IS_EMPTY);
        else
            this.content = clarificationAnswer.getContent();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getUserKey() {
        return userKey;
    }

    public void setUserKey(Integer userKey) {
        this.userKey = userKey;
    }

    @Override
    public String toString() {
        return "ClarificationAnswerDto{" +
                "id=" + id +
                ", content='" + content + '\'' +
                '}';
    }
}
