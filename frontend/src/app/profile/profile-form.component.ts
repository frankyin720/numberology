import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../auth/auth.service';
import { CalendarType, Gender, ProfileService, UserProfileDto } from './profile.service';
import {
  BIRTH_TIME_SLOTS,
  DEFAULT_BIRTH_TIME,
  birthTimeSelectLabel,
  resolveBirthTimeValue,
} from './birth-time-options';
import { buildTimezoneOptions, DEFAULT_TIMEZONE } from './timezone-options';

@Component({
  selector: 'app-profile-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './profile-form.component.html',
  styleUrls: ['./profile-form.component.scss'],
})
export class ProfileFormComponent implements OnInit {
  @Input() hint = '請填寫命理諮詢所需基本資料，保存後即可與陳大師對話。';
  @Input() submitLabel = '保存';
  @Output() saved = new EventEmitter<void>();

  model: UserProfileDto = {
    fullName: '',
    gender: 'MALE',
    birthDate: '',
    calendarType: 'SOLAR',
    birthTime: DEFAULT_BIRTH_TIME,
    birthPlace: '',
    currentResidence: '',
    timezone: DEFAULT_TIMEZONE,
  };
  timezoneOptions = buildTimezoneOptions();
  readonly birthTimeSlots = BIRTH_TIME_SLOTS;
  readonly birthTimeLabel = birthTimeSelectLabel;
  loading = false;
  error = '';
  email = '';
  showPasswordForm = false;
  currentPassword = '';
  newPassword = '';
  confirmPassword = '';
  passwordLoading = false;
  passwordError = '';
  passwordSuccess = '';

  readonly today = new Date().toISOString().slice(0, 10);

  constructor(
    private profile: ProfileService,
    private auth: AuthService,
  ) {}

  ngOnInit(): void {
    this.loadProfile();
  }

  loadEmail(): void {
    this.auth.me().subscribe({
      next: (u) => (this.email = u.email),
      error: () => (this.email = ''),
    });
  }

  togglePasswordForm(): void {
    this.showPasswordForm = !this.showPasswordForm;
    this.passwordError = '';
    this.passwordSuccess = '';
    this.currentPassword = '';
    this.newPassword = '';
    this.confirmPassword = '';
  }

  changePassword(): void {
    this.passwordError = '';
    this.passwordSuccess = '';
    if (this.newPassword !== this.confirmPassword) {
      this.passwordError = '兩次輸入的新密碼不一致';
      return;
    }
    if (this.newPassword.length < 8) {
      this.passwordError = '新密碼至少 8 個字元';
      return;
    }
    this.passwordLoading = true;
    this.auth.changePassword(this.currentPassword, this.newPassword).subscribe({
      next: () => {
        this.passwordLoading = false;
        this.passwordSuccess = '密碼已更新';
        this.currentPassword = '';
        this.newPassword = '';
        this.confirmPassword = '';
        this.showPasswordForm = false;
      },
      error: (err) => {
        this.passwordLoading = false;
        this.passwordError = err.error?.message ?? '密碼更新失敗';
      },
    });
  }

  loadProfile(): void {
    this.loadEmail();
    this.profile.getStatus().subscribe({
      next: (res) => {
        if (res.profile) {
          this.model = {
            fullName: res.profile.fullName ?? '',
            gender: (res.profile.gender as Gender) ?? 'MALE',
            birthDate: res.profile.birthDate ?? '',
            calendarType: (res.profile.calendarType as CalendarType) ?? 'SOLAR',
            birthTime: resolveBirthTimeValue(res.profile.birthTime),
            birthPlace: res.profile.birthPlace ?? '',
            currentResidence: res.profile.currentResidence ?? '',
            timezone: res.profile.timezone ?? DEFAULT_TIMEZONE,
          };
          this.timezoneOptions = buildTimezoneOptions(this.model.timezone);
        }
      },
    });
  }

  submit(): void {
    this.error = '';
    this.loading = true;
    this.profile.save(this.model).subscribe({
      next: () => {
        this.loading = false;
        this.saved.emit();
      },
      error: (err) => {
        this.loading = false;
        this.error = err.error?.message ?? '保存失敗';
      },
    });
  }
}
