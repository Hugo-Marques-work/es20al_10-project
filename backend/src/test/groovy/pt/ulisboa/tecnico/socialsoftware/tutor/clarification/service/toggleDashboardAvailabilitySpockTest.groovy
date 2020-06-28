package pt.ulisboa.tecnico.socialsoftware.tutor.clarification.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.AnswerService
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.ClarificationService
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseService
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.AnswersXmlImport
import pt.ulisboa.tecnico.socialsoftware.tutor.question.AssessmentService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.QuestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.TopicService
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.QuizService
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserService
import spock.lang.Shared
import spock.lang.Specification

@DataJpaTest
class toggleDashboardAvailabilitySpockTest extends Specification {
    static final String NAME = "test user"
    static final String USERNAME = "test_user"
    static final User.Role ROLE = User.Role.STUDENT

    @Autowired
    ClarificationService clarificationService

    @Autowired
    UserRepository userRepository

    @Shared
    User user

    def setup(){
        user = new User(NAME, USERNAME, ROLE)
        userRepository.save(user)
    }

    def "dashboard availability is private by default" () {
        when:
        def result = clarificationService.getDashboardAvailability(user.getId())

        then: "false means dashboard availability is set to private"
        !result
    }

    def "change dashboard availability" () {
        when:
        clarificationService.changeDashboardAvailability(user.getId())

        then: "dashboard availability should be public"
        user.getDashboardPublic() == User.ClarificationDashboardAvailability.PUBLIC
    }

    def "change dashboard availability twice" () {
        given: "a dashboard availability changed once"
        clarificationService.changeDashboardAvailability(user.getId())

        when:
        clarificationService.changeDashboardAvailability(user.getId())

        then: "dashboard availability should be public"
        user.getDashboardPublic() == User.ClarificationDashboardAvailability.PRIVATE
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
