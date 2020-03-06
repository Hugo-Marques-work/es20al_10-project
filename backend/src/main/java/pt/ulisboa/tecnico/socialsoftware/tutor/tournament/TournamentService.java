package pt.ulisboa.tecnico.socialsoftware.tutor.tournament;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.domain.Tournament;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto.TournamentDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.repository.TournamentRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.dto.UserDto;

import java.sql.SQLException;
import java.util.Set;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

public class TournamentService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private CourseExecutionRepository courseExecutionRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public TournamentDto createTournament(int executionId, TournamentDto tournamentDto) {
        CourseExecution courseExecution = getCourseExecution(executionId);

        Tournament tournament = new Tournament(tournamentDto);
        tournament.setCourseExecution(courseExecution);

        checkTopics(tournamentDto, tournament);
        checkSignUp(tournamentDto);

        tournamentRepository.save(tournament);
        return new TournamentDto(tournament, true);
    }

    private CourseExecution getCourseExecution(int executionId) {
        return courseExecutionRepository.findById(executionId)
                    .orElseThrow(() -> new TutorException(COURSE_EXECUTION_NOT_FOUND, executionId));
    }

    //fixme name
    private void checkTopics(TournamentDto tournamentDto, Tournament tournament) {
        if (tournamentDto.getTopics() != null) {
            Set<TopicDto> topics = tournamentDto.getTopics();
            if (topics.isEmpty()) {
                throw new TutorException(TOURNAMENT_NOT_CONSISTENT, "Topic list is empty"
                        + tournamentDto.getTopics());
            }

            for (TopicDto topicDto : topics) {
                Topic topic = topicRepository.findById(topicDto.getId())
                        .orElseThrow(() -> new TutorException(TOPIC_NOT_FOUND, topicDto.getId()));
                tournament.addTopic(topic);
            }
        }
    }

    private void checkSignUp(TournamentDto tournamentDto) {
        if (tournamentDto.getSignedUpUsers() != null) {
            Set<UserDto> users = tournamentDto.getSignedUpUsers();
            if (!users.isEmpty()) {
                throw new TutorException(TOURNAMENT_NOT_CONSISTENT, "Sign up list is empty"
                        + tournamentDto.getSignedUpUsers());
            }
        }
    }


    public void signUp(Integer userId, Integer tournamentId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new TutorException(USER_NOT_FOUND, userId));

        Tournament tournament = tournamentRepository.findById(tournamentId).orElseThrow(() -> new TutorException(TOURNAMENT_NOT_FOUND, tournamentId));

        checkUserReadyForSignUp(userId, user, tournament);

        tournament.checkReadyForSignUp();

        executeSignUp(user, tournament);
    }

    private void checkUserReadyForSignUp(Integer userId, User user, Tournament tournament) {
        if( ! user.getCourseExecutions().contains(tournament.getCourseExecution())) {
            throw new TutorException(USER_NOT_ENROLLED,userId.toString());
        }

        if(user.getSignUpTournaments().contains(tournament)) {
            throw new TutorException(USER_DUPLICATE_SIGN_UP, tournament.getId().toString());
        }
    }

    private void executeSignUp(User user, Tournament tournament) {
        tournament.addSignUp(user);
        user.signUpForTournament(tournament);
        userRepository.save(user);
        tournamentRepository.save(tournament);
    }
}
