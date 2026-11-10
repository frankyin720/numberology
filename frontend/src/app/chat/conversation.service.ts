import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ConversationDto {
  id: string;
  title: string;
  createdAt: string;
  updatedAt: string;
}

export interface ChatMessageDto {
  /** 固定小寫：user | assistant（與後端 ChatMessageRoles 一致） */
  role: 'user' | 'assistant';
  content: string;
}

export interface MessageResponse {
  conversationId: string;
  messages: ChatMessageDto[];
}

@Injectable({ providedIn: 'root' })
export class ConversationService {
  constructor(private http: HttpClient) {}

  list(): Observable<ConversationDto[]> {
    return this.http.get<ConversationDto[]>('/api/conversations');
  }

  create(): Observable<ConversationDto> {
    return this.http.post<ConversationDto>('/api/conversations', {});
  }

  messages(conversationId: string): Observable<MessageResponse> {
    return this.http.get<MessageResponse>(`/api/conversations/${conversationId}/messages`);
  }

  delete(conversationId: string): Observable<void> {
    return this.http.delete<void>(`/api/conversations/${conversationId}`);
  }

  rename(conversationId: string, title: string): Observable<ConversationDto> {
    return this.http.patch<ConversationDto>(`/api/conversations/${conversationId}`, { title });
  }
}
