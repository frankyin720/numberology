import { HttpInterceptorFn } from '@angular/common/http';

const TOKEN_KEY = 'numerology_auth_token';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem(TOKEN_KEY);
  if (
    !token ||
    req.url.startsWith('/api/auth/login') ||
    req.url.startsWith('/api/auth/register') ||
    req.url.startsWith('/api/auth/resend-verification')
  ) {
    return next(req);
  }
  return next(
    req.clone({
      setHeaders: { Authorization: `Bearer ${token}` },
    }),
  );
};
