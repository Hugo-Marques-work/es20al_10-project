package pt.ulisboa.tecnico.socialsoftware.tutor.clarification.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.ClarificationService
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain.Clarification
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.repository.ClarificationAnswerRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.repository.ClarificationRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Shared
import spock.lang.Specification

@DataJpaTest
class CreateClarificationAnswerTest extends Specification {
    static final String NAME = "test user"
    static final String USERNAME = "test_user"
    static final Integer USER_KEY = 1
    static final User.Role ROLE = User.Role.TEACHER
    static final String CONTENT = "I explain your clarification."

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
        clarification.setId(1)
        clarification.setContent("")
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
        result.content == CONTENT
        and: "the clarification answer was added to the clarification"
        clarification.getClarificationAnswers().size() == 1
    }

    def "clarification doesn't exist"(){
        given: "a user"
        def user = new User(NAME, USERNAME, USER_KEY, ROLE)
        userRepository.save(user)
        and: "a clarification"

        when:
        clarificationService.createClarificationAnswer(clarification, user, CONTENT)

        then: "an exception is thrown"
        thrown(TutorException)
    }

    def "user doesn't exist"() {
        given: "a user"
        def user = new User(NAME, USERNAME, USER_KEY, ROLE)
        and: "a clarification"
        clarificationRepository.save(clarification)

        when:
        clarificationService.createClarificationAnswer(clarification, user, CONTENT)

        then: "an exception is thrown"
        thrown(TutorException)
    }

    def "user is not a teacher"() {
        given: "a user"
        def user = new User(NAME, USERNAME, USER_KEY, User.Role.STUDENT)
        userRepository.save(user)
        and: "a clarification"
        clarificationRepository.save(clarification)

        when:
        clarificationService.createClarificationAnswer(clarification, user, CONTENT)

        then: "an exception is thrown"
        thrown(TutorException)
    }

    def "content is blank"(){
        given: "a user"
        def user = new User(NAME, USERNAME, USER_KEY, User.Role.STUDENT)
        userRepository.save(user)
        and: "a clarification"
        clarificationRepository.save(clarification)

        when:
        clarificationService.createClarificationAnswer(clarification, user, null)

        then: "an exception is thrown"
        thrown(TutorException)
    }

    def "content is empty"(){
        given: "a user"
        def user = new User(NAME, USERNAME, USER_KEY, User.Role.STUDENT)
        userRepository.save(user)
        and: "a clarification"
        clarificationRepository.save(clarification)

        when:
        clarificationService.createClarificationAnswer(clarification, user, "   ")

        then: "an exception is thrown"
        thrown(TutorException)
    }

    @TestConfiguration
    static class ClarificationServiceImplTestContextConfiguration {
        @Bean
        ClarificationService clarificationService() {
            return new ClarificationService()
        }
    }
}
