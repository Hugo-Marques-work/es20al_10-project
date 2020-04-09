<template>
  <v-card class="table">
    <v-data-table
      :headers="headers"
      :items="clarifications"
      :search="search"
      disable-pagination
      :hide-default-footer="true"
      :mobile-breakpoint="0"
      multi-sort
    >
      <template v-slot:top>
        <v-card-title>
          <v-text-field
            v-model="search"
            append-icon="search"
            label="Search"
            class="mx-2"
          />
        </v-card-title>
      </template>

      <template v-slot:item.topic="{ item }">
        <span v-for="topic in item.question.topics" :key="topic.name">
          {{ topic.name }}
        </span>
      </template>

      <template v-slot:item.question="{ item }">
        <v-tooltip bottom>
          <template v-slot:activator="{ on }">
            <v-icon
              small
              class="mr-2"
              v-on="on"
              @click="viewQuestion(item)"
              data-cy="viewQuestion"
              >mdi-eye</v-icon
            >
          </template>
          <span>View Question</span>
        </v-tooltip>
      </template>

      <template v-slot:item.answer="{ item }">
        <v-tooltip bottom v-if="item.answered">
          <template v-slot:activator="{ on }">
            <v-icon
              medium
              class="mr-2"
              v-on="on"
              data-cy="answered"
              color="green"
            >
              mdi-check
            </v-icon>
          </template>
          <span>Answered</span>
        </v-tooltip>
        <v-tooltip bottom v-if="!item.answered">
          <template v-slot:activator="{ on }">
            <v-icon
              medium
              class="mr-2"
              v-on="on"
              data-cy="notAnswered"
              color="red"
            >
              mdi-close
            </v-icon>
          </template>
          <span>Not answered</span>
        </v-tooltip>
      </template>

      <template v-slot:item.action="{ item }">
        <v-tooltip bottom>
          <template v-slot:activator="{ on }">
            <v-icon
              small
              class="mr-2"
              v-on="on"
              @click="viewClarification(item)"
              data-cy="viewClarification"
              >mdi-eye</v-icon
            >
          </template>
          <span>View Clarification</span>
        </v-tooltip>
      </template>
    </v-data-table>

    <show-question-dialog
      v-if="currentQuestion"
      :dialog="questionDialog"
      :question="currentQuestion"
      v-on:close-show-question-dialog="onCloseShowQuestionDialog"
    />
    <show-clarification-dialog
      v-if="currentClarification"
      :dialog="clarificationDialog"
      :clarification="currentClarification"
      :isTeacher="isTeacher"
      v-on:close-show-clarification-dialog="onCloseShowClarificationDialog"
      @send-clarification-answer="sendClarificationAnswer"
    />
  </v-card>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import RemoteServices from '@/services/RemoteServices';
import Clarification from '@/models/clarification/Clarification';
import Course from '@/models/user/Course';
import Question from '@/models/management/Question';
import ShowQuestionDialog from '@/views/teacher/questions/ShowQuestionDialog.vue';
import ShowClarificationDialog from '@/views/clarification/ShowClarificationDialog.vue';
import ClarificationAnswer from '@/models/clarification/ClarificationAnswer';
import { stringifyQuery } from 'vue-router/src/util/query';

@Component({
  components: {
    'show-question-dialog': ShowQuestionDialog,
    'show-clarification-dialog': ShowClarificationDialog
  }
})
export default class ClarificationListView extends Vue {
  isTeacher: boolean = false;
  clarificationAnswer: ClarificationAnswer | null = null;
  clarifications: Clarification[] = [];
  currentCourse: Course | null = null;
  currentQuestion: Question | null = null;
  currentClarification: Clarification | null = null;
  questionDialog: boolean = false;
  clarificationDialog: boolean = false;
  search: string = '';
  headers: object = [
    {
      text: 'Content',
      value: 'content',
      align: 'center',
      width: '15%'
    },
    {
      text: 'User',
      value: 'user.name',
      align: 'center',
      width: '10%'
    },
    // {
    //   text: 'Topics',
    //   value: 'topic',
    //   align: 'center',
    //   width: '10%'
    // },
    {
      text: 'Question',
      value: 'question',
      align: 'center',
      width: '10%',
      sortable: false
    },
    {
      text: 'Answered',
      value: 'answer',
      align: 'center',
      width: '5%'
    },
    {
      text: 'Actions',
      value: 'action',
      align: 'center',
      width: '7%',
      sortable: false
    }
  ];

  async created() {
    await this.$store.dispatch('loading');
    try {
      if (this.$store.getters.isTeacher || this.$store.getters.isAdmin) {
        this.clarifications = (
          await RemoteServices.getClarifications()
        ).reverse();
        this.isTeacher = true;
      } else
        this.clarifications = (
          await RemoteServices.getClarificationsByUser()
        ).reverse();
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  async viewClarification(clarification: Clarification) {
    // let clarificationManager: ClarificationManager =
    //   ClarificationManager.getInstance;
    // clarificationManager.clarification = clarification;
    //
    // await this.$router.push({ name: 'view-clarification' });
    this.clarificationDialog = true;
    this.currentClarification = clarification;
  }

  async viewQuestion(clarification: Clarification) {
    try {
      let question: Question = await RemoteServices.getQuestion(
        clarification.question.id
      );
      this.questionDialog = true;
      this.currentQuestion = question;
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
  }

  onCloseShowQuestionDialog() {
    this.questionDialog = false;
  }

  onCloseShowClarificationDialog() {
    this.clarificationDialog = false;
  }

  async sendClarificationAnswer(answer: string) {
    await this.$store.dispatch('loading');

    if (answer != null) {
    } else {
      await this.$store.dispatch('error', 'Answer can not be empty');
      return;
    }

    try {
      if (
        this.currentClarification != null &&
        this.currentClarification.id != null
      ) {
        this.clarificationAnswer = await RemoteServices.createClarificationAnswer(
          this.currentClarification.id,
          answer
        );
        await this.$store.dispatch('confirmation', 'Answer sent');
      }
    } catch (error) {
      await this.$store.dispatch('error', error);
    }

    await this.$store.dispatch('clearLoading');
  }
}
</script>

<style lang="scss" scoped>
.container {
  max-width: 1000px;
  margin-left: auto;
  margin-right: auto;
  padding-left: 10px;
  padding-right: 10px;

  h2 {
    font-size: 26px;
    margin: 20px 0;
    text-align: center;
    small {
      font-size: 0.5em;
    }
  }

  ul {
    overflow: hidden;
    padding: 0 5px;

    li {
      border-radius: 3px;
      padding: 15px 10px;
      display: flex;
      justify-content: space-between;
      margin-bottom: 10px;
    }

    .list-header {
      background-color: #1976d2;
      color: white;
      font-size: 14px;
      text-transform: uppercase;
      letter-spacing: 0.03em;
      text-align: center;
    }

    .col {
      flex-basis: 25% !important;
      margin: auto; /* Important */
      text-align: center;
    }

    .list-row {
      background-color: #ffffff;
      box-shadow: 0 0 9px 0 rgba(0, 0, 0, 0.1);
      display: flex;
    }
  }
}
</style>
