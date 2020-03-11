package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentService
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.domain.Tournament
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.repository.TournamentRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.TOURNAMENT_NOT_FOUND
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.TOURNAMENT_SIGN_UP_NOT_READY
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.TOURNAMENT_SIGN_UP_OVER
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.TOURNAMENT_DUPLICATE_SIGN_UP
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

    DateTimeFormatter formatter
    //LocalDateTime startingDate
    //LocalDateTime conclusionDate

    LocalDateTime currentDate

    User creator
    User user


    def setup() {
        creator = new User("host", "chost", 1, User.Role.STUDENT)
        userRepository.save(creator)

        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        LocalDateTime startingDate = LocalDateTime.now().minusDays(1)
        LocalDateTime conclusionDate = LocalDateTime.now().plusDays(3)
        currentDate = LocalDateTime.now();
        
        CourseExecution courseExe = new CourseExecution();
        courseExecutionRepository.save(courseExe)

        def tournament = new Tournament(creator, startingDate, conclusionDate, 10)
        tournament.setId(1)
        tournament.setCourseExecution(courseExe)
        tournamentRepository.save(tournament)

        user = new User("pessoa","pessoa1337",3, User.Role.STUDENT)
        user.addCourse(courseExe)
        userRepository.save(user)
    }

    def "sign up for a tournament"() {
        given: "a tournament id"
        def tournamentId = tournamentRepository.findAll().get(0).getId()
        and: "a user id"
        def userId = user.getId()

        when:
        tournamentService.signUp(userId, tournamentId);

        then: "tournament has user registered"

        tournamentRepository.findAll().size() == 1
        def updatedTournament = tournamentRepository.findAll().get(0)
        def userSignedTournament = new ArrayList<>(updatedTournament.getSignedUpUsers()).get(0)
        userSignedTournament != null
        userRepository.findAll().size() == 2
        userSignedTournament.getId() ==  userId

        and: "user has tournament registered"
        def tournamentSignedUser = new ArrayList<>(user.getSignUpTournaments()).get(0)
        tournamentSignedUser != null
        tournamentRepository.findAll().size() == 1
        tournamentSignedUser.getId() ==  tournamentRepository.findAll().get(0).getId()
    }

    def "tournament exists but you aren't enrolled to the course execution"() {
        given: "a tournament id"
        def tournamentId = tournamentRepository.findAll().get(0).getId()
        and: "a user id"
        def userId = user.getId()
        user.setCourseExecutions(new HashSet<CourseExecution>())
        userRepository.save(user)

        when:
        tournamentService.signUp(userId, tournamentId)

        then:
        def error = thrown(TutorException)
        //fixme user not enrolled?
        error.errorMessage == USER_NOT_ENROLLED
    }

    def "tournament signup is not ready"() {
        given: "a tournament id"
        def tournament = tournamentRepository.findAll().get(0)
        def tournamentId = tournament.getId()
        and: "a user id"
        def userId = user.getId()
        and: "a starting date later than the current date"
        tournament.setStartingDate(currentDate.plusDays(1))
        tournamentRepository.save(tournament)

        when:
        tournamentService.signUp(userId, tournamentId)

        then:
        def error = thrown(TutorException)
        error.errorMessage == TOURNAMENT_SIGN_UP_NOT_READY

    }

    def "tournament signup is finished"() {
        given: "a tournament id"
        def tournament = tournamentRepository.findAll().get(0)
        def tournamentId = tournament.getId()
        and: "a user id"
        def userId = user.getId()
        and: "a starting date later than the current date"
        tournament.setConclusionDate(currentDate.minusDays(1))

        when:
        tournamentService.signUp(userId, tournamentId)

        then:
        def error = thrown(TutorException)
        error.errorMessage == TOURNAMENT_SIGN_UP_OVER
    }

    def "already signed up"() {
        given: "a tournament id"
        def tournament = tournamentRepository.findAll().get(0)
        def tournamentId = tournament.getId()
        and: "a user id"
        def userId = user.getId()
        user.signUpForTournament(tournament)
        userRepository.save(user)

        when:
        tournamentService.signUp(userId, tournamentId)

        then:
        def error = thrown(TutorException)
        error.errorMessage == TOURNAMENT_DUPLICATE_SIGN_UP
    }

    def "user does not exist"() {
        given: "a tournament id"
        def tournamentId = tournamentRepository.findAll().get(0).getId()

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
