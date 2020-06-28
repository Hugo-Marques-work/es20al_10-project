<template style="height: 100%">
  <div
    tabindex="0"
    class="quiz-container"
    v-if="!confirmed && questionOrderCalculated"
  >
    <header>
      <span
        class="timer"
        @click="hideTime = !hideTime"
        v-if="statementQuiz && statementQuiz.timeToSubmission"
      >
        <i class="fas fa-clock"></i>
        <span v-if="!hideTime">{{ submissionTimer }}</span>
      </span>
      <span
        color="primary"
        class="end-quiz"
        @click="leaveQuiz"
        data-cy="leaveQuizButton"
        ><i class="fas fa-times" />Leave Quiz</span
      >
    </header>

    <div class="question-navigation">
      <div class="navigation-buttons">
        <span
          v-for="index in +statementQuiz.questions.length"
          v-bind:class="[
            'question-button',
            index === questionOrder + 1 ? 'current-question-button' : ''
          ]"
          :key="index"
          @click="changeOrder(index - 1)"
        >
          {{ index }}
        </span>
      </div>
      <span
        class="right-button"
        @click="confirmAnswer"
        data-cy="confirmAnswer"
        v-if="questionOrder !== statementQuiz.questions.length - 1"
        ><i class="fas fa-chevron-right"
      /></span>
      <span
        class="right-button"
        @click="confirmFinish"
        data-cy="confirmFinish"
        v-if="questionOrder === statementQuiz.questions.length - 1"
        ><i class="fas fa-check"
      /></span>
    </div>
    <div v-if="showResult" style="padding-top: 25px">
      <span
        >Last answer:
        <v-icon v-if="correctResult" large color="green">check_circle</v-icon>
        <v-icon v-else large color="red">cancel</v-icon>
      </span>
    </div>
    <question-component
      v-model="questionOrder"
      v-if="statementQuiz.answers[questionOrder]"
      :optionId="statementQuiz.answers[questionOrder].optionId"
      :question="statementQuiz.questions[questionOrder]"
      :questionNumber="statementQuiz.questions.length"
      :backsies="false"
      :tournament="true"
      @increase-order="confirmAnswer"
      @finish="confirmFinish"
      @select-option="changeAnswer"
    />

    <v-dialog v-model="nextConfirmationDialog" width="50%">
      <v-card>
        <v-card-title primary-title class="secondary white--text headline">
          Confirmation
        </v-card-title>

        <v-card-text class="text--black title">
          <br />
          Are you sure you want to go to the next question?
          <br />
        </v-card-text>

        <v-divider />

        <v-card-actions>
          <v-spacer />
          <v-btn color="secondary" text @click="nextConfirmationDialog = false">
            Cancel
          </v-btn>
          <v-btn
            color="primary"
            text
            @click="increaseOrder(false)"
            data-cy="confirm"
          >
            I'm sure
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-dialog v-model="finishConfirmationDialog" width="50%">
      <v-card>
        <v-card-title primary-title class="secondary white--text headline">
          Confirmation
        </v-card-title>

        <v-card-text class="text--black title">
          <br />
          Are you sure you want to finish the tournament?
          <br />
        </v-card-text>

        <v-divider />

        <v-card-actions>
          <v-spacer />
          <v-btn
            color="secondary"
            text
            @click="finishConfirmationDialog = false"
          >
            Cancel
          </v-btn>
          <v-btn
            color="primary"
            text
            @click="increaseOrder(true)"
            data-cy="confirmFinishDialog"
          >
            I'm sure
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script lang="ts">
import { Component, Vue, Watch } from 'vue-property-decorator';
import QuestionComponent from '@/views/student/quiz/QuestionComponent.vue';
import StatementManager from '@/models/statement/StatementManager';
import RemoteServices from '@/services/RemoteServices';
import StatementQuiz from '@/models/statement/StatementQuiz';
import { milisecondsToHHMMSS } from '@/services/ConvertDateService';

