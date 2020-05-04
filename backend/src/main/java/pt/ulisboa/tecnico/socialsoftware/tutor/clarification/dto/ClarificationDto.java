package pt.ulisboa.tecnico.socialsoftware.tutor.clarification.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain.Clarification;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.dto.UserDto;

import java.io.Serializable;

public class ClarificationDto implements Serializable {
    private Integer id;
    private String content;
    private UserDto user;
    private QuestionDto question;
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
            this.user = new UserDto(clarification.getUser());
        else throw new TutorException(ErrorMessage.USER_NOT_FOUND, "unknown");

        if (clarification.getQuestion() != null)
            this.question = new QuestionDto(clarification.getQuestion());
        else throw new TutorException(ErrorMessage.QUESTION_NOT_FOUND, "unknown");
    }

    public Integer getId() { return id; }

    public String getContent() { return content; }

    public UserDto getUser() {
        return user;
    }

    public void setUser(UserDto user) {
        this.user = user;
    }

    public boolean isAnswered() { return isAnswered; }

    public void setId(Integer id) { this.id = id; }

    public void setContent(String content) { this.content = content; }

    public QuestionDto getQuestion() {
        return question;
    }

    public void setQuestion(QuestionDto question) {
        this.question = question;
    }

    public void setAnswered(boolean answered) { this.isAnswered = answered; }

    @Override
    public String toString() {
        return "ClarificationDto{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", userId=" + user.getId() +
                ", questionId=" + question.getId() +
                ", isAnswered=" + isAnswered +
                '}';
    }
}
