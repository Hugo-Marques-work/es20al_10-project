package pt.ulisboa.tecnico.socialsoftware.tutor.clarification.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain.Clarification;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.dto.UserDto;

import java.io.Serializable;

public class ClarificationDto implements Serializable {
    private Integer id;
    private String content;
    private UserDto user;
    private QuestionDto question;

    public ClarificationDto() {}

    public ClarificationDto(Clarification clarification) {
        this.id = clarification.getId();
        this.content = clarification.getContent();

        if (clarification.getUser() != null)
            this.user = new UserDto(clarification.getUser());

        if (clarification.getQuestion() != null)
            this.question = new QuestionDto(clarification.getQuestion());
    }

    public Integer getId() { return id; }

    public String getContent() { return content; }

    public UserDto getUser() { return user; }

    public QuestionDto getQuestion() { return question; }

    public void setId(Integer id) { this.id = id; }

    public void setContent(String content) { this.content = content; }

    public void setUser(UserDto user) { this.user = user; }

    public void setQuestion(QuestionDto question) { this.question = question; }

    @Override
    public String toString() {
        return "ClarificationDto{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", user=" + user +
                ", question=" + question +
                '}';
    }
}
