<template>
  <v-dialog
    :value="dialog"
    @input="$emit('close-dialog')"
    @keydown.esc="$emit('close-dialog')"
    max-width="75%"
    max-height="80%"
  >
    <v-card>
      <v-card-title>
        <span class="headline">
          New Tournament
        </span>
      </v-card-title>

      <v-card-text class="text-left">
        <v-container grid-list-md fluid>
          <v-layout column wrap>
            <v-flex xs24 sm12 md8>
              <v-text-field
                v-model="tournament.title"
                label="Title"
                data-cy="Title"
              />
            </v-flex>
          </v-layout>
        </v-container>
      </v-card-text>

      <v-card-text>
        <v-container grid-list-md fluid>
          <v-layout column wrap>
            <v-flex xs24 sm12 md8>
              <v-datetime-picker
                label="Starting Date"
                format="yyyy-MM-dd HH:mm"
                v-model="tournament.startingDate"
                date-format="yyyy-MM-dd"
                time-format="HH:mm"
              >
              </v-datetime-picker>
              <v-datetime-picker
                label="Conclusion Date"
                v-model="tournament.conclusionDate"
                date-format="yyyy-MM-dd"
                time-format="HH:mm"
              >
              </v-datetime-picker>
            </v-flex>
          </v-layout>
        </v-container>
      </v-card-text>

      <v-card-text>
        <v-container grid-list-md fluid>
          <v-layout column wrap>
            <v-autocomplete
              label="Choose your topics"
              v-model="tournament.topics"
              :items="allTopics"
              item-text="name"
              multiple
              @change="updateMaxQuestions"
              return-object
              data-cy="Topics"
            ></v-autocomplete>
          </v-layout>
        </v-container>
      </v-card-text>

      <v-card-text>
        <v-container grid-list-md fluid v-if="this.maxQuestions > 0">
          <v-subheader>Number of questions</v-subheader>
          <br />
          <v-layout column wrap>
            <v-slider
              v-model="tournament.numberOfQuestions"
              class="align-center"
              :max="maxQuestions"
              :min="minQuestions"
              thumb-label="always"
              hide-details
              data-cy="Number Questions"
            ></v-slider>
          </v-layout>
        </v-container>
      </v-card-text>

      <v-card-actions>
        <v-spacer />
        <v-btn
          color="blue darken-1"
          @click="$emit('close-dialog')"
          data-cy="cancelButton"
          >Cancel</v-btn
        >
        <v-btn
          color="blue darken-1"
          @click="createTournament"
          data-cy="saveButton"
          >Save</v-btn
        >
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import { Component, Model, Vue, Watch } from 'vue-property-decorator';
import RemoteServices from '@/services/RemoteServices';
import { Tournament } from '@/models/management/Tournament';
import Topic from '@/models/management/Topic';
import Question from '@/models/management/Question';

@Component
export default class CreateTournamentDialog extends Vue {
  @Model('dialog', Boolean) dialog!: boolean;

  tournament: Tournament = new Tournament();
  minQuestions: number = 1;
  maxQuestions: number = 0;

  allTopics: Topic[] = [];
  allQuestions: Question[] = [];

  async created() {
    this.allTopics = await RemoteServices.getTopics();
    this.allQuestions = await RemoteServices.getAvailableQuestions();
  }

  async updateMaxQuestions() {
    let topicIds: Set<number> = new Set(
      this.tournament.topics.map(topic => topic.id)
    );
    this.maxQuestions = this.allQuestions
      .map(question => question.topics.map(topic => topic.id))
      .filter(topics => topics.some(topicId => topicIds.has(topicId))).length;
    await this.clampNumberQuestions();
  }

  async clampNumberQuestions() {
    this.tournament.numberOfQuestions = Math.min(
      Math.max(this.tournament.numberOfQuestions, this.minQuestions),
      this.maxQuestions
    );
  }

  async createTournament() {
    if (!this.validTournament()) {
      await this.$store.dispatch(
        'error',
        'Error: Tournament must have a title, starting and ending date, and at least one topic'
      );
      return;
    }

    if (this.tournament) {
      try {
        const result = await RemoteServices.createTournament(this.tournament);
        this.$emit('new-tournament', result);
      } catch (error) {
        await this.$store.dispatch('error', error);
      }
    }
  }

  validTournament() {
    return (
      this.tournament &&
      this.tournament.title &&
      this.tournament.startingDate &&
      this.tournament.conclusionDate &&
      this.tournament.topics.length > 0 &&
      this.tournament.numberOfQuestions > 0
    );
  }
}
</script>
