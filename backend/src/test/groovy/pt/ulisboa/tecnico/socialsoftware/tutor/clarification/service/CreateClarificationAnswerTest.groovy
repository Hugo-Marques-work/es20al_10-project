package pt.ulisboa.tecnico.socialsoftware.tutor.clarification.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.ClarificationService
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain.Clarification
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
    static final User.Role ROLE = User.Role.TEACHER
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


    def "user and clarification exists and creates clarification answers"(){
        given: "a user"
        def user = new User(NAME, USERNAME, USER_KEY, ROLE)
        userRepository.save(user)
        and: "a clarification"
        clarificationRepository.save(clarification)

        when:
        clarificationService.createClarificationAnswer(clarification, user, CONTENT)

        then: "the correct clarification answer is inside the repository"
        clarificationAnswerRepository.count() == 1
        def result = clarificationAnswerRepository.findAll().get(0)
        result.getContent() == CONTENT
        and: "the clarification answer was added to the clarification"
        clarification.getClarificationAnswers().size() == 1
    }

    @Unroll("invalid user and clarification: #clarificationType | #userType || errorMessage")
    def "invalid user and clarification"() {

        when:
        clarificationService.createClarificationAnswer(getClarification(clarificationType), getUser(userType), CONTENT)

        then: "an exception is thrown"
        def error = thrown(TutorException)
        error.errorMessage == errorMessage

        where:
        clarificationType   | userType              ||  errorMessage
        unitType.INEXISTENT | unitType.EXISTENT     ||  ErrorMessage.CLARIFICATION_NOT_FOUND
        unitType.NULL       | unitType.EXISTENT     ||  ErrorMessage.CLARIFICATION_NOT_FOUND
        unitType.EXISTENT   | unitType.INEXISTENT   ||  ErrorMessage.USER_NOT_FOUND
        unitType.EXISTENT   | unitType.NULL         ||  ErrorMessage.USER_NOT_FOUND
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
        "  "    |   User.Role.TEACHER   ||  ErrorMessage.CLARIFICATION_ANSWER_IS_EMPTY
        CONTENT |   User.Role.STUDENT   ||  ErrorMessage.CLARIFICATION_WRONG_USER
    }

    def getUser(type) {
        def user = new User(NAME, USERNAME, USER_KEY, ROLE)

        switch (type) {
            case unitType.EXISTENT:
                userRepository.save(user)
                return user
            case unitType.INEXISTENT:
                user.setId(1)
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

    @TestConfiguration
    static class ClarificationServiceImplTestContextConfiguration {
        @Bean
        ClarificationService clarificationService() {
            return new ClarificationService()
        }
    }
}
