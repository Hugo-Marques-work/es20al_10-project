package pt.ulisboa.tecnico.socialsoftware.tutor.clarification;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain.ClarificationAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.dto.ClarificationAnswerDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain.Clarification;
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.dto.ClarificationDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.repository.ClarificationAnswerRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.repository.ClarificationRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.dto.UserDto;

import java.util.Comparator;
import java.util.*;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

@Service("ClarificationService")
public class ClarificationService {
    @Autowired
    QuestionRepository questionRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ClarificationRepository clarificationRepository;

    @Autowired
    CourseRepository courseRepository;

    @Autowired
    ClarificationAnswerRepository clarificationAnswerRepository;

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public ClarificationDto createClarification(Question question, User user, String content) {
        checkQuestion(question);
        checkUser(user, User.Role.STUDENT);
        checkContent(content, ErrorMessage.CLARIFICATION_IS_EMPTY);

        Clarification clarification = new Clarification(content, question, user);
        clarificationRepository.save(clarification);
        question.addClarification(clarification);
        user.addClarification(clarification);
        return new ClarificationDto(clarification);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public ClarificationDto createClarification(int questionId, int userId, String content) {
        return createClarification(questionRepository.findById(questionId).orElse(null)
                , userRepository.findById(userId).orElse(null)
                , content);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public ClarificationAnswerDto createClarificationAnswer(Clarification clarification, User user, String content){
        checkClarification(clarification);
        checkUser(user, null);
        checkLastUserToAnswer(clarification, user);
        checkContent(content, ErrorMessage.CLARIFICATION_ANSWER_IS_EMPTY);

        ClarificationAnswer clarificationAnswer = new ClarificationAnswer(content, clarification, user);
        clarificationAnswerRepository.save(clarificationAnswer);
        clarification.addClarificationAnswer(clarificationAnswer);
        user.addClarificationAnswer(clarificationAnswer);

        return new ClarificationAnswerDto(clarificationAnswer);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public ClarificationAnswerDto createClarificationAnswer(int clarificationId, String content, int userId) {
        return createClarificationAnswer(clarificationRepository.findById(clarificationId).orElse(null),
                userRepository.findById(userId).orElse(null),
                content);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public List<ClarificationDto> getClarificationsByQuestion(int questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new TutorException(ErrorMessage.QUESTION_NOT_FOUND, questionId));
        return convertClarificationsToList(question.getClarifications());
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public List<ClarificationDto> getClarificationsByUser(int userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new TutorException(ErrorMessage.USER_NOT_FOUND, userId));
        return convertClarificationsToList(user.getClarifications());
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public List<ClarificationDto> getClarificationsByCourse(int courseId, User.Role role) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new TutorException(ErrorMessage.COURSE_NOT_FOUND, courseId));
        Set<Clarification> clarifications = new HashSet<>();
        List<Question> questions = new ArrayList<>(course.getQuestions());
        for (Question question : questions) {
            clarifications.addAll(
                    question.getClarifications()
                            .stream()
                            .filter(
                                    t -> (
                                            (t.getAvailability() != Clarification.Availability.NONE)) ||
                                            role.equals(User.Role.TEACHER)
                            )
                            .collect(Collectors.toList())
            );
        }

        return convertClarificationsToList(clarifications, true);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public List<ClarificationAnswerDto> getClarificationAnswers(int clarificationId) {
        Clarification clarification = clarificationRepository.findById(clarificationId)
                .orElseThrow(()-> new TutorException(ErrorMessage.CLARIFICATION_NOT_FOUND, clarificationId));

        return Lists.newArrayList(clarification.getClarificationAnswers().stream()
            .map(ClarificationAnswerDto::new)
            .sorted(Comparator.comparing(ClarificationAnswerDto::getId))
            .collect(Collectors.toList())
        );
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public ClarificationDto updateClarification(int  clarificationId, String content) {
        Clarification clarification = clarificationRepository.findById(clarificationId)
                .orElseThrow(() -> new TutorException(ErrorMessage.CLARIFICATION_NOT_FOUND, clarificationId));

        clarification.setContent(content);
        return new ClarificationDto(clarification);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public ClarificationAnswerDto updateClarificationAnswer(int clarificationAnswerId, String content) {
        ClarificationAnswer clarificationAnswer = clarificationAnswerRepository.findById(clarificationAnswerId)
                .orElseThrow(()-> new TutorException(ErrorMessage.CLARIFICATION_ANSWER_NOT_FOUND, clarificationAnswerId));

        clarificationAnswer.setContent(content);
        return new ClarificationAnswerDto(clarificationAnswer);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public ClarificationDto makeClarificationAvailableByTeacher(int clarificationId) {
        Clarification clarification = clarificationRepository.findById(clarificationId)
                .orElseThrow(() -> new TutorException(ErrorMessage.CLARIFICATION_NOT_FOUND, clarificationId));

        clarification.makeAvailableTeacher();
        return new ClarificationDto(clarification);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public ClarificationDto setClarificationAvailabilityByStudent(int clarificationId, boolean available, int userId) {
        Clarification clarification = clarificationRepository.findById(clarificationId)
                .orElseThrow(() -> new TutorException(ErrorMessage.CLARIFICATION_NOT_FOUND, clarificationId));

        if (clarification.getUser().getId() != userId)
            throw new TutorException(CLARIFICATION_NOT_CREATOR);

        clarification.setAvailabilityStudent(available);
        return new ClarificationDto(clarification);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public List<ClarificationDto> getCreditedClarificationsByStudent(int userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new TutorException(USER_NOT_FOUND, userId));

        return user.getClarifications().stream()
                .map(ClarificationDto::new)
                .filter(ClarificationDto::isAvailableByTeacher)
                .collect(Collectors.toList());
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public boolean getDashboardAvailability(int userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new TutorException(USER_NOT_FOUND, userId));
        return user.getDashboardPublic() == User.ClarificationDashboardAvailability.PUBLIC;
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public UserDto changeDashboardAvailability(int userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new TutorException(USER_NOT_FOUND, userId));
        user.toggleDashboardAvailability();

        return new UserDto(user);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void removeClarification(int clarificationId) {
        Clarification clarification = clarificationRepository.findById(clarificationId)
                .orElseThrow(() -> new TutorException(ErrorMessage.CLARIFICATION_NOT_FOUND, clarificationId));

        clarification.remove();
        clarificationRepository.delete(clarification);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void removeClarificationAnswer(int clarificationAnswerId) {
        ClarificationAnswer clarificationAnswer = clarificationAnswerRepository.findById(clarificationAnswerId)
                .orElseThrow(()-> new TutorException(ErrorMessage.CLARIFICATION_ANSWER_NOT_FOUND, clarificationAnswerId));

        clarificationAnswer.remove();
        clarificationAnswerRepository.delete(clarificationAnswer);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public CourseDto findClarificationCourseById(int clarificationId) {
        return clarificationRepository.findById(clarificationId)
                .map(Clarification::getQuestion)
                .map(Question::getCourse)
                .map(CourseDto::new)
                .orElseThrow(() -> new TutorException(CLARIFICATION_NOT_FOUND, clarificationId));
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public CourseDto findClarificationAnswerCourseById(int clarificationAnswerId) {
        return clarificationAnswerRepository.findById(clarificationAnswerId)
                .map(ClarificationAnswer::getClarification)
                .map(Clarification::getQuestion)
                .map(Question::getCourse)
                .map(CourseDto::new)
                .orElseThrow(() -> new TutorException(CLARIFICATION_ANSWER_NOT_FOUND, clarificationAnswerId));
    }

    private void checkClarification(Clarification clarification){
        if (clarification == null)
            throw new TutorException(ErrorMessage.CLARIFICATION_NOT_FOUND, null);
        Clarification clr = clarificationRepository.findById(clarification.getId()).orElse(null);
        if (clr == null)
            throw new TutorException(ErrorMessage.CLARIFICATION_NOT_FOUND, clarification.getId());
    }

    private void checkLastUserToAnswer(Clarification clarification, User user) {
        Clarification clr = clarificationRepository.findById(clarification.getId()).orElse(null);
        if (clr != null &&
                (!clr.getClarificationAnswers().isEmpty() && getLastClarificationAnswer(clr).getUser().getId() == user.getId()
                || clr.getClarificationAnswers().isEmpty() && clr.getUser().getId() == user.getId()))
            throw new TutorException(CLARIFICATION_SAME_USER);
    }

    private void checkQuestion(Question question) {
        if (question == null)
            throw new TutorException(QUESTION_NOT_FOUND);

        Question qt = questionRepository.findById(question.getId()).orElse(null);
        if (qt == null)
            throw new TutorException(ErrorMessage.QUESTION_NOT_FOUND, question.getId());
    }

    private void checkUser(User user, User.Role role) {
        if (user == null)
            throw new TutorException(ErrorMessage.USER_NOT_FOUND, null);

        User usr = userRepository.findById(user.getId()).orElseThrow(() -> new TutorException(ErrorMessage.USER_NOT_FOUND, user.getId()));
        if (role != null && usr.getRole() != role)
            throw new TutorException(ErrorMessage.CLARIFICATION_WRONG_USER, role.toString());
    }

    private void checkContent(String content, ErrorMessage errorMessage) {
        if (content == null || content.isBlank() || content.isEmpty())
            throw new TutorException(errorMessage);
    }

    private List<ClarificationDto> convertClarificationsToList(Set<Clarification> clarifications) {
        return convertClarificationsToList(clarifications, false);
    }

    private List<ClarificationDto> convertClarificationsToList(Set<Clarification> clarifications,
                                                               boolean checkAvailability) {
        return clarifications.stream()
                .map(c -> new ClarificationDto(c, checkAvailability))
                .sorted(Comparator
                        .comparing(ClarificationDto::getId))
                .collect(Collectors.toList());
    }

    private ClarificationAnswer getLastClarificationAnswer(Clarification clarification) {
        ClarificationAnswer last = null;
        for (ClarificationAnswer clr: clarification.getClarificationAnswers()) {
            if (last == null)
                last = clr;
            else if (clr.getId() > last.getId())
                last = clr;
        }

        return last;
    }
}
