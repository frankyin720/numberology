import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from './auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './register.component.html',
  styleUrl: './auth.component.scss',
})
export class RegisterComponent {
  email = '';
  password = '';
  confirm = '';
  error = '';
  success = '';
  resendMsg = '';
  loading = false;
  resendLoading = false;

  constructor(
    private auth: AuthService,
    private router: Router,
  ) {}

  submit(): void {
    this.error = '';
    this.success = '';
    if (this.password !== this.confirm) {
      this.error = '兩次密碼不一致';
      return;
    }
    this.loading = true;
    this.auth.register(this.email.trim(), this.password).subscribe({
      next: (res) => {
        this.loading = false;
        this.success = res.message;
      },
      error: (err) => {
        this.loading = false;
        const msg = err.error?.message ?? '註冊失敗';
        this.error = msg;
        this.showResendOnError = msg.includes('已註冊');
      },
    });
  }

  showResendOnError = false;

  resend(): void {
    const mail = this.email.trim();
    if (!mail) {
      this.error = '請先輸入信箱';
      return;
    }
    this.resendMsg = '';
    this.error = '';
    this.resendLoading = true;
    this.auth.resendVerification(mail).subscribe({
      next: (res) => {
        this.resendLoading = false;
        this.resendMsg = res.message;
        this.success = res.message;
        this.showResendOnError = false;
      },
      error: () => {
        this.resendLoading = false;
        this.error = '發送失敗，請稍後再試';
      },
    });
  }

  goLogin(): void {
    this.router.navigate(['/login']);
  }
}
