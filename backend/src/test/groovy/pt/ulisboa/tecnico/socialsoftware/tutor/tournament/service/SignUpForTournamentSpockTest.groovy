package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.question.QuestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.TopicService
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentService
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.domain.Tournament
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.repository.TournamentRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification

import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.TOURNAMENT_NOT_CONSISTENT
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.TOURNAMENT_NOT_FOUND
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.TOURNAMENT_SIGN_UP_NOT_READY
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.TOURNAMENT_SIGN_UP_OVER
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.USER_DUPLICATE_SIGN_UP
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.USER_NOT_ENROLLED
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.USER_NOT_FOUND

@DataJpaTest
class SignUpForTournamentSpockTest extends Specification{
    @Autowired
    TournamentService tournamentService

    @Autowired
    TournamentRepository tournamentRepository

    @Autowired
    CourseExecutionRepository courseExecutionRepository
    @Autowired
    UserRepository userRepository

    Tournament tournament
    User user

    DateTimeFormatter formatter
    //LocalDateTime startingDate
    //LocalDateTime conclusionDate

    LocalDateTime currentDate


    def setup() {
        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")



        CourseExecution courseExe = new CourseExecution();
        courseExe.setId(1)
        courseExecutionRepository.save(courseExe)
        LocalDateTime startingDate = LocalDateTime.now().plusDays(1)
        LocalDateTime conclusionDate = LocalDateTime.now().plusDays(3)
        tournament = new Tournament(startingDate, conclusionDate, 10)
        tournament.setId(1)
        tournament.setCourseExecution(courseExe)
        tournamentRepository.save(tournament)

        user = new User("pessoa","pessoa1337",3, User.Role.STUDENT)
        user.setId(1)
        HashSet<CourseExecution> courseExes = new HashSet<CourseExecution>(1)
        courseExes.add(courseExe)
        user.setCourseExecutions(courseExes)

        userRepository.save(user)

        currentDate = LocalDateTime.now();
    }

    def "sign up for a tournament"() {
        given: "a tournament id"
        def tournamentId = tournament.getId()
        and: "a user id"
        def userId = user.getId()

        when:
        tournamentService.signUp(userId, tournamentId)

        then: "tournament has user registered"
        user.getSignUpTournaments().size() == 1
        def userSignedTournament = new ArrayList<>(tournament.getSignedUpUsers()).get(0)
        userSignedTournament != null
        userSignedTournament.getId() == user.getId()

        and: "user has tournament registered"
        user.getSignUpTournaments().size() == 1
        def tournamentSignedUser = new ArrayList<>(user.getSignUpTournaments()).get(0)
        tournamentSignedUser != null
        tournamentSignedUser.getId() == tournament.getId()
    }

    def "tournament exists but you aren't enrolled to the course execution"() {
        given: "a tournament id"
        def tournamentId = tournament.getId()
        tournament.setCourseExecution(null)
        and: "a user id"
        def userId = user.getId()

        when:
        tournamentService.signUp(userId, tournamentId)

        then:
        def error = thrown(TutorException)
        //fixme user not enrolled?
        error.errorMessage == USER_NOT_ENROLLED
    }

    def "tournament signup is not ready"() {
        given: "a tournament id"
        def tournamentId = tournament.getId()
        tournament.setCourseExecution(null)
        and: "a user id"
        def userId = user.getId()
        and: "a starting date later than the current date"
        def startingDate = currentDate.plusDays(1).format(formatter)
        tournament.setStartingDate(startingDate)

        when:
        tournamentService.signUp(userId, tournamentId)

        then:
        def error = thrown(TutorException)
        error.errorMessage == TOURNAMENT_SIGN_UP_NOT_READY

    }

    def "tournament signup is finished"() {
        given: "a tournament id"
        def tournamentId = tournament.getId()
        tournament.setCourseExecution(null)
        and: "a user id"
        def userId = user.getId()
        and: "a starting date later than the current date"
        def conclusionDate = currentDate.minusDays(1).format(formatter)
        tournament.setConclusionDate(conclusionDate)

        when:
        tournamentService.signUp(userId, tournamentId)

        then:
        def error = thrown(TutorException)
        error.errorMessage == TOURNAMENT_SIGN_UP_OVER
    }

    def "already signed up"() {
        given: "a tournament id"
        def tournamentId = tournament.getId()
        and: "a user that is already signed up"
        def userId = user.getId()
        user.setSignUpTournaments(new HashSet<Tournament>(tournament))

        when:
        tournamentService.signUp(userId, tournamentId)

        then:
        def error = thrown(TutorException)
        error.errorMessage == USER_DUPLICATE_SIGN_UP
    }

    def "user does not exist"() {
        given: "a tournament id"
        def tournamentId = tournament.getId()

        when:
        tournamentService.signUp(-1,tournamentId)

        then:
        def error = thrown(TutorException)
        error.errorMessage == USER_NOT_FOUND
    }

    def "tournament does not exist"() {
        given: "a user id"
        def userId = user.getId()

        when:
        tournamentService.signUp(userId, -1)

        then:
        def error = thrown(TutorException)
        error.errorMessage == TOURNAMENT_NOT_FOUND
    }

    @TestConfiguration
    static class ServiceImplTestContextConfiguration {

        @Bean
        TournamentService tournamentService() {
            return new TournamentService()
        }
    }
}
