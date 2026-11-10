import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProfileFormComponent } from './profile-form.component';

@Component({
  selector: 'app-profile-dialog',
  standalone: true,
  imports: [CommonModule, ProfileFormComponent],
  templateUrl: './profile-dialog.component.html',
  styleUrl: './profile-dialog.component.scss',
})
export class ProfileDialogComponent {
  @Input() open = false;
  /** 未完成資料時強制填寫，不可關閉 */
  @Input() required = false;
  @Output() closed = new EventEmitter<void>();
  @Output() saved = new EventEmitter<void>();

  get hint(): string {
    return this.required
      ? '請先補全命理基本資料，保存後即可與陳大師對話。'
      : '更新後保存，陳大師將依最新資料作答。';
  }

  get submitLabel(): string {
    return this.required ? '保存並開始對話' : '保存';
  }

  onBackdropClick(): void {
    if (!this.required) {
      this.close();
    }
  }

  close(): void {
    if (this.required) {
      return;
    }
    this.closed.emit();
  }

  onSaved(): void {
    this.saved.emit();
    this.closed.emit();
  }
}
