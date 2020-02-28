package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.service

import org.springframework.beans.factory.annotation.Autowired
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.TopicService
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentService
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.repository.TournamentRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification

class SignUpForTournamentSpockTest extends Specification{
    public static final String COURSE_NAME = "Software Architecture"
    public static final String ACRONYM = "AS1"
    public static final String ACADEMIC_TERM = "1 SEM"
    static final String TOPIC_NAME = "Risk Management"
    static final Integer NUMBER_QUESTIONS = 1

    @Autowired
    SignUpForTournamentService signUpService

    @Autowired
    TournamentRepository tournamentRepository

    @Autowired
    UserRepository courseRepository

    def setup() {

    }

    def "sign up for a tournament" {
        return false
    }

    def "tournament exists but you aren't enrolled to the course execution" {
        return false
    }

    def "tournament does not exist" {
        return false
    }

    def "tournament signup is not ready" {
        return false
    }

    def "tournament signup is finished" {
        return false
    }

    def "already signed up" {
        return falseSe calhar recebe é o toutnsmentId;
    }

    def "user is invalid" {
        return false;
    }
}
