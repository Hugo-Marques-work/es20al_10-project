<template>
  <div class="container">
    <h2>Dashboard</h2>
    <div class="stats-container">
      <div
        class="items"
        @click="$router.push('/clarification/list')"
        data-cy="clarificationListButton"
      >
        <div class="icon-wrapper">
          <animated-number :number="clarifications.length" />
        </div>
        <div class="project-name">
          <p>Total Clarification Requests</p>
        </div>
      </div>
      <div
        class="items"
        @click="$router.push('/clarification/list/credited')"
        data-cy="clarificationCreditedButton"
      >
        <div class="icon-wrapper">
          <animated-number :number="clarificationsCredited.length" />
        </div>
        <div class="project-name">
          <p>Total Credited Clarifications</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import RemoteServices from '@/services/RemoteServices';
import AnimatedNumber from '@/components/AnimatedNumber.vue';
import Clarification from '@/models/clarification/Clarification';

@Component({
  components: { AnimatedNumber }
})
export default class ClarificationDashboard extends Vue {
  clarifications: Clarification[] = [];
  clarificationsCredited: Clarification[] = [];

  async created() {
    await this.$store.dispatch('loading');
    try {
      this.clarifications = await RemoteServices.getClarificationsByUser();
      this.clarificationsCredited = await RemoteServices.getCreditedClarificationsByStudent();
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }
}
</script>

<style lang="scss" scoped>
.stats-container {
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  justify-content: center;
  align-items: stretch;
  align-content: center;
  height: 100%;

  .items {
    background-color: rgba(255, 255, 255, 0.75);
    color: #1976d2;
    border-radius: 5px;
    flex-basis: 25%;
    margin: 20px;
    cursor: pointer;
    transition: all 0.6s;
  }
}

.icon-wrapper,
.project-name {
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-wrapper {
  font-size: 100px;
  transform: translateY(0px);
  transition: all 0.6s;
}

.icon-wrapper {
  align-self: end;
}

.project-name {
  align-self: start;
}
.project-name p {
  font-size: 24px;
  font-weight: bold;
  letter-spacing: 2px;
  transform: translateY(0px);
  transition: all 0.5s;
}

.items:hover {
  border: 3px solid black;

  & .project-name p {
    transform: translateY(-10px);
  }
  & .icon-wrapper i {
    transform: translateY(5px);
  }
}
</style>
