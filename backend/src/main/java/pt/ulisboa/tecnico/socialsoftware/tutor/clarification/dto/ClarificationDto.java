package pt.ulisboa.tecnico.socialsoftware.tutor.clarification.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain.Clarification;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;

import java.io.Serializable;

public class ClarificationDto implements Serializable {
    private Integer id;
    private String content;
    private Integer userId;
    private Integer questionId;
    private boolean isAnswered;

    public ClarificationDto() {}

    public ClarificationDto(Clarification clarification) {
        this.id = clarification.getId();
        this.isAnswered = clarification.isAnswered();

        if (clarification.getContent() == null || clarification.getContent().isEmpty() || clarification.getContent().isBlank())
            throw new TutorException(ErrorMessage.CLARIFICATION_IS_EMPTY);
        else
            this.content = clarification.getContent();

        if (clarification.getUser() != null)
            this.userId = clarification.getUser().getId();
        else throw new TutorException(ErrorMessage.USER_NOT_FOUND, clarification.getUser().getId());

        if (clarification.getQuestion() != null)
            this.questionId = clarification.getQuestion().getId();
        else throw new TutorException(ErrorMessage.QUESTION_NOT_FOUND, clarification.getQuestion().getId());
    }

    public Integer getId() { return id; }

    public String getContent() { return content; }

    public Integer getUserId() { return userId; }

    public Integer getQuestionId() { return questionId; }

    public boolean isAnswered() { return isAnswered; }

    public void setId(Integer id) { this.id = id; }

    public void setContent(String content) { this.content = content; }

    public void setUserId(Integer userId) { this.userId = userId; }

    public void setQuestionId(Integer questionId) { this.questionId = questionId; }

    public void setAnswered(boolean answered) { this.isAnswered = answered; }

    @Override
    public String toString() {
        return "ClarificationDto{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", userId=" + userId +
                ", questionId=" + questionId +
                ", isAnswered=" + isAnswered +
                '}';
    }
}
