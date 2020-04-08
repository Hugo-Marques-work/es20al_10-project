<template>
  <v-dialog v-model="dialog">
    <v-alert v-model="dialog" type="success" close-text="Close Alert" dismissible>
      {{ confirmationMessage }}
    </v-alert>
  </v-dialog>
</template>

<script lang="ts">
import { Component, Vue, Watch } from 'vue-property-decorator';

@Component
export default class ConfirmationMessage extends Vue {
  dialog: boolean = this.$store.getters.getConfirmation;
  confirmationMessage: string = this.$store.getters.getConfirmationMessage;

  created() {
    this.dialog = this.$store.getters.getConfirmation;
    this.confirmationMessage = this.$store.getters.getConfirmationMessage;
    this.$store.watch(
      (state, getters) => getters.getConfirmation,
      () => {
        this.dialog = this.$store.getters.getConfirmation;
        this.confirmationMessage = this.$store.getters.getConfirmationMessage;
      }
    );
  }

  @Watch('dialog')
  closeError() {
    if (!this.dialog) {
      this.$store.dispatch('clearConfirmation');
    }
  }
}
</script>

<style scoped lang="scss">
.v-dialog__container {
  display: unset !important;
}

.v-alert {
  z-index: 9999;
  position: absolute;
  left: 20px;
  top: 80px;
  width: calc(100% - 40px);
}
</style>
