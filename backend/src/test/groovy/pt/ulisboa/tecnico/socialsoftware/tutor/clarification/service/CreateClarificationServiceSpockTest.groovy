package pt.ulisboa.tecnico.socialsoftware.tutor.clarification.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean;
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.ClarificationService
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification;

@DataJpaTest
class CreateClarificationServiceSpockTest extends Specification {
    static final String NAME = "test user"
    static final String USERNAME = "test_user"
    static final Integer USER_KEY = 1000
    static final User.Role ROLE = User.Role.STUDENT
    static final String CONTENT = "I want a clarification in this question."

    @Autowired
    ClarificationService clarificationService

    @Autowired
    UserRepository userRepository

    @Autowired
    QuestionRepository questionRepository

    def "question and user exists and creates clarification"() {
        given: "a user"
        def user = new User(NAME, USERNAME, USER_KEY, ROLE)
        userRepository.save(user)
        and: "a question"
        def question = new Question()
        question.setKey(1000)
        questionRepository.save(question)

        when:
        def result = clarificationService.createClarification(question, user, CONTENT)

        then: "the returned data are correct"
        result.content == CONTENT
        and: "the clarification is added to the user"
        user.getClarifications().size() == 1
        def userClarifications = new ArrayList<>(user.getClarifications()).get(0)
        userClarifications != null
        and: "has the correct value"
        userClarifications.content == CONTENT
        and: "the clarification was added to the question"
        question.getClarification().size() == 1
        def questionClarification = new ArrayList<>(question.getClarification()).get(0)
        questionClarification != null
        and: "has the correct value"
        questionClarification.content == CONTENT
    }

    def "question does not exist"() {
        given: "a user"
        def user = new User(NAME, USERNAME, USER_KEY, ROLE)
        userRepository.save(user)
        and: "a question"
        def question = new Question()
        question.setKey(1000)

        when:
        clarificationService.createClarification(question, user, CONTENT)

        then: "the returned data is incorrect"
        thrown(TutorException)
    }

    def "user does not exist"() {
        given: "a user"
        def user = new User(NAME, USERNAME, USER_KEY, ROLE)
        and: "a question"
        def question = new Question()
        question.setKey(1000)
        questionRepository.save(question)
        and: "a clarification"

        when:
        clarificationService.createClarification(question, user, CONTENT)

        then: "the returned data is incorrect"
        thrown(TutorException)
    }

    def "content is empty"() {
        given: "a user"
        def user = new User(NAME, USERNAME, USER_KEY, ROLE)
        userRepository.save(user)
        and: "a question"
        def question = new Question()
        question.setKey(1000)
        questionRepository.save(question)

        when:
        clarificationService.createClarification(question, user, null)

        then: "the returned data is incorrect"
        thrown(TutorException)
    }

    def "content is blank"() {
        given: "a user"
        def user = new User(NAME, USERNAME, USER_KEY, ROLE)
        userRepository.save(user)
        and: "a question"
        def question = new Question()
        question.setKey(1000)
        questionRepository.save(question)

        when:
        clarificationService.createClarification(question, user, "    ")

        then: "the returned data is incorrect"
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
