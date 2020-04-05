<template>
    <v-dialog
            :value="dialog"
            @input="$emit('close-dialog')"
            @keydown.esc="$emit('close-dialog')"
            max-width="75%"
            max-height="80%"
    >
        <v-card>
            <v-card-actions>
                <v-spacer />
                <v-btn
                        color="blue darken-1"
                        @click="$emit('close-dialog')"
                        data-cy="cancelButton"
                >Cancel</v-btn
                >
                <v-btn color="blue darken-1" @click="saveCourse" data-cy="saveButton"
                >Save</v-btn
                >
            </v-card-actions>
        </v-card>
    </v-dialog>
</template>

<script lang="ts">
  import { Component, Model, Prop, Vue } from 'vue-property-decorator';
  import RemoteServices from '@/services/RemoteServices';
  import Course from '@/models/user/Course';

  @Component
  export default class EditCourseDialog extends Vue {
    @Model('dialog', Boolean) dialog!: boolean;
    @Prop({ type: Course, required: true }) readonly course!: Course;

    editCourse!: Course;
    isCreateCourse: boolean = false;

    created() {
      this.editCourse = new Course(this.course);
      this.isCreateCourse = !!this.editCourse.name;
    }

    async saveCourse() {
      if (
        this.editCourse &&
        (!this.editCourse.name ||
          !this.editCourse.acronym ||
          !this.editCourse.academicTerm)
      ) {
        await this.$store.dispatch(
          'error',
          'Course must have name, acronym and academicTerm'
        );
        return;
      }

      if (this.editCourse && this.editCourse.courseExecutionId == null) {
        try {
          const result = await RemoteServices.createCourse(this.editCourse);
          this.$emit('new-course', result);
        } catch (error) {
          await this.$store.dispatch('error', error);
        }
      }
    }
  }
</script>
