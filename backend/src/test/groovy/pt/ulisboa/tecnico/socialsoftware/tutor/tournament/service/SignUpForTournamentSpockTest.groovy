package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.TopicService
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentService
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.repository.TournamentRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification

@DataJpaTest
class SignUpForTournamentSpockTest extends Specification{
    @Autowired
    TournamentService tournamentService

    @Autowired
    TournamentRepository tournamentRepository

    @Autowired
    UserRepository courseRepository

    def tournament
    def user

    def setup() {

    }

    def "sign up for a tournament"() {
        expect: false
    }

    def "tournament exists but you aren't enrolled to the course execution"() {
        expect: false
    }

    def "tournament does not exist"() {
        expect: false
    }

    def "tournament signup is not ready"() {
        expect: false
    }

    def "tournament signup is finished"() {
        expect: false
    }

    def "already signed up"() {
        expect: false
    }

    def "user is invalid"() {
        expect: false
    }
}
