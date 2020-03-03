package pt.ulisboa.tecnico.socialsoftware.tutor.question.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Image;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.StudentQuestionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;


import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

@Entity
@Table(name = "student_question")
public class StudentQuestion {

    public enum Status {
        TOAPPROVE, APPROVED, REJECTED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private StudentQuestion.Status status = StudentQuestion.Status.TOAPPROVE;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String title;

    @OneToOne(cascade = CascadeType.ALL)
    private Image image;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Option> options = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<Topic> topics = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    public StudentQuestion() {
    }

    public StudentQuestion(Course course, User student,StudentQuestionDto ssquestionDto) {
        checkConsistentStudentQuestion(ssquestionDto);
        this.title = ssquestionDto.getTitle();
        this.content = ssquestionDto.getContent();
        this.status = StudentQuestion.Status.valueOf(ssquestionDto.getStatus());

        this.course = course;
        //course.addStudentQuestion(this);

        this.user = student;
        //course.addStudentQuestion(this);
        
        if (ssquestionDto.getImage() != null) {
            Image img = new Image(ssquestionDto.getImage());
            setImage(img);
            //img.setStudentQuestion(this);
        }

        int index = 0;
        for (OptionDto optionDto : ssquestionDto.getOptions()) {
            optionDto.setSequence(index++);
            Option option = new Option(optionDto);
            this.options.add(option);
            //option.setStudentQuestion(this);
        }
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

    public StudentQuestion.Status getStatus() {
        return status;
    }

    public void setStatus(StudentQuestion.Status status) {
        this.status = status;
    }

    public List<Option> getOptions() {
        return options;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
        //image.setStudentQuestion(this);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    

    public Set<Topic> getTopics() {
        return topics;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public void addOption(Option option) {
        options.add(option);
    }

    public void addTopic(Topic topic) {
        topics.add(topic);
    }

    public void remove() {
        //getCourse().getStudentQuestions().remove(this);
        course = null;
        //getTopics().forEach(topic -> topic.getStudentQuestions().remove(this));
        //getTopics().clear();
    }

    private void checkConsistentStudentQuestion(StudentQuestionDto squestionDto) {
        if (squestionDto.getTitle().trim().length() == 0 ||
                squestionDto.getContent().trim().length() == 0 ||
                squestionDto.getOptions().stream().anyMatch(optionDto -> optionDto.getContent().trim().length() == 0)) {
            throw new TutorException(STUDENT_QUESTION_MISSING_DATA);
        }

        if (squestionDto.getOptions().stream().filter(OptionDto::getCorrect).count() != 1) {
            throw new TutorException(STUDENT_QUESTION_MULTIPLE_CORRECT_OPTIONS);
        }

    }
}
