package pt.ulisboa.tecnico.socialsoftware.tutor.clarification.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain.Clarification;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;

import java.io.Serializable;

public class ClarificationDto implements Serializable {
    private Integer id;
    private String content;
    private Integer userKey;
    private Integer questionKey;

    public ClarificationDto() {}

    public ClarificationDto(Clarification clarification) {
        this.id = clarification.getId();

        if (clarification.getContent() == null || clarification.getContent().isEmpty() || clarification.getContent().isBlank())
            throw new TutorException(ErrorMessage.CLARIFICATION_IS_EMPTY);
        else
            this.content = clarification.getContent();

        if (clarification.getUser() != null)
            this.userKey = clarification.getUser().getKey();
        else throw new TutorException(ErrorMessage.USER_NOT_FOUND, clarification.getUser().getKey());

        if (clarification.getQuestion() != null)
            this.questionKey = clarification.getQuestion().getKey();
        else throw new TutorException(ErrorMessage.QUESTION_NOT_FOUND, clarification.getQuestion().getKey());
    }

    public Integer getId() { return id; }

    public String getContent() { return content; }

    public Integer getUserKey() { return userKey; }

    public Integer getQuestionKey() { return questionKey; }

    public void setId(Integer id) { this.id = id; }

    public void setContent(String content) { this.content = content; }

    public void setUserKey(Integer userKey) { this.userKey = userKey; }

    public void setQuestionKey(Integer questionKey) { this.questionKey = questionKey; }

    @Override
    public String toString() {
        return "ClarificationDto{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", userKey=" + userKey +
                ", questionKey=" + questionKey +
                '}';
    }
}