@Component({
  components: {
    'question-component': QuestionComponent
  }
})
export default class TournamentQuizView extends Vue {
  statementManager: StatementManager = StatementManager.getInstance;
  statementQuiz: StatementQuiz | null =
    StatementManager.getInstance.statementQuiz;
  questionOrderCalculated: boolean = false;
  confirmed: boolean = false;
  nextConfirmationDialog: boolean = false;
  finishConfirmationDialog: boolean = false;
  startTime: Date = new Date();
  questionOrder: number = 0;
  hideTime: boolean = false;
  showResult: boolean = false;
  correctResult: boolean = false;
  submissionTimer: string = '';

  async created() {
    if (!this.statementQuiz?.id) {
      await this.$router.push({ name: 'tournaments' });
    } else {
      try {
        await RemoteServices.startQuiz(this.statementQuiz?.id);
        this.updateQuestionOrder();
        this.questionOrderCalculated = true;
      } catch (error) {
        await this.$store.dispatch('error', error);
        await this.$router.push({ name: 'tournaments' });
      }
    }
  }

  updateQuestionOrder(): void {
    if (this.statementQuiz?.answers.length) {
      for (let i = 0; i < this.statementQuiz?.answers.length; i++) {
        if (!this.statementQuiz?.answers[i].timeTaken) {
          this.questionOrder = i;
          return;
        }
      }
    }
    throw Error('You have already completed this tournament');
  }

  async increaseOrder(last: boolean) {
    this.calculateTime();
    await this.submitAnswer();
    if (!last) this.questionOrder += 1;
    else await this.leaveQuiz();
    this.nextConfirmationDialog = false;
  }

  private async submitAnswer() {
    if (this.statementQuiz) {
      if (!this.statementQuiz.answers[this.questionOrder].optionId)
        this.statementQuiz.answers[this.questionOrder].optionId = null;

      let correct = await RemoteServices.submitTournamentAnswer(
        this.statementQuiz.id,
        this.statementQuiz.answers[this.questionOrder]
      );

      this.showResult = true;
      this.correctResult = correct;
    }
  }

  async changeAnswer(optionId: number) {
    if (this.statementQuiz && this.statementQuiz.answers[this.questionOrder]) {
      let previousAnswer = this.statementQuiz.answers[this.questionOrder]
        .optionId;
      try {
        this.calculateTime();

        if (
          this.statementQuiz.answers[this.questionOrder].optionId === optionId
        ) {
          this.statementQuiz.answers[this.questionOrder].optionId = null;
        } else {
          this.statementQuiz.answers[this.questionOrder].optionId = optionId;
        }
      } catch (error) {
        this.statementQuiz.answers[
          this.questionOrder
        ].optionId = previousAnswer;

        await this.$store.dispatch('error', error);
      }
    }
  }

  confirmAnswer() {
    this.nextConfirmationDialog = true;
  }

  confirmFinish() {
    this.finishConfirmationDialog = true;
  }

  @Watch('statementQuiz.timeToSubmission')
  submissionTimerWatcher() {
    if (!!this.statementQuiz && this.statementQuiz.timeToSubmission === 0) {
      this.concludeQuiz();
    }

    this.submissionTimer = milisecondsToHHMMSS(
      this.statementQuiz?.timeToSubmission
    );
  }

  async concludeQuiz() {
    await this.$store.dispatch('loading');
    try {
      //this.calculateTime();
      this.confirmed = true;
      await this.statementManager.concludeQuiz();

      if (this.statementManager.correctAnswers.length !== 0) {
        await this.$router.push({ name: 'quiz-results' });
      }
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  async leaveQuiz() {
    await this.$router.push({ name: 'tournaments' });
  }

  calculateTime() {
    if (this.statementQuiz) {
      this.statementQuiz.answers[this.questionOrder].timeTaken +=
        new Date().getTime() - this.startTime.getTime();
      this.startTime = new Date();
    }
  }
}
</script>
