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

            <!--SEARCH BY STATUS-->
            <div class="text-center">
              <v-menu open-on-hover bottom offset-y>
                <template v-slot:activator="{ on }">
                  <v-btn v-on="on">
                    {{ statusSearch }}
                    <v-icon small>
                      fas fa-angle-down
                    </v-icon>
                  </v-btn>
                </template>

                <v-list>
                  <v-list-item
                    v-for="(item, index) in statusSearchList"
                    :key="index"
                    @click="searchStatus(item)"
                  >
                    <v-list-item-title>{{ item }}</v-list-item-title>
                  </v-list-item>
                </v-list>
              </v-menu>

              <v-btn
                color="primary"
                dark
                @click="newTournament"
                data-cy="createButton"
                >New Tournament</v-btn
              >
            </div>

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

      <!-- ACTIONS -->
      <template v-slot:item.action="{ item }">
        <!-- OPEN ACTIONS-->
        <v-tooltip v-if="signUpTournamentConditions(item)" bottom>
          <template v-slot:activator="{ on }">
            <v-icon
              small
              class="mr-2"
              v-on="on"
              @click="signUp(item)"
              data-cy="signUp"
              >fas fa-sign-in-alt</v-icon
            >
          </template>
          <span>Sign Up For Tournament</span>
        </v-tooltip>
        <!-- cancel tournament -->
        <v-tooltip v-if="cancelTournamentConditions(item)" bottom>
          <template v-slot:activator="{ on }">
            <v-icon
              small
              color="red"
              class="mr-2"
              v-on="on"
              @click="cancel(item)"
              data-cy="cancel"
              >cancel</v-icon
            >
          </template>
          <span>Cancel Tournament</span>
        </v-tooltip>
        <!-- SIGNED UP ACTIONS-->
        <v-tooltip v-if="enterTournamentConditions(item)" bottom>
          <template v-slot:activator="{ on }">
            <v-icon small class="mr-2" v-on="on">fas fa-sign-in-alt</v-icon>
          </template>
          <span>Enter Tournament</span>
        </v-tooltip>
      </template>



      <!-- TOPICS -->

      <template v-slot:expanded-item="{ headers, item }">
        <td :colspan="headers.length">{{ getTopicNames(item) }}</td>
      </template>
    </v-data-table>
    <!-- DIALOG -->
    <sign-up-for-tournament-dialog
      v-if="currentTournament"
      v-model="signUpForTournamentDialog"
      :tournament="currentTournament"
      v-on:signedUp="onSignUp"
      v-on:close-dialog="onCloseDialog"
    ></sign-up-for-tournament-dialog>
    <create-tournament-dialog
      v-if="createTournamentDialog"
      v-model="createTournamentDialog"
      v-on:new-tournament="onCreateTournament"
      v-on:close-dialog="onCloseTournamentDialog"
    ></create-tournament-dialog>
    <cancel-tournament-dialog
      v-if="currentTournamentToCancel"
      :tournament="currentTournamentToCancel"
      v-on:canceled="onCancel"
      v-on:close-dialog="onCloseCancelDialog"
      v-on:error="onCloseCancelDialog"
    ></cancel-tournament-dialog>
  </v-card>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import RemoteServices from '@/services/RemoteServices';
import { Tournament, TournamentStatus } from '@/models/management/Tournament';
import Topic from '@/models/management/Topic';
import SignUpForTournamentDialog from '@/views/student/tournament/SignUpForTournamentDialog.vue';
import CreateTournamentDialog from '@/views/student/tournament/CreateTournamentDialog.vue';
import CancelTournamentDialog from '@/views/student/tournament/CancelTournamentDialog.vue';

