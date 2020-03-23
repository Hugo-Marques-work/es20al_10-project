package pt.ulisboa.tecnico.socialsoftware.tutor.clarification.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.ClarificationService
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain.Clarification
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain.ClarificationAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.repository.ClarificationAnswerRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.repository.ClarificationRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Shared
import spock.lang.Specification

@DataJpaTest
class GetClarificationAnswerSpockTest extends Specification {
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
    ClarificationRepository clarificationRepository

    @Autowired
    ClarificationAnswerRepository clarificationAnswerRepository

    @Shared
    Clarification clarification

    @Shared
    User user

    def setup(){
        clarification = new Clarification()
        clarification.setContent(CONTENT)
        clarificationRepository.save(clarification)

        user = new User(NAME, USERNAME, USER_KEY, ROLE)
        userRepository.save(user)
    }


    def "a clarification answer exists and gets it"(){
        given: "a clarification answer"
        def clarificationAnswer = new ClarificationAnswer(CONTENT, clarification, user)
        clarification.addClarificationAnswer(clarificationAnswer)
        user.addClarificationAnswer(clarificationAnswer)
        clarificationAnswerRepository.save(clarificationAnswer)

        when:
        def result = clarificationService.getClarificationAnswers(clarification.getId())

        then: "it returns only one clarification answer"
        result.size() == 1I

        and: "the correct clarification answer is obtained"
        def clarificationAnswerObtained = result.get(0)
        clarificationAnswerObtained.getContent() == CONTENT
        clarificationAnswerObtained.getUserKey() == USER_KEY
    }

    def "a clarification answer exists but it is given an invalid clarification id"(){
        given: "a clarification answer"
        def clarificationAnswer = new ClarificationAnswer(CONTENT, clarification, user)
        clarification.addClarificationAnswer(clarificationAnswer)
        user.addClarificationAnswer(clarificationAnswer)
        clarificationAnswerRepository.save(clarificationAnswer)

        when:
        def result = clarificationService.getClarificationAnswers(clarification.getId()+1)

        then: "an error is returned"
        def error = thrown(TutorException)
        error.errorMessage == ErrorMessage.CLARIFICATION_NOT_FOUND
    }

    def "no clarification answer exists"(){
        when:
        def result = clarificationService.getClarificationAnswers(clarification.getId())

        then: "an empty list is returned"
        result.size() == 0I
    }

    @TestConfiguration
    static class ClarificationServiceImplTestContextConfiguration {
        @Bean
        ClarificationService clarificationService() {
            return new ClarificationService()
        }
    }
}
