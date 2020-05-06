package pt.ulisboa.tecnico.socialsoftware.tutor.clarification.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.ClarificationService
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Shared
import spock.lang.Specification

@DataJpaTest
class toggleDashboardAvailabilitySpockTest extends Specification {
    static final String NAME = "test user"
    static final String USERNAME = "test_user"
    static final Integer USER_KEY = 1
    static final User.Role ROLE = User.Role.STUDENT

    @Autowired
    ClarificationService clarificationService

    @Autowired
    UserRepository userRepository

    @Shared
    User user

    def setup(){
        user = new User(NAME, USERNAME, USER_KEY, ROLE)
        userRepository.save(user)
    }

    def "change dashboard availability" () {
        when:
        clarificationService.changeDashboardAvailability(user.getId())

        then: "dashboard availability should be public"
        user.getDashboardPublic() == User.DashboardAvailability.PUBLIC
    }

    def "change dashboard availability twice" () {
        given: "a dashboard availability changed once"
        clarificationService.changeDashboardAvailability(user.getId())

        when:
        clarificationService.changeDashboardAvailability(user.getId())

        then: "dashboard availability should be public"
        user.getDashboardPublic() == User.DashboardAvailability.PRIVATE
    }

    def "check dashboard availability when its private" () {
        when:
        def result = clarificationService.getDashboardAvailability(user.getId())

        then: "dashboard availability should be public"
        !result
    }

    def "check dashboard availability when its public" () {
        given: "a dashboard availability changed once"
        clarificationService.changeDashboardAvailability(user.getId())

        when:
        def result = clarificationService.getDashboardAvailability(user.getId())

        then: "dashboard availability should be public"
        result
    }

    @TestConfiguration
    static class ClarificationServiceImplTestContextConfiguration {
        @Bean
        ClarificationService clarificationService() {
            return new ClarificationService()
        }
    }
}