@Component({
  components: {
    'sign-up-for-tournament-dialog': SignUpForTournamentDialog,
    'create-tournament-dialog': CreateTournamentDialog,
    'cancel-tournament-dialog': CancelTournamentDialog
  }
})
export default class TournamentsView extends Vue {
  createTournamentDialog: boolean = false;
  expanded: any = [];
  tournaments: Tournament[] = [];
  signUpForTournamentDialog: boolean = false;
  currentTournament: Tournament | null = null;
  currentTournamentToCancel: Tournament | null = null;
  search: string = '';
  statusSearchList: string[] = [
    'Open Tournaments',
    'Signed Up Tournaments',
    'Running Tournaments',
    'Created Tournaments'
  ];
  activeFilters: { (tournament: Tournament): boolean }[] = [];
  statusSearch: string = this.statusSearchList[0];
  headers: object = [
    {
      text: 'Tournament Title',
      value: 'title',
      align: 'center',
      width: '10%'
    },
    {
      text: 'Status',
      value: 'status',
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
      text: 'Actions',
      value: 'action',
      align: 'center',
      sortable: false,
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
    await this.searchStatus(this.statusSearchList[0]);
    await this.$store.dispatch('clearLoading');
  }

  onCloseDialog() {
    this.signUpForTournamentDialog = false;
    this.currentTournament = null;
  }

  onCloseCancelDialog() {
    this.currentTournamentToCancel = null;
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

  hasStatus(tournament: Tournament, status: TournamentStatus) {
    return tournament.status == status;
  }

  userSignedInTournament(tournament: Tournament) {
    return tournament.signedUpUsers
      .map(user => user.username)
      .includes(this.$store.getters.getUser.username);
  }

  userCreatedTournament(tournament: Tournament) {
    return tournament.creator.username == this.$store.getters.getUser.username;
  }

  async onSignUp() {
    await this.refresh();
    this.onCloseDialog();
  }

  async onCancel() {
    await this.refresh();
    this.onCloseCancelDialog();
  }

  async signUp(tournament: Tournament) {
    this.currentTournament = tournament;
    this.signUpForTournamentDialog = true;
  }

  async cancel(tournament: Tournament) {
    this.currentTournamentToCancel = tournament;
  }

  async getTournaments() {
    let tournaments: Tournament[] = await RemoteServices.getTournaments();
    this.activeFilters.forEach(
      filter =>
        (tournaments = tournaments.filter(tournament => filter(tournament)))
    );
    this.tournaments = tournaments;
  }

  signUpTournamentConditions(tournament: Tournament){
    return !this.userSignedInTournament(tournament) && this.hasStatus(tournament, TournamentStatus.Open);
  }

  cancelTournamentConditions(tournament: Tournament){
    return this.userCreatedTournament(tournament) && this.hasStatus(tournament, TournamentStatus.Open);
  }

  enterTournamentConditions(tournament: Tournament){
    return this.userSignedInTournament(tournament) && this.hasStatus(tournament, TournamentStatus.Running)
  }

  async searchStatus(item: string) {
    this.statusSearch = item;
    switch (item) {
      case this.statusSearchList[0]:
        this.activeFilters = [
          t => !this.userSignedInTournament(t),
          t => this.hasStatus(t, TournamentStatus.Open)
        ];
        break;
      case this.statusSearchList[1]:
        this.activeFilters = [
          t => this.userSignedInTournament(t),
          t => this.hasStatus(t, TournamentStatus.Open)
        ];
        break;
      case this.statusSearchList[2]:
        this.activeFilters = [
          t => this.userSignedInTournament(t),
          t => this.hasStatus(t, TournamentStatus.Running)
        ];
        break;
      case this.statusSearchList[3]:
        this.activeFilters = [
          t => this.userCreatedTournament(t),
        ];
        break;
    }
    await this.getTournaments();
  }

  async refresh() {
    await this.getTournaments();
  }

  newTournament() {
    this.createTournamentDialog = true;
  }

  async onCreateTournament() {
    await this.refresh();
    this.createTournamentDialog = false;
  }

  onCloseTournamentDialog() {
    this.createTournamentDialog = false;
  }
}
</script>

<style scoped></style>
