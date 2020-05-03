package pt.ulisboa.tecnico.socialsoftware.tutor.clarification.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.ClarificationService
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain.Clarification
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain.ClarificationAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.dto.ClarificationAnswerDto
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.repository.ClarificationAnswerRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.repository.ClarificationRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

@DataJpaTest
class CreateClarificationAnswerTest extends Specification {
    static final String NAME = "test user"
    static final String USERNAME = "test_user"
    static final Integer USER_KEY = 1
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

    def setup(){
        clarification = new Clarification()
        clarification.setContent(CONTENT)
    }

    def "student replies to the teacher answer"(){
        given: "a user"
        def user = new User(NAME, USERNAME, USER_KEY, User.Role.STUDENT)
        userRepository.save(user)
        and: "a clarification"
        clarification.setUser(user)
        clarificationRepository.save(clarification)
        and: "a clarification answer from a teacher"
        def teacher = new User("teacher", "teacher", USER_KEY + 1, User.Role.TEACHER)
        userRepository.save(teacher)
        clarificationService.createClarificationAnswer(clarification, teacher, CONTENT)

        when:
        clarificationService.createClarificationAnswer(clarification, user, CONTENT)

        then:
        then: "the correct clarification answer is inside the repository"
        clarificationAnswerRepository.count() == 2
        def result = clarificationAnswerRepository.findAll().get(1)
        result.getContent() == CONTENT
        and: "the clarification answer was added to the clarification"
        clarification.getClarificationAnswers().size() == 2
    }

    @Unroll("user and clarification exists and creates clarification answers: #userType | #userRole")
    def "user and clarification exists and creates clarification answers"(){
        when:
        clarificationService.createClarificationAnswer(makeClarificationWithUser(getUser(userType, userRole)), clarification.getUser(), CONTENT)

        then: "the correct clarification answer is inside the repository"
        clarificationAnswerRepository.count() == 1
        def result = clarificationAnswerRepository.findAll().get(0)
        result.getContent() == CONTENT
        and: "the clarification answer was added to the clarification"
        clarification.getClarificationAnswers().size() == 1

        where:
        userType              || userRole
        unitType.EXISTENT     || User.Role.TEACHER
        unitType.EXISTENT     || User.Role.STUDENT
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

    @Unroll("invalid arguments: #content | #role || errorMessage")
    def "invalid input values"(){
        given: "a user"
        def user = new User(NAME, USERNAME, USER_KEY, role)
        userRepository.save(user)
        and: "a clarification"
        clarificationRepository.save(clarification)

        when:
        clarificationService.createClarificationAnswer(clarification, user, content)

        then: "an exception is thrown"
        def error = thrown(TutorException)
        error.errorMessage == errorMessage

        where:
        content |   role                ||  errorMessage
        null    |   User.Role.TEACHER   ||  ErrorMessage.CLARIFICATION_ANSWER_IS_EMPTY
        null    |   User.Role.STUDENT   ||  ErrorMessage.CLARIFICATION_ANSWER_IS_EMPTY
        "  "    |   User.Role.TEACHER   ||  ErrorMessage.CLARIFICATION_ANSWER_IS_EMPTY
        "  "    |   User.Role.STUDENT   ||  ErrorMessage.CLARIFICATION_ANSWER_IS_EMPTY
    }

    def getUser(type, userRole) {
        def user = new User(NAME, USERNAME, USER_KEY, userRole)

        switch (type) {
            case unitType.EXISTENT:
                userRepository.save(user)
                return user
            case unitType.INEXISTENT:
                userRepository.save(user)
                userRepository.delete(user)
                return user
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

    def makeClarificationWithUser(user) {
        clarification.setUser(user)
        clarificationRepository.save(clarification)
        return clarification
    }

    @TestConfiguration
    static class ClarificationServiceImplTestContextConfiguration {
        @Bean
        ClarificationService clarificationService() {
            return new ClarificationService()
        }
    }
}
