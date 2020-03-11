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

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.TOURNAMENT_ALREADY_CANCELED
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.TOURNAMENT_NOT_FOUND
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.TOURNAMENT_NOT_THE_CREATOR
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.TOURNAMENT_RUNNING
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.USER_NOT_ENROLLED
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.USER_NOT_FOUND

@DataJpaTest
class CancelTournamentSpockTest extends Specification{

    static final Integer NUMBER_QUESTIONS = 1
    static final String USER_NAME = "Carlos"
    static final String USER_UNAME = "carlos"
    static final String USER2_UNAME = "carlos2"
    static final Integer USER_KEY = 1
    static final Integer USER2_KEY = 2

    @Autowired
    TournamentService tournamentService

    @Autowired
    TournamentRepository tournamentRepository

    @Autowired
    UserRepository userRepository

    DateTimeFormatter formatter

    LocalDateTime currentDate
    User user
    User creator

    def setup() {
        creator = new User(USER_NAME, USER_UNAME, USER_KEY, User.Role.STUDENT)
        userRepository.save(creator)

        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        LocalDateTime startingDate = LocalDateTime.now().plusDays(1)
        LocalDateTime conclusionDate = LocalDateTime.now().plusDays(3)

        currentDate = LocalDateTime.now();

        def tournament = new Tournament(creator,startingDate, conclusionDate, NUMBER_QUESTIONS)
        tournamentRepository.save(tournament)

        user = new User(USER_NAME,USER2_UNAME,USER2_KEY, User.Role.STUDENT)
        userRepository.save(user)
    }

    def "cancel a tournament"() {
        given: "a tournament id"
        def tournamentId = tournamentRepository.findAll().get(0).getId()
        and: "the creator id"
        def creatorId = creator.getId()

        when:
        tournamentService.cancelTournament(creatorId, tournamentId);

        then:"tournament has been canceled"
        tournamentRepository.findAll().get(0).getStatus() == Tournament.Status.CANCELED
    }

    def "a user that is not the creator cancels the tournament"(){
        given: "a tournament id"
        def tournamentId = tournamentRepository.findAll().get(0).getId()
        and: "a user id"
        def userId = user.getId()

        when:
        tournamentService.cancelTournament(userId, tournamentId);

        then:
        def error = thrown(TutorException)
        error.errorMessage == TOURNAMENT_NOT_THE_CREATOR
    }

    def "the tournament is running"(){
        given: "a tournament id"
        def tournament = tournamentRepository.findAll().get(0)
        def tournamentId = tournament.getId()
        and: "the creator id"
        def creatorId = creator.getId()
        and: "the tournament starts"
        tournament.setStartingDate(currentDate.minusDays(1))

        when:
        tournamentService.cancelTournament(creatorId, tournamentId);

        then:
        def error = thrown(TutorException)
        error.errorMessage == TOURNAMENT_RUNNING
    }

    def "the tournament has already been canceled"(){
        given: "a tournament id"
        def tournament = tournamentRepository.findAll().get(0)
        def tournamentId = tournament.getId()
        and: "the creator id"
        def creatorId = creator.getId()
        and: "the tournament starts"
        tournament.setStatus(Tournament.Status.CANCELED)

        when:
        tournamentService.cancelTournament(creatorId, tournamentId);

        then:
        def error = thrown(TutorException)
        error.errorMessage == TOURNAMENT_ALREADY_CANCELED
    }

    def "user does not exist"(){
        given: "a tournament id"
        def tournamentId = tournamentRepository.findAll().get(0).getId()

        when:
        tournamentService.signUp(-1,tournamentId)

        then:
        def error = thrown(TutorException)
        error.errorMessage == USER_NOT_FOUND
    }

    def "tournament does not exist"(){
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