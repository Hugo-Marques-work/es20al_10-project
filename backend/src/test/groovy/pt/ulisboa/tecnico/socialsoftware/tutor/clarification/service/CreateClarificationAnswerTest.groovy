package pt.ulisboa.tecnico.socialsoftware.tutor.clarification.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.AnswerService
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.ClarificationService
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain.Clarification
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain.ClarificationAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.dto.ClarificationAnswerDto
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.repository.ClarificationAnswerRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.repository.ClarificationRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseService
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.AnswersXmlImport
import pt.ulisboa.tecnico.socialsoftware.tutor.question.AssessmentService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.QuestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.TopicService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.QuizService
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserService
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

@DataJpaTest
class CreateClarificationAnswerTest extends Specification {
    static final String NAME = "test user"
    static final String USERNAME = "test_user"
    static final String USERNAME2 = "test_user2"
    static final String CONTENT = "I explain your clarification."

    static final enum unitType {
        EXISTENT,
        INEXISTENT,
        NULL
    }

    @Autowired
    ClarificationService clarificationService

    @Autowired
    UserRepository userRepository

    @Autowired
    QuestionRepository questionRepository

    @Autowired
    ClarificationRepository clarificationRepository

    @Autowired
    ClarificationAnswerRepository clarificationAnswerRepository

    @Shared
    Clarification clarification

    @Shared
    User user

    def setup(){
        user = new User(NAME, USERNAME, User.Role.STUDENT)
        userRepository.save(user)
        clarification = new Clarification()
        clarification.setContent(CONTENT)
        clarification.setUser(user)
    }

    def "student replies to the teacher answer"(){
        given: "a clarification"
        clarificationRepository.save(clarification)
        and: "a clarification answer from a teacher"
        def teacher = new User("teacher", "teacher", User.Role.TEACHER)
        userRepository.save(teacher)
        clarificationService.createClarificationAnswer(clarification, teacher, CONTENT)

        when:
        clarificationService.createClarificationAnswer(clarification, user, CONTENT)

        then: "the correct clarification answer is inside the repository"
        clarificationAnswerRepository.count() == 2
        def result = clarificationAnswerRepository.findAll().get(1)
        result.getContent() == CONTENT
        and: "the clarification answer was added to the clarification"
        clarification.getClarificationAnswers().size() == 2
    }

    def "student tries to send a second clarifications before it is answered"(){
        given: "a clarification"
        clarificationRepository.save(clarification)

        when:
        clarificationService.createClarificationAnswer(clarification, user, CONTENT)

        then: "throws exception"
        def error = thrown(TutorException)
        error.errorMessage == ErrorMessage.CLARIFICATION_SAME_USER
    }

    @Unroll("invalid user and clarification: #clarificationType | #userType | #userRole || errorMessage")
    def "invalid user and clarification"() {

        when:
        clarificationService.createClarificationAnswer(getClarification(clarificationType), getUser(userType, userRole), CONTENT)

        then: "an exception is thrown"
        def error = thrown(TutorException)
        error.errorMessage == errorMessage

        where:
        clarificationType   | userType              | userRole            ||  errorMessage
        unitType.INEXISTENT | unitType.EXISTENT     | User.Role.TEACHER   ||  ErrorMessage.CLARIFICATION_NOT_FOUND
        unitType.INEXISTENT | unitType.EXISTENT     | User.Role.STUDENT   ||  ErrorMessage.CLARIFICATION_NOT_FOUND
        unitType.NULL       | unitType.EXISTENT     | User.Role.TEACHER   ||  ErrorMessage.CLARIFICATION_NOT_FOUND
        unitType.NULL       | unitType.EXISTENT     | User.Role.STUDENT   ||  ErrorMessage.CLARIFICATION_NOT_FOUND
        unitType.EXISTENT   | unitType.INEXISTENT   | User.Role.TEACHER   ||  ErrorMessage.USER_NOT_FOUND
        unitType.EXISTENT   | unitType.INEXISTENT   | User.Role.STUDENT   ||  ErrorMessage.USER_NOT_FOUND
        unitType.EXISTENT   | unitType.NULL         | User.Role.TEACHER   ||  ErrorMessage.USER_NOT_FOUND
        unitType.EXISTENT   | unitType.NULL         | User.Role.STUDENT   ||  ErrorMessage.USER_NOT_FOUND
    }

    @Unroll("invalid arguments: #content || errorMessage")
    def "invalid input values"(){
        given: "a user"
        def teacher = new User(NAME, USERNAME2, User.Role.TEACHER)
        userRepository.save(teacher)
        and: "a clarification"
        clarificationRepository.save(clarification)

        when:
        clarificationService.createClarificationAnswer(clarification, teacher, content)

        then: "an exception is thrown"
        def error = thrown(TutorException)
        error.errorMessage == errorMessage

        where:
        content ||  errorMessage
        null    ||  ErrorMessage.CLARIFICATION_ANSWER_IS_EMPTY
        "  "    ||  ErrorMessage.CLARIFICATION_ANSWER_IS_EMPTY
    }

    def getUser(type, userRole) {
        def newUser = new User(NAME, USERNAME2, userRole)

        switch (type) {
            case unitType.EXISTENT:
                userRepository.save(newUser)
                return newUser
            case unitType.INEXISTENT:
                userRepository.save(newUser)
                userRepository.delete(newUser)
                return newUser
            case unitType.NULL:
            default:
                return null
        }
    }

    def getClarification(type) {
        switch (type) {
            case unitType.EXISTENT:
                clarificationRepository.save(clarification)
                return clarification
            case unitType.INEXISTENT:
                clarification.setId(1)
                return clarification
            case unitType.NULL:
            default:
                return null
        }
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
