interface ActionRecord {
  timestamps: number[];
}

const actions = new Map<string, ActionRecord>();

/** Records a destructive action by an executor and returns true if it exceeds the nuke threshold. */
export function recordActionAndCheckNuke(
  guildId: string,
  executorId: string,
  thresholdCount: number,
  windowSeconds: number
): boolean {
  const now = Date.now();
  const windowMs = windowSeconds * 1000;
  const key = `${guildId}:${executorId}`;
  const record = actions.get(key) ?? { timestamps: [] };
  record.timestamps = record.timestamps.filter((t) => now - t < windowMs);
  record.timestamps.push(now);
  actions.set(key, record);
  return record.timestamps.length >= thresholdCount;
}

export function resetNukeTracking(guildId: string, executorId: string) {
  actions.delete(`${guildId}:${executorId}`);
}
