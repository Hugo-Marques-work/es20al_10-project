package pt.ulisboa.tecnico.socialsoftware.tutor.question.dto;

import org.springframework.data.annotation.Transient;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion;

import java.io.Serializable;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class StudentQuestionDto implements Serializable {
    private Integer id;
    private String title;
    private String content;
    private String status;
    private List<OptionDto> options = new ArrayList<>();
    private ImageDto image;
    private List<TopicDto> topics = new ArrayList<>();

    public StudentQuestionDto() {
    }

    public StudentQuestionDto(StudentQuestion squestion) {
        this.id = squestion.getId();
        this.title = squestion.getTitle();
        this.content = squestion.getContent();
        this.status = squestion.getStatus().name();
        this.options = squestion.getOptions().stream().map(OptionDto::new).collect(Collectors.toList());
        this.topics = squestion.getTopics().stream().sorted(Comparator.comparing(Topic::getName)).map(TopicDto::new).collect(Collectors.toList());

        if (squestion.getImage() != null)
            this.image = new ImageDto(squestion.getImage());

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<OptionDto> getOptions() {
        return options;
    }

    public void setOptions(List<OptionDto> options) {
        this.options = options;
    }

    public ImageDto getImage() {
        return image;
    }

    public void setImage(ImageDto image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<TopicDto> getTopics() {
        return topics;
    }

    public void setTopics(List<TopicDto> topics) {
        this.topics = topics;
    }

    @Override
    public String toString() {
        return "StudentQuestionDto{" +
                "id=" + id +
                ", id=" + id +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", status='" + status + '\'' +
                ", options=" + options +
                ", image=" + image +
                ", topics=" + topics +
                '}';
    }
}
