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
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification

@DataJpaTest
class GetClarificationAnswerSpockPerformanceTest extends Specification {
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

    def "performance testing to get 1000 clarification answers"(){
        given: "a user"
        def user = new User(NAME, USERNAME, USER_KEY, ROLE)
        userRepository.save(user)
        and: "a clarification"
        def clarification = new Clarification()
        clarification.setContent(CONTENT)
        clarificationRepository.save(clarification)
        def clarification_id = clarificationRepository.findAll().get(0).getId()
        and: "1000 clarification answers"
        1.upto(1, {
            clarificationAnswerRepository.save(new ClarificationAnswer(CONTENT, clarification, user))
        })

        when:
        1.upto(1, {
            clarificationService.getClarificationAnswers(clarification_id)
        })

        then:
        true
    }

    @TestConfiguration
    static class ClarificationServiceImplTestContextConfiguration {
        @Bean
        ClarificationService clarificationService() {
            return new ClarificationService()
        }
    }
}
