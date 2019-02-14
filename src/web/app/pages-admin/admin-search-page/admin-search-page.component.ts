import { Component } from '@angular/core';
import { AccountService } from '../../../services/account.service';
import { StatusMessageService } from '../../../services/status-message.service';
import { AdminSearchResult, InstructorBundle, StudentBundle } from '../../../types/api-output';
import { ErrorMessageOutput } from '../../error-message-output';

/**
 * Admin search page.
 */
@Component({
  selector: 'tm-admin-search-page',
  templateUrl: './admin-search-page.component.html',
  styleUrls: ['./admin-search-page.component.scss'],
})
export class AdminSearchPageComponent {

  searchQuery: string = '';
  instructors: InstructorBundle[] = [];
  students: StudentBundle[] = [];

  constructor(private statusMessageService: StatusMessageService, private accountService: AccountService) {}

  /**
   * Searches for students and instructors matching the search query.
   */
  search(): void {
    const paramMap: { [key: string]: string } = {
      searchkey: this.searchQuery,
    };
    this.accountService.searchAccounts(paramMap).subscribe((resp: AdminSearchResult) => {
      this.instructors = resp.instructors;
      for (const instructor of this.instructors) {
        instructor.showLinks = false;
      }

      this.students = resp.students;
      for (const student of this.students) {
        student.showLinks = false;
      }
    }, (resp: ErrorMessageOutput) => {
      this.statusMessageService.showErrorMessage(resp.error.message);
    });
  }

  /**
   * Shows all instructors' links in the page.
   */
  showAllInstructorsLinks(): void {
    for (const instructor of this.instructors) {
      instructor.showLinks = true;
    }
  }

  /**
   * Hides all instructors' links in the page.
   */
  hideAllInstructorsLinks(): void {
    for (const instructor of this.instructors) {
      instructor.showLinks = false;
    }
  }

  /**
   * Shows all students' links in the page.
   */
  showAllStudentsLinks(): void {
    for (const student of this.students) {
      student.showLinks = true;
    }
  }

  /**
   * Hides all students' links in the page.
   */
  hideAllStudentsLinks(): void {
    for (const student of this.students) {
      student.showLinks = false;
    }
  }

  /**
   * Resets the instructor's Google ID.
   */
  resetInstructorGoogleId(instructor: InstructorBundle, event: any): void {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }

    const paramMap: { [key: string]: string } = {
      courseid: instructor.courseId,
      instructoremail: instructor.email,
    };
    this.accountService.resetAccount(paramMap).subscribe(() => {
      this.search();
      this.statusMessageService.showSuccessMessage('The instructor\'s Google ID has been reset.');
    }, (resp: ErrorMessageOutput) => {
      this.statusMessageService.showErrorMessage(resp.error.message);
    });
  }

  /**
   * Resets the student's Google ID.
   */
  resetStudentGoogleId(student: StudentBundle, event: any): void {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }

    const paramMap: { [key: string]: string } = {
      courseid: student.courseId,
      studentemail: student.email,
    };
    this.accountService.resetAccount(paramMap).subscribe(() => {
      student.googleId = '';
      this.statusMessageService.showSuccessMessage('The student\'s Google ID has been reset.');
    }, (resp: ErrorMessageOutput) => {
      this.statusMessageService.showErrorMessage(resp.error.message);
    });
  }

}
