package pt.ulisboa.tecnico.socialsoftware.tutor.tournament;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto;
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

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
        courseExecution.addTournament(tournament);
        creator.addCreatedTournament(tournament);
        return new TournamentDto(tournament, true);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public List<TournamentDto> getOpenTournaments(int courseExecutionId) {
        CourseExecution courseExecution = getCourseExecution(courseExecutionId);

        return courseExecution.getTournaments().stream()
                .map(tournament -> new TournamentDto(tournament, true))
                .filter(TournamentDto::isOpen)
                .sorted(Comparator
                        .comparing(TournamentDto::getStartingDateDate))
                .collect(Collectors.toList());
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

    private void checkSignUp(TournamentDto tournamentDto) {
        Set<UserDto> users = tournamentDto.getSignedUpUsers();
        if (users == null) {
            throw new TutorException(TOURNAMENT_NOT_CONSISTENT, "Sign up list can't be null");
        }
        if (!users.isEmpty()) {
            throw new TutorException(TOURNAMENT_NOT_CONSISTENT, "Sign up list is not empty"
                    + tournamentDto.getSignedUpUsers());
        }
    }

    @Transactional
    public void signUp(Integer userId, Integer tournamentId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new TutorException(USER_NOT_FOUND, userId));

        Tournament tournament = tournamentRepository.findById(tournamentId).orElseThrow(
                () -> new TutorException(TOURNAMENT_NOT_FOUND, tournamentId));

        user.checkReadyForSignUp(tournament);

        tournament.checkReadyForSignUp();

        executeSignUp(user, tournament);
    }


    private void executeSignUp(User user, Tournament tournament) {
        tournament.addSignUp(user);
        user.signUpForTournament(tournament);
    }

    @Transactional
    public void cancelTournament(Integer userId, Integer tournamentId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new TutorException(USER_NOT_FOUND, userId));

        Tournament tournament = tournamentRepository.findById(tournamentId).orElseThrow(
                () -> new TutorException(TOURNAMENT_NOT_FOUND, tournamentId));

        executeCancel(user,tournament);
    }

    private void executeCancel(User user, Tournament tournament) {
        tournament.cancel(user);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public CourseDto findTournamentCourseExecution(int tournamentId) {
        return this.tournamentRepository.findById(tournamentId)
                .map(Tournament::getCourseExecution)
                .map(CourseDto::new)
                .orElseThrow(() -> new TutorException(TOURNAMENT_NOT_FOUND, tournamentId));
    }

}
