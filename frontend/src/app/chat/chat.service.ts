import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ChatResponse {
  conversationId: string;
  output: string;
}

@Injectable({ providedIn: 'root' })
export class ChatService {
  constructor(private http: HttpClient) {}

  send(query: string, conversationId: string): Observable<ChatResponse> {
    return this.http.post<ChatResponse>('/api/chat', { query, conversationId });
  }
}
