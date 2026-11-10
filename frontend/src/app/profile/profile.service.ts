import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export type CalendarType = 'SOLAR' | 'LUNAR';
export type Gender = 'MALE' | 'FEMALE';

export interface UserProfileDto {
  fullName: string;
  gender: Gender;
  birthDate: string;
  calendarType: CalendarType;
  birthTime: string;
  birthPlace: string;
  currentResidence: string;
  timezone: string;
}

export interface ProfileStatusResponse {
  complete: boolean;
  profile: Partial<UserProfileDto>;
}

@Injectable({ providedIn: 'root' })
export class ProfileService {
  constructor(private http: HttpClient) {}

  getStatus(): Observable<ProfileStatusResponse> {
    return this.http.get<ProfileStatusResponse>('/api/profile');
  }

  save(profile: UserProfileDto): Observable<UserProfileDto> {
    return this.http.put<UserProfileDto>('/api/profile', profile);
  }
}
