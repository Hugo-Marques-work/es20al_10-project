<template>
  <v-data-table
    :headers="headers"
    :fixed-header="true"
    :items="tournaments"
    :search="search"
    :items-per-page="20"
    class="elevation-1"
  ></v-data-table>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import RemoteServices from '@/services/RemoteServices';
import { Tournament } from '@/models/management/Tournament';

@Component({
  components: {}
})
export default class TournamentView extends Vue {
  tournaments: Tournament[] = [];

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
      width: '20%'
    },
    {
      text: 'Conclusion Date',
      value: 'conclusionDate',
      align: 'center',
      width: '20%'
    },
    {
      text: 'Creator',
      value: 'creator.name',
      align: 'center',
      width: '10%'
    },
    {
      text: 'Topics',
      value: 'topics',
      align: 'center',
      sortable: false,
      width: '30%'
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
}
</script>

<style scoped></style>