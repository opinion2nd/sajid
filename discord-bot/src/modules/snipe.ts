interface SnipedMessage {
  content: string;
  authorTag: string;
  authorAvatar: string;
  deletedAt: number;
}

const lastDeleted = new Map<string, SnipedMessage>();
const TTL_MS = 5 * 60_000;

export function recordDeletedMessage(channelId: string, snippet: SnipedMessage) {
  lastDeleted.set(channelId, snippet);
}

export function getSnipe(channelId: string): SnipedMessage | null {
  const entry = lastDeleted.get(channelId);
  if (!entry) return null;
  if (Date.now() - entry.deletedAt > TTL_MS) {
    lastDeleted.delete(channelId);
    return null;
  }
  return entry;
}
