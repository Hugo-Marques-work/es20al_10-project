<template>
  <v-data-table
    :headers="headers"
    :fixed-header="true"
    :items="tournaments"
    :search="search"
    :items-per-page="20"
    class="elevation-1"
  >
    <template v-slot:item.topics="{ item }">
      <v-chip> {{getTopicNames(item)}}</v-chip>
    </template>
    <template v-slot:item.actions="{ item }">
      <v-icon
        small
        @click="signUp(item)"
      >
        signUp
      </v-icon>
    </template>

    <sign-up-for-tournament-dialog
      v-if="currentTournament"
      v-model="signUpForTournamentDialog"
      :course="currentTournament"
      v-on:signUp="onSignUp"
      v-on:close-dialog="onCloseDialog"
    />
  </v-data-table>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import RemoteServices from '@/services/RemoteServices';
import { Tournament } from '@/models/management/Tournament';
import Topic from '@/models/management/Topic';
import Course from '@/models/user/Course';

@Component({
  components: {}
})
export default class TournamentView extends Vue {
  tournaments: Tournament[] = [];
  signUpForTournamentDialog: boolean = false;
  currentTournament: Tournament | null = null;
  search: string = '';
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
      align: 'center',
      sortable: false,
      width: '10%'
    },
    {
      text: 'Topics',
      value: 'topics',
      align: 'center',
      sortable: false,
      width: '20%'
    }
  ];

  async created() {
    await this.$store.dispatch('loading');
    try {
      this.tournaments = await RemoteServices.getTournaments();
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  onCloseDialog() {
    this.signUpForTournamentDialog = false;
    this.currentTournament= null;
  }

  getTopicNames(topicItems: any) : String {
    let result = '';
    console.log(topicItems.topics);
    topicItems.topics.forEach(function(topic: Topic) {
      result += topic.name + ', ';
    });
    result = result.substring(0, result.length - 2);
    console.log(result);
    return result;
  }

  onSignUp() {
    this.onCloseDialog();
  }
}
</script>

<style scoped></style>
