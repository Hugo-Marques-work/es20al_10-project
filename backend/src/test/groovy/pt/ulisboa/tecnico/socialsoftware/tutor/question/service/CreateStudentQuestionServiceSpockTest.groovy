package pt.ulisboa.tecnico.socialsoftware.tutor.question.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.QuestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.StudentQuestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.StudentQuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.StudentQuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification

@DataJpaTest
class CreateStudentQuestionServiceSpockTest extends Specification{
    public static final String COURSE_NAME = "Software Architecture"
    public static final String ACRONYM = "AS1"
    public static final String ACADEMIC_TERM = "1 SEM"
    public static final String QUESTION_TITLE = 'question title'
    public static final String QUESTION_CONTENT = 'question content'
    public static final String OPTION_CONTENT = "optionId content"
    public static final String URL = 'URL'
    public static final String NAME = "test user"
    public static final String USERNAME = "test_user"
    public static final Integer KEY = 1
    public static final User.Role ROLE = User.Role.STUDENT


    @Autowired
    StudentQuestionService squestionService

    @Autowired
    CourseRepository courseRepository

    @Autowired
    CourseExecutionRepository courseExecutionRepository

    @Autowired
    UserRepository userRepository

    @Autowired
    StudentQuestionRepository studentquestionRepository;

    def course
    def courseExecution
    def user

    def setup() {
        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)

        courseExecution = new CourseExecution(course, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecutionRepository.save(courseExecution)

        user = new User(NAME, USERNAME, KEY, ROLE)
        userRepository.save(user)


    }

    def "create a student question with no image and one option"() {
        given: "a studentquestionDto"
        def squestionDto = new StudentQuestionDto()
        squestionDto.setTitle(QUESTION_TITLE)
        squestionDto.setContent(QUESTION_CONTENT)
        squestionDto.setStatus(StudentQuestion.Status.TOAPPROVE.name())
        and: 'a optionId'
        def optionDto = new OptionDto()
        optionDto.setContent(OPTION_CONTENT)
        optionDto.setCorrect(true)
        def options = new ArrayList<OptionDto>()
        options.add(optionDto)
        squestionDto.setOptions(options)

        when:
        squestionService.createStudentQuestion(course.getId(), user.getId(),squestionDto)

        then: "the correct question is inside the repository"
        studentquestionRepository.count() == 1L
        def result = studentquestionRepository.findAll().get(0)
        result.getId() != null
        result.getStatus() == StudentQuestion.Status.TOAPPROVE
        result.getTitle() == QUESTION_TITLE
        result.getContent() == QUESTION_CONTENT
        result.getImage() == null
        result.getOptions().size() == 1
        result.getCourse().getName() == COURSE_NAME
        //course.getQuestions().contains(result)
        //user.getQuestions().contains(result)
        def resOption = result.getOptions().get(0)
        resOption.getContent() == OPTION_CONTENT
        resOption.getCorrect()

    }

    @TestConfiguration
    static class StudentQuestionServiceImplTestContextConfiguration {

        @Bean
        StudentQuestionService squestionService() {
            return new StudentQuestionService()
        }
    }

}
