import { Routes } from '@angular/router';
import { authGuard, guestGuard } from './auth/auth.guard';
export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'chat' },
  {
    path: 'login',
    loadComponent: () => import('./auth/login.component').then((m) => m.LoginComponent),
    canActivate: [guestGuard],
  },
  {
    path: 'register',
    loadComponent: () => import('./auth/register.component').then((m) => m.RegisterComponent),
    canActivate: [guestGuard],
  },
  {
    path: 'verify',
    loadComponent: () => import('./auth/verify.component').then((m) => m.VerifyComponent),
  },
  { path: 'profile', redirectTo: 'chat', pathMatch: 'full' },
  {
    path: 'chat',
    loadComponent: () => import('./chat/chat-page.component').then((m) => m.ChatPageComponent),
    canActivate: [authGuard],
  },
  { path: '**', redirectTo: 'chat' },
];
