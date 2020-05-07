<template>
  <v-card class="table">
    <v-data-table
      :headers="headers"
      :fixed-header="true"
      :items="tournaments"
      :search="search"
      show-expand
      :single-expand="true"
      :expanded.sync="expanded"
      :items-per-page="5"
      @click:row="goToLeaderboard"
    >
      <!-- STATUS -->
      <template v-slot:top>
        <!-- SEARCH -->
        <template>
          <v-card-title>
            <v-text-field
              v-model="search"
              append-icon="search"
              label="Search"
              class="mx-2"
            />
            <!-- REFRESH -->
            <v-tooltip bottom>
              <template v-slot:activator="{ on }">
                <v-icon
                  small
                  class="mr-2"
                  v-on="on"
                  @click="refresh()"
                  data-cy="signUp"
                  >fas fa-sync-alt</v-icon
                >
              </template>
              <span>Refresh</span>
            </v-tooltip>
          </v-card-title>
        </template>
      </template>

      <template v-slot:item.place="{ item }">
        <a> {{ getPlace(item) }}</a>
      </template>

      <template v-slot:item.score="{ item }">
        <a> {{ calculateScore(item) }}</a>
      </template>
      <!-- TOPICS -->
      <template v-slot:expanded-item="{ headers, item }">
        <td :colspan="headers.length">{{ getTopicNames(item) }}</td>
      </template>
    </v-data-table>

    <!-- DIALOG -->
    <tournament-leaderboard-dialog
      v-if="currentTournament"
      v-model="tournamentLeaderboardDialog"
      :tournament="currentTournament"
      v-on:close-dialog="onCloseDialog"
    ></tournament-leaderboard-dialog>
  </v-card>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import RemoteServices from '@/services/RemoteServices';
import { Tournament, TournamentStatus } from '@/models/tournament/Tournament';
import Topic from '@/models/management/Topic';
import SignUpForTournamentDialog from '@/views/student/tournament/SignUpForTournamentDialog.vue';
import CreateTournamentDialog from '@/views/student/tournament/CreateTournamentDialog.vue';
import CancelTournamentDialog from '@/views/student/tournament/CancelTournamentDialog.vue';
import { UserBoardPlace } from '@/models/tournament/UserBoardPlace';
import TournamentLeaderboardDialog from '@/views/student/tournament/TournamentLeaderboardDialog.vue';

@Component({
  components: {
    'tournament-leaderboard-dialog': TournamentLeaderboardDialog
  }
})
export default class FinishedTournamentsView extends Vue {
  expanded: any = [];
  tournaments: Tournament[] = [];
  currentTournament: Tournament | null = null;
  tournamentLeaderboardDialog: boolean = false;
  search: string = '';
  headers: object = [
    {
      text: 'Tournament Title',
      value: 'title',
      align: 'center',
      width: '10%'
    },
    {
      text: 'Starting Date',
      value: 'startingDate',
      align: 'center',
      width: '15%'
    },
    {
      text: 'Conclusion Date',
      value: 'conclusionDate',
      align: 'center',
      width: '15%'
    },
    {
      text: 'Creator',
      value: 'creator.name',
      align: 'center',
      width: '10%'
    },
    {
      text: 'Place',
      value: 'place',
      align: 'center',
      width: '10%'
    },
    {
      text: 'Score',
      value: 'score',
      align: 'center',
      width: '10%'
    },
    {
      text: 'Topics',
      value: 'data-table-expand',
      align: 'center',
      sortable: false,
      width: '10%'
    }
  ];

  async created() {
    await this.$store.dispatch('loading');
    await this.$store.dispatch('clearLoading');
    await this.getTournaments();
  }

  getTopicNames(topicItems: any): String {
    let result = '';

    topicItems.topics.forEach(function(topic: Topic) {
      result += topic.name + ', ';
    });
    if (topicItems.topics.length > 0) {
      result = result.substring(0, result.length - 2);
    } else {
      return 'Tournament has no topics';
    }
    return result;
  }
  async getTournaments() {
    await this.$store.dispatch('loading');
    try {
      let tournaments: Tournament[] = await RemoteServices.getUserClosedTournaments();
      this.tournaments = tournaments;
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  calculateScore(tournament: Tournament): String {
    let userBoardPlace = this.getUserBoardPlace(tournament);
    if (userBoardPlace == null) {
      return 'Not applicable';
    }
    let correctAnswers = userBoardPlace.correctAnswers;
    return correctAnswers + '/' + tournament.numberOfQuestions;
  }

  getPlace(tournament: Tournament): String {
    let userBoardPlace = this.getUserBoardPlace(tournament);
    if (userBoardPlace == null) {
      return 'Not applicable';
    }
    let place = userBoardPlace.place;
    return place + '/' + tournament.leaderboard.length;
  }

  getUserBoardPlace(tournament: Tournament): UserBoardPlace | null {
    for (let ubp of tournament.leaderboard) {
      if (ubp.user.name == this.$store.getters.getUser.name) {
        return ubp;
      }
    }
    return null;
  }

  async refresh() {
    await this.getTournaments();
  }

  async goToLeaderboard(tournament: Tournament) {
    this.currentTournament = tournament;
    this.tournamentLeaderboardDialog = true;
  }

  onCloseDialog() {
    this.tournamentLeaderboardDialog = true;
    this.currentTournament = null;
  }
}
</script>

<style scoped></style>
