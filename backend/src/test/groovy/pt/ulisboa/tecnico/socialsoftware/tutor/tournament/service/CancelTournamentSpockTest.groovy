package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import spock.lang.Unroll
import spock.lang.Shared
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

    @Shared
    static final LocalDateTime BAD_START_DATE = LocalDateTime.now().minusDays(1)
    @Shared
    static final LocalDateTime GOOD_START_DATE = LocalDateTime.now().plusDays(1)

    static final LocalDateTime CONCLUSION_DATE = LocalDateTime.now().plusDays(3)

    def getTournamentId(tournament, validTournamentId) {
        if(validTournamentId) return tournament.getId();
        return -1;
    }

    def getUserId(creator,user,userType) {
        if(userType == "CREATOR") return creator.getId();
        else if (userType == "USER") return user.getId();
        return -1;
    }

    def setStatusCanceled(tournament, statusCanceled) {
        if(statusCanceled) tournament.setStatus(Tournament.Status.CANCELED)
    }

    def "cancel a tournament"() {
        given: "a creator"
        def creator = new User(USER_NAME, USER_UNAME, USER_KEY, User.Role.STUDENT)
        userRepository.save(creator)
        def creatorId=creator.getId()

        and:"a tournament id"
        def tournament = new Tournament(creator,GOOD_START_DATE, CONCLUSION_DATE, NUMBER_QUESTIONS)
        tournamentRepository.save(tournament)
        def tournamentId = tournamentRepository.findAll().get(0).getId()

        when:
        tournamentService.cancelTournament(creatorId, tournamentId);

        then:"tournament has been canceled"
        tournamentRepository.findAll().get(0).getStatus() == Tournament.Status.CANCELED
    }

    @Unroll
    def  "invalid arguments: startingDate=#startingDate | \
        statusCanceled=#statusCanceled |userId=#userId | \
        validTournamentId=#validTournamentId | errorMessage=#errorMessage "() {
        given: "a creator"
        def creator = new User(USER_NAME, USER_UNAME, USER_KEY, User.Role.STUDENT)
        userRepository.save(creator)

        and:"a tournament id"
        def tournament = new Tournament(creator,startingDate, CONCLUSION_DATE, NUMBER_QUESTIONS)
        tournamentRepository.save(tournament)
        def tournamentId = getTournamentId(tournament, validTournamentId)

        and: "a user"
        def user = new User(USER_NAME,USER2_UNAME,USER2_KEY, User.Role.STUDENT)
        userRepository.save(user)

        and: "a user id"
        def userId = getUserId(creator,user,userType)

        when:
        setStatusCanceled(tournament,statusCanceled)
        tournamentService.cancelTournament(userId, tournamentId)

        then:
        def error = thrown(TutorException)
        error.errorMessage == errorMessage

        where:
        startingDate    | statusCanceled | userType       | validTournamentId || errorMessage
        GOOD_START_DATE | false          | "USER"         | true              || TOURNAMENT_NOT_THE_CREATOR
        BAD_START_DATE  | false          | "CREATOR"      | true              || TOURNAMENT_RUNNING
        GOOD_START_DATE | true           | "CREATOR"      | true              || TOURNAMENT_ALREADY_CANCELED
        GOOD_START_DATE | false          | "INVALID_USER" | true              || USER_NOT_FOUND
        GOOD_START_DATE | false          | "CREATOR"      | false             || TOURNAMENT_NOT_FOUND
    }

    @TestConfiguration
    static class ServiceImplTestContextConfiguration {

        @Bean
        TournamentService tournamentService() {
            return new TournamentService()
        }
    }

}