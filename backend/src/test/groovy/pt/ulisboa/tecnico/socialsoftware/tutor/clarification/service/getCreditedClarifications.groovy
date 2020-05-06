package pt.ulisboa.tecnico.socialsoftware.tutor.clarification.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.ClarificationService
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain.Clarification
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.repository.ClarificationAnswerRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.repository.ClarificationRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Shared
import spock.lang.Specification

@DataJpaTest
class getCreditedClarifications extends Specification {
    static final String NAME = "test user"
    static final String USERNAME = "test_user"
    static final Integer KEY = 1
    static final User.Role ROLE = User.Role.STUDENT
    static final String CONTENT = "I want a clarification in this question."
    static final String CONTENT_AVAILABLE = "I want a clarification available in this question."
    static final String COURSE_NAME = "Software Architecture"
    static final int NON_EXISTING_ID = 1000

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

    @Autowired
    ClarificationAnswerRepository clarificationAnswerRepository

    @Shared
    User user
    Question question
    Course course
    Clarification clarification1
    Clarification clarification2

    def setup(){
        user = new User(NAME, USERNAME, KEY, ROLE)
        userRepository.save(user)

        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)

        question = new Question()
        question.setKey(KEY)
        question.setTitle("Title")
        question.setCourse(course)
        questionRepository.save(question)
        course.addQuestion(question)

        clarification1 = new Clarification(CONTENT, question, user)
        clarification1.setAvailability(Clarification.Availability.NONE)
        clarificationRepository.save(clarification1)
        user.addClarification(clarification1)
        question.addClarification(clarification1)

        clarification2 = new Clarification(CONTENT, question, user)
        clarification2.setAvailability(Clarification.Availability.BOTH)
        clarificationRepository.save(clarification2)
        user.addClarification(clarification2)
        question.addClarification(clarification2)
    }

    def "change dashboard availability" () {
        when:
        def results = clarificationService.getCreditedClarificationsByStudent(user.getId())

        then: "dashboard availability should be public"
        results.size() == 1
    }

    @TestConfiguration
    static class ClarificationServiceImplTestContextConfiguration {
        @Bean
        ClarificationService clarificationService() {
            return new ClarificationService()
        }
    }
}
