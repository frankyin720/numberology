/** 與後端 ChatMessageRoles、Spring AI 一致：API 僅使用小寫 */
export const CHAT_ROLE_USER = 'user' as const;
export const CHAT_ROLE_ASSISTANT = 'assistant' as const;

export type ApiChatRole = typeof CHAT_ROLE_USER | typeof CHAT_ROLE_ASSISTANT;

/** 介面展示：assistant → bot */
export function toUiRole(apiRole: string | undefined): 'user' | 'bot' {
  return apiRole?.toLowerCase() === CHAT_ROLE_USER ? 'user' : 'bot';
}
