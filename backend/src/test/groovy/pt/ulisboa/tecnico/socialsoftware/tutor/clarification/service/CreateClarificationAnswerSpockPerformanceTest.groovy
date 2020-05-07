package pt.ulisboa.tecnico.socialsoftware.tutor.clarification.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.ClarificationService
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain.Clarification
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.repository.ClarificationAnswerRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.repository.ClarificationRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification

@DataJpaTest
class CreateClarificationAnswerSpockPerformanceTest extends Specification {
    static final String NAME = "test user"
    static final String USERNAME = "test_user"
    static final String USERNAME2 = "test_user2"
    static final Integer USER_KEY = 1
    static final Integer USER_KEY2 = 2
    static final String CONTENT = "I explain your clarification."

    @Autowired
    ClarificationService clarificationService

    @Autowired
    UserRepository userRepository

    @Autowired
    ClarificationRepository clarificationRepository

    @Autowired
    ClarificationAnswerRepository clarificationAnswerRepository

    def "performance testing to create 5000 clarification answers"(){
        given: "a teacher"
        def teacher = new User(NAME, USERNAME, USER_KEY, User.Role.TEACHER)
        userRepository.save(teacher)
        and: "a student"
        def student = new User(NAME, USERNAME2, USER_KEY2, User.Role.STUDENT)
        userRepository.save(student)
        and: "a clarification"
        def clarification = new Clarification()
        clarification.setContent(CONTENT)
        clarification.setUser(student)
        clarificationRepository.save(clarification)

        when:
        1.upto(1, {
            clarificationService.createClarificationAnswer(clarification, teacher, CONTENT)
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
