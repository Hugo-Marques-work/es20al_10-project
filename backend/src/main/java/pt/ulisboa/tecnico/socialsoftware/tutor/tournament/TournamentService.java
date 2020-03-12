package pt.ulisboa.tecnico.socialsoftware.tutor.tournament;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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

import java.util.Set;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

@Service("TournamentService")
public class TournamentService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private CourseExecutionRepository courseExecutionRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public TournamentDto createTournament(int creatorId, int executionId, TournamentDto tournamentDto) {
        CourseExecution courseExecution = getCourseExecution(executionId);

        User creator = getTournamentCreator(creatorId);

        Tournament tournament = new Tournament(creator, tournamentDto);
        tournament.setCourseExecution(courseExecution);

        checkAndAddTopics(tournamentDto, tournament);
        checkSignUp(tournamentDto);

        tournamentRepository.save(tournament);
        return new TournamentDto(tournament, true);
    }

    private User getTournamentCreator(int creatorId) {
        return userRepository.findById(creatorId)
                .orElseThrow(() -> new TutorException(USER_NOT_FOUND, creatorId));
    }

    private CourseExecution getCourseExecution(int executionId) {
        return courseExecutionRepository.findById(executionId)
                    .orElseThrow(() -> new TutorException(COURSE_EXECUTION_NOT_FOUND, executionId));
    }

    private void checkAndAddTopics(TournamentDto tournamentDto, Tournament tournament) {
        if (tournamentDto.getTopics() != null) {
            Set<TopicDto> topics = tournamentDto.getTopics();
            if (topics.isEmpty()) {
                throw new TutorException(TOURNAMENT_NOT_CONSISTENT, "topic list (empty)"
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
        User user = userRepository.findById(userId).orElseThrow(
                () -> new TutorException(USER_NOT_FOUND, userId));

        Tournament tournament = tournamentRepository.findById(tournamentId).orElseThrow(
                () -> new TutorException(TOURNAMENT_NOT_FOUND, tournamentId));

        checkUserReadyForSignUp(user, tournament);

        tournament.checkReadyForSignUp();

        executeSignUp(user, tournament);
    }

    private void checkUserReadyForSignUp(User user, Tournament tournament) {
        if(!user.getCourseExecutions().contains(tournament.getCourseExecution())) {
            throw new TutorException(USER_NOT_ENROLLED,user.getUsername());
        }

        if(user.getSignUpTournaments().contains(tournament)) {
            throw new TutorException(TOURNAMENT_DUPLICATE_SIGN_UP, tournament.getId().toString());
        }
    }

    private void executeSignUp(User user, Tournament tournament) {
        tournament.addSignUp(user);
        user.signUpForTournament(tournament);
        userRepository.save(user);
        tournamentRepository.save(tournament);
    }

    public void cancelTournament(Integer userId, Integer tournamentId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new TutorException(USER_NOT_FOUND, userId));

        Tournament tournament = tournamentRepository.findById(tournamentId).orElseThrow(
                () -> new TutorException(TOURNAMENT_NOT_FOUND, tournamentId));

        executeCancel(user,tournament);
    }

    private void executeCancel(User user, Tournament tournament) {
        tournament.cancel(user);
        tournamentRepository.save(tournament);
    }

}
