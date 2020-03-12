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
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDateTime

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.TOURNAMENT_NOT_FOUND
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

    @Shared
    LocalDateTime BAD_START_DATE
    @Shared
    LocalDateTime GOOD_START_DATE

    User creator
    User user


    def setup() {
        creator = new User("host", "chost", 1, User.Role.STUDENT)
        userRepository.save(creator)

        def currentDate = LocalDateTime.now();
        LocalDateTime startingDate = currentDate.plusDays(1)
        GOOD_START_DATE = currentDate.plusDays(1)
        BAD_START_DATE = currentDate.minusDays(1)
        CourseExecution courseExe = new CourseExecution();
        courseExecutionRepository.save(courseExe)

        def tournament = new Tournament(creator, startingDate, currentDate.plusDays(2), 10)
        tournament.setId(1)
        tournament.setCourseExecution(courseExe)
        tournamentRepository.save(tournament)

        user = new User("pessoa","pessoa1337",3, User.Role.STUDENT)
        user.addCourse(courseExe)
        HashSet<CourseExecution> courseExes = new HashSet<CourseExecution>(1)
        courseExes.add(courseExe)
        user.setCourseExecutions(courseExes)
        userRepository.save(user)
    }

    def getTournamentId(tournament, validTournamentId) {
        if(validTournamentId) return tournament.getId();
        return -1;
    }

    def getUserId(user, validUserId) {
        if(validUserId) return user.getId();
        return -1;
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

        when:
        tournamentService.signUp(userId, tournamentId)

        then:
        def error = thrown(TutorException)
        error.errorMessage == USER_NOT_ENROLLED
    }

    def "already signed up"() {
        given: "a tournament id"
        def tournament = tournamentRepository.findAll().get(0)
        def tournamentId = tournament.getId()
        and: "a user id"
        def userId = user.getId()
        user.signUpForTournament(tournament)

        when:
        tournamentService.signUp(userId, tournamentId)

        then:
        def error = thrown(TutorException)
        error.errorMessage == TOURNAMENT_DUPLICATE_SIGN_UP
    }

    @Unroll
    def  "invalid arguments: startingDate=#startingDate | \
        userId=#userId | tournamentId=#tournamentId ||\
        errorMessage=#errorMessage "() {
        given: "a tournament id"
        def tournament = tournamentRepository.findAll().get(0)
        def tournamentId = getTournamentId(tournament, validTournamentId)
        and: "a user id"
        def userId = getUserId(user, validUserId)
        and: "a starting date"
        tournament.setStartingDate(startingDate)

        when:
        tournamentService.signUp(userId, tournamentId)

        then:
        def error = thrown(TutorException)
        error.errorMessage == errorMessage

        where:
        startingDate    | validUserId  | validTournamentId || errorMessage
        BAD_START_DATE  | true         | true              || TOURNAMENT_SIGN_UP_OVER
        GOOD_START_DATE | false        | true              || USER_NOT_FOUND
        GOOD_START_DATE | true         | false             || TOURNAMENT_NOT_FOUND
    }

    @TestConfiguration
    static class ServiceImplTestContextConfiguration {

        @Bean
        TournamentService tournamentService() {
            return new TournamentService()
        }
    }
}
