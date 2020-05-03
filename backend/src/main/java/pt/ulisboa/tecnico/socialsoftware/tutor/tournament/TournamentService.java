package pt.ulisboa.tecnico.socialsoftware.tutor.tournament;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.config.DateHandler;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.QuizService;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.dto.QuizDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository.QuizRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.domain.Tournament;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto.TournamentDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.repository.TournamentRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.dto.UserDto;

import java.util.Collections;
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

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuizService quizService;

    @Autowired
    private QuestionRepository questionRepository;

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public TournamentDto createTournament(int creatorId, int executionId, TournamentDto tournamentDto) {
        CourseExecution courseExecution = getCourseExecution(executionId);

        User creator = getUser(creatorId);

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
        Set<Tournament> tournaments = getUpdatedTournaments(courseExecution);

        return tournaments.stream()
                .map(tournament -> new TournamentDto(tournament, true))
                .filter(TournamentDto::isOpen)
                .sorted(Comparator
                        .comparing(TournamentDto::getStartingDateDate))
                .collect(Collectors.toList());
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public List<TournamentDto> getRunningTournaments(int courseExecutionId) {
        CourseExecution courseExecution = getCourseExecution(courseExecutionId);
        Set<Tournament> tournaments = getUpdatedTournaments(courseExecution);

        for (Tournament tournament : tournaments) {
            generateQuiz(tournament);
        }

        return tournaments.stream()
                .filter(t -> t.getStatus() == Tournament.Status.RUNNING)
                .map(t -> new TournamentDto(t, true))
                .sorted(Comparator
                .comparing(TournamentDto::getConclusionDate))
                .collect(Collectors.toList());
    }

    private Set<Tournament> getUpdatedTournaments(CourseExecution courseExecution) {
        Set<Tournament> tournaments = courseExecution.getTournaments();
        for (Tournament tournament : tournaments) {
            tournament.updateStatus();
        }
        return tournaments;
    }

    private User getUser(int userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new TutorException(USER_NOT_FOUND, userId));
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

    private void generateQuiz(Tournament tournament) {
        // check if quiz was already generated
        if (!(tournament.getStatus() == Tournament.Status.RUNNING && !tournament.hasQuiz())) {
            return;
        }

        CourseExecution courseExecution = tournament.getCourseExecution();

        Quiz quiz = new Quiz();
        quiz.setKey(quizService.getMaxQuizKey() + 1);
        quiz.setType(Quiz.QuizType.GENERATED.toString());
        quiz.setCreationDate(DateHandler.now());
        quiz.setTitle("Tournament - " + tournament.getTitle());

        List<Question> availableQuestions = questionRepository.findAvailableQuestions(courseExecution.getCourse().getId());
        availableQuestions = filterByTopics(availableQuestions, tournament.getTopics());

        if (availableQuestions.size() < tournament.getNumberOfQuestions()) {
            throw new TutorException(NOT_ENOUGH_QUESTIONS);
        }

        Collections.shuffle(availableQuestions);
        availableQuestions = availableQuestions.subList(0, tournament.getNumberOfQuestions());

        quiz.generate(availableQuestions);

        //TODO: this will happen once the user picks the quiz
//        QuizAnswer quizAnswer = new QuizAnswer(user, quiz);

        quiz.setCourseExecution(courseExecution);
        courseExecution.addQuiz(quiz);

        quizRepository.save(quiz);
        tournament.setQuiz(quiz);
        //TODO: Same as above
//        quizAnswerRepository.save(quizAnswer);
    }

    public List<Question> filterByTopics(List<Question> availableQuestions, Set<Topic> topics) {
        return availableQuestions.stream()
                .filter(question ->
                    question.getTopics().stream().anyMatch(topics::contains)
                )
                .collect(Collectors.toList());
    }

    @Transactional
    public void signUp(Integer userId, Integer tournamentId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new TutorException(USER_NOT_FOUND, userId));

        Tournament tournament = getTournament(tournamentId);

        user.checkReadyForSignUp(tournament);

        tournament.checkReadyForSignUp();

        executeSignUp(user, tournament);
    }


    private void executeSignUp(User user, Tournament tournament) {
        tournament.addSignUp(user);
        user.signUpForTournament(tournament);
    }

    @Transactional
    public void cancelTournament(Integer tournamentId) {
        Tournament tournament = getTournament(tournamentId);
        tournament.cancel();
    }

    private Tournament getTournament(Integer tournamentId) {
        return tournamentRepository.findById(tournamentId).orElseThrow(
                () -> new TutorException(TOURNAMENT_NOT_FOUND, tournamentId));
    }

    public CourseDto findTournamentCourseExecution(int tournamentId) {
        return this.tournamentRepository.findById(tournamentId)
                .map(Tournament::getCourseExecution)
                .map(CourseDto::new)
                .orElseThrow(() -> new TutorException(TOURNAMENT_NOT_FOUND, tournamentId));
    }

}
