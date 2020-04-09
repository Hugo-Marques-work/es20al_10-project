<template>
  <v-dialog
    :value="dialog"
    @input="$emit('close-dialog')"
    @keydown.esc="$emit('close-dialog')"
    max-width="75%"
    max-height="80%"
  >
    <v-card>
      <!-- INFO -->
      <v-card-title v-if="!isSignedUp">
        Are you sure you want to sign up to tournament {{ tournament.title }}?
      </v-card-title>
      <v-card-title v-if="isSignedUp">
        Already signed up
      </v-card-title>

      <!-- SIGNED UP ACTIONS -->
      <v-card-actions v-if="isSignedUp">
        <v-spacer />
        <v-btn
          color="blue darken-1"
          @click="$emit('close-dialog')"
          data-cy="okButton"
          >Ok</v-btn
        >
      </v-card-actions>
      <!-- NOT SIGN UP ACTIONS -->
      <v-card-actions v-if="!isSignedUp">
        <v-spacer />
        <v-btn
          color="blue darken-1"
          @click="$emit('close-dialog')"
          data-cy="cancelButton"
          >Cancel</v-btn
        >
        <v-btn
          color="blue darken-1"
          @click="executeSignUp()"
          data-cy="executeSignUpButton"
          >Sign Up
        </v-btn>

        <!-- FUTURE SIGN OFF
        <v-btn
          v-if="isSignedUp"
          color="blue darken-1"
          @click="executeSignOff()"
          data-cy="executeSignUpButton"
          >Sign Off
        </v-btn>
        -->
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import { Component, Model, Prop, Vue } from 'vue-property-decorator';
import RemoteServices from '@/services/RemoteServices';
import { Tournament } from '@/models/management/Tournament';
import User from '@/models/user/User';
import { SimpleUser } from '@/models/user/SimpleUser';

@Component
export default class SignUpForTournamentDialog extends Vue {
  @Model('dialog', Boolean) dialog!: boolean;
  @Prop({ type: Tournament, required: true }) readonly tournament!: Tournament;

  editTournament!: Tournament;
  isSignedUp: boolean = false;

  created() {
    this.editTournament = this.tournament;
    this.isSignedUp = false;
    for (let i in this.editTournament.signedUpUsers) {
      let user = this.editTournament.signedUpUsers[i];
      if (user.name == this.$store.getters.getUser.name) {
        this.isSignedUp = true;
        break;
      }
    }
  }

  async executeSignUp() {
    if (this.isSignedUp) {
      await this.$store.dispatch('error', 'can\'t sign in');
    }

    try {
      const result = await RemoteServices.signUp(this.tournament.id);

      this.$emit('signedUp', result);
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
  }
  /* FUTURE SIGN OFF
  async executeSignOff() {
    if (!this.isSignedUp) {
      await this.$store.dispatch('error', 'can\'t sign off');
    }

    try {
      const result = await RemoteServices.createCourse(this.editTournament);
      this.$emit('new-course', result);
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
  }*/
}
</script>
