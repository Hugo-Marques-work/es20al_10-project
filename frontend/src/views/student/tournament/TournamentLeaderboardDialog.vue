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
          Leaderboard
        </span>
      </v-card-title>
      <v-card-text class="text-left">
        <v-container grid-list-md fluid>
          <v-layout column wrap>
            <v-flex xs24 sm12 md8>
              <h2 v-if="firstPlace().length === 1">
                The Winner of the Tournament
                <strong> {{ tournament.title }} </strong> is: <br />
                <span> {{ firstPlace()[0].user.name }}</span>
              </h2>
              <h2 v-else>
                The Winners of the Tournament
                <strong> {{ tournament.title }} </strong> are: <br />
                <span> {{ firstPlaceNames() }}</span>
              </h2>
            </v-flex>
            <v-simple-table>
              <tbody>
                <tr
                  id="goldPlace"
                  v-for="userBoardPlace in sortedLeaderBoard()"
                  v-bind:key="userBoardPlace.id"
                >
                  <td class="graphArea">
                    <v-progress-linear
                      v-if="userBoardPlace.place === 1"
                      rounded
                      :value="
                        (userBoardPlace.correctAnswers /
                          tournament.numberOfQuestions) *
                          100
                      "
                      disabled
                      color="amber"
                      striped
                      height="25"
                    >
                      <template v-slot="{}">
                        <strong
                          >{{ userBoardPlace.correctAnswers }} /
                          {{ tournament.numberOfQuestions }}</strong
                        >
                      </template>
                    </v-progress-linear>
                    <v-progress-linear
                      v-else
                      rounded
                      :value="
                        (userBoardPlace.correctAnswers /
                          tournament.numberOfQuestions) *
                          100
                      "
                      disabled
                      color="blue"
                      height="25"
                    >
                      <template v-slot="{}">
                        <strong
                          >{{ userBoardPlace.correctAnswers }} /
                          {{ tournament.numberOfQuestions }}</strong
                        >
                      </template>
                    </v-progress-linear>
                  </td>
                  <td>
                    {{ userBoardPlace.user.name }}
                  </td>
                  <td v-if="userBoardPlace.place === 1">
                    <strong>{{ userBoardPlace.place }}º </strong>
                  </td>
                  <td v-else>{{ userBoardPlace.place }}º</td>
                </tr>
              </tbody>
            </v-simple-table>
          </v-layout>
        </v-container>
      </v-card-text>
      <v-card-actions>
        <v-spacer />
        <v-btn
          color="blue darken-1"
          @click="$emit('close-dialog')"
          data-cy="closeLeaderboard"
          >Close</v-btn
        >
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import { Component, Model, Prop, Vue } from 'vue-property-decorator';
import { Tournament, TournamentStatus } from '@/models/tournament/Tournament';
import Topic from '@/models/management/Topic';
import { UserBoardPlace } from '@/models/tournament/UserBoardPlace';
import User from '@/models/user/User';

@Component({
  components: {}
})
export default class TournamentLeaderboard extends Vue {
  @Model('dialog', Boolean) dialog!: boolean;
  @Prop({ type: Tournament, required: true }) readonly tournament!: Tournament;

  async created() {
    await this.$store.dispatch('loading');
    await this.$store.dispatch('clearLoading');
    this.tournament.leaderboard.sort(function(ubp1, ubp2): number {
      if (ubp1.place == ubp2.place) {
        return ubp1.user.name > ubp2.user.name ? 1 : -1;
      }
      return ubp1.place - ubp2.place;
    });
  }

  firstPlaceNames(): String {
    return this.firstPlace()
      .map(ubp => ubp.user.name)
      .join(', ');
  }
  firstPlace(): UserBoardPlace[] {
    let ubps = [];
    for (let ubp of this.tournament.leaderboard) {
      if (ubp.place == 1) {
        ubps.push(ubp);
      }
    }
    return ubps;
  }

  sortedLeaderBoard(): UserBoardPlace[] {
    return this.tournament.leaderboard;
  }
}
</script>

<style scoped>
.graphArea {
  width: 70%;
}
#goldPlace {
  border-color: yellow;
  border-width: 5px;
  margin: 10px;
  padding: 100px;
}
</style>
