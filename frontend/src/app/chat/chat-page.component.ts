import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../auth/auth.service';
import { ChatService } from './chat.service';
import { toUiRole } from './chat-message-roles';
import { ConversationDto, ConversationService } from './conversation.service';
import { ProfileDialogComponent } from '../profile/profile-dialog.component';
import { ProfileService } from '../profile/profile.service';

const ACTIVE_CONVERSATION_KEY = 'numerology_active_conversation';

const WELCOME = {
  role: 'bot' as const,
  text: '老夫陳玉樓，精通陰陽五行，能算命、占卜、解夢、風水。請問閣下有何指教？',
};

type UiMessage = { role: 'bot' | 'user'; text: string };

@Component({
  selector: 'app-chat-page',
  standalone: true,
  imports: [CommonModule, FormsModule, ProfileDialogComponent],
  templateUrl: './chat-page.component.html',
  styleUrl: './chat-page.component.scss',
})
export class ChatPageComponent implements OnInit {
  conversations: ConversationDto[] = [];
  activeConversationId: string | null = null;
  messages: UiMessage[] = [{ ...WELCOME }];
  userInput = '';
  loading = false;
  listLoading = false;
  error = '';
  editingConversationId: string | null = null;
  editingTitle = '';
  private renameSuppressBlurCommit = false;
  profileOpen = false;
  profileRequired = false;

  constructor(
    private auth: AuthService,
    private conversationsApi: ConversationService,
    private chat: ChatService,
    private profile: ProfileService,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.auth.me().subscribe({
      error: () => this.logout(),
    });
    this.loadConversations();
    this.profile.getStatus().subscribe({
      next: (s) => {
        if (!s.complete) {
          this.profileRequired = true;
          this.profileOpen = true;
        }
      },
    });
  }

  private refreshConversationList(): void {
    this.conversationsApi.list().subscribe({
      next: (list) => (this.conversations = list),
    });
  }

  loadConversations(selectId?: string | null): void {
    this.listLoading = true;
    this.conversationsApi.list().subscribe({
      next: (list) => {
        this.conversations = list;
        this.listLoading = false;
        const saved = selectId ?? localStorage.getItem(ACTIVE_CONVERSATION_KEY);
        const target =
          list.find((c) => c.id === saved)?.id ?? list[0]?.id ?? null;
        if (target) {
          this.selectConversation(target);
        } else {
          this.newConversation();
        }
      },
      error: () => {
        this.listLoading = false;
        this.error = '無法載入會話列表';
      },
    });
  }

  selectConversation(id: string): void {
    if (this.loading) {
      return;
    }
    this.activeConversationId = id;
    localStorage.setItem(ACTIVE_CONVERSATION_KEY, id);
    this.error = '';
    this.messages = [{ ...WELCOME }];
    this.conversationsApi.messages(id).subscribe({
      next: (res) => {
        if (res.messages?.length) {
          this.messages = res.messages.map((m) => ({
            role: toUiRole(m.role),
            text: m.content,
          }));
        }
      },
      error: () => {
        this.error = '無法載入訊息';
      },
    });
  }

  newConversation(): void {
    if (this.loading) {
      return;
    }
    this.conversationsApi.create().subscribe({
      next: (c) => {
        this.conversations = [c, ...this.conversations];
        this.selectConversation(c.id);
      },
      error: () => {
        this.error = '無法建立新會話';
      },
    });
  }

  startRename(event: Event, c: ConversationDto): void {
    event.stopPropagation();
    if (this.loading) {
      return;
    }
    this.editingConversationId = c.id;
    this.editingTitle = c.title;
  }

  onRenameBlur(event: Event, id: string): void {
    if (this.renameSuppressBlurCommit) {
      this.renameSuppressBlurCommit = false;
      return;
    }
    if (this.editingConversationId !== id) {
      return;
    }
    this.commitRename(event, id);
  }

  commitRename(event: Event, id: string): void {
    event.stopPropagation();
    if (this.editingConversationId !== id) {
      return;
    }
    const title = this.editingTitle.trim();
    if (!title || title === this.conversations.find((c) => c.id === id)?.title) {
      this.cancelRename();
      return;
    }
    this.conversationsApi.rename(id, title).subscribe({
      next: (updated) => {
        this.conversations = this.conversations.map((c) => (c.id === id ? updated : c));
        this.cancelRename();
      },
      error: () => {
        this.error = '重新命名失敗';
        this.cancelRename();
      },
    });
  }

  onRenameKeydown(event: KeyboardEvent, id: string): void {
    if (event.key === 'Enter') {
      event.preventDefault();
      this.renameSuppressBlurCommit = true;
      this.commitRename(event, id);
    } else if (event.key === 'Escape') {
      event.preventDefault();
      this.renameSuppressBlurCommit = true;
      this.cancelRename();
    }
  }

  cancelRename(): void {
    this.editingConversationId = null;
    this.editingTitle = '';
  }

  deleteConversation(event: Event, c: ConversationDto): void {
    event.stopPropagation();
    if (this.loading) {
      return;
    }
    const ok = window.confirm(`確定要刪除會話「${c.title}」嗎？此操作無法復原。`);
    if (!ok) {
      return;
    }
    const id = c.id;
    this.conversationsApi.delete(id).subscribe({
      next: () => {
        this.conversations = this.conversations.filter((c) => c.id !== id);
        if (this.activeConversationId === id) {
          localStorage.removeItem(ACTIVE_CONVERSATION_KEY);
          this.activeConversationId = null;
          if (this.conversations.length) {
            this.selectConversation(this.conversations[0].id);
          } else {
            this.messages = [{ ...WELCOME }];
            this.newConversation();
          }
        }
      },
    });
  }

  onEnterKey(event: Event): void {
    const e = event as KeyboardEvent;
    if (e.shiftKey || e.isComposing) {
      return;
    }
    e.preventDefault();
    this.send();
  }

  send(): void {
    const query = this.userInput.trim();
    if (!query || this.loading || !this.activeConversationId) {
      return;
    }
    this.messages.push({ role: 'user', text: query });
    this.userInput = '';
    this.loading = true;
    this.error = '';

    this.chat.send(query, this.activeConversationId).subscribe({
      next: (res) => {
        this.messages.push({ role: 'bot', text: res.output });
        this.loading = false;
        this.refreshConversationList();
      },
      error: () => {
        this.error = '送出失敗，請確認後端與登入狀態';
        this.loading = false;
      },
    });
  }

  logout(): void {
    this.auth.logout();
    this.router.navigate(['/login']);
  }

  goProfile(): void {
    this.profileRequired = false;
    this.profileOpen = true;
  }

  onProfileClosed(): void {
    this.profileOpen = false;
  }

  onProfileSaved(): void {
    this.profileRequired = false;
    this.profileOpen = false;
  }
}
