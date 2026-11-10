import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { AuthService } from './auth.service';

@Component({
  selector: 'app-verify',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './verify.component.html',
  styleUrl: './auth.component.scss',
})
export class VerifyComponent implements OnInit {
  message = '驗證中…';
  ok = false;

  constructor(
    private route: ActivatedRoute,
    private auth: AuthService,
  ) {}

  ngOnInit(): void {
    const token = this.route.snapshot.queryParamMap.get('token');
    if (!token) {
      this.message = '缺少驗證 token';
      return;
    }
    this.auth.verify(token).subscribe({
      next: (res) => {
        this.ok = true;
        this.message = res.message;
      },
      error: (err) => {
        this.message = err.error?.message ?? '驗證失敗';
      },
    });
  }
}
