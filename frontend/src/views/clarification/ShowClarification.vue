<template>
  <div>
    <span v-html="convertMarkDown(clarification.content)" />
    <br />
    <br />
    <div v-if="hasAnswers">
      <!--      TODO: Improve title and text-->
      <span class="headline">Answer</span>
      <ul>
        <li v-for="answer in answers" :key="answer.id">
          <span v-html="convertMarkDown(answer.content)" />
        </li>
      </ul>
    </div>
    <div v-else>
      <b><span v-html="convertMarkDown('No answers')"/></b>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue, Prop, Watch } from 'vue-property-decorator';
import { convertMarkDown } from '@/services/ConvertMarkdownService';
import Image from '@/models/management/Image';
import Clarification from '@/models/clarification/Clarification';
import ClarificationAnswer from '@/models/clarification/ClarificationAnswer';
import RemoteServices from '@/services/RemoteServices';

@Component
export default class ShowClarification extends Vue {
  @Prop({ type: Clarification, required: true })
  readonly clarification!: Clarification;
  answers: ClarificationAnswer[] = [];
  hasAnswers: boolean = false;

  @Watch('clarification')
  async onChildChanged(val: Clarification, oldVal: Clarification) {
    await this.$store.dispatch('loading');
    try {
      this.answers = await RemoteServices.getClarificationAnswers(
        this.clarification
      );
      this.hasAnswers = this.answers.length != 0;
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  // async created() {
  //   await this.$store.dispatch('loading');
  //   try {
  //     this.answers = await RemoteServices.getClarificationAnswers(
  //       this.clarification
  //     );
  //     if (this.answers.length != 0) this.hasAnswers = true;
  //   } catch (error) {
  //     await this.$store.dispatch('error', error);
  //   }
  //   await this.$store.dispatch('clearLoading');
  // }

  convertMarkDown(text: string, image: Image | null = null): string {
    return convertMarkDown(text, image);
  }
}
</script>
