import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

const TOKEN_KEY = 'numerology_auth_token';

export interface UserDto {
  id: number;
  email: string;
  emailVerified: boolean;
}

export interface AuthResponse {
  token: string;
  user: UserDto;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  constructor(private http: HttpClient) {}

  get token(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  isLoggedIn(): boolean {
    return !!this.token;
  }

  register(email: string, password: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>('/api/auth/register', { email, password });
  }

  login(email: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>('/api/auth/login', { email, password }).pipe(
      tap((res) => localStorage.setItem(TOKEN_KEY, res.token)),
    );
  }

  verify(token: string): Observable<{ message: string }> {
    return this.http.get<{ message: string }>('/api/auth/verify', { params: { token } });
  }

  resendVerification(email: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>('/api/auth/resend-verification', { email });
  }

  me(): Observable<UserDto> {
    return this.http.get<UserDto>('/api/auth/me');
  }

  changePassword(currentPassword: string, newPassword: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>('/api/auth/change-password', {
      currentPassword,
      newPassword,
    });
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem('numerology_active_conversation');
  }
}
