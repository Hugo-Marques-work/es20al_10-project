package pt.ulisboa.tecnico.socialsoftware.tutor.answer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.CorrectAnswerDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.QuizAnswerDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.repository.QuestionAnswerRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.repository.QuizAnswerRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.config.DateHandler;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.AnswersXmlExport;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.AnswersXmlImport;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.OptionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository.QuizRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.statement.QuizAnswerItem;
import pt.ulisboa.tecnico.socialsoftware.tutor.statement.QuizAnswerItemRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.statement.dto.StatementAnswerDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.statement.dto.StatementQuizDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

@Service
public class AnswerService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QuestionAnswerRepository questionAnswerRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuizAnswerRepository quizAnswerRepository;

    @Autowired
    private QuizAnswerItemRepository quizAnswerItemRepository;

    @Autowired
    private AnswersXmlImport xmlImporter;

    @Retryable(
      value = { SQLException.class },
      backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public QuizAnswerDto createQuizAnswer(Integer userId, Integer quizId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new TutorException(USER_NOT_FOUND, userId));

        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new TutorException(QUIZ_NOT_FOUND, quizId));

        QuizAnswer quizAnswer = new QuizAnswer(user, quiz);
        quizAnswerRepository.save(quizAnswer);

        return new QuizAnswerDto(quizAnswer);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<CorrectAnswerDto> concludeQuiz(StatementQuizDto statementQuizDto) {
        QuizAnswer quizAnswer = quizAnswerRepository.findById(statementQuizDto.getQuizAnswerId())
                .orElseThrow(() -> new TutorException(QUIZ_ANSWER_NOT_FOUND, statementQuizDto.getId()));

        if (quizAnswer.getQuiz().getAvailableDate() != null && quizAnswer.getQuiz().getAvailableDate().isAfter(DateHandler.now())) {
            throw new TutorException(QUIZ_NOT_YET_AVAILABLE);
        }

        if (quizAnswer.getQuiz().getConclusionDate() != null && quizAnswer.getQuiz().getConclusionDate().isBefore(DateHandler.now().minusMinutes(10))) {
            throw new TutorException(QUIZ_NO_LONGER_AVAILABLE);
        }

        if (!quizAnswer.isCompleted()) {
            quizAnswer.setCompleted(true);

            if (quizAnswer.getQuiz().getType().equals(Quiz.QuizType.IN_CLASS)) {
                QuizAnswerItem quizAnswerItem = new QuizAnswerItem(statementQuizDto);
                quizAnswerItemRepository.save(quizAnswerItem);
            } else {
                quizAnswer.setAnswerDate(DateHandler.now());

                for (QuestionAnswer questionAnswer : quizAnswer.getQuestionAnswers()) {
                    writeQuestionAnswer(questionAnswer, statementQuizDto.getAnswers());
                }
                return quizAnswer.getQuestionAnswers().stream()
                        .sorted(Comparator.comparing(QuestionAnswer::getSequence))
                        .map(CorrectAnswerDto::new)
                        .collect(Collectors.toList());
            }
        }
        return new ArrayList<>();
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void writeQuizAnswers(Integer quizId) {
        Quiz quiz = quizRepository.findQuizWithAnswersAndQuestionsById(quizId).orElseThrow(() -> new TutorException(QUIZ_NOT_FOUND, quizId));
        Map<Integer, QuizAnswer> quizAnswersMap = quiz.getQuizAnswers().stream().collect(Collectors.toMap(QuizAnswer::getId, Function.identity()));

        List<QuizAnswerItem> quizAnswerItems = quizAnswerItemRepository.findQuizAnswerItemsByQuizId(quizId);

        quizAnswerItems.forEach(quizAnswerItem -> {
            QuizAnswer quizAnswer = quizAnswersMap.get(quizAnswerItem.getQuizAnswerId());

            if (quizAnswer.getAnswerDate() == null) {
                quizAnswer.setAnswerDate(quizAnswerItem.getAnswerDate());

                for (QuestionAnswer questionAnswer : quizAnswer.getQuestionAnswers()) {
                    writeQuestionAnswer(questionAnswer, quizAnswerItem.getAnswersList());
                }
            }
            quizAnswerItemRepository.deleteById(quizAnswerItem.getId());
        });
    }

    private void writeQuestionAnswer(QuestionAnswer questionAnswer, List<StatementAnswerDto> statementAnswerDtoList) {
        StatementAnswerDto statementAnswerDto = statementAnswerDtoList.stream()
                .filter(statementAnswerDto1 -> statementAnswerDto1.getQuestionAnswerId().equals(questionAnswer.getId()))
                .findAny()
                .orElseThrow(() -> new TutorException(QUESTION_ANSWER_NOT_FOUND, questionAnswer.getId()));

        questionAnswer.setTimeTaken(statementAnswerDto.getTimeTaken());

        if (statementAnswerDto.getOptionId() != null) {

                Option option = questionAnswer.getQuizQuestion().getQuestion().getOptions().stream()
                        .filter(option1 -> option1.getId().equals(statementAnswerDto.getOptionId()))
                        .findAny()
                        .orElseThrow(() -> new TutorException(QUESTION_OPTION_MISMATCH, statementAnswerDto.getOptionId()));

            if (questionAnswer.getOption() != null) {
                questionAnswer.getOption().getQuestionAnswers().remove(questionAnswer);
            }
            questionAnswer.setOption(option);
        } else {
            questionAnswer.setOption(null);
        }
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public boolean submitTournamentAnswer(User user, Integer quizId, StatementAnswerDto answer) {
        QuizAnswer quizAnswer = user.getQuizAnswers().stream()
                .filter(qa -> qa.getQuiz().getId().equals(quizId))
                .findFirst()
                .orElseThrow(() -> new TutorException(QUIZ_NOT_FOUND, quizId));

        QuestionAnswer questionAnswer = quizAnswer.getQuestionAnswers().stream()
                .filter(qa -> qa.getSequence().equals(answer.getSequence()))
                .findFirst()
                .orElseThrow(() -> new TutorException(QUESTION_ANSWER_NOT_FOUND, answer.getSequence()));

        if (isNotAssignedStudent(user, quizAnswer)) {
            throw new TutorException(QUIZ_USER_MISMATCH, String.valueOf(quizAnswer.getQuiz().getId()), user.getUsername());
        }

        if (quizAnswer.getQuiz().getConclusionDate() != null && quizAnswer.getQuiz().getConclusionDate().isBefore(DateHandler.now())) {
            throw new TutorException(QUIZ_NO_LONGER_AVAILABLE);
        }

        if (quizAnswer.getQuiz().getAvailableDate() != null && quizAnswer.getQuiz().getAvailableDate().isAfter(DateHandler.now())) {
            throw new TutorException(QUIZ_NOT_YET_AVAILABLE);
        }

        if (!quizAnswer.isCompleted()) {
            Option option;
            if (answer.getOptionId() != null) {
                option = questionAnswer.getQuizQuestion().getQuestion().getOptions().stream()
                        .filter(option1 -> option1.getId().equals(answer.getOptionId()))
                        .findAny()
                        .orElseThrow(() -> new TutorException(OPTION_NOT_FOUND, answer.getOptionId()));
                if (isNotQuestionOption(questionAnswer.getQuizQuestion(), option)) {
                    throw new TutorException(QUESTION_OPTION_MISMATCH, questionAnswer.getQuizQuestion().getQuestion().getId(), option.getId());
                }
                if (questionAnswer.getTimeTaken() != null) {
                    throw new TutorException(QUESTION_ALREADY_ANSWERED, questionAnswer.getOption().getId());
                }
                if (answer.getTimeTaken() == null) {
                    answer.setTimeTaken(1);
                }
                questionAnswer.setOption(option);
            }
            questionAnswer.setTimeTaken(answer.getTimeTaken());
            quizAnswer.setAnswerDate(DateHandler.now());

        }
        return questionAnswer.getQuizQuestion().getQuestion().getCorrectOptionId().equals(answer.getOptionId());
    }

    private boolean isNotQuestionOption(QuizQuestion quizQuestion, Option option) {
        return quizQuestion.getQuestion().getOptions().stream().map(Option::getId).noneMatch(value -> value.equals(option.getId()));
    }

    private boolean isNotAssignedStudent(User user, QuizAnswer quizAnswer) {
        return !user.getId().equals(quizAnswer.getUser().getId());
    }

    @Retryable(
      value = { SQLException.class },
      backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public String exportAnswers() {
        AnswersXmlExport xmlExport = new AnswersXmlExport();

        return xmlExport.export(quizAnswerRepository.findAll());
    }

    @Retryable(
      value = { SQLException.class },
      backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void importAnswers(String answersXml) {
        xmlImporter.importAnswers(answersXml);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void deleteQuizAnswer(QuizAnswer quizAnswer) {
        List<QuestionAnswer> questionAnswers = new ArrayList<>(quizAnswer.getQuestionAnswers());
        questionAnswers.forEach(questionAnswer ->
        {
            questionAnswer.remove();
            questionAnswerRepository.delete(questionAnswer);
        });
        quizAnswer.remove();
        quizAnswerRepository.delete(quizAnswer);
    }
}
