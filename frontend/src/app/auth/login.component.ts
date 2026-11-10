import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from './auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './auth.component.scss',
})
export class LoginComponent {
  email = '';
  password = '';
  error = '';
  resendMsg = '';
  needsVerification = false;
  loading = false;
  resendLoading = false;

  constructor(
    private auth: AuthService,
    private router: Router,
  ) {}

  submit(): void {
    this.error = '';
    this.resendMsg = '';
    this.needsVerification = false;
    this.loading = true;
    this.auth.login(this.email.trim(), this.password).subscribe({
      next: () => {
        this.loading = false;
        this.router.navigate(['/chat']);
      },
      error: (err) => {
        this.loading = false;
        this.auth.logout();
        const msg = err.error?.message ?? '登入失敗';
        this.error = msg;
        this.needsVerification = err.status === 403 || msg.includes('驗證');
      },
    });
  }

  resend(): void {
    const mail = this.email.trim();
    if (!mail) {
      this.error = '請先輸入信箱';
      return;
    }
    this.resendLoading = true;
    this.resendMsg = '';
    this.auth.resendVerification(mail).subscribe({
      next: (res) => {
        this.resendLoading = false;
        this.resendMsg = res.message;
      },
      error: () => {
        this.resendLoading = false;
        this.error = '發送失敗，請稍後再試';
      },
    });
  }
}
