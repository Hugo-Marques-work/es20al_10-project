<template>
  <v-dialog
    :value="tournament"
    @input="$emit('close-dialog')"
    @keydown.esc="$emit('close-dialog')"
    max-width="75%"
    max-height="80%"
  >
    <v-card>
      <v-card-title>
        Are you sure you want to cancel tournament {{ tournament.title }}?
      </v-card-title>

      <v-card-actions>
        <v-spacer />
        <v-btn
          color="blue darken-1"
          @click="$emit('close-dialog')"
          data-cy="cancelButton"
          >No, I don't want to cancel</v-btn
        >
        <v-btn
          color="blue darken-1"
          @click="executeCancel()"
          data-cy="executeCancelButton"
          >Yes, I want to cancel
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import { Component, Model, Prop, Vue } from 'vue-property-decorator';
import RemoteServices from '@/services/RemoteServices';
import { Tournament } from '@/models/management/Tournament';

@Component
export default class CancelTournamentDialog extends Vue {
  @Prop({ type: Tournament, required: true }) readonly tournament!: Tournament;

  async executeCancel() {
    try {
      const result = await RemoteServices.cancel(this.tournament.id);
      this.$emit('canceled', result);
    } catch (error) {
      this.$emit('error');
      await this.$store.dispatch('error', error);
    }
  }
}
</script>
