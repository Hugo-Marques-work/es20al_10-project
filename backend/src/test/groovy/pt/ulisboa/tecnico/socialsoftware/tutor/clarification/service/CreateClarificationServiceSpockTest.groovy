package pt.ulisboa.tecnico.socialsoftware.tutor.clarification.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.AnswerService;
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.ClarificationService
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.repository.ClarificationRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseService
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.AnswersXmlImport
import pt.ulisboa.tecnico.socialsoftware.tutor.question.AssessmentService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.QuestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.TopicService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.QuizService;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserService
import spock.lang.Specification
import spock.lang.Unroll;

@DataJpaTest
class CreateClarificationServiceSpockTest extends Specification {
    static final String NAME = "test user"
    static final String USERNAME = "test_user"
    static final String USERNAME2 = "test_user_2"
    static final Integer KEY = 1
    static final User.Role ROLE = User.Role.STUDENT
    static final String CONTENT = "I want a clarification in this question."

    @Autowired
    ClarificationService clarificationService

    @Autowired
    UserRepository userRepository

    @Autowired
    QuestionRepository questionRepository

    @Autowired
    CourseRepository courseRepository

    @Autowired
    ClarificationRepository clarificationRepository

    def user
    def question

    def setup() {
        user = new User(NAME, USERNAME, ROLE)
        userRepository.save(user)
        question = new Question()
        def course = new Course("teste", Course.Type.TECNICO)
        courseRepository.save(course)
        question.setCourse(course)
        question.setTitle("Title")
        questionRepository.save(question)
    }

    def "question and user exists and creates clarification"() {
        when:
        def result = clarificationService.createClarification(question, user, CONTENT)

        then: "the returned data are correct"
        result.content == CONTENT
        and: "the clarification is added to the user"
        user.getClarifications().size() == 1
        def userClarifications = new ArrayList<>(user.getClarifications()).get(0)
        userClarifications != null
        and: "has the correct value"
        userClarifications.content == CONTENT
        and: "the clarification was added to the question"
        question.getClarifications().size() == 1
        def questionClarification = new ArrayList<>(question.getClarifications()).get(0)
        questionClarification != null
        and: "has the correct value"
        questionClarification.content == CONTENT
        and: "clarification was added to the repository"
        clarificationRepository.count() == 1
    }

    @Unroll("invalid arguments: #userRole | #content || #errorMessage")
    def "invalid inputs for user roles and content"() {
        given: "a user"
        def genericUser = new User(NAME, USERNAME2, userRole)
        userRepository.save(genericUser)

        when:
        clarificationService.createClarification(question, genericUser, content)

        then: "throws exception"
        def error = thrown(TutorException)
        error.errorMessage == errorMessage

        where:
        userRole             | content || errorMessage
        User.Role.ADMIN      | CONTENT || ErrorMessage.CLARIFICATION_WRONG_USER
        User.Role.DEMO_ADMIN | CONTENT || ErrorMessage.CLARIFICATION_WRONG_USER
        User.Role.TEACHER    | CONTENT || ErrorMessage.CLARIFICATION_WRONG_USER
        User.Role.STUDENT    | null    || ErrorMessage.CLARIFICATION_IS_EMPTY
        User.Role.STUDENT    | "  "    || ErrorMessage.CLARIFICATION_IS_EMPTY
    }

    def "question is not saved in the database" () {
        given: "a question not saved"
        def questionNotSaved = new Question()
        def course = new Course("teste", Course.Type.TECNICO)
        courseRepository.save(course)
        questionNotSaved.setCourse(course)
        questionNotSaved.setTitle("Title")
        questionRepository.save(questionNotSaved)
        questionRepository.delete(questionNotSaved)

        when:
        clarificationService.createClarification(questionNotSaved, user, CONTENT)

        then: "throws exception"
        def error = thrown(TutorException)
        error.errorMessage == ErrorMessage.QUESTION_NOT_FOUND
    }

    def "question is empty" () {
        when:
        clarificationService.createClarification(null, user, CONTENT)

        then: "throws exception"
        def error = thrown(TutorException)
        error.errorMessage == ErrorMessage.QUESTION_NOT_FOUND
    }

    def "user is not saved in the database" () {
        given: "a user not saved"
        def userNotSaved = new User()
        userRepository.save(userNotSaved)
        userRepository.delete(userNotSaved)

        when:
        clarificationService.createClarification(question, userNotSaved, CONTENT)

        then: "throws exception"
        def error = thrown(TutorException)
        error.errorMessage == ErrorMessage.USER_NOT_FOUND
    }

    def "user is empty" () {
        when:
        clarificationService.createClarification(question, null, CONTENT)

        then: "throws exception"
        def error = thrown(TutorException)
        error.errorMessage == ErrorMessage.USER_NOT_FOUND
    }

    @TestConfiguration
    static class ClarificationServiceImplTestContextConfiguration {
        @Bean
        ClarificationService clarificationService() {
            return new ClarificationService()
        }

        @Bean
        UserService userService() {
            return new UserService()
        }

        @Bean
        CourseService courseService() {
            return new CourseService()
        }

        @Bean
        AnswerService answerService() {
            return new AnswerService()
        }

        @Bean
        AnswersXmlImport answersXmlImport() {
            return new AnswersXmlImport()
        }

        @Bean
        QuizService quizService() {
            return new QuizService()
        }

        @Bean
        QuestionService questionService() {
            return new QuestionService()
        }

        @Bean
        TopicService topicService() {
            return new TopicService()
        }

        @Bean
        AssessmentService assessmentService() {
            return new AssessmentService()
        }
    }
}
