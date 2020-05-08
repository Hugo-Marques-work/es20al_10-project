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
    private boolean isAvailableByTeacher;
    private boolean isAvailableByStudent;

    public ClarificationDto() {}

    public ClarificationDto(Clarification clarification) {
        this(clarification, false);
    }

    public ClarificationDto(Clarification clarification, boolean checkAvailability) {
        this.id = clarification.getId();
        this.isAnswered = clarification.isAnswered();
        this.isAvailableByTeacher = (clarification.getAvailability() == Clarification.Availability.TEACHER ||
                clarification.getAvailability() == Clarification.Availability.BOTH );
        this.isAvailableByStudent = (clarification.getAvailability() == Clarification.Availability.STUDENT ||
                clarification.getAvailability() == Clarification.Availability.BOTH );

        if (clarification.getContent() == null || clarification.getContent().isEmpty() || clarification.getContent().isBlank())
            throw new TutorException(ErrorMessage.CLARIFICATION_IS_EMPTY);
        else
            this.content = clarification.getContent();


        if (!isAvailableByStudent && checkAvailability) {
            this.user = null;
        }
        else if (clarification.getUser() != null)
            this.user = new UserDto(clarification.getUser());
        else throw new TutorException(ErrorMessage.USER_NOT_FOUND);

        if (clarification.getQuestion() != null)
            this.question = new QuestionDto(clarification.getQuestion());
        else throw new TutorException(ErrorMessage.QUESTION_NOT_FOUND);
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

    public boolean isAvailableByTeacher() {
        return isAvailableByTeacher;
    }

    public void setAvailableByTeacher(boolean availableByTeacher) {
        isAvailableByTeacher = availableByTeacher;
    }

    public boolean isAvailableByStudent() {
        return isAvailableByStudent;
    }

    public void setAvailableByStudent(boolean availableByStudent) {
        isAvailableByStudent = availableByStudent;
    }

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
