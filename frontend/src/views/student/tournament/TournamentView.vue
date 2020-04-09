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
            <div class="text-center" style="padding-right:2%">
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
        <v-tooltip v-if="statusSearch == statusSearchList[0]" bottom>
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
        <!-- SIGNED UP ACTIONS-->
        <v-tooltip v-if="statusSearch == statusSearchList[2]" bottom>
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
  </v-card>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import RemoteServices from '@/services/RemoteServices';
import { Tournament, TournamentStatus } from '@/models/management/Tournament';
import Topic from '@/models/management/Topic';
import SignUpForTournamentDialog from '@/views/student/tournament/SignUpForTournamentDialog.vue';

@Component({
  components: {
    'sign-up-for-tournament-dialog': SignUpForTournamentDialog
  }
})
export default class TournamentView extends Vue {
  expanded: any = [];
  tournaments: Tournament[] = [];
  signUpForTournamentDialog: boolean = false;
  currentTournament: Tournament | null = null;
  search: string = '';
  statusSearchList: string[] = [
    'Open tournaments',
    'Signed up tournaments',
    'Running tournaments'
  ];
  statusSearch: string = this.statusSearchList[0];
  statusFilter: TournamentStatus | null = null;
  signedInFilter: boolean = false;
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
    this.statusFilter = TournamentStatus.Open;
    try {
      await this.getTournaments();
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  onCloseDialog() {
    this.signUpForTournamentDialog = false;
    this.currentTournament = null;
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

  async onSignUp() {
    await this.getTournaments();
    this.onCloseDialog();
  }

  async signUp(tournament: Tournament) {
    this.currentTournament = tournament;
    this.signUpForTournamentDialog = true;
  }

  async getTournaments() {
    let tournaments: Tournament[] = await RemoteServices.getTournaments();
    this.tournaments = [];
    let username = this.$store.getters.getUser.name;

    for (let i in tournaments) {
      let tournament = tournaments[i];
      if (tournament.status == this.statusFilter) {
        let userSignedIn = false;
        for (let k in tournament.signedUpUsers) {
          if (username == tournament.signedUpUsers[k].name) {
            userSignedIn = true;
            break;
          }
        }
        if (userSignedIn && this.signedInFilter)
          this.tournaments.push(tournament);
        else if (!userSignedIn && !this.signedInFilter)
          this.tournaments.push(tournament);
      }
    }
  }

  async searchStatus(item: String) {
    if (item == this.statusSearchList[0]) {
      this.signedInFilter = false;
      this.statusFilter = TournamentStatus.Open;
      this.statusSearch = this.statusSearchList[0];
    } else if (item == this.statusSearchList[1]) {
      this.signedInFilter = true;
      this.statusFilter = TournamentStatus.Open;
      this.statusSearch = this.statusSearchList[1];
    } else if (item == this.statusSearchList[2]) {
      this.signedInFilter = true;
      this.statusFilter = TournamentStatus.Running;
      this.statusSearch = this.statusSearchList[2];
    }
    await this.getTournaments();
  }

  async refresh() {
    await this.getTournaments();
  }
}
</script>

<style scoped></style>
