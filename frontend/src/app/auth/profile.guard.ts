import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { catchError, map, of } from 'rxjs';
import { AuthService } from './auth.service';
import { ProfileService } from '../profile/profile.service';

export const profileCompleteGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const profile = inject(ProfileService);
  const router = inject(Router);
  if (!auth.isLoggedIn()) {
    return router.createUrlTree(['/login']);
  }
  return profile.getStatus().pipe(
    map((s) => (s.complete ? true : router.createUrlTree(['/profile']))),
    catchError(() => of(router.createUrlTree(['/login']))),
  );
};
